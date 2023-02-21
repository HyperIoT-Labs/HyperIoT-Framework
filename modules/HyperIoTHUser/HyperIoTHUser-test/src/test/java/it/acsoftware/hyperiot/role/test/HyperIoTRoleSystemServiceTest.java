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

import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.base.api.HyperIoTPermissionManager;
import it.acsoftware.hyperiot.base.api.HyperIoTUser;
import it.acsoftware.hyperiot.base.api.authentication.AuthenticationApi;
import it.acsoftware.hyperiot.base.service.rest.HyperIoTBaseRestApi;
import it.acsoftware.hyperiot.base.test.HyperIoTTestConfigurationBuilder;
import it.acsoftware.hyperiot.huser.api.HUserApi;
import it.acsoftware.hyperiot.huser.api.HUserSystemApi;
import it.acsoftware.hyperiot.huser.model.HUser;
import it.acsoftware.hyperiot.huser.service.rest.HUserRestApi;
import it.acsoftware.hyperiot.permission.api.PermissionSystemApi;
import it.acsoftware.hyperiot.permission.model.Permission;
import it.acsoftware.hyperiot.role.api.RoleApi;
import it.acsoftware.hyperiot.role.api.RoleRepository;
import it.acsoftware.hyperiot.role.api.RoleSystemApi;
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

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import static it.acsoftware.hyperiot.role.test.HyperIoTRoleConfiguration.CODE_COVERAGE_PACKAGE_FILTER;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HyperIoTRoleSystemServiceTest extends KarafTestSupport {

    //force global configuration
    public Option[] config() {
        return null;
    }

    public void impersonateUser(HyperIoTBaseRestApi restApi, HyperIoTUser user) {
        restApi.impersonate(user);
    }

    @Test
    public void test00s_hyperIoTFrameworkShouldBeInstalled() {
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
    public void test01s_getUserRolesShouldWorkIfUserNotHasRole() {
        RoleSystemApi roleSystemApi = getOsgiService(RoleSystemApi.class);
        // the following call getUserRoles tries to find all user Role, HUser not
        // has role and listRoles is empty
        HUser huser = createHUser();
        Collection<Role> listRoles = roleSystemApi.getUserRoles(huser.getId());
        Assert.assertTrue(listRoles.isEmpty());
        Assert.assertEquals(0, listRoles.size());
    }


    @Test
    public void test02s_getUserRolesShouldWork() {
        RoleSystemApi roleSystemApi = getOsgiService(RoleSystemApi.class);
        // the following call getUserRoles find all user Role
        RoleApi roleApi = getOsgiService(RoleApi.class);
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HyperIoTContext ctx = roleRestService.impersonate(adminUser);
        HUser huser = createHUser();
        Role role = createRole();
        huser.addRole(role);
        roleApi.saveUserRole(huser.getId(), role.getId(), ctx);
        Collection<Role> roles = roleSystemApi.getUserRoles(huser.getId());
        Assert.assertFalse(roles.isEmpty());
        boolean roleFound = false;
        for (Role listRoles : roles) {
            if (role.getId() == listRoles.getId())
                roleFound = true;
        }
        Assert.assertTrue(roleFound);
        Assert.assertEquals(1, roles.size());
    }


    @Test
    public void test03s_findByNameShouldWork() {
        RoleSystemApi roleSystemApi = getOsgiService(RoleSystemApi.class);
        // the following call findByName find specific Role by name
        Role role = createRole();
        Role findRole = roleSystemApi.findByName(role.getName());
        Assert.assertEquals(role.getName(), findRole.getName());
        Assert.assertNotNull(findRole);
    }

    @Test
    public void test04s_findByNameShouldFailIfEntityNotFound() {
        RoleSystemApi roleSystemApi = getOsgiService(RoleSystemApi.class);
        // the following call tries to Role by name with the following call findByName,
        // but entity not found
        Role findRole = roleSystemApi.findByName(null);
        Assert.assertNull(findRole);
    }

    @Test
    public void test05s_findByNameShouldFailIfRoleIsNotStoredInDB() {
        RoleSystemApi roleSystemApi = getOsgiService(RoleSystemApi.class);
        // the following call findByName tries to find Role by name;
        // this call fails because Role is not stored in database
        Role role = new Role();
        role.setName("Role" + java.util.UUID.randomUUID());
        role.setDescription("Description");
        Role findRole = roleSystemApi.findByName(role.getName());
        Assert.assertNull(findRole);
    }


    @Test
    public void test06s_defaultRoleAndPermissionsCreatedInHyperIoTFramework() {
        RoleRepository roleRepository = getOsgiService(RoleRepository.class);
        // This test checks if default role "RegisteredUser" and permissions has been created in HyperIoTFramework
        Role role = roleRepository.findByName("RegisteredUser");
        PermissionSystemApi permissionSystemApi = getOsgiService(PermissionSystemApi.class);
        Collection<Permission> listPermissions = permissionSystemApi.findByRole(role);
        Assert.assertFalse(listPermissions.isEmpty());
        Assert.assertEquals(2, listPermissions.size());
        boolean resourceNameAssetCategory = false;
        boolean resourceNameAssetTag = false;
        for (int i = 0; i < listPermissions.size(); i++) {
            if (((Permission) ((ArrayList) listPermissions).get(i)).getEntityResourceName().contains(HyperIoTRoleConfiguration.permissionAssetCategory)) {
                resourceNameAssetCategory = true;
                Assert.assertEquals("it.acsoftware.hyperiot.asset.category.model.AssetCategory", ((Permission) ((ArrayList) listPermissions).get(i)).getEntityResourceName());
                Assert.assertEquals(HyperIoTRoleConfiguration.permissionAssetCategory + HyperIoTRoleConfiguration.nameRegisteredPermission, ((Permission) ((ArrayList) listPermissions).get(i)).getName());
                Assert.assertEquals(31, ((Permission) ((ArrayList) listPermissions).get(i)).getActionIds());
                Assert.assertEquals(role.getName(), ((Permission) ((ArrayList) listPermissions).get(i)).getRole().getName());
            }
            if (((Permission) ((ArrayList) listPermissions).get(i)).getEntityResourceName().contains(HyperIoTRoleConfiguration.permissionAssetTag)) {
                resourceNameAssetTag = true;
                Assert.assertEquals("it.acsoftware.hyperiot.asset.tag.model.AssetTag", ((Permission) ((ArrayList) listPermissions).get(i)).getEntityResourceName());
                Assert.assertEquals(HyperIoTRoleConfiguration.permissionAssetTag + HyperIoTRoleConfiguration.nameRegisteredPermission, ((Permission) ((ArrayList) listPermissions).get(i)).getName());
                Assert.assertEquals(31, ((Permission) ((ArrayList) listPermissions).get(i)).getActionIds());
                Assert.assertEquals(role.getName(), ((Permission) ((ArrayList) listPermissions).get(i)).getRole().getName());
            }
        }
        Assert.assertTrue(resourceNameAssetCategory);
        Assert.assertTrue(resourceNameAssetTag);
    }

    @Test
    public void test07s_getUserHasRolePermissionMethod() {
        HUserSystemApi hUserSystemApi = getOsgiService(HUserSystemApi.class);
        HyperIoTPermissionManager pmManager = getOsgiService(HyperIoTPermissionManager.class);
        HUser huser = createHUser();
        Role r = createRole();
        huser.addRole(r);
        hUserSystemApi.update(huser,null);
        Assert.assertTrue(pmManager.userHasRoles(huser.getUsername(),new String[]{r.getName()}));
    }





    /*
     *
     *
     * UTILITY METHODS
     *
     *
     */

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

}
