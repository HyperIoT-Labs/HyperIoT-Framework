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
import it.acsoftware.hyperiot.base.api.HyperIoTUser;
import it.acsoftware.hyperiot.base.api.authentication.AuthenticationApi;
import it.acsoftware.hyperiot.base.service.rest.HyperIoTBaseRestApi;
import it.acsoftware.hyperiot.base.test.http.*;
import it.acsoftware.hyperiot.base.test.http.matcher.HyperIoTHttpResponseValidator;
import it.acsoftware.hyperiot.base.test.http.matcher.HyperIoTHttpResponseValidatorBuilder;
import it.acsoftware.hyperiot.huser.api.HUserSystemApi;
import it.acsoftware.hyperiot.huser.model.HUser;
import it.acsoftware.hyperiot.huser.service.rest.HUserRestApi;
import it.acsoftware.hyperiot.huser.test.util.HyperIoTHUserTestUtils;
import it.acsoftware.hyperiot.role.api.RoleSystemApi;
import it.acsoftware.hyperiot.role.model.Role;
import it.acsoftware.hyperiot.role.service.rest.RoleRestApi;
import org.apache.karaf.features.FeaturesService;
import org.apache.karaf.itests.KarafTestSupport;
import org.junit.After;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

import javax.ws.rs.core.Response;
import java.util.*;
import static it.acsoftware.hyperiot.role.test.HyperIoTRoleConfiguration.*;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HyperIoTRoleRestInterfaceTest extends KarafTestSupport {

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
        assertContains("HyperIoTPermission-features ", features);
        assertContains("HyperIoTHUser-features ", features);
        assertContains("HyperIoTCompany-features ", features);
        assertContains("HyperIoTAuthentication-features ", features);
        assertContains("HyperIoTAssetCategory-features ", features);
        assertContains("HyperIoTAssetTag-features ", features);
        assertContains("HyperIoTSharedEntity-features ", features);
        String datasource = executeCommand("jdbc:ds-list");
        assertContains("hyperiot", datasource);
    }

    @Test
    public void test001_saveRoleShouldSerializeResponseCorrectly(){
        Role role = new Role();
        role.setName("Name".concat(UUID.randomUUID().toString().replaceAll("-","")));
        role.setDescription("Description text");
        HyperIoTHttpRequest request = HyperIoTHttpRequestBuilder
                .post()
                .withUri(HyperIoTHttpUtils.SERVICE_BASE_URL.concat("/roles"))
                .withContentTypeHeader("application/json")
                .withAuthorizationAsHyperIoTAdmin()
                .withJsonBody(role)
                .build();
        List<String> expectedRoleProperties = roleProperties();
        expectedRoleProperties.addAll(hyperIoTAbstractEntityProperties());
        HyperIoTHttpResponse response = HyperIoTHttpClient
                .hyperIoTHttpClient()
                .execute(request);
        HyperIoTHttpResponseValidator testValidator = HyperIoTHttpResponseValidatorBuilder
                .validatorBuilder()
                .withStatusEqual(200)
                .containExactProperties(expectedRoleProperties)
                .build();
        boolean testSuccessful = testValidator.validateResponse(response);
        Assert.assertTrue(testSuccessful);
    }

    @Test
    public void test002_findRoleShouldSerializeResponseCorrectly(){
        Role role = createRole();
        Assert.assertNotEquals(0, role.getId());
        HyperIoTHttpRequest request = HyperIoTHttpRequestBuilder
                .get()
                .withUri(HyperIoTHttpUtils.SERVICE_BASE_URL.concat("/roles/").concat(String.valueOf(role.getId())))
                .withContentTypeHeader("application/json")
                .withAuthorizationAsHyperIoTAdmin()
                .build();
        List<String> expectedRoleProperties = roleProperties();
        expectedRoleProperties.addAll(hyperIoTAbstractEntityProperties());
        HyperIoTHttpResponse response = HyperIoTHttpClient
                .hyperIoTHttpClient()
                .execute(request);
        HyperIoTHttpResponseValidator testValidator = HyperIoTHttpResponseValidatorBuilder
                .validatorBuilder()
                .withStatusEqual(200)
                .containExactProperties(expectedRoleProperties)
                .build();
        boolean testSuccessful = testValidator.validateResponse(response);
        Assert.assertTrue(testSuccessful);
    }

    @Test
    public void test003_updateRoleShouldSerializeResponseCorrectly(){
        Role role = createRole();
        role.setDescription("NewDescription");
        Assert.assertNotEquals(0, role.getId());
        HyperIoTHttpRequest request = HyperIoTHttpRequestBuilder
                .put()
                .withUri(HyperIoTHttpUtils.SERVICE_BASE_URL.concat("/roles"))
                .withContentTypeHeader("application/json")
                .withAuthorizationAsHyperIoTAdmin()
                .withJsonBody(role)
                .build();
        List<String> expectedRoleProperties = roleProperties();
        expectedRoleProperties.addAll(hyperIoTAbstractEntityProperties());
        HyperIoTHttpResponse response = HyperIoTHttpClient
                .hyperIoTHttpClient()
                .execute(request);
        HyperIoTHttpResponseValidator testValidator = HyperIoTHttpResponseValidatorBuilder
                .validatorBuilder()
                .withStatusEqual(200)
                .containExactProperties(expectedRoleProperties)
                .build();
        boolean testSuccessful = testValidator.validateResponse(response);
        Assert.assertTrue(testSuccessful);
    }

    @Test
    public void test004_deleteRoleShouldSerializeResponseCorrectly(){
        Role role = createRole();
        Assert.assertNotEquals(0, role.getId());
        HyperIoTHttpRequest request = HyperIoTHttpRequestBuilder
                .delete()
                .withUri(HyperIoTHttpUtils.SERVICE_BASE_URL.concat("/roles/").concat(String.valueOf(role.getId())))
                .withAuthorizationAsHyperIoTAdmin()
                .build();
        List<String> expectedRoleProperties = roleProperties();
        expectedRoleProperties.addAll(hyperIoTAbstractEntityProperties());
        HyperIoTHttpResponse response = HyperIoTHttpClient
                .hyperIoTHttpClient()
                .execute(request);
        HyperIoTHttpResponseValidator testValidator = HyperIoTHttpResponseValidatorBuilder
                .validatorBuilder()
                .withStatusEqual(200)
                .withCustomCriteria((hyperIoTHttpResponse -> response.getResponseBody().isEmpty()))
                .build();
        boolean testSuccessful = testValidator.validateResponse(response);
        Assert.assertTrue(testSuccessful);
    }

    @Test
    public void test005_findAllRoleShouldSerializeResponseCorrectly(){
        Role role = createRole();
        Assert.assertNotEquals(0, role.getId());
        Role role2 = createRole();
        Assert.assertNotEquals(0, role2.getId());
        HyperIoTHttpRequest request = HyperIoTHttpRequestBuilder
                .get()
                .withUri(HyperIoTHttpUtils.SERVICE_BASE_URL.concat("/roles/all"))
                .withContentTypeHeader("application/json")
                .withAuthorizationAsHyperIoTAdmin()
                .build();
        List<String> expectedRoleProperties = roleProperties();
        expectedRoleProperties.addAll(hyperIoTAbstractEntityProperties());
        HyperIoTHttpResponse response = HyperIoTHttpClient
                .hyperIoTHttpClient()
                .execute(request);
        HyperIoTHttpResponseValidator testValidator = HyperIoTHttpResponseValidatorBuilder
                .validatorBuilder()
                .withStatusEqual(200)
                .containExactProperties(expectedRoleProperties)
                .build();
        boolean testSuccessful = testValidator.validateResponse(response);
        Assert.assertTrue(testSuccessful);
    }

    @Test
    public void test006_findAllRolePaginatedShouldSerializeResponseCorrectly(){
        Role role = createRole();
        Assert.assertNotEquals(0, role.getId());
        Role role2 = createRole();
        Assert.assertNotEquals(0, role2.getId());
        HyperIoTHttpRequest request = HyperIoTHttpRequestBuilder
                .get()
                .withUri(HyperIoTHttpUtils.SERVICE_BASE_URL.concat("/roles"))
                .withContentTypeHeader("application/json")
                .withAuthorizationAsHyperIoTAdmin()
                .withParameter("delta",String.valueOf(defaultDelta))
                .withParameter("page", String.valueOf(defaultPage))
                .build();
        List<String> expectedRoleProperties = roleProperties();
        expectedRoleProperties.addAll(hyperIoTAbstractEntityProperties());
        HyperIoTHttpResponse response = HyperIoTHttpClient
                .hyperIoTHttpClient()
                .execute(request);
        HyperIoTHttpResponseValidator testValidator = HyperIoTHttpResponseValidatorBuilder
                .validatorBuilder()
                .withStatusEqual(200)
                .containExactHyperIoTPaginatedProperties()
                .containExactInnerProperties("results",expectedRoleProperties)
                .build();
        boolean testSuccessful = testValidator.validateResponse(response);
        Assert.assertTrue(testSuccessful);
    }

    @Test
    public void test007_findAllUserRolesShouldSerializeResponseCorrectly(){
        HUser user = createHUser();
        Assert.assertNotEquals(0, user.getId());
        Role r = createUserRole(user);
        Assert.assertNotEquals(0, r.getId());
        HyperIoTHttpRequest request = HyperIoTHttpRequestBuilder
                .get()
                .withUri(HyperIoTHttpUtils.SERVICE_BASE_URL.concat("/roles/user/").concat(String.valueOf(user.getId())))
                .withContentTypeHeader("application/json")
                .withAuthorizationAsHyperIoTAdmin()
                .build();
        List<String> expectedRoleProperties = roleProperties();
        expectedRoleProperties.addAll(hyperIoTAbstractEntityProperties());
        HyperIoTHttpResponse response = HyperIoTHttpClient
                .hyperIoTHttpClient()
                .execute(request);
        HyperIoTHttpResponseValidator testValidator = HyperIoTHttpResponseValidatorBuilder
                .validatorBuilder()
                .withStatusEqual(200)
                .containExactProperties(expectedRoleProperties)
                .build();
        boolean testSuccessful = testValidator.validateResponse(response);
        Assert.assertTrue(testSuccessful);
    }

    @Test
    public void test008_saveUserRoleShouldSerializeResponseCorrectly(){
        HUser user = createHUser();
        Assert.assertNotEquals(0, user.getId());
        Role r = createRole();
        Assert.assertNotEquals(0, r.getId());
        String requestUri = HyperIoTHttpUtils
                .SERVICE_BASE_URL
                .concat("/roles/").concat(String.valueOf(r.getId()))
                .concat("/user/").concat(String.valueOf(user.getId()));
        HyperIoTHttpRequest request = HyperIoTHttpRequestBuilder
                .post()
                .withUri(requestUri)
                .withContentTypeHeader("application/json")
                .withAuthorizationAsHyperIoTAdmin()
                .build();
        List<String> expectedRoleProperties = roleProperties();
        expectedRoleProperties.addAll(hyperIoTAbstractEntityProperties());
        HyperIoTHttpResponse response = HyperIoTHttpClient
                .hyperIoTHttpClient()
                .execute(request);
        HyperIoTHttpResponseValidator testValidator = HyperIoTHttpResponseValidatorBuilder
                .validatorBuilder()
                .withStatusEqual(200)
                .containExactProperties(expectedRoleProperties)
                .build();
        boolean testSuccessful = testValidator.validateResponse(response);
        Assert.assertTrue(testSuccessful);
    }

    @Test
    public void test009_deleteUserRoleShouldSerializeResponseCorrectly(){
        HUser user = createHUser();
        Assert.assertNotEquals(0, user.getId());
        Role r = createUserRole(user);
        Assert.assertNotEquals(0, r.getId());
        String requestUri = HyperIoTHttpUtils
                .SERVICE_BASE_URL
                .concat("/roles/").concat(String.valueOf(r.getId()))
                .concat("/user/").concat(String.valueOf(user.getId()));
        HyperIoTHttpRequest request = HyperIoTHttpRequestBuilder
                .delete()
                .withUri(requestUri)
                .withContentTypeHeader("application/json")
                .withAuthorizationAsHyperIoTAdmin()
                .build();
        List<String> expectedRoleProperties = roleProperties();
        expectedRoleProperties.addAll(hyperIoTAbstractEntityProperties());
        HyperIoTHttpResponse response = HyperIoTHttpClient
                .hyperIoTHttpClient()
                .execute(request);
        HyperIoTHttpResponseValidator testValidator = HyperIoTHttpResponseValidatorBuilder
                .validatorBuilder()
                .withStatusEqual(200)
                .containExactProperties(expectedRoleProperties)
                .build();
        boolean testSuccessful = testValidator.validateResponse(response);
        Assert.assertTrue(testSuccessful);
    }




    /*
    *
    * UTILITY METHODS
    *
    *
    */


    private List<String> roleProperties(){
        LinkedList<String> huserRoleProperties = new LinkedList<>();
        huserRoleProperties.add("name");
        huserRoleProperties.add("description");
        return huserRoleProperties;
    }

    private List<String> hyperIoTAbstractEntityProperties(){
        List<String> hyperIoTAbstractEntityFields = new ArrayList<>();
        hyperIoTAbstractEntityFields.add("id");
        hyperIoTAbstractEntityFields.add("entityCreateDate");
        hyperIoTAbstractEntityFields.add("entityModifyDate");
        hyperIoTAbstractEntityFields.add("entityVersion");
        hyperIoTAbstractEntityFields.add("categoryIds");
        hyperIoTAbstractEntityFields.add("tagIds");
        return hyperIoTAbstractEntityFields;
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

    @After
    public void afterTest() {
        HUserSystemApi hUserSystemApi = getOsgiService(HUserSystemApi.class);
        RoleSystemApi roleSystemApi = getOsgiService(RoleSystemApi.class);
        HyperIoTHUserTestUtils.truncateHUsers(hUserSystemApi);
        HyperIoTHUserTestUtils.truncateRoles(roleSystemApi);
    }
}
