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

package it.acsoftware.hyperiot.kafka.connector.model;

import java.util.HashMap;
import java.util.Objects;

public class ACLConfig {

    private String username;
    private HashMap<String, HyperIoTKafkaPermission> permissions;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public HashMap<String, HyperIoTKafkaPermission> getPermissions() {
        return permissions;
    }

    public void setPermissions(HashMap<String, HyperIoTKafkaPermission> permissions) {
        this.permissions = permissions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ACLConfig)) return false;
        ACLConfig aclConfig = (ACLConfig) o;
        return getUsername().equals(aclConfig.getUsername()) &&
                getPermissions().equals(aclConfig.getPermissions());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUsername(), getPermissions());
    }
}
