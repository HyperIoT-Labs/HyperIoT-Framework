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

package it.acsoftware.hyperiot.base.action.util;

import it.acsoftware.hyperiot.base.action.HyperIoTActionName;

/**
 * @author Aristide Cittadino Class that enumerate all CRUD base actions. Note
 * that all base actions are used by entities in the HyperIoT platform.
 */
public enum HyperIoTViewAction implements HyperIoTActionName {
    VIEW("view");

    /**
     * String name for base action
     */
    private String name;

    /**
     * Base Action with the specified name
     *
     * @param name parameter that represent the base action
     */
    private HyperIoTViewAction(String name) {
        this.name = name;
    }

    /**
     * Gets the name of base action
     */
    public String getName() {
        return name;
    }

}
