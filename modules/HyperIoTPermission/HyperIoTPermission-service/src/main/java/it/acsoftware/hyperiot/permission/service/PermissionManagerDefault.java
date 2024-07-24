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

package it.acsoftware.hyperiot.permission.service;

import it.acsoftware.hyperiot.base.action.util.HyperIoTActionsUtil;
import it.acsoftware.hyperiot.base.api.*;
import it.acsoftware.hyperiot.base.api.entity.*;
import it.acsoftware.hyperiot.base.util.HyperIoTConstants;
import it.acsoftware.hyperiot.base.util.HyperIoTUtil;
import it.acsoftware.hyperiot.huser.actions.HyperIoTHUserAction;
import it.acsoftware.hyperiot.huser.api.HUserSystemApi;
import it.acsoftware.hyperiot.huser.model.HUser;
import it.acsoftware.hyperiot.permission.api.PermissionSystemApi;
import it.acsoftware.hyperiot.permission.model.Permission;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.NoResultException;
import java.util.*;

/**
 * @author Aristide Cittadino Implementation class of HyperIoTPermissionManager
 * interface. It is used to implement all methods able to check if a
 * user has permissions through every actions of the HyperIoTAction.
 */
@Component(service = HyperIoTPermissionManager.class, property = {
        HyperIoTConstants.OSGI_PERMISSION_MANAGER_IMPLEMENTATION
                + "=default"}, immediate = true, servicefactory = false)
public class PermissionManagerDefault implements HyperIoTPermissionManager {
    private Logger log = LoggerFactory.getLogger(PermissionManagerDefault.class.getName());
    /**
     * Injecting the PermissionSystemService to use methods in PermissionSystemApi
     * interface
     */
    private PermissionSystemApi systemService;
    /**
     * Injecting the HUserSystemService to use methods in HUserSystemApi interface
     */
    private HUserSystemApi huserSystemService;

    /**
     * Injecting the SharedEntitySystemService to use methods in SharedEntitySystemApi interface
     */
    private HyperIoTSharingEntityService sharedEntityService;


    /**
     * @param username
     * @param rolesNames
     * @return
     */
    @Override
    public boolean userHasRoles(String username, String[] rolesNames) {
        if (username == null || username.length() == 0)
            return false;
        HUser user = huserSystemService.findUserByUsername(username);
        if (user == null)
            return false;
        Collection<String> rolesNamesCollection = Arrays.asList(rolesNames);
        return user.getRoles().stream().anyMatch(r -> rolesNamesCollection.contains(r.getName()));
    }

    /**
     * Checks if an existing user has permissions for action of HyperIoTAction.
     * Moreover every user, if protected, is set as a base entity of the HyperIoT
     * platform.
     *
     * @param username parameter that indicates the username of user
     * @param entity   parameter that indicates the resource name of user
     * @param action   interaction of the user with HyperIoT platform
     */
    @Override
    public boolean checkPermission(String username, HyperIoTResource entity,
                                   HyperIoTAction action) {
        if (!HyperIoTPermissionManager.isProtectedEntity(entity.getResourceName()))
            return true;
        log.debug(
                "invoking checkPermission User {} Entity Resource Name: {}", new Object[]{username, entity.getResourceName(), action.getResourceName(), action.getActionId()});
        if (!HyperIoTPermissionManager.isProtectedEntity(entity))
            return true;

        if (username == null || entity == null || action == null)
            return false;

        HyperIoTUser user = this.huserSystemService.findUserByUsername(username);

        // every protected entity is a base entity
        HyperIoTProtectedEntity entityResource = (HyperIoTProtectedEntity) entity;

        return hasPermission(user, entityResource, action);
    }

    /**
     * Checks if an existing user has permissions for action of HyperIoTAction.
     *
     * @param username     parameter that indicates the username of user
     * @param resourceName parameter that indicates the resource name of action
     * @param action       interaction of the user with HyperIoT platform
     */
    @Override
    public boolean checkPermission(String username, String resourceName, HyperIoTAction action) {
        if (!HyperIoTPermissionManager.isProtectedEntity(resourceName))
            return true;
        log.debug(
                "invoking checkPermission User {} Entity Resource Name: {} Action Name: {}  actionId: {}", new Object[]{username, resourceName, action.getResourceName(), action.getActionId()});
        if (username == null || resourceName == null || action == null)
            return false;

        if (!HyperIoTPermissionManager.isProtectedEntity(resourceName))
            return true;

        return hasPermission(username, resourceName, action);
    }

    /**
     * Checks if an existing user has permissions for action of HyperIoTAction.
     *
     * @param username parameter that indicates the username of user
     * @param resource parameter that indicates the resource name of action
     * @param action   interaction of the user with HyperIoT platform
     */
    @Override
    public boolean checkPermission(String username, Class<? extends HyperIoTResource> resource,
                                   HyperIoTAction action) {
        if (!HyperIoTPermissionManager.isProtectedEntity(resource.getName()))
            return true;
        log.debug(
                "invoking checkPermission User {} Entity Resource Name: {} Action Name: {}  actionId: {}", new Object[]{username, resource.getName(), action.getResourceName(), action.getActionId()});
        if (username == null || resource == null || action == null)
            return false;

        return hasPermission(username, resource.getName(), action);
    }

    /**
     * Returns a map containing all actiona available for every resource
     *
     * @param username
     * @param entityPks
     * @return
     */
    @Override
    public Map<String, Map<String, Map<String, Boolean>>> entityPermissionMap(String username, Map<String, List<Long>> entityPks) {
        Map<String, Map<String, Map<String, Boolean>>> userPermissionMap = new HashMap<>();
        entityPks.keySet().forEach(apiClass -> {
            userPermissionMap.computeIfAbsent(apiClass, key -> new HashMap<>());
            HyperIoTBaseEntitySystemApi<?> baseEntitySystemApi = HyperIoTUtil.findEntitySystemApi(apiClass);
            if (baseEntitySystemApi != null) {
                entityPks.get(apiClass).forEach(entityId -> {
                    String entityIdsSts = String.valueOf(entityId);
                    userPermissionMap.get(apiClass).computeIfAbsent(entityIdsSts, key -> new HashMap<>());
                    try {
                        HyperIoTBaseEntity entity = baseEntitySystemApi.find(entityId, null);
                        if (entity != null) {
                            List<HyperIoTAction> actions = HyperIoTActionsUtil.getHyperIoTActions(apiClass);
                            actions.forEach(action -> {
                                boolean hasPermission = checkPermission(username, entity, action);
                                userPermissionMap.get(apiClass).get(entityIdsSts).put(action.getActionName(), hasPermission);
                            });
                        }
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                });
            }
        });
        return userPermissionMap;
    }

    /**
     * @param username     parameter that indicates the username of entity
     * @param resourceName parameter that indicates the resource name of action
     * @param action       interaction of the user with HyperIoT platform
     * @param entities     List of entities User must own in order to perform the action
     * @return
     */
    public boolean checkPermissionAndOwnership(String username, String resourceName, HyperIoTAction action, HyperIoTResource... entities) {
        boolean hasPermission = false;
        if (!HyperIoTPermissionManager.isProtectedEntity(resourceName))
            hasPermission = true;
        else
            hasPermission = checkPermission(username, resourceName, action);

        if (hasPermission && entities != null) {
            HyperIoTUser user = this.huserSystemService.findUserByUsername(username);
            for (int i = 0; i < entities.length && hasPermission; i++) {
                hasPermission = hasPermission && user != null && entities[i] != null && checkUserOwnsResource(user, entities[i]);
            }
        }
        return hasPermission;
    }

    /**
     * @param username parameter that indicates the username of entity
     * @param resource parameter that indicates the resource on which the action should be performed
     * @param action   interaction of the user with HyperIoT platform
     * @param entities List of other entities User must own in order to perform the action
     * @return
     */
    public boolean checkPermissionAndOwnership(String username, HyperIoTResource resource, HyperIoTAction action, HyperIoTResource... entities) {
        boolean hasPermission = false;
        if (!HyperIoTPermissionManager.isProtectedEntity(resource.getResourceName()))
            hasPermission = true;
        else
            hasPermission = checkPermission(username, resource.getResourceName(), action);

        if (hasPermission && entities != null) {
            HyperIoTUser user = this.huserSystemService.findUserByUsername(username);
            for (int i = 0; i < entities.length && hasPermission; i++) {
                hasPermission = hasPermission && user != null && entities[i] != null && checkUserOwnsResource(user, entities[i]);
            }
        }
        return hasPermission;
    }


    /**
     * @return The current PermissionSystemService
     */
    public PermissionSystemApi getSystemService() {
        log.debug("invoking getSystemService, returning: {}", this.systemService);
        return systemService;
    }

    /**
     * @param systemService Injecting via OSGi DS current PermissionSystemService
     */
    @Reference
    protected void setSystemService(PermissionSystemApi systemService) {
        log.debug("invoking setSystemService, setting: {}", systemService);
        this.systemService = systemService;
    }

    /**
     * @return The current HUserSystemService
     */
    public HUserSystemApi getHuserSystemService() {
        log.debug("invoking getSystemService, returning: {}", this.huserSystemService);
        return huserSystemService;
    }

    /**
     * @param huserSystemService Injecting via OSGi DS current HUserSystemService
     */
    @Reference
    public void setHuserSystemService(HUserSystemApi huserSystemService) {
        log.debug("invoking setHUserSystemService, setting: {}", huserSystemService);
        this.huserSystemService = huserSystemService;
    }

    /**
     * @param sharedEntitySystemService Injecting via OSGi DS current SharedEntitySystemService
     */
    @Reference
    public void setSharedEntitySystemService(HyperIoTSharingEntityService sharedEntitySystemService) {
        log.debug("invoking setSharedEntitySystemService, setting: {}", sharedEntitySystemService);
        this.sharedEntityService = sharedEntitySystemService;
    }

    /**
     * Find an existing user by username. Returns actions permission by user role.
     *
     * @param username     parameter required to find a user by his username
     * @param resourceName parameter that indicates the resource name
     * @param action       interaction of the user with HyperIoT platform
     * @return Actions permission by user
     */
    private boolean hasPermission(String username, String resourceName, HyperIoTAction action) {
        HyperIoTUser user = this.huserSystemService.findUserByUsername(username);
        if (user == null) {
            return false;
        }

        if (user.isAdmin())
            return true;

        if (user.getRoles() == null)
            return false;

        Iterator<? extends HyperIoTRole> it = user.getRoles().iterator();

        while (it.hasNext()) {
            HyperIoTRole r = it.next();
            Permission permission = systemService.findByRoleAndResourceName(r, resourceName);
            if (permission != null
                    && hasPermission(permission.getActionIds(), action.getActionId()))
                return true;
        }
        return false;
    }

    /**
     * Find an existing user by username. Returns actions permission by user role.
     *
     * @param user   parameter required to find a user by his username
     * @param action interaction of the user with HyperIoT platform
     * @return Actions permission by user
     */
    private boolean hasPermission(HyperIoTUser user, HyperIoTProtectedEntity entity,
                                  HyperIoTAction action) {
        if (user == null) {
            return false;
        }
        if (user.isAdmin())
            return true;

        if (user.getRoles() == null)
            return false;

        Iterator<? extends HyperIoTRole> it = user.getRoles().iterator();
        while (it.hasNext()) {
            HyperIoTRole r = it.next();
            Permission permission = null;

            try {
                permission = systemService.findByRoleAndResourceName(r, entity.getResourceName());
            } catch (NoResultException e) {
                log.warn("No permission found for: {}  on resource {}"
                        , new Object[]{r.getName(), entity.getResourceName()});
            }

            Permission userPermission = null;
            try {
                userPermission = systemService.findByUserAndResourceName(user, entity.getResourceName());
            } catch (NoResultException e) {
                log.warn("No permission found for: {}  on resource {}"
                        , new Object[]{user.getUsername(), entity.getResourceName()});
            }

            Permission permissionSpecific = null;
            try {
                permissionSpecific = systemService.findByRoleAndResourceNameAndResourceId(r,
                        entity.getResourceName(), entity.getId());
            } catch (NoResultException e) {
                log.warn("No specific permission found for: {}  on resource {}"
                        , new Object[]{r.getName(), entity.getResourceName()});
            }

            Permission userPermissionSpecific = null;
            try {
                userPermissionSpecific = systemService.findByUserAndResourceNameAndResourceId(user,
                        entity.getResourceName(), entity.getId());
            } catch (NoResultException e) {
                log.warn("No specific permission found for: {}  on resource {}"
                        , new Object[]{user.getUsername(), entity.getResourceName()});
            }

            Permission permissionImpersonation = null;
            try {
                permissionImpersonation = systemService.findByRoleAndResourceName(r,
                        HUser.class.getName());
            } catch (NoResultException e) {
                log.warn("No impersonification permission found for: {}  on resource {}"
                        , new Object[]{r.getName(), entity.getResourceName()});
            }
            // it initialize the value with the general value based on resource name
            // general permission is : permission based on the role or permission based on user
            boolean hasGeneralPermission = (permission != null
                    && hasPermission(permission.getActionIds(), action.getActionId())) || (userPermission != null && hasPermission(userPermission.getActionIds(), action.getActionId()));
            // entity permission is specific if it is found on role or user
            boolean hasEntityPermission = (permissionSpecific != null
                    && hasPermission(permissionSpecific.getActionIds(), action.getActionId())) || (userPermissionSpecific != null
                    && hasPermission(userPermissionSpecific.getActionIds(), action.getActionId()));

            boolean existPermissionSpecificToEntity = this.systemService.existPermissionSpecificToEntity(entity.getResourceName(), entity.getId());

            HyperIoTAction impersonateAction = HyperIoTActionsUtil
                    .getHyperIoTAction(HUser.class.getName(), HyperIoTHUserAction.IMPERSONATE);
            boolean userOwnsResource = checkUserOwnsResource(user, entity);
            boolean userSharesResource = checkUserSharesResource(user, entity);
            boolean hasImpersonationPermission = permissionImpersonation != null && hasPermission(
                    permissionImpersonation.getActionIds(), impersonateAction.getActionId());
            // The value is true only if the entity permission exists and contains the
            // actionId, or if
            // the entity permission doesn't exists then the rule follow the
            // generalPermission
            // AND if the resource is an owned resource is accessed by the right user or the
            // accessing user has the impersonation permission
            if ((((permissionSpecific != null || userPermissionSpecific != null) && hasEntityPermission) || (permissionSpecific == null && userPermissionSpecific == null && hasGeneralPermission))
                    && (userOwnsResource ||
                    ((userSharesResource && !existPermissionSpecificToEntity && hasGeneralPermission) || (userSharesResource && (permissionSpecific != null || userPermissionSpecific != null) && hasEntityPermission)) ||
                    hasImpersonationPermission))
                return true;
        }
        return false;
    }

    /**
     * Performs a bitwise operation between the permissionActionIds and the
     * actionId. It manipulate the bits with & operator used to compare bits of each
     * operand.
     *
     * @param permissionActionIds parameter that indicates the Permission actionIds
     * @param actionId            parameter that indicates the id of HyperIoTAction
     */
    private boolean hasPermission(int permissionActionIds, int actionId) {
        boolean hasPermission = (permissionActionIds & actionId) == actionId;
        log.debug(
                "invoking hasPermission permissionActionIds & actionId == actionId {}",
                new Object[]{permissionActionIds, actionId, hasPermission});
        return hasPermission;
    }

    /**
     * @param user     the current logged user
     * @param resource the current resource
     * @return true if the resource is owned by the current logged user or the
     * resource is not a owned resource, false otherwise.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public boolean checkUserOwnsResource(HyperIoTUser user, Object resource) {
        if (user.isAdmin())
            return true;

        HyperIoTBaseEntity entity = (HyperIoTBaseEntity) resource;
        HyperIoTUser resourceOwner = null;
        //used when user shares only child entities
        boolean userSharesResource = checkUserSharesResource(user, entity);
        // looks up for a persisted entity in the hierarchy chain
        while (true) {
            // double check if the passed entity is consistent (must belong to `user`)
            if (entity instanceof HyperIoTOwnedResource) {
                resourceOwner = ((HyperIoTOwnedResource) entity).getUserOwner();
                if (!userSharesResource && resourceOwner != null && resourceOwner.getId() != 0
                        && user.getId() != resourceOwner.getId()) {
                    return false;
                } else
                    break;
            } else if (entity instanceof HyperIoTOwnedChildResource) {
                HyperIoTOwnedChildResource child = (HyperIoTOwnedChildResource) entity;
                if (child.getParent() != null)
                    entity = child.getParent();
                else
                    break;
            } else
                break;
        }
        if (entity.getId() != 0) {
            // load the persisted entity
            BundleContext context = HyperIoTUtil.getBundleContext(this);
            ServiceReference serviceReference = context
                    .getServiceReference(entity.getSystemApiClassName());
            HyperIoTBaseEntitySystemApi service = (HyperIoTBaseEntitySystemApi) context
                    .getService(serviceReference);
            HyperIoTBaseEntity persistedEntity = service.find(entity.getId(), null);
            // verify the owner
            if (persistedEntity instanceof HyperIoTOwnedResource) {
                resourceOwner = ((HyperIoTOwnedResource) persistedEntity).getUserOwner();
            } else if (persistedEntity instanceof HyperIoTOwnedChildResource) {
                HyperIoTOwnedChildResource persistedChildEntity = (HyperIoTOwnedChildResource) persistedEntity;
                if (persistedChildEntity != null && persistedChildEntity.getParent() != null) {
                    // retry with the parent resource
                    return (persistedChildEntity.getParent() == null || checkUserOwnsResource(user, persistedChildEntity.getParent()))
                            && checkUserOwnsResource(user, persistedChildEntity.getParent());
                }
            } else {
                // resource is not owned so check can pass
                return (persistedEntity != null);
            }
        } else {
            return true;
        }

        return (user != null && resourceOwner != null && (user.getId() == resourceOwner.getId() || userSharesResource));
    }

    /**
     * @param user     the current logged user
     * @param resource the current resource
     * @return true if the resource is shared to the current logged user or the
     * resource is not a shared resource, false otherwise.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private boolean checkUserSharesResource(HyperIoTUser user, Object resource) {
        if (user.isAdmin())
            return true;
        HyperIoTBaseEntity entity = (HyperIoTBaseEntity) resource;
        List<HyperIoTUser> sharingUsers = new ArrayList<>();
        // looks up for a persisted entity in the hierarchy chain
        boolean loop = true;
        while (loop) {
            // double check if the passed entity is consistent (must be shared to `user`)
            if (entity instanceof HyperIoTSharedEntity) {
                sharingUsers = sharedEntityService.getSharingUsers(entity.getResourceName(), entity.getId(), null);
                if (sharingUsers.stream().noneMatch(u -> u.getId() == user.getId())) {
                    return false;
                } else
                    loop = false;
            } else if (entity instanceof HyperIoTOwnedChildResource) {
                HyperIoTOwnedChildResource child = (HyperIoTOwnedChildResource) entity;
                if (child.getParent() != null)
                    entity = child.getParent();
                else
                    loop = false;
                ;
            } else
                loop = false;
            ;
        }
        if (entity.getId() != 0) {
            // load the persisted entity
            BundleContext context = HyperIoTUtil.getBundleContext(this);
            ServiceReference serviceReference = context
                    .getServiceReference(entity.getSystemApiClassName());
            HyperIoTBaseEntitySystemApi service = (HyperIoTBaseEntitySystemApi) context
                    .getService(serviceReference);
            HyperIoTBaseEntity persistedEntity = service.find(entity.getId(), null);
            // verify the owner
            if (persistedEntity instanceof HyperIoTSharedEntity) {
                sharingUsers = sharedEntityService.getSharingUsers(entity.getResourceName(), entity.getId(), null);
            } else if (persistedEntity instanceof HyperIoTOwnedChildResource) {
                HyperIoTOwnedChildResource persistedChildEntity = (HyperIoTOwnedChildResource) persistedEntity;
                if (persistedChildEntity != null && persistedChildEntity.getParent() != null) {
                    // retry with the parent resource
                    return (persistedChildEntity.getParent() == null || checkUserOwnsResource(user, persistedChildEntity.getParent()))
                            && checkUserOwnsResource(user, persistedChildEntity.getParent());
                } else {
                    // resource is not shared so check can pass
                    return (persistedEntity != null);
                }
            }
        } else {
            return false;
        }
        return (user != null && sharingUsers.stream().anyMatch(u -> u.getId() == user.getId()));
    }

}
