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

package it.acsoftware.hyperiot.role.service;

import it.acsoftware.hyperiot.base.action.util.HyperIoTActionsUtil;
import it.acsoftware.hyperiot.base.action.util.HyperIoTCrudAction;
import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.base.exception.HyperIoTEntityNotFound;
import it.acsoftware.hyperiot.base.exception.HyperIoTUnauthorizedException;
import it.acsoftware.hyperiot.base.security.annotations.AllowPermissions;
import it.acsoftware.hyperiot.base.security.util.HyperIoTSecurityUtil;
import it.acsoftware.hyperiot.base.service.entity.HyperIoTBaseEntityServiceImpl;
import it.acsoftware.hyperiot.huser.api.HUserSystemApi;
import it.acsoftware.hyperiot.huser.model.HUser;
import it.acsoftware.hyperiot.role.actions.HyperIoTRoleAction;
import it.acsoftware.hyperiot.role.api.RoleApi;
import it.acsoftware.hyperiot.role.api.RoleSystemApi;
import it.acsoftware.hyperiot.role.model.Role;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.persistence.NoResultException;
import java.util.Collection;


/**
 * @author Aristide Cittadino Implementation class of RoleApi interface. It is
 * used to implement all additional methods in order to interact with
 * the system layer.
 */
@Component(service = RoleApi.class, immediate = true)
public final class RoleServiceImpl extends HyperIoTBaseEntityServiceImpl<Role> implements RoleApi {
    private static final String resourceName = Role.class.getName();
    /**
     * Injecting the RoleSystemService to use methods in RoleSystemApi interface
     */
    private RoleSystemApi systemService;
    /**
     * Injecting the HUserSystemApi to use methods in HUserSystemApi interface
     */
    private HUserSystemApi userSystemService;

    /**
     * Constructor for a RoleServiceImpl
     */
    public RoleServiceImpl() {
        super(Role.class);
    }

    /**
     * @return The current RoleSystemService
     */
    public RoleSystemApi getSystemService() {
        getLog().debug("invoking getSystemService, returning: {}", this.systemService);
        return systemService;
    }

    /**
     * @param roleSystemService Injecting via OSGi DS current RoleSystemService
     */
    @Reference
    protected void setSystemService(RoleSystemApi roleSystemService) {
        getLog().debug("invoking setSystemService, setting: {}", systemService);
        this.systemService = roleSystemService;
    }

    /**
     * @param hUserSystemService Injecting via OSGi DS current HUserSystemService
     */
    @Reference
    protected void setUserSystemService(HUserSystemApi hUserSystemService) {
        getLog().debug("invoking setUserSystemService, setting: {}", hUserSystemService);
        this.userSystemService = hUserSystemService;
    }

    /**
     * Collection of user roles obtained via query
     *
     * @param userId parameter required to find all user roles
     * @return collection of user roles
     */
    @AllowPermissions(actions = HyperIoTCrudAction.Names.FINDALL, idParamIndex = 0, checkById = true, systemApiRef = "it.acsoftware.hyperiot.huser.api.HUserSystemApi")
    public Collection<Role> getUserRoles(long userId, HyperIoTContext ctx) {
        getLog().debug("invoking getUserRoles, by: " + userId);
        try {
            return this.systemService.getUserRoles(userId);
        } catch (NoResultException e) {
            getLog().debug("invoking getUserRoles, Entity not found! ");
            throw new HyperIoTEntityNotFound();
        }
    }

    /**
     * Save a user role
     *
     * @param userId parameter required to find an existing user
     * @param roleId parameter required to save a user role
     * @param ctx    user context of HyperIoT platform
     * @return the user's role saved
     */
    @AllowPermissions(actions = HyperIoTRoleAction.Names.ASSIGN_MEMBERS, checkById = true, idParamIndex = 1)
    public Role saveUserRole(long userId, long roleId, HyperIoTContext ctx) {
        getLog().debug("invoking saveUserRole, save role: {}  from user: {}", new Object[]{roleId, userId});
        Role r = null;
        try {
            HUser u = this.userSystemService.find(userId, ctx);
            r = this.systemService.find(roleId, ctx);
            u.addRole(r);
            userSystemService.update(u, ctx);
        } catch (NoResultException e) {
            getLog().debug("invoking saveUserRole, HUser not found! ");
            throw new HyperIoTEntityNotFound();
        }
        return r;
    }

    /**
     * Remove a user role
     *
     * @param userId parameter required to find an existing role
     * @param roleId parameter required to delete a user role
     * @param ctx    user context of HyperIoT platform
     * @return the user's role deleted
     */
    @AllowPermissions(actions = HyperIoTRoleAction.Names.REMOVE_MEMBERS, checkById = true, idParamIndex = 1)
    public Role removeUserRole(long userId, long roleId, HyperIoTContext ctx) {
        getLog().debug("invoking removeUserRole, remove role: {} from user: {}", new Object[]{roleId, userId});
        Role r;
        try {
            HUser u = this.userSystemService.find(userId, ctx);
            r = this.systemService.find(roleId, ctx);
            u.removeRole(r);
            userSystemService.update(u, ctx);
        } catch (NoResultException e) {
            getLog().debug("invoking removeUserRole, HUser not found! ");
            throw new HyperIoTEntityNotFound();
        }
        return r;
    }

}
