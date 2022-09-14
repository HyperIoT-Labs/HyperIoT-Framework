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
