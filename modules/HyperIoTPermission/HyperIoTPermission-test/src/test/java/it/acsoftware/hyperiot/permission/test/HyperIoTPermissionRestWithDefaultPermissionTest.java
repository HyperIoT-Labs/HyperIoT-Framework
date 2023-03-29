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

package it.acsoftware.hyperiot.permission.test;

import it.acsoftware.hyperiot.base.api.authentication.AuthenticationApi;
import it.acsoftware.hyperiot.base.action.util.HyperIoTActionsUtil;
import it.acsoftware.hyperiot.base.action.util.HyperIoTCrudAction;
import it.acsoftware.hyperiot.base.api.HyperIoTAction;
import it.acsoftware.hyperiot.base.api.HyperIoTUser;
import it.acsoftware.hyperiot.base.model.HyperIoTBaseError;
import it.acsoftware.hyperiot.base.service.rest.HyperIoTBaseRestApi;
import it.acsoftware.hyperiot.base.test.HyperIoTTestConfigurationBuilder;
import it.acsoftware.hyperiot.huser.api.HUserSystemApi;
import it.acsoftware.hyperiot.huser.model.HUser;
import it.acsoftware.hyperiot.huser.service.rest.HUserRestApi;
import it.acsoftware.hyperiot.huser.test.util.HyperIoTHUserTestUtils;
import it.acsoftware.hyperiot.permission.api.PermissionSystemApi;
import it.acsoftware.hyperiot.permission.model.Permission;
import it.acsoftware.hyperiot.permission.service.rest.PermissionRestApi;
import it.acsoftware.hyperiot.permission.test.util.HyperIoTPermissionTestUtil;
import it.acsoftware.hyperiot.role.api.RoleSystemApi;
import it.acsoftware.hyperiot.role.model.Role;
import it.acsoftware.hyperiot.role.service.rest.RoleRestApi;
import org.apache.karaf.features.FeaturesService;
import org.apache.karaf.itests.KarafTestSupport;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.*;

import static it.acsoftware.hyperiot.permission.test.HyperIoTPermissionConfiguration.*;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HyperIoTPermissionRestWithDefaultPermissionTest extends KarafTestSupport {

    //force global configuration
    public Option[] config() {
        return null;
    }

    public void impersonateUser(HyperIoTBaseRestApi restApi, HyperIoTUser user) {
        restApi.impersonate(user);
    }

    @Test
    public void test000_hyperIoTFrameworkShouldBeInstalled() {
        // assert on an available service
        assertServiceAvailable(FeaturesService.class,0);
        String features = executeCommand("feature:list -i");
        assertContains("HyperIoTBase-features ", features);
        assertContains("HyperIoTMail-features ", features);
        assertContains("HyperIoTAuthentication-features ", features);
        assertContains("HyperIoTPermission-features ", features);
        assertContains("HyperIoTHUser-features ", features);
        assertContains("HyperIoTCompany-features ", features);
        assertContains("HyperIoTAssetCategory-features", features);
        assertContains("HyperIoTAssetTag-features", features);
        assertContains("HyperIoTSharedEntity-features", features);
        String datasource = executeCommand("jdbc:ds-list");
//		System.out.println(executeCommand("bundle:list | grep HyperIoT"));
        assertContains("hyperiot", datasource);
    }

    @Test
    public void test001_permissionModuleShouldWork() {
        PermissionRestApi permissionRestService = getOsgiService(PermissionRestApi.class);
        // the following call sayHi checks if Permission module working correctly
        HUser huser = huserWithDefaultPermissionInHyperIoTFramework(true);
        Assert.assertNotEquals(0, huser.getId());
        Assert.assertTrue(huser.isActive());
        this.impersonateUser(permissionRestService, huser);
        Response response = permissionRestService.sayHi();
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("HyperIoT Permission Module works!", response.getEntity());
    }

    // Permission action save: 1 not assigned in default permission
    @Test
    public void test002_createNewPermissionWithDefaultPermissionShouldFail() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // huser, with default permission, tries to save new Permission with the following call savePermission
        // huser to save a new permission needs the "save permission" permission
        // response status code '403' HyperIoTUnauthorizedException
        HUser huser = huserWithDefaultPermissionInHyperIoTFramework(true);
        Assert.assertNotEquals(0, huser.getId());
        Assert.assertTrue(huser.isActive());
        Role role = createRole();
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(permissionResourceName,
                HyperIoTCrudAction.SAVE);
        Permission permission = new Permission();
        permission.setName(action.getActionName());
        permission.setActionIds(action.getActionId());
        permission.setEntityResourceName(action.getResourceName());
        permission.setRole(role);
        this.impersonateUser(permissionRestApi, huser);
        Response restResponse = permissionRestApi.savePermission(permission);
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }


    // Permission action update: 2 not assigned in default permission
    @Test
    public void test003_updatePermissionWithDefaultPermissionShouldFail() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // huser, with default permission, tries to update Permission with the following call updatePermission
        // huser to update permission needs the "update permission" permission
        // response status code '403' HyperIoTUnauthorizedException
        HUser huser = huserWithDefaultPermissionInHyperIoTFramework(true);
        Assert.assertNotEquals(0, huser.getId());
        Assert.assertTrue(huser.isActive());

        Permission permission = createPermission();
        Assert.assertNotEquals(0, permission.getId());
        Assert.assertNotEquals(0, permission.getActionIds());

        permission.setName("name edited");
        this.impersonateUser(permissionRestApi, huser);
        Response restResponse = permissionRestApi.updatePermission(permission);
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }


    // Permission action remove: 4 not assigned in default permission
    @Test
    public void test004_deletePermissionWithDefaultPermissionShouldFail() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // huser, with default permission, tries to delete Permission with the following call deletePermission
        // huser to delete permission needs the "remove permission" permission
        // response status code '403' HyperIoTUnauthorizedException
        HUser huser = huserWithDefaultPermissionInHyperIoTFramework(true);
        Assert.assertNotEquals(0, huser.getId());
        Assert.assertTrue(huser.isActive());

        Permission permission = createPermission();
        Assert.assertNotEquals(0, permission.getId());
        Assert.assertNotEquals(0, permission.getActionIds());

        this.impersonateUser(permissionRestApi, huser);
        Response restResponse = permissionRestApi.deletePermission(permission.getId());
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }


    // Permission action find: 8 not assigned in default permission
    @Test
    public void test005_findPermissionWithDefaultPermissionShouldFail() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // huser, with default permission, tries to find Permission with the following call findPermission
        // huser to find permission needs the "find permission" permission
        // response status code '403' HyperIoTUnauthorizedException
        HUser huser = huserWithDefaultPermissionInHyperIoTFramework(true);
        Assert.assertNotEquals(0, huser.getId());
        Assert.assertTrue(huser.isActive());

        Permission permission = createPermission();
        Assert.assertNotEquals(0, permission.getId());
        Assert.assertNotEquals(0, permission.getActionIds());

        this.impersonateUser(permissionRestApi, huser);
        Response restResponse = permissionRestApi.findPermission(permission.getId());
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }


    // Permission action find-all: 16 not assigned in default permission
    @Test
    public void test006_findAllPermissionWithDefaultPermissionShouldFail() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // huser, with default permission, tries to find all Permissions with the following call findAllPermission
        // huser to find all permissions needs the "find-all permission" permission
        // response status code '403' HyperIoTUnauthorizedException
        HUser huser = huserWithDefaultPermissionInHyperIoTFramework(true);
        Assert.assertNotEquals(0, huser.getId());
        Assert.assertTrue(huser.isActive());

        Permission permission = createPermission();
        Assert.assertNotEquals(0, permission.getId());
        Assert.assertNotEquals(0, permission.getActionIds());

        this.impersonateUser(permissionRestApi, huser);
        Response restResponse = permissionRestApi.findAllPermission();
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }


    // Permission action find-all: 16 not assigned in default permission
    @Test
    public void test007_findAllPermissionPaginatedWithDefaultPermissionShouldFail() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // huser, with default permission, tries to find all Permissions with the following call findAllPermission
        // huser to find all permissions needs the "find-all permission" permission
        // response status code '403' HyperIoTUnauthorizedException
        HUser huser = huserWithDefaultPermissionInHyperIoTFramework(true);
        Assert.assertNotEquals(0, huser.getId());
        Assert.assertTrue(huser.isActive());

        List<Permission> permissions = new ArrayList<>();
        for (int i = 0; i < defaultDelta; i++) {
            Permission permission = createPermission();
            Assert.assertNotEquals(0, permission.getId());
            Assert.assertNotEquals(0, permission.getActionIds());
            permissions.add(permission);
        }
        this.impersonateUser(permissionRestApi, huser);
        Response restResponse = permissionRestApi.findAllPermissionPaginated(defaultDelta, defaultPage);
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }


    // Permission action list_actions: 64 not assigned in default permission
    @Test
    public void test008_findAllActionsWithDefaultPermissionShouldFail() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // huser, with default permission, tries to find all actions with the following call findAllActions
        // huser to find all actions needs the "list_actions permission" permission
        // response status code '403' HyperIoTUnauthorizedException
        HUser huser = huserWithDefaultPermissionInHyperIoTFramework(true);
        Assert.assertNotEquals(0, huser.getId());
        Assert.assertTrue(huser.isActive());

        this.impersonateUser(permissionRestApi, huser);
        Response restResponse = permissionRestApi.findAllActions();
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }


    /*
     *
     *
     * UTILITY METHODS
     *
     *
     */

    private Role createRole() {
        RoleRestApi roleRestApi = getOsgiService(RoleRestApi.class);
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        this.impersonateUser(roleRestApi, adminUser);
        Role role = new Role();
        role.setName("Role" + UUID.randomUUID());
        role.setDescription("Description");
        Response restResponse = roleRestApi.saveRole(role);
        Assert.assertEquals(200, restResponse.getStatus());
        Assert.assertNotEquals(0, ((Role) restResponse.getEntity()).getId());
        Assert.assertEquals(role.getName(), ((Role) restResponse.getEntity()).getName());
        Assert.assertEquals("Description", ((Role) restResponse.getEntity()).getDescription());
        return role;
    }

    private Permission createPermission() {
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(permissionResourceName,
                HyperIoTCrudAction.FIND);
        Role role = createRole();
        Permission permission = new Permission();
        permission.setName(permissionResourceName + " by role_id " + role.getId());
        permission.setActionIds(action.getActionId());
        permission.setEntityResourceName(action.getResourceName());
        permission.setRole(role);
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        this.impersonateUser(permissionRestApi, adminUser);
        Response restResponse = permissionRestApi.savePermission(permission);
        Assert.assertEquals(200, restResponse.getStatus());
        Assert.assertNotEquals(0, ((Permission) restResponse.getEntity()).getId());
        Assert.assertEquals(permission.getName(), ((Permission) restResponse.getEntity()).getName());
        Assert.assertEquals(permission.getActionIds(), ((Permission) restResponse.getEntity()).getActionIds());
        Assert.assertEquals(permission.getEntityResourceName(), ((Permission) restResponse.getEntity()).getEntityResourceName());
        Assert.assertEquals(role.getId(), ((Permission) restResponse.getEntity()).getRole().getId());
        return permission;
    }

    private HUser huserWithDefaultPermissionInHyperIoTFramework(boolean isActive) {
        HUserRestApi hUserRestApi = getOsgiService(HUserRestApi.class);
        // In HyperIoTFramework default permissions is assigned only for the following entities:
        // AssetCategory, AssetTag
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        this.impersonateUser(hUserRestApi, adminUser);
        String username = "TestUser";
        List<Object> roles = new ArrayList<>();
        HUser huser = new HUser();
        huser.setName("name");
        huser.setLastname("lastname");
        huser.setUsername(username + UUID.randomUUID().toString().replaceAll("-", ""));
        huser.setEmail(huser.getUsername() + "@hyperiot.com");
        huser.setPassword("passwordPass&01");
        huser.setPasswordConfirm("passwordPass&01");
        huser.setAdmin(false);
        huser.setActive(false);
        Assert.assertNull(huser.getActivateCode());
        Response restResponse = hUserRestApi.register(huser);
        Assert.assertEquals(200, restResponse.getStatus());
        Assert.assertNotEquals(0, ((HUser) restResponse.getEntity()).getId());
        Assert.assertEquals("name", ((HUser) restResponse.getEntity()).getName());
        Assert.assertEquals("lastname", ((HUser) restResponse.getEntity()).getLastname());
        Assert.assertEquals(huser.getUsername(), ((HUser) restResponse.getEntity()).getUsername());
        Assert.assertEquals(huser.getEmail(), ((HUser) restResponse.getEntity()).getEmail());
        Assert.assertFalse(huser.isAdmin());
        Assert.assertFalse(huser.isActive());
        Assert.assertTrue(roles.isEmpty());
        if (isActive) {
            //Activate huser and checks if default role has been assigned
            Role role = null;
            Assert.assertFalse(huser.isActive());
            String activationCode = huser.getActivateCode();
            Assert.assertNotNull(activationCode);
            Response restResponseActivateUser = hUserRestApi.activate(huser.getEmail(), activationCode);
            Assert.assertEquals(200, restResponseActivateUser.getStatus());
            huser = (HUser) authService.login(huser.getUsername(), "passwordPass&01");
            roles = Arrays.asList(huser.getRoles().toArray());
            Assert.assertFalse(roles.isEmpty());
            Assert.assertTrue(huser.isActive());

            // checks: default role has been assigned to new huser
            Assert.assertEquals(1, huser.getRoles().size());
            Assert.assertEquals(roles.size(), huser.getRoles().size());
            Assert.assertFalse(roles.isEmpty());
            for (int i = 0; i < roles.size(); i++){
                role = ((Role) roles.get(i));
            }
            Assert.assertNotNull(role);
            Assert.assertEquals("RegisteredUser", role.getName());
            Assert.assertEquals("Role associated with the registered user",
                    role.getDescription());
            PermissionSystemApi permissionSystemApi = getOsgiService(PermissionSystemApi.class);
            Collection<Permission> listPermissions = permissionSystemApi.findByRole(role);
            Assert.assertFalse(listPermissions.isEmpty());
            Assert.assertEquals(2, listPermissions.size());
        }
        return huser;
    }

    @After
    public void afterTest() {
        HUserSystemApi hUserSystemApi = getOsgiService(HUserSystemApi.class);
        RoleSystemApi roleSystemApi = getOsgiService(RoleSystemApi.class);
        PermissionSystemApi permissionSystemApi = getOsgiService(PermissionSystemApi.class);
        HyperIoTPermissionTestUtil.dropPermissions(roleSystemApi,permissionSystemApi);
        HyperIoTHUserTestUtils.truncateHUsers(hUserSystemApi);
        HyperIoTHUserTestUtils.truncateRoles(roleSystemApi);
    }


}
