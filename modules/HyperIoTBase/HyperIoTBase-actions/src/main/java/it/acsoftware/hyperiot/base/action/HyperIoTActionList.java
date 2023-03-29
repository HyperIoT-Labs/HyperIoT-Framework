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

package it.acsoftware.hyperiot.base.action;

import it.acsoftware.hyperiot.base.api.HyperIoTAction;

import java.util.*;

/**
 * @author Aristide Cittadino Model class that defines method to create a list
 * of actions.
 */
public class HyperIoTActionList {
    /**
     * List of actions for HyperIoTAction
     */
    private List<HyperIoTAction> actions;
    private String resourceName;
    private int currentActionId;

    /**
     * Constructor of HyperIoTActionList
     */
    public HyperIoTActionList(String resourceName) {
        actions = new ArrayList<>();
        this.resourceName = resourceName;
        currentActionId = 1;
    }

    /**
     *
     * @return The name of the resource associted with actions
     */
    public String getResourceName() {
        return resourceName;
    }

    /**
     * Add items to the list of actions
     *
     * @param action
     */
    public void addAction(HyperIoTAction action) {
        action.setActionId(this.currentActionId);
        this.actions.add(action);
        currentActionId *= 2;
    }

    /**
     * Gets a actions list
     *
     * @return actions list
     */
    public List<HyperIoTAction> getList() {
        //Sorting based on actionIds
        Collections.sort(this.actions, new Comparator<HyperIoTAction>() {
            @Override
            public int compare(HyperIoTAction o1, HyperIoTAction o2) {
                if (o1.getActionId() > o2.getActionId())
                    return 1;
                else if (o1.getActionId() < o2.getActionId())
                    return -1;

                return 0;
            }
        });
        return this.actions;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < actions.size(); i++) {
            sb.append("Action " + actions.get(i).getActionName() + " - " + actions.get(i).getActionId() + "\n");
        }
        return sb.toString();
    }

}
