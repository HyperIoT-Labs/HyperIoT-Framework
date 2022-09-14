package it.acsoftware.hyperiot.kafka.connector.api;

import it.acsoftware.hyperiot.kafka.connector.model.HyperIoTKafkaMessage;

public interface KafkaSystemMessageNotifier {
    void notifyKafkaMessage(HyperIoTKafkaMessage message);
}
