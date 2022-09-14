package it.acsoftware.hyperiot.kafka.connector.api;

import reactor.kafka.receiver.ReceiverPartition;

import java.util.Collection;
import java.util.function.Consumer;

public interface KafkaPartitionRevokeListener extends Consumer<Collection<ReceiverPartition>> {

}
