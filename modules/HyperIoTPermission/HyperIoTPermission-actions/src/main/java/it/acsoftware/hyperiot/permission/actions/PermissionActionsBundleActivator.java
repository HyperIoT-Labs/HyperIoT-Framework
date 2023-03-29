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

package it.acsoftware.hyperiot.permission.actions;

import it.acsoftware.hyperiot.base.action.HyperIoTActionList;
import it.acsoftware.hyperiot.base.action.HyperIoTPermissionActivator;
import it.acsoftware.hyperiot.base.action.util.HyperIoTActionFactory;
import it.acsoftware.hyperiot.permission.model.Permission;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Aristide Cittadino Model class that define a bundle activator and
 * register actions for Permission
 */
public class PermissionActionsBundleActivator extends HyperIoTPermissionActivator {

    /**
     * Return a list actions that have to be registered as OSGi components
     */
    @Override
    public List<HyperIoTActionList> getActions() {
        getLog().info( "Registering base CRUD actions for {}", Permission.class.getName());
        ArrayList<HyperIoTActionList> actionsLists = new ArrayList<>();
        // creates base Actions save,update,remove,find,findAll for the specified entity
        HyperIoTActionList actionList = HyperIoTActionFactory
            .createBaseCrudActionList(Permission.class.getName(), Permission.class.getName());
        log.info("Registering " + HyperIoTPermissionAction.PERMISSION.getName() + " action");
        actionList.addAction(HyperIoTActionFactory.createAction(Permission.class.getName(),
            Permission.class.getName(), HyperIoTPermissionAction.PERMISSION));
        actionList.addAction(HyperIoTActionFactory.createAction(Permission.class.getName(),
            Permission.class.getName(), HyperIoTPermissionAction.LIST_ACTIONS));
        actionsLists.add(actionList);
        return actionsLists;
    }

}
