/*
 * Copyright 2019-2023 HyperIoT
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
