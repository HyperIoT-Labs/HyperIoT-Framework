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
