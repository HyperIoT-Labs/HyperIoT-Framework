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

package it.acsoftware.hyperiot.permission.api;

import it.acsoftware.hyperiot.base.api.HyperIoTAction;
import it.acsoftware.hyperiot.base.api.HyperIoTResource;
import it.acsoftware.hyperiot.base.api.HyperIoTRole;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTBaseEntitySystemApi;
import it.acsoftware.hyperiot.permission.model.Permission;

import java.util.Collection;
import java.util.List;

/**
 * @author Aristide Cittadino Interface component for PermissionSystemApi. This
 * interface defines methods for additional operations.
 */
public interface PermissionSystemApi extends HyperIoTBaseEntitySystemApi<Permission> {

    /**
     * Find a permission by a specific role and resource
     *
     * @param role     parameter required to find role by roleId
     * @param resource parameter required to find a resource
     * @return Permission if found
     */
    public Permission findByRoleAndResource(HyperIoTRole role, HyperIoTResource resource);

    /**
     * Find a permission by a specific role and resource name
     *
     * @param role         parameter required to find role by roleId
     * @param resourceName parameter required to find a resource name
     * @return Permission if found
     */
    public Permission findByRoleAndResourceName(HyperIoTRole role, String resourceName);

    /**
     * Find a permission by a specific role and resource name
     *
     * @param role parameter required to find role by roleId
     * @return Permission if found
     */
    public Collection<Permission> findByRole(HyperIoTRole role);

    /**
     * Find a permission by a specific role, resource name and resource id
     *
     * @param role         parameter required to find role by roleId
     * @param resourceName parameter required to find a resource name
     * @param id           parameter required to find a resource id
     * @return Permission if found
     */
    public Permission findByRoleAndResourceNameAndResourceId(HyperIoTRole role, String resourceName, long id);

    /**
     * @param roleName
     * @param actions  List actions to add as permissions
     */
    public void checkOrCreateRoleWithPermissions(String roleName, List<HyperIoTAction> actions);

    public void checkOrCreateRoleWithPermissionsSpecificToEntity(String roleName, long entityId, List<HyperIoTAction> actions);

    /**
     * Verify if exist a permission specific to entity
     * @param resourceName parameter required to find a resource name
     * @param resourceId   parameter required to find a resource id
     * @return true if exist a specific permission to this entity, false otherwise
     */
    public boolean existPermissionSpecificToEntity(String resourceName, long resourceId);

}
