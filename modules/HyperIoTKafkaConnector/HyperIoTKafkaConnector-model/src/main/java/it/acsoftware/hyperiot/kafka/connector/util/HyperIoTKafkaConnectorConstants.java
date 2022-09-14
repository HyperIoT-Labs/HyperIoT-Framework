package it.acsoftware.hyperiot.kafka.connector.util;

/**
 * @author Aristide Cittadino
 * Constants class
 */
public class HyperIoTKafkaConnectorConstants {
    /**
     * Property for max threads inside system consumer
     */
    public static final String HYPERIOT_KAFKA_SYSTEM_CONSUMER_MAX_THREADS = "it.acsoftware.hyperiot.kafka.system.max.consumer.thread";

    /**
     * Property prefix in the file it.acsoftware.hyperiot.cfg for Kafka Reactor consumer threads
     */
    public static final String HYPERIOT_KAFKA_REACTOR_PROP_MAX_CONSUMER_THREADS = "it.acsoftware.hyperiot.kafka.reactor.max.consumer.thread";
    /**
     * Property prefix in the file it.acsoftware.hyperiot.cfg for Kafka consumer properties
     */
    public static final String HYPERIOT_KAFKA_PROPS_CONSUMER_PREFIX = "it.acsoftware.hyperiot.kafka.consumer";
    /**
     * Property prefix in the file it.acsoftware.hyperiot.cfg for Kafka consumer properties
     */
    public static final String HYPERIOT_KAFKA_PROPS_SYSTEM_CONSUMER_PREFIX = "it.acsoftware.hyperiot.kafka.system.consumer";
    /**
     * Property prefix in the file it.acsoftware.hyperiot.cfg for Kafka producer properties
     */
    public static final String HYPERIOT_KAFKA_PROPS_PRODUCER_PREFIX = "it.acsoftware.hyperiot.kafka.producer";
    /**
     * Property prefix in the file it.acsoftware.hyperiot.cfg for Kafka admin properties
     */
    public static final String HYPERIOT_KAFKA_PROPS_ADMIN_PREFIX = "it.acsoftware.hyperiot.kafka.admin";
    /**
     * Property prefix in the file it.acsoftware.hyperiot.cfg for Kafla consumer,producer and admin properties
     */
    public static final String HYPERIOT_KAFKA_PROPS_GLOBAL_PREFIX = "it.acsoftware.hyperiot.kafka.all";
    /**
     * Property used inside OSGi component registration/loading to filter components for specifics topics
     */
    public static final String HYPERIOT_KAFKA_OSGI_TOPIC_FILTER = "it.acsoftware.hyperiot.kafka.topic";

    /**
     * Property used inside OSGi component registration/loading to filter components for specifics topics
     */
    public static final String HYPERIOT_KAFKA_OSGI_KEY_FILTER = "it.acsoftware.hyperiot.kafka.key";

    /**
     * Property used for kafka system consumer to define poll ms
     */
    public static final String HYPERIOT_KAFKA_SYSTEM_CONSUMER_POLL_MS = "it.acsoftware.hyperiot.kafka.system.consumer.poll.ms";

    /**
     * Basic Kafka topic
     */
    public static final String HYPERIOT_KAFKA_OSGI_BASIC_TOPIC = "hyperiot_layer";
    /**
     * Microservices Kafka topic
     */
    public static final String HYPERIOT_KAFKA_OSGI_MICROSERVICES_TOPIC =
            HYPERIOT_KAFKA_OSGI_BASIC_TOPIC + "_microservices";
    /**
     * Property used inside OSGi component registration/loading to filter components for specifics topics
     */
    public static final String HYPERIOT_KAFKA_OSGI_CONNECT_URL = "it.acsoftware.hyperiot.kafka.connect.url";

    /**
     * Property used to identify process event as a system message type
     */
    public static final String HYPERIOT_KAFKA_SYSTEM_MESSAGE_TYPE_PROCESS_EVENT = "PROCESS_EVENT";

}
