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

import it.acsoftware.hyperiot.base.action.HyperIoTActionList;
import it.acsoftware.hyperiot.base.action.HyperIoTPermissionActivator;
import it.acsoftware.hyperiot.base.action.util.HyperIoTActionFactory;
import it.acsoftware.hyperiot.kafka.connector.model.KafkaConnector;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Aristide Cittadino Model class that define a bundle activator and
 * register actions for KafkaConnector
 */
public class KafkaConnectorActionsBundleActivator extends HyperIoTPermissionActivator {

    /**
     * Return a list actions that have to be registered as OSGi components
     */
    @Override
    public List<HyperIoTActionList> getActions() {
        // creates base Actions save,update,remove,find,findAll for the specified entity
        getLog().info( "Registering base actions for {}", KafkaConnector.class.getName());
        ArrayList<HyperIoTActionList> actionsLists = new ArrayList<>();
        HyperIoTActionList actionList = HyperIoTActionFactory.createEmptyActionList(KafkaConnector.class.getName());
        actionList.addAction(
            HyperIoTActionFactory.createAction(KafkaConnector.class.getName(),
                KafkaConnector.class.getName(), KafkaConnectorAction.ADMIN_KAFKA_TOPICS_ADD)
        );
        actionList.addAction(
            HyperIoTActionFactory.createAction(KafkaConnector.class.getName(),
                KafkaConnector.class.getName(), KafkaConnectorAction.ADMIN_KAFKA_TOPICS_DELETE)
        );
        actionList.addAction(
            HyperIoTActionFactory.createAction(KafkaConnector.class.getName(),
                KafkaConnector.class.getName(), KafkaConnectorAction.ADMIN_KAFKA_TOPICS_UPDATE)
        );
        actionList.addAction(
            HyperIoTActionFactory.createAction(KafkaConnector.class.getName(),
                KafkaConnector.class.getName(), KafkaConnectorAction.ADMIN_KAFKA_TOPICS_METRICS)
        );

        actionList.addAction(
            HyperIoTActionFactory.createAction(KafkaConnector.class.getName(),
                KafkaConnector.class.getName(), KafkaConnectorAction.ADMIN_KAFKA_TOPICS_UPDATE)
        );
        actionList.addAction(
            HyperIoTActionFactory.createAction(KafkaConnector.class.getName(),
                KafkaConnector.class.getName(), KafkaConnectorAction.ADMIN_KAFKA_ACL_ADD)
        );
        actionList.addAction(
            HyperIoTActionFactory.createAction(KafkaConnector.class.getName(),
                KafkaConnector.class.getName(), KafkaConnectorAction.ADMIN_KAFKA_ACL_DELETE)
        );
        actionList.addAction(
            HyperIoTActionFactory.createAction(KafkaConnector.class.getName(),
                KafkaConnector.class.getName(), KafkaConnectorAction.ADMIN_KAFKA_ACL_UPDATE)
        );
        actionList.addAction(
            HyperIoTActionFactory.createAction(KafkaConnector.class.getName(),
                KafkaConnector.class.getName(), KafkaConnectorAction.CONSUME_FROM_TOPIC)
        );
        actionsLists.add(actionList);
        return actionsLists;
    }
}
