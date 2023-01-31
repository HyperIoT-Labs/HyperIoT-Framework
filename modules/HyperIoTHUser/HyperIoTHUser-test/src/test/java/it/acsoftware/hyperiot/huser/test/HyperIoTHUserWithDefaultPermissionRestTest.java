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

package it.acsoftware.hyperiot.huser.test;

import it.acsoftware.hyperiot.authentication.service.rest.AuthenticationRestApi;
import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.base.api.HyperIoTUser;
import it.acsoftware.hyperiot.base.api.authentication.AuthenticationApi;
import it.acsoftware.hyperiot.base.model.HyperIoTBaseError;
import it.acsoftware.hyperiot.base.service.rest.HyperIoTBaseRestApi;
import it.acsoftware.hyperiot.base.test.HyperIoTTestConfigurationBuilder;
import it.acsoftware.hyperiot.base.test.util.HyperIoTTestUtils;
import it.acsoftware.hyperiot.base.util.HyperIoTUtil;
import it.acsoftware.hyperiot.huser.api.HUserRepository;
import it.acsoftware.hyperiot.huser.api.HUserSystemApi;
import it.acsoftware.hyperiot.huser.model.HUser;
import it.acsoftware.hyperiot.huser.service.rest.HUserRestApi;
import it.acsoftware.hyperiot.huser.test.util.HyperIoTHUserTestUtils;
import it.acsoftware.hyperiot.permission.api.PermissionSystemApi;
import it.acsoftware.hyperiot.permission.model.Permission;
import it.acsoftware.hyperiot.role.api.RoleRepository;
import it.acsoftware.hyperiot.role.api.RoleSystemApi;
import it.acsoftware.hyperiot.role.model.Role;
import it.acsoftware.hyperiot.role.service.rest.RoleRestApi;
import it.acsoftware.hyperiot.role.util.HyperIoTRoleConstants;
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

import static it.acsoftware.hyperiot.huser.test.HyperIoTHUserConfiguration.hyperIoTException;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HyperIoTHUserWithDefaultPermissionRestTest extends KarafTestSupport {
    public static final String CODE_COVERAGE_PACKAGE_FILTER = "it.acsoftware.hyperiot.huser.*";

    //force global configuration
    public Option[] config() {
        return null;
    }


    public HyperIoTContext impersonateUser(HyperIoTBaseRestApi restApi, HyperIoTUser user) {
        return restApi.impersonate(user);
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
    public void test001_huserRegistrationAccountIsNotActivated() {
        List<Object> roles = new ArrayList<>();
        HUser huser = huserWithDefaultPermissionInHyperIoTFramework(false);
        Assert.assertNotEquals(0, huser.getId());
        Assert.assertFalse(huser.isActive());

        //checks: huser is not active
        AuthenticationRestApi authRestService = getOsgiService(AuthenticationRestApi.class);
        Response restResponse = authRestService.login(huser.getUsername(), huser.getPassword());
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUserNotActivated",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().size());
        Assert.assertEquals("it.acsoftware.hyperiot.authentication.error.user.not.active",
                ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(0));
        Assert.assertTrue(roles.isEmpty());
    }


    @Test
    public void test002_huserRegistrationAndActivationAccountStep() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        List<Object> roles = new ArrayList<>();
        HUser huser = huserWithDefaultPermissionInHyperIoTFramework(false);
        Assert.assertNotEquals(0, huser.getId());
        Assert.assertFalse(huser.isActive());

        //checks: huser is not active
        AuthenticationRestApi authRestService = getOsgiService(AuthenticationRestApi.class);
        Response restResponse = authRestService.login(huser.getUsername(), huser.getPassword());
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUserNotActivated",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().size());
        Assert.assertEquals("it.acsoftware.hyperiot.authentication.error.user.not.active",
                ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(0));
        Assert.assertTrue(roles.isEmpty());

        // Activate huser
        String activationCode = huser.getActivateCode();
        Assert.assertNotNull(activationCode);
        Response restResponseActivateUser = huserRestService.activate(huser.getEmail(), activationCode);
        Assert.assertEquals(200, restResponseActivateUser.getStatus());
        Role role = null;
        huser = (HUser) authService.login(huser.getUsername(), "passwordPass&01");
        roles = Arrays.asList(huser.getRoles().toArray());
        Assert.assertFalse(roles.isEmpty());
        Assert.assertTrue(huser.isActive());

        // checks: default role has been assigned to new huser
        Assert.assertEquals(1, huser.getRoles().size());
        Assert.assertEquals(roles.size(), huser.getRoles().size());
        Assert.assertFalse(roles.isEmpty());
        for (int i = 0; i < roles.size(); i++) {
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


    @Test
    public void test003_huserActiveChangePasswordShouldWork() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // huser changes password with the following call changePassword
        // response status code '200'
        HUser huser = huserWithDefaultPermissionInHyperIoTFramework(true);
        Assert.assertNotEquals(0, huser.getId());
        Assert.assertTrue(huser.isActive());
        String oldPassword = "passwordPass&01";
        String newPassword = "testPass01/";
        String passwordConfirm = "testPass01/";
        this.impersonateUser(huserRestService, huser);
        Assert.assertTrue(HyperIoTUtil.passwordMatches(oldPassword,huser.getPassword()));
        Response restResponse = huserRestService.changeHUserPassword(huser.getId(), oldPassword, newPassword,
                passwordConfirm);
        Assert.assertEquals(200, restResponse.getStatus());
        Assert.assertTrue(HyperIoTUtil.passwordMatches(newPassword, ((HUser) restResponse.getEntity()).getPassword()));
        Assert.assertTrue(HyperIoTUtil.passwordMatches(passwordConfirm, ((HUser) restResponse.getEntity()).getPasswordConfirm()));
    }


    @Test
    public void test004_huserActiveChangeHisAccountInfoShouldWork() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // HUser changes his account info with the following call changeAccountInfo
        // response status code '200'
        HUser huser = huserWithDefaultPermissionInHyperIoTFramework(true);
        Assert.assertNotEquals(0, huser.getId());
        Assert.assertTrue(huser.isActive());

        huser.setName("Giulio");
        huser.setLastname("Cesare");

        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.updateAccountInfo(huser);
        Assert.assertEquals(200, restResponse.getStatus());
        Assert.assertEquals("Giulio", ((HUser) restResponse.getEntity()).getName());
        Assert.assertEquals("Cesare", ((HUser) restResponse.getEntity()).getLastname());
        Assert.assertEquals(huser.getEntityVersion(),
                ((HUser) restResponse.getEntity()).getEntityVersion());
    }


    @Test
    public void test005_huserActiveTriesToChangeHUser2AccountInfoShouldFail() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // HUser tries to change huser2 account info with the following call changeAccountInfo
        // response status code '403' HyperIoTUnauthorizedException
        HUser huser = huserWithDefaultPermissionInHyperIoTFramework(true);
        Assert.assertNotEquals(0, huser.getId());
        Assert.assertTrue(huser.isActive());

        HUser huser2 = huserWithDefaultPermissionInHyperIoTFramework(true);
        Assert.assertNotEquals(0, huser2.getId());
        Assert.assertTrue(huser2.isActive());
        huser2.setName("Giulio");
        huser2.setLastname("Cesare");

        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.updateAccountInfo(huser2);
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }


    // HUser action save: 1 not assigned in default permission
    @Test
    public void test006_createNewHUserWithDefaultPermissionShouldFail() {
        HUserRestApi hUserRestApi = getOsgiService(HUserRestApi.class);
        // huser, with default permission, tries to save new HUser with the following call saveHUser.
        // huser to save a new huser needs the "save huser" permission
        // response status code '403' HyperIoTUnauthorizedException
        HUser huser = huserWithDefaultPermissionInHyperIoTFramework(true);
        Assert.assertNotEquals(0, huser.getId());
        Assert.assertTrue(huser.isActive());

        HUser huser2 = new HUser();
        huser2.setName("name");
        huser2.setLastname("lastname");
        huser2.setUsername("TestUser" + UUID.randomUUID().toString().replaceAll("-", ""));
        huser2.setEmail("testusername" + UUID.randomUUID().toString() + "@hyperiot.com");
        huser2.setPassword("passwordPass&01");
        huser2.setPasswordConfirm("passwordPass&01");

        this.impersonateUser(hUserRestApi, huser);
        Response restResponse = hUserRestApi.saveHUser(huser2);
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }


    // HUser action update: 2 not assigned in default permission
    @Test
    public void test007_updateHUserWithDefaultPermissionShouldFail() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // huser, with default permission, tries to update HUser with the following call updateHUser.
        // huser to update a huser needs the "update huser" permission
        // response status code '403' HyperIoTUnauthorizedException
        HUser huser = huserWithDefaultPermissionInHyperIoTFramework(true);
        Assert.assertNotEquals(0, huser.getId());
        Assert.assertTrue(huser.isActive());

        HUser huser2 = huserWithDefaultPermissionInHyperIoTFramework(true);
        Assert.assertNotEquals(0, huser2.getId());
        Assert.assertTrue(huser2.isActive());

        huser2.setName("edited failed");

        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.updateHUser(huser2);
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }


    // HUser action update: 2 not assigned in default permission
    @Test
    public void test008_huserActiveTriesToUpdateHisDataWithDefaultPermissionShouldFail() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // huser, with default permission, tries to update his data with the following call updateHUser.
        // huser to update a huser needs the "update huser" permission
        // response status code '403' HyperIoTUnauthorizedException
        HUser huser = huserWithDefaultPermissionInHyperIoTFramework(true);
        Assert.assertNotEquals(0, huser.getId());
        Assert.assertTrue(huser.isActive());

        huser.setName("new name");

        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.updateHUser(huser);
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }


    // HUser action remove: 4 not assigned in default permission
    @Test
    public void test009_removeHUserWithDefaultPermissionShouldFail() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // huser, with default permission, tries to remove huser2 with the following call removeHUser.
        // huser to delete a huser needs the "remove huser" permission
        // response status code '403' HyperIoTUnauthorizedException
        HUser huser = huserWithDefaultPermissionInHyperIoTFramework(true);
        Assert.assertNotEquals(0, huser.getId());
        Assert.assertTrue(huser.isActive());

        HUser huser2 = huserWithDefaultPermissionInHyperIoTFramework(true);
        Assert.assertNotEquals(0, huser2.getId());
        Assert.assertTrue(huser2.isActive());

        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.deleteHUser(huser2.getId());
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }


    // HUser action find: 8 not assigned in default permission
    @Test
    public void test010_findHUserWithDefaultPermissionShouldFail() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // huser, with default permission, tries to find huser2 with the following call findHUser.
        // huser to find a huser needs the "find huser" permission
        // response status code '403' HyperIoTUnauthorizedException
        HUser huser = huserWithDefaultPermissionInHyperIoTFramework(true);
        Assert.assertNotEquals(0, huser.getId());
        Assert.assertTrue(huser.isActive());

        HUser huser2 = huserWithDefaultPermissionInHyperIoTFramework(true);
        Assert.assertNotEquals(0, huser2.getId());
        Assert.assertTrue(huser2.isActive());

        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.findHUser(huser2.getId());
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    // HUser action find: 8 not assigned in default permission
    @Test
    public void test011_findHUserWithDefaultPermissionShouldFail() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // huser, with default permission, tries to find huser2 with the following call findHUser.
        // huser to find a huser needs the "find huser" permission
        // response status code '403' HyperIoTUnauthorizedException
        HUser huser = huserWithDefaultPermissionInHyperIoTFramework(true);
        Assert.assertNotEquals(0, huser.getId());
        Assert.assertTrue(huser.isActive());

        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.findHUser(huser.getId());
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }


    // HUser action find-all: 16 not assigned in default permission
    @Test
    public void test012_findAllHUserWithDefaultPermissionShouldFail() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // huser, with default permission, tries to find all husers with the following call findAllHUser.
        // huser to find all husers needs the "find-all huser" permission
        // response status code '403' HyperIoTUnauthorizedException
        HUser huser = huserWithDefaultPermissionInHyperIoTFramework(true);
        Assert.assertNotEquals(0, huser.getId());
        Assert.assertTrue(huser.isActive());

        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.findAllHUser();
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }


    // HUser action find-all: 16 not assigned in default permission
    @Test
    public void test013_findAllHUserPaginatedWithDefaultPermissionShouldFail() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // huser, with default permission, tries to find all husers paginated with the following call findAllHUserPaginated.
        // huser to find all husers paginated needs the "find-all huser" permission
        // response status code '403' HyperIoTUnauthorizedException
        int delta = 5;
        int page = 1;
        HUser huser = huserWithDefaultPermissionInHyperIoTFramework(true);
        Assert.assertNotEquals(0, huser.getId());
        Assert.assertTrue(huser.isActive());

        List<HUser> husers = new ArrayList<>();
        for (int i = 0; i < delta; i++) {
            HUser user = huserWithDefaultPermissionInHyperIoTFramework(false);
            Assert.assertNotEquals(0, user.getId());
            Assert.assertFalse(user.isActive());
            husers.add(huser);
        }
        Assert.assertEquals(delta, husers.size());
        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.findAllHUserPaginated(delta, page);
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


    private HUser huserWithDefaultPermissionInHyperIoTFramework(boolean isActive) {
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
            for (int i = 0; i < roles.size(); i++) {
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
    @Before
    public void afterTest() {
        HUserSystemApi hUserSystemApi = getOsgiService(HUserSystemApi.class);
        RoleSystemApi roleSystemApi = getOsgiService(RoleSystemApi.class);
        HyperIoTHUserTestUtils.truncateHUsers(hUserSystemApi);
        HyperIoTHUserTestUtils.truncateRoles(roleSystemApi);
    }

}
