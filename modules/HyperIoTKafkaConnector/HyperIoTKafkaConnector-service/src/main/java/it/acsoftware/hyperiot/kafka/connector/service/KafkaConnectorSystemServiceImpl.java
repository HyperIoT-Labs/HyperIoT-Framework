/*
 * Copyright 2019-2023 HyperIoT
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package it.acsoftware.hyperiot.kafka.connector.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.acsoftware.hyperiot.base.service.HyperIoTBaseSystemServiceImpl;
import it.acsoftware.hyperiot.base.util.HyperIoTConstants;
import it.acsoftware.hyperiot.base.util.HyperIoTThreadFactoryBuilder;
import it.acsoftware.hyperiot.base.util.HyperIoTUtil;
import it.acsoftware.hyperiot.kafka.connector.api.*;
import it.acsoftware.hyperiot.kafka.connector.consumer.KafkaConsumerThread;
import it.acsoftware.hyperiot.kafka.connector.consumer.KafkaGloabalNotifier;
import it.acsoftware.hyperiot.kafka.connector.model.ConnectorConfig;
import it.acsoftware.hyperiot.kafka.connector.model.HyperIoTKafkaMessage;
import it.acsoftware.hyperiot.kafka.connector.model.HyperIoTKafkaPermission;
import it.acsoftware.hyperiot.kafka.connector.model.KafkaConnector;
import it.acsoftware.hyperiot.kafka.connector.producer.KafkaProducerPoolImpl;
import it.acsoftware.hyperiot.kafka.connector.util.HyperIoTKafkaConnectorConstants;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.acl.AccessControlEntry;
import org.apache.kafka.common.acl.AccessControlEntryFilter;
import org.apache.kafka.common.acl.AclBinding;
import org.apache.kafka.common.acl.AclBindingFilter;
import org.apache.kafka.common.resource.ResourcePattern;
import org.apache.kafka.common.resource.ResourcePatternFilter;
import org.apache.kafka.common.resource.ResourceType;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Aristide Cittadino
 * Implementation class of the KafkaConnectorSystemApi interface.
 * This class is used to implements all additional methods
 * to interact with the persistence layer.
 */
@Component(service = KafkaConnectorSystemApi.class, immediate = true, servicefactory = false)
public final class KafkaConnectorSystemServiceImpl extends HyperIoTBaseSystemServiceImpl
        implements KafkaConnectorSystemApi {

    private final String KAFKA_CONNECT_SERVICE_PATH = "/connectors";
    private final String CONNECTOR_ADD_TEMPLATE = "{\"name\":\"%s\", \"config\": {\"max.poll.interval.ms\":\"500\", \"connector.class\":\"\",\"tasks.max\":\"1\"}}";

    private List<KafkaConsumerThread> kcts;
    private Producer<byte[], byte[]> producer;
    private int kafkaSystemConsumerMaxThreads;

    private AdminClient adminClient;

    private ExecutorService executor;
    private ExecutorService notifierExecutor;

    private BundleContext ctx;

    private Properties consumerProperties;
    private Properties systemConsumerProperties;
    private Properties producerProperties;
    private Properties adminProperties;
    private Properties connectorProperties;

    private String kafkaConnectUrl;

    // for reactiveStreaming
    private Scheduler reactorScheduler;

    public KafkaConnectorSystemServiceImpl() {
        super();
        this.consumerProperties = new Properties();
        this.systemConsumerProperties = new Properties();
        this.producerProperties = new Properties();
        this.adminProperties = new Properties();
        this.connectorProperties = new Properties();
    }

    /**
     * This method is used if you want register manually an instance as
     * KafkaMessageReceiver and not with @Component annotation.
     *
     * @param receiver Kafka Message Receiver
     * @param topics   Kafka topics on which receiver should receive messages
     * @param props    OSGi properties
     */
    @Override
    public ServiceRegistration<KafkaMessageReceiver> registerKafkaMessageReceiver(
            KafkaMessageReceiver receiver, List<String> topics, Dictionary<String, Object> props) {
        props.put(HyperIoTKafkaConnectorConstants.HYPERIOT_KAFKA_OSGI_TOPIC_FILTER, topics);
        return ctx.registerService(KafkaMessageReceiver.class, receiver, props);
    }

    /**
     * This method is used if you want register manually an instance as
     * KafkaMessageReceiver and not with @Component annotation.
     *
     * @param registration Kafka Message Receiver
     */
    @Override
    public void unregisterKafkaMessageReceiver(
            ServiceRegistration<KafkaMessageReceiver> registration) {
        registration.unregister();
    }

    /**
     * Method executed on bundle activation. It start a Kafka consumer thread
     * (single thread). When a new message is incoming the thread will notify OSGi
     * components registered to that topic.
     *
     * @param properties Properties
     */
    @Activate
    public void activate(Map<String, Object> properties, BundleContext context) {
        getLog().info("activating Kafka Connector with properties: {}", properties);
        this.ctx = context;
        try {
            this.loadKafkaConfiguration(HyperIoTUtil.getBundleContext(this));
            this.adminClient = AdminClient.create(adminProperties);
            this.kafkaSystemConsumerMaxThreads = Integer.parseInt(connectorProperties.getProperty(HyperIoTKafkaConnectorConstants.HYPERIOT_KAFKA_SYSTEM_CONSUMER_MAX_THREADS, "50"));
            List<String> basicTopics = this.createBasicTopics();
            this.startConsumingFromKafka(basicTopics);
            int reactorMaxThreads = Integer.parseInt(connectorProperties.getProperty(HyperIoTKafkaConnectorConstants.HYPERIOT_KAFKA_REACTOR_PROP_MAX_CONSUMER_THREADS, "1"));
            this.reactorScheduler = Schedulers.fromExecutor(Executors.newFixedThreadPool(reactorMaxThreads));
        } catch (Throwable t) {
            getLog().error(t.getMessage(), t);
        }
    }

    /**
     * On Deactivation the consumer thread is stopped
     *
     * @param ctx Bundle Context
     */
    @Deactivate
    public void deactivate(BundleContext ctx) {
        getLog().info("deactivating Kafka Connector....");
        this.stopConsumingFromKafka();
        this.reactorScheduler.dispose();
        this.notifierExecutor.shutdown();
        this.ctx = null;
    }

    /**
     * Method for stopping kafka consumer thread
     */
    @Override
    public void stopConsumingFromKafka() {
        getLog().debug("Stopping admin client from Kafka...");
        try {
            // close call is blocking, so better to put it inside a thread
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    adminClient.close(20, TimeUnit.SECONDS);
                }
            };
            Thread t = new Thread(r);
            t.start();
        } catch (Exception e) {
            getLog().warn(e.getMessage(), e);
        }
        getLog().debug("Stopping consuming from Kafka...");
        kcts.stream().forEach(kct -> kct.stop());
        executor.shutdown();
    }

    /**
     * Method for starting consuming from Kafka. Note: the component starts
     * automatically consuming from kafka at bundle activation
     */
    @Override
    public void startConsumingFromKafka(List<String> basicTopics) {
        getLog().debug(
                "Activating bundle Kafka Connector, creating kafka thread with this topics: {}"
                , basicTopics.toString());
        if (consumerProperties != null && consumerProperties.size() > 0) {
            try {
                consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, Class
                        .forName("org.apache.kafka.common.serialization.ByteArrayDeserializer"));
                consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, Class
                        .forName("org.apache.kafka.common.serialization.ByteArrayDeserializer"));
                //With this group ID Broadcast messaging is working, but only one thread will receive data on each node
                consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG,
                        "hyperiot_" + HyperIoTUtil.getLayer() + "_" + HyperIoTUtil.getNodeId());

                ThreadFactory tf = HyperIoTThreadFactoryBuilder.build("hyperiot-kafka-system-consumer-%d", false);
                ThreadFactory tfNotifier = HyperIoTThreadFactoryBuilder.build("hyperiot-kafka-system-consumer-message-notifier-%d", false);
                executor = Executors.newFixedThreadPool(this.kafkaSystemConsumerMaxThreads, tf);
                notifierExecutor = Executors.newFixedThreadPool(this.kafkaSystemConsumerMaxThreads, tfNotifier);
                kcts = new ArrayList<>();
                for (int i = 0; i < this.kafkaSystemConsumerMaxThreads; i++) {
                    Properties threadProperties = new Properties();
                    threadProperties.putAll(consumerProperties);
                    threadProperties.put(ConsumerConfig.CLIENT_ID_CONFIG, HyperIoTUtil.getLayer() + "_" + HyperIoTUtil.getNodeId() + "-" + i);
                    KafkaConsumerThread kct = new KafkaConsumerThread(threadProperties, systemConsumerProperties, ctx, basicTopics, notifierExecutor);
                    kcts.add(kct);
                    executor.submit(kct);
                }
            } catch (Exception e) {
                getLog().error(e.getMessage(), e);
            }
        } else {
            getLog().error("No Kafka Properties found, Consumer thread did not start!");
        }
    }

    /**
     * Method which produces a message on Kafka and invoke callback
     */
    @Override
    public void produceMessage(HyperIoTKafkaMessage message, Callback callback) {
        if (producerProperties != null && producerProperties.size() > 0) {
            if (this.producer == null) {
                this.producer = getNewProducer();
            }
            this.produceMessage(message, this.producer, callback);
        }
    }

    public void produceMessage(HyperIoTKafkaMessage message, Producer<byte[], byte[]> producer, Callback callback) {
        ProducerRecord<byte[], byte[]> record = null;
        if (message.getPartition() >= 0) {
            record = new ProducerRecord<>(message.getTopic(), message.getPartition(),
                    message.getKey(), message.getPayload());
        } else {
            record = new ProducerRecord<>(message.getTopic(),
                    message.getKey(), message.getPayload());
        }
        final long beforeSendTime = System.currentTimeMillis();
        if (callback != null) {
            producer.send(record, callback);
            if (getLog().isDebugEnabled()) {
                getLog().debug("Produced message in {} ", (System.currentTimeMillis() - beforeSendTime));
            }

        } else
            producer.send(record, new Callback() {
                @Override
                public void onCompletion(RecordMetadata metadata, Exception exception) {
                    if (getLog().isDebugEnabled()) {
                        getLog().debug("Produced message in {} ", (System.currentTimeMillis() - beforeSendTime));
                    }

                    if (exception != null)
                        getLog().error(exception.getMessage(), exception);
                    else
                        getLog().debug(metadata.toString());
                }
            });
    }

    @Override
    public KafkaProducer<byte[], byte[]> getNewProducer() {
        return this.getNewProducer(null);
    }

    @Override
    public KafkaProducerPool getNewProducerPool(int poolSize) {
        return new KafkaProducerPoolImpl(poolSize, this);
    }

    /**
     * @return New Kafka Producer
     */
    @Override
    public KafkaProducer<byte[], byte[]> getNewProducer(String clientId) {
        KafkaProducer<byte[], byte[]> newProducer = null;
        if (producerProperties != null && producerProperties.size() > 0) {
            Properties props = new Properties();
            props.putAll(producerProperties);
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                    ByteArraySerializer.class.getName());
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                    ByteArraySerializer.class.getName());
            if (clientId != null)
                props.put("client.id", clientId);
            ClassLoader karafClassLoader = Thread.currentThread().getContextClassLoader();
            ClassLoader thisClassLoader = this.getClass().getClassLoader();
            Thread.currentThread().setContextClassLoader(thisClassLoader);
            try {
                newProducer = new KafkaProducer<>(props);
            } catch (Exception e) {
                getLog().error(e.getMessage(), e);
            } finally {
                Thread.currentThread().setContextClassLoader(karafClassLoader);
            }
        }
        return newProducer;
    }

    /**
     * @Return Flux for reactive consuming from Kafka
     */
    @SuppressWarnings("rawtypes")
    @Override
    public Flux<ReceiverRecord<byte[], byte[]>> consumeReactive(String kafkaGroupId, List<String> topics, List<Integer> partitions, long pollTime, Class keyDeserializerClass, Class valueDeserializerClass, KafkaPartitionAssignListener assignListener, KafkaPartitionRevokeListener revokeListener) {
        ReceiverOptions<byte[], byte[]> options = this.createReceiverOptionsForTopics(kafkaGroupId, topics, partitions, pollTime, keyDeserializerClass, valueDeserializerClass, assignListener, revokeListener);
        if (options != null) {
            Flux<ReceiverRecord<byte[], byte[]>> kafkaFlux = KafkaReceiver.create(options).receive().subscribeOn(reactorScheduler);
            return kafkaFlux;
        }
        return null;
    }

    /**
     * @Return Flux for reactive consuming from Kafka
     */
    @SuppressWarnings("rawtypes")
    @Override
    public Flux<ReceiverRecord<byte[], byte[]>> consumeReactive(String kafkaGroupId, List<String> topics, long pollTime, Class keyDeserializerClass, Class valueDeserializerClass) {
        return this.consumeReactive(kafkaGroupId, topics, null, pollTime, keyDeserializerClass, valueDeserializerClass,
                partitions -> getLog().debug("Consumer Reactive onPartitionsAssigned {}", partitions),
                partitions -> getLog().debug("Consumer Reactive onPartitionsRevoked {}", partitions));
    }

    /**
     * @Return Flux for reactive consuming from Kafka
     */
    @SuppressWarnings("rawtypes")
    @Override
    public Flux<ReceiverRecord<byte[], byte[]>> consumeReactive(String kafkaGroupId, List<String> topics, long pollTime, Class keyDeserializerClass, Class valueDeserializerClass, KafkaPartitionAssignListener assignListener, KafkaPartitionRevokeListener revokeListener) {
        return this.consumeReactive(kafkaGroupId, topics, null, pollTime, keyDeserializerClass, valueDeserializerClass, assignListener, revokeListener);
    }

    /**
     * @Return Flux for reactive consuming from Kafka
     */
    @SuppressWarnings("rawtypes")
    @Override
    public Flux<ReceiverRecord<byte[], byte[]>> consumeReactive(String kafkaGroupId, String topic, int partition, long pollTime, Class keyDeserializerClass, Class valueDeserializerClass) throws ClassNotFoundException {
        List<String> topics = Collections.singletonList(topic);
        List<Integer> partitions = Collections.singletonList(partition);
        return consumeReactive(kafkaGroupId, topics, partitions, pollTime, keyDeserializerClass, valueDeserializerClass,
                partitionsListener -> getLog().debug("Consumer Reactive onPartitionsAssigned {}", partitions),
                partitionsListener -> getLog().debug("Consumer Reactive onPartitionsRevoked {}", partitions));
    }

    /**
     * This method should be called once for each topic pattern
     *
     * @Return Flux for reactive consuming from Kafka
     */
    @SuppressWarnings("rawtypes")
    @Override
    public void consumeReactiveAsSystem(String kafkaGroupId, Pattern topicPattern, long pollTime, Class keyDeserializerClass, Class valueDeserializerClass) throws ClassNotFoundException {
        ReceiverOptions<byte[], byte[]> receiverOptions = this.createReceiverOptionsForTopicPattern(kafkaGroupId, topicPattern, pollTime, keyDeserializerClass, valueDeserializerClass, null, null);
        KafkaReceiver.create(receiverOptions)
                .receive().subscribeOn(reactorScheduler).parallel().runOn(Schedulers.elastic()).subscribe(m -> {
                    String topic = m.topic();
                    byte[] key = m.key();
                    int partition = m.partition();
                    HyperIoTKafkaMessage message = HyperIoTKafkaMessage.from(topic, key, m.value(), partition);
                    getLog().debug("Got message from kafka: {} on partition {} with key {}", new Object[]{message.toString(), partition, new String(m.key())});
                    KafkaGloabalNotifier.notifyKafkaMessage(message);
                });
    }

    /**
     * This method should be called once for each topic pattern
     *
     * @Return Flux for reactive consuming from Kafka
     */
    @SuppressWarnings("rawtypes")
    @Override
    public void consumeReactiveAsSystem(String kafkaGroupId, String topic, long pollTime, Class keyDeserializerClass, Class valueDeserializerClass) throws ClassNotFoundException {
        ReceiverOptions<byte[], byte[]> receiverOptions = this.createReceiverOptionsForTopics(kafkaGroupId, Collections.singletonList(topic), null, pollTime, keyDeserializerClass, valueDeserializerClass, null, null);
        KafkaReceiver.create(receiverOptions)
                .receive().subscribeOn(reactorScheduler).parallel().runOn(Schedulers.elastic()).subscribe(m -> {
                    byte[] key = m.key();
                    int partition = m.partition();
                    HyperIoTKafkaMessage message = HyperIoTKafkaMessage.from(topic, key, m.value(), partition);
                    getLog().debug("Got message from kafka: {} on partition {} with key {}", new Object[]{message.toString(), partition, new String(m.key())});
                    KafkaGloabalNotifier.notifyKafkaMessage(message);
                });
    }


    /**
     * Method which produces a message on Kafka without callback
     */
    @Override
    public void produceMessage(HyperIoTKafkaMessage message) {
        this.produceMessage(message, null);
    }

    /**
     * @param context Bundle Context
     */
    private void loadKafkaConfiguration(BundleContext context) {
        getLog().debug("Kafka Properties not cached, reading from .cfg file...");
        ServiceReference<?> configurationAdminReference = context
                .getServiceReference(ConfigurationAdmin.class.getName());

        if (configurationAdminReference != null) {
            ConfigurationAdmin confAdmin = (ConfigurationAdmin) context
                    .getService(configurationAdminReference);
            try {
                Configuration configuration = confAdmin.getConfiguration(
                        HyperIoTConstants.HYPERIOT_KAFKA_CONNECTOR_CONFIG_FILE_NAME);
                if (configuration != null && configuration.getProperties() != null) {
                    getLog().debug("Reading properties for Kafka....");
                    Dictionary<String, Object> dict = configuration.getProperties();
                    List<String> keys = Collections.list(dict.keys());
                    Map<String, Object> dictCopy = keys.stream()
                            .collect(Collectors.toMap(Function.identity(), dict::get));
                    this.kafkaConnectUrl = (String) dictCopy.getOrDefault(HyperIoTKafkaConnectorConstants.HYPERIOT_KAFKA_OSGI_CONNECT_URL, "localhost:8080");
                    Iterator<String> it = dictCopy.keySet().iterator();
                    while (it.hasNext()) {
                        String propName = it.next();
                        if (propName.startsWith(
                                HyperIoTKafkaConnectorConstants.HYPERIOT_KAFKA_PROPS_CONSUMER_PREFIX)) {
                            getLog().debug("Reading consumer property for Kafka: {}", propName);
                            consumerProperties.put(propName.replaceAll(
                                    HyperIoTKafkaConnectorConstants.HYPERIOT_KAFKA_PROPS_CONSUMER_PREFIX,
                                    "").substring(1), dictCopy.get(propName));
                        } else if (propName.startsWith(
                                HyperIoTKafkaConnectorConstants.HYPERIOT_KAFKA_PROPS_SYSTEM_CONSUMER_PREFIX)) {
                            getLog().debug("Reading consumer property for Kafka: {}", propName);
                            systemConsumerProperties.put(propName, dictCopy.get(propName));
                        } else if (propName.startsWith(
                                HyperIoTKafkaConnectorConstants.HYPERIOT_KAFKA_PROPS_PRODUCER_PREFIX)) {
                            getLog().debug("Reading producer property for Kafka: {}", propName);
                            producerProperties.put(propName.replaceAll(
                                    HyperIoTKafkaConnectorConstants.HYPERIOT_KAFKA_PROPS_PRODUCER_PREFIX,
                                    "").substring(1), dictCopy.get(propName));
                        } else if (propName.startsWith(
                                HyperIoTKafkaConnectorConstants.HYPERIOT_KAFKA_PROPS_ADMIN_PREFIX)) {
                            getLog().debug("Reading admin property for Kafka: {}", propName);
                            adminProperties.put(propName.replaceAll(
                                    HyperIoTKafkaConnectorConstants.HYPERIOT_KAFKA_PROPS_ADMIN_PREFIX,
                                    "").substring(1), dictCopy.get(propName));
                        } else if (propName.startsWith(
                                HyperIoTKafkaConnectorConstants.HYPERIOT_KAFKA_PROPS_GLOBAL_PREFIX)) {
                            getLog().debug("Reading global property for Kafka: {}", propName);
                            String globalPropName = propName.replaceAll(
                                    HyperIoTKafkaConnectorConstants.HYPERIOT_KAFKA_PROPS_GLOBAL_PREFIX,
                                    "").substring(1);
                            consumerProperties.put(globalPropName, dictCopy.get(propName));
                            producerProperties.put(globalPropName, dictCopy.get(propName));
                            adminProperties.put(globalPropName, dictCopy.get(propName));
                        } else {
                            connectorProperties.put(propName, dictCopy.get(propName));
                        }
                    }
                    return;
                } else {
                    getLog().error(
                            "Impossible to find Configuration admin reference, kafka consumer won't start!");
                }
            } catch (IOException e) {
                getLog().error(
                        "Impossible to find {}", new Object[]{HyperIoTConstants.HYPERIOT_KAFKA_CONNECTOR_CONFIG_FILE_NAME, e});

            }
        }
        getLog().error(
                "Impossible to find {}", new Object[]{HyperIoTConstants.HYPERIOT_KAFKA_CONNECTOR_CONFIG_FILE_NAME});

    }

    /**
     * @return cluster system topic
     */
    @Override
    public String getClusterSystemTopic() {
        return HyperIoTKafkaConnectorConstants.HYPERIOT_KAFKA_OSGI_BASIC_TOPIC + "_"
                + HyperIoTUtil.getLayer();
    }

    /**
     * This method creates automatically basic topic needed by current instance in
     * order to communicate with HyperIoT Infrastructure
     */
    private List<String> createBasicTopics() {
        getLog().info("Creating basic topic on kafka if they do not exists...");
        // register on hyperiot_layer_<layer>
        String[] topics = new String[2];
        int[] numPartitions = new int[2];
        short[] numReplicas = new short[2];
        topics[0] = getClusterSystemTopic();
        topics[1] = getClusterSystemTopic() + "_" + HyperIoTUtil.getNodeId();
        //Setting num partitions equal to max threads defined
        numPartitions[0] = this.kafkaSystemConsumerMaxThreads;
        numPartitions[1] = this.kafkaSystemConsumerMaxThreads;
        numReplicas[0] = (short) 1;
        numReplicas[1] = (short) 1;
        this.adminCreateTopic(topics, numPartitions, numReplicas);
        List<String> topicList = Arrays.asList(topics);
        getLog().debug("Topics for this node are: {}", topicList.toString());
        if (getLog().isDebugEnabled()) {
            getLog().debug("Partitions for topics are: {}", Arrays.toString(numPartitions));
        }
        return topicList;
    }

    /**
     * @param topic         Topic name which must be created
     * @param numPartitions Number of partitions to assign to that topic
     * @param numReplicas   Number of replicas to assing to that topic
     * @return Future cointaining the execution result
     */
    @Override
    public CreateTopicsResult adminCreateTopic(String topic, int numPartitions, short numReplicas) {
        NewTopic newTopic = new NewTopic(topic, numPartitions, numReplicas);
        List<NewTopic> topics = new ArrayList<>();
        topics.add(newTopic);
        return this.adminClient.createTopics(topics);
    }

    /**
     * @param topics        Array of topic that must be created
     * @param numPartitions Array in which in the same position in topic array it
     *                      must be present the relative numPartitions
     * @param numReplicas   Array in which in the same position in topic array it
     *                      must be present the relative numReplicas
     * @return Future cointaining the execution result
     */
    @Override
    public CreateTopicsResult adminCreateTopic(String[] topics, int[] numPartitions,
                                               short[] numReplicas) {
        if (topics.length != numPartitions.length || topics.length != numReplicas.length)
            return null;

        List<NewTopic> topicsList = new ArrayList<>();
        for (int i = 0; i < topics.length; i++) {
            getLog().debug("Topic to be created: {} with num partition {} and replica {}", new Object[]{topics[i], numPartitions[i], numReplicas[i]});
            topicsList.add(new NewTopic(topics[i], numPartitions[i], numReplicas[i]));
        }
        getLog().debug("Invoking Kafka ADMIN Client..");

        CreateTopicsResult result = this.adminClient.createTopics(topicsList);
        return result;
    }

    /**
     * @param topics List of topics to be dropped
     * @return Future containing the execution result
     */
    @Override
    public DeleteTopicsResult adminDropTopic(List<String> topics) {
        return this.adminClient.deleteTopics(topics);
    }

    /**
     * Method for controlling access on Kafka resources
     *
     * @param username
     * @param permissions
     * @return
     */
    public CreateAclsResult adminAddACLs(String username, Map<String, HyperIoTKafkaPermission> permissions) {
        Iterator<String> it = permissions.keySet().iterator();
        List<AclBinding> acls = new ArrayList<>();
        while (it.hasNext()) {
            HyperIoTKafkaPermission permission = permissions.get(it.next());
            ResourcePattern resourcePattern = new ResourcePattern(ResourceType.TOPIC, permission.getTopic(), permission.getPatternType());
            AclBinding userAcl = new AclBinding(resourcePattern, new AccessControlEntry(username, "*", permission.getAclOperation(), permission.getAclPermissionType()));
            acls.add(userAcl);
        }
        CreateAclsOptions options = new CreateAclsOptions();
        //no options
        return this.adminClient.createAcls(acls, options);
    }

    /**
     * @return DeleteAclsResult
     */
    public DeleteAclsResult adminDeleteACLs(String username, Map<String, HyperIoTKafkaPermission> permissions) {
        Iterator<String> it = permissions.keySet().iterator();
        List<AclBindingFilter> acls = new ArrayList<>();
        while (it.hasNext()) {
            HyperIoTKafkaPermission permission = permissions.get(it.next());
            ResourcePatternFilter resourcePattern = new ResourcePatternFilter(ResourceType.TOPIC, permission.getTopic(), permission.getPatternType());
            AclBindingFilter userAcl = new AclBindingFilter(resourcePattern, new AccessControlEntryFilter(username, "*", permission.getAclOperation(), permission.getAclPermissionType()));
            acls.add(userAcl);
        }
        DeleteAclsOptions options = new DeleteAclsOptions();
        //no options
        return this.adminClient.deleteAcls(acls, options);
    }

    /**
     * @param instanceName
     * @param config
     * @return
     * @throws IOException
     */
    @Override
    public KafkaConnector addNewConnector(String instanceName, ConnectorConfig config) throws IOException {
        // TO DO: define post data
        String postData = String.format(CONNECTOR_ADD_TEMPLATE, config.getName(), config.getMaxPollIntervalMs(), config.getConnectorClass());
        String response = kafkaConnectPost(kafkaConnectUrl, postData);
        if (response.indexOf("error_code") > 0) {
            throw new IOException(response);
        } else {
            ObjectMapper mapper = new ObjectMapper();
            KafkaConnector connector = mapper.readValue(response, KafkaConnector.class);
            return connector;
        }
    }

    /**
     * @param instanceName
     * @param deleteKafkaTopic
     * @throws IOException
     */
    @Override
    public void deleteConnector(String instanceName, boolean deleteKafkaTopic) throws IOException {
        String response = kafkaConnectDelete(kafkaConnectUrl, instanceName);
        if (response.indexOf("error_code") > 0) {
            throw new IOException(response);
        } else if (deleteKafkaTopic) {
            // TODO: delete Kafka topic as well
        }
    }

    /**
     * @param instanceName
     * @return
     * @throws IOException
     */
    @Override
    public KafkaConnector getConnector(String instanceName) throws IOException {
        String response = kafkaConnectGet(kafkaConnectUrl, instanceName, null);
        if (response.indexOf("error_code") > 0) {
            throw new IOException(response);
        } else {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(response, KafkaConnector.class);
        }
    }

    /**
     * @return
     * @throws IOException
     */
    @Override
    public List<String> listConnectors() throws IOException {
        String response = kafkaConnectGet(kafkaConnectUrl, "", null);
        if (response.indexOf("error_code") > 0) {
            throw new IOException(response);
        } else {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(response, new TypeReference<List<String>>() {
            });
        }
    }

    /**
     * @param instanceName
     * @param config
     * @return
     * @throws IOException
     */
    @Override
    public KafkaConnector updateConnector(String instanceName, ConnectorConfig config) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String putData = mapper.writeValueAsString(config);
        String response = kafkaConnectPut(kafkaConnectUrl, instanceName + "/config", putData);
        if (response.indexOf("error_code") > 0) {
            throw new IOException(response);
        } else {
            return mapper.readValue(response, KafkaConnector.class);
        }
    }

    // Utility methods

    /**
     * @param kafkaConnectUrl
     * @param path
     * @param putData
     * @return
     * @throws IOException
     */
    private String kafkaConnectPut(String kafkaConnectUrl, String path, String putData) throws IOException {
        HttpPut httpPut = new HttpPut(kafkaConnectUrl + KAFKA_CONNECT_SERVICE_PATH + "/" + path);
        httpPut.setHeader("Accept", "application/json");
        httpPut.setHeader("Content-type", "application/json");
        StringEntity entity = new StringEntity(putData);
        httpPut.setEntity(entity);
        CloseableHttpClient client = HttpClients.createDefault();
        CloseableHttpResponse response = client.execute(httpPut);
        String responseText = getKafkaConnectResponseText(response);
        response.close();
        client.close();
        return responseText;
    }

    /**
     * @param kafkaConnectUrl
     * @param postData
     * @return
     * @throws IOException
     */
    private String kafkaConnectPost(String kafkaConnectUrl, String postData) throws IOException {
        HttpPost httpPost = new HttpPost(kafkaConnectUrl + KAFKA_CONNECT_SERVICE_PATH);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
        // set post data
        StringEntity entity = new StringEntity(postData);
        httpPost.setEntity(entity);
        CloseableHttpClient client = HttpClients.createDefault();
        CloseableHttpResponse response = client.execute(httpPost);
        String responseText = getKafkaConnectResponseText(response);
        response.close();
        client.close();
        return responseText;
    }

    /**
     * @param kafkaConnectUrl
     * @param path
     * @param parameters
     * @return
     * @throws IOException
     */
    private String kafkaConnectGet(String kafkaConnectUrl, String path, Map<String, String> parameters) throws
            IOException {
        HttpGet httpGet = new HttpGet(kafkaConnectUrl + KAFKA_CONNECT_SERVICE_PATH + "/" + path);
        CloseableHttpClient client = HttpClients.createDefault();
        CloseableHttpResponse response = client.execute(httpGet);
        String responseText = getKafkaConnectResponseText(response);
        response.close();
        client.close();
        return responseText;
    }

    /**
     * @param kafkaConnectUrl
     * @param arguments
     * @return
     * @throws IOException
     */
    private String kafkaConnectDelete(String kafkaConnectUrl, String arguments) throws IOException {
        HttpDelete httpDelete = new HttpDelete(kafkaConnectUrl + KAFKA_CONNECT_SERVICE_PATH + "/" + arguments);
        httpDelete.setHeader("Accept", "application/json");
        // set post data
        CloseableHttpClient client = HttpClients.createDefault();
        CloseableHttpResponse response = client.execute(httpDelete);
        String responseText = getKafkaConnectResponseText(response);
        response.close();
        client.close();
        return responseText;
    }

    /**
     * @param response
     * @return
     * @throws IOException
     */
    private String getKafkaConnectResponseText(CloseableHttpResponse response) throws IOException {
        if (response.getEntity() == null) return "";
        BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        return content.toString();
    }

    private ReceiverOptions<byte[], byte[]> createReceiverOptionsForTopicPattern(String kafkaGroupId, Pattern topicPattern, long pollTime, Class keyDeserializerClass, Class valueDeserializerClass, KafkaPartitionAssignListener assignListener, KafkaPartitionRevokeListener revokeListener) {
        ReceiverOptions<byte[], byte[]> receiverOptions =
                this.createBasicReceiverOptions(kafkaGroupId, pollTime, keyDeserializerClass, valueDeserializerClass, assignListener, revokeListener);
        if (receiverOptions != null) {
            receiverOptions.subscription(topicPattern);
        }
        return receiverOptions;
    }

    private ReceiverOptions<byte[], byte[]> createReceiverOptionsForTopics(String kafkaGroupId, List<String> topics, List<Integer> partitions, long pollTime, Class keyDeserializerClass, Class valueDeserializerClass, KafkaPartitionAssignListener assignListener, KafkaPartitionRevokeListener revokeListener) {
        ReceiverOptions<byte[], byte[]> receiverOptions =
                this.createBasicReceiverOptions(kafkaGroupId, pollTime, keyDeserializerClass, valueDeserializerClass, assignListener, revokeListener);
        if (receiverOptions != null) {
            if (partitions == null || partitions.isEmpty()) {
                receiverOptions
                        .subscription(Collections.synchronizedCollection(topics));
            } else if (partitions != null && topics.size() == partitions.size()) {
                List<TopicPartition> topicsAndPartiions = new ArrayList<>();
                for (int i = 0; i < topics.size(); i++) {
                    topicsAndPartiions.add(new TopicPartition(topics.get(i), partitions.get(i)));
                }
                receiverOptions.assignment(topicsAndPartiions);
            }
        }
        return receiverOptions;
    }

    private ReceiverOptions<byte[], byte[]> createBasicReceiverOptions(String kafkaGroupId, long pollTime, Class keyDeserializerClass, Class valueDeserializerClass, KafkaPartitionAssignListener assignListener, KafkaPartitionRevokeListener revokeListener) {
        if (consumerProperties != null && consumerProperties.size() > 0) {
            ReceiverOptions<byte[], byte[]> receiverOptions = ReceiverOptions.create(consumerProperties);
            receiverOptions.consumerProperties().put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializerClass);
            receiverOptions.consumerProperties().put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializerClass);
            receiverOptions.consumerProperties().put(ConsumerConfig.GROUP_ID_CONFIG, kafkaGroupId);
            if (assignListener != null)
                receiverOptions.addAssignListener(assignListener);
            if (revokeListener != null)
                receiverOptions.addRevokeListener(revokeListener);
            if (pollTime >= 0)
                receiverOptions.pollTimeout(Duration.ofMillis(pollTime));
            return receiverOptions;
        }
        return null;
    }

}
