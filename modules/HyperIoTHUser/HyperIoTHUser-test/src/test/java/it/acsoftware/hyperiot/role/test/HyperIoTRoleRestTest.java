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

package it.acsoftware.hyperiot.role.test;

import it.acsoftware.hyperiot.base.action.util.HyperIoTActionsUtil;
import it.acsoftware.hyperiot.base.action.util.HyperIoTCrudAction;
import it.acsoftware.hyperiot.base.api.HyperIoTAction;
import it.acsoftware.hyperiot.base.api.HyperIoTUser;
import it.acsoftware.hyperiot.base.api.authentication.AuthenticationApi;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTPaginableResult;
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
import it.acsoftware.hyperiot.role.api.RoleRepository;
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

import static it.acsoftware.hyperiot.role.test.HyperIoTRoleConfiguration.CODE_COVERAGE_PACKAGE_FILTER;
import static it.acsoftware.hyperiot.role.test.HyperIoTRoleConfiguration.hyperIoTException;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HyperIoTRoleRestTest extends KarafTestSupport {

    //force global configuration
    public Option[] config() {
        return null;
    }

    public void impersonateUser(HyperIoTBaseRestApi restApi, HyperIoTUser user) {
        restApi.impersonate(user);
    }

    @Test
    public void test00_hyperIoTFrameworkShouldBeInstalled() {
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
    public void test01_roleModuleShouldWork() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // the following call checkModuleWorking checks if Role module working correctly
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        this.impersonateUser(roleRestService, adminUser);
        Response restResponse = roleRestService.checkModuleWorking();
        Assert.assertNotNull(roleRestService);
        Assert.assertEquals(200, restResponse.getStatus());
    }

    @Test
    public void test02_saveRoleShouldWork() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // hadmin save Role with the following call saveRole
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Role role = new Role();
        role.setName("Role" + java.util.UUID.randomUUID());
        role.setDescription("Description");
        this.impersonateUser(roleRestService, adminUser);
        Response restResponse = roleRestService.saveRole(role);
        Assert.assertEquals(200, restResponse.getStatus());
    }

    @Test
    public void test03_saveRoleShouldFailIfNotLogged() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // the following call tries to save Role, but HUser is not logged
        // response status code '403' HyperIoTUnauthorizedException
        Role role = new Role();
        role.setName("Role" + java.util.UUID.randomUUID());
        role.setDescription("Description");
        this.impersonateUser(roleRestService, null);
        Response restResponse = roleRestService.saveRole(role);
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test04_updateRoleShouldWork() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // hadmin update Role with the following call updateRole
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Role role = createRole();
        role.setDescription("Description edited" + java.util.UUID.randomUUID());
        this.impersonateUser(roleRestService, adminUser);
        Response restResponse = roleRestService.updateRole(role);
        Assert.assertEquals(200, restResponse.getStatus());
    }

    @Test
    public void test05_updateRoleShouldFailIfNotLogged() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // the following call tries to update Role, but HUser is not logged
        // response status code '403' HyperIoTUnauthorizedException
        Role role = createRole();
        role.setDescription("Description edited" + java.util.UUID.randomUUID());
        this.impersonateUser(roleRestService, null);
        Response restResponse = roleRestService.updateRole(role);
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test06_findRoleShouldWork() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // hadmin find Role with the following call findRole
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        this.impersonateUser(roleRestService, adminUser);
        Role role = createRole();
        Response restResponse = roleRestService.findRole(role.getId());
        Assert.assertEquals(200, restResponse.getStatus());
    }

    @Test
    public void test07_findRoleShouldFailIfNotLogged() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // the following call tries to find Role, but HUser is not logged
        // response status code '403' HyperIoTUnauthorizedException
        Role role = createRole();
        this.impersonateUser(roleRestService, null);
        Response restResponse = roleRestService.findRole(role.getId());
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test08_findRoleShouldFailIfEntityNotFound() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // hadmin tries to find Role with the following call findRole,
        // but entity not found
        // response status code '404' HyperIoTEntityNotFound
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        this.impersonateUser(roleRestService, adminUser);
        Response restResponse = roleRestService.findRole(0);
        Assert.assertEquals(404, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test09_findAllRolesShouldWork() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // hadmin find all Role with the following call findAllRole
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Role role = createRole();
        this.impersonateUser(roleRestService, adminUser);
        Response restResponse = roleRestService.findAllRoles();
        List<Role> listRoles = restResponse.readEntity(new GenericType<List<Role>>() {
        });
        Assert.assertFalse(listRoles.isEmpty());
        Assert.assertEquals(2, listRoles.size());
        boolean roleFound = false;
        for (Role r : listRoles) {
            if (r.getId() == role.getId()) {
                roleFound = true;
            }
        }
        Assert.assertTrue(roleFound);
        Assert.assertEquals(200, restResponse.getStatus());
    }

    @Test
    public void test10_findAllRolesShouldFailIfNotLogged() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // the following call tries to find all Role, but HUser is not logged
        // response status code '403' HyperIoTUnauthorizedException
        this.impersonateUser(roleRestService, null);
        Response restResponse = roleRestService.findAllRoles();
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test11_deleteRoleShouldWork() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // hadmin delete Role with the following call deleteRole
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        this.impersonateUser(roleRestService, adminUser);
        Role role = createRole();
        Response restResponse = roleRestService.deleteRole(role.getId());
        Assert.assertEquals(200, restResponse.getStatus());
    }


    @Test
    public void test12_deleteRoleShouldFailIfNotLogged() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // the following call tries to delete Role, but HUser is not logged
        // response status code '403' HyperIoTUnauthorizedException
        Role role = createRole();
        this.impersonateUser(roleRestService, null);
        Response restResponse = roleRestService.deleteRole(role.getId());
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test13_deleteRoleShouldFailIfEntityNotFound() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // hadmin tries to delete Role with the following call deleteRole, but entity
        // not found
        // response status code '404' HyperIoTEntityNotFound
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        this.impersonateUser(roleRestService, adminUser);
        Response restResponse = roleRestService.deleteRole(0);
        Assert.assertEquals(404, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test14_saveRoleShouldFailIfNameNull() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // hadmin tries to save Role with the following call saveRole, but
        // name is null
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        this.impersonateUser(roleRestService, adminUser);
        Role role = new Role();
        role.setName(null);
        role.setDescription("Description");
        Response restResponse = roleRestService.saveRole(role);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(2, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("role-name", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getMessage().isEmpty());
        Assert.assertEquals("role-name", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getField());
    }

    @Test
    public void test15_saveRoleShouldFailIfNameIsEmpty() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // hadmin tries to save Role with the following call saveRole, but
        // name is empty
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        this.impersonateUser(roleRestService, adminUser);
        Role role = new Role();
        role.setName("");
        role.setDescription("Description");
        Response restResponse = roleRestService.saveRole(role);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("role-name", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(role.getName(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }

    @Test
    public void test16_saveRoleShouldFailIfNameIsMaliciousCode() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // hadmin tries to save Role with the following call saveRole, but
        // name is malicious code
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        this.impersonateUser(roleRestService, adminUser);
        Role role = new Role();
        role.setName("</script>");
        role.setDescription("Description");
        Response restResponse = roleRestService.saveRole(role);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("role-name", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(role.getName(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }

    @Test
    public void test17_saveRoleShouldFailIfDescriptionNull() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // hadmin tries to save Role with the following call saveRole, but
        // description is null
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        this.impersonateUser(roleRestService, adminUser);
        Role role = new Role();
        role.setName("Role" + java.util.UUID.randomUUID());
        role.setDescription(null);
        Response restResponse = roleRestService.saveRole(role);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("role-description", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
    }

    @Test
    public void test18_saveRoleShouldFailIfDescriptionIsMaliciousCode() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // hadmin tries to save Role with the following call saveRole, but
        // description is malicious code
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        this.impersonateUser(roleRestService, adminUser);
        Role role = new Role();
        role.setName("Role" + java.util.UUID.randomUUID());
        role.setDescription("</script>");
        Response restResponse = roleRestService.saveRole(role);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("role-description", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(role.getDescription(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }

    @Test
    public void test19_saveRoleShouldFailIfMaxDescriptionIsOver3000Chars() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // hadmin tries to save Role with the following call saveRole, but
        // description is over 3000 chars
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        this.impersonateUser(roleRestService, adminUser);
        Role role = new Role();
        role.setName("Role" + java.util.UUID.randomUUID());
        role.setDescription(testMaxDescription(3001));
        Response restResponse = roleRestService.saveRole(role);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("role-description", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(role.getDescription(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }

    @Test
    public void test20_updateRoleShouldFailIfEntityNotFound() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // hadmin tries to update Role with the following call updateRole, but
        // entity not found
        // response status code '404' HyperIoTEntityNotFound
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        this.impersonateUser(roleRestService, adminUser);
        Role role = new Role();
        role.setName("just a simple name edited");
        Response restResponse = roleRestService.updateRole(role);
        Assert.assertEquals(404, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test21_updateRoleShouldFailIfNameIsNull() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // hadmin tries to update Role with the following call updateRole, but
        // name is null
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        this.impersonateUser(roleRestService, adminUser);
        Role role = createRole();
        role.setName(null);
        Response restResponse = roleRestService.updateRole(role);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(2, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("role-name", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getMessage().isEmpty());
        Assert.assertEquals("role-name", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getField());
    }

    @Test
    public void test22_updateRoleShouldFailIfNameIsEmpty() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // hadmin tries to update Role with the following call updateRole, but
        // name is empty
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        this.impersonateUser(roleRestService, adminUser);
        Role role = createRole();
        role.setName("");
        Response restResponse = roleRestService.updateRole(role);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("role-name", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(role.getName(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }

    @Test
    public void test23_updateRoleShouldFailIfNameIsMaliciousCode() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // hadmin tries to update Role with the following call updateRole, but
        // name is malicious code
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        this.impersonateUser(roleRestService, adminUser);
        Role role = createRole();
        role.setName("javascript:");
        Response restResponse = roleRestService.updateRole(role);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("role-name", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(role.getName(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }

    @Test
    public void test24_updateRoleShouldFailIfDescriptionNull() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // hadmin tries to update Role with the following call updateRole, but
        // description is null
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        this.impersonateUser(roleRestService, adminUser);
        Role role = createRole();
        role.setDescription(null);
        Response restResponse = roleRestService.updateRole(role);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("role-description", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
    }

    @Test
    public void test25_updateRoleShouldFailIfDescriptionIsMaliciousCode() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // hadmin tries to update Role with the following call updateRole, but
        // description is malicious code
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        this.impersonateUser(roleRestService, adminUser);
        Role role = createRole();
        role.setDescription("vbscript:");
        Response restResponse = roleRestService.updateRole(role);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("role-description", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(role.getDescription(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }

    @Test
    public void test26_updateRoleShouldFailIfMaxDescriptionIsOver3000Chars() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // hadmin tries to update Role with the following call updateRole, but
        // description is over 3000 chars
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        this.impersonateUser(roleRestService, adminUser);
        Role role = createRole();
        role.setDescription(testMaxDescription(3001));
        Response restResponse = roleRestService.updateRole(role);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("role-description", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(role.getDescription(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }

    @Test
    public void test27_findAllUserRolesShouldWorkIfUserNotHasRole() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // hadmin find all user Role with the following call findAllUserRoles, HUser not
        // has role and listRoles is empty
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = createHUser();
        this.impersonateUser(roleRestService, adminUser);
        Response restResponse = roleRestService.findAllUserRoles(huser.getId());
        Set<Role> listRoles = restResponse.readEntity(new GenericType<Set<Role>>() {
        });
        Assert.assertTrue(listRoles.isEmpty());
        Assert.assertEquals(200, restResponse.getStatus());
    }

    @Test
    public void test28_findAllUserRolesShouldWork() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // hadmin find all user Role with the following call findAllUserRoles
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = createHUser();
        Role role = createUserRole(huser);
        this.impersonateUser(roleRestService, adminUser);
        Response restResponse = roleRestService.findAllUserRoles(huser.getId());
        Set<Role> listRoles = restResponse.readEntity(new GenericType<Set<Role>>() {
        });
        Assert.assertFalse(listRoles.isEmpty());
        Assert.assertEquals(200, restResponse.getStatus());
    }

    @Test
    public void test29_findAllUserRolesShouldFailIfNotLogged() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // the following call tries to find all user Role, but HUser is not logged
        // response status code '403' HyperIoTUnauthorizedException
        HUser huser = createHUser();
        createUserRole(huser);
        this.impersonateUser(roleRestService, null);
        Response restResponse = roleRestService.findAllUserRoles(huser.getId());
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test30_findAllUserRolesShouldFailIfUserNotFound() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // hadmin tries to find all user Role with the following call findAllUserRoles,
        // but HUser not has roles
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = createHUser();
        this.impersonateUser(roleRestService, adminUser);
        Response restResponse = roleRestService.findAllUserRoles(huser.getId());
        Assert.assertEquals(200, restResponse.getStatus());
    }

    @Test
    public void test31_saveUserRoleShouldWork() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // hadmin save user Role with the following call saveUserRole
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Role role = createRole();
        HUser huser = createHUser();
        huser.addRole(role);
        this.impersonateUser(roleRestService, adminUser);
        Response restResponse = roleRestService.saveUserRole(role.getId(), huser.getId());
        Assert.assertEquals(200, restResponse.getStatus());
    }


    @Test
    public void test32_saveUserRoleShouldFailIfNotLogged() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // the following call tries to save user Role, but HUser is not logged
        // response status code '403' HyperIoTUnauthorizedException
        Role role = createRole();
        HUser huser = createHUser();
        huser.addRole(role);
        this.impersonateUser(roleRestService, null);
        Response restResponse = roleRestService.saveUserRole(role.getId(), huser.getId());
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test33_saveUserRoleShouldFailIfRoleNotFound() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // hadmin tries to save user Role with the following call saveUserRole,
        // but Role not found
        // response status code '404' HyperIoTEntityNotFound
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = createHUser();
        this.impersonateUser(roleRestService, adminUser);
        Response restResponse = roleRestService.saveUserRole(0, huser.getId());
        Assert.assertEquals(404, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test34_saveUserRoleShouldFailIfUserNotFound() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // hadmin tries to save user Role with the following call saveUserRole,
        // but HUser not found
        // response status code '404' HyperIoTEntityNotFound
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Role role = createRole();
        this.impersonateUser(roleRestService, adminUser);
        Response restResponse = roleRestService.saveUserRole(role.getId(), 0);
        Assert.assertEquals(404, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }


    @Test
    public void test35_deleteUserRoleShouldWork() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // hadmin delete user Role with the following call deleteUserRole
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = createHUser();
        Role role = createUserRole(huser);
        this.impersonateUser(roleRestService, adminUser);
        Response restResponse = roleRestService.deleteUserRole(role.getId(), huser.getId());
        Assert.assertEquals(200, restResponse.getStatus());
    }

    @Test
    public void test36_deleteUserRoleShouldFailIfNotLogged() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // the following call tries to delete user Role, but HUser is not logged
        // response status code '403' HyperIoTUnauthorizedException
        HUser huser = createHUser();
        Role role = createUserRole(huser);
        this.impersonateUser(roleRestService, null);
        Response restResponse = roleRestService.deleteUserRole(role.getId(), huser.getId());
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test37_deleteUserRoleShouldFailIfRoleNotFound() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // hadmin tries to delete user Role with the following call deleteUserRole,
        // but Role not found
        // response status code '404' HyperIoTEntityNotFound
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = createHUser();
        this.impersonateUser(roleRestService, adminUser);
        Response restResponse = roleRestService.deleteUserRole(0, huser.getId());
        Assert.assertEquals(404, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test38_deleteUserRoleShouldFailIfUserNotFound() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // hadmin tries to delete user Role with the following call deleteUserRole,
        // but HUser not found
        // response status code '404' HyperIoTEntityNotFound
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Role role = createRole();
        this.impersonateUser(roleRestService, adminUser);
        Response restResponse = roleRestService.deleteUserRole(role.getId(), 0);
        Assert.assertEquals(404, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }


    @Test
    public void test39_saveRoleShouldFailIfEntityIsDuplicated() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // hadmin tries to save Role with the following call saveRole, but entity is
        // duplicated
        // response status code '422' HyperIoTDuplicateEntityException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        this.impersonateUser(roleRestService, adminUser);
        Role role = createRole();
        Role duplicateRole = new Role();
        duplicateRole.setName(role.getName());
        duplicateRole.setDescription("Description");
        Response restResponse = roleRestService.saveRole(duplicateRole);
        Assert.assertEquals(409, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTDuplicateEntityException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        boolean nameIsDuplicated = false;
        for (int i = 0; i < ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().size(); i++) {
            if (((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(i).contentEquals("name")) {
                nameIsDuplicated = true;
                Assert.assertEquals("name",
                        ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(i));
            }
        }
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().size());
        Assert.assertTrue(nameIsDuplicated);
    }

    @Test
    public void test40_updateRoleShouldFailIfEntityIsDuplicated() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // hadmin tries to update Role with the following call updateRole, but entity is
        // duplicated
        // response status code '422' HyperIoTDuplicateEntityException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Role role = createRole();
        Role duplicateRole = createRole();
        duplicateRole.setName(role.getName());
        this.impersonateUser(roleRestService, adminUser);
        Response restResponse = roleRestService.updateRole(duplicateRole);
        Assert.assertEquals(409, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTDuplicateEntityException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        boolean nameIsDuplicated = false;
        for (int i = 0; i < ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().size(); i++) {
            if (((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(i).contentEquals("name")) {
                nameIsDuplicated = true;
                Assert.assertEquals("name",
                        ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(i));
            }
        }
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().size());
        Assert.assertTrue(nameIsDuplicated);
    }


    /*
     *
     *
     * CUSTOM TESTS
     *
     *
     */

    @Test
    public void test41_getUserRolesShouldFailIfDeleteUserAfterCallSaveUserRole() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // hadmin tries to finds all user roles if user has been deleted after
        // call saveUserRole
        // response status code '404' HyperIoTEntityNotFound
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = createHUser();
        //save new UserRole
        Role role = createUserRole(huser);
        this.impersonateUser(roleRestService, adminUser);
        Response restResponse = roleRestService.findAllUserRoles(huser.getId());
        Set<Role> listRoles = restResponse.readEntity(new GenericType<Set<Role>>() {
        });
        Assert.assertFalse(listRoles.isEmpty());
        Assert.assertTrue(huser.hasRole(role.getId()));
        Assert.assertEquals(200, restResponse.getStatus());

        //delete huser with call deleteHUser, not will deletes Role
        HUserRestApi hUserRestApi = getOsgiService(HUserRestApi.class);
        this.impersonateUser(hUserRestApi, adminUser);
        Response restRemoveHUser = hUserRestApi.deleteHUser(huser.getId());
        Assert.assertEquals(200, restRemoveHUser.getStatus());

        // role is still stored in database
        this.impersonateUser(roleRestService, adminUser);
        Response restResponse1 = roleRestService.findRole(role.getId());
        Assert.assertEquals(200, restResponse1.getStatus());
    }

    @Test
    public void test42_getUserRolesShouldWorkIfDeleteRoleAfterCallSaveUserRoleListIsEmpty() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // hadmin tries to finds all user roles if role has been deleted after
        // call saveUserRole, list roles is empty
        // deleteRole not deletes in cascade mode huser
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = createHUser();
        //save new UserRole
        Role role = createUserRole(huser);
        this.impersonateUser(roleRestService, adminUser);
        Response restResponse = roleRestService.findAllUserRoles(huser.getId());
        Set<Role> listRoles = restResponse.readEntity(new GenericType<Set<Role>>() {
        });
        Assert.assertFalse(listRoles.isEmpty());
        Assert.assertEquals(200, restResponse.getStatus());

        //Delete role with call deleteRole
        Response restRemoveRole = roleRestService.deleteRole(role.getId());
        Assert.assertEquals(200, restRemoveRole.getStatus());

        this.impersonateUser(roleRestService, adminUser);
        restResponse = roleRestService.findAllUserRoles(huser.getId());
        Set<Role> listRolesEmpty = restResponse.readEntity(new GenericType<Set<Role>>() {
        });
        Assert.assertTrue(listRolesEmpty.isEmpty());
        Assert.assertEquals(0, listRolesEmpty.size());
        Assert.assertEquals(200, restResponse.getStatus());

        // call deleteRole not has delete huser
        // checks if huser is stored in database
        HUserRestApi hUserRestApi = getOsgiService(HUserRestApi.class);
        this.impersonateUser(hUserRestApi, adminUser);
        Response restFindHUser = hUserRestApi.findHUser(huser.getId());
        Assert.assertEquals(200, restFindHUser.getStatus());
    }


    @Test
    public void test43_deleteUserRoleAfterDeleteUserShouldFail() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // hadmin tries to delete user roles, with call deleteUserRole,
        // if user has been deleted after call deleteHUser
        // response status code '404' HyperIoTEntityNotFound
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = createHUser();
        //save new UserRole
        Role role = createUserRole(huser);
        this.impersonateUser(roleRestService, adminUser);
        Response restResponse = roleRestService.findAllUserRoles(huser.getId());
        Set<Role> listRoles = restResponse.readEntity(new GenericType<Set<Role>>() {
        });
        Assert.assertFalse(listRoles.isEmpty());
        Assert.assertEquals(1, listRoles.size());
        Assert.assertTrue(huser.hasRole(role.getId()));
        Assert.assertEquals(200, restResponse.getStatus());
        //delete huser with call deleteRole
        HUserRestApi hUserRestApi = getOsgiService(HUserRestApi.class);
        this.impersonateUser(hUserRestApi, adminUser);
        Response restRemoveHUser = hUserRestApi.deleteHUser(huser.getId());
        Assert.assertEquals(200, restRemoveHUser.getStatus());

        //huser not found
        this.impersonateUser(roleRestService, adminUser);
        restResponse = roleRestService.deleteUserRole(role.getId(), huser.getId());
        Assert.assertEquals(404, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test44_deleteUserRoleAfterDeleteRoleShouldFail() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // hadmin tries to delete user roles, with call deleteUserRole,
        // if role has been deleted after call deleteRole
        // response status code '404' HyperIoTEntityNotFound
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = createHUser();
        //save new UserRole
        Role role = createUserRole(huser);
        this.impersonateUser(roleRestService, adminUser);
        Response restResponse = roleRestService.findAllUserRoles(huser.getId());
        Set<Role> listRoles = restResponse.readEntity(new GenericType<Set<Role>>() {
        });
        Assert.assertFalse(listRoles.isEmpty());
        Assert.assertEquals(1, listRoles.size());
        Assert.assertTrue(huser.hasRole(role.getId()));
        Assert.assertEquals(200, restResponse.getStatus());
        //delete role with call deleteRole
        Response restRemoveRole = roleRestService.deleteRole(role.getId());
        Assert.assertEquals(200, restRemoveRole.getStatus());

        //User Role not found
        this.impersonateUser(roleRestService, adminUser);
        restResponse = roleRestService.deleteUserRole(role.getId(), huser.getId());
        Assert.assertEquals(404, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }


    @Test
    public void test45_deleteRoleWithCascadeRemoveShouldWorkListRoleAndPermissionIsEmpty() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // hadmin delete Role in cascade mode, this call deletes all related permissions.
        // List Roles is empty, list permissions is empty
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = createHUser();
        Role role = createUserRole(huser);

        HyperIoTAction actionRole = HyperIoTActionsUtil.getHyperIoTAction(Role.class.getName(),
                HyperIoTCrudAction.FIND);
        HyperIoTAction actionHUser = HyperIoTActionsUtil.getHyperIoTAction(HUser.class.getName(),
                HyperIoTCrudAction.FIND);
        HyperIoTAction actionPermission = HyperIoTActionsUtil.getHyperIoTAction(Permission.class.getName(),
                HyperIoTCrudAction.FIND);

        utilGrantPermission(huser, role, actionRole);
        utilGrantPermission(huser, role, actionHUser);
        utilGrantPermission(huser, role, actionPermission);

        // check user roles, List has three permissions
        this.impersonateUser(roleRestService, adminUser);
        Response restResponseRoles = roleRestService.findAllUserRoles(huser.getId());
        Set<Role> listRoles = restResponseRoles.readEntity(new GenericType<Set<Role>>() {
        });
        Assert.assertFalse(listRoles.isEmpty());
        Assert.assertEquals(1, listRoles.size());
        Assert.assertEquals(200, restResponseRoles.getStatus());

        // list of user permissions is full
        PermissionSystemApi permissionSystemApi = getOsgiService(PermissionSystemApi.class);
        Collection<Permission> listPermissions = permissionSystemApi.findByRole(role);
        Assert.assertFalse(listPermissions.isEmpty());
        Assert.assertEquals(3, listPermissions.size());

        // delete role with call deleteRole,
        // this call delete in cascade Role and Permission
        this.impersonateUser(roleRestService, adminUser);
        restResponseRoles = roleRestService.deleteRole(role.getId());
        Assert.assertEquals(200, restResponseRoles.getStatus());
        restResponseRoles = roleRestService.findAllUserRoles(huser.getId());
        Set<Role> listRolesEmpty = restResponseRoles.readEntity(new GenericType<Set<Role>>() {
        });
        Assert.assertTrue(listRolesEmpty.isEmpty());
        Assert.assertEquals(0, listRolesEmpty.size());
        Assert.assertEquals(200, restResponseRoles.getStatus());

        // checks: list of user permission is empty
        // call deleteRole has also removed permissions
        Collection<Permission> listPermissionsEmpty = permissionSystemApi.findByRole(role);
        Assert.assertTrue(listPermissionsEmpty.isEmpty());
        Assert.assertEquals(0, listPermissionsEmpty.size());
    }

    @Test
    public void test46_deleteRoleWithoutCascadeRemoveShouldWorkListRoleIsEmptyListPermissionIsFull() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // hadmin delete Role, without cascade mode, with call deleteUserRole,
        // this call not deletes all related permissions
        // list Roles is empty, list permissions is full
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = createHUser();
        Role role = createUserRole(huser);

        HyperIoTAction actionRole = HyperIoTActionsUtil.getHyperIoTAction(Role.class.getName(),
                HyperIoTCrudAction.FIND);
        HyperIoTAction actionHUser = HyperIoTActionsUtil.getHyperIoTAction(HUser.class.getName(),
                HyperIoTCrudAction.FIND);
        HyperIoTAction actionPermission = HyperIoTActionsUtil.getHyperIoTAction(Permission.class.getName(),
                HyperIoTCrudAction.FIND);

        utilGrantPermission(huser, role, actionRole);
        utilGrantPermission(huser, role, actionHUser);
        utilGrantPermission(huser, role, actionPermission);

        // check if user roles exists
        this.impersonateUser(roleRestService, adminUser);
        Response restResponseRoles = roleRestService.findAllUserRoles(huser.getId());
        Set<Role> listRoles = restResponseRoles.readEntity(new GenericType<Set<Role>>() {
        });
        Assert.assertFalse(listRoles.isEmpty());
        Assert.assertEquals(1, listRoles.size());
        Assert.assertEquals(200, restResponseRoles.getStatus());

        // list of user permissions is full
        PermissionSystemApi permissionSystemApi = getOsgiService(PermissionSystemApi.class);
        Collection<Permission> listPermissions = permissionSystemApi.findByRole(role);
        Assert.assertFalse(listPermissions.isEmpty());
        Assert.assertEquals(3, listPermissions.size());

        // delete role with deleteUserRole,
        // this call not remove on cascade mode role and permission
        this.impersonateUser(roleRestService, adminUser);
        restResponseRoles = roleRestService.deleteUserRole(role.getId(), huser.getId());
        Assert.assertEquals(200, restResponseRoles.getStatus());
        restResponseRoles = roleRestService.findAllUserRoles(huser.getId());
        Set<Role> listRolesEmpty = restResponseRoles.readEntity(new GenericType<Set<Role>>() {
        });
        Assert.assertTrue(listRolesEmpty.isEmpty());
        Assert.assertEquals(0, listRolesEmpty.size());
        Assert.assertEquals(200, restResponseRoles.getStatus());

        // permissions is still stored in database
        Collection<Permission> listPermissions2 = permissionSystemApi.findByRole(role);
        Assert.assertFalse(listPermissions2.isEmpty());
        Assert.assertEquals(3, listPermissions2.size());
    }


    @Test
    public void test47_tryToFindRoleWithPermissionIfRoleHasBeenDeletedInCascadeModeShouldFail() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // hadmin deletes role in cascade mode, with call deleteRole,
        // the call deleteRole also deletes the permission entity and
        // huser will lose FIND permission
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");

        //Save UserRole and assign FIND permission
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(Role.class.getName(),
                HyperIoTCrudAction.FIND);
        HUser huser = createHUser();
        Role role = createUserRole(huser);
        Permission permission = utilGrantPermission(huser, role, action);
        // Test user permission
        this.impersonateUser(roleRestService, huser);
        Response restResponse = roleRestService.findRole(role.getId());
        Assert.assertEquals(200, restResponse.getStatus());

        // delete role with call deleteRole, role has been removed in cascade mode
        // this call also deletes the permission entity
        // with this call huser will lose FIND permission
        this.impersonateUser(roleRestService, adminUser);
        Response restRemoveRole = roleRestService.deleteRole(role.getId());
        Assert.assertEquals(200, restRemoveRole.getStatus());

        //try to delete User Role, this call fail
        Response restRemoveRole1 = roleRestService.deleteUserRole(role.getId(), huser.getId());
        Assert.assertEquals(404, restRemoveRole1.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
                ((HyperIoTBaseError) restRemoveRole1.getEntity()).getType());

        // permission not found, permission has been removed with call deleteRole
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        this.impersonateUser(permissionRestApi, adminUser);
        Response restResponsePermission = permissionRestApi.findPermission(permission.getId());
        Assert.assertEquals(404, restResponsePermission.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
                ((HyperIoTBaseError) restResponsePermission.getEntity()).getType());

        String sqlRole = "select * from role";
        String resultRole = executeCommand("jdbc:query hyperiot " + sqlRole);
        System.out.println(resultRole);

        String sqlPermission = "select * from permission";
        String resultPermission = executeCommand("jdbc:query hyperiot " + sqlPermission);
        System.out.println(resultPermission);
    }


    @Test
    public void test48_tryToFindRoleWithPermissionIfRoleHasBeenDeletedShouldFail() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // hadmin deletes user role with call deleteUserRole,
        // this call does not deletes the permission entity
        // with deleteUserRole call huser will lose FIND permission
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");

        //Save UserRole and assign FIND permission
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(Role.class.getName(),
                HyperIoTCrudAction.FIND);
        HUser huser = createHUser();
        Role role = createUserRole(huser);
        Permission permission = utilGrantPermission(huser, role, action);
        // Test user permission
        this.impersonateUser(roleRestService, huser);
        Response restResponse = roleRestService.findRole(role.getId());
        Assert.assertEquals(200, restResponse.getStatus());

        // delete user role with call deleteUserRole,
        // this call does not deletes the permission entity
        this.impersonateUser(roleRestService, adminUser);
        Response restRemoveRole = roleRestService.deleteUserRole(role.getId(), huser.getId());
        Assert.assertEquals(200, restRemoveRole.getStatus());


        // permission is still stored in database
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        this.impersonateUser(permissionRestApi, adminUser);
        Response restResponsePermission = permissionRestApi.findPermission(permission.getId());
        Assert.assertEquals(200, restResponsePermission.getStatus());

        //fail because user not has user role, deleted with call deleteUserRole
        this.impersonateUser(roleRestService, huser);
        restResponse = roleRestService.findRole(role.getId());
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }


    @Test
    public void test49_deleteUserWithRoleNotDeleteRoleAndPermissionEntity() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // hadmin delete a user, with call deleteHUser, with a role and permission.
        // user cancellation will not eliminate the role or permission entity
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        //Save UserRole and assign FIND permission
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(Role.class.getName(),
                HyperIoTCrudAction.FIND);
        HUser huser = createHUser();
        Role role = createUserRole(huser);
        Permission permission = utilGrantPermission(huser, role, action);
        // Test user permission
        this.impersonateUser(roleRestService, huser);
        Response restResponse = roleRestService.findRole(role.getId());
        Assert.assertEquals(200, restResponse.getStatus());

        // delete huser with call deleteHUser
        HUserRestApi hUserRestApi = getOsgiService(HUserRestApi.class);
        this.impersonateUser(hUserRestApi, adminUser);
        Response restResponseDeleteUser = hUserRestApi.deleteHUser(huser.getId());
        Assert.assertEquals(200, restResponseDeleteUser.getStatus());

        // role is still stored in database
        this.impersonateUser(roleRestService, adminUser);
        Response restResponseFindRole = roleRestService.findRole(role.getId());
        Assert.assertEquals(200, restResponseFindRole.getStatus());


        // permission is still stored in database
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        this.impersonateUser(permissionRestApi, adminUser);
        Response restResponsePermission = permissionRestApi.findPermission(permission.getId());
        Assert.assertEquals(200, restResponsePermission.getStatus());

        // huser not found
        this.impersonateUser(hUserRestApi, adminUser);
        Response restResponseUser = hUserRestApi.findHUser(huser.getId());
        Assert.assertEquals(404, restResponseUser.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
                ((HyperIoTBaseError) restResponseUser.getEntity()).getType());
    }

    @Test
    public void test50_deleteUserPermissionNotDeleteUserRoleEntity() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // hadmin delete a permission, with call deletePermission, with associate a role and huser.
        // permission cancellation will not eliminate the role or huser entity
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");

        //Save UserRole and assign FIND permission
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(Role.class.getName(),
                HyperIoTCrudAction.FIND);
        HUser huser = createHUser();
        Role role = createUserRole(huser);
        Permission permission = utilGrantPermission(huser, role, action);
        // Test user permission
        this.impersonateUser(roleRestService, huser);
        Response restResponse = roleRestService.findRole(role.getId());
        Assert.assertEquals(200, restResponse.getStatus());


        // delete permission
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        this.impersonateUser(permissionRestApi, adminUser);
        Response restResponsePermission = permissionRestApi.deletePermission(permission.getId());
        Assert.assertEquals(200, restResponsePermission.getStatus());


        // huser has role
        this.impersonateUser(roleRestService, adminUser);
        restResponse = roleRestService.findRole(role.getId());
        Assert.assertEquals(200, restResponse.getStatus());
        restResponse = roleRestService.findAllUserRoles(huser.getId());
        Set<Role> listRoles = restResponse.readEntity(new GenericType<Set<Role>>() {
        });
        Assert.assertFalse(listRoles.isEmpty());
        Assert.assertEquals(200, restResponse.getStatus());


        // Test user permission, this call fail because permission has been deleted
        // huser has role but not has permission
        this.impersonateUser(roleRestService, huser);
        restResponse = roleRestService.findRole(role.getId());
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test51_findAllRolesPaginatedShouldWork() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // In this following call findAllRolesPaginated, hadmin find all Roles with pagination
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        int delta = 5;
        int page = 2;
        List<Role> roles = new ArrayList<>();
        for (int i = 0; i < HyperIoTRoleConfiguration.defaultDelta; i++) {
            Role role = createRole();
            Assert.assertNotEquals(0, role.getId());
            roles.add(role);
        }
        Assert.assertEquals(HyperIoTRoleConfiguration.defaultDelta, roles.size());
        this.impersonateUser(roleRestService, adminUser);
        Response restResponse = roleRestService.findAllRolesPaginated(delta, page);
        HyperIoTPaginableResult<Role> listRoles = restResponse
                .readEntity(new GenericType<HyperIoTPaginableResult<Role>>() {
                });
        Assert.assertFalse(listRoles.getResults().isEmpty());
        Assert.assertEquals(delta, listRoles.getResults().size());
        Assert.assertEquals(delta, listRoles.getDelta());
        Assert.assertEquals(page, listRoles.getCurrentPage());
        // delta is 5, page 2: 10 entities stored in database
        Assert.assertEquals(3, listRoles.getNumPages());
        Assert.assertEquals(200, restResponse.getStatus());

        // checks with page = 1
        this.impersonateUser(roleRestService, adminUser);
        Response restResponsePage1 = roleRestService.findAllRolesPaginated(delta, 1);
        HyperIoTPaginableResult<Role> listRolesPage1 = restResponsePage1
                .readEntity(new GenericType<HyperIoTPaginableResult<Role>>() {
                });
        Assert.assertFalse(listRolesPage1.getResults().isEmpty());
        Assert.assertEquals(delta, listRolesPage1.getResults().size());
        Assert.assertEquals(delta, listRolesPage1.getDelta());
        Assert.assertEquals(HyperIoTRoleConfiguration.defaultPage, listRolesPage1.getCurrentPage());
        Assert.assertEquals(page, listRolesPage1.getNextPage());
        // delta is 5, page 1: 10 entities stored in database
        Assert.assertEquals(3, listRolesPage1.getNumPages());
        Assert.assertEquals(200, restResponsePage1.getStatus());
    }

    @Test
    public void test52_findAllRolesPaginatedShouldWorkIfDeltaAndPageAreNull() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // In this following call findAllRolesPaginated, hadmin find all Roles with pagination
        // if delta and page are null
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Integer delta = null;
        Integer page = null;
        List<Role> roles = new ArrayList<>();
        int numbEntities = 6;
        for (int i = 0; i < numbEntities; i++) {
            Role role = createRole();
            Assert.assertNotEquals(0, role.getId());
            roles.add(role);
        }
        Assert.assertEquals(numbEntities, roles.size());
        this.impersonateUser(roleRestService, adminUser);
        Response restResponse = roleRestService.findAllRolesPaginated(delta, page);
        HyperIoTPaginableResult<Role> listRoles = restResponse
                .readEntity(new GenericType<HyperIoTPaginableResult<Role>>() {
                });
        Assert.assertFalse(listRoles.getResults().isEmpty());
        Assert.assertEquals(numbEntities+1, listRoles.getResults().size());
        Assert.assertEquals(HyperIoTRoleConfiguration.defaultDelta, listRoles.getDelta());
        Assert.assertEquals(HyperIoTRoleConfiguration.defaultPage, listRoles.getCurrentPage());
        Assert.assertEquals(HyperIoTRoleConfiguration.defaultPage, listRoles.getNextPage());
        // default delta is 10, default page is 1: 6 entities stored in database
        Assert.assertEquals(1, listRoles.getNumPages());
        Assert.assertEquals(200, restResponse.getStatus());
    }

    @Test
    public void test53_findAllRolesPaginatedShouldWorkIfDeltaIsLowerThanZero() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // In this following call findAllRolesPaginated, hadmin find all Roles with pagination
        // if delta is lower than zero
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        int delta = -1;
        int page = 2;
        List<Role> roles = new ArrayList<>();
        int numbEntities = 12;
        for (int i = 0; i < numbEntities; i++) {
            Role role = createRole();
            Assert.assertNotEquals(0, role.getId());
            roles.add(role);
        }
        Assert.assertEquals(numbEntities, roles.size());
        this.impersonateUser(roleRestService, adminUser);
        Response restResponse = roleRestService.findAllRolesPaginated(delta, page);
        HyperIoTPaginableResult<Role> listRoles = restResponse
                .readEntity(new GenericType<HyperIoTPaginableResult<Role>>() {
                });
        Assert.assertFalse(listRoles.getResults().isEmpty());
        Assert.assertEquals(3, listRoles.getResults().size());
        Assert.assertEquals(HyperIoTRoleConfiguration.defaultDelta, listRoles.getDelta());
        Assert.assertEquals(page, listRoles.getCurrentPage());
        Assert.assertEquals(HyperIoTRoleConfiguration.defaultPage, listRoles.getNextPage());
        // default delta is 10, page is 2: 12 entities stored in database
        Assert.assertEquals(2, listRoles.getNumPages());
        Assert.assertEquals(200, restResponse.getStatus());

        //checks with page = 1
        this.impersonateUser(roleRestService, adminUser);
        Response restResponsePage1 = roleRestService.findAllRolesPaginated(delta, 1);
        HyperIoTPaginableResult<Role> listRolesPage1 = restResponsePage1
                .readEntity(new GenericType<HyperIoTPaginableResult<Role>>() {
                });
        Assert.assertFalse(listRolesPage1.getResults().isEmpty());
        Assert.assertEquals(HyperIoTRoleConfiguration.defaultDelta, listRolesPage1.getResults().size());
        Assert.assertEquals(HyperIoTRoleConfiguration.defaultDelta, listRolesPage1.getDelta());
        Assert.assertEquals(HyperIoTRoleConfiguration.defaultPage, listRolesPage1.getCurrentPage());
        Assert.assertEquals(page, listRolesPage1.getNextPage());
        // default delta is 10, page is 1: 12 entities stored in database
        Assert.assertEquals(2, listRolesPage1.getNumPages());
        Assert.assertEquals(200, restResponsePage1.getStatus());
    }

    @Test
    public void test54_findAllRolesPaginatedShouldWorkIfDeltaIsZero() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // In this following call findAllRolesPaginated, hadmin find all Roles with pagination
        // if delta is zero
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        int delta = 0;
        int page = 2;
        List<Role> roles = new ArrayList<>();
        int numEntities = 14;
        for (int i = 0; i < numEntities; i++) {
            Role role = createRole();
            Assert.assertNotEquals(0, role.getId());
            roles.add(role);
        }
        Assert.assertEquals(numEntities, roles.size());
        this.impersonateUser(roleRestService, adminUser);
        Response restResponse = roleRestService.findAllRolesPaginated(delta, page);
        HyperIoTPaginableResult<Role> listRoles = restResponse
                .readEntity(new GenericType<HyperIoTPaginableResult<Role>>() {
                });
        Assert.assertFalse(listRoles.getResults().isEmpty());
        Assert.assertEquals(5, listRoles.getResults().size());
        Assert.assertEquals(HyperIoTRoleConfiguration.defaultDelta, listRoles.getDelta());
        Assert.assertEquals(page, listRoles.getCurrentPage());
        Assert.assertEquals(1, listRoles.getNextPage());
        // because delta is 10, page is 2: 14 entities stored in database
        Assert.assertEquals(2, listRoles.getNumPages());
        Assert.assertEquals(200, restResponse.getStatus());

        //checks with page = 1
        this.impersonateUser(roleRestService, adminUser);
        Response restResponsePage1 = roleRestService.findAllRolesPaginated(delta, 1);
        HyperIoTPaginableResult<Role> listRolesPage1 = restResponsePage1
                .readEntity(new GenericType<HyperIoTPaginableResult<Role>>() {
                });
        Assert.assertFalse(listRolesPage1.getResults().isEmpty());
        Assert.assertEquals(HyperIoTRoleConfiguration.defaultDelta, listRolesPage1.getResults().size());
        Assert.assertEquals(HyperIoTRoleConfiguration.defaultDelta, listRolesPage1.getDelta());
        Assert.assertEquals(HyperIoTRoleConfiguration.defaultPage, listRolesPage1.getCurrentPage());
        Assert.assertEquals(page, listRolesPage1.getNextPage());
        // default delta is 10, page is 1: 14 entities stored in database
        Assert.assertEquals(2, listRolesPage1.getNumPages());
        Assert.assertEquals(200, restResponsePage1.getStatus());
    }

    @Test
    public void test55_findAllRolesPaginatedShouldWorkIfPageIsLowerThanZero() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // In this following call findAllRolesPaginated, hadmin find all Roles with pagination
        // if page is lower than zero
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        int delta = 8;
        int page = -1;
        List<Role> roles = new ArrayList<>();
        for (int i = 0; i < HyperIoTRoleConfiguration.defaultDelta; i++) {
            Role role = createRole();
            Assert.assertNotEquals(0, role.getId());
            roles.add(role);
        }
        Assert.assertEquals(HyperIoTRoleConfiguration.defaultDelta, roles.size());
        this.impersonateUser(roleRestService, adminUser);
        Response restResponse = roleRestService.findAllRolesPaginated(delta, page);
        HyperIoTPaginableResult<Role> listRoles = restResponse
                .readEntity(new GenericType<HyperIoTPaginableResult<Role>>() {
                });
        Assert.assertFalse(listRoles.getResults().isEmpty());
        Assert.assertEquals(delta, listRoles.getResults().size());
        Assert.assertEquals(delta, listRoles.getDelta());
        Assert.assertEquals(HyperIoTRoleConfiguration.defaultPage, listRoles.getCurrentPage());
        Assert.assertEquals(HyperIoTRoleConfiguration.defaultPage + 1, listRoles.getNextPage());
        // delta is 8, default page 1: 10 entities stored in database
        Assert.assertEquals(2, listRoles.getNumPages());
        Assert.assertEquals(200, restResponse.getStatus());

        //checks with page = 2
        this.impersonateUser(roleRestService, adminUser);
        Response restResponsePage2 = roleRestService.findAllRolesPaginated(delta, 2);
        HyperIoTPaginableResult<Role> listRolesPage2 = restResponsePage2
                .readEntity(new GenericType<HyperIoTPaginableResult<Role>>() {
                });
        Assert.assertFalse(listRolesPage2.getResults().isEmpty());
        Assert.assertEquals(3, listRolesPage2.getResults().size());
        Assert.assertEquals(delta, listRolesPage2.getDelta());
        Assert.assertEquals(HyperIoTRoleConfiguration.defaultPage + 1, listRolesPage2.getCurrentPage());
        Assert.assertEquals(HyperIoTRoleConfiguration.defaultPage, listRolesPage2.getNextPage());
        // delta is 8, page 2: 10 entities stored in database
        Assert.assertEquals(2, listRolesPage2.getNumPages());
        Assert.assertEquals(200, restResponsePage2.getStatus());
    }

    @Test
    public void test56_findAllRolesPaginatedShouldWorkIfPageIsZero() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // In this following call findAllRolesPaginated, hadmin find all Roles with pagination
        // if page is zero
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        int delta = 6;
        int page = 0;
        List<Role> roles = new ArrayList<>();
        for (int i = 0; i < HyperIoTRoleConfiguration.defaultDelta; i++) {
            Role role = createRole();
            Assert.assertNotEquals(0, role.getId());
            roles.add(role);
        }
        Assert.assertEquals(HyperIoTRoleConfiguration.defaultDelta, roles.size());
        this.impersonateUser(roleRestService, adminUser);
        Response restResponse = roleRestService.findAllRolesPaginated(delta, page);
        HyperIoTPaginableResult<Role> listRoles = restResponse
                .readEntity(new GenericType<HyperIoTPaginableResult<Role>>() {
                });
        Assert.assertFalse(listRoles.getResults().isEmpty());
        Assert.assertEquals(delta, listRoles.getResults().size());
        Assert.assertEquals(delta, listRoles.getDelta());
        Assert.assertEquals(HyperIoTRoleConfiguration.defaultPage, listRoles.getCurrentPage());
        Assert.assertEquals(HyperIoTRoleConfiguration.defaultPage + 1, listRoles.getNextPage());
        // delta is 6, default page is 1: 10 entities stored in database
        Assert.assertEquals(2, listRoles.getNumPages());
        Assert.assertEquals(200, restResponse.getStatus());

        //checks with page = 2
        this.impersonateUser(roleRestService, adminUser);
        Response restResponsePage2 = roleRestService.findAllRolesPaginated(delta, 2);
        HyperIoTPaginableResult<Role> listRolesPage2 = restResponsePage2
                .readEntity(new GenericType<HyperIoTPaginableResult<Role>>() {
                });
        Assert.assertFalse(listRolesPage2.getResults().isEmpty());
        Assert.assertEquals(5, listRolesPage2.getResults().size());
        Assert.assertEquals(delta, listRolesPage2.getDelta());
        Assert.assertEquals(HyperIoTRoleConfiguration.defaultPage + 1, listRolesPage2.getCurrentPage());
        Assert.assertEquals(HyperIoTRoleConfiguration.defaultPage, listRolesPage2.getNextPage());
        // delta is 6, page 2: 10 entities stored in database
        Assert.assertEquals(2, listRolesPage2.getNumPages());
        Assert.assertEquals(200, restResponsePage2.getStatus());
    }

    @Test
    public void test57_findAllRolesPaginatedShouldFailIfNotLogged() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // the following call tries to find all Roles with pagination,
        // but HUser is not logged
        // response status code '403' HyperIoTUnauthorizedException
        this.impersonateUser(roleRestService, null);
        Response restResponse = roleRestService.findAllRolesPaginated(null, null);
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test58_saveRoleShouldFailIfNameIsGreaterThan255Chars() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // hadmin tries to save Role with the following call saveRole, but
        // name is greater than 255 chars
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        this.impersonateUser(roleRestService, adminUser);
        Role role = new Role();
        role.setName(createStringFieldWithSpecifiedLenght(256));
        role.setDescription("Description");
        Response restResponse = roleRestService.saveRole(role);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("role-name", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(role.getName(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }

    @Test
    public void test59_updateRoleShouldFailIfNameIsGreaterThan255Chars() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // hadmin tries to update Role with the following call updateRole, but
        // name is greater than 255 chars.
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        this.impersonateUser(roleRestService, adminUser);
        Role role = createRole();
        role.setName(createStringFieldWithSpecifiedLenght(256));
        Response restResponse = roleRestService.updateRole(role);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("role-name", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(role.getName(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }

    @Test
    public void test60_saveRoleShouldWorkWhenDescriptionIs3000Chars() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // hadmin tries to save Role with the following call saveRole,
        // role's description is  3000 chars
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        this.impersonateUser(roleRestService, adminUser);
        Role role = new Role();
        role.setName("Role" + java.util.UUID.randomUUID());
        role.setDescription(testMaxDescription(3000));
        Response restResponse = roleRestService.saveRole(role);
        Assert.assertEquals(200, restResponse.getStatus());
        Assert.assertNotEquals(0, ((Role)restResponse.getEntity()).getId());
        Assert.assertEquals(3000, ((Role)restResponse.getEntity()).getDescription().length());
    }

    @Test
    public void test61_updateRoleShouldWorkWhenDescriptionIs3000Chars() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // hadmin tries to update Role with the following call updateRole,
        // role's description is  3000 chars
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Role role = createRole();
        Assert.assertNotEquals(0, role.getId());
        long roleId = role.getId();
        this.impersonateUser(roleRestService, adminUser);
        role.setDescription(testMaxDescription(3000));
        Response restResponse = roleRestService.updateRole(role);
        Assert.assertEquals(200, restResponse.getStatus());
        Assert.assertEquals(roleId, ((Role)restResponse.getEntity()).getId());
        Assert.assertEquals(3000, ((Role)restResponse.getEntity()).getDescription().length());
    }


    /*
     *
     *
     * UTILITY METHODS
     *
     *
     */

    private String createStringFieldWithSpecifiedLenght(int length) {
        String symbol = "a";
        String field = String.format("%" + length + "s", " ").replaceAll(" ", symbol);
        Assert.assertEquals(length, field.length());
        return field;
    }

    private Role createRole() {
        RoleRestApi roleRestApi = getOsgiService(RoleRestApi.class);
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        this.impersonateUser(roleRestApi, adminUser);
        Role role = new Role();
        role.setName("Role" + java.util.UUID.randomUUID());
        role.setDescription("Description");
        Response restResponse = roleRestApi.saveRole(role);
        Assert.assertEquals(200, restResponse.getStatus());
        return role;
    }

    private HUser createHUser() {
        HUserRestApi hUserRestApi = getOsgiService(HUserRestApi.class);
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        this.impersonateUser(hUserRestApi, adminUser);
        String username = "TestUser";
        HUser huser = new HUser();
        huser.setName("name");
        huser.setLastname("lastname");
        huser.setUsername(username + UUID.randomUUID().toString().replaceAll("-", ""));
        huser.setEmail("testusername" + UUID.randomUUID().toString() + "@hyperiot.com");
        huser.setPassword("passwordPass&01");
        huser.setPasswordConfirm("passwordPass&01");
        huser.setAdmin(false);
        huser.setActive(true);
        Response restResponse = hUserRestApi.saveHUser(huser);
        Assert.assertEquals(200, restResponse.getStatus());
        return huser;
    }

    private Role createUserRole(HUser huser) {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Role role = createRole();
        huser.addRole(role);
        this.impersonateUser(roleRestService, adminUser);
        Response restResponse = roleRestService.saveUserRole(role.getId(), huser.getId());
        Assert.assertEquals(200, restResponse.getStatus());
        return role;
    }

    private String testMaxDescription(int lengthDescription) {
        String symbol = "a";
        String description = String.format("%" + lengthDescription + "s", " ").replaceAll(" ", symbol);
        Assert.assertEquals(lengthDescription, description.length());
        return description;
    }


    private Permission utilGrantPermission(HUser huser, Role role, HyperIoTAction action) {
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        if (action == null) {
            Assert.assertNull(action);
            return null;
        } else {
            PermissionSystemApi permissionSystemApi = getOsgiService(PermissionSystemApi.class);
            Permission testPermission = permissionSystemApi.findByRoleAndResourceName(role, action.getResourceName());
            if (testPermission == null) {
                Permission permission = new Permission();
                permission.setName(action.getActionName());
                permission.setActionIds(action.getActionId());
                permission.setEntityResourceName(action.getResourceName());
                permission.setRole(role);
                this.impersonateUser(permissionRestApi, adminUser);
                Response restResponse = permissionRestApi.savePermission(permission);
                testPermission = permission;
                Assert.assertEquals(200, restResponse.getStatus());
            } else {
                this.impersonateUser(permissionRestApi, adminUser);
                testPermission.addPermission(action);
                Response restResponseUpdate = permissionRestApi.updatePermission(testPermission);
                Assert.assertEquals(200, restResponseUpdate.getStatus());
            }
            return testPermission;
        }
    }



    @After
    public void afterTest() {
        HUserSystemApi hUserSystemApi = getOsgiService(HUserSystemApi.class);
        RoleSystemApi roleSystemApi = getOsgiService(RoleSystemApi.class);
        HyperIoTHUserTestUtils.truncateHUsers(hUserSystemApi);
        HyperIoTHUserTestUtils.truncateRoles(roleSystemApi);
    }


}
