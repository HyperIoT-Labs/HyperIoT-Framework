package it.acsoftware.hyperiot.kafka.connector.api;

import it.acsoftware.hyperiot.base.api.HyperIoTBaseSystemApi;
import it.acsoftware.hyperiot.kafka.connector.model.ConnectorConfig;
import it.acsoftware.hyperiot.kafka.connector.model.HyperIoTKafkaMessage;
import it.acsoftware.hyperiot.kafka.connector.model.HyperIoTKafkaPermission;
import it.acsoftware.hyperiot.kafka.connector.model.KafkaConnector;
import org.apache.kafka.clients.admin.CreateAclsResult;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.DeleteAclsResult;
import org.apache.kafka.clients.admin.DeleteTopicsResult;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.osgi.framework.ServiceRegistration;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.ReceiverRecord;

import java.io.IOException;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Aristide Cittadino Interface component for %- projectSuffixUC
 * SystemApi. This interface defines methods for additional operations.
 */
public interface KafkaConnectorSystemApi extends HyperIoTBaseSystemApi {
    /**
     * This method is used if you want register manually an instance as KafkaMessageReceiver and not with @Component annotation.
     *
     * @param receiver Kafka Message Receiver
     * @param topic    Kafka topic on which receiver should receive messages
     * @param props    OSGi properties
     */
    ServiceRegistration<KafkaMessageReceiver> registerKafkaMessageReceiver(KafkaMessageReceiver receiver, List<String> topic, Dictionary<String, Object> props);

    /**
     * This method is used if you want hunregister manually an instance as KafkaMessageReceiver and not with @Component annotation.
     *
     * @param registration
     */
    void unregisterKafkaMessageReceiver(ServiceRegistration<KafkaMessageReceiver> registration);

    /**
     * Method for stopping consuming from Kafka
     */
    void stopConsumingFromKafka();

    /**
     * Method for starting consuming from Kafka. Note: the component starts
     * automatically consuming from kafka at bundle activation
     */
    void startConsumingFromKafka(List<String> topics);

    /**
     * Method which produces a message on Kafka
     */
    void produceMessage(HyperIoTKafkaMessage message, Callback callback);

    /**
     * Method which produces a message on Kafka without Callback
     */
    void produceMessage(HyperIoTKafkaMessage message);

    /**
     * @param message
     * @param producer
     * @param callback
     */
    void produceMessage(HyperIoTKafkaMessage message, Producer<byte[], byte[]> producer, Callback callback);

    /**
     * Instantiate a new kafka producer
     *
     * @return
     */
    KafkaProducer<byte[], byte[]> getNewProducer();

    /**
     *
     */
    KafkaProducerPool getNewProducerPool(int poolSize);

    /**
     * @return
     */
    KafkaProducer<byte[], byte[]> getNewProducer(String clientId);

    /**
     * Start consuming via Reactor-Flux from a kafka topic.
     *
     * @param kafkaGroupId           Kafka group identifier
     * @param topics                 List of topic names to consume from
     * @param keyDeserializerClass   Key deserializer class
     * @param valueDeserializerClass Value deserializer class
     * @return Flux for reactive streaming of realtime data
     * @throws ClassNotFoundException
     */
    @SuppressWarnings("rawtypes")
    Flux<ReceiverRecord<byte[], byte[]>> consumeReactive(String kafkaGroupId, List<String> topics, long pollTime, Class keyDeserializerClass, Class valueDeserializerClass) throws ClassNotFoundException;

    /**
     * @param kafkaGroupId           Kafka group identifier
     * @param topics                 List of topic names to consume from
     * @param keyDeserializerClass   Key deserializer class
     * @param valueDeserializerClass Value deserializer class
     * @param assignListener         partition assignment listener
     * @param revokeListener         partition revoke listener
     * @param pollTime               consumer poll time
     * @return
     */
    Flux<ReceiverRecord<byte[], byte[]>> consumeReactive(String kafkaGroupId, List<String> topics, long pollTime, Class keyDeserializerClass, Class valueDeserializerClass, KafkaPartitionAssignListener assignListener, KafkaPartitionRevokeListener revokeListener) throws ClassNotFoundException;

    /**
     * @param kafkaGroupId           Kafka group identifier
     * @param topics                 List of topic names to consume from
     * @param partitions
     * @param keyDeserializerClass   Key deserializer class
     * @param valueDeserializerClass Value deserializer class
     * @param assignListener         partition assignment listener
     * @param revokeListener         partition revoke listener
     * @param pollTime               consumer poll time
     * @return
     */
    Flux<ReceiverRecord<byte[], byte[]>> consumeReactive(String kafkaGroupId, List<String> topics, List<Integer> partitions, long pollTime, Class keyDeserializerClass, Class valueDeserializerClass, KafkaPartitionAssignListener assignListener, KafkaPartitionRevokeListener revokeListener) throws ClassNotFoundException;

    /**
     * @param kafkaGroupId           kafka GroupId
     * @param topic                  Kafka Topic
     * @param partition              Partition from which data must be consumed
     * @param keyDeserializerClass   Key Deserializer class
     * @param valueDeserializerClass Value Deserializer class
     * @param pollTime               consumer poll time
     * @return
     */
    Flux<ReceiverRecord<byte[], byte[]>> consumeReactive(String kafkaGroupId, String topic, int partition, long pollTime, Class keyDeserializerClass, Class valueDeserializerClass) throws ClassNotFoundException;

    /**
     * @param kafkaGroupId           kafka GroupId
     * @param topicPattern           Kafka Topic Pattern
     * @param keyDeserializerClass   Key Deserializer class
     * @param valueDeserializerClass Value Deserializer class
     * @param pollTime               consumer poll time
     * @return
     * @throws ClassNotFoundException
     */
    void consumeReactiveAsSystem(String kafkaGroupId, Pattern topicPattern, long pollTime, Class keyDeserializerClass, Class valueDeserializerClass) throws ClassNotFoundException;

    /**
     * @param kafkaGroupId           kafka GroupId
     * @param topic                  Kafka Topic Pattern
     * @param keyDeserializerClass   Key Deserializer class
     * @param valueDeserializerClass Value Deserializer class
     * @param pollTime               consumer poll time
     * @return
     * @throws ClassNotFoundException
     */
    void consumeReactiveAsSystem(String kafkaGroupId, String topic, long pollTime, Class keyDeserializerClass, Class valueDeserializerClass) throws ClassNotFoundException;


    /**
     * @param topic         Topic name which must be created
     * @param numPartitions Number of partitions to assign to that topic
     * @param numReplicas   Number of replicas to assing to that topic
     * @return Future cointaining the execution result
     */
    CreateTopicsResult adminCreateTopic(String topic, int numPartitions, short numReplicas);

    /**
     * @param topics        Array of topic that must be created
     * @param numPartitions Array in which in the same position in topic array it must be present the relative numPartitions
     * @param numReplicas   Array in which in the same position in topic array it must be present the relative numReplicas
     * @return Future cointaining the execution result
     */
    CreateTopicsResult adminCreateTopic(String[] topics, int[] numPartitions, short[] numReplicas);

    /**
     * @param topics Topic to be dropped
     * @return Future cointaining the execution result
     */
    DeleteTopicsResult adminDropTopic(List<String> topics);

    /**
     * Method for controlling access on Kafka resources
     *
     * @param username
     * @param permissions
     * @return
     */
    CreateAclsResult adminAddACLs(String username, Map<String, HyperIoTKafkaPermission> permissions);

    /**
     * Method for controlling access on Kafka resources
     *
     * @param username
     * @param permissions
     * @return
     */
    DeleteAclsResult adminDeleteACLs(String username, Map<String, HyperIoTKafkaPermission> permissions);

    /**
     * @param instanceName
     * @param config
     * @return
     * @throws IOException
     */
    KafkaConnector addNewConnector(String instanceName, ConnectorConfig config) throws IOException;

    /**
     * @param instanceName
     * @param deleteKafkaTopic
     * @throws IOException
     */
    void deleteConnector(String instanceName, boolean deleteKafkaTopic) throws IOException;

    /**
     * @param instanceName
     * @return
     * @throws IOException
     */
    KafkaConnector getConnector(String instanceName) throws IOException;

    /**
     * @return
     * @throws IOException
     */
    List<String> listConnectors() throws IOException;

    /**
     * @param instanceName
     * @param config
     * @return
     * @throws IOException
     */
    KafkaConnector updateConnector(String instanceName, ConnectorConfig config) throws IOException;

    //TO DO: add more administration methods

    /**
     * @return The topic used on kafka for cluster communication
     */
    public String getClusterSystemTopic();
}
