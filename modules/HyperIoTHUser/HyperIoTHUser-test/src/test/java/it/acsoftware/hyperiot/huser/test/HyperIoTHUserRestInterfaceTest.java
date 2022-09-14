package it.acsoftware.hyperiot.huser.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.acsoftware.hyperiot.base.api.HyperIoTAction;
import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.base.api.HyperIoTUser;
import it.acsoftware.hyperiot.base.api.authentication.AuthenticationApi;
import it.acsoftware.hyperiot.base.service.rest.HyperIoTBaseRestApi;
import it.acsoftware.hyperiot.base.test.http.*;
import it.acsoftware.hyperiot.base.test.http.matcher.HyperIoTHttpResponseValidator;
import it.acsoftware.hyperiot.base.test.http.matcher.HyperIoTHttpResponseValidatorBuilder;
import it.acsoftware.hyperiot.base.util.HyperIoTUtil;
import it.acsoftware.hyperiot.huser.api.HUserRepository;
import it.acsoftware.hyperiot.huser.api.HUserSystemApi;
import it.acsoftware.hyperiot.huser.model.HUser;
import it.acsoftware.hyperiot.huser.model.HUserPasswordReset;
import it.acsoftware.hyperiot.huser.service.rest.HUserRestApi;
import it.acsoftware.hyperiot.huser.test.util.HyperIoTHUserTestUtils;
import it.acsoftware.hyperiot.permission.api.PermissionSystemApi;
import it.acsoftware.hyperiot.permission.model.Permission;
import it.acsoftware.hyperiot.permission.service.rest.PermissionRestApi;
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

import static it.acsoftware.hyperiot.huser.test.HyperIoTHUserConfiguration.*;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HyperIoTHUserRestInterfaceTest extends KarafTestSupport {

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
    public void test001_saveHUserShouldSerializeResponseCorrectly(){
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
        HyperIoTHttpRequest request = HyperIoTHttpRequestBuilder
                .post()
                .withUri(HyperIoTHttpUtils.SERVICE_BASE_URL.concat("/husers"))
                .withContentTypeHeader("application/json")
                .withAuthorizationAsHyperIoTAdmin()
                .withJsonBody(serializeHUserForRequest(huser))
                .build();
        List<String> expectedHuserProperties = hUserEntityProperties();
        expectedHuserProperties.addAll(hyperIoTAbstractEntityProperties());
        HyperIoTHttpResponse response = HyperIoTHttpClient
                .hyperIoTHttpClient()
                .execute(request);
        HyperIoTHttpResponseValidator testValidator = HyperIoTHttpResponseValidatorBuilder
                .validatorBuilder()
                .withStatusEqual(200)
                .containExactProperties(expectedHuserProperties)
                //Assert that user's roles list is empty.
                .containExactInnerProperties("roles", new LinkedList<>())
                .build();
        boolean testSuccessful = testValidator.validateResponse(response);
        Assert.assertTrue(testSuccessful);
    }

    @Test
    public void test002_findHUserWhenUserHasNotRolesShouldSerializeResponseCorrectly(){
        HUser user = createHUser();
        Assert.assertNotEquals(0, user.getId());
        HyperIoTHttpRequest request = HyperIoTHttpRequestBuilder
                .get()
                .withUri(HyperIoTHttpUtils.SERVICE_BASE_URL.concat("/husers/").concat(String.valueOf(user.getId())))
                .withContentTypeHeader("application/json")
                .withAuthorizationAsHyperIoTAdmin()
                .build();
        List<String> expectedHuserProperties = hUserEntityProperties();
        expectedHuserProperties.addAll(hyperIoTAbstractEntityProperties());
        HyperIoTHttpResponse response = HyperIoTHttpClient
                .hyperIoTHttpClient()
                .execute(request);
        HyperIoTHttpResponseValidator testValidator = HyperIoTHttpResponseValidatorBuilder
                .validatorBuilder()
                .withStatusEqual(200)
                .containExactProperties(expectedHuserProperties)
                //Assert that user's roles list is empty.
                .containExactInnerProperties("roles", new LinkedList<>())
                .build();
        boolean testSuccessful = testValidator.validateResponse(response);
        Assert.assertTrue(testSuccessful);
    }

    @Test
    public void test003_findHUserWhenUserHasRolesShouldSerializeResponseCorrectly(){
        HUser user = huserWithDefaultPermissionInHyperIoTFramework(true);
        Assert.assertNotEquals(0, user.getId());
        HyperIoTHttpRequest request = HyperIoTHttpRequestBuilder
                .get()
                .withUri(HyperIoTHttpUtils.SERVICE_BASE_URL.concat("/husers/").concat(String.valueOf(user.getId())))
                .withContentTypeHeader("application/json")
                .withAuthorizationAsHyperIoTAdmin()
                .build();
        List<String> expectedHuserProperties = hUserEntityProperties();
        expectedHuserProperties.addAll(hyperIoTAbstractEntityProperties());
        List<String> expectedHuserRoleProperties = hUserRoleProperties();
        expectedHuserRoleProperties.addAll(hyperIoTAbstractEntityProperties());
        HyperIoTHttpResponse response = HyperIoTHttpClient
                .hyperIoTHttpClient()
                .execute(request);
        HyperIoTHttpResponseValidator testValidator = HyperIoTHttpResponseValidatorBuilder
                .validatorBuilder()
                .withStatusEqual(200)
                .containExactProperties(expectedHuserProperties)
                .containExactInnerProperties("roles", expectedHuserRoleProperties)
                .build();
        boolean testSuccessful = testValidator.validateResponse(response);
        Assert.assertTrue(testSuccessful);
    }

    @Test
    public void test004_registerHUserShouldSerializeResponseCorrectly(){
        String username = "TestUser";
        HUser huser = new HUser();
        huser.setName("name");
        huser.setLastname("lastname");
        huser.setUsername(username + UUID.randomUUID().toString().replaceAll("-", ""));
        huser.setEmail("testusername" + UUID.randomUUID().toString() + "@hyperiot.com");
        huser.setPassword("passwordPass&01");
        huser.setPasswordConfirm("passwordPass&01");
        HyperIoTHttpRequest request = HyperIoTHttpRequestBuilder
                .post()
                .withUri(HyperIoTHttpUtils.SERVICE_BASE_URL.concat("/husers/register"))
                .withContentTypeHeader("application/json")
                .withAuthorizationAsHyperIoTAdmin()
                .withJsonBody(serializeHUserForRequest(huser))
                .build();
        List<String> expectedHuserProperties = hUserEntityProperties();
        expectedHuserProperties.addAll(hyperIoTAbstractEntityProperties());
        HyperIoTHttpResponse response = HyperIoTHttpClient
                .hyperIoTHttpClient()
                .execute(request);
        HyperIoTHttpResponseValidator testValidator = HyperIoTHttpResponseValidatorBuilder
                .validatorBuilder()
                .withStatusEqual(200)
                .containExactProperties(expectedHuserProperties)
                //Assert that user's roles list is empty.
                .containExactInnerProperties("roles", new LinkedList<>())
                .build();
        boolean testSuccessful = testValidator.validateResponse(response);
        Assert.assertTrue(testSuccessful);
    }

    @Test
    public void test005_activateHUserShouldSerializeResponseCorrectly(){
        HUser huser = registerHUser();
        String requestUri = HyperIoTHttpUtils.SERVICE_BASE_URL.concat("/husers/activate").
                concat("?email=").concat(huser.getEmail()).
                concat("&code=").concat(huser.getActivateCode());
        HyperIoTHttpRequest request = HyperIoTHttpRequestBuilder
                .post()
                .withUri(requestUri)
                .withParameter("email", huser.getEmail())
                .withParameter("code",huser.getActivateCode())
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
    public void test006_resetPasswordRequestShouldSerializeResponseCorrectly(){
        HUser huser = createHUser();
        String requestUri = HyperIoTHttpUtils.SERVICE_BASE_URL
                        .concat("/husers/resetPasswordRequest")
                        .concat("?email=").concat(huser.getEmail());
        HyperIoTHttpRequest request = HyperIoTHttpRequestBuilder
                .post()
                .withUri(requestUri)
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
    public void test007_resetPasswordShouldSerializeResponseCorrectly() throws JsonProcessingException {
        HUser huser = huserWithDefaultPermissionInHyperIoTFramework(true);
        HUserPasswordReset passwordReset = new HUserPasswordReset();
        String newPassword = "passwordPass&01?";
        passwordReset.setEmail(huser.getEmail());
        passwordReset.setResetCode(findHUserPasswordResetCode(huser));
        passwordReset.setPassword(newPassword);
        passwordReset.setPasswordConfirm(newPassword);
        String requestBody = new ObjectMapper().writeValueAsString(passwordReset);
        String requestUri = HyperIoTHttpUtils.SERVICE_BASE_URL
                .concat("/husers/resetPassword");
        HyperIoTHttpRequest request = HyperIoTHttpRequestBuilder
                .post()
                .withUri(requestUri)
                .withAuthorization(huser.getUsername(), "passwordPass&01")
                .withJsonBody(requestBody)
                .build();
        List<String> expectedHuserProperties = hUserEntityProperties();
        expectedHuserProperties.addAll(hyperIoTAbstractEntityProperties());
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
    public void test008_updateHUserWhenUserHasNotRolesShouldSerializeResponseCorrectly(){
        HUser user = createHUser();
        Assert.assertNotEquals(0, user.getId());
        user.setName("NewName".concat(UUID.randomUUID().toString().replaceAll("-","")));
        HyperIoTHttpRequest request = HyperIoTHttpRequestBuilder
                .put()
                .withUri(HyperIoTHttpUtils.SERVICE_BASE_URL.concat("/husers"))
                .withContentTypeHeader("application/json")
                .withAuthorizationAsHyperIoTAdmin()
                .withJsonBody(serializeHUserForRequest(user))
                .build();
        List<String> expectedHuserProperties = hUserEntityProperties();
        expectedHuserProperties.addAll(hyperIoTAbstractEntityProperties());
        HyperIoTHttpResponse response = HyperIoTHttpClient
                .hyperIoTHttpClient()
                .execute(request);
        HyperIoTHttpResponseValidator testValidator = HyperIoTHttpResponseValidatorBuilder
                .validatorBuilder()
                .withStatusEqual(200)
                .containExactProperties(expectedHuserProperties)
                //Assert that user's roles list is empty.
                .containExactInnerProperties("roles", new LinkedList<>())
                .build();
        boolean testSuccesful = testValidator.validateResponse(response);
        Assert.assertTrue(testSuccesful);
    }

    @Test
    public void test009_updateHUserWhenUserHasRolesShouldSerializeResponseCorrectly(){
        HUser user = huserWithDefaultPermissionInHyperIoTFramework(true);
        Assert.assertNotEquals(0, user.getId());
        user.setName("NewName".concat(UUID.randomUUID().toString().replaceAll("-","")));
        HyperIoTHttpRequest request = HyperIoTHttpRequestBuilder
                .put()
                .withUri(HyperIoTHttpUtils.SERVICE_BASE_URL.concat("/husers"))
                .withContentTypeHeader("application/json")
                .withAuthorizationAsHyperIoTAdmin()
                .withJsonBody(serializeHUserForRequest(user))
                .build();
        List<String> expectedHuserProperties = hUserEntityProperties();
        expectedHuserProperties.addAll(hyperIoTAbstractEntityProperties());
        List<String> expectedHuserRoleProperties = hUserRoleProperties();
        expectedHuserRoleProperties.addAll(hyperIoTAbstractEntityProperties());
        HyperIoTHttpResponse response = HyperIoTHttpClient
                .hyperIoTHttpClient()
                .execute(request);
        HyperIoTHttpResponseValidator testValidator = HyperIoTHttpResponseValidatorBuilder
                .validatorBuilder()
                .withStatusEqual(200)
                .containExactProperties(expectedHuserProperties)
                .containExactInnerProperties("roles", expectedHuserRoleProperties)
                .build();
        boolean testSuccessful = testValidator.validateResponse(response);
        Assert.assertTrue(testSuccessful);
    }


    @Test
    public void test010_updateAccountInfoWhenUserHasNotRolesShouldSerializeResponseCorrectly(){
        HUser user = createHUser();
        Assert.assertNotEquals(0, user.getId());
        user.setName("updateNameAccount");
        user.setLastname("updateLastnameAccount");
        user.setEmail("updateemail@hyperiot.com");
        HyperIoTHttpRequest request = HyperIoTHttpRequestBuilder
                .put()
                .withUri(HyperIoTHttpUtils.SERVICE_BASE_URL.concat("/husers").concat("/account"))
                .withContentTypeHeader("application/json")
                .withAuthorization(user.getUsername(),"passwordPass&01")
                .withJsonBody(serializeHUserForRequest(user))
                .build();
        List<String> expectedHuserProperties = hUserEntityProperties();
        expectedHuserProperties.addAll(hyperIoTAbstractEntityProperties());
        HUserRestApi hUserRestApi = getOsgiService(HUserRestApi.class);
        impersonateUser(hUserRestApi,user);
        HyperIoTHttpResponse response = HyperIoTHttpClient
                .hyperIoTHttpClient()
                .execute(request);
        HyperIoTHttpResponseValidator testValidator = HyperIoTHttpResponseValidatorBuilder
                .validatorBuilder()
                .withStatusEqual(200)
                .containExactProperties(expectedHuserProperties)
                //Assert that user's roles list is empty.
                .containExactInnerProperties("roles", new LinkedList<>())
                .build();
        boolean testSuccesful = testValidator.validateResponse(response);
        Assert.assertTrue(testSuccesful);
    }

    @Test
    public void test011_updateAccountInfoWhenUserHasRolesShouldSerializeResponseCorrectly(){
        HUser user = createHUser();
        Assert.assertNotEquals(0, user.getId());
        user.setName("updateNameAccount");
        user.setLastname("updateLastnameAccount");
        user.setEmail("updateemail@hyperiot.com");
        HyperIoTHttpRequest request = HyperIoTHttpRequestBuilder
                .put()
                .withUri(HyperIoTHttpUtils.SERVICE_BASE_URL.concat("/husers").concat("/account"))
                .withContentTypeHeader("application/json")
                .withAuthorization(user.getUsername(),"passwordPass&01")
                .withJsonBody(serializeHUserForRequest(user))
                .build();
        List<String> expectedHuserProperties = hUserEntityProperties();
        expectedHuserProperties.addAll(hyperIoTAbstractEntityProperties());
        List<String> expectedHuserRoleProperties = hUserRoleProperties();
        expectedHuserRoleProperties.addAll(hyperIoTAbstractEntityProperties());
        HUserRestApi hUserRestApi = getOsgiService(HUserRestApi.class);
        impersonateUser(hUserRestApi,user);
        HyperIoTHttpResponse response = HyperIoTHttpClient
                .hyperIoTHttpClient()
                .execute(request);
        HyperIoTHttpResponseValidator testValidator = HyperIoTHttpResponseValidatorBuilder
                .validatorBuilder()
                .withStatusEqual(200)
                .containExactProperties(expectedHuserProperties)
                .containExactInnerProperties("roles", expectedHuserRoleProperties)
                .build();
        boolean testSuccessful = testValidator.validateResponse(response);
        Assert.assertTrue(testSuccessful);
    }

    @Test
    public void test012_changeHUserPasswordWhenUserHasNotRolesShouldSerializeResponseCorrectly(){
        HUser user = createHUser();
        Assert.assertNotEquals(0, user.getId());
        String newPassword = "passwordPass&01".concat("2");
        HyperIoTHttpRequest request = HyperIoTHttpRequestBuilder
                .put()
                .withUri(HyperIoTHttpUtils.SERVICE_BASE_URL.concat("/husers").concat("/password"))
                .withContentTypeHeader("application/x-www-form-urlencoded")
                .withAuthorization(user.getUsername(),"passwordPass&01")
                .withParameter("userId",String.valueOf(user.getId()))
                .withParameter("oldPassword","passwordPass&01")
                .withParameter("newPassword", newPassword)
                .withParameter("passwordConfirm",newPassword)
                .build();
        List<String> expectedHuserProperties = hUserEntityProperties();
        expectedHuserProperties.addAll(hyperIoTAbstractEntityProperties());
        HUserRestApi hUserRestApi = getOsgiService(HUserRestApi.class);
        impersonateUser(hUserRestApi,user);
        HyperIoTHttpResponse response = HyperIoTHttpClient
                .hyperIoTHttpClient()
                .execute(request);
        HyperIoTHttpResponseValidator testValidator = HyperIoTHttpResponseValidatorBuilder
                .validatorBuilder()
                .withStatusEqual(200)
                .containExactProperties(expectedHuserProperties)
                //Assert that user's roles list is empty.
                .containExactInnerProperties("roles", new LinkedList<>())
                .build();
        boolean testSuccesful = testValidator.validateResponse(response);
        Assert.assertTrue(testSuccesful);
    }

    @Test
    public void test013_changeHUserPasswordWhenUserHasRolesShouldSerializeResponseCorrectly(){
        HUser user = huserWithDefaultPermissionInHyperIoTFramework(true);
        Assert.assertNotEquals(0, user.getId());
        String newPassword = "passwordPass&01".concat("2");
        HyperIoTHttpRequest request = HyperIoTHttpRequestBuilder
                .put()
                .withUri(HyperIoTHttpUtils.SERVICE_BASE_URL.concat("/husers").concat("/password"))
                .withAuthorization(user.getUsername(),"passwordPass&01")
                .withContentTypeHeader("application/x-www-form-urlencoded")
                .withParameter("userId",String.valueOf(user.getId()))
                .withParameter("oldPassword","passwordPass&01")
                .withParameter("newPassword", newPassword)
                .withParameter("passwordConfirm",newPassword)
                .build();
        List<String> expectedHuserProperties = hUserEntityProperties();
        expectedHuserProperties.addAll(hyperIoTAbstractEntityProperties());
        List<String> expectedHuserRoleProperties = hUserRoleProperties();
        expectedHuserRoleProperties.addAll(hyperIoTAbstractEntityProperties());
        HUserRestApi hUserRestApi = getOsgiService(HUserRestApi.class);
        impersonateUser(hUserRestApi,user);
        HyperIoTHttpResponse response = HyperIoTHttpClient
                .hyperIoTHttpClient()
                .execute(request);
        HyperIoTHttpResponseValidator testValidator = HyperIoTHttpResponseValidatorBuilder
                .validatorBuilder()
                .withStatusEqual(200)
                .containExactProperties(expectedHuserProperties)
                .containExactInnerProperties("roles", expectedHuserRoleProperties)
                .build();
        boolean testSuccessful = testValidator.validateResponse(response);
        Assert.assertTrue(testSuccessful);
    }

    @Test
    public void test014_deleteHUserShouldSerializeResponseCorrectly(){
        HUser user = createHUser();
        Assert.assertNotEquals(0, user.getId());
        HyperIoTHttpRequest request = HyperIoTHttpRequestBuilder
                .delete()
                .withUri(HyperIoTHttpUtils.SERVICE_BASE_URL.concat("/husers/").concat(String.valueOf(user.getId())))
                .withAuthorizationAsHyperIoTAdmin()
                .build();
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
    public void test015_findAllHUserSerializeResponseCorrectly(){
        HUser user = createHUser();
        Assert.assertNotEquals(0, user.getId());
        HUser user2 = createHUser();
        Assert.assertNotEquals(0, user2.getId());
        HyperIoTHttpRequest request = HyperIoTHttpRequestBuilder
                .get()
                .withUri(HyperIoTHttpUtils.SERVICE_BASE_URL.concat("/husers/all"))
                .withContentTypeHeader("application/json")
                .withAuthorizationAsHyperIoTAdmin()
                .build();
        List<String> expectedHuserProperties = hUserEntityProperties();
        expectedHuserProperties.addAll(hyperIoTAbstractEntityProperties());
        HyperIoTHttpResponse response = HyperIoTHttpClient
                .hyperIoTHttpClient()
                .execute(request);
        HyperIoTHttpResponseValidator testValidator = HyperIoTHttpResponseValidatorBuilder
                .validatorBuilder()
                .withStatusEqual(200)
                .containExactProperties(expectedHuserProperties)
                .containExactInnerProperties("roles", new LinkedList<>())
                .build();
        boolean testSuccessful = testValidator.validateResponse(response);
        Assert.assertTrue(testSuccessful);
    }

    @Test
    public void test016_findAllHUserPaginatedSerializeResponseCorrectly(){
        HUser user = createHUser();
        Assert.assertNotEquals(0, user.getId());
        HUser user2 = createHUser();
        Assert.assertNotEquals(0, user2.getId());
        HyperIoTHttpRequest request = HyperIoTHttpRequestBuilder
                .get()
                .withUri(HyperIoTHttpUtils.SERVICE_BASE_URL.concat("/husers"))
                .withContentTypeHeader("application/json")
                .withAuthorizationAsHyperIoTAdmin()
                .withParameter("page",String.valueOf(defaultPage))
                .withParameter("delta",String.valueOf(defaultDelta))
                .build();
        List<String> expectedHuserProperties = hUserEntityProperties();
        expectedHuserProperties.addAll(hyperIoTAbstractEntityProperties());
        HyperIoTHttpResponse response = HyperIoTHttpClient
                .hyperIoTHttpClient()
                .execute(request);
        HyperIoTHttpResponseValidator testValidator = HyperIoTHttpResponseValidatorBuilder
                .validatorBuilder()
                .withStatusEqual(200)
                .containExactHyperIoTPaginatedProperties()
                .containExactInnerProperties("results",expectedHuserProperties)
                .containExactInnerProperties("results.roles", new LinkedList<>())
                .build();
        boolean testSuccessful = testValidator.validateResponse(response);
        Assert.assertTrue(testSuccessful);
    }

    @Test
    public void test017_requestAccountDeletionSerializeResponseCorrectly(){
        String requestUri = HyperIoTHttpUtils.SERVICE_BASE_URL.concat("/husers/account/deletioncode");
        HyperIoTHttpRequest request = HyperIoTHttpRequestBuilder
                .put()
                .withUri(requestUri)
                .withContentTypeHeader("application/json")
                .withAuthorizationAsHyperIoTAdmin()
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
    public void test018_deleteAccountSerializeResponseCorrectly() {
        HUser huser = huserWithDefaultPermissionInHyperIoTFramework(true);
        Assert.assertNotEquals(0, huser.getId());
        HUserRestApi hUserRestApi = getOsgiService(HUserRestApi.class);
        impersonateUser(hUserRestApi, huser);
        String huserDeletionCode = UUID.randomUUID().toString();
        //Set user deletion code directly from HUserRepository such that we need to know the deletion code to perform request(Raw deletion code is sent to user through email).
        HUserRepository hUserRepository = getOsgiService(HUserRepository.class);
        HUser user = hUserRepository.find(huser.getId(), null);
        Assert.assertEquals(user.getId(), huser.getId());
        Assert.assertNull(huser.getDeletionCode());
        user.setDeletionCode(HyperIoTUtil.encodeRawString(huserDeletionCode));
        HUser userWithDeletionCode = hUserRepository.update(user);
        Assert.assertNotNull(userWithDeletionCode.getDeletionCode());
        Assert.assertTrue(HyperIoTUtil.matchesEncoding(huserDeletionCode, userWithDeletionCode.getDeletionCode()));
        String requestUri = HyperIoTHttpUtils.SERVICE_BASE_URL.concat("/husers/account/");
        HyperIoTHttpRequest request = HyperIoTHttpRequestBuilder
                .delete(requestUri)
                .withUri(requestUri)
                .withContentTypeHeader("application/x-www-form-urlencoded")
                .withAuthorization(huser.getUsername(), "passwordPass&01")
                .withParameter("userId", String.valueOf(huser.getId()))
                .withParameter("accountDeletionCode", huserDeletionCode)
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



    /*
     *
     *
     * UTILITY METHODS
     *
     *
     */

    private String serializeHUserForRequest(HUser user)  {
        //We need to remove the screenName field from the user ,
        //Otherwise the HyperIoTBaseRestApi's objectMapper fail to deserialize the request body because he found an unrecognized field(screenName).
        ObjectMapper mapper = new ObjectMapper();
        try {
            String hUser = mapper.writeValueAsString(user);
            JsonNode node = mapper.readTree(hUser);
            Assert.assertTrue(node instanceof ObjectNode);
            ObjectNode objectNode = ((ObjectNode) node);
            if(objectNode.has("screenName")){
                objectNode.remove("screenName");
            }
            Assert.assertFalse(objectNode.has("screenName"));
            return mapper.writeValueAsString(objectNode);
        } catch (Exception e){
            Assert.fail("Fail hUserSerialization");
            throw new RuntimeException();
        }
    }

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

    private String findHUserPasswordResetCode(HUser huser){
        HUserRestApi hUserRestService = getOsgiService(HUserRestApi.class);
        impersonateUser(hUserRestService,huser);
        Response passwordResetResponse = hUserRestService.resetPasswordRequest(huser.getEmail());
        HUserRepository hUserRepository = getOsgiService(HUserRepository.class);
        return hUserRepository.find(huser.getId(),null).getPasswordResetCode();
    }

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
        huser.setActive(true);
        huser.setAdmin(false);
        Response restResponse = hUserRestApi.saveHUser(huser);
        Assert.assertEquals(200, restResponse.getStatus());
        Assert.assertNotEquals(0, ((HUser) restResponse.getEntity()).getId());
        Assert.assertEquals("name", ((HUser) restResponse.getEntity()).getName());
        Assert.assertEquals("lastname", ((HUser) restResponse.getEntity()).getLastname());
        Assert.assertEquals(huser.getUsername(), ((HUser) restResponse.getEntity()).getUsername());
        Assert.assertEquals(huser.getEmail(), ((HUser) restResponse.getEntity()).getEmail());
        Assert.assertFalse(huser.isAdmin());
        Assert.assertTrue(huser.isActive());
        if (action != null) {
            Role role = createRole();
            huser.addRole(role);
            RoleRestApi roleRestApi = getOsgiService(RoleRestApi.class);
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
            return null;
        } else {
            PermissionSystemApi permissionSystemApi = getOsgiService(PermissionSystemApi.class);
            Permission testPermission = permissionSystemApi.findByRoleAndResourceName(role, action.getResourceName());
            if (testPermission == null) {
                Permission permission = new Permission();
                permission.setName(huserResourceName + " assigned to huser_id " + huser.getId());
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

    private HUser requestResetPassword() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        this.impersonateUser(huserRestService, adminUser);
        String username = "TestUser";
        HUser huser = new HUser();
        huser.setName("name");
        huser.setLastname("lastname");
        huser.setUsername(username + UUID.randomUUID().toString().replaceAll("-", ""));
        huser.setEmail("testusername" + UUID.randomUUID().toString() + "@hyperiot.com");
        huser.setPassword("passwordPass01/");
        huser.setPasswordConfirm("passwordPass01/");
        huser.setActive(true);
        huser.setAdmin(false);
        huser.setPasswordResetCode(java.util.UUID.randomUUID().toString());
        Response restResponse = huserRestService.saveHUser(huser);
        Assert.assertEquals(200, restResponse.getStatus());
        return huser;
    }

    private HUser registerHUser() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        this.impersonateUser(huserRestService, null);
        HUser huser = new HUser();
        huser.setName("Mark");
        huser.setLastname("Norris");
        huser.setUsername(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
        huser.setEmail(java.util.UUID.randomUUID() + "@hyperiot.com");
        huser.setActive(false);
        huser.setPassword("passwordPass&01");
        huser.setPasswordConfirm("passwordPass&01");
        Assert.assertNull(huser.getActivateCode());
        Response restResponse = huserRestService.register(huser);
        Assert.assertEquals(200, restResponse.getStatus());
        return huser;
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


    @After
    public void afterTest() {
        HUserSystemApi hUserSystemApi = getOsgiService(HUserSystemApi.class);
        RoleSystemApi roleSystemApi = getOsgiService(RoleSystemApi.class);
        HyperIoTHUserTestUtils.truncateHUsers(hUserSystemApi);
        HyperIoTHUserTestUtils.truncateRoles(roleSystemApi);
    }
}
