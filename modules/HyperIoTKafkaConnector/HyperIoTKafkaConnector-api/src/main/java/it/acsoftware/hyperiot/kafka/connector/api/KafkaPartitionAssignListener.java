package it.acsoftware.hyperiot.kafka.connector.api;

import reactor.kafka.receiver.ReceiverPartition;

import java.util.Collection;
import java.util.function.Consumer;

public interface KafkaPartitionAssignListener extends Consumer<Collection<ReceiverPartition>> {

}
