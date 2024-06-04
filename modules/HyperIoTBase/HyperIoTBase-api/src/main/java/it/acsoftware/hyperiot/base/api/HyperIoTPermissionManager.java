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

package it.acsoftware.hyperiot.base.api;

import it.acsoftware.hyperiot.base.api.entity.HyperIoTProtectedEntity;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * @author Aristide Cittadino Generic Interface Component for
 * HyperIoTPermissionManager. This interface define all methods able to
 * check if a user has permissions for each actions of the HyperIoT
 * platform.
 */
public interface HyperIoTPermissionManager {
    static final org.slf4j.Logger log = LoggerFactory.getLogger(HyperIoTPermissionManager.class.getName());

    /**
     * Checks if the user corresponding to the username has the specified roles
     *
     * @param rolesNames
     * @return
     */
    boolean userHasRoles(String username, String[] rolesNames);

    /**
     * Checks if an existing user has permissions for action of HyperIoTAction.
     * Moreover every user, if protected, is set as a base entity of the HyperIoT
     * platform.
     *
     * @param username parameter that indicates the username of entity
     * @param entity   parameter that indicates the resource on which the action should be performed
     * @param action   interaction of the entity with HyperIoT platform
     */
    boolean checkPermission(String username, HyperIoTResource entity, HyperIoTAction action);

    /**
     * Checks if an existing user has permissions for action of HyperIoTAction.
     *
     * @param username parameter that indicates the username of entity
     * @param resource parameter that indicates the resource on which the action should be performed
     * @param action   interaction of the user with HyperIoT platform
     */
    boolean checkPermission(String username, Class<? extends HyperIoTResource> resource,
                            HyperIoTAction action);

    /**
     * Checks if an existing user has permissions for action of HyperIoTAction.
     *
     * @param username     parameter that indicates the username of entity
     * @param resourceName parameter that indicates the resource name of action
     * @param action       interaction of the user with HyperIoT platform
     */
    boolean checkPermission(String username, String resourceName, HyperIoTAction action);

    /**
     * Returns a permission map of actions available for a specific user on specific resources
     * ex.
     * {
     *   "resourceA":{
     *        "38":{
     *          "save":true,
     *          "update":true,
     *          "find":true
     *       },
     *       "54":{
     *           "save":false,
     *           "update":true,
     *           "find":true
     *       }
     *   }
     * }
     * Each entry is the action name value.
     * @param username
     * @param entityPks map with key the entity class and a list of ids
     * @return
     */
    Map<String, Map<String, Map<String, Boolean>>> entityPermissionMap(String username, Map<String, List<Long>> entityPks);

    /**
     * @param username     parameter that indicates the username of entity
     * @param resourceName parameter that indicates the resource name of action
     * @param action       interaction of the user with HyperIoT platform
     * @param entities     List of entities User must own in order to perform the action
     * @return
     */
    boolean checkPermissionAndOwnership(String username, String resourceName, HyperIoTAction action, HyperIoTResource... entities);

    /**
     * @param username parameter that indicates the username of entity
     * @param resource parameter that indicates the resource on which the action should be performed
     * @param action   interaction of the user with HyperIoT platform
     * @param entities List of other entities User must own in order to perform the action
     * @return
     */
    boolean checkPermissionAndOwnership(String username, HyperIoTResource resource, HyperIoTAction action, HyperIoTResource... entities);

    /**
     * Checks wether resource is owned by the user
     *
     * @param user     User that should own the resource
     * @param resource Object that should be owned by the user
     * @return true if the user owns the resource
     */
    boolean checkUserOwnsResource(HyperIoTUser user, Object resource);

    /**
     * Return the protected entity of HyperIoT platform
     *
     * @param entity parameter that indicates the protected entity of HyperIoT
     *               platform
     * @return protected entity
     */
    static boolean isProtectedEntity(Object entity) {
        log.debug("invoking Permission Manager getProtectedEntity "
            + entity.getClass().getSimpleName());
        if (entity instanceof HyperIoTProtectedEntity)
            return true;

        if (entity instanceof HyperIoTProtectedResource)
            return true;

        return false;
    }

    /**
     * Return the protected resource name of entity of HyperIoT platform
     *
     * @param resourceName parameter that indicates the protected resource name of
     *                     entity of HyperIoT platform
     * @return protected resource name of entity
     */
    static boolean isProtectedEntity(String resourceName) {
        log.debug("invoking Permission getProtectedEntity " + resourceName);
        try {
            boolean isAssignable = HyperIoTProtectedEntity.class.isAssignableFrom(Class.forName(resourceName));
            isAssignable = isAssignable || HyperIoTProtectedResource.class.isAssignableFrom(Class.forName(resourceName));
            return isAssignable;
        } catch (ClassNotFoundException e) {
            log.warn(e.getMessage());
        }
        // return the most restrictive condition
        return true;
    }
}
