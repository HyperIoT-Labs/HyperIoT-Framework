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

package it.acsoftware.hyperiot.role.test;

import it.acsoftware.hyperiot.base.api.authentication.AuthenticationApi;
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

import static it.acsoftware.hyperiot.role.test.HyperIoTRoleConfiguration.*;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HyperIoTRoleRestWithDefaultPermissionTest extends KarafTestSupport {

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
        assertContains("HyperIoTPermission-features ", features);
        assertContains("HyperIoTHUser-features ", features);
        assertContains("HyperIoTCompany-features ", features);
        assertContains("HyperIoTAuthentication-features ", features);
        assertContains("HyperIoTAssetCategory-features ", features);
        assertContains("HyperIoTAssetTag-features ", features);
        assertContains("HyperIoTSharedEntity-features ", features);
        String datasource = executeCommand("jdbc:ds-list");
//		System.out.println(executeCommand("bundle:list | grep HyperIoT"));
        assertContains("hyperiot", datasource);
    }

    @Test
    public void test001_roleModuleShouldWork() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // the following call checkModuleWorking checks if Role module working correctly
        HUser huser = huserWithDefaultPermissionInHyperIoTFramework(true);
        Assert.assertNotEquals(0, huser.getId());
        Assert.assertTrue(huser.isActive());
        this.impersonateUser(roleRestService, huser);
        Response restResponse = roleRestService.checkModuleWorking();
        Assert.assertEquals(200, restResponse.getStatus());
        Assert.assertEquals("HyperIoT Role Module works!", restResponse.getEntity());
    }

    // Role action save: 1 not assigned in default permission
    @Test
    public void test002_createNewRoleWithDefaultPermissionShouldFail() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // huser, with default permission, tries to save new Role with the following call saveRole
        // huser to save a new role needs the "save role" permission
        // response status code '403' HyperIoTUnauthorizedException
        HUser huser = huserWithDefaultPermissionInHyperIoTFramework(true);
        Assert.assertNotEquals(0, huser.getId());
        Assert.assertTrue(huser.isActive());

        Role role = new Role();
        role.setName("Role" + java.util.UUID.randomUUID());
        role.setDescription("Description");

        this.impersonateUser(roleRestService, huser);
        Response restResponse = roleRestService.saveRole(role);
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    // Role action update: 2 not assigned in default permission
    @Test
    public void test003_updateRoleWithDefaultPermissionShouldFail() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // huser, with default permission, tries to update Role with the following call updateRole
        // huser to update a new role needs the "update role" permission
        // response status code '403' HyperIoTUnauthorizedException
        HUser huser = huserWithDefaultPermissionInHyperIoTFramework(true);
        Assert.assertNotEquals(0, huser.getId());
        Assert.assertTrue(huser.isActive());

        Role role = createRole();
        role.setDescription("description edited");

        this.impersonateUser(roleRestService, huser);
        Response restResponse = roleRestService.updateRole(role);
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    // Role action remove: 4 not assigned in default permission
    @Test
    public void test004_deleteRoleWithDefaultPermissionShouldFail() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // huser, with default permission, tries to delete Role with the following call deleteRole
        // huser to delete a new role needs the "remove role" permission
        // response status code '403' HyperIoTUnauthorizedException
        HUser huser = huserWithDefaultPermissionInHyperIoTFramework(true);
        Assert.assertNotEquals(0, huser.getId());
        Assert.assertTrue(huser.isActive());
        Role role = createRole();
        this.impersonateUser(roleRestService, huser);
        Response restResponse = roleRestService.deleteRole(role.getId());
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    // Role action find: 8 not assigned in default permission
    @Test
    public void test005_findRoleWithDefaultPermissionShouldFail() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // huser, with default permission, tries to find Role with the following call findRole
        // huser to find a new role needs the "find role" permission
        // response status code '403' HyperIoTUnauthorizedException
        HUser huser = huserWithDefaultPermissionInHyperIoTFramework(true);
        Assert.assertNotEquals(0, huser.getId());
        Assert.assertTrue(huser.isActive());
        Role role = createRole();
        this.impersonateUser(roleRestService, huser);
        Response restResponse = roleRestService.findRole(role.getId());
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    // Role action find: 8 not assigned in default permission
    @Test
    public void test006_getUserRolesWithDefaultPermissionShouldFail() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // huser, with default permission, tries to find all huserRoles with the following call findAllUserRoles
        // huser to find all huser roles needs the "find role" permission
        // response status code '403' HyperIoTUnauthorizedException
        HUser huser = huserWithDefaultPermissionInHyperIoTFramework(true);
        Assert.assertNotEquals(0, huser.getId());
        Assert.assertTrue(huser.isActive());

        this.impersonateUser(roleRestService, huser);
        Response restResponse = roleRestService.findAllUserRoles(huser.getId());
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    // Role action find-all: 16 not assigned in default permission
    @Test
    public void test007_findAllRoleWithDefaultPermissionShouldFail() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // huser, with default permission, tries to find all Role with the following call findAllRole
        // huser to find all role needs the "find-all role" permission
        // response status code '403' HyperIoTUnauthorizedException
        HUser huser = huserWithDefaultPermissionInHyperIoTFramework(true);
        Assert.assertNotEquals(0, huser.getId());
        Assert.assertTrue(huser.isActive());
        this.impersonateUser(roleRestService, huser);
        Response restResponse = roleRestService.findAllRoles();
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    // Role action find-all: 16 not assigned in default permission
    @Test
    public void test008_findAllRolesPaginatedWithDefaultPermissionShouldFail() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // huser, with default permission, tries to find all Role with the following call findAllRolesPaginated
        // huser to find all role needs the "find-all role" permission
        // response status code '403' HyperIoTUnauthorizedException
        int delta = 5;
        int page = 2;
        HUser huser = huserWithDefaultPermissionInHyperIoTFramework(true);
        Assert.assertNotEquals(0, huser.getId());
        Assert.assertTrue(huser.isActive());

        List<Role> roles = new ArrayList<>();
        for (int i = 0; i < delta; i++) {
            Role role = createRole();
            roles.add(role);
        }
        this.impersonateUser(roleRestService, huser);
        Response restResponse = roleRestService.findAllRolesPaginated(delta, page);
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    // Role action assign_members: 32 not assigned in default permission
    @Test
    public void test009_saveUserRoleWithDefaultPermissionShouldFail() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // huser, with default permission, tries to save user Role with the following call saveUserRole
        // huser to assign huser role needs the "assign_members role" permission
        // response status code '403' HyperIoTUnauthorizedException
        HUser huser = huserWithDefaultPermissionInHyperIoTFramework(true);
        Assert.assertNotEquals(0, huser.getId());
        Assert.assertTrue(huser.isActive());
        Role role = createRole();
        huser.addRole(role);
        this.impersonateUser(roleRestService, huser);
        Response restResponse = roleRestService.saveUserRole(role.getId(), huser.getId());
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    // Role action unassign: 64 not assigned in default permission
    @Test
    public void test010_deleteUserRoleWithDefaultPermissionShouldFail() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // huser, with default permission, tries to remove user Role with the following call deleteUserRole
        // huser to remove huser role needs the "unassign role" permission
        // response status code '403' HyperIoTUnauthorizedException
        HUser huser = huserWithDefaultPermissionInHyperIoTFramework(true);
        Assert.assertNotEquals(0, huser.getId());
        Assert.assertTrue(huser.isActive());
        Role role = createUserRole(huser);
        this.impersonateUser(roleRestService, huser);
        Response restResponse = roleRestService.deleteUserRole(role.getId(), huser.getId());
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
        return role;
    }

    private Role createUserRole(HUser huser) {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        this.impersonateUser(roleRestService, adminUser);
        Role role = createRole();
        huser.addRole(role);
        Response restResponse = roleRestService.saveUserRole(role.getId(), huser.getId());
        Assert.assertEquals(200, restResponse.getStatus());
        return role;
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
        HyperIoTHUserTestUtils.truncateHUsers(hUserSystemApi);
        HyperIoTHUserTestUtils.truncateRoles(roleSystemApi);
    }

}
