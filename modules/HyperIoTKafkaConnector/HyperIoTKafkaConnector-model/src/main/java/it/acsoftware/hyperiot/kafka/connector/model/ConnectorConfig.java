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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Gene (generoso.martello@acsoftware.it)
 * @version 2019-03-11 Initial release
 */
@JsonIgnoreProperties
public class ConnectorConfig {
    private String name;

    @JsonProperty("max.poll.interval.ms")
    protected int maxPollIntervalMs = 500;
    @JsonProperty("connector.class")
    protected String connectorClass;
    @JsonProperty("tasks.max")
    protected int taskMax = 1;

    // the default constructor is required for serialization
    public ConnectorConfig() {

    }

    public ConnectorConfig(String connectorName, String connectorClass) {
        this.name = connectorName;
        this.connectorClass = connectorClass;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMaxPollIntervalMs() {
        return maxPollIntervalMs;
    }

    public void setMaxPollIntervalMs(int maxPollIntervalMs) {
        this.maxPollIntervalMs = maxPollIntervalMs;
    }

    public String getConnectorClass() {
        return connectorClass;
    }

    public void setConnectorClass(String connectorClass) {
        this.connectorClass = connectorClass;
    }

    public int getTaskMax() {
        return taskMax;
    }

    public void setTaskMax(int taskMax) {
        this.taskMax = taskMax;
    }
}
