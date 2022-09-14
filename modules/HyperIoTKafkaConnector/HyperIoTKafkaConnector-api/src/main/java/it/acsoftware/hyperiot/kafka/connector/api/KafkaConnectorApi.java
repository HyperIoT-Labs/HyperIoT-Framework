package it.acsoftware.hyperiot.kafka.connector.api;

import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.kafka.connector.model.ConnectorConfig;
import it.acsoftware.hyperiot.kafka.connector.model.HyperIoTKafkaPermission;
import it.acsoftware.hyperiot.kafka.connector.model.KafkaConnector;
import org.apache.kafka.clients.admin.CreateAclsResult;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.DeleteAclsResult;
import org.apache.kafka.clients.admin.DeleteTopicsResult;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.ReceiverRecord;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Aristide Cittadino
 * Interface for exposing data from Kafka to the "outside" world as a service (rest)
 */
public interface KafkaConnectorApi {
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
    Flux<ReceiverRecord<byte[], byte[]>> consumeReactive(String kafkaGroupId, List<String> topics,long pollTime, Class keyDeserializerClass, Class valueDeserializerClass) throws ClassNotFoundException;

    /**
     * @param kafkaGroupId           kafka GroupId
     * @param topic                  Kafka Topic
     * @param partition              Partition from which data must be consumed
     * @param keyDeserializerClass   Key Deserializer class
     * @param valueDeserializerClass Value Deserializer class
     * @return
     */
    Flux<ReceiverRecord<byte[], byte[]>> consumeReactive(String kafkaGroupId, String topic,long pollTime, int partition, Class keyDeserializerClass, Class valueDeserializerClass) throws ClassNotFoundException;


    /**
     * @param kafkaGroupId           kafka GroupId
     * @param topicPattern           Kafka Topic Pattern
     * @param keyDeserializerClass   Key Deserializer class
     * @param valueDeserializerClass Value Deserializer class
     * @return
     * @throws ClassNotFoundException
     */
    void consumeReactiveAsSystem(String kafkaGroupId, Pattern topicPattern, long pollTime, Class keyDeserializerClass, Class valueDeserializerClass) throws ClassNotFoundException;

    /**
     * @param topic         Topic name which must be created
     * @param numPartitions Number of partitions to assign to that topic
     * @param numReplicas   Number of replicas to assing to that topic
     * @return Future cointaining the execution result
     */
    CreateTopicsResult adminCreateTopic(HyperIoTContext context, String topic, int numPartitions, short numReplicas);

    /**
     * @param topics        Array of topic that must be created
     * @param numPartitions Array in which in the same position in topic array it must be present the relative numPartitions
     * @param numReplicas   Array in which in the same position in topic array it must be present the relative numReplicas
     * @return Future cointaining the execution result
     */
    CreateTopicsResult adminCreateTopic(HyperIoTContext context, String[] topics, int[] numPartitions, short[] numReplicas);

    /**
     * @param topics Topic to be dropped
     * @return Future cointaining the execution result
     */
    DeleteTopicsResult adminDropTopic(HyperIoTContext context, List<String> topics);

    /**
     * Method for controlling access on Kafka resources
     *
     * @param username
     * @param permissions
     * @return
     */
    CreateAclsResult adminAddACLs(HyperIoTContext context, String username, Map<String, HyperIoTKafkaPermission> permissions);

    /**
     * Method for controlling access on Kafka resources
     *
     * @param username
     * @param permissions
     * @return
     */
    DeleteAclsResult adminDeleteACLs(HyperIoTContext context, String username, Map<String, HyperIoTKafkaPermission> permissions);

    /**
     * @param instanceName
     * @param config
     * @return
     * @throws IOException
     */
    KafkaConnector addNewConnector(HyperIoTContext context, String instanceName, ConnectorConfig config) throws IOException;

    /**
     * @param instanceName
     * @param deleteKafkaTopic
     * @throws IOException
     */
    void deleteConnector(HyperIoTContext context, String instanceName, boolean deleteKafkaTopic) throws IOException;

    /**
     * @param instanceName
     * @return
     * @throws IOException
     */
    KafkaConnector getConnector(HyperIoTContext context, String instanceName) throws IOException;

    /**
     * @return
     * @throws IOException
     */
    List<String> listConnectors(HyperIoTContext context) throws IOException;

    /**
     * @param instanceName
     * @param config
     * @return
     * @throws IOException
     */
    KafkaConnector updateConnector(HyperIoTContext context, String instanceName, ConnectorConfig config) throws IOException;
}
