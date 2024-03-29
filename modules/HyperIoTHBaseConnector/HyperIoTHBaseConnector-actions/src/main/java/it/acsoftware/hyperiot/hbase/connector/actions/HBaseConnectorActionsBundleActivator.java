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

package it.acsoftware.hyperiot.hbase.connector.actions;

import it.acsoftware.hyperiot.base.action.HyperIoTActionList;
import it.acsoftware.hyperiot.base.action.HyperIoTPermissionActivator;
import it.acsoftware.hyperiot.base.action.util.HyperIoTActionFactory;
import it.acsoftware.hyperiot.hbase.connector.model.HBaseConnector;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Aristide Cittadino Model class that define a bundle activator and
 * register actions for HBaseConnector
 */
public class HBaseConnectorActionsBundleActivator extends HyperIoTPermissionActivator {

    /**
     * Return a list actions that have to be registerd as OSGi components
     */
    @Override
    public List<HyperIoTActionList> getActions() {
        // creates base Actions save,update,remove,find,findAll for the specified entity
        getLog().info( "Registering base CRUD actions for {}", HBaseConnector.class.getName());
        ArrayList<HyperIoTActionList> actionsLists = new ArrayList<>();
        HyperIoTActionList actionList = HyperIoTActionFactory.createEmptyActionList(HBaseConnector.class.getName());
        actionList.addAction(HyperIoTActionFactory.createAction(HBaseConnector.class.getName(),
            HBaseConnector.class.getName(), HBaseConnectorAction.CHECK_CONNECTION));
        actionList.addAction(HyperIoTActionFactory.createAction(HBaseConnector.class.getName(),
            HBaseConnector.class.getName(), HBaseConnectorAction.CREATE_TABLE));
        actionList.addAction(HyperIoTActionFactory.createAction(HBaseConnector.class.getName(),
            HBaseConnector.class.getName(), HBaseConnectorAction.DELETE_DATA));
        actionList.addAction(HyperIoTActionFactory.createAction(HBaseConnector.class.getName(),
            HBaseConnector.class.getName(), HBaseConnectorAction.DISABLE_TABLE));
        actionList.addAction(HyperIoTActionFactory.createAction(HBaseConnector.class.getName(),
            HBaseConnector.class.getName(), HBaseConnectorAction.DROP_TABLE));
        actionList.addAction(HyperIoTActionFactory.createAction(HBaseConnector.class.getName(),
            HBaseConnector.class.getName(), HBaseConnectorAction.ENABLE_TABLE));
        actionList.addAction(HyperIoTActionFactory.createAction(HBaseConnector.class.getName(),
            HBaseConnector.class.getName(), HBaseConnectorAction.INSERT_DATA));
        actionList.addAction(HyperIoTActionFactory.createAction(HBaseConnector.class.getName(),
            HBaseConnector.class.getName(), HBaseConnectorAction.READ_DATA));
        actionsLists.add(actionList);
        return actionsLists;
    }

}
