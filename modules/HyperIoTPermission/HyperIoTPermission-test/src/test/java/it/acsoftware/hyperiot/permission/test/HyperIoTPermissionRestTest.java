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

package it.acsoftware.hyperiot.permission.test;

import it.acsoftware.hyperiot.base.api.authentication.AuthenticationApi;
import it.acsoftware.hyperiot.base.action.util.HyperIoTActionsUtil;
import it.acsoftware.hyperiot.base.action.util.HyperIoTCrudAction;
import it.acsoftware.hyperiot.base.api.HyperIoTAction;
import it.acsoftware.hyperiot.base.api.HyperIoTUser;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTPaginableResult;
import it.acsoftware.hyperiot.base.model.HyperIoTBaseError;
import it.acsoftware.hyperiot.base.service.rest.HyperIoTBaseRestApi;
import it.acsoftware.hyperiot.base.test.HyperIoTTestConfigurationBuilder;
import it.acsoftware.hyperiot.huser.api.HUserSystemApi;
import it.acsoftware.hyperiot.huser.model.HUser;
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
public class HyperIoTPermissionRestTest extends KarafTestSupport {

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
    public void test01_permissionModuleShouldWork() {
        PermissionRestApi permissionRestService = getOsgiService(PermissionRestApi.class);
        // the following call sayHi checks if Permission module working correctly
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        this.impersonateUser(permissionRestService, adminUser);
        Response response = permissionRestService.sayHi();
        Assert.assertEquals(200, response.getStatus());
    }

    @Test
    public void test02_savePermissionShouldWork() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // hadmin save Permission with the following call savePermission
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Role role = createRole();
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(permissionResourceName,
                HyperIoTCrudAction.SAVE);
        Permission permission = new Permission();
        permission.setName(action.getActionName());
        permission.setActionIds(action.getActionId());
        permission.setEntityResourceName(action.getResourceName());
        permission.setRole(role);
        this.impersonateUser(permissionRestApi, adminUser);
        Response restResponse = permissionRestApi.savePermission(permission);
        Assert.assertEquals(200, restResponse.getStatus());
    }

    @Test
    public void test03_savePermissionShouldFailIfNotLogged() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // the following call tries to save Permission, but HUser is not logged
        // response status code '403' HyperIoTUnauthorizedException
        Role role = createRole();
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(permissionResourceName,
                HyperIoTCrudAction.SAVE);
        Permission permission = new Permission();
        permission.setName(action.getActionName());
        permission.setActionIds(action.getActionId());
        permission.setEntityResourceName(action.getResourceName());
        permission.setRole(role);
        this.impersonateUser(permissionRestApi, null);
        Response restResponse = permissionRestApi.savePermission(permission);
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test04_updatePermissionShouldWork() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // hadmin update Permission with the following call updatePermission
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Permission permission = createPermission();
        Date date = new Date();
        permission.setName("name edited in date: " + date);
        this.impersonateUser(permissionRestApi, adminUser);
        Response restResponse = permissionRestApi.updatePermission(permission);
        Assert.assertEquals(200, restResponse.getStatus());
        Assert.assertEquals("name edited in date: " + date,
                ((Permission) restResponse.getEntity()).getName());
        Assert.assertEquals(permission.getEntityVersion() + 1,
                (((Permission) restResponse.getEntity()).getEntityVersion()));
    }

    @Test
    public void test05_updatePermissionShouldFailIfNotLogged() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // the following call tries to update Permission, but HUser is not logged
        // response status code '403' HyperIoTUnauthorizedException
        Permission permission = createPermission();
        permission.setName("name edited");
        this.impersonateUser(permissionRestApi, null);
        Response restResponse = permissionRestApi.updatePermission(permission);
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test06_findPermissionShouldWork() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // hadmin find Permission with the following call findPermission
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Permission permission = createPermission();
        this.impersonateUser(permissionRestApi, adminUser);
        Response restResponse = permissionRestApi.findPermission(permission.getId());
        Assert.assertEquals(200, restResponse.getStatus());
    }

    @Test
    public void test07_findPermissionShouldFailIfNotLogged() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // the following call tries to find Permission, but HUser is not logged
        // response status code '403' HyperIoTUnauthorizedException
        Permission permission = createPermission();
        this.impersonateUser(permissionRestApi, null);
        Response restResponse = permissionRestApi.findPermission(permission.getId());
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test08_findPermissionShouldFailIfEntityNotFound() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // hadmin tries to find Permission, but entity not found
        // response status code '404' HyperIoTEntityNotFound
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        this.impersonateUser(permissionRestApi, adminUser);
        Response restResponse = permissionRestApi.findPermission(0);
        Assert.assertEquals(404, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test09_findAllPermissionShouldWork() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // hadmin find all Permission with the following call findAllPermission
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Permission permission = createPermission();
        this.impersonateUser(permissionRestApi, adminUser);
        Response restResponse = permissionRestApi.findAllPermission();
        List<Permission> listPermissions = restResponse.readEntity(new GenericType<List<Permission>>() {
        });
        Assert.assertNotEquals(0, listPermissions.size());
        Assert.assertFalse(listPermissions.isEmpty());
        boolean permissionFound = false;
        for (Permission permissions : listPermissions) {
            if (permission.getId() == permissions.getId())
                permissionFound = true;
        }
        Assert.assertTrue(permissionFound);
        Assert.assertEquals(200, restResponse.getStatus());
    }

    @Test
    public void test10_findAllPermissionShouldFailIfNotLogged() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // the following call tries to find all Permission, but HUser is not logged
        // response status code '403' HyperIoTUnauthorizedException
        this.impersonateUser(permissionRestApi, null);
        Response restResponse = permissionRestApi.findAllPermission();
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test11_deletePermissionShouldWork() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // hadmin delete Permission with the following call deletePermission
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Permission permission = createPermission();
        this.impersonateUser(permissionRestApi, adminUser);
        Response restResponse = permissionRestApi.deletePermission(permission.getId());
        Assert.assertEquals(200, restResponse.getStatus());
    }

    @Test
    public void test12_deletePermissionShouldFailIfNotLogged() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // the following call tries to delete Permission, but HUser is not logged
        // response status code '403' HyperIoTUnauthorizedException
        Permission permission = createPermission();
        this.impersonateUser(permissionRestApi, null);
        Response restResponse = permissionRestApi.deletePermission(permission.getId());
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test13_deletePermissionShouldFailIfEntityNotFound() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // hadmin tries to delete Permission, but entity not found
        // response status code '404' HyperIoTEntityNotFound
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        this.impersonateUser(permissionRestApi, adminUser);
        Response restResponse = permissionRestApi.deletePermission(0);
        Assert.assertEquals(404, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test14_savePermissionShouldFailIfNameIsMaliciousCode() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // hadmin tries to save Permission with the following call savePermission, but
        // name is malicious code
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Role role = createRole();
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(permissionResourceName,
                HyperIoTCrudAction.SAVE);
        Permission permission = new Permission();
        permission.setName("</script>");
        permission.setActionIds(action.getActionId());
        permission.setEntityResourceName(action.getResourceName());
        permission.setRole(role);
        this.impersonateUser(permissionRestApi, adminUser);
        Response restResponse = permissionRestApi.savePermission(permission);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("permission-name", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(permission.getName(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }

    @Test
    public void test15_savePermissionShouldFailIfNameIsBlank() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // hadmin tries to save Permission with the following call savePermission, but
        // name is blank
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Role role = createRole();
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(permissionResourceName,
                HyperIoTCrudAction.SAVE);
        Permission permission = new Permission();
        permission.setName("");
        permission.setActionIds(action.getActionId());
        permission.setEntityResourceName(action.getResourceName());
        permission.setRole(role);
        this.impersonateUser(permissionRestApi, adminUser);
        Response restResponse = permissionRestApi.savePermission(permission);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("permission-name", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(permission.getName(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }

    @Test
    public void test16_savePermissionShouldFailIfActionIdsIsNegative() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // hadmin tries to save Permission with the following call savePermission, but
        // actionIds is not a positive number
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Role role = createRole();
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(permissionResourceName,
                HyperIoTCrudAction.SAVE);
        Permission permission = new Permission();
        permission.setName(action.getActionName());
        permission.setActionIds(-1);
        permission.setEntityResourceName(action.getResourceName());
        permission.setRole(role);
        this.impersonateUser(permissionRestApi, adminUser);
        Response restResponse = permissionRestApi.savePermission(permission);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("permission-actionids", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(String.valueOf(permission.getActionIds()), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }

    @Test
    public void test17_savePermissionShouldFailIfEntityResourceNameIsNull() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // hadmin tries to save Permission with the following call savePermission, but
        // entityResourceName is null
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Role role = createRole();
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(permissionResourceName,
                HyperIoTCrudAction.SAVE);
        Permission permission = new Permission();
        permission.setName(action.getActionName());
        permission.setActionIds(action.getActionId());
        permission.setEntityResourceName(null);
        permission.setRole(role);
        this.impersonateUser(permissionRestApi, adminUser);
        Response restResponse = permissionRestApi.savePermission(permission);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(2, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("permission-entityresourcename", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getMessage().isEmpty());
        Assert.assertEquals("permission-entityresourcename", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getField());
    }

    @Test
    public void test18_savePermissionShouldFailIfEntityResourceNameIsEmpty() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // hadmin tries to save Permission with the following call savePermission, but
        // entityResourceName is empty
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Role role = createRole();
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(permissionResourceName,
                HyperIoTCrudAction.SAVE);
        Permission permission = new Permission();
        permission.setName(action.getActionName());
        permission.setActionIds(action.getActionId());
        permission.setEntityResourceName("");
        permission.setRole(role);
        this.impersonateUser(permissionRestApi, adminUser);
        Response restResponse = permissionRestApi.savePermission(permission);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("permission-entityresourcename", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(permission.getEntityResourceName(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }

    @Test
    public void test19_savePermissionShouldFailIfEntityResourceNameIsMaliciousCode() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // hadmin tries to save Permission with the following call savePermission, but
        // entityResourceName is malicious code
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Role role = createRole();
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(permissionResourceName,
                HyperIoTCrudAction.SAVE);
        Permission permission = new Permission();
        permission.setName(action.getActionName());
        permission.setActionIds(action.getActionId());
        permission.setEntityResourceName("eval(malicious code)");
        permission.setRole(role);
        this.impersonateUser(permissionRestApi, adminUser);
        Response restResponse = permissionRestApi.savePermission(permission);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("permission-entityresourcename", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(permission.getEntityResourceName(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }

    @Test
    public void test20_savePermissionShouldFailIfRoleIsNull() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // hadmin tries to save Permission with the following call savePermission, but
        // Role is null
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(permissionResourceName,
                HyperIoTCrudAction.SAVE);
        Permission permission = new Permission();
        permission.setName(action.getActionName());
        permission.setActionIds(action.getActionId());
        permission.setEntityResourceName(action.getResourceName());
        permission.setRole(null);
        this.impersonateUser(permissionRestApi, adminUser);
        Response restResponse = permissionRestApi.savePermission(permission);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("permission-role", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
    }

    @Test
    public void test21_updatePermissionShouldFailIfNameIsMaliciousCode() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // hadmin tries to update Permission with the following call updatePermission,
        // but name is malicious code
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Permission permission = createPermission();
        permission.setName("expression(malicious code)");
        this.impersonateUser(permissionRestApi, adminUser);
        Response restResponse = permissionRestApi.updatePermission(permission);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("permission-name", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(permission.getName(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }

    @Test
    public void test22_updatePermissionShouldFailIfNameIsBlank() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // hadmin tries to update Permission with the following call updatePermission,
        // but name is blank
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Permission permission = createPermission();
        permission.setName(" ");
        this.impersonateUser(permissionRestApi, adminUser);
        Response restResponse = permissionRestApi.updatePermission(permission);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("permission-name", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(permission.getName(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }

    @Test
    public void test23_updatePermissionShouldFailIfActionIdsIsNegative() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // hadmin tries to update Permission with the following call updatePermission,
        // but actionIds is not a positive number
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Permission permission = createPermission();
        permission.setActionIds(-1);
        this.impersonateUser(permissionRestApi, adminUser);
        Response restResponse = permissionRestApi.updatePermission(permission);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("permission-actionids", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(String.valueOf(permission.getActionIds()), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }

    @Test
    public void test24_updatePermissionShouldFailIfEntityResourceNameIsNull() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // hadmin tries to update Permission with the following call updatePermission,
        // but entityResourceName is null
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Permission permission = createPermission();
        permission.setEntityResourceName(null);
        this.impersonateUser(permissionRestApi, adminUser);
        Response restResponse = permissionRestApi.updatePermission(permission);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(2, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("permission-entityresourcename", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getMessage().isEmpty());
        Assert.assertEquals("permission-entityresourcename", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getField());
    }

    @Test
    public void test25_updatePermissionShouldFailIfEntityResourceNameIsEmpty() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // hadmin tries to update Permission with the following call updatePermission,
        // but entityResourceName is empty
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Permission permission = createPermission();
        permission.setEntityResourceName("");
        this.impersonateUser(permissionRestApi, adminUser);
        Response restResponse = permissionRestApi.updatePermission(permission);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("permission-entityresourcename", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(permission.getEntityResourceName(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }

    @Test
    public void test26_updatePermissionShouldFailIfEntityResourceNameIsMaliciousCode() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // hadmin tries to update Permission with the following call updatePermission,
        // but entityResourceName is malicious code
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Permission permission = createPermission();
        permission.setEntityResourceName("</script>");
        this.impersonateUser(permissionRestApi, adminUser);
        Response restResponse = permissionRestApi.updatePermission(permission);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("permission-entityresourcename", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(permission.getEntityResourceName(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }

    @Test
    public void test27_updatePermissionShouldFailIfRoleIsNull() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // hadmin tries to update Permission with the following call updatePermission,
        // but Role is null
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Permission permission = createPermission();
        permission.setRole(null);
        this.impersonateUser(permissionRestApi, adminUser);
        Response restResponse = permissionRestApi.updatePermission(permission);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("permission-role", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
    }

    @Test
    public void test28_findAllActionsShouldWork() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // hadmin find all Actions with the following call findAllActions
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        this.impersonateUser(permissionRestApi, adminUser);
        Response restResponse = permissionRestApi.findAllActions();
        HashMap<String, List<HyperIoTAction>> actions = restResponse
                .readEntity(new GenericType<HashMap<String, List<HyperIoTAction>>>() {
                });
        Assert.assertFalse(actions.isEmpty());
        Assert.assertEquals(200, restResponse.getStatus());
    }

    @Test
    public void test29_findAllActionsShouldFailIfNotLogged() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // the following call tries to find all Actions, but HUser is not logged
        // response status code '403' HyperIoTUnauthorizedException
        this.impersonateUser(permissionRestApi, null);
        Response restResponse = permissionRestApi.findAllActions();
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test30_findAllPermissionPaginatedShouldWork() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // In this following call findAllPermissionPaginated, hadmin find
        // all Permissions with pagination
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        List<Permission> permissions = new ArrayList<>();
        int delta = 5;
        int page = 1;
        for (int i = 0; i < defaultDelta; i++) {
            Permission permission = createPermission();
            Assert.assertNotEquals(0, permission.getId());
            permissions.add(permission);
        }
        Assert.assertEquals(defaultDelta, permissions.size());
        this.impersonateUser(permissionRestApi, adminUser);
        Response restResponse = permissionRestApi.findAllPermissionPaginated(delta, page);
        HyperIoTPaginableResult<Permission> listPermissions = restResponse
                .readEntity(new GenericType<HyperIoTPaginableResult<Permission>>() {
                });
        Assert.assertFalse(listPermissions.getResults().isEmpty());
        Assert.assertEquals(delta, listPermissions.getResults().size());
        Assert.assertEquals(delta, listPermissions.getDelta());
        Assert.assertEquals(page, listPermissions.getCurrentPage());
        Assert.assertEquals(page + 1, listPermissions.getNextPage());
        // delta is 5, page 1: 10 entities stored in database
        Assert.assertEquals(3, listPermissions.getNumPages());
        Assert.assertEquals(200, restResponse.getStatus());
    }


    @Test
    public void test31_findAllPermissionsPaginatedShouldWorkIfDeltaAndPageAreNull() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // In this following call findAllPermissionPaginated, hadmin find
        // all Permissions with pagination, if delta and page are null
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Integer delta = null;
        Integer page = null;
        List<Permission> permissions = new ArrayList<>();
        int numbEntities = 6;
        for (int i = 0; i < numbEntities; i++) {
            Permission permission = createPermission();
            Assert.assertNotEquals(0, permission.getId());
            permissions.add(permission);
        }
        Assert.assertEquals(numbEntities, permissions.size());
        this.impersonateUser(permissionRestApi, adminUser);
        Response restResponse = permissionRestApi.findAllPermissionPaginated(delta, page);
        HyperIoTPaginableResult<Permission> listPermissions = restResponse
                .readEntity(new GenericType<HyperIoTPaginableResult<Permission>>() {
                });
        Assert.assertFalse(listPermissions.getResults().isEmpty());
        Assert.assertEquals(numbEntities+2, listPermissions.getResults().size());
        Assert.assertEquals(defaultDelta, listPermissions.getDelta());
        Assert.assertEquals(defaultPage, listPermissions.getCurrentPage());
        Assert.assertEquals(defaultPage, listPermissions.getNextPage());
        // default delta is 10, default page is 1: 6 entities stored in database
        Assert.assertEquals(1, listPermissions.getNumPages());
        Assert.assertEquals(200, restResponse.getStatus());
    }

    @Test
    public void test32_findAllPermissionsPaginatedShouldWorkIfDeltaIsLowerThanZero() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // In this following call findAllPermissionPaginated, hadmin find
        // all Permissions with pagination, if delta is lower than zero
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        int delta = -1;
        int page = 2;
        List<Permission> permissions = new ArrayList<>();
        int numbEntities = 14;
        for (int i = 0; i < numbEntities; i++) {
            Permission permission = createPermission();
            Assert.assertNotEquals(0, permission.getId());
            permissions.add(permission);
        }
        Assert.assertEquals(numbEntities, permissions.size());
        this.impersonateUser(permissionRestApi, adminUser);
        Response restResponse = permissionRestApi.findAllPermissionPaginated(delta, page);
        HyperIoTPaginableResult<Permission> listPermissions = restResponse
                .readEntity(new GenericType<HyperIoTPaginableResult<Permission>>() {
                });
        Assert.assertFalse(listPermissions.getResults().isEmpty());
        Assert.assertTrue( listPermissions.getResults().size() > 0);
        Assert.assertEquals(defaultDelta, listPermissions.getDelta());
        Assert.assertEquals(page, listPermissions.getCurrentPage());
        Assert.assertEquals(defaultPage, listPermissions.getNextPage());
        // default delta is 10, page is 2: 14 entities stored in database
        Assert.assertEquals(2, listPermissions.getNumPages());
        Assert.assertEquals(200, restResponse.getStatus());

        //checks with page = 1
        this.impersonateUser(permissionRestApi, adminUser);
        Response restResponsePage1 = permissionRestApi.findAllPermissionPaginated(delta, 1);
        HyperIoTPaginableResult<Permission> listPermissionsPage1 = restResponsePage1
                .readEntity(new GenericType<HyperIoTPaginableResult<Permission>>() {
                });
        Assert.assertFalse(listPermissionsPage1.getResults().isEmpty());
        Assert.assertEquals(defaultDelta, listPermissionsPage1.getResults().size());
        Assert.assertEquals(defaultDelta, listPermissionsPage1.getDelta());
        Assert.assertEquals(defaultPage, listPermissionsPage1.getCurrentPage());
        Assert.assertEquals(page, listPermissionsPage1.getNextPage());
        // default delta is 10, page is 1: 14 entities stored in database
        Assert.assertEquals(2, listPermissionsPage1.getNumPages());
        Assert.assertEquals(200, restResponsePage1.getStatus());
    }

    @Test
    public void test33_findAllPermissionsPaginatedShouldWorkIfDeltaIsZero() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // In this following call findAllPermissionPaginated, hadmin find
        // all Permissions with pagination, if delta is zero
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        int delta = 0;
        int page = 2;
        List<Permission> permissions = new ArrayList<>();
        int numbEntities = 11;
        for (int i = 0; i < numbEntities; i++) {
            Permission permission = createPermission();
            Assert.assertNotEquals(0, permission.getId());
            permissions.add(permission);
        }
        Assert.assertEquals(numbEntities, permissions.size());
        this.impersonateUser(permissionRestApi, adminUser);
        Response restResponse = permissionRestApi.findAllPermissionPaginated(delta, page);
        HyperIoTPaginableResult<Permission> listPermissions = restResponse
                .readEntity(new GenericType<HyperIoTPaginableResult<Permission>>() {
                });
        Assert.assertFalse(listPermissions.getResults().isEmpty());
        Assert.assertEquals(3, listPermissions.getResults().size());
        Assert.assertEquals(defaultDelta, listPermissions.getDelta());
        Assert.assertEquals(page, listPermissions.getCurrentPage());
        Assert.assertEquals(1, listPermissions.getNextPage());
        // because delta is 10, page is 2: 11 entities stored in database
        Assert.assertEquals(2, listPermissions.getNumPages());
        Assert.assertEquals(200, restResponse.getStatus());

        //checks with page = 1
        this.impersonateUser(permissionRestApi, adminUser);
        Response restResponsePage1 = permissionRestApi.findAllPermissionPaginated(delta, 1);
        HyperIoTPaginableResult<Permission> listPermissionsPage1 = restResponsePage1
                .readEntity(new GenericType<HyperIoTPaginableResult<Permission>>() {
                });
        Assert.assertFalse(listPermissionsPage1.getResults().isEmpty());
        Assert.assertEquals(defaultDelta, listPermissionsPage1.getResults().size());
        Assert.assertEquals(defaultDelta, listPermissionsPage1.getDelta());
        Assert.assertEquals(defaultPage, listPermissionsPage1.getCurrentPage());
        Assert.assertEquals(page, listPermissionsPage1.getNextPage());
        // default delta is 10, page is 1: 11 entities stored in database
        Assert.assertEquals(2, listPermissionsPage1.getNumPages());
        Assert.assertEquals(200, restResponsePage1.getStatus());
    }

    @Test
    public void test34_findAllPermissionsPaginatedShouldWorkIfPageIsLowerThanZero() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // In this following call findAllPermissionPaginated, hadmin find
        // all Permissions with pagination, if page is lower than zero
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        int delta = 6;
        int page = -1;
        List<Permission> permissions = new ArrayList<>();
        for (int i = 0; i < defaultDelta; i++) {
            Permission permission = createPermission();
            Assert.assertNotEquals(0, permission.getId());
            permissions.add(permission);
        }
        Assert.assertEquals(defaultDelta, permissions.size());
        this.impersonateUser(permissionRestApi, adminUser);
        Response restResponse = permissionRestApi.findAllPermissionPaginated(delta, page);
        HyperIoTPaginableResult<Permission> listPermissions = restResponse
                .readEntity(new GenericType<HyperIoTPaginableResult<Permission>>() {
                });
        Assert.assertFalse(listPermissions.getResults().isEmpty());
        Assert.assertEquals(delta, listPermissions.getResults().size());
        Assert.assertEquals(delta, listPermissions.getDelta());
        Assert.assertEquals(defaultPage, listPermissions.getCurrentPage());
        Assert.assertEquals(defaultPage + 1, listPermissions.getNextPage());
        // delta is 6, default page 1: 10 entities stored in database
        Assert.assertEquals(2, listPermissions.getNumPages());
        Assert.assertEquals(200, restResponse.getStatus());

        //checks with page = 2
        this.impersonateUser(permissionRestApi, adminUser);
        Response restResponsePage2 = permissionRestApi.findAllPermissionPaginated(delta, 2);
        HyperIoTPaginableResult<Permission> listPermissionsPage2 = restResponsePage2
                .readEntity(new GenericType<HyperIoTPaginableResult<Permission>>() {
                });
        Assert.assertFalse(listPermissionsPage2.getResults().isEmpty());
        Assert.assertEquals(6, listPermissionsPage2.getResults().size());
        Assert.assertEquals(delta, listPermissionsPage2.getDelta());
        Assert.assertEquals(defaultPage + 1, listPermissionsPage2.getCurrentPage());
        Assert.assertEquals(defaultPage, listPermissionsPage2.getNextPage());
        // delta is 6, page 2: 10 entities stored in database
        Assert.assertEquals(2, listPermissionsPage2.getNumPages());
        Assert.assertEquals(200, restResponsePage2.getStatus());
    }

    @Test
    public void test35_findAllPermissionsPaginatedShouldWorkIfPageIsZero() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // In this following call findAllPermissionPaginated, hadmin find
        // all Permissions with pagination, if page is zero
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        int delta = 7;
        int page = 0;
        List<Permission> permissions = new ArrayList<>();
        for (int i = 0; i < defaultDelta; i++) {
            Permission permission = createPermission();
            Assert.assertNotEquals(0, permission.getId());
            permissions.add(permission);
        }
        Assert.assertEquals(defaultDelta, permissions.size());
        this.impersonateUser(permissionRestApi, adminUser);
        Response restResponse = permissionRestApi.findAllPermissionPaginated(delta, page);
        HyperIoTPaginableResult<Permission> listPermissions = restResponse
                .readEntity(new GenericType<HyperIoTPaginableResult<Permission>>() {
                });
        Assert.assertFalse(listPermissions.getResults().isEmpty());
        Assert.assertEquals(delta, listPermissions.getResults().size());
        Assert.assertEquals(delta, listPermissions.getDelta());
        Assert.assertEquals(defaultPage, listPermissions.getCurrentPage());
        Assert.assertEquals(defaultPage + 1, listPermissions.getNextPage());
        // delta is 7, default page is 1: 10 entities stored in database
        Assert.assertEquals(2, listPermissions.getNumPages());
        Assert.assertEquals(200, restResponse.getStatus());

        //checks with page = 2
        this.impersonateUser(permissionRestApi, adminUser);
        Response restResponsePage2 = permissionRestApi.findAllPermissionPaginated(delta, 2);
        HyperIoTPaginableResult<Permission> listPermissionsPage2 = restResponsePage2
                .readEntity(new GenericType<HyperIoTPaginableResult<Permission>>() {
                });
        Assert.assertFalse(listPermissionsPage2.getResults().isEmpty());
        Assert.assertEquals(5, listPermissionsPage2.getResults().size());
        Assert.assertEquals(delta, listPermissionsPage2.getDelta());
        Assert.assertEquals(defaultPage + 1, listPermissionsPage2.getCurrentPage());
        Assert.assertEquals(defaultPage, listPermissionsPage2.getNextPage());
        // delta is 7, page 2: 10 entities stored in database
        Assert.assertEquals(2, listPermissionsPage2.getNumPages());
        Assert.assertEquals(200, restResponsePage2.getStatus());
    }

    @Test
    public void test36_findAllPermissionPaginatedShouldFailIfNotLogged() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // the following call tries to find all Permission with pagination, but HUser is
        // not logged
        // response status code '403' HyperIoTUnauthorizedException
        List<Permission> permissions = new ArrayList<>();
        for (int i = 0; i < defaultDelta; i++) {
            Permission permission = createPermission();
            Assert.assertNotEquals(0, permission.getId());
            permissions.add(permission);
        }
        Assert.assertEquals(defaultDelta, permissions.size());
        this.impersonateUser(permissionRestApi, null);
        Response restResponse = permissionRestApi.findAllPermissionPaginated(defaultDelta, defaultPage);
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test37_savePermissionShouldFailIfEntityIsDuplicated() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // hadmin tries to save Permission with the following call savePermission, but
        // entity is duplicated
        // response status code '422' HyperIoTDuplicateEntityException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Permission permission = createPermission();
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(permissionResourceName,
                HyperIoTCrudAction.SAVE);
        Permission duplicatePermission = new Permission();
        duplicatePermission.setName(action.getActionName());
        duplicatePermission.setActionIds(action.getActionId());
        duplicatePermission.setEntityResourceName(permission.getEntityResourceName());
        duplicatePermission.setRole(permission.getRole());
        this.impersonateUser(permissionRestApi, adminUser);
        Response restResponse = permissionRestApi.savePermission(duplicatePermission);
        Assert.assertEquals(409, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTDuplicateEntityException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(3, ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().size());
        boolean roleIdIsDuplicated = false;
        boolean entityResourceNameIsDuplicated = false;
        boolean resourceIdIsDuplicated = false;
        for (int i = 0; i < ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().size(); i++) {
            if (((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(i).contentEquals("role_id")) {
                roleIdIsDuplicated = true;
                Assert.assertEquals("role_id",
                        ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(i));
            }
            if (((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(i).contentEquals("entityResourceName")) {
                entityResourceNameIsDuplicated = true;
                Assert.assertEquals("entityResourceName",
                        ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(i));
            }
            if (((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(i).contentEquals("resourceId")) {
                resourceIdIsDuplicated = true;
                Assert.assertEquals("resourceId",
                        ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(i));
            }
        }
        Assert.assertTrue(roleIdIsDuplicated);
        Assert.assertTrue(entityResourceNameIsDuplicated);
        Assert.assertTrue(resourceIdIsDuplicated);
    }

    @Test
    public void test38_updatePermissionShouldFailIfEntityNotFound() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // hadmin tries to update Permission with the following call updatePermission,
        // but entity not found
        // response status code '404' HyperIoTEntityNotFound
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Permission permission = new Permission();
        permission.setName("name edited");
        this.impersonateUser(permissionRestApi, adminUser);
        Response restResponse = permissionRestApi.updatePermission(permission);
        Assert.assertEquals(404, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test39_addActionIdInPermissionShouldWork() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // hadmin add actionId and update Permission with the following call updatePermission
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Permission permission = createPermission();
        int oldActionId = permission.getActionIds();

        HyperIoTAction addSaveAction = HyperIoTActionsUtil.getHyperIoTAction(permissionResourceName,
                HyperIoTCrudAction.SAVE);
        int addNewActionId = addSaveAction.getActionId();

        permission.addPermission(addSaveAction);
        this.impersonateUser(permissionRestApi, adminUser);
        Response restResponse = permissionRestApi.updatePermission(permission);
        Assert.assertEquals(200, restResponse.getStatus());
        Assert.assertNotEquals(oldActionId,
                ((Permission) restResponse.getEntity()).getActionIds());
        Assert.assertEquals(oldActionId + addNewActionId,
                ((Permission) restResponse.getEntity()).getActionIds());
        Assert.assertEquals(permission.getEntityVersion() + 1,
                (((Permission) restResponse.getEntity()).getEntityVersion()));
    }

    @Test
    public void test40_removeActionIdInPermissionShouldWork() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // hadmin remove actionId and update Permission with the following call updatePermission
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Permission permission = permissionWithBasicActionIds();
        int oldActionId = permission.getActionIds();

        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(permissionResourceName,
                HyperIoTCrudAction.FIND);

        int removeActionId = action.getActionId();

        permission.removePermission(action);
        this.impersonateUser(permissionRestApi, adminUser);
        Response restResponse = permissionRestApi.updatePermission(permission);
        Assert.assertEquals(200, restResponse.getStatus());
        Assert.assertNotEquals(oldActionId,
                ((Permission) restResponse.getEntity()).getActionIds());
        Assert.assertEquals(oldActionId - removeActionId,
                ((Permission) restResponse.getEntity()).getActionIds());
        Assert.assertEquals(permission.getEntityVersion() + 1,
                (((Permission) restResponse.getEntity()).getEntityVersion()));
    }

    @Test
    public void test41_removeActionIdShouldFailIfActionIdsIsNotGreaterThanZero() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // hadmin tries to remove actionId with the following call updatePermission,
        // but actionIds is not greater than zero
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Permission permission = createPermission();

        HyperIoTAction removeFindAction = HyperIoTActionsUtil.getHyperIoTAction(permissionResourceName,
                HyperIoTCrudAction.FIND);

        permission.removePermission(removeFindAction);
        this.impersonateUser(permissionRestApi, adminUser);
        Response restResponse = permissionRestApi.updatePermission(permission);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("permission-actionids", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(String.valueOf(permission.getActionIds()), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }


    @Test
    public void test42_findAllPermissionShouldWorkIfListIsEmpty() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // hadmin find all Permission with the following call findAllPermission,
        // there are still no entities saved in the database, this call return an empty list
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        this.impersonateUser(permissionRestApi, adminUser);
        Response restResponse = permissionRestApi.findAllPermission();
        List<Permission> listPermissions = restResponse.readEntity(new GenericType<List<Permission>>() {
        });
        listPermissions.stream().forEach(p -> System.out.println(p.toString()));
        Assert.assertEquals(2, listPermissions.size());
        Assert.assertEquals(200, restResponse.getStatus());
    }

    @Test
    public void test43_savePermissionShouldFailIfNameIsGreaterThan255Chars() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // hadmin tries to save Permission with the following call savePermission, but
        // but name is greater than 255 chars
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Role role = createRole();
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(permissionResourceName,
                HyperIoTCrudAction.SAVE);
        Permission permission = new Permission();
        permission.setName(createStringFieldWithSpecifiedLenght(256));
        permission.setActionIds(action.getActionId());
        permission.setEntityResourceName(action.getResourceName());
        permission.setRole(role);
        this.impersonateUser(permissionRestApi, adminUser);
        Response restResponse = permissionRestApi.savePermission(permission);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("permission-name", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(permission.getName(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }

    @Test
    public void test44_savePermissionShouldFailIfEntityResourceNameIsGreaterThan255Chars() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // hadmin tries to save Permission with the following call savePermission, but
        // entityResourceName is greater than 255 chars
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Role role = createRole();
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(permissionResourceName,
                HyperIoTCrudAction.SAVE);
        Permission permission = new Permission();
        permission.setName(action.getActionName());
        permission.setActionIds(action.getActionId());
        permission.setEntityResourceName(createStringFieldWithSpecifiedLenght(256));
        permission.setRole(role);
        this.impersonateUser(permissionRestApi, adminUser);
        Response restResponse = permissionRestApi.savePermission(permission);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("permission-entityresourcename", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(permission.getEntityResourceName(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }

    @Test
    public void test45_updatePermissionShouldFailIfNameIsGreaterThan255Chars() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // hadmin tries to update Permission with the following call updatePermission,
        // but name is greater than 255 chars
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Permission permission = createPermission();
        permission.setName(createStringFieldWithSpecifiedLenght(256));
        this.impersonateUser(permissionRestApi, adminUser);
        Response restResponse = permissionRestApi.updatePermission(permission);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("permission-name", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(permission.getName(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }

    @Test
    public void test45_updatePermissionShouldFailIfEntityResourceNameIsGreaterThan255Chars() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // hadmin tries to update Permission with the following call updatePermission,
        // but entity resource name is greater than 255 chars
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Permission permission = createPermission();
        permission.setEntityResourceName(createStringFieldWithSpecifiedLenght(256));
        this.impersonateUser(permissionRestApi, adminUser);
        Response restResponse = permissionRestApi.updatePermission(permission);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("permission-entityresourcename", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(permission.getEntityResourceName(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
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

    private Permission permissionWithBasicActionIds() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // hadmin update Permission with the following call updatePermission
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Permission permission = createPermission();
        int oldActionId = permission.getActionIds();

        // Add basic crud actions
        HyperIoTAction actionFindAll = HyperIoTActionsUtil.getHyperIoTAction(permissionResourceName,
                HyperIoTCrudAction.FINDALL);
        int findActionId = actionFindAll.getActionId();
        permission.addPermission(actionFindAll);

        HyperIoTAction actionRemove = HyperIoTActionsUtil.getHyperIoTAction(permissionResourceName,
                HyperIoTCrudAction.REMOVE);
        int removeActionId = actionRemove.getActionId();
        permission.addPermission(actionRemove);

        HyperIoTAction actionUpdate = HyperIoTActionsUtil.getHyperIoTAction(permissionResourceName,
                HyperIoTCrudAction.UPDATE);
        int updateActionId = actionUpdate.getActionId();
        permission.addPermission(actionUpdate);

        HyperIoTAction actionSave = HyperIoTActionsUtil.getHyperIoTAction(permissionResourceName,
                HyperIoTCrudAction.SAVE);
        int saveActionId = actionSave.getActionId();
        permission.addPermission(actionSave);

        this.impersonateUser(permissionRestApi, adminUser);
        Response restResponse = permissionRestApi.updatePermission(permission);
        Assert.assertEquals(200, restResponse.getStatus());
        Assert.assertEquals(oldActionId + findActionId + removeActionId + updateActionId + saveActionId,
                ((Permission) restResponse.getEntity()).getActionIds());
        Assert.assertEquals(permission.getEntityVersion() + 1,
                (((Permission) restResponse.getEntity()).getEntityVersion()));
        return permission;
    }

    public void beforeTest() {
        // Utility method: remove default permissions
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        this.impersonateUser(permissionRestApi, adminUser);
        Response restResponsePermission = permissionRestApi.findAllPermission();
        List<Permission> listPermissions = restResponsePermission.readEntity(new GenericType<List<Permission>>() {
        });
        if (!listPermissions.isEmpty()) {
            Assert.assertFalse(listPermissions.isEmpty());
            for (Permission permission : listPermissions) {
                if (permission.getName().contains("RegisteredUser Permissions")) {
                    this.impersonateUser(permissionRestApi, adminUser);
                    Response restResponse = permissionRestApi.deletePermission(permission.getId());
                    Assert.assertEquals(200, restResponse.getStatus());
                    Assert.assertNull(restResponse.getEntity());
                }
            }
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
