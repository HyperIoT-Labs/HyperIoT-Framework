package it.acsoftware.hyperiot.kafka.connector.api;

import it.acsoftware.hyperiot.kafka.connector.model.HyperIoTKafkaMessage;
import org.apache.kafka.clients.producer.Callback;

public interface KafkaProducerPool {
    void send(HyperIoTKafkaMessage message);
    void send(HyperIoTKafkaMessage message, Callback callback);
    void shutdown();
}
