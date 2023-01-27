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

import it.acsoftware.hyperiot.base.api.authentication.AuthenticationApi;
import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.base.api.HyperIoTUser;
import it.acsoftware.hyperiot.base.exception.HyperIoTEntityNotFound;
import it.acsoftware.hyperiot.base.exception.HyperIoTUnauthorizedException;
import it.acsoftware.hyperiot.base.test.HyperIoTTestConfigurationBuilder;
import it.acsoftware.hyperiot.huser.api.HUserApi;
import it.acsoftware.hyperiot.huser.model.HUser;
import it.acsoftware.hyperiot.huser.service.rest.HUserRestApi;
import it.acsoftware.hyperiot.role.api.RoleApi;
import it.acsoftware.hyperiot.role.model.Role;
import it.acsoftware.hyperiot.role.service.rest.RoleRestApi;
import org.apache.karaf.features.FeaturesService;
import org.apache.karaf.itests.KarafTestSupport;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

import java.util.Collection;
import java.util.UUID;

import static it.acsoftware.hyperiot.role.test.HyperIoTRoleConfiguration.CODE_COVERAGE_PACKAGE_FILTER;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HyperIoTRoleServiceTest extends KarafTestSupport {

    //force global configuration
    public Option[] config() {
        return null;
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
    public void test01_getUserRolesShouldWorkIfUserNotHasRole() {
        RoleApi roleApi = getOsgiService(RoleApi.class);
        // hadmin find all user Role with the following call getUserRoles, HUser not
        // has role and listRoles is empty
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = createHUser();
        HyperIoTContext ctx = roleRestService.impersonate(adminUser);
        Collection<Role> listRoles = roleApi.getUserRoles(huser.getId(), ctx);
        Assert.assertTrue(listRoles.isEmpty());
        Assert.assertEquals(0, listRoles.size());
    }

    @Test
    public void test02_getUserRolesShouldWork() {
        RoleApi roleApi = getOsgiService(RoleApi.class);
        // hadmin find all user Role with the following call getUserRoles
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = createHUser();
        createUserRole(huser);
        HyperIoTContext ctx = roleRestService.impersonate(adminUser);
        Collection<Role> listRoles = roleApi.getUserRoles(huser.getId(), ctx);
        Assert.assertFalse(listRoles.isEmpty());
        Assert.assertEquals(1, listRoles.size());
    }

    @Test
    public void test03_getUserRolesShouldFailIfUserIsUnauthorized() throws HyperIoTUnauthorizedException {
        RoleApi roleApi = getOsgiService(RoleApi.class);
        // huser tries to find all user Role with the following call getUserRoles,
        // but it's unauthorized
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        HUser huser = createHUser();
        createUserRole(huser);
        boolean unauthorized = false;
        HyperIoTContext ctx = roleRestService.impersonate(huser);
        try {
            roleApi.getUserRoles(huser.getId(), ctx);
        } catch (HyperIoTUnauthorizedException e) {
            unauthorized = true;
        }
        Assert.assertTrue(unauthorized);
    }

    @Test
    public void test04_getUserRolesShouldFailIfUserNotFound() {
        RoleApi roleApi = getOsgiService(RoleApi.class);
        // hadmin tries to find all user Role with the following call getUserRoles,
        // but HUser not found
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = createHUser();
        HyperIoTContext ctx = roleRestService.impersonate(adminUser);
        Collection<Role> listRoles = roleApi.getUserRoles(huser.getId(), ctx);
        Assert.assertTrue(listRoles.isEmpty());
        Assert.assertEquals(0, listRoles.size());
    }

    @Test
    public void test05_saveUserRoleShouldWork() {
        RoleApi roleApi = getOsgiService(RoleApi.class);
        // hadmin save user Role with the following call saveUserRole
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = createHUser();
        Role role = createRole();
        HyperIoTContext ctx = roleRestService.impersonate(adminUser);
        Role userRole = roleApi.saveUserRole(huser.getId(), role.getId(), ctx);
        Assert.assertNotNull(userRole);
    }

    @Test
    public void test06_saveUserRoleShouldFailIfRoleNotFound() throws HyperIoTEntityNotFound {
        RoleApi roleApi = getOsgiService(RoleApi.class);
        // hadmin tries to save user Role with the following call saveUserRole,
        // but Role not found
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = createHUser();
        HyperIoTContext ctx = roleRestService.impersonate(adminUser);
        boolean entityFound = true;
        try {
            roleApi.saveUserRole(huser.getId(), 0, ctx);
        } catch (HyperIoTEntityNotFound e) {
            entityFound = false;
        }
        Assert.assertFalse(entityFound);
    }

    @Test
    public void test07_saveUserRoleShouldFailIfUserNotFound() throws HyperIoTEntityNotFound {
        RoleApi roleApi = getOsgiService(RoleApi.class);
        // hadmin tries to save user Role with the following call saveUserRole,
        // but HUser not found
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HyperIoTContext ctx = roleRestService.impersonate(adminUser);
        Role role = createRole();
        boolean entityFound = true;
        try {
            roleApi.saveUserRole(0, role.getId(), ctx);
        } catch (HyperIoTEntityNotFound e) {
            entityFound = false;
        }
        Assert.assertFalse(entityFound);
    }

    @Test
    public void test08_saveUserRoleShouldFailIfUserIsUnauthorized() throws HyperIoTUnauthorizedException {
        RoleApi roleApi = getOsgiService(RoleApi.class);
        // huser tries to save user Role with the following call saveUserRole,
        // but it's unauthorized
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        HUser huser = createHUser();
        Role role = createRole();
        HyperIoTContext ctx = roleRestService.impersonate(huser);
        boolean unauthorized = false;
        try {
            roleApi.saveUserRole(huser.getId(), role.getId(), ctx);
        } catch (HyperIoTUnauthorizedException e) {
            unauthorized = true;
        }
        Assert.assertTrue(unauthorized);
    }

    @Test
    public void test09_deleteUserRoleShouldWork() {
        RoleApi roleApi = getOsgiService(RoleApi.class);
        // hadmin delete user Role with the following call removeUserRole
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = createHUser();
        Role role = createUserRole(huser);
        HyperIoTContext ctx = roleRestService.impersonate(adminUser);
        Role removeRole = roleApi.removeUserRole(huser.getId(), role.getId(), ctx);
        Assert.assertNotNull(removeRole);
    }

    @Test
    public void test10_deleteUserRoleShouldFailIfRoleNotFound() throws HyperIoTEntityNotFound {
        RoleApi roleApi = getOsgiService(RoleApi.class);
        // hadmin tries to delete user Role with the following call removeUserRole,
        // but Role not found
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = createHUser();
        HyperIoTContext ctx = roleRestService.impersonate(adminUser);
        boolean entityFound = true;
        try {
            roleApi.removeUserRole(huser.getId(), 0, ctx);
        } catch (HyperIoTEntityNotFound e) {
            entityFound = false;
        }
        Assert.assertFalse(entityFound);
    }

    @Test
    public void test11_deleteUserRoleShouldFailIfUserNotFound() throws HyperIoTEntityNotFound {
        RoleApi roleApi = getOsgiService(RoleApi.class);
        // hadmin tries to delete user Role with the following call removeUserRole,
        // but HUser not found
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Role role = createRole();
        HyperIoTContext ctx = roleRestService.impersonate(adminUser);
        boolean entityFound = true;
        try {
            roleApi.removeUserRole(0, role.getId(), ctx);
        } catch (HyperIoTEntityNotFound e) {
            entityFound = false;
        }
        Assert.assertFalse(entityFound);
    }

    @Test
    public void test12_deleteUserRoleShouldFailIfUserIsUnauthorized() throws HyperIoTUnauthorizedException {
        RoleApi roleApi = getOsgiService(RoleApi.class);
        // hadmin tries to delete user Role with the following call removeUserRole,
        // but it's unauthorized
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        HUser huser = createHUser();
        Role role = createUserRole(huser);
        HyperIoTContext ctx = roleRestService.impersonate(huser);
        boolean unauthorized = false;
        try {
            roleApi.removeUserRole(huser.getId(), role.getId(), ctx);
        } catch (HyperIoTUnauthorizedException e) {
            unauthorized = true;
        }
        Assert.assertTrue(unauthorized);
    }


    /*
     *
     *
     * UTILITY METHODS
     *
     *
     */

    private Role createRole() {
        RoleApi roleApi = getOsgiService(RoleApi.class);
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HyperIoTContext ctx = roleRestService.impersonate(adminUser);
        Role role = new Role();
        role.setName("Role" + java.util.UUID.randomUUID());
        role.setDescription("Description");
        roleApi.save(role, ctx);
        Assert.assertNotNull(role);
        return role;
    }

    private HUser createHUser() {
        HUserApi hUserApi = getOsgiService(HUserApi.class);
        HUserRestApi hUserRestApi = getOsgiService(HUserRestApi.class);
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HyperIoTContext ctx = hUserRestApi.impersonate(adminUser);
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
        hUserApi.save(huser, ctx);
        return huser;
    }

    private Role createUserRole(HUser huser) {
        RoleApi roleApi = getOsgiService(RoleApi.class);
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HyperIoTContext ctx = roleRestService.impersonate(adminUser);
        Role role = createRole();
        huser.addRole(role);
        Role userRole = roleApi.saveUserRole(huser.getId(), role.getId(), ctx);
        return userRole;
    }

}
