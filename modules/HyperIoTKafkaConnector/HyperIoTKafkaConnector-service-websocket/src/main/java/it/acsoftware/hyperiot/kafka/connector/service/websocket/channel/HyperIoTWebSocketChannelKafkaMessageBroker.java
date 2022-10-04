package it.acsoftware.hyperiot.kafka.connector.service.websocket.channel;

import it.acsoftware.hyperiot.base.model.HyperIoTClusterNodeInfo;
import it.acsoftware.hyperiot.base.util.HyperIoTConstants;
import it.acsoftware.hyperiot.base.util.HyperIoTUtil;
import it.acsoftware.hyperiot.kafka.connector.api.KafkaConnectorSystemApi;
import it.acsoftware.hyperiot.kafka.connector.api.KafkaMessageReceiver;
import it.acsoftware.hyperiot.kafka.connector.api.KafkaProducerPool;
import it.acsoftware.hyperiot.kafka.connector.model.HyperIoTKafkaMessage;
import it.acsoftware.hyperiot.kafka.connector.util.HyperIoTKafkaConnectorConstants;
import it.acsoftware.hyperiot.websocket.api.channel.HyperIoTWebSocketChannel;
import it.acsoftware.hyperiot.websocket.api.channel.HyperIoTWebSocketChannelClusterMessageBroker;
import it.acsoftware.hyperiot.websocket.api.channel.HyperIoTWebSocketChannelManager;
import it.acsoftware.hyperiot.websocket.api.channel.HyperIoTWebSocketChannelRemoteCommand;
import it.acsoftware.hyperiot.websocket.channel.command.HyperIoTWebSocketChannelCommandFactory;
import it.acsoftware.hyperiot.websocket.channel.util.HyperIoTWebSocketChannelConstants;
import it.acsoftware.hyperiot.websocket.model.HyperIoTWebSocketConstants;
import it.acsoftware.hyperiot.websocket.model.message.HyperIoTWebSocketMessage;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.kafka.receiver.KafkaReceiver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component(service = HyperIoTWebSocketChannelClusterMessageBroker.class, property = {
        HyperIoTWebSocketConstants.CHANNEL_CLUSTER_MESSAGE_BROKER_OSGI_FILTER_NAME + "=kafka-cluster-message-broker"
})
public class HyperIoTWebSocketChannelKafkaMessageBroker implements HyperIoTWebSocketChannelClusterMessageBroker, KafkaMessageReceiver {
    private static Logger log = LoggerFactory.getLogger(HyperIoTWebSocketChannelKafkaMessageBroker.class);

    private static final String TOPIC = "it.acsoftware.hyperiot.kafka.connector.ws.channel.message.broker.topic";
    private static final String PARTITIONS = "it.acsoftware.hyperiot.kafka.connector.ws.channel.message.broker.topic.partitions";
    private static final String REPLICAS = "it.acsoftware.hyperiot.kafka.connector.ws.channel.message.broker.topic.replicas";
    private static final String POLLTIME = "it.acsoftware.hyperiot.kafka.connector.ws.channel.message.broker.topic.polltime";
    private static final String PRODUCERS_POOL_SIZE = "it.acsoftware.hyperiot.kafka.connector.ws.channel.message.broker.topic.producerPoolSize";

    private String communicationTopic;
    private ServiceRegistration<KafkaMessageReceiver> registration;
    private ServiceReference<KafkaMessageReceiver> reference;
    private KafkaProducerPool kafkaProducerPool;

    private HyperIoTWebSocketChannelManager channelManager;

    public HyperIoTWebSocketChannelKafkaMessageBroker() {
        Map<String, Object> props = loadClusterMessageBrokerConfig(HyperIoTUtil.getBundleContext(this));
        String groupId = HyperIoTUtil.getLayer() + "_" + HyperIoTUtil.getNodeId();
        String communicationTopic = (String) props.getOrDefault(TOPIC, null);
        int partitions = Integer.parseInt((String) props.getOrDefault(PARTITIONS, "0"));
        short replicas = Short.parseShort((String) props.getOrDefault(REPLICAS, "0"));
        long polltime = Long.parseLong((String) props.getOrDefault(POLLTIME, "0"));
        int producersPoolSize = Integer.parseInt((String) props.getOrDefault(PRODUCERS_POOL_SIZE, "0"));
        init(groupId, communicationTopic, partitions, replicas, polltime, producersPoolSize);
    }

    public HyperIoTWebSocketChannelKafkaMessageBroker(String groupId, String communicationTopic, int partitions, short replicas, long pollTime, int producersPoolSize) {
        this.init(groupId, communicationTopic, partitions, replicas, pollTime, producersPoolSize);
    }

    private void init(String groupId, String communicationTopic, int partitions, short replicas, long pollTime, int producersPoolSize) {
        if (groupId == null || communicationTopic == null || partitions == 0 || replicas == 0 || producersPoolSize == 0)
            throw new IllegalArgumentException("Impossible to instantiate Kafka WS Channel Cluster Message broker, required parameters are missing. Please provide them inside file: " + HyperIoTConstants.HYPERIOT_KAFKA_CONNECTOR_WS_CHANNEL_CLUSTER_MESSAGE_BROKER_CONFIG_FILE_NAME);
        this.communicationTopic = communicationTopic;
        this.createTopicIfNotExists(communicationTopic, partitions, replicas);
        this.registerKafkaReceiverComponent(groupId, communicationTopic, pollTime);
        kafkaProducerPool = this.getKafkaConnectorSystemApi().getNewProducerPool(producersPoolSize);
    }

    /**
     * Return the basic name communication topic on which nodeId is added <comm_basic_topic>-<nodeId>
     *
     * @return
     */
    private String getBaseCommunicationTopic() {
        int nodeIdIndex = this.communicationTopic.lastIndexOf("-");
        if (this.communicationTopic != null && nodeIdIndex > 0)
            return this.communicationTopic.substring(0, nodeIdIndex);
        return this.communicationTopic;
    }

    private String getClusterNodeCommunicationTopic(HyperIoTClusterNodeInfo nodeInfo){
        return this.getBaseCommunicationTopic()+"-"+nodeInfo.getNodeId();
    }

    @Override
    public void registerChannelManager(HyperIoTWebSocketChannelManager hyperIoTWebSocketChannelManager) {
        this.channelManager = hyperIoTWebSocketChannelManager;
    }

    /**
     * @param channelId                used as kafka key
     * @param hyperIoTWebSocketMessage
     */
    @Override
    public void sendMessage(String channelId, HyperIoTWebSocketMessage hyperIoTWebSocketMessage) {
        Optional<HyperIoTWebSocketChannel> channelOpt = this.channelManager.getAvailableChannels().stream().filter(channel -> channel.getChannelId().equalsIgnoreCase(channelId)).findAny();
        if (channelOpt.isPresent()) {
            //gets all server hosting a user session and sends to the related topics the message
            //todo manage private messages since at this moment it's like a broadcast message
            Set<HyperIoTClusterNodeInfo> info = channelOpt.get().getPeers();
            Iterator<HyperIoTClusterNodeInfo> it = info.iterator();
            while (it.hasNext()) {
                HyperIoTClusterNodeInfo clusterNodeInfo = it.next();
                String destTopic = getClusterNodeCommunicationTopic(clusterNodeInfo);
                HyperIoTKafkaMessage message = new HyperIoTKafkaMessage(channelId.getBytes(StandardCharsets.UTF_8), destTopic, hyperIoTWebSocketMessage.toJson().getBytes(StandardCharsets.UTF_8));
                this.kafkaProducerPool.send(message);
            }
        }
    }

    @Override
    public void receive(HyperIoTKafkaMessage message) {
        HyperIoTWebSocketMessage wsMessage = HyperIoTWebSocketMessage.fromString(new String(message.getPayload()));
        //executing the remote related command associated with the message
        HyperIoTWebSocketChannelRemoteCommand command = HyperIoTWebSocketChannelCommandFactory.createRemoteCommand(wsMessage.getCmd());
        try {
            String channelId = wsMessage.getParams().get(HyperIoTWebSocketChannelConstants.CHANNEL_ID_PARAM);
            command.execute(wsMessage, channelId, this.channelManager);
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
        }

    }

    private void createTopicIfNotExists(String topicName, int numPartitions, short numReplicas) {
        KafkaConnectorSystemApi kafkaConnectorSystemApi = this.getKafkaConnectorSystemApi();
        try {
            log.debug("Init kafka websocket message broker: Creating topic {} if it not exists", new Object[]{communicationTopic});
            kafkaConnectorSystemApi.adminCreateTopic(topicName, numPartitions, (short) numReplicas).all().get();
        } catch (Exception e) {
            log.debug(e.getMessage(), e);
        }
    }

    private void registerKafkaReceiverComponent(String groupId, String communicationTopic, long pollTime) {
        log.debug("Registering web socket to OSGi services...");
        KafkaConnectorSystemApi kafkaConnectorSystemApi = this.getKafkaConnectorSystemApi();
        try {
            kafkaConnectorSystemApi.consumeReactiveAsSystem(groupId, communicationTopic, pollTime, ByteArrayDeserializer.class, ByteArrayDeserializer.class);
            this.registration = HyperIoTUtil.getBundleContext(this).registerService( KafkaMessageReceiver.class.getName(),this,getServicerRegistrationProeperties());
            this.reference = this.registration.getReference();
        } catch (ClassNotFoundException e) {
            log.error(e.getMessage(), e);
        }
    }

    private Dictionary getServicerRegistrationProeperties() {
        Hashtable dictionary = new Hashtable<>();
        List<String> basicTopics = new ArrayList<>();
        basicTopics.add(this.communicationTopic);
        dictionary.put(HyperIoTKafkaConnectorConstants.HYPERIOT_KAFKA_OSGI_TOPIC_FILTER, basicTopics.toArray());
        dictionary.put(HyperIoTKafkaConnectorConstants.HYPERIOT_KAFKA_OSGI_KEY_FILTER, "*");
        return dictionary;
    }

    private KafkaConnectorSystemApi getKafkaConnectorSystemApi() {
        BundleContext ctx = HyperIoTUtil.getBundleContext(KafkaConnectorSystemApi.class);
        ServiceReference<KafkaConnectorSystemApi> kafkaConnectorSystemApiRef = ctx.getServiceReference(KafkaConnectorSystemApi.class);
        if (kafkaConnectorSystemApiRef != null) {
            return ctx.getService(kafkaConnectorSystemApiRef);
        }
        return null;
    }

    private Map<String, Object> loadClusterMessageBrokerConfig(BundleContext context) {
        log.debug("Loading kafka websocket channel message broker config");
        ServiceReference<?> configurationAdminReference = context
                .getServiceReference(ConfigurationAdmin.class.getName());

        if (configurationAdminReference != null) {
            ConfigurationAdmin confAdmin = (ConfigurationAdmin) context
                    .getService(configurationAdminReference);
            try {
                Configuration configuration = confAdmin.getConfiguration(
                        HyperIoTConstants.HYPERIOT_KAFKA_CONNECTOR_WS_CHANNEL_CLUSTER_MESSAGE_BROKER_CONFIG_FILE_NAME);
                if (configuration != null && configuration.getProperties() != null) {
                    log.debug("Reading properties for Kafka....");
                    Dictionary<String, Object> dict = configuration.getProperties();
                    List<String> keys = Collections.list(dict.keys());
                    Map<String, Object> dictCopy = keys.stream()
                            .collect(Collectors.toMap(Function.identity(), dict::get));
                    return dictCopy;
                } else {
                    log.error(
                            "Impossible to find Configuration admin reference, kafka channel cluster message broker won't start!");
                }
            } catch (IOException e) {
                log.error(
                        "Impossible to find {}", new Object[]{HyperIoTConstants.HYPERIOT_KAFKA_CONNECTOR_WS_CHANNEL_CLUSTER_MESSAGE_BROKER_CONFIG_FILE_NAME, e});
            }
        }
        log.error(
                "Impossible to find {}", new Object[]{HyperIoTConstants.HYPERIOT_KAFKA_CONNECTOR_WS_CHANNEL_CLUSTER_MESSAGE_BROKER_CONFIG_FILE_NAME});
        return null;

    }
}
