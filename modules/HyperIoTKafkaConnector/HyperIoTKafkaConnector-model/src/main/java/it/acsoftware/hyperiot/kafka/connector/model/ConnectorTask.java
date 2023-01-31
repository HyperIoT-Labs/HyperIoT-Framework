/*
 * Copyright 2019-2023 ACSoftware
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

/**
 *
 * @author Gene (generoso.martello@acsoftware.it)
 * @version 2019-03-11 Initial release
 *
 */
@JsonIgnoreProperties
public class ConnectorTask {
    private String connector;
    private int task;

    /**
     * Gets the connector instance name.
     */
    public String getConnector() {
        return connector;
    }

    /**
     * Sets the connector instance name.
     * @param connector Connector name.
     */
    public void setConnector(String connector) {
        this.connector = connector;
    }

    /**
     * Gets the connector task number.
     */
    public int getTask() {
        return task;
    }
}
