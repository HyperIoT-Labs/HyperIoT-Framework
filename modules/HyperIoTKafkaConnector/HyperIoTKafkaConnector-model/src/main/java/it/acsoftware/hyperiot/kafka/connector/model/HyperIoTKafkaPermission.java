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

import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.PatternType;

public class HyperIoTKafkaPermission {
    private String topic;
    private PatternType patternType;
    private AclOperation aclOperation;
    private AclPermissionType aclPermissionType;

    public HyperIoTKafkaPermission(String topic, PatternType patternType, AclOperation aclOperation, AclPermissionType aclPermissionType) {
        this.topic = topic;
        this.patternType = patternType;
        this.aclOperation = aclOperation;
        this.aclPermissionType = aclPermissionType;
    }

    public String getTopic() {
        return topic;
    }

    public PatternType getPatternType() {
        return patternType;
    }

    public AclOperation getAclOperation() {
        return aclOperation;
    }

    public AclPermissionType getAclPermissionType() {
        return aclPermissionType;
    }
}
