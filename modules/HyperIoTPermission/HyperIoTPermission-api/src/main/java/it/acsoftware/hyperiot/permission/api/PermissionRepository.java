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
import it.acsoftware.hyperiot.base.api.HyperIoTUser;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTBaseRepository;
import it.acsoftware.hyperiot.permission.model.Permission;

import java.util.Collection;
import java.util.List;

/**
 * @author Aristide Cittadino Interface component for Permission Repository. It
 * is used for CRUD operations, and to interact with the persistence
 * layer.
 */
public interface PermissionRepository extends HyperIoTBaseRepository<Permission> {
    /**
     * Find a permission by a specific user and resource
     *
     * @param user     user parameter
     * @param resource parameter required to find a resource
     * @return Permission if found
     */
    public Permission findByUserAndResource(HyperIoTUser user, HyperIoTResource resource);

    /**
     * Find a permission by a specific user and resource name via query
     *
     * @param user               user parameter
     * @param entityResourceName parameter required to find a resource name
     * @return Permission if found
     */
    Permission findByUserAndResourceName(HyperIoTUser user, String entityResourceName);

    /**
     * Find a permission by a specific role, resource name and resource id via query
     *
     * @param user               user parameter
     * @param entityResourceName parameter required to find a resource name
     * @param id                 parameter required to find a resource id
     * @return Permission if found
     */
    Permission findByUserAndResourceNameAndResourceId(HyperIoTUser user,
                                                      String entityResourceName, long id);

    /**
     * Find a permission by a specific role and resource
     *
     * @param role     parameter required to find role by roleId
     * @param resource parameter required to find a resource
     * @return Permission if found
     */
    public Permission findByRoleAndResource(HyperIoTRole role, HyperIoTResource resource);

    /**
     * Find a permission by a specific role and resource name via query
     *
     * @param role               parameter required to find role by roleId
     * @param entityResourceName parameter required to find a resource name
     * @return Permission if found
     */
    public Permission findByRoleAndResourceName(HyperIoTRole role, String entityResourceName);

    /**
     * Find a permissions by a specific role
     *
     * @param role parameter required to find role by roleId
     * @return Permissions if found
     */
    public Collection<Permission> findByRole(HyperIoTRole role);

    /**
     * Find a permission by a specific role, resource name and resource id via query
     *
     * @param role               parameter required to find role by roleId
     * @param entityResourceName parameter required to find a resource name
     * @param id                 parameter required to find a resource id
     * @return Permission if found
     */
    public Permission findByRoleAndResourceNameAndResourceId(HyperIoTRole role,
                                                             String entityResourceName, long id);

    /**
     * @param roleName
     * @param actions  List actions to add as permissions
     */
    public void checkOrCreateRoleWithPermissions(String roleName, List<HyperIoTAction> actions);

    public void checkOrCreateRoleWithPermissionsSpecificToEntity(String roleName, long entityId, List<HyperIoTAction> actions);

    /**
     * Verify if exist a permission specific to entity
     *
     * @param resourceName parameter required to find a resource name
     * @param resourceId   parameter required to find a resource id
     * @return true if exist a specific permission to this entity, false otherwise
     */
    public boolean existPermissionSpecificToEntity(String resourceName, long resourceId);


}
