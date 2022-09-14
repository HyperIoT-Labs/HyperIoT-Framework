package it.acsoftware.hyperiot.permission.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.acsoftware.hyperiot.base.action.util.HyperIoTActionsUtil;
import it.acsoftware.hyperiot.base.action.util.HyperIoTCrudAction;
import it.acsoftware.hyperiot.base.api.HyperIoTAction;
import it.acsoftware.hyperiot.base.api.HyperIoTUser;
import it.acsoftware.hyperiot.base.api.authentication.AuthenticationApi;
import it.acsoftware.hyperiot.base.service.rest.HyperIoTBaseRestApi;
import it.acsoftware.hyperiot.base.test.http.*;
import it.acsoftware.hyperiot.base.test.http.matcher.HyperIoTHttpResponseValidator;
import it.acsoftware.hyperiot.base.test.http.matcher.HyperIoTHttpResponseValidatorBuilder;
import it.acsoftware.hyperiot.huser.api.HUserSystemApi;
import it.acsoftware.hyperiot.huser.model.HUser;
import it.acsoftware.hyperiot.huser.test.util.HyperIoTHUserTestUtils;
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

import static it.acsoftware.hyperiot.permission.test.HyperIoTPermissionConfiguration.*;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HyperIoTPermissionRestInterfaceTest extends KarafTestSupport {


    public Option[] config() {
        return null;
    }

    public void impersonateUser(HyperIoTBaseRestApi restApi, HyperIoTUser user) {
        restApi.impersonate(user);
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
        assertContains("hyperiot", datasource);
    }

    @Test
    public void test001_savePermissionShouldSerializeResponseCorrectly(){
        Role role = createRole();
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(permissionResourceName,
                HyperIoTCrudAction.FIND);
        Permission permission = new Permission();
        permission.setName(permissionResourceName + " by role_id " + role.getId());
        permission.setActionIds(action.getActionId());
        permission.setEntityResourceName(action.getResourceName());
        permission.setRole(role);
        HyperIoTHttpRequest request = HyperIoTHttpRequestBuilder
                .post()
                .withUri(HyperIoTHttpUtils.SERVICE_BASE_URL.concat("/permissions"))
                .withContentTypeHeader("application/json")
                .withAuthorizationAsHyperIoTAdmin()
                .withJsonBody(permission)
                .build();
        List<String> expectedPermissionProperties = permissionProperties();
        expectedPermissionProperties.addAll(hyperIoTAbstractEntityProperties());
        //expectedPermissionRoleProperties is the property we expect on Role reference in Permission entity.
        List<String> expectedPermissionRoleProperties = permissionRoleProperties();
        expectedPermissionRoleProperties.addAll(hyperIoTAbstractEntityProperties());
        HyperIoTHttpResponse response = HyperIoTHttpClient
                .hyperIoTHttpClient()
                .execute(request);
        HyperIoTHttpResponseValidator testValidator = HyperIoTHttpResponseValidatorBuilder
                .validatorBuilder()
                .withStatusEqual(200)
                .containExactProperties(expectedPermissionProperties)
                .containExactInnerProperties("role",expectedPermissionRoleProperties)
                .build();
        boolean testSuccessful = testValidator.validateResponse(response);
        Assert.assertTrue(testSuccessful);
    }

    @Test
    public void test002_findPermissionShouldSerializeResponseCorrectly(){
        Permission permission = createPermission();
        Assert.assertNotEquals(0, permission.getId());
        HyperIoTHttpRequest request = HyperIoTHttpRequestBuilder
                .get()
                .withUri(HyperIoTHttpUtils.SERVICE_BASE_URL.concat("/permissions/").concat(String.valueOf(permission.getId())))
                .withContentTypeHeader("application/json")
                .withAuthorizationAsHyperIoTAdmin()
                .withJsonBody(permission)
                .build();
        List<String> expectedPermissionProperties = permissionProperties();
        expectedPermissionProperties.addAll(hyperIoTAbstractEntityProperties());
        //expectedPermissionRoleProperties is the property we expect on Role reference in Permission entity.
        List<String> expectedPermissionRoleProperties = permissionRoleProperties();
        expectedPermissionRoleProperties.addAll(hyperIoTAbstractEntityProperties());
        HyperIoTHttpResponse response = HyperIoTHttpClient
                .hyperIoTHttpClient()
                .execute(request);
        HyperIoTHttpResponseValidator testValidator = HyperIoTHttpResponseValidatorBuilder
                .validatorBuilder()
                .withStatusEqual(200)
                .containExactProperties(expectedPermissionProperties)
                .containExactInnerProperties("role",expectedPermissionRoleProperties)
                .build();
        boolean testSuccessful = testValidator.validateResponse(response);
        Assert.assertTrue(testSuccessful);
    }

    @Test
    public void test003_updatePermissionShouldSerializeResponseCorrectly(){
        Permission permission = createPermission();
        Assert.assertNotEquals(0, permission.getId());
        permission.setName("Name".concat(UUID.randomUUID().toString().replaceAll("-","")));
        HyperIoTHttpRequest request = HyperIoTHttpRequestBuilder
                .put()
                .withUri(HyperIoTHttpUtils.SERVICE_BASE_URL.concat("/permissions"))
                .withContentTypeHeader("application/json")
                .withAuthorizationAsHyperIoTAdmin()
                .withJsonBody(permission)
                .build();
        List<String> expectedPermissionProperties = permissionProperties();
        expectedPermissionProperties.addAll(hyperIoTAbstractEntityProperties());
        //expectedPermissionRoleProperties is the property we expect on Role reference in Permission entity.
        List<String> expectedPermissionRoleProperties = permissionRoleProperties();
        expectedPermissionRoleProperties.addAll(hyperIoTAbstractEntityProperties());
        HyperIoTHttpResponse response = HyperIoTHttpClient
                .hyperIoTHttpClient()
                .execute(request);
        HyperIoTHttpResponseValidator testValidator = HyperIoTHttpResponseValidatorBuilder
                .validatorBuilder()
                .withStatusEqual(200)
                .containExactProperties(expectedPermissionProperties)
                .containExactInnerProperties("role",expectedPermissionRoleProperties)
                .build();
        boolean testSuccessful = testValidator.validateResponse(response);
        Assert.assertTrue(testSuccessful);
    }

    @Test
    public void test004_deletePermissionShouldSerializeResponseCorrectly(){
        Permission permission = createPermission();
        Assert.assertNotEquals(0, permission.getId());
        HyperIoTHttpRequest request = HyperIoTHttpRequestBuilder
                .delete()
                .withUri(HyperIoTHttpUtils.SERVICE_BASE_URL.concat("/permissions/").concat(String.valueOf(permission.getId())))
                .withContentTypeHeader("application/json")
                .withAuthorizationAsHyperIoTAdmin()
                .withJsonBody(permission)
                .build();
        HyperIoTHttpResponse response = HyperIoTHttpClient
                .hyperIoTHttpClient()
                .execute(request);
        HyperIoTHttpResponseValidator testValidator = HyperIoTHttpResponseValidatorBuilder
                .validatorBuilder()
                .withStatusEqual(200)
                .withCustomCriteria(hyperIoTHttpResponse -> response.getResponseBody().isEmpty())
                .build();
        boolean testSuccessful = testValidator.validateResponse(response);
        Assert.assertTrue(testSuccessful);
    }

    @Test
    public void test005_findAllPermissionShouldSerializeResponseCorrectly(){
        Permission permission = createPermission();
        Assert.assertNotEquals(0, permission.getId());
        Permission permission2 = createPermission();
        Assert.assertNotEquals(0, permission2.getId());
        HyperIoTHttpRequest request = HyperIoTHttpRequestBuilder
                .get()
                .withUri(HyperIoTHttpUtils.SERVICE_BASE_URL.concat("/permissions/all"))
                .withContentTypeHeader("application/json")
                .withAuthorizationAsHyperIoTAdmin()
                .build();
        List<String> expectedPermissionProperties = permissionProperties();
        expectedPermissionProperties.addAll(hyperIoTAbstractEntityProperties());
        //expectedPermissionRoleProperties is the property we expect on Role reference in Permission entity.
        List<String> expectedPermissionRoleProperties = permissionRoleProperties();
        expectedPermissionRoleProperties.addAll(hyperIoTAbstractEntityProperties());
        HyperIoTHttpResponse response = HyperIoTHttpClient
                .hyperIoTHttpClient()
                .execute(request);
        HyperIoTHttpResponseValidator testValidator = HyperIoTHttpResponseValidatorBuilder
                .validatorBuilder()
                .withStatusEqual(200)
                .containExactProperties(expectedPermissionProperties)
                .containExactInnerProperties("role",expectedPermissionRoleProperties)
                .build();
        boolean testSuccessful = testValidator.validateResponse(response);
        Assert.assertTrue(testSuccessful);
    }

    @Test
    public void test006_findAllPermissionPaginatedShouldSerializeResponseCorrectly(){
        Permission permission = createPermission();
        Assert.assertNotEquals(0, permission.getId());
        Permission permission2 = createPermission();
        Assert.assertNotEquals(0, permission2.getId());
        HyperIoTHttpRequest request = HyperIoTHttpRequestBuilder
                .get()
                .withUri(HyperIoTHttpUtils.SERVICE_BASE_URL.concat("/permissions"))
                .withContentTypeHeader("application/json")
                .withAuthorizationAsHyperIoTAdmin()
                .withParameter("delta",String.valueOf(defaultDelta))
                .withParameter("page",String.valueOf(defaultPage))
                .build();
        List<String> expectedPermissionProperties = permissionProperties();
        expectedPermissionProperties.addAll(hyperIoTAbstractEntityProperties());
        //expectedPermissionRoleProperties is the property we expect on Role reference in Permission entity.
        List<String> expectedPermissionRoleProperties = permissionRoleProperties();
        expectedPermissionRoleProperties.addAll(hyperIoTAbstractEntityProperties());
        HyperIoTHttpResponse response = HyperIoTHttpClient
                .hyperIoTHttpClient()
                .execute(request);
        HyperIoTHttpResponseValidator testValidator = HyperIoTHttpResponseValidatorBuilder
                .validatorBuilder()
                .withStatusEqual(200)
                .containExactHyperIoTPaginatedProperties()
                .containExactInnerProperties("results",expectedPermissionProperties)
                .containExactInnerProperties("results.role",expectedPermissionRoleProperties)
                .build();
        boolean testSuccessful = testValidator.validateResponse(response);
        Assert.assertTrue(testSuccessful);
    }

    @Test
    public void test007_findAllActionsShouldSerializeResponseCorrectly(){
        HyperIoTHttpRequest request = HyperIoTHttpRequestBuilder
                .get()
                .withUri(HyperIoTHttpUtils.SERVICE_BASE_URL.concat("/permissions/actions"))
                .withContentTypeHeader("application/json")
                .withAuthorizationAsHyperIoTAdmin()
                .build();
        HyperIoTHttpResponse response = HyperIoTHttpClient
                .hyperIoTHttpClient()
                .execute(request);
        HyperIoTHttpResponseValidator testValidator = HyperIoTHttpResponseValidatorBuilder
                .validatorBuilder()
                .withStatusEqual(200)
                //Assert that we can deserialize the response body in a HashMap of HyperIoTAction.
                .withCustomCriteria(hyperIoTHttpResponse -> {
                  ObjectMapper mapper = new ObjectMapper();
                  LinkedHashMap<String,List<HyperIoTAction>> mapActionList ;
                  try {
                      mapActionList = mapper.readValue(response.getResponseBody(), LinkedHashMap.class);
                  }catch ( Exception e){
                      return false;
                  }
                  return true;
                })
                .build();
        boolean testSuccessful = testValidator.validateResponse(response);
        Assert.assertTrue(testSuccessful);
    }

    /*
    *
    *
    *   UTILITY METHODS.
    *
    *
    */

    private List<String> permissionProperties(){
        List<String> permissionProperties = new ArrayList<>();
        permissionProperties.add("name");
        permissionProperties.add("actionIds");
        permissionProperties.add("entityResourceName");
        permissionProperties.add("resourceId");
        permissionProperties.add("role");
        return permissionProperties;
    }

    private List<String> permissionRoleProperties(){
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

    @After
    public void afterTest() {
        HUserSystemApi hUserSystemApi = getOsgiService(HUserSystemApi.class);
        RoleSystemApi roleSystemApi = getOsgiService(RoleSystemApi.class);
        HyperIoTHUserTestUtils.truncateHUsers(hUserSystemApi);
        HyperIoTHUserTestUtils.truncateRoles(roleSystemApi);
    }

}
