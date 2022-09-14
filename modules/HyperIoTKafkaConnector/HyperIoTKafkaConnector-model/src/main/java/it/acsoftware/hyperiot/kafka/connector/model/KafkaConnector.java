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

import it.acsoftware.hyperiot.base.api.HyperIoTProtectedResource;


public class KafkaConnector implements HyperIoTProtectedResource {

    private String name;
    private String type = "source";
    private ConnectorConfig config;
    private it.acsoftware.hyperiot.kafka.connector.model.ConnectorTask[] tasks;

    /**
     * Gets connector instance name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets connector instance name.
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the connector configuration object.
     */
    public ConnectorConfig getConfig() {
        return config;
    }

    /**
     * Sets the connector configuration object.
     *
     * @param config Connector configuration object.
     */
    public void setConfig(ConnectorConfig config) {
        this.config = config;
    }

    /**
     * Gets the connector type (should always be "source")
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the connector task list.
     */
    public ConnectorTask[] getTasks() {
        return tasks;
    }

    @Override
    public String getResourceId() {
        return name;
    }

    @Override
    public String getResourceName() {
        return this.getClass().getName();
    }
}
