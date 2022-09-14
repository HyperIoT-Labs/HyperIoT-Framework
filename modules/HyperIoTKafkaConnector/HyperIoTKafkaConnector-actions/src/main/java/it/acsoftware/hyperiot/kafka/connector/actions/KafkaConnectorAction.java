/*
 * Copyright (C) AC Software, S.r.l. - All Rights Reserved
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Gene <generoso.martello@acsoftware.it>, March 2019
 *
 */
package it.acsoftware.hyperiot.kafka.connector.actions;

import it.acsoftware.hyperiot.base.action.HyperIoTActionName;

/**
 * @author Aristide Cittadino Model class that enumerate KafkaMQTTConnector Actions
 */
public enum KafkaConnectorAction implements HyperIoTActionName {

    // TODO: add enumerations here
    ADMIN_KAFKA_TOPICS_ADD(Names.ADMIN_KAFKA_TOPICS_ADD),
    ADMIN_KAFKA_TOPICS_UPDATE(Names.ADMIN_KAFKA_TOPICS_UPDATE),
    ADMIN_KAFKA_TOPICS_DELETE(Names.ADMIN_KAFKA_TOPICS_DELETE),
    ADMIN_KAFKA_TOPICS_METRICS(Names.ADMIN_KAFKA_TOPICS_METRICS),
    ADMIN_KAFKA_ACL_ADD(Names.ADMIN_KAFKA_ACL_ADD),
    ADMIN_KAFKA_ACL_UPDATE(Names.ADMIN_KAFKA_ACL_UPDATE),
    ADMIN_KAFKA_ACL_DELETE(Names.ADMIN_KAFKA_ACL_DELETE),
    ADMIN_KAFKA_CONNECTOR_NEW(Names.ADMIN_KAFKA_CONNECTOR_NEW),
    ADMIN_KAFKA_CONNECTOR_DELETE(Names.ADMIN_KAFKA_CONNECTOR_DELETE),
    ADMIN_KAFKA_CONNECTOR_VIEW(Names.ADMIN_KAFKA_CONNECTOR_VIEW),
    ADMIN_KAFKA_CONNECTOR_LIST(Names.ADMIN_KAFKA_CONNECTOR_LIST),
    ADMIN_KAFKA_CONNECTOR_UPDATE(Names.ADMIN_KAFKA_CONNECTOR_UPDATE),
    CONSUME_FROM_TOPIC(Names.CONSUME_FROM_TOPIC);

    private String name;

    /**
     * Role Action with the specified name.
     *
     * @param name parameter that represent the KafkaConnector  action
     */
    KafkaConnectorAction(String name) {
        this.name = name;
    }

    /**
     * Gets the name of KafkaConnector action
     */
    public String getName() {
        return name;
    }

    public class Names {
        public static final String ADMIN_KAFKA_TOPICS_ADD = "admin_kafka_topics_add";
        public static final String ADMIN_KAFKA_TOPICS_UPDATE = "admin_kafka_topics_alter";
        public static final String ADMIN_KAFKA_TOPICS_DELETE = "admin_kafka_topics_delete";
        public static final String ADMIN_KAFKA_TOPICS_METRICS = "admin_kafka_topics_metrics";
        public static final String ADMIN_KAFKA_ACL_ADD = "admin_kafka_acl_add";
        public static final String ADMIN_KAFKA_ACL_UPDATE = "admin_kafka_acl_update";
        public static final String ADMIN_KAFKA_ACL_DELETE = "admin_kafka_acl_add";
        public static final String ADMIN_KAFKA_CONNECTOR_NEW = "admin_kafka_connector_new";
        public static final String ADMIN_KAFKA_CONNECTOR_DELETE = "admin_kafka_connector_delete";
        public static final String ADMIN_KAFKA_CONNECTOR_VIEW = "admin_kafka_connector_view";
        public static final String ADMIN_KAFKA_CONNECTOR_LIST = "admin_kafka_connector_list";
        public static final String ADMIN_KAFKA_CONNECTOR_UPDATE = "admin_kafka_connector_update";
        public static final String CONSUME_FROM_TOPIC = "consume_from_topic";
    }

}
