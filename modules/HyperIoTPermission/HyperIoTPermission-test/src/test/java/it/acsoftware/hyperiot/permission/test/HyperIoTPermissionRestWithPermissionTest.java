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
import it.acsoftware.hyperiot.base.api.entity.HyperIoTPaginableResult;
import it.acsoftware.hyperiot.base.model.HyperIoTBaseError;
import it.acsoftware.hyperiot.base.service.rest.HyperIoTBaseRestApi;
import it.acsoftware.hyperiot.base.test.HyperIoTTestConfigurationBuilder;
import it.acsoftware.hyperiot.huser.api.HUserSystemApi;
import it.acsoftware.hyperiot.huser.model.HUser;
import it.acsoftware.hyperiot.huser.service.rest.HUserRestApi;
import it.acsoftware.hyperiot.huser.test.util.HyperIoTHUserTestUtils;
import it.acsoftware.hyperiot.permission.actions.HyperIoTPermissionAction;
import it.acsoftware.hyperiot.permission.api.PermissionSystemApi;
import it.acsoftware.hyperiot.permission.model.Permission;
import it.acsoftware.hyperiot.permission.service.rest.PermissionRestApi;
import it.acsoftware.hyperiot.permission.test.util.HyperIoTPermissionTestUtil;
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

import static it.acsoftware.hyperiot.permission.test.HyperIoTPermissionConfiguration.*;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HyperIoTPermissionRestWithPermissionTest extends KarafTestSupport {

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
    public void test02_saveWithPermissionShouldWork() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // HUser, with permission, save Permission with the following call
        // savePermission
        // response status code '200'
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(permissionResourceName,
                HyperIoTCrudAction.SAVE);
        HUser huser = createHUser(action);
        Role role = createRole();
        Permission permission = new Permission();
        permission.setName(action.getActionName());
        permission.setActionIds(action.getActionId());
        permission.setEntityResourceName(action.getResourceName());
        permission.setRole(role);
        this.impersonateUser(permissionRestApi, huser);
        Response restResponse = permissionRestApi.savePermission(permission);
        Assert.assertEquals(200, restResponse.getStatus());
    }

    @Test
    public void test03_saveWithoutPermissionShouldFail() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // HUser, without permission, tries to save Permission with the following call
        // savePermission
        // response status code '403' HyperIoTUnauthorizedException
        HUser huser = createHUser(null);
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

    @Test
    public void test04_updateWithPermissionShouldWork() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // HUser, with permission, update Permission with the following call
        // updatePermission
        // response status code '200'
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(permissionResourceName,
                HyperIoTCrudAction.UPDATE);
        HUser huser = createHUser(action);
        Permission permission = createPermission();
        permission.setName("name edited");
        this.impersonateUser(permissionRestApi, huser);
        Response restResponse = permissionRestApi.updatePermission(permission);
        Assert.assertEquals(200, restResponse.getStatus());
    }

    @Test
    public void test05_updateWithoutPermissionShouldFail() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // HUser, without permission, tries to update Permission with the following call
        // updatePermission
        // response status code '403' HyperIoTUnauthorizedException
        HUser huser = createHUser(null);
        Permission permission = createPermission();
        permission.setName("name edited");
        this.impersonateUser(permissionRestApi, huser);
        Response restResponse = permissionRestApi.updatePermission(permission);
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test06_findWithPermissionShouldWork() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // HUser, with permission, find Permission with the following call
        // findPermission
        // response status code '200'
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(permissionResourceName,
                HyperIoTCrudAction.FIND);
        HUser huser = createHUser(action);
        Permission permission = createPermission();
        this.impersonateUser(permissionRestApi, huser);
        Response restResponse = permissionRestApi.findPermission(permission.getId());
        Assert.assertEquals(200, restResponse.getStatus());
    }

    @Test
    public void test07_findWithoutPermissionShouldFail() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // HUser, without permission, tries to find Permission with the following call
        // findPermission
        // response status code '403' HyperIoTUnauthorizedException
        HUser huser = createHUser(null);
        Permission permission = createPermission();
        this.impersonateUser(permissionRestApi, huser);
        Response restResponse = permissionRestApi.findPermission(permission.getId());
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test08_findWithPermissionShouldFailIfEntityNotFound() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // HUser, with permission, find Permission with the following call
        // findPermission, but entity not found
        // response status code '404' HyperIoTEntityNotFound
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(permissionResourceName,
                HyperIoTCrudAction.FIND);
        HUser huser = createHUser(action);
//		createPermission();
        this.impersonateUser(permissionRestApi, huser);
        Response restResponse = permissionRestApi.findPermission(0);
        Assert.assertEquals(404, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test10_deleteWithPermissionShouldWork() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // HUser, with permission, delete Permission with the following call
        // deletePermission
        // response status code '200'
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(permissionResourceName,
                HyperIoTCrudAction.REMOVE);
        HUser huser = createHUser(action);
        Permission permission = createPermission();
        this.impersonateUser(permissionRestApi, huser);
        Response restResponse = permissionRestApi.deletePermission(permission.getId());
        Assert.assertEquals(200, restResponse.getStatus());
    }

    @Test
    public void test11_deleteWithoutPermissionShouldFail() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // HUser, without permission, tries to delete Permission with the following call
        // deletePermission
        // response status code '403' HyperIoTUnauthorizedException
        HUser huser = createHUser(null);
        Permission permission = createPermission();
        this.impersonateUser(permissionRestApi, huser);
        Response restResponse = permissionRestApi.deletePermission(permission.getId());
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test12_deleteWithPermissionShouldFailIfEntityNotFound() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // HUser, with permission, delete Permission with the following call
        // deletePermission, but entity not found
        // response status code '404' HyperIoTEntityNotFound
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(permissionResourceName,
                HyperIoTCrudAction.REMOVE);
        HUser huser = createHUser(action);
//		createPermission();
        this.impersonateUser(permissionRestApi, huser);
        Response restResponse = permissionRestApi.deletePermission(0);
        Assert.assertEquals(404, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test14_findAllWithPermissionShouldWork() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // HUser, with permission, find all Permission with the following call
        // findAllPermission
        // response status code '200'
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(permissionResourceName,
                HyperIoTCrudAction.FINDALL);
        HUser huser = createHUser(action);
        Permission permission = createPermission();
        Assert.assertNotEquals(0, permission.getId());
        this.impersonateUser(permissionRestApi, huser);
        Response restResponse = permissionRestApi.findAllPermission();
        List<Permission> listPermissions = restResponse.readEntity(new GenericType<List<Permission>>() {
        });
        Assert.assertNotEquals(0, listPermissions.size());
        Assert.assertEquals(4, listPermissions.size());
        Assert.assertFalse(listPermissions.isEmpty());
        boolean permissionFound = false;
        boolean permissionAssignedToHuserFound = false;
        for (Permission p : listPermissions) {
            if (permission.getId() == p.getId()) {
                permissionFound = true;
            }
            if (p.getName().contains("assigned to huser_id " + huser.getId())) {
                Assert.assertEquals(permissionResourceName + " assigned to huser_id " + huser.getId(),
                        p.getName());
                Assert.assertEquals(action.getActionId(), p.getActionIds());
                permissionAssignedToHuserFound = true;
            }
        }
        Assert.assertTrue(permissionFound);
        Assert.assertTrue(permissionAssignedToHuserFound);
        Assert.assertEquals(200, restResponse.getStatus());
    }

    @Test
    public void test15_findAllWithoutPermissionShouldFail() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // HUser, without permission, tries to find all Permission with the following
        // call findAllPermission
        // response status code '403' HyperIoTUnauthorizedException
        HUser huser = createHUser(null);
        Permission permission = createPermission();
        Assert.assertNotEquals(0, permission.getId());
        this.impersonateUser(permissionRestApi, huser);
        Response restResponse = permissionRestApi.findAllPermission();
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test16_saveWithPermissionShouldFailIfNameIsMaliciousCode() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // HUser, with permission, tries to save Permission with the following call
        // savePermission, but name is malicious code
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(permissionResourceName,
                HyperIoTCrudAction.SAVE);
        HUser huser = createHUser(action);
        Role role = createRole();
        Permission permission = new Permission();
        permission.setName("</script>");
        permission.setActionIds(action.getActionId());
        permission.setEntityResourceName(action.getResourceName());
        permission.setRole(role);
        this.impersonateUser(permissionRestApi, huser);
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
    public void test17_saveWithPermissionShouldFailIfNameIsBlank() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // HUser, with permission, tries to save Permission with the following call
        // savePermission, but name is blank
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(permissionResourceName,
                HyperIoTCrudAction.SAVE);
        HUser huser = createHUser(action);
        Role role = createRole();
        Permission permission = new Permission();
        permission.setName(" ");
        permission.setActionIds(action.getActionId());
        permission.setEntityResourceName(action.getResourceName());
        permission.setRole(role);
        this.impersonateUser(permissionRestApi, huser);
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
    public void test18_saveWithPermissionShouldFailIfActionIdsIsNegative() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // HUser, with permission, tries to save Permission with the following call
        // savePermission, but actionIds is not a positive number
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(permissionResourceName,
                HyperIoTCrudAction.SAVE);
        HUser huser = createHUser(action);
        Role role = createRole();
        Permission permission = new Permission();
        permission.setName(action.getActionName());
        permission.setActionIds(-1);
        permission.setEntityResourceName(action.getResourceName());
        permission.setRole(role);
        this.impersonateUser(permissionRestApi, huser);
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
    public void test19_saveWithPermissionShouldFailIfEntityResourceNameIsNull() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // HUser, with permission, tries to save Permission with the following call
        // savePermission, but entityResourceName is null
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(permissionResourceName,
                HyperIoTCrudAction.SAVE);
        HUser huser = createHUser(action);
        Role role = createRole();
        Permission permission = new Permission();
        permission.setName(action.getActionName());
        permission.setActionIds(action.getActionId());
        permission.setEntityResourceName(null);
        permission.setRole(role);
        this.impersonateUser(permissionRestApi, huser);
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
    public void test20_saveWithPermissionShouldFailIfEntityResourceNameIsEmpty() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // HUser, with permission, tries to save Permission with the following call
        // savePermission, but entityResourceName is empty
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(permissionResourceName,
                HyperIoTCrudAction.SAVE);
        HUser huser = createHUser(action);
        Role role = createRole();
        Permission permission = new Permission();
        permission.setName(action.getActionName());
        permission.setActionIds(action.getActionId());
        permission.setEntityResourceName("");
        permission.setRole(role);
        this.impersonateUser(permissionRestApi, huser);
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
    public void test21_saveWithPermissionShouldFailIfEntityResourceNameIsMaliciousCode() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // HUser, with permission, tries to save Permission with the following call
        // savePermission, but entityResourceName is malicious code
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(permissionResourceName,
                HyperIoTCrudAction.SAVE);
        HUser huser = createHUser(action);
        Role role = createRole();
        Permission permission = new Permission();
        permission.setName(action.getActionName());
        permission.setActionIds(action.getActionId());
        permission.setEntityResourceName("vbscript:");
        permission.setRole(role);
        this.impersonateUser(permissionRestApi, huser);
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
    public void test22_saveWithPermissionShouldFailIfRoleIsNull() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // HUser, with permission, tries to save Permission with the following call
        // savePermission, but Role is null
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(permissionResourceName,
                HyperIoTCrudAction.SAVE);
        HUser huser = createHUser(action);
        Permission permission = new Permission();
        permission.setName(action.getActionName());
        permission.setActionIds(action.getActionId());
        permission.setEntityResourceName(action.getResourceName());
        permission.setRole(null);
        this.impersonateUser(permissionRestApi, huser);
        Response restResponse = permissionRestApi.savePermission(permission);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("permission-role", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
    }

    @Test
    public void test23_updateWithPermissionShouldFailIfNameIsMaliciousCode() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // HUser, with permission, tries to update Permission with the following call
        // updatePermission, but name is malicious code
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(permissionResourceName,
                HyperIoTCrudAction.UPDATE);
        HUser huser = createHUser(action);
        Permission permission = createPermission();
        permission.setName("eval(malicious code)");
        this.impersonateUser(permissionRestApi, huser);
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
    public void test24_updateWithPermissionShouldFailIfNameIsBlank() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // HUser, with permission, tries to update Permission with the following call
        // updatePermission, but name is blank
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(permissionResourceName,
                HyperIoTCrudAction.UPDATE);
        HUser huser = createHUser(action);
        Permission permission = createPermission();
        permission.setName(" ");
        this.impersonateUser(permissionRestApi, huser);
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
    public void test25_updateWithPermissionShouldFailIfActionIdsIsNegative() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // HUser, with permission, tries to update Permission with the following call
        // updatePermission, but actionIds is not a positive number
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(permissionResourceName,
                HyperIoTCrudAction.UPDATE);
        HUser huser = createHUser(action);
        Permission permission = createPermission();
        permission.setActionIds(-1);
        this.impersonateUser(permissionRestApi, huser);
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
    public void test26_updateWithPermissionShouldFailIfEntityResourceNameIsNull() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // HUser, with permission, tries to update Permission with the following call
        // updatePermission, but entityResourceName is null
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(permissionResourceName,
                HyperIoTCrudAction.UPDATE);
        HUser huser = createHUser(action);
        Permission permission = createPermission();
        permission.setEntityResourceName(null);
        this.impersonateUser(permissionRestApi, huser);
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
    public void test27_updateWithPermissionShouldFailIfEntityResourceNameIsEmpty() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // HUser, with permission, tries to update Permission with the following call
        // updatePermission, but entityResourceName is empty
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(permissionResourceName,
                HyperIoTCrudAction.UPDATE);
        HUser huser = createHUser(action);
        Permission permission = createPermission();
        permission.setEntityResourceName("");
        this.impersonateUser(permissionRestApi, huser);
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
    public void test28_updateWithPermissionShouldFailIfEntityResourceNameIsMaliciousCode() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // HUser, with permission, tries to update Permission with the following call
        // updatePermission, but entityResourceName is malicious code
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(permissionResourceName,
                HyperIoTCrudAction.UPDATE);
        HUser huser = createHUser(action);
        Permission permission = createPermission();
        permission.setEntityResourceName("</script>");
        this.impersonateUser(permissionRestApi, huser);
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
    public void test29_updateWithPermissionShouldFailIfRoleIsNull() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // HUser, with permission, tries to update Permission with the following call
        // updatePermission, but Role is null
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(permissionResourceName,
                HyperIoTCrudAction.UPDATE);
        HUser huser = createHUser(action);
        Permission permission = createPermission();
        permission.setRole(null);
        this.impersonateUser(permissionRestApi, huser);
        Response restResponse = permissionRestApi.updatePermission(permission);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("permission-role", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
    }

    @Test
    public void test30_findAllActionsWithPermissionShouldWork() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // HUser, with permission, find all Actions with the following call
        // findAllActions
        // response status code '200'
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(permissionResourceName,
                HyperIoTPermissionAction.LIST_ACTIONS);
        HUser huser = createHUser(action);
        this.impersonateUser(permissionRestApi, huser);
        Response restResponse = permissionRestApi.findAllActions();
        HashMap<String, List<HyperIoTAction>> actions = restResponse
                .readEntity(new GenericType<HashMap<String, List<HyperIoTAction>>>() {
                });
        Assert.assertFalse(actions.isEmpty());
        Assert.assertEquals(200, restResponse.getStatus());
    }

    @Test
    public void test31_findAllActionsWithoutPermissionShouldFail() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // HUser, without permission, tries to find all Actions with the following call
        // findAllActions
        // response status code '403' HyperIoTUnauthorizedException
        HUser huser = createHUser(null);
        this.impersonateUser(permissionRestApi, huser);
        Response restResponse = permissionRestApi.findAllActions();
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test32_findAllPermissionPaginatedWithAuthorizationShouldWork() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // In this following call findAllPermissionPaginated, HUser, with permission,
        // find all Permissions with pagination
        // response status code '200'
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(permissionResourceName,
                HyperIoTCrudAction.FINDALL);
        HUser huser = createHUser(action);
        List<Permission> permissions = new ArrayList<>();
        int delta = 10;
        int page = 1;
        int numbEntities = 8; // +1 Permission assigned to huser
        for (int i = 0; i < numbEntities; i++) {
            Permission permission = createPermission();
            Assert.assertNotEquals(0, permission.getId());
            permissions.add(permission);
        }
        Assert.assertEquals(numbEntities, permissions.size());
        this.impersonateUser(permissionRestApi, huser);
        Response restResponse = permissionRestApi.findAllPermissionPaginated(delta, page);
        HyperIoTPaginableResult<Permission> listPermissions = restResponse
                .readEntity(new GenericType<HyperIoTPaginableResult<Permission>>() {
                });
        Assert.assertFalse(listPermissions.getResults().isEmpty());
        Assert.assertEquals(numbEntities + 2, listPermissions.getResults().size());
        Assert.assertEquals(delta, listPermissions.getDelta());
        Assert.assertEquals(page, listPermissions.getCurrentPage());
        Assert.assertEquals(defaultPage+1, listPermissions.getNextPage());
        // delta is 10, page 1: 9 entities stored in database
        Assert.assertEquals(2, listPermissions.getNumPages());
        Assert.assertEquals(200, restResponse.getStatus());
    }

    @Test
    public void test33_findAllPermissionPaginatedWithAuthorizationShouldWorkIfDeltaAndPageAreNull() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // In this following call findAllPermissionsPaginated HUser, with permission,
        // find all Permissions with pagination if delta and page are null
        // response status code '200'
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(permissionResourceName,
                HyperIoTCrudAction.FINDALL);
        HUser huser = createHUser(action);
        Integer delta = null;
        Integer page = null;
        List<Permission> permissions = new ArrayList<>();
        int numbEntities = 7; // +1 Permission assigned to huser
        for (int i = 0; i < numbEntities; i++) {
            Permission permission = createPermission();
            Assert.assertNotEquals(0, permission.getId());
            permissions.add(permission);
        }
        Assert.assertEquals(numbEntities, permissions.size());
        this.impersonateUser(permissionRestApi, huser);
        Response restResponse = permissionRestApi.findAllPermissionPaginated(delta, page);
        HyperIoTPaginableResult<Permission> listPermissions = restResponse
                .readEntity(new GenericType<HyperIoTPaginableResult<Permission>>() {
                });
        Assert.assertFalse(listPermissions.getResults().isEmpty());
        Assert.assertEquals(numbEntities + 3, listPermissions.getResults().size());
        Assert.assertEquals(defaultDelta, listPermissions.getDelta());
        Assert.assertEquals(defaultPage, listPermissions.getCurrentPage());
        Assert.assertEquals(defaultPage, listPermissions.getNextPage());
        // default delta is 10, default page is 1: 8 entities stored in database
        Assert.assertEquals(1, listPermissions.getNumPages());
        Assert.assertEquals(200, restResponse.getStatus());
    }

    @Test
    public void test34_findAllPermissionPaginatedWithAuthorizationShouldWorkIfDeltaIsLowerThanZero() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // In this following call findAllPermissionsPaginated HUser, with permission,
        // find all Permissions with pagination if delta is lower than zero
        // response status code '200'
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(permissionResourceName,
                HyperIoTCrudAction.FINDALL);
        HUser huser = createHUser(action);
        int delta = -1;
        int page = 2;
        List<Permission> permissions = new ArrayList<>();
        int numbEntities = 11; // +1 Permission assigned to huser
        for (int i = 0; i < numbEntities; i++) {
            Permission permission = createPermission();
            Assert.assertNotEquals(0, permission.getId());
            permissions.add(permission);
        }
        Assert.assertEquals(numbEntities, permissions.size());
        this.impersonateUser(permissionRestApi, huser);
        Response restResponse = permissionRestApi.findAllPermissionPaginated(delta, page);
        HyperIoTPaginableResult<Permission> listPermissions = restResponse
                .readEntity(new GenericType<HyperIoTPaginableResult<Permission>>() {
                });
        Assert.assertFalse(listPermissions.getResults().isEmpty());
        Assert.assertEquals(4, listPermissions.getResults().size());
        Assert.assertEquals(defaultDelta, listPermissions.getDelta());
        Assert.assertEquals(page, listPermissions.getCurrentPage());
        Assert.assertEquals(defaultPage, listPermissions.getNextPage());
        // default delta is 10, page is 2: 12 entities stored in database
        Assert.assertEquals(2, listPermissions.getNumPages());
        Assert.assertEquals(200, restResponse.getStatus());

        //checks with page = 1
        this.impersonateUser(permissionRestApi, huser);
        Response restResponsePage1 = permissionRestApi.findAllPermissionPaginated(delta, 1);
        HyperIoTPaginableResult<Permission> listPermissionsPage1 = restResponsePage1
                .readEntity(new GenericType<HyperIoTPaginableResult<Permission>>() {
                });
        Assert.assertFalse(listPermissionsPage1.getResults().isEmpty());
        Assert.assertEquals(defaultDelta, listPermissionsPage1.getResults().size());
        Assert.assertEquals(defaultDelta, listPermissionsPage1.getDelta());
        Assert.assertEquals(defaultPage, listPermissionsPage1.getCurrentPage());
        Assert.assertEquals(page, listPermissionsPage1.getNextPage());
        // default delta is 10, page is 1: 12 entities stored in database
        Assert.assertEquals(2, listPermissionsPage1.getNumPages());
        Assert.assertEquals(200, restResponsePage1.getStatus());
    }

    @Test
    public void test35_findAllPermissionPaginatedWithAuthorizationShouldWorkIfDeltaIsZero() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // In this following call findAllPermissionsPaginated HUser, with permission,
        // find all Permissions with pagination if delta is zero
        // response status code '200'
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(permissionResourceName,
                HyperIoTCrudAction.FINDALL);
        HUser huser = createHUser(action);
        int delta = 0;
        int page = 3;
        List<Permission> permissions = new ArrayList<>();
        int numbEntities = 21; // +1 Permission assigned to huser
        for (int i = 0; i < numbEntities; i++) {
            Permission permission = createPermission();
            Assert.assertNotEquals(0, permission.getId());
            permissions.add(permission);
        }
        Assert.assertEquals(numbEntities, permissions.size());
        this.impersonateUser(permissionRestApi, huser);
        Response restResponse = permissionRestApi.findAllPermissionPaginated(delta, page);
        HyperIoTPaginableResult<Permission> listPermissions = restResponse
                .readEntity(new GenericType<HyperIoTPaginableResult<Permission>>() {
                });
        Assert.assertFalse(listPermissions.getResults().isEmpty());
        Assert.assertEquals(4, listPermissions.getResults().size());
        Assert.assertEquals(defaultDelta, listPermissions.getDelta());
        Assert.assertEquals(page, listPermissions.getCurrentPage());
        Assert.assertEquals(defaultPage, listPermissions.getNextPage());
        // because delta is 10, page is 3: 22 entities stored in database
        Assert.assertEquals(3, listPermissions.getNumPages());
        Assert.assertEquals(200, restResponse.getStatus());

        //checks with page = 1
        this.impersonateUser(permissionRestApi, huser);
        Response restResponsePage1 = permissionRestApi.findAllPermissionPaginated(delta, 1);
        HyperIoTPaginableResult<Permission> listPermissionsPage1 = restResponsePage1
                .readEntity(new GenericType<HyperIoTPaginableResult<Permission>>() {
                });
        Assert.assertFalse(listPermissionsPage1.getResults().isEmpty());
        Assert.assertEquals(defaultDelta, listPermissionsPage1.getResults().size());
        Assert.assertEquals(defaultDelta, listPermissionsPage1.getDelta());
        Assert.assertEquals(defaultPage, listPermissionsPage1.getCurrentPage());
        Assert.assertEquals(defaultPage + 1, listPermissionsPage1.getNextPage());
        // default delta is 10, page is 1: 22 entities stored in database
        Assert.assertEquals(3, listPermissionsPage1.getNumPages());
        Assert.assertEquals(200, restResponsePage1.getStatus());

        //checks with page = 2
        this.impersonateUser(permissionRestApi, huser);
        Response restResponsePage2 = permissionRestApi.findAllPermissionPaginated(delta, 2);
        HyperIoTPaginableResult<Permission> listPermissionsPage2 = restResponsePage2
                .readEntity(new GenericType<HyperIoTPaginableResult<Permission>>() {
                });
        Assert.assertFalse(listPermissionsPage2.getResults().isEmpty());
        Assert.assertEquals(defaultDelta, listPermissionsPage2.getResults().size());
        Assert.assertEquals(defaultDelta, listPermissionsPage2.getDelta());
        Assert.assertEquals(defaultPage + 1, listPermissionsPage2.getCurrentPage());
        Assert.assertEquals(page, listPermissionsPage2.getNextPage());
        // default delta is 10, page is 2: 22 entities stored in database
        Assert.assertEquals(3, listPermissionsPage2.getNumPages());
        Assert.assertEquals(200, restResponsePage2.getStatus());
    }

    @Test
    public void test36_findAllPermissionPaginatedWithAuthorizationShouldWorkIfPageIsLowerThanZero() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // In this following call findAllPermissionsPaginated HUser, with permission,
        // find all Permissions with pagination if page is lower than zero
        // response status code '200'
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(permissionResourceName,
                HyperIoTCrudAction.FINDALL);
        HUser huser = createHUser(action);
        int delta = 8;
        int page = -1;
        List<Permission> permissions = new ArrayList<>();
        // +1 Permission assigned to huser
        for (int i = 0; i < defaultDelta; i++) {
            Permission permission = createPermission();
            Assert.assertNotEquals(0, permission.getId());
            permissions.add(permission);
        }
        Assert.assertEquals(defaultDelta, permissions.size());
        this.impersonateUser(permissionRestApi, huser);
        Response restResponse = permissionRestApi.findAllPermissionPaginated(delta, page);
        HyperIoTPaginableResult<Permission> listPermissions = restResponse
                .readEntity(new GenericType<HyperIoTPaginableResult<Permission>>() {
                });
        Assert.assertFalse(listPermissions.getResults().isEmpty());
        Assert.assertEquals(delta, listPermissions.getResults().size());
        Assert.assertEquals(delta, listPermissions.getDelta());
        Assert.assertEquals(defaultPage, listPermissions.getCurrentPage());
        Assert.assertEquals(defaultPage + 1, listPermissions.getNextPage());
        // delta is 8, default page 1: 11 entities stored in database
        Assert.assertEquals(2, listPermissions.getNumPages());
        Assert.assertEquals(200, restResponse.getStatus());

        //checks with page = 2
        this.impersonateUser(permissionRestApi, huser);
        Response restResponsePage2 = permissionRestApi.findAllPermissionPaginated(delta, 2);
        HyperIoTPaginableResult<Permission> listPermissionsPage2 = restResponsePage2
                .readEntity(new GenericType<HyperIoTPaginableResult<Permission>>() {
                });
        Assert.assertFalse(listPermissionsPage2.getResults().isEmpty());
        Assert.assertEquals(5, listPermissionsPage2.getResults().size());
        Assert.assertEquals(delta, listPermissionsPage2.getDelta());
        Assert.assertEquals(defaultPage + 1, listPermissionsPage2.getCurrentPage());
        Assert.assertEquals(defaultPage, listPermissionsPage2.getNextPage());
        // delta is 8, page 2: 11 entities stored in database
        Assert.assertEquals(2, listPermissionsPage2.getNumPages());
        Assert.assertEquals(200, restResponsePage2.getStatus());
    }

    @Test
    public void test37_findAllPermissionPaginatedWithAuthorizationShouldWorkIfPageIsZero() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // In this following call findAllPermissionsPaginated HUser, with permission,
        // find all Permissions with pagination if page is zero
        // response status code '200'
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(permissionResourceName,
                HyperIoTCrudAction.FINDALL);
        HUser huser = createHUser(action);
        int delta = 7;
        int page = 0;
        List<Permission> permissions = new ArrayList<>();
        int numbEntities = 5; // +1 Permission assigned to huser
        for (int i = 0; i < numbEntities; i++) {
            Permission permission = createPermission();
            Assert.assertNotEquals(0, permission.getId());
            permissions.add(permission);
        }
        Assert.assertEquals(numbEntities, permissions.size());
        this.impersonateUser(permissionRestApi, huser);
        Response restResponse = permissionRestApi.findAllPermissionPaginated(delta, page);
        HyperIoTPaginableResult<Permission> listPermissions = restResponse
                .readEntity(new GenericType<HyperIoTPaginableResult<Permission>>() {
                });
        Assert.assertFalse(listPermissions.getResults().isEmpty());
        Assert.assertEquals(7, listPermissions.getResults().size());
        Assert.assertEquals(delta, listPermissions.getDelta());
        Assert.assertEquals(defaultPage, listPermissions.getCurrentPage());
        Assert.assertEquals(defaultPage+1, listPermissions.getNextPage());
        // delta is 7, default page is 1: 6 entities stored in database
        Assert.assertEquals(2, listPermissions.getNumPages());
        Assert.assertEquals(200, restResponse.getStatus());
    }

    @Test
    public void test38_findAllPermissionPaginatedWithoutAuthorizationShouldFail() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // In this following call findAllPermission, HUser, without permission, tries to
        // find all Permissions with pagination
        // response status code '403' HyperIoTUnauthorizedException
        HUser huser = createHUser(null);
        int delta = 10;
        int page = 1;
        this.impersonateUser(permissionRestApi, huser);
        Response restResponse = permissionRestApi.findAllPermissionPaginated(delta, page);
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test39_saveWithPermissionShouldFailIfEntityIsDuplicated() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // HUser, with permission, tries to save Permission with the following call
        // savePermission, but entity is duplicated
        // response status code '422' HyperIoTDuplicateEntityException
        Permission permission = createPermission();
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(permissionResourceName,
                HyperIoTCrudAction.SAVE);
        HUser huser = createHUser(action);
        Permission duplicatePermission = new Permission();
        duplicatePermission.setName(action.getActionName());
        duplicatePermission.setActionIds(action.getActionId());
        duplicatePermission.setEntityResourceName(permission.getEntityResourceName());
        duplicatePermission.setRole(permission.getRole());
        this.impersonateUser(permissionRestApi, huser);
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
    public void test40_updateWithPermissionShouldFailIfEntityNotFound() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // HUser, with permission, tries to update Permission with the following call
        // updatePermission, but entity not found
        // response status code '404' HyperIoTEntityNotFound
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(permissionResourceName,
                HyperIoTCrudAction.UPDATE);
        HUser huser = createHUser(action);
        Permission permission = new Permission();
        permission.setName("name edited");
        this.impersonateUser(permissionRestApi, huser);
        Response restResponse = permissionRestApi.updatePermission(permission);
        Assert.assertEquals(404, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test41_updateWithoutPermissionShouldFailAndEntityNotFound() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // HUser, without permission, tries to update Permission not found with the
        // following call updatePermission
        // response status code '403' HyperIoTUnauthorizedException
        HUser huser = createHUser(null);
        Permission permission = new Permission();
        permission.setName("name edited");
        this.impersonateUser(permissionRestApi, huser);
        Response restResponse = permissionRestApi.updatePermission(permission);
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test42_addActionIdWithPermissionShouldWork() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // huser, with permission, add actionId and update Permission with the following call updatePermission
        // response status code '200'
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(permissionResourceName,
                HyperIoTCrudAction.UPDATE);
        HUser huser = createHUser(action);

        Permission permission = createPermission();
        int oldActionId = permission.getActionIds();

        HyperIoTAction addSaveAction = HyperIoTActionsUtil.getHyperIoTAction(permissionResourceName,
                HyperIoTCrudAction.SAVE);
        int addNewActionId = addSaveAction.getActionId();

        permission.addPermission(addSaveAction);
        this.impersonateUser(permissionRestApi, huser);
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
    public void test43_addActionIdWithoutPermissionShouldFail() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // huser, without permission, tries to add actionId with the following call updatePermission
        // response status code '403' HyperIoTUnauthorizedException
        HUser huser = createHUser(null);
        Permission permission = createPermission();

        HyperIoTAction addFindAction = HyperIoTActionsUtil.getHyperIoTAction(permissionResourceName,
                HyperIoTCrudAction.FIND);

        permission.addPermission(addFindAction);
        this.impersonateUser(permissionRestApi, huser);
        Response restResponse = permissionRestApi.updatePermission(permission);
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test44_removeActionIdWithPermissionShouldWork() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // huser, with permission, remove actionId and update Permission with the following call updatePermission
        // response status code '200'
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(permissionResourceName,
                HyperIoTCrudAction.UPDATE);
        HUser huser = createHUser(action);
        Permission permission = permissionWithBasicActionIds();
        int oldActionId = permission.getActionIds();

        HyperIoTAction removeFindAction = HyperIoTActionsUtil.getHyperIoTAction(permissionResourceName,
                HyperIoTCrudAction.FIND);
        int removeActionId = removeFindAction.getActionId();

        permission.removePermission(removeFindAction);
        this.impersonateUser(permissionRestApi, huser);
        Response restResponse = permissionRestApi.updatePermission(permission);
        Assert.assertEquals(200, restResponse.getStatus());
        Assert.assertNotEquals(oldActionId, ((Permission) restResponse.getEntity()).getActionIds());
        Assert.assertEquals(oldActionId - removeActionId,
                ((Permission) restResponse.getEntity()).getActionIds());
        Assert.assertEquals(permission.getEntityVersion() + 1,
                (((Permission) restResponse.getEntity()).getEntityVersion()));
    }

    @Test
    public void test45_removeActionIdWithoutPermissionShouldFail() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // huser, without permission, tries to remove actionId with the following call updatePermission
        // response status code '403' HyperIoTUnauthorizedException
        HUser huser = createHUser(null);
        Permission permission = permissionWithBasicActionIds();

        HyperIoTAction removeFindAction = HyperIoTActionsUtil.getHyperIoTAction(permissionResourceName,
                HyperIoTCrudAction.FIND);

        permission.removePermission(removeFindAction);
        this.impersonateUser(permissionRestApi, huser);
        Response restResponse = permissionRestApi.updatePermission(permission);
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test46_removeActionIdWithPermissionShouldFailIfActionIdsIsNotGreaterThanZero() {
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        // huser, with permission, tries to remove actionId with the following call
        // updatePermission, but actionIds is not greater than zero
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(permissionResourceName,
                HyperIoTCrudAction.UPDATE);
        HUser huser = createHUser(action);
        Permission permission = createPermission();

        HyperIoTAction removeFindAction = HyperIoTActionsUtil.getHyperIoTAction(permissionResourceName,
                HyperIoTCrudAction.FIND);

        permission.removePermission(removeFindAction);
        this.impersonateUser(permissionRestApi, huser);
        Response restResponse = permissionRestApi.updatePermission(permission);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("permission-actionids", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(String.valueOf(permission.getActionIds()), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }


    /*
     *
     *
     * UTILITY METHODS
     *
     *
     */

    private HUser createHUser(HyperIoTAction action) {
        HUserRestApi hUserRestApi = getOsgiService(HUserRestApi.class);
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        this.impersonateUser(hUserRestApi, adminUser);
        String username = "TestUser";
        List<Object> roles = new ArrayList<>();
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
        Assert.assertNotEquals(0, ((HUser) restResponse.getEntity()).getId());
        Assert.assertEquals("name", ((HUser) restResponse.getEntity()).getName());
        Assert.assertEquals("lastname", ((HUser) restResponse.getEntity()).getLastname());
        Assert.assertEquals(huser.getUsername(), ((HUser) restResponse.getEntity()).getUsername());
        Assert.assertEquals(huser.getEmail(), ((HUser) restResponse.getEntity()).getEmail());
        Assert.assertFalse(huser.isAdmin());
        Assert.assertTrue(huser.isActive());
        Assert.assertTrue(roles.isEmpty());
        if (action != null) {
            Role role = createRole();
            huser.addRole(role);
            RoleRestApi roleRestApi = getOsgiService(RoleRestApi.class);
            Response restUserRole = roleRestApi.saveUserRole(role.getId(), huser.getId());
            Assert.assertEquals(200, restUserRole.getStatus());
            Assert.assertTrue(huser.hasRole(role));
            roles = Arrays.asList(huser.getRoles().toArray());
            Assert.assertFalse(roles.isEmpty());
            utilGrantPermission(huser, role, action);
        }
        return huser;
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
                permission.setName(permissionResourceName + " assigned to huser_id " + huser.getId());
                permission.setActionIds(action.getActionId());
                permission.setEntityResourceName(action.getResourceName());
                permission.setRole(role);
                this.impersonateUser(permissionRestApi, adminUser);
                Response restResponse = permissionRestApi.savePermission(permission);
                testPermission = permission;
                Assert.assertEquals(200, restResponse.getStatus());
                Assert.assertNotEquals(0, ((Permission) restResponse.getEntity()).getId());
                Assert.assertEquals(testPermission.getName(), ((Permission) restResponse.getEntity()).getName());
                Assert.assertEquals(testPermission.getActionIds(), ((Permission) restResponse.getEntity()).getActionIds());
                Assert.assertEquals(testPermission.getEntityResourceName(), ((Permission) restResponse.getEntity()).getEntityResourceName());
                Assert.assertEquals(testPermission.getRole().getId(), ((Permission) restResponse.getEntity()).getRole().getId());
            } else {
                this.impersonateUser(permissionRestApi, adminUser);
                testPermission.addPermission(action);
                Response restResponseUpdate = permissionRestApi.updatePermission(testPermission);
                Assert.assertEquals(200, restResponseUpdate.getStatus());
                Assert.assertEquals(testPermission.getActionIds(), ((Permission) restResponseUpdate.getEntity()).getActionIds());
                Assert.assertEquals(testPermission.getEntityVersion() + 1,
                        ((Permission) restResponseUpdate.getEntity()).getEntityVersion());
            }
            Assert.assertTrue(huser.hasRole(role.getId()));
            return testPermission;
        }
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

    @After
    public void afterTest() {
        HUserSystemApi hUserSystemApi = getOsgiService(HUserSystemApi.class);
        RoleSystemApi roleSystemApi = getOsgiService(RoleSystemApi.class);
        PermissionSystemApi permissionSystemApi = getOsgiService(PermissionSystemApi.class);
        HyperIoTHUserTestUtils.truncateHUsers(hUserSystemApi);
        HyperIoTHUserTestUtils.truncateRoles(roleSystemApi);
        HyperIoTPermissionTestUtil.dropPermissions(roleSystemApi,permissionSystemApi);
    }

}
