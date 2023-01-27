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

package it.acsoftware.hyperiot.base.model.authentication;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Aristide Cittadino
 * Profile Object which stores all Users permissions specific for a resource
 */
public class JWTProfile {
    private Set<String> permissions;

    public JWTProfile() {
        super();
        permissions = new HashSet<String>();
    }

    /**
     * @param actions Action list
     */
    public void addPermissionInfo(List<String> actions) {
        permissions.addAll(actions);
    }

    /**
     * @return
     */
    public Set<String> getPermissions() {
        return permissions;
    }

}
