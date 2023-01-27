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

package it.acsoftware.hyperiot.permission.api;

import java.util.HashMap;
import java.util.List;

import it.acsoftware.hyperiot.base.api.HyperIoTAction;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTBaseEntityApi;
import it.acsoftware.hyperiot.permission.model.Permission;

/**
 * @author Aristide Cittadino Interface component for PermissionApi. This
 * inteface defines methods for additional operations.
 */
public interface PermissionApi extends HyperIoTBaseEntityApi<Permission> {

    /**
     * This method finds a list of all available permissions for HyperIoT platform
     */
    public HashMap<String, List<HyperIoTAction>> getAvailablePermissions();
}
