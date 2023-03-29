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

package it.acsoftware.hyperiot.sparkmanager.actions;

import it.acsoftware.hyperiot.base.action.HyperIoTActionList;
import it.acsoftware.hyperiot.base.action.HyperIoTPermissionActivator;
import it.acsoftware.hyperiot.base.action.util.HyperIoTActionFactory;
import it.acsoftware.hyperiot.sparkmanager.model.SparkManager;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Aristide Cittadino Model class that define a bundle activator and
 * register actions for SparkManager
 */
public class SparkManagerActionsBundleActivator extends HyperIoTPermissionActivator {

    /**
     * Return a list actions that have to be registered as OSGi components
     */
    @Override
    public List<HyperIoTActionList> getActions() {
        getLog().info( "Registering base actions for {}", SparkManager.class.getName());
        ArrayList<HyperIoTActionList> actionsLists = new ArrayList<>();
        HyperIoTActionList actionList = HyperIoTActionFactory.createEmptyActionList(SparkManager.class.getName());
        actionList.addAction(HyperIoTActionFactory.createAction(SparkManager.class.getName(),
            SparkManager.class.getName(), SparkManagerAction.GET_JOB_STATUS));
        actionList.addAction(HyperIoTActionFactory.createAction(SparkManager.class.getName(),
            SparkManager.class.getName(), SparkManagerAction.KILL_JOB));
        actionList.addAction(HyperIoTActionFactory.createAction(SparkManager.class.getName(),
            SparkManager.class.getName(), SparkManagerAction.SUBMIT_JOB));
        actionsLists.add(actionList);
        return actionsLists;
    }

}
