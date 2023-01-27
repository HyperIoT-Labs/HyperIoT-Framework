/*
 * Copyright 2019-2023 ACSoftware
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
public enum HyperIoTCrudAction implements HyperIoTActionName {
    SAVE(Names.SAVE), UPDATE(Names.UPDATE), FIND(Names.FIND), FINDALL(Names.FINDALL), REMOVE(Names.REMOVE);
    public String name;

    public class Names {
        public static final String SAVE = "save";
        public static final String UPDATE = "update";
        public static final String FIND = "find";
        public static final String FINDALL = "find-all";
        public static final String REMOVE = "remove";
    }

    HyperIoTCrudAction(String name) {
        this.name = name;
    }

    public final String getName() {
        return name;
    }
}
