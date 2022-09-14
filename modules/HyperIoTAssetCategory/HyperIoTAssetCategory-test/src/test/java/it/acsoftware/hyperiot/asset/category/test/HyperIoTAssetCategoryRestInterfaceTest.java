package it.acsoftware.hyperiot.asset.category.test;


import it.acsoftware.hyperiot.asset.category.model.AssetCategory;
import it.acsoftware.hyperiot.asset.category.service.rest.AssetCategoryRestApi;
import it.acsoftware.hyperiot.base.action.HyperIoTActionName;
import it.acsoftware.hyperiot.base.api.HyperIoTAction;
import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.base.api.HyperIoTUser;
import it.acsoftware.hyperiot.base.api.authentication.AuthenticationApi;
import it.acsoftware.hyperiot.base.api.authentication.AuthenticationSystemApi;
import it.acsoftware.hyperiot.base.model.HyperIoTAssetOwnerImpl;
import it.acsoftware.hyperiot.base.service.rest.HyperIoTBaseRestApi;
import it.acsoftware.hyperiot.base.test.http.*;
import it.acsoftware.hyperiot.base.test.http.matcher.HyperIoTHttpResponseValidator;
import it.acsoftware.hyperiot.base.test.http.matcher.HyperIoTHttpResponseValidatorBuilder;
import it.acsoftware.hyperiot.base.util.HyperIoTConstants;
import it.acsoftware.hyperiot.company.model.Company;
import it.acsoftware.hyperiot.company.service.rest.CompanyRestApi;
import it.acsoftware.hyperiot.huser.model.HUser;
import it.acsoftware.hyperiot.osgi.util.filter.OSGiFilterBuilder;
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

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static it.acsoftware.hyperiot.asset.category.test.HyperIoTAssetCategoryConfiguration.companyResourceName;

/**
 * @author Francesco Salerno
 *
 * This is a test class relative to Interface's test of AssetCategoryRestApi
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HyperIoTAssetCategoryRestInterfaceTest extends KarafTestSupport {

    /*
        TODO
            The resourceName of the HyperIoTAssetOwnerImpl is serialized only because
            the get method of the entity is not annotated with jsonIgnore.
            Verify if we expect this behaviour or not.
     */
    //force global configuration
    public Option[] config() {
        return null;
    }

    public HyperIoTContext impersonateUser(HyperIoTBaseRestApi restApi, HyperIoTUser user) {
        return restApi.impersonate(user);
    }

    public HyperIoTAction getHyperIoTAction(String resourceName, HyperIoTActionName action, long timeout) {
        String actionFilter = OSGiFilterBuilder.createFilter(HyperIoTConstants.OSGI_ACTION_RESOURCE_NAME, resourceName)
                .and(HyperIoTConstants.OSGI_ACTION_NAME, action.getName()).getFilter();
        return getOsgiService(HyperIoTAction.class, actionFilter, timeout);
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
    public void test001_saveAssetCategoryShouldSerializeResponseCorrectly()  {
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");

        Company company = createCompany((HUser) adminUser);
        Assert.assertNotEquals(0, company.getId());

        AssetCategory assetCategory = new AssetCategory();
        assetCategory.setName("Category" + java.util.UUID.randomUUID());
        HyperIoTAssetOwnerImpl owner = new HyperIoTAssetOwnerImpl();
        owner.setOwnerResourceName(companyResourceName);
        owner.setOwnerResourceId(company.getId());
        owner.setUserId(adminUser.getId());
        assetCategory.setOwner(owner);

        HyperIoTHttpRequest request = HyperIoTHttpRequestBuilder
                .post()
                .withUri(HyperIoTHttpUtils.SERVICE_BASE_URL.concat("/assets/categories"))
                .withAuthorizationAsHyperIoTAdmin()
                .withContentTypeHeader("application/json")
                .withJsonBody(assetCategory)
                .build();
        HyperIoTHttpResponse response = HyperIoTHttpClient
                .hyperIoTHttpClient()
                .execute(request);
        List<String> expectedProperty = assetCategoryProperty();
        expectedProperty.addAll(hyperIoTAbstractEntityProperties());
        HyperIoTHttpResponseValidator testValidator = HyperIoTHttpResponseValidatorBuilder
                .validatorBuilder()
                .withStatusEqual(200)
                .containExactProperties(expectedProperty)
                .containExactInnerProperties("owner",assetCategoryOwnerProperty())
                .build();
        boolean testSuccesful = testValidator.validateResponse(response);
        Assert.assertTrue(testSuccesful);
    }

    @Test
    public void test002_findAssetCategoryShouldSerializeResponseCorrectly()  {
        AssetCategory assetCategory = createAssetCategory();
        Assert.assertNotEquals(0, assetCategory.getId());
        HyperIoTHttpRequest request = HyperIoTHttpRequestBuilder
                .get()
                .withUri(HyperIoTHttpUtils.SERVICE_BASE_URL.concat("/assets/categories/").concat(String.valueOf(assetCategory.getId())))
                .withAuthorizationAsHyperIoTAdmin()
                .withContentTypeHeader("application/json")
                .build();
        HyperIoTHttpResponse response = HyperIoTHttpClient
                .hyperIoTHttpClient()
                .execute(request);
        List<String> expectedProperty = assetCategoryProperty();
        expectedProperty.addAll(hyperIoTAbstractEntityProperties());
        HyperIoTHttpResponseValidator testValidator = HyperIoTHttpResponseValidatorBuilder
                .validatorBuilder()
                .withStatusEqual(200)
                .containExactProperties(expectedProperty)
                .containExactInnerProperties("owner",assetCategoryOwnerProperty())
                .build();
        boolean testSuccesful = testValidator.validateResponse(response);
        Assert.assertTrue(testSuccesful);
    }

    @Test
    public void test003_updateAssetCategoryShouldSerializeResponseCorrectly()  {
        AssetCategory assetCategory = createAssetCategory();
        assetCategory.setName("CategoryName".concat(UUID.randomUUID().toString().replaceAll("-","")));
        HyperIoTHttpRequest request = HyperIoTHttpRequestBuilder
                .put()
                .withUri(HyperIoTHttpUtils.SERVICE_BASE_URL.concat("/assets/categories"))
                .withAuthorizationAsHyperIoTAdmin()
                .withContentTypeHeader("application/json")
                .withJsonBody(assetCategory)
                .build();
        HyperIoTHttpResponse response = HyperIoTHttpClient
                .hyperIoTHttpClient()
                .execute(request);
        List<String> expectedProperty = assetCategoryProperty();
        expectedProperty.addAll(hyperIoTAbstractEntityProperties());
        HyperIoTHttpResponseValidator testValidator = HyperIoTHttpResponseValidatorBuilder
                .validatorBuilder()
                .withStatusEqual(200)
                .containExactProperties(expectedProperty)
                .containExactInnerProperties("owner",assetCategoryOwnerProperty())
                .build();
        boolean testSuccesful = testValidator.validateResponse(response);
        Assert.assertTrue(testSuccesful);
    }

    @Test
    public void test004_deleteAssetCategoryShouldSerializeResponseCorrectly()  {
        AssetCategory assetCategory = createAssetCategory();
        HyperIoTHttpRequest request = HyperIoTHttpRequestBuilder
                .delete()
                .withUri(HyperIoTHttpUtils.SERVICE_BASE_URL.concat("/assets/categories/".concat(String.valueOf(assetCategory.getId()))))
                .withAuthorizationAsHyperIoTAdmin()
                .withContentTypeHeader("application/json")
                .build();
        HyperIoTHttpResponse response = HyperIoTHttpClient
                .hyperIoTHttpClient()
                .execute(request);
        HyperIoTHttpResponseValidator testValidator = HyperIoTHttpResponseValidatorBuilder
                .validatorBuilder()
                .withStatusEqual(200)
                .withCustomCriteria( (hyperIoTHttpResponse -> hyperIoTHttpResponse.getResponseBody().isEmpty()))
                .build();
        boolean testSuccesful = testValidator.validateResponse(response);
        Assert.assertTrue(testSuccesful);
    }

    @Test
    public void test005_findAllAssetCategoryShouldSerializeResponseCorrectly()  {
        AssetCategory assetCategory = createAssetCategory();
        AssetCategory assetCategory2 = createAssetCategory();
        HyperIoTHttpRequest request = HyperIoTHttpRequestBuilder
                .get()
                .withUri(HyperIoTHttpUtils.SERVICE_BASE_URL.concat("/assets/categories/all"))
                .withAuthorizationAsHyperIoTAdmin()
                .withContentTypeHeader("application/json")
                .build();
        HyperIoTHttpResponse response = HyperIoTHttpClient
                .hyperIoTHttpClient()
                .execute(request);
        List<String> expectedProperty = assetCategoryProperty();
        expectedProperty.addAll(hyperIoTAbstractEntityProperties());
        HyperIoTHttpResponseValidator testValidator = HyperIoTHttpResponseValidatorBuilder
                .validatorBuilder()
                .withStatusEqual(200)
                .containExactProperties(expectedProperty)
                .containExactInnerProperties("owner",assetCategoryOwnerProperty())
                .build();
        boolean testSuccesful = testValidator.validateResponse(response);
        Assert.assertTrue(testSuccesful);
    }

    @Test
    public void test006_findAllAssetCategoryPaginatedShouldSerializeResponseCorrectly()  {
        AssetCategory assetCategory = createAssetCategory();
        AssetCategory assetCategory2 = createAssetCategory();
        HyperIoTHttpRequest request = HyperIoTHttpRequestBuilder
                .get()
                .withUri(HyperIoTHttpUtils.SERVICE_BASE_URL.concat("/assets/categories"))
                .withAuthorizationAsHyperIoTAdmin()
                .withParameter("delta",String.valueOf(10))
                .withParameter("page",String.valueOf(1))
                .withContentTypeHeader("application/json")
                .build();
        HyperIoTHttpResponse response = HyperIoTHttpClient
                .hyperIoTHttpClient()
                .execute(request);
        List<String> resultsFieldExpectedProperty = assetCategoryProperty();
        resultsFieldExpectedProperty.addAll(hyperIoTAbstractEntityProperties());
        HyperIoTHttpResponseValidator testValidator = HyperIoTHttpResponseValidatorBuilder
                .validatorBuilder()
                .withStatusEqual(200)
                .containExactHyperIoTPaginatedProperties()
                .containExactInnerProperties("results",resultsFieldExpectedProperty)
                .containInnerProperties("results.owner",assetCategoryOwnerProperty())
                .build();
        boolean testSuccesful = testValidator.validateResponse(response);
        Assert.assertTrue(testSuccesful);
    }


    /* This is an an example of a test in which we use the validation of entity schema.
    @Test
    public void test00x_trialWithJsonSchema()  {
        AssetCategory assetCategory = createAssetCategory();
        HyperIoTHttpRequest request = HyperIoTHttpRequestBuilder
                .get()
                .withUri(HyperIoTHttpUtils.SERVICE_BASE_URL.concat("/assets/categories/").concat(String.valueOf(assetCategory.getId())))
                .withAuthorizationAsHyperIoTAdmin()
                .withContentTypeHeader("application/json")
                .build();
        HyperIoTHttpResponse response = HyperIoTHttpClient
                .hyperIoTHttpClient()
                .execute(request);
        HyperIoTHttpResponseValidator testValidator = HyperIoTHttpResponseValidatorBuilder
                .validatorBuilder()
                .matchEntitySchema(assetCategory.getClass())
                .build();
        boolean testSuccesful = testValidator.validateResponse(response);
        Assert.assertTrue(testSuccesful);
    }

     */

    /*
     *
     *
     * UTILITY METHODS
     *
     *
     */

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


    private List<String> assetCategoryProperty(){
        List<String> propertyList = new LinkedList<>();
        propertyList.add("name");
        propertyList.add("parent");
        propertyList.add("owner");
        return propertyList;
    }

    private List<String> assetCategoryOwnerProperty(){
        List<String> ownerProperty = new LinkedList<>();
        ownerProperty.add("ownerResourceName");
        ownerProperty.add("ownerResourceId");
        ownerProperty.add("userId");
        ownerProperty.add("resourceName");
        return ownerProperty;
    }

    private String createStringFieldWithSpecifiedLenght(int length) {
        String symbol = "a";
        String field = String.format("%" + length + "s", " ").replaceAll(" ", symbol);
        Assert.assertEquals(length, field.length());
        return field;
    }

    private AssetCategory createAssetCategory() {
        AssetCategoryRestApi assetCategoryRestApi = getOsgiService(AssetCategoryRestApi.class);
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");

        Company company = createCompany((HUser) adminUser);
        Assert.assertNotEquals(0, company.getId());

        AssetCategory assetCategory = new AssetCategory();
        assetCategory.setName("Category" + java.util.UUID.randomUUID());
        HyperIoTAssetOwnerImpl owner = new HyperIoTAssetOwnerImpl();
        owner.setOwnerResourceName(companyResourceName);
        owner.setOwnerResourceId(company.getId());
        owner.setUserId(adminUser.getId());
        assetCategory.setOwner(owner);
        this.impersonateUser(assetCategoryRestApi, adminUser);
        Response restResponse = assetCategoryRestApi.saveAssetCategory(assetCategory);
        Assert.assertEquals(200, restResponse.getStatus());
        Assert.assertNotEquals(0,
                ((AssetCategory) restResponse.getEntity()).getId());
        Assert.assertEquals(assetCategory.getName(), ((AssetCategory) restResponse.getEntity()).getName());
        Assert.assertEquals(companyResourceName,
                ((AssetCategory) restResponse.getEntity()).getOwner().getOwnerResourceName());
        Assert.assertEquals((Long) company.getId(),
                ((AssetCategory) restResponse.getEntity()).getOwner().getOwnerResourceId());
        Assert.assertEquals(adminUser.getId(),
                ((AssetCategory) restResponse.getEntity()).getOwner().getUserId());
        return assetCategory;
    }

    private Company createCompany(HUser huser) {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = new Company();
        company.setBusinessName("ACSoftware");
        company.setCity("Lamezia Terme");
        company.setInvoiceAddress("Lamezia Terme");
        company.setNation("Italy");
        company.setPostalCode("88046");
        company.setVatNumber("01234567890" + java.util.UUID.randomUUID().toString().replaceAll("-", ""));
        company.setHUserCreator(huser);
        this.impersonateUser(companyRestApi, adminUser);
        Response restResponse = companyRestApi.saveCompany(company);
        Assert.assertEquals(200, restResponse.getStatus());
        Assert.assertNotEquals(0, ((Company) restResponse.getEntity()).getId());
        Assert.assertEquals("ACSoftware", ((Company) restResponse.getEntity()).getBusinessName());
        Assert.assertEquals("Lamezia Terme", ((Company) restResponse.getEntity()).getCity());
        Assert.assertEquals("Lamezia Terme", ((Company) restResponse.getEntity()).getInvoiceAddress());
        Assert.assertEquals("Italy", ((Company) restResponse.getEntity()).getNation());
        Assert.assertEquals("88046", ((Company) restResponse.getEntity()).getPostalCode());
        Assert.assertEquals(company.getVatNumber(), ((Company) restResponse.getEntity()).getVatNumber());
        Assert.assertEquals(huser.getId(), ((Company) restResponse.getEntity()).getHUserCreator().getId());
        return company;
    }


    @After
    public void afterTest() {
        AssetCategoryRestApi assetCategoryRestApi = getOsgiService(AssetCategoryRestApi.class);
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        AuthenticationSystemApi authenticationSystemApi = getOsgiService(AuthenticationSystemApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        this.impersonateUser(assetCategoryRestApi, adminUser);
        Response restResponse = assetCategoryRestApi.findAllAssetCategory();
        List<AssetCategory> listCategories = restResponse.readEntity(new GenericType<List<AssetCategory>>() {
        });
        if (!listCategories.isEmpty()) {
            Assert.assertFalse(listCategories.isEmpty());
            for (AssetCategory assetCategory : listCategories) {
                this.impersonateUser(assetCategoryRestApi, adminUser);
                Response restResponse1 = assetCategoryRestApi.deleteAssetCategory(assetCategory.getId());
                Assert.assertEquals(200, restResponse1.getStatus());
            }
        }
    }
}
