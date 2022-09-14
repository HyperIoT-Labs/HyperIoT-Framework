package it.acsoftware.hyperiot.kafka.connector.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class TopicConfig {
    private String topic;
    private int numPartition;
    private short replicationFactor;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getNumPartition() {
        return numPartition;
    }

    public void setNumPartition(int numPartition) {
        this.numPartition = numPartition;
    }

    public short getReplicationFactor() { return replicationFactor;
    }

    public void setReplicationFactor(short replicationFactor) {
        this.replicationFactor = replicationFactor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TopicConfig)) return false;
        TopicConfig that = (TopicConfig) o;
        return getNumPartition() == that.getNumPartition() &&
                getReplicationFactor() == that.getReplicationFactor() &&
                getTopic().equals(that.getTopic());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTopic(), getNumPartition(), getReplicationFactor());
    }
}
