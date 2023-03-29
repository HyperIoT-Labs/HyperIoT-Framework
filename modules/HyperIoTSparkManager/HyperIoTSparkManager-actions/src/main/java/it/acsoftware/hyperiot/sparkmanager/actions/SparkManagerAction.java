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

import it.acsoftware.hyperiot.base.action.HyperIoTActionName;

/**
 * @author Aristide Cittadino Model class that enumerate SparkManager Actions
 */
public enum SparkManagerAction implements HyperIoTActionName {

    GET_JOB_STATUS(Names.GET_JOB_STATUS),
    KILL_JOB(Names.KILL_JOB),
    SUBMIT_JOB(Names.SUBMIT_JOB);

    private String name;

    /**
     * Role Action with the specified name.
     *
     * @param name parameter that represent the SparkManager  action
     */
    SparkManagerAction(String name) {
        this.name = name;
    }

    /**
     * Gets the name of SparkManager action
     */
    public String getName() {
        return name;
    }

    public class Names {
        public static final String GET_JOB_STATUS = "get_job_status";
        public static final String KILL_JOB = "kill_job";
        public static final String SUBMIT_JOB = "submit_job";
    }

}
