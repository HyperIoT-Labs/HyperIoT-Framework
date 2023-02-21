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
