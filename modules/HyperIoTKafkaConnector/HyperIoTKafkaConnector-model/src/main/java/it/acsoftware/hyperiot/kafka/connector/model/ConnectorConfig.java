/*
 * Copyright (C) AC Software, S.r.l. - All Rights Reserved
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Gene <generoso.martello@acsoftware.it>, March 2019
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
