package it.acsoftware.hyperiot.role.test;

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
import it.acsoftware.hyperiot.permission.api.PermissionSystemApi;
import it.acsoftware.hyperiot.permission.model.Permission;
import it.acsoftware.hyperiot.permission.service.rest.PermissionRestApi;
import it.acsoftware.hyperiot.role.actions.HyperIoTRoleAction;
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

import static it.acsoftware.hyperiot.role.test.HyperIoTRoleConfiguration.*;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HyperIoTRoleRestWithPermissionTest extends KarafTestSupport {

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
    public void test02_saveRoleWithPermissionShouldWork() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // HUser, with permission, save Role with the following call saveRole
        // response status code '200'
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(roleResourceName,
                HyperIoTCrudAction.SAVE);
        HUser huser = createHUser(action);
        Role role = new Role();
        role.setName("Role" + java.util.UUID.randomUUID());
        role.setDescription("Description");
        this.impersonateUser(roleRestService, huser);
        Response restResponse = roleRestService.saveRole(role);
        Assert.assertEquals(200, restResponse.getStatus());
    }

    @Test
    public void test03_saveRoleWithoutPermissionShouldFail() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // HUser, without permission, tries to save Role with the following call
        // saveRole
        // response status code '403' HyperIoTUnauthorizedException
        HUser huser = createHUser(null);
        this.impersonateUser(roleRestService, huser);
        Role role = new Role();
        role.setName("Role" + java.util.UUID.randomUUID());
        role.setDescription("Description");
        Response restResponse = roleRestService.saveRole(role);
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test04_updateRoleWithPermissionShouldWork() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // HUser, with permission, update Role with the following call updateRole
        // response status code '200'
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(roleResourceName,
                HyperIoTCrudAction.UPDATE);
        HUser huser = createHUser(action);
        Role role = createRole();
        role.setDescription("description edited");
        this.impersonateUser(roleRestService, huser);
        Response restResponse = roleRestService.updateRole(role);
        Assert.assertEquals(200, restResponse.getStatus());
    }

    @Test
    public void test05_updateRoleWithoutPermissionShouldFail() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // HUser, without permission, tries to update Role with the following call
        // updateRole
        // response status code '403' HyperIoTUnauthorizedException
        HUser huser = createHUser(null);
        Role role = createRole();
        role.setDescription("description edited");
        this.impersonateUser(roleRestService, huser);
        Response restResponse = roleRestService.updateRole(role);
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test06_findRoleWithPermissionShouldWork() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // HUser, with permission, find Role with the following call findRole
        // response status code '200'
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(roleResourceName,
                HyperIoTCrudAction.FIND);
        HUser huser = createHUser(action);
        Role role = createRole();
        this.impersonateUser(roleRestService, huser);
        Response restResponse = roleRestService.findRole(role.getId());
        Assert.assertEquals(200, restResponse.getStatus());
    }

    @Test
    public void test07_findRoleWithPermissionShouldFailIfEntityNotFound() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // HUser, with permission, tries to find Role with the following call findRole,
        // but entity not found
        // response status code '404' HyperIoTEntityNotFound
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(roleResourceName,
                HyperIoTCrudAction.FIND);
        HUser huser = createHUser(action);
        this.impersonateUser(roleRestService, huser);
        Response restResponse = roleRestService.findRole(0);
        Assert.assertEquals(404, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test08_findRoleWithoutPermissionShouldFail() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // HUser, without permission, tries to find Role with the following call
        // findRole
        // response status code '403' HyperIoTUnauthorizedException
        HUser huser = createHUser(null);
        Role role = createRole();
        this.impersonateUser(roleRestService, huser);
        Response restResponse = roleRestService.findRole(role.getId());
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test09_findAllRoleWithPermissionShouldWork() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // HUser, with permission, find all Role with the following call findAllRole
        // response status code '200'
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(roleResourceName,
                HyperIoTCrudAction.FINDALL);
        HUser huser = createHUser(action);
        Role role = createRole();
        this.impersonateUser(roleRestService, huser);
        Response restResponse = roleRestService.findAllRoles();
        List<Role> listRoles = restResponse.readEntity(new GenericType<List<Role>>() {
        });
        Assert.assertFalse(listRoles.isEmpty());
        boolean roleFound = false;
        for (Role roles : listRoles) {
            if (role.getId() == roles.getId())
                roleFound = true;
        }
        Assert.assertTrue(roleFound);
        Assert.assertEquals(200, restResponse.getStatus());
    }

    @Test
    public void test10_findAllRoleWithoutPermissionShouldFail() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // HUser, without permission, tries to find all Role with the following call
        // findAllRole
        // response status code '403' HyperIoTUnauthorizedException
        HUser huser = createHUser(null);
        this.impersonateUser(roleRestService, huser);
        Response restResponse = roleRestService.findAllRoles();
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test11_deleteRoleWithPermissionShouldWork() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // HUser, with permission, delete Role with the following call deleteRole
        // response status code '200'
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(roleResourceName,
                HyperIoTCrudAction.REMOVE);
        HUser huser = createHUser(action);
        Role role = createRole();
        this.impersonateUser(roleRestService, huser);
        Response restResponse = roleRestService.deleteRole(role.getId());
        Assert.assertEquals(200, restResponse.getStatus());
    }

    @Test
    public void test12_deleteRoleWithPermissionShouldFailIfEntityNotFound() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // HUser, with permission, tries to delete Role with the following call
        // deleteRole, but entity not found
        // response status code '404' HyperIoTEntityNotFound
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(roleResourceName,
                HyperIoTCrudAction.REMOVE);
        HUser huser = createHUser(action);
        this.impersonateUser(roleRestService, huser);
        Response restResponse = roleRestService.deleteRole(0);
        Assert.assertEquals(404, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test13_deleteRoleWithoutPermissionShouldFail() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // HUser, without permission, tries to delete Role with the following call
        // deleteRole
        // response status code '403' HyperIoTUnauthorizedException
        HUser huser = createHUser(null);
        Role role = createRole();
        this.impersonateUser(roleRestService, huser);
        Response restResponse = roleRestService.deleteRole(role.getId());
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test14_saveRoleWithPermissionShouldFailIfNameIsNull() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // HUser, with permission, tries to save Role with the following call
        // saveRole, but name is null
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(roleResourceName,
                HyperIoTCrudAction.SAVE);
        HUser huser = createHUser(action);
        Role role = new Role();
        role.setName(null);
        role.setDescription("Description");
        this.impersonateUser(roleRestService, huser);
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
    public void test15_saveRoleWithPermissionShouldFailIfNameIsEmpty() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // HUser, with permission, tries to save Role with the following call
        // saveRole, but name is empty
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(roleResourceName,
                HyperIoTCrudAction.SAVE);
        HUser huser = createHUser(action);
        Role role = new Role();
        role.setName("");
        role.setDescription("Description");
        this.impersonateUser(roleRestService, huser);
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
    public void test16_saveRoleWithPermissionShouldFailIfNameIsMaliciousCode() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // HUser, with permission, tries to save Role with the following call
        // saveRole, but name is malicious code
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(roleResourceName,
                HyperIoTCrudAction.SAVE);
        HUser huser = createHUser(action);
        Role role = new Role();
        role.setName("javascript:");
        role.setDescription("Description");
        this.impersonateUser(roleRestService, huser);
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
    public void test17_saveRoleWithPermissionShouldFailIfDescriptionIsNull() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // HUser, with permission, tries to save Role with the following call
        // saveRole, but description is null
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(roleResourceName,
                HyperIoTCrudAction.SAVE);
        HUser huser = createHUser(action);
        Role role = new Role();
        role.setName("Role" + java.util.UUID.randomUUID());
        role.setDescription(null);
        this.impersonateUser(roleRestService, huser);
        Response restResponse = roleRestService.saveRole(role);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("role-description", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
    }

    @Test
    public void test18_saveRoleWithPermissionShouldFailIfDescriptionIsMaliciousCode() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // HUser, with permission, tries to save Role with the following call
        // saveRole, but description is malicious code
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(roleResourceName,
                HyperIoTCrudAction.SAVE);
        HUser huser = createHUser(action);
        Role role = new Role();
        role.setName("Role" + java.util.UUID.randomUUID());
        role.setDescription("vbscript:");
        this.impersonateUser(roleRestService, huser);
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
    public void test19_saveRoleWithPermissionShouldFailIfMaxDescriptionIsOver3000Chars() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // HUser, with permission, tries to save Role with the following call
        // saveRole, but description is over 3000 chars
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(roleResourceName,
                HyperIoTCrudAction.SAVE);
        HUser huser = createHUser(action);
        Role role = new Role();
        role.setName("Role" + java.util.UUID.randomUUID());
        role.setDescription(testMaxDescription(3001));
        this.impersonateUser(roleRestService, huser);
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
    public void test20_updateRoleWithPermissionShouldFailIfNameIsNull() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // HUser, with permission, tries to update Role with the following call
        // updateRole, but name is null
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(roleResourceName,
                HyperIoTCrudAction.UPDATE);
        HUser huser = createHUser(action);
        Role role = createRole();
        role.setName(null);
        this.impersonateUser(roleRestService, huser);
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
    public void test21_updateRoleWithPermissionShouldFailIfNameIsEmpty() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // HUser, with permission, tries to update Role with the following call
        // updateRole, but name is empty
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(roleResourceName,
                HyperIoTCrudAction.UPDATE);
        HUser huser = createHUser(action);
        Role role = createRole();
        role.setName("");
        this.impersonateUser(roleRestService, huser);
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
    public void test22_updateRoleWithPermissionShouldFailIfNameIsMaliciousCode() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // HUser, with permission, tries to update Role with the following call
        // updateRole, but name is malicious code
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(roleResourceName,
                HyperIoTCrudAction.UPDATE);
        HUser huser = createHUser(action);
        Role role = createRole();
        role.setName("</script>");
        this.impersonateUser(roleRestService, huser);
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
    public void test23_updateRoleWithPermissionShouldFailIfDescriptionIsNull() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // HUser, with permission, tries to update Role with the following call
        // updateRole, but description is null
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(roleResourceName,
                HyperIoTCrudAction.UPDATE);
        HUser huser = createHUser(action);
        Role role = createRole();
        role.setDescription(null);
        this.impersonateUser(roleRestService, huser);
        Response restResponse = roleRestService.updateRole(role);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("role-description", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
    }

    @Test
    public void test24_updateRoleWithPermissionShouldFailIfDescriptionIsMaliciousCode() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // HUser, with permission, tries to update Role with the following call
        // updateRole, but description is malicious code
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(roleResourceName,
                HyperIoTCrudAction.UPDATE);
        HUser huser = createHUser(action);
        Role role = createRole();
        role.setDescription("vbscript:");
        this.impersonateUser(roleRestService, huser);
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
    public void test25_updateRoleWithPermissionShouldFailIfMaxDescriptionIsOver3000Chars() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // HUser, with permission, tries to update Role with the following call
        // updateRole, but description is over 3000 chars
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(roleResourceName,
                HyperIoTCrudAction.UPDATE);
        HUser huser = createHUser(action);
        Role role = createRole();
        role.setDescription(testMaxDescription(3001));
        this.impersonateUser(roleRestService, huser);
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
    public void test26_getUserRolesWithPermissionShouldWork() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // HUser, with permission, find all user Role with the following call
        // findAllUserRoles
        // response status code '200'
       HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(huserResourceName,
                HyperIoTCrudAction.FINDALL);
        HUser huser = createHUser(action);
        this.impersonateUser(roleRestService, huser);
        Response restResponse = roleRestService.findAllUserRoles(huser.getId());
        Set<Role> listRoles = restResponse.readEntity(new GenericType<Set<Role>>() {
        });
        Assert.assertFalse(listRoles.isEmpty());
        Assert.assertEquals(200, restResponse.getStatus());
    }

    @Test
    public void test27_getUserRolesWithPermissionShouldWorkIfUserNotHasRole() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // HUser, with permission, find all user Role with the following call
        // findAllUserRoles, huser2 not has Role and listRoles is empty.
        // response status code '200'
        HUser huser2 = createHUser(null);
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(huserResourceName,
                HyperIoTCrudAction.FINDALL);
        HUser huser = createHUser(action);
        this.impersonateUser(roleRestService, huser);
        Response restResponse = roleRestService.findAllUserRoles(huser2.getId());
        Set<Role> listRoles = restResponse.readEntity(new GenericType<Set<Role>>() {
        });
        Assert.assertTrue(listRoles.isEmpty());
        Assert.assertEquals(0, listRoles.size());
        Assert.assertEquals(200, restResponse.getStatus());
    }

    @Test
    public void test28_getUserRolesWithoutPermissionShouldFail() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // HUser, without permission, tries to find all user Role with the following
        // call findAllUserRoles
        // response status code '403' HyperIoTUnauthorizedException
        HUser huser = createHUser(null);
        this.impersonateUser(roleRestService, huser);
        Response restResponse = roleRestService.findAllUserRoles(huser.getId());
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test29_saveUserRoleWithPermissionShouldWork() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // HUser, with permission, save user Role with the following call saveUserRole
        // response status code '200'
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(roleResourceName,
                HyperIoTRoleAction.ASSIGN_MEMBERS);
        HUser huser = createHUser(action);
        Role role = createRole();
        huser.addRole(role);
        this.impersonateUser(roleRestService, huser);
        Response restResponse = roleRestService.saveUserRole(role.getId(), huser.getId());
        Assert.assertEquals(200, restResponse.getStatus());
    }

    @Test
    public void test30_saveUserRoleWithoutPermissionShouldFail() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // HUser, without permission, tries to save user Role with the following call
        // saveUserRole
        // response status code '403' HyperIoTUnauthorizedException
        HUser huser = createHUser(null);
        Role role = createRole();
        huser.addRole(role);
        this.impersonateUser(roleRestService, huser);
        Response restResponse = roleRestService.saveUserRole(role.getId(), huser.getId());
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test31_saveUserRoleWithPermissionShouldFailIfRoleNotFound() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // HUser, with permission, tries to save user Role with the following call
        // saveUserRole, but Role not found
        // response status code '404' HyperIoTEntityNotFound
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(roleResourceName,
                HyperIoTRoleAction.ASSIGN_MEMBERS);
        HUser huser = createHUser(action);
        Role role = createRole();
        huser.addRole(role);
        this.impersonateUser(roleRestService, huser);
        Response restResponse = roleRestService.saveUserRole(0, huser.getId());
        Assert.assertEquals(404, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test32_saveUserRoleWithPermissionShouldFailIfUserNotFound() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // HUser, with permission, tries to save user Role with the following call
        // saveUserRole, but HUser not found
        // response status code '404' HyperIoTEntityNotFound
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(roleResourceName,
                HyperIoTRoleAction.ASSIGN_MEMBERS);
        HUser huser = createHUser(action);
        Role role = createRole();
        huser.addRole(role);
        this.impersonateUser(roleRestService, huser);
        Response restResponse = roleRestService.saveUserRole(role.getId(), 0);
        Assert.assertEquals(404, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }


    @Test
    public void test33_deleteUserRoleWithPermissionShouldWork() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // HUser, with permission, delete user Role with the following call
        // deleteUserRole
        // response status code '200'
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(roleResourceName,
                HyperIoTRoleAction.REMOVE_MEMBERS);
        HUser huser = createHUser(action);
        Role role = createUserRole(huser);
        this.impersonateUser(roleRestService, huser);
        Response restResponse = roleRestService.deleteUserRole(role.getId(), huser.getId());
        Assert.assertEquals(200, restResponse.getStatus());
    }

    @Test
    public void test34_deleteUserRoleWithoutPermissionShouldFail() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // HUser, without permission, tries to delete user Role with the following call
        // deleteUserRole
        // response status code '403' HyperIoTUnauthorizedException
        HUser huser = createHUser(null);
        Role role = createUserRole(huser);
        this.impersonateUser(roleRestService, huser);
        Response restResponse = roleRestService.deleteUserRole(role.getId(), huser.getId());
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test35_deleteUserRoleWithPermissionShouldFailIfRoleNotFound() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // HUser, with permission, tries to delete user Role with the following call
        // deleteUserRole, but Role not found
        // response status code '404' HyperIoTEntityNotFound
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(roleResourceName,
                HyperIoTRoleAction.REMOVE_MEMBERS);
        HUser huser = createHUser(action);
        createUserRole(huser);
        this.impersonateUser(roleRestService, huser);
        Response restResponse = roleRestService.deleteUserRole(0, huser.getId());
        Assert.assertEquals(404, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test36_deleteUserRoleWithPermissionShouldFailIfUserNotFound() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // HUser, with permission, tries to delete user Role with the following call
        // deleteUserRole, but HUser not found
        // response status code '404' HyperIoTEntityNotFound
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(roleResourceName,
                HyperIoTRoleAction.REMOVE_MEMBERS);
        HUser huser = createHUser(action);
        Role role = createUserRole(huser);
        this.impersonateUser(roleRestService, huser);
        Response restResponse = roleRestService.deleteUserRole(role.getId(), 0);
        Assert.assertEquals(404, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test37_saveRoleWithPermissionShouldFailIfEntityIsDuplicated() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // HUser, with permission, tries to save Role with the following call saveRole,
        // but entity is duplicated
        // response status code '422' HyperIoTDuplicateEntityException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(roleResourceName,
                HyperIoTCrudAction.SAVE);
        HUser huser = createHUser(action);
        Role role = createRole();
        Role duplicateRole = new Role();
        duplicateRole.setName(role.getName());
        duplicateRole.setDescription("Description");
        this.impersonateUser(roleRestService, huser);
        Response restResponse = roleRestService.saveRole(duplicateRole);
        Assert.assertEquals(409, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTDuplicateEntityException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().size());
        boolean nameIsDuplicated = false;
        for (int i = 0; i < ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().size(); i++) {
            if (((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(i).contentEquals("name")) {
                nameIsDuplicated = true;
                Assert.assertEquals("name",
                        ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(i));
            }
        }
        Assert.assertTrue(nameIsDuplicated);
    }

    @Test
    public void test38_saveRoleWithoutPermissionShouldFailAndEntityIsDuplicated() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // HUser, without permission, tries to save Role duplicated with the following
        // call saveRole
        // response status code '403' HyperIoTUnauthorizedException
        HUser huser = createHUser(null);
        Role role = createRole();
        Role duplicateRole = new Role();
        duplicateRole.setName(role.getName());
        duplicateRole.setDescription("Description");
        this.impersonateUser(roleRestService, huser);
        Response restResponse = roleRestService.saveRole(duplicateRole);
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test39_updateRoleWithPermissionShouldFailIfEntityNotFound() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // HUser, with permission, tries to update Role with the following call
        // updateRole, but entity not found
        // response status code '404' HyperIoTEntityNotFound
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(roleResourceName,
                HyperIoTCrudAction.UPDATE);
        HUser huser = createHUser(action);
        Role role = new Role();
        role.setDescription("description edited");
        this.impersonateUser(roleRestService, huser);
        Response restResponse = roleRestService.updateRole(role);
        Assert.assertEquals(404, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test40_updateRoleWithoutPermissionShouldFailAndEntityNotFound() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // HUser, without permission, tries to update Role not found with the following
        // call updateRole
        // response status code '403' HyperIoTUnauthorizedException
        HUser huser = createHUser(null);
        Role role = new Role();
        role.setDescription("description edited");
        this.impersonateUser(roleRestService, huser);
        Response restResponse = roleRestService.updateRole(role);
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test41_updateRoleWithPermissionShouldFailIfEntityIsDuplicated() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // HUser, with permission, tries to update Role with the following call updateRole,
        // but entity is duplicated
        // response status code '422' HyperIoTDuplicateEntityException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(roleResourceName,
                HyperIoTCrudAction.UPDATE);
        HUser huser = createHUser(action);
        Role role = createRole();
        Role duplicateRole = createRole();
        duplicateRole.setName(role.getName());
        this.impersonateUser(roleRestService, huser);
        Response restResponse = roleRestService.updateRole(duplicateRole);
        Assert.assertEquals(409, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTDuplicateEntityException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().size());
        boolean nameIsDuplicated = false;
        for (int i = 0; i < ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().size(); i++) {
            if (((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(i).contentEquals("name")) {
                nameIsDuplicated = true;
                Assert.assertEquals("name",
                        ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(i));
            }
        }
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
    public void test42_getUserRolesShouldFailIfDeleteUserAfterCallSaveUserRole() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // huser with permission FIND, tries to finds all user roles
        // if huser has been deleted after call saveUserRole
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");

        //Create HUser, save UserRole and assign FIND permission
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(huserResourceName,
                HyperIoTCrudAction.FIND);
        HUser huserWithPermission = createHUser(action);
        HyperIoTAction action1 = HyperIoTActionsUtil.getHyperIoTAction(huserResourceName,
                HyperIoTCrudAction.FINDALL);
        addPermission(huserWithPermission, action1);

        HUser huserWithRole = createHUser();
        Role role = createUserRole(huserWithRole);
        this.impersonateUser(roleRestService, huserWithPermission);
        Response restResponse = roleRestService.findAllUserRoles(huserWithRole.getId());
        Set<Role> listRoles = restResponse.readEntity(new GenericType<Set<Role>>() {
        });
        Assert.assertFalse(listRoles.isEmpty());
        Assert.assertTrue(huserWithRole.hasRole(role.getId()));
        Assert.assertEquals(200, restResponse.getStatus());

        //hadmin deletes huser with call deleteHUser, not will deletes Role
        HUserRestApi hUserRestApi = getOsgiService(HUserRestApi.class);
        this.impersonateUser(hUserRestApi, adminUser);
        Response restRemoveHUser = hUserRestApi.deleteHUser(huserWithRole.getId());
        Assert.assertEquals(200, restRemoveHUser.getStatus());

        // role is still stored in database
        HyperIoTAction action2 = HyperIoTActionsUtil.getHyperIoTAction(roleResourceName,
                HyperIoTCrudAction.FIND);
        addPermission(huserWithPermission, action2);
        this.impersonateUser(roleRestService, huserWithPermission);
        Response restResponse1 = roleRestService.findRole(role.getId());
        Assert.assertEquals(200, restResponse1.getStatus());
    }


    @Test
    public void test43_tryToFindRoleWithPermissionIfRoleHasBeenDeletedInCascadeModeShouldFail() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // firstHUser deletes role in cascade mode, with call deleteRole,
        // the call deleteRole also deletes the permission entity and
        // secondHUser will lose FIND permission
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");

        HyperIoTAction firstAction = HyperIoTActionsUtil.getHyperIoTAction(roleResourceName,
                HyperIoTCrudAction.REMOVE);
        HUser firstHUser = createHUser(firstAction);

        // secondHUser has FIND permission
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(roleResourceName,
                HyperIoTCrudAction.FIND);
        HUser secondHUser = createHUser();
        Role role = createUserRole(secondHUser);
        Permission permission = utilGrantPermission(secondHUser, role, action);
        // Test secondHUser permission
        this.impersonateUser(roleRestService, secondHUser);
        Response restResponse = roleRestService.findRole(role.getId());
        Assert.assertEquals(200, restResponse.getStatus());

        // firstHUser delete role with call deleteRole, role has been removed in cascade mode
        // this call also deletes the permission entity
        // with this call secondHUser will lose FIND permission
        this.impersonateUser(roleRestService, firstHUser);
        Response restRemoveRole = roleRestService.deleteRole(role.getId());
        Assert.assertEquals(200, restRemoveRole.getStatus());

        // hadmin try to delete User Role, this call fail because
        // Role has been removed with call deleteRole by firstHUser
        this.impersonateUser(roleRestService, adminUser);
        restRemoveRole = roleRestService.deleteUserRole(role.getId(), secondHUser.getId());
        Assert.assertEquals(404, restRemoveRole.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
                ((HyperIoTBaseError) restRemoveRole.getEntity()).getType());

        // permission not found, permission has been removed with
        // call deleteRole by firstHUser
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        this.impersonateUser(permissionRestApi, adminUser);
        Response restResponsePermission = permissionRestApi.findPermission(permission.getId());
        Assert.assertEquals(404, restResponsePermission.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
                ((HyperIoTBaseError) restResponsePermission.getEntity()).getType());

        // this call fail because secondHUser not has user role,
        // Role has been deleted with call deleteRole by firstHUser
        this.impersonateUser(roleRestService, secondHUser);
        Response restResponse1 = roleRestService.findRole(role.getId());
        Assert.assertEquals(404, restResponse1.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
                ((HyperIoTBaseError) restResponse1.getEntity()).getType());
    }


    @Test
    public void test44_tryToFindRoleWithPermissionIfRoleHasBeenDeletedShouldFail() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // firstHUser deletes user role with call deleteUserRole,
        // this call does not deletes the permission entity
        // with deleteUserRole call huser will lose FIND permission
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");

        HyperIoTAction firstAction = HyperIoTActionsUtil.getHyperIoTAction(roleResourceName,
                HyperIoTRoleAction.REMOVE_MEMBERS);
        HUser firstHUser = createHUser(firstAction);

        // secondHUser has FIND permission
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(roleResourceName,
                HyperIoTCrudAction.FIND);
        HUser secondHUser = createHUser();
        Role role = createUserRole(secondHUser);
        Permission permission = utilGrantPermission(secondHUser, role, action);
        // Test secondHUser permission
        this.impersonateUser(roleRestService, secondHUser);
        Response restResponse = roleRestService.findRole(role.getId());
        Assert.assertEquals(200, restResponse.getStatus());

        // delete user role with call deleteUserRole,
        // this call does not deletes the permission entity
        this.impersonateUser(roleRestService, firstHUser);
        Response restRemoveRole = roleRestService.deleteUserRole(role.getId(), secondHUser.getId());
        Assert.assertEquals(200, restRemoveRole.getStatus());

        // permission is still stored in database
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        this.impersonateUser(permissionRestApi, adminUser);
        Response restResponsePermission = permissionRestApi.findPermission(permission.getId());
        Assert.assertEquals(200, restResponsePermission.getStatus());

        //fail because secondHUser not has user role, deleted with call deleteUserRole
        this.impersonateUser(roleRestService, secondHUser);
        restResponse = roleRestService.findRole(role.getId());
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }


    @Test
    public void test45_deleteUserRoleNotDeletePermissionOrHUserEntityInCascadeMode() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // firstHUser deletes user role with call deleteUserRole,
        // this call does not deletes the permission or huser entity
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");

        HyperIoTAction firstAction = HyperIoTActionsUtil.getHyperIoTAction(roleResourceName,
                HyperIoTRoleAction.REMOVE_MEMBERS);
        HUser firstHUser = createHUser(firstAction);

        // secondHUser has FIND permission
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(roleResourceName,
                HyperIoTCrudAction.FIND);
        HUser secondHUser = createHUser();
        Role role = createUserRole(secondHUser);
        Permission permission = utilGrantPermission(secondHUser, role, action);
        // Test secondHUser permission
        this.impersonateUser(roleRestService, secondHUser);
        Response restResponse = roleRestService.findRole(role.getId());
        Assert.assertEquals(200, restResponse.getStatus());

        // delete user role with call deleteUserRole,
        // this call does not deletes the permission entity
        this.impersonateUser(roleRestService, firstHUser);
        Response restRemoveRole = roleRestService.deleteUserRole(role.getId(), secondHUser.getId());
        Assert.assertEquals(200, restRemoveRole.getStatus());

        // permission is still stored in database
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        this.impersonateUser(permissionRestApi, adminUser);
        Response restResponsePermission = permissionRestApi.findPermission(permission.getId());
        Assert.assertEquals(200, restResponsePermission.getStatus());

        // secondHUser is still stored in database
        HUserRestApi hUserRestApi = getOsgiService(HUserRestApi.class);
        this.impersonateUser(hUserRestApi, adminUser);
        Response restResponseHUser = hUserRestApi.findHUser(secondHUser.getId());
        Assert.assertEquals(200, restResponseHUser.getStatus());
    }


    @Test
    public void test46_deleteRoleInCascadeModeNotDeleteHUserButDeletePermissionEntity() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // firstHUser deletes user role with call deleteRole,
        // this call deletes the permission entity
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");

        HyperIoTAction firstAction = HyperIoTActionsUtil.getHyperIoTAction(roleResourceName,
                HyperIoTCrudAction.REMOVE);
        HUser firstHUser = createHUser(firstAction);

        // secondHUser has FIND permission
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(roleResourceName,
                HyperIoTCrudAction.FIND);
        HUser secondHUser = createHUser();
        Role role = createUserRole(secondHUser);
        Permission permission = utilGrantPermission(secondHUser, role, action);
        // Test secondHUser permission
        this.impersonateUser(roleRestService, secondHUser);
        Response restResponse = roleRestService.findRole(role.getId());
        Assert.assertEquals(200, restResponse.getStatus());

        // delete user role with call deleteUserRole,
        // this call does not deletes the permission entity
        this.impersonateUser(roleRestService, firstHUser);
        Response restRemoveRole = roleRestService.deleteRole(role.getId());
        Assert.assertEquals(200, restRemoveRole.getStatus());

        // permission not found, permission has been removed with call deleteRole
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        this.impersonateUser(permissionRestApi, adminUser);
        Response restResponsePermission = permissionRestApi.findPermission(permission.getId());
        Assert.assertEquals(404, restResponsePermission.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
                ((HyperIoTBaseError) restResponsePermission.getEntity()).getType());

        // secondHUser is still stored in database
        HUserRestApi hUserRestApi = getOsgiService(HUserRestApi.class);
        this.impersonateUser(hUserRestApi, adminUser);
        Response restResponseHUser = hUserRestApi.findHUser(secondHUser.getId());
        Assert.assertEquals(200, restResponseHUser.getStatus());
    }


    @Test
    public void test47_findAllRolesPaginationWithPermissionShouldWork() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // In this following call findAllRolesPaginated HUser, with permission,
        // find all Roles with pagination
        // response status code '200'
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(roleResourceName,
                HyperIoTCrudAction.FINDALL);
        HUser huser = createHUser(action);
        int delta = 5;
        int page = 1;
        List<Role> roles = new ArrayList<>();
        int numbEntities = 7; // +1 Role associated with huser
        for (int i = 0; i < numbEntities; i++) {
            Role role = createRole();
            Assert.assertNotEquals(0, role.getId());
            roles.add(role);
        }
        Assert.assertEquals(numbEntities, roles.size());
        this.impersonateUser(roleRestService, huser);
        Response restResponse = roleRestService.findAllRolesPaginated(delta, page);
        HyperIoTPaginableResult<Role> listRoles = restResponse
                .readEntity(new GenericType<HyperIoTPaginableResult<Role>>() {
                });
        Assert.assertFalse(listRoles.getResults().isEmpty());
        Assert.assertEquals(delta, listRoles.getResults().size());
        Assert.assertEquals(delta, listRoles.getDelta());
        Assert.assertEquals(page, listRoles.getCurrentPage());
        Assert.assertEquals(page + 1, listRoles.getNextPage());
        // delta is 5, page 2: 8 entities stored in database
        Assert.assertEquals(2, listRoles.getNumPages());
        Assert.assertEquals(200, restResponse.getStatus());

        //checks with page = 2
        this.impersonateUser(roleRestService, huser);
        Response restResponsePage2 = roleRestService.findAllRolesPaginated(delta, 2);
        HyperIoTPaginableResult<Role> listRolesPage2 = restResponsePage2
                .readEntity(new GenericType<HyperIoTPaginableResult<Role>>() {
                });
        Assert.assertFalse(listRolesPage2.getResults().isEmpty());
        Assert.assertEquals(4, listRolesPage2.getResults().size());
        Assert.assertEquals(delta, listRolesPage2.getDelta());
        Assert.assertEquals(page + 1, listRolesPage2.getCurrentPage());
        Assert.assertEquals(page, listRolesPage2.getNextPage());
        // delta is 5, page 2: 8 entities stored in database
        Assert.assertEquals(2, listRolesPage2.getNumPages());
        Assert.assertEquals(200, restResponsePage2.getStatus());
    }


    @Test
    public void test48_findAllRolesPaginationWithPermissionShouldWorkIfDeltaAndPageAreNull() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // In this following call findAllRolesPaginated HUser, with permission,
        // find all Roles with pagination if delta and page are null
        // response status code '200'
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(roleResourceName,
                HyperIoTCrudAction.FINDALL);
        HUser huser = createHUser(action);
        Integer delta = null;
        Integer page = null;
        List<Role> roles = new ArrayList<>();
        int numbEntities = 8; // +1 Role associated with huser
        for (int i = 0; i < numbEntities; i++) {
            Role role = createRole();
            Assert.assertNotEquals(0, role.getId());
            roles.add(role);
        }
        Assert.assertEquals(numbEntities, roles.size());
        this.impersonateUser(roleRestService, huser);
        Response restResponse = roleRestService.findAllRolesPaginated(delta, page);
        HyperIoTPaginableResult<Role> listRoles = restResponse
                .readEntity(new GenericType<HyperIoTPaginableResult<Role>>() {
                });
        Assert.assertFalse(listRoles.getResults().isEmpty());
        Assert.assertEquals(numbEntities + 2, listRoles.getResults().size());
        Assert.assertEquals(HyperIoTRoleConfiguration.defaultDelta, listRoles.getDelta());
        Assert.assertEquals(HyperIoTRoleConfiguration.defaultPage, listRoles.getCurrentPage());
        Assert.assertEquals(HyperIoTRoleConfiguration.defaultPage, listRoles.getNextPage());
        // default delta is 10, default page is 1: 9 entities stored in database
        Assert.assertEquals(1, listRoles.getNumPages());
        Assert.assertEquals(200, restResponse.getStatus());
    }


    @Test
    public void test49_findAllRolesPaginationWithPermissionShouldWorkIfDeltaIsLowerThanZero() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // In this following call findAllRolesPaginated HUser, with permission,
        // find all Roles with pagination if delta is lower than zero
        // response status code '200'
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(roleResourceName,
                HyperIoTCrudAction.FINDALL);
        HUser huser = createHUser(action);
        int delta = -1;
        int page = 2;
        List<Role> roles = new ArrayList<>();
        int numbEntities = 16; // +1 Role associated with huser
        for (int i = 0; i < numbEntities; i++) {
            Role role = createRole();
            Assert.assertNotEquals(0, role.getId());
            roles.add(role);
        }
        Assert.assertEquals(numbEntities, roles.size());
        this.impersonateUser(roleRestService, huser);
        Response restResponse = roleRestService.findAllRolesPaginated(delta, page);
        HyperIoTPaginableResult<Role> listRoles = restResponse
                .readEntity(new GenericType<HyperIoTPaginableResult<Role>>() {
                });
        Assert.assertFalse(listRoles.getResults().isEmpty());
        Assert.assertEquals(8, listRoles.getResults().size());
        Assert.assertEquals(HyperIoTRoleConfiguration.defaultDelta, listRoles.getDelta());
        Assert.assertEquals(page, listRoles.getCurrentPage());
        Assert.assertEquals(HyperIoTRoleConfiguration.defaultPage, listRoles.getNextPage());
        // default delta is 10, page is 2: 17 entities stored in database
        Assert.assertEquals(2, listRoles.getNumPages());
        Assert.assertEquals(200, restResponse.getStatus());

        //checks with page = 1
        this.impersonateUser(roleRestService, huser);
        Response restResponsePage1 = roleRestService.findAllRolesPaginated(delta, 1);
        HyperIoTPaginableResult<Role> listRolesPage1 = restResponsePage1
                .readEntity(new GenericType<HyperIoTPaginableResult<Role>>() {
                });
        Assert.assertFalse(listRolesPage1.getResults().isEmpty());
        Assert.assertEquals(HyperIoTRoleConfiguration.defaultDelta, listRolesPage1.getResults().size());
        Assert.assertEquals(HyperIoTRoleConfiguration.defaultDelta, listRolesPage1.getDelta());
        Assert.assertEquals(HyperIoTRoleConfiguration.defaultPage, listRolesPage1.getCurrentPage());
        Assert.assertEquals(page, listRolesPage1.getNextPage());
        // default delta is 10, page is 1: 17 entities stored in database
        Assert.assertEquals(2, listRolesPage1.getNumPages());
        Assert.assertEquals(200, restResponsePage1.getStatus());
    }


    @Test
    public void test50_findAllRolesPaginationWithPermissionShouldWorkIfDeltaIsZero() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // In this following call findAllRolesPaginated HUser, with permission,
        // find all Roles  with pagination if delta is zero
        // response status code '200'
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(roleResourceName,
                HyperIoTCrudAction.FINDALL);
        HUser huser = createHUser(action);
        int delta = 0;
        int page = 2;
        List<Role> roles = new ArrayList<>();
        int numEntities = 11; // +1 Role associated with huser
        for (int i = 0; i < numEntities; i++) {
            Role role = createRole();
            Assert.assertNotEquals(0, role.getId());
            roles.add(role);
        }
        Assert.assertEquals(numEntities, roles.size());
        this.impersonateUser(roleRestService, huser);
        Response restResponse = roleRestService.findAllRolesPaginated(delta, page);
        HyperIoTPaginableResult<Role> listRoles = restResponse
                .readEntity(new GenericType<HyperIoTPaginableResult<Role>>() {
                });
        Assert.assertFalse(listRoles.getResults().isEmpty());
        Assert.assertEquals(3, listRoles.getResults().size());
        Assert.assertEquals(HyperIoTRoleConfiguration.defaultDelta, listRoles.getDelta());
        Assert.assertEquals(page, listRoles.getCurrentPage());
        Assert.assertEquals(HyperIoTRoleConfiguration.defaultPage, listRoles.getNextPage());
        // because delta is 10, page is 2: 12 entities stored in database
        Assert.assertEquals(2, listRoles.getNumPages());
        Assert.assertEquals(200, restResponse.getStatus());

        //checks with page = 1
        this.impersonateUser(roleRestService, huser);
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
    public void test51_findAllRolesPaginationWithPermissionShouldWorkIfPageIsLowerThanZero() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // In this following call findAllRolesPaginated HUser, with permission,
        // find all Roles with pagination if page is lower than zero
        // response status code '200'
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(roleResourceName,
                HyperIoTCrudAction.FINDALL);
        HUser huser = createHUser(action);
        int delta = 8;
        int page = -1;
        List<Role> roles = new ArrayList<>();
        int numEntities = 16; // +1 Role associated with huser
        for (int i = 0; i < numEntities; i++) {
            Role role = createRole();
            Assert.assertNotEquals(0, role.getId());
            roles.add(role);
        }
        Assert.assertEquals(numEntities, roles.size());
        this.impersonateUser(roleRestService, huser);
        Response restResponse = roleRestService.findAllRolesPaginated(delta, page);
        HyperIoTPaginableResult<Role> listRoles = restResponse
                .readEntity(new GenericType<HyperIoTPaginableResult<Role>>() {
                });
        Assert.assertFalse(listRoles.getResults().isEmpty());
        Assert.assertEquals(delta, listRoles.getResults().size());
        Assert.assertEquals(delta, listRoles.getDelta());
        Assert.assertEquals(HyperIoTRoleConfiguration.defaultPage, listRoles.getCurrentPage());
        Assert.assertEquals(HyperIoTRoleConfiguration.defaultPage + 1, listRoles.getNextPage());
        // delta is 8, default page 1: 17 entities stored in database
        Assert.assertEquals(3, listRoles.getNumPages());
        Assert.assertEquals(200, restResponse.getStatus());

        //checks with page = 2
        this.impersonateUser(roleRestService, huser);
        Response restResponsePage2 = roleRestService.findAllRolesPaginated(delta, 2);
        HyperIoTPaginableResult<Role> listRolesPage2 = restResponsePage2
                .readEntity(new GenericType<HyperIoTPaginableResult<Role>>() {
                });
        Assert.assertFalse(listRolesPage2.getResults().isEmpty());
        Assert.assertEquals(delta, listRolesPage2.getResults().size());
        Assert.assertEquals(delta, listRolesPage2.getDelta());
        Assert.assertEquals(HyperIoTRoleConfiguration.defaultPage + 1, listRolesPage2.getCurrentPage());
        Assert.assertEquals(HyperIoTRoleConfiguration.defaultPage + 2, listRolesPage2.getNextPage());
        // delta is 8, page 2: 17 entities stored in database
        Assert.assertEquals(3, listRolesPage2.getNumPages());
        Assert.assertEquals(200, restResponsePage2.getStatus());

        //checks with page = 3
        this.impersonateUser(roleRestService, huser);
        Response restResponsePage3 = roleRestService.findAllRolesPaginated(delta, 3);
        HyperIoTPaginableResult<Role> listRolesPage3 = restResponsePage3
                .readEntity(new GenericType<HyperIoTPaginableResult<Role>>() {
                });
        Assert.assertFalse(listRolesPage3.getResults().isEmpty());
        Assert.assertEquals(2, listRolesPage3.getResults().size());
        Assert.assertEquals(delta, listRolesPage3.getDelta());
        Assert.assertEquals(HyperIoTRoleConfiguration.defaultPage + 2, listRolesPage3.getCurrentPage());
        Assert.assertEquals(HyperIoTRoleConfiguration.defaultPage, listRolesPage3.getNextPage());
        // delta is 8, page 3: 17 entities stored in database
        Assert.assertEquals(3, listRolesPage3.getNumPages());
        Assert.assertEquals(200, restResponsePage3.getStatus());
    }


    @Test
    public void test52_findAllRolesPaginationWithPermissionShouldWorkIfPageIsZero() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // In this following call findAllRolesPaginated HUser, with permission,
        // find all Roles with pagination if page is zero
        // response status code '200'
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(roleResourceName,
                HyperIoTCrudAction.FINDALL);
        HUser huser = createHUser(action);
        int delta = 7;
        int page = 0;
        int numbEntites = 12; // +1 Role associated with huser
        List<Role> roles = new ArrayList<>();
        for (int i = 0; i < numbEntites; i++) {
            Role role = createRole();
            Assert.assertNotEquals(0, role.getId());
            roles.add(role);
        }
        Assert.assertEquals(numbEntites, roles.size());
        this.impersonateUser(roleRestService, huser);
        Response restResponse = roleRestService.findAllRolesPaginated(delta, 0);
        HyperIoTPaginableResult<Role> listRoles = restResponse
                .readEntity(new GenericType<HyperIoTPaginableResult<Role>>() {
                });
        Assert.assertFalse(listRoles.getResults().isEmpty());
        Assert.assertEquals(delta, listRoles.getResults().size());
        Assert.assertEquals(delta, listRoles.getDelta());
        Assert.assertEquals(HyperIoTRoleConfiguration.defaultPage, listRoles.getCurrentPage());
        Assert.assertEquals(HyperIoTRoleConfiguration.defaultPage + 1, listRoles.getNextPage());
        // delta is 7, default page is 1: 13 entities stored in database
        Assert.assertEquals(2, listRoles.getNumPages());
        Assert.assertEquals(200, restResponse.getStatus());

        //checks with page = 2
        this.impersonateUser(roleRestService, huser);
        Response restResponsePage2 = roleRestService.findAllRolesPaginated(delta, 2);
        HyperIoTPaginableResult<Role> listRolesPage2 = restResponsePage2
                .readEntity(new GenericType<HyperIoTPaginableResult<Role>>() {
                });
        Assert.assertFalse(listRolesPage2.getResults().isEmpty());
        Assert.assertEquals(7, listRolesPage2.getResults().size());
        Assert.assertEquals(delta, listRolesPage2.getDelta());
        Assert.assertEquals(HyperIoTRoleConfiguration.defaultPage + 1, listRolesPage2.getCurrentPage());
        Assert.assertEquals(HyperIoTRoleConfiguration.defaultPage, listRolesPage2.getNextPage());
        // delta is 7, page is 2: 13 entities stored in database
        Assert.assertEquals(2, listRolesPage2.getNumPages());
        Assert.assertEquals(200, restResponsePage2.getStatus());
    }


    @Test
    public void test53_findAllRolePaginationWithoutPermissionShouldFail() {
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        // In this following call findAllRolesPaginated HUser, without permission, tries to find
        // all Roles with pagination
        // response status code '403' HyperIoTUnauthorizedException
        HUser huser = createHUser(null);
        this.impersonateUser(roleRestService, huser);
        Response restResponse = roleRestService.findAllRolesPaginated(null, null);
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

    private HUser createHUser(HyperIoTAction action) {
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
        Response restResponse = hUserRestApi.saveHUser(huser);
        Assert.assertEquals(200, restResponse.getStatus());
        if (action != null) {
            Role role = createRole();
            huser.addRole(role);
            RoleRestApi roleRestApi = getOsgiService(RoleRestApi.class);
            this.impersonateUser(roleRestApi, adminUser);
            Response restUserRole = roleRestApi.saveUserRole(role.getId(), huser.getId());
            Assert.assertEquals(200, restUserRole.getStatus());
            Assert.assertTrue(huser.hasRole(role));
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

    private String testMaxDescription(int lengthDescription) {
        String symbol = "a";
        String description = String.format("%" + lengthDescription + "s", " ").replaceAll(" ", symbol);
        Assert.assertEquals(lengthDescription, description.length());
        return description;
    }

    private Permission addPermission(HUser huser, HyperIoTAction action){
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Role role = createRole();
        huser.addRole(role);
        RoleRestApi roleRestApi = getOsgiService(RoleRestApi.class);
        this.impersonateUser(roleRestApi, adminUser);
        Response restUserRole = roleRestApi.saveUserRole(role.getId(), huser.getId());
        Assert.assertEquals(200, restUserRole.getStatus());
        Assert.assertTrue(huser.hasRole(role));
        Permission permission = utilGrantPermission(huser, role, action);
        return permission;
    }


    @After
    public void afterTest() {
        HUserSystemApi hUserSystemApi = getOsgiService(HUserSystemApi.class);
        RoleSystemApi roleSystemApi = getOsgiService(RoleSystemApi.class);
        HyperIoTHUserTestUtils.truncateHUsers(hUserSystemApi);
        HyperIoTHUserTestUtils.truncateRoles(roleSystemApi);
    }


}
