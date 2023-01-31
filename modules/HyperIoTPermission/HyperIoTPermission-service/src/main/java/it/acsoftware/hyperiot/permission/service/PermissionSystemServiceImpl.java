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

package it.acsoftware.hyperiot.permission.service;

import it.acsoftware.hyperiot.base.api.HyperIoTAction;
import it.acsoftware.hyperiot.base.api.HyperIoTResource;
import it.acsoftware.hyperiot.base.api.HyperIoTRole;
import it.acsoftware.hyperiot.base.service.entity.HyperIoTBaseEntitySystemServiceImpl;
import it.acsoftware.hyperiot.permission.api.PermissionRepository;
import it.acsoftware.hyperiot.permission.api.PermissionSystemApi;
import it.acsoftware.hyperiot.permission.model.Permission;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.persistence.NoResultException;
import java.util.Collection;
import java.util.List;


/**
 * @author Aristide Cittadino Implementation class of the PermissionSystemApi
 * interface. It is used to implements all additional methods to
 * interact with the persistence layer.
 */
@Component(service = PermissionSystemApi.class, immediate = true)
public class PermissionSystemServiceImpl extends HyperIoTBaseEntitySystemServiceImpl<Permission>
        implements PermissionSystemApi {

    /**
     * Injecting the PermissionRepository to interact with persistence layer
     */
    private PermissionRepository repository;

    /**
     * Constructor for a PermissionSystemServiceImpl
     */
    public PermissionSystemServiceImpl() {
        super(Permission.class);
    }

    /**
     * Return the current repository
     */
    public PermissionRepository getRepository() {
        getLog().debug( "invoking getRepository, returning: {}", this.repository);
        return repository;
    }

    /**
     * @param repository The current value to interact with persistence layer
     */
    @Reference
    protected void setRepository(PermissionRepository repository) {
        getLog().debug( "invoking setRepository, setting: {}", repository);
        this.repository = repository;
    }

    /**
     * Find a permission by a specific role and resource
     *
     * @param role     parameter required to find role by roleId
     * @param resource parameter required to find a resource
     * @return Permission if found
     */
    public Permission findByRoleAndResource(HyperIoTRole role, HyperIoTResource resource) {
        getLog().debug( "invoking findByRoleAndResource role: {} Resource: {}"
                , new Object[]{role.getName(), resource.getResourceName()});
        try {
            return repository.findByRoleAndResource(role, resource);
        } catch (NoResultException e) {
            getLog().debug( "No result searching for permission for role: {} Resource: {}"
                    , new Object[]{role.getName(), resource.getResourceName()});
            return null;
        }
    }

    /**
     * Find a permission by a specific role and resource name
     *
     * @param role         parameter required to find role by roleId
     * @param resourceName parameter required to find a resource name
     * @return Permission if found
     */
    public Permission findByRoleAndResourceName(HyperIoTRole role, String resourceName) {
        getLog().debug( "invoking findByRoleAndResourceName role: {} Resource: {}"
                , new Object[]{role.getName(), resourceName});
        try {
            return repository.findByRoleAndResourceName(role, resourceName);
        } catch (NoResultException e) {
            getLog().debug( "No result searching for permission for role " + role.getName()
                    + " Resource: " + resourceName);
            return null;
        }
    }

    /**
     * Find a permission by a specific role, resource name and resource id
     *
     * @param role         parameter required to find role by roleId
     * @param resourceName parameter required to find a resource name
     * @param id           parameter required to find a resource id
     * @return Permission if found
     */
    public Permission findByRoleAndResourceNameAndResourceId(HyperIoTRole role, String resourceName,
                                                             long id) {
        getLog().debug( "invoking findByRoleAndResourceNameAndResourceId role: {} Resource: {}"
                , new Object[]{role.getName(), resourceName});
        try {
            return repository.findByRoleAndResourceNameAndResourceId(role, resourceName, id);
        } catch (NoResultException e) {
            getLog().debug( "No result searching for permission for role " + role.getName()
                    + " Resource: " + resourceName + " with id: " + id);
            return null;
        }
    }

    /**
     * Find a permission by a specific role, resource name and resource id
     *
     * @param role parameter required to find role by roleId
     * @return Permission if found
     */
    @Override
    public Collection<Permission> findByRole(HyperIoTRole role) {
        getLog().debug( "invoking findByRoleAndResourceName role: {}", role.getName());
        try {
            return repository.findByRole(role);
        } catch (NoResultException e) {
            getLog().debug( "No result searching for permission for role {}", role.getName());
            return null;
        }
    }

    /**
     * @param roleName
     * @param actions  List actions to add as permissions
     */
    @Override
    public void checkOrCreateRoleWithPermissions(String roleName, List<HyperIoTAction> actions) {
        this.repository.checkOrCreateRoleWithPermissions(roleName, actions);
    }

    @Override
    public void checkOrCreateRoleWithPermissionsSpecificToEntity(String roleName, long entityId, List<HyperIoTAction> actions) {
        this.repository.checkOrCreateRoleWithPermissionsSpecificToEntity(roleName,entityId,actions);
    }

    @Override
    public boolean existPermissionSpecificToEntity(String resourceName, long resourceId) {
        return this.repository.existPermissionSpecificToEntity(resourceName,resourceId);
    }
}
