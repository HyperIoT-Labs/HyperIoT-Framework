package it.acsoftware.hyperiot.kafka.connector.api;

import it.acsoftware.hyperiot.kafka.connector.model.HyperIoTKafkaMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Aristide Cittadino
 * Interfaces that must be implemented from components that wants to be notified when a message from Kafka arrives.
 * When register the component, an OSGi property (or more) must be declared to identify the topic from which the component wants to be notified.
 * <p>
 * For example: @Component(property={"it.acsoftware.hyperiot.kafka.topic=dataSaved"} will be notified only for kafka topic "/dataSaved"
 * Or if you want to filter also Kafka keys "key1" and "key2" on "dataSave" topic
 * @Component(property={"it.acsoftware.hyperiot.kafka.topic=dataSaved",it.acsoftware.hyperiot.kafka.key=dataSave:key1,it.acsoftware.hyperiot.kafka.key=dataSave:key2}
 */
public interface KafkaMessageReceiver {
    /**
     * Method invoked by System when a kafka message arrvived on the specified topic
     *
     * @param message
     */
    void receive(HyperIoTKafkaMessage message);
}
