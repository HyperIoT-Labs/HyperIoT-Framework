package it.acsoftware.hyperiot.shared.entity.example.test;

import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.base.api.HyperIoTUser;
import it.acsoftware.hyperiot.base.api.authentication.AuthenticationApi;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTBaseEntity;
import it.acsoftware.hyperiot.base.service.rest.HyperIoTBaseRestApi;
import it.acsoftware.hyperiot.base.test.http.*;
import it.acsoftware.hyperiot.base.test.http.matcher.HyperIoTHttpResponseValidator;
import it.acsoftware.hyperiot.base.test.http.matcher.HyperIoTHttpResponseValidatorBuilder;
import it.acsoftware.hyperiot.base.test.util.HyperIoTTestUtils;
import it.acsoftware.hyperiot.huser.api.HUserSystemApi;
import it.acsoftware.hyperiot.huser.model.HUser;
import it.acsoftware.hyperiot.huser.service.rest.HUserRestApi;
import it.acsoftware.hyperiot.huser.test.util.HyperIoTHUserTestUtils;
import it.acsoftware.hyperiot.permission.api.PermissionSystemApi;
import it.acsoftware.hyperiot.role.api.RoleSystemApi;
import it.acsoftware.hyperiot.role.util.HyperIoTRoleConstants;
import it.acsoftware.hyperiot.shared.entity.api.SharedEntitySystemApi;
import it.acsoftware.hyperiot.shared.entity.example.api.SharedEntityExampleSystemApi;
import it.acsoftware.hyperiot.shared.entity.example.model.SharedEntityExample;
import it.acsoftware.hyperiot.shared.entity.example.service.rest.SharedEntityExampleRestApi;
import it.acsoftware.hyperiot.shared.entity.model.SharedEntity;
import it.acsoftware.hyperiot.shared.entity.service.rest.SharedEntityRestApi;
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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static it.acsoftware.hyperiot.shared.entity.example.test.HyperIoTSharedEntityExampleConfiguration.*;

/**
 * @author Francesco Salerno
 *
 * This is a test class relative to Interface's test of SharedEntityRestApi
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HyperIoTSharedEntityExampleRestInterfaceTest extends KarafTestSupport {

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
        //HyperIoTCore
        assertContains("HyperIoTBase-features ", features);
        assertContains("HyperIoTMail-features ", features);
        assertContains("HyperIoTAuthentication-features ", features);
        assertContains("HyperIoTPermission-features ", features);
        assertContains("HyperIoTHUser-features ", features);
        assertContains("HyperIoTCompany-features ", features);
        assertContains("HyperIoTAssetCategory-features", features);
        assertContains("HyperIoTAssetTag-features", features);
        assertContains("HyperIoTSharedEntity-features", features);
        assertContains("HyperIoTSharedEntityExample-features", features);
        String datasource = executeCommand("jdbc:ds-list");
        assertContains("hyperiot", datasource);
    }

    @Test
    public void test001_saveSharedEntityShouldSerializeResponseCorrectly(){
        AuthenticationApi authenticationApi  = getOsgiService(AuthenticationApi.class);
        HUser adminUser = (HUser) authenticationApi.login("hadmin","admin");
        SharedEntityExample sharedEntityExample = createSharedEntityExample();
        SharedEntity sharedEntity = new SharedEntity();
        sharedEntity.setEntityId(sharedEntityExample.getId());
        sharedEntity.setEntityResourceName(entityExampleResourceName);
        sharedEntity.setUserId(adminUser.getId());
        HyperIoTHttpRequest request = HyperIoTHttpRequestBuilder
                .post()
                .withUri(HyperIoTHttpUtils.SERVICE_BASE_URL.concat("/sharedentity"))
                .withContentTypeHeader("application/json")
                .withAuthorizationAsHyperIoTAdmin()
                .withJsonBody(sharedEntity)
                .build();
        HyperIoTHttpResponse response = HyperIoTHttpClient
                .hyperIoTHttpClient()
                .execute(request);
        HyperIoTHttpResponseValidator testValidator = HyperIoTHttpResponseValidatorBuilder
                .validatorBuilder()
                .withStatusEqual(200)
                .containExactProperties(expectedSharedEntityProperty())
                .build();
        boolean testSuccessful = testValidator.validateResponse(response);
        Assert.assertTrue(testSuccessful);
    }


    @Test
    public void test002_deleteSharedEntityShouldSerializeResponseCorrectly(){
        AuthenticationApi authenticationApi  = getOsgiService(AuthenticationApi.class);
        HUser adminUser = (HUser) authenticationApi.login("hadmin","admin");
        SharedEntityExample sharedEntityExample = createSharedEntityExample();
        SharedEntity sharedEntity = createSharedEntity(sharedEntityExample,adminUser,adminUser);
        HyperIoTHttpRequest request = HyperIoTHttpRequestBuilder
                .delete()
                .withUri(HyperIoTHttpUtils.SERVICE_BASE_URL.concat("/sharedentity"))
                .withContentTypeHeader("application/json")
                .withAuthorizationAsHyperIoTAdmin()
                .withJsonBody(sharedEntity)
                .build();
        HyperIoTHttpResponse response = HyperIoTHttpClient
                .hyperIoTHttpClient()
                .execute(request);
        HyperIoTHttpResponseValidator testValidator = HyperIoTHttpResponseValidatorBuilder
                .validatorBuilder()
                .withStatusEqual(200)
                .withCustomCriteria(hyperIoTHttpResponse -> hyperIoTHttpResponse.getResponseBody().isEmpty())
                .build();
        boolean testSuccessful = testValidator.validateResponse(response);
        Assert.assertTrue(testSuccessful);
    }

    @Test
    public void test003_findAllSharedEntityShouldSerializeResponseCorrectly(){
        AuthenticationApi authenticationApi  = getOsgiService(AuthenticationApi.class);
        HUser adminUser = (HUser) authenticationApi.login("hadmin","admin");
        SharedEntityExample sharedEntityExample = createSharedEntityExample();
        createSharedEntity(sharedEntityExample,adminUser,adminUser);
        SharedEntityExample sharedEntityExample2 = createSharedEntityExample();
        createSharedEntity(sharedEntityExample2,adminUser,adminUser);
        HyperIoTHttpRequest request = HyperIoTHttpRequestBuilder
                .get()
                .withUri(HyperIoTHttpUtils.SERVICE_BASE_URL.concat("/sharedentity/all"))
                .withContentTypeHeader("application/json")
                .withAuthorizationAsHyperIoTAdmin()
                .build();
        HyperIoTHttpResponse response = HyperIoTHttpClient
                .hyperIoTHttpClient()
                .execute(request);
        HyperIoTHttpResponseValidator testValidator = HyperIoTHttpResponseValidatorBuilder
                .validatorBuilder()
                .withStatusEqual(200)
                .containExactProperties(expectedSharedEntityProperty())
                .build();
        boolean testSuccessful = testValidator.validateResponse(response);
        Assert.assertTrue(testSuccessful);

    }

    @Test
    public void test004_findAllSharedEntityPaginatedShouldSerializeResponseCorrectly(){
        AuthenticationApi authenticationApi  = getOsgiService(AuthenticationApi.class);
        HUser adminUser = (HUser) authenticationApi.login("hadmin","admin");
        SharedEntityExample sharedEntityExample = createSharedEntityExample();
        createSharedEntity(sharedEntityExample,adminUser,adminUser);
        SharedEntityExample sharedEntityExample2 = createSharedEntityExample();
        createSharedEntity(sharedEntityExample2,adminUser,adminUser);
        HyperIoTHttpRequest request = HyperIoTHttpRequestBuilder
                .get()
                .withUri(HyperIoTHttpUtils.SERVICE_BASE_URL.concat("/sharedentity"))
                .withContentTypeHeader("application/json")
                .withAuthorizationAsHyperIoTAdmin()
                .withParameter("delta",String.valueOf(defaultDelta))
                .withParameter("page",String.valueOf(defaultPage))
                .build();
        HyperIoTHttpResponse response = HyperIoTHttpClient
                .hyperIoTHttpClient()
                .execute(request);
        HyperIoTHttpResponseValidator testValidator = HyperIoTHttpResponseValidatorBuilder
                .validatorBuilder()
                .withStatusEqual(200)
                .containExactHyperIoTPaginatedProperties()
                .containExactInnerProperties("results",expectedSharedEntityProperty())
                .build();
        boolean testSuccessful = testValidator.validateResponse(response);
        Assert.assertTrue(testSuccessful);

    }

    @Test
    public void test005_findByPkShouldSerializeResponseCorrectly(){
        AuthenticationApi authenticationApi  = getOsgiService(AuthenticationApi.class);
        HUser adminUser = (HUser) authenticationApi.login("hadmin","admin");
        SharedEntityExample sharedEntityExample = createSharedEntityExample();
        SharedEntity sharedEntity = createSharedEntity(sharedEntityExample,adminUser,adminUser);
        HyperIoTHttpRequest request = HyperIoTHttpRequestBuilder
                .get()
                .withUri(HyperIoTHttpUtils.SERVICE_BASE_URL.concat("/sharedentity/findByPK"))
                .withContentTypeHeader("application/json")
                .withAuthorizationAsHyperIoTAdmin()
                .withJsonBody(sharedEntity)
                .build();
        HyperIoTHttpResponse response = HyperIoTHttpClient
                .hyperIoTHttpClient()
                .execute(request);
        HyperIoTHttpResponseValidator testValidator = HyperIoTHttpResponseValidatorBuilder
                .validatorBuilder()
                .withStatusEqual(200)
                .containExactProperties(expectedSharedEntityProperty())
                .build();
        boolean testSuccessful = testValidator.validateResponse(response);
        Assert.assertTrue(testSuccessful);
    }

    @Test
    public void test006_findByEntityShouldSerializeResponseCorrectly(){
        AuthenticationApi authenticationApi  = getOsgiService(AuthenticationApi.class);
        HUser adminUser = (HUser) authenticationApi.login("hadmin","admin");
        SharedEntityExample sharedEntityExample = createSharedEntityExample();
        SharedEntity sharedEntity = createSharedEntity(sharedEntityExample,adminUser,adminUser);
        HyperIoTHttpRequest request = HyperIoTHttpRequestBuilder
                .get()
                .withUri(HyperIoTHttpUtils.SERVICE_BASE_URL.concat("/sharedentity/findByEntity"))
                .withContentTypeHeader("application/json")
                .withAuthorizationAsHyperIoTAdmin()
                .withParameter("entityResourceName",sharedEntity.getEntityResourceName())
                .withParameter("entityId", String.valueOf(sharedEntity.getEntityId()))
                .build();
        HyperIoTHttpResponse response = HyperIoTHttpClient
                .hyperIoTHttpClient()
                .execute(request);
        HyperIoTHttpResponseValidator testValidator = HyperIoTHttpResponseValidatorBuilder
                .validatorBuilder()
                .withStatusEqual(200)
                .containExactProperties(expectedSharedEntityProperty())
                .build();
        boolean testSuccessful = testValidator.validateResponse(response);
        Assert.assertTrue(testSuccessful);
    }

    @Test
    public void test007_findByUserShouldSerializeResponseCorrectly(){
        AuthenticationApi authenticationApi  = getOsgiService(AuthenticationApi.class);
        HUser adminUser = (HUser) authenticationApi.login("hadmin","admin");
        SharedEntityExample sharedEntityExample = createSharedEntityExample();
        SharedEntity sharedEntity = createSharedEntity(sharedEntityExample,adminUser,adminUser);
        HyperIoTHttpRequest request = HyperIoTHttpRequestBuilder
                .get()
                .withUri(HyperIoTHttpUtils.SERVICE_BASE_URL.concat("/sharedentity/findByEntity"))
                .withContentTypeHeader("application/json")
                .withAuthorizationAsHyperIoTAdmin()
                .withParameter("userId",String.valueOf(sharedEntity.getUserId()))
                .build();
        HyperIoTHttpResponse response = HyperIoTHttpClient
                .hyperIoTHttpClient()
                .execute(request);
        HyperIoTHttpResponseValidator testValidator = HyperIoTHttpResponseValidatorBuilder
                .validatorBuilder()
                .withStatusEqual(200)
                .containExactProperties(expectedSharedEntityProperty())
                .build();
        boolean testSuccessful = testValidator.validateResponse(response);
        Assert.assertTrue(testSuccessful);
    }

    @Test
    public void test008_getUsersShouldSerializeResponseCorrectly(){
        AuthenticationApi authenticationApi  = getOsgiService(AuthenticationApi.class);
        HUser adminUser = (HUser) authenticationApi.login("hadmin","admin");
        SharedEntityExample sharedEntityExample = createSharedEntityExample();
        SharedEntity sharedEntity = createSharedEntity(sharedEntityExample,adminUser,adminUser);
        HyperIoTHttpRequest request = HyperIoTHttpRequestBuilder
                .get()
                .withUri(HyperIoTHttpUtils.SERVICE_BASE_URL.concat("/sharedentity/getUsers"))
                .withContentTypeHeader("application/json")
                .withAuthorizationAsHyperIoTAdmin()
                .withParameter("entityResourceName",String.valueOf(sharedEntity.getEntityResourceName()))
                .withParameter("entityId",String.valueOf(sharedEntity.getEntityId()))
                .build();
        HyperIoTHttpResponse response = HyperIoTHttpClient
                .hyperIoTHttpClient()
                .execute(request);
        List<String> expectedUserProperties = hUserEntityProperties();
        expectedUserProperties.addAll(hyperIoTAbstractEntityProperties());
        HyperIoTHttpResponseValidator testValidator = HyperIoTHttpResponseValidatorBuilder
                .validatorBuilder()
                .withStatusEqual(200)
                .containExactProperties(expectedUserProperties)
                .containExactInnerProperties("roles",hUserRoleProperties())
                .build();
        boolean testSuccessful = testValidator.validateResponse(response);
        Assert.assertTrue(testSuccessful);
    }


    /*
    *
    *  Utility methods for test.
    *
    */

    private List<String> hUserRoleProperties(){
        LinkedList<String> huserRoleProperties = new LinkedList<>();
        huserRoleProperties.add("name");
        huserRoleProperties.add("description");
        return huserRoleProperties;
    }

    private List<String> hUserEntityProperties(){
        List<String> huserEntityProperties = new ArrayList<>();
        huserEntityProperties.add("name");
        huserEntityProperties.add("lastname");
        huserEntityProperties.add("username");
        huserEntityProperties.add("admin");
        huserEntityProperties.add("email");
        huserEntityProperties.add("roles");
        huserEntityProperties.add("imagePath");
        return huserEntityProperties;
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

    private List<String> expectedSharedEntityProperty(){
        List<String> sharedEntityFields = new ArrayList<>();
        sharedEntityFields.add("entityResourceName");
        sharedEntityFields.add("entityId");
        sharedEntityFields.add("userId");
        return sharedEntityFields;
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
        huser.setActive(true);
        huser.setAdmin(false);
        Response restResponse = hUserRestApi.saveHUser(huser);
        Assert.assertEquals(200, restResponse.getStatus());
        return huser;
    }

    private SharedEntityExample createSharedEntityExample() {
        SharedEntityExampleRestApi sharedEntityExampleRestApi = getOsgiService(SharedEntityExampleRestApi.class);
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HUser adminUser = (HUser) authService.login("hadmin", "admin");
        Assert.assertNotNull(adminUser);
        Assert.assertTrue(adminUser.isAdmin());
        SharedEntityExample entityExample = new SharedEntityExample();
        entityExample.setName("Shared entity example " + java.util.UUID.randomUUID());
        entityExample.setDescription("Shared entity example of user: " + adminUser.getUsername());
        entityExample.setUser(adminUser);
        this.impersonateUser(sharedEntityExampleRestApi, adminUser);
        Response restResponse = sharedEntityExampleRestApi.saveSharedEntityExample(entityExample);
        Assert.assertEquals(200, restResponse.getStatus());
        Assert.assertNotEquals(0, ((SharedEntityExample) restResponse.getEntity()).getId());
        Assert.assertEquals(entityExample.getName(), ((SharedEntityExample) restResponse.getEntity()).getName());
        Assert.assertEquals("Shared entity example of user: " + adminUser.getUsername(),
                ((SharedEntityExample) restResponse.getEntity()).getDescription());
        Assert.assertEquals(adminUser.getId(), ((SharedEntityExample) restResponse.getEntity()).getUser().getId());
        return entityExample;
    }


    private SharedEntity createSharedEntity(HyperIoTBaseEntity hyperIoTBaseEntity, HUser ownerUser, HUser huser) {
        SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
        SharedEntity sharedEntity = new SharedEntity();
        sharedEntity.setEntityId(hyperIoTBaseEntity.getId());
        sharedEntity.setEntityResourceName(hyperIoTBaseEntity.getResourceName());
        sharedEntity.setUserId(huser.getId());
        this.impersonateUser(sharedEntityRestApi, ownerUser);
        Response restResponse = sharedEntityRestApi.saveSharedEntity(sharedEntity);
        Assert.assertEquals(200, restResponse.getStatus());
        Assert.assertEquals(hyperIoTBaseEntity.getId(), ((SharedEntity) restResponse.getEntity()).getEntityId());
        Assert.assertEquals(huser.getId(), ((SharedEntity) restResponse.getEntity()).getUserId());
        Assert.assertEquals(hyperIoTBaseEntity.getResourceName(), ((SharedEntity) restResponse.getEntity()).getEntityResourceName());
        return ((SharedEntity) restResponse.getEntity());
    }

    @After
    public void afterTest() {

        SharedEntityExampleSystemApi sharedEntityExampleSystemApi = getOsgiService(SharedEntityExampleSystemApi.class);
        SharedEntitySystemApi sharedEntitySystemApi = getOsgiService(SharedEntitySystemApi.class);
        PermissionSystemApi permissionSystemApi = getOsgiService(PermissionSystemApi.class);
        RoleSystemApi roleSystemApi = getOsgiService(RoleSystemApi.class);
        HUserSystemApi hUserSystemApi = getOsgiService(HUserSystemApi.class);


        HyperIoTTestUtils.truncateTables(sharedEntityExampleSystemApi, null);
        HyperIoTTestUtils.truncateTables(sharedEntitySystemApi, null);

        //DELETE Permission except permission relative to  Registered_Users
        List<String> roleNameNotToRemove = new LinkedList<>();
        roleNameNotToRemove.add(HyperIoTRoleConstants.ROLE_NAME_REGISTERED_USER);
        roleSystemApi.findAll(null, null)
                .stream()
                .filter(role -> (!roleNameNotToRemove.contains(role.getName())))
                .forEach(role -> {
                    permissionSystemApi.findByRole(role).stream().forEach(permission -> {
                        permissionSystemApi.remove(permission.getId(), null);
                    });
                });
        //DELETE ROLES EXCEPT Registered_Users
        HyperIoTTestUtils.truncateTables(roleSystemApi, (role -> (!roleNameNotToRemove.contains(role.getName()))));
        //Delete huser except admin.
        HyperIoTHUserTestUtils.truncateHUsers(hUserSystemApi);
    }

}
