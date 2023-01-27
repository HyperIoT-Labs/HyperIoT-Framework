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

package it.acsoftware.hyperiot.asset.tag.test;

import it.acsoftware.hyperiot.asset.tag.api.AssetTagRepository;
import it.acsoftware.hyperiot.asset.tag.model.AssetTag;
import it.acsoftware.hyperiot.asset.tag.service.rest.AssetTagRestApi;
import it.acsoftware.hyperiot.base.action.HyperIoTActionName;
import it.acsoftware.hyperiot.base.api.HyperIoTAction;
import it.acsoftware.hyperiot.base.api.HyperIoTAssetTagManager;
import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.base.api.HyperIoTUser;
import it.acsoftware.hyperiot.base.api.authentication.AuthenticationApi;
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
import org.apache.aries.jpa.template.TransactionType;
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

import static it.acsoftware.hyperiot.asset.tag.test.HyperIoTAssetTagConfiguration.companyResourceName;

/**
 * @author Francesco Salerno
 *
 * This is a test class relative to Interface's test of AssetTagRestApi
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HyperIoTAssetTagRestInterfaceTest extends KarafTestSupport {

    //force global configuration
    public Option[] config() {
        return null;
    }

    public HyperIoTContext impersonateUser(HyperIoTBaseRestApi restApi, HyperIoTUser user) {
        return restApi.impersonate(user);
    }

    @SuppressWarnings("unused")
    private HyperIoTAction getHyperIoTAction(String resourceName, HyperIoTActionName action, long timeout) {
        String actionFilter = OSGiFilterBuilder.createFilter(HyperIoTConstants.OSGI_ACTION_RESOURCE_NAME, resourceName)
                .and(HyperIoTConstants.OSGI_ACTION_NAME, action.getName()).getFilter();
        return getOsgiService(HyperIoTAction.class, actionFilter, timeout);
    }

    @Test
    public void test000_hyperIoTFrameworkShouldBeInstalled() {
        // assert on an available service
        // hyperiot-core import the following features: base, mail, authentication, permission, huser, company, role,
        // assetcategory, assettag, sharedentity.
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
    public void test001_saveAssetTagShouldSerializeResponseCorrectly()  {
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");

        Company company = createCompany((HUser) adminUser);
        Assert.assertNotEquals(0, company.getId());

        AssetTag assetTag = new AssetTag();
        assetTag.setName("Tag" + java.util.UUID.randomUUID());
        HyperIoTAssetOwnerImpl owner = new HyperIoTAssetOwnerImpl();
        owner.setOwnerResourceName(companyResourceName);
        owner.setOwnerResourceId(company.getId());
        owner.setUserId(adminUser.getId());
        assetTag.setOwner(owner);

        HyperIoTHttpRequest request = HyperIoTHttpRequestBuilder
                .post()
                .withUri(HyperIoTHttpUtils.SERVICE_BASE_URL.concat("/assets/tags"))
                .withAuthorizationAsHyperIoTAdmin()
                .withContentTypeHeader("application/json")
                .withJsonBody(assetTag)
                .build();
        HyperIoTHttpResponse response = HyperIoTHttpClient
                .hyperIoTHttpClient()
                .execute(request);
        List<String> expectProperty = assetTagProperty();
        expectProperty.addAll(hyperIoTAbstractEntityProperties());
        HyperIoTHttpResponseValidator testValidator = HyperIoTHttpResponseValidatorBuilder
                .validatorBuilder()
                .withStatusEqual(200)
                .containHyperIoTAbstractEntityFields()
                .containExactProperties(expectProperty)
                .containExactInnerProperties("owner",hyperIoTAssetOwnerImplProperty())
                .build();
        boolean testSuccesful = testValidator.validateResponse(response);
        Assert.assertTrue(testSuccesful);
    }

    @Test
    public void test002_findAssetTagShouldSerializeResponseCorrectly()  {
        AssetTag assetCategory = createAssetTag();
        Assert.assertNotEquals(0, assetCategory.getId());
        HyperIoTHttpRequest request = HyperIoTHttpRequestBuilder
                .get()
                .withUri(HyperIoTHttpUtils.SERVICE_BASE_URL.concat("/assets/tags/").concat(String.valueOf(assetCategory.getId())))
                .withAuthorizationAsHyperIoTAdmin()
                .withContentTypeHeader("application/json")
                .build();
        HyperIoTHttpResponse response = HyperIoTHttpClient
                .hyperIoTHttpClient()
                .execute(request);
        List<String> expectProperty = assetTagProperty();
        expectProperty.addAll(hyperIoTAbstractEntityProperties());
        HyperIoTHttpResponseValidator testValidator = HyperIoTHttpResponseValidatorBuilder
                .validatorBuilder()
                .withStatusEqual(200)
                .containHyperIoTAbstractEntityFields()
                .containExactProperties(expectProperty)
                .containExactInnerProperties("owner",hyperIoTAssetOwnerImplProperty())
                .build();
        boolean testSuccesful = testValidator.validateResponse(response);
        Assert.assertTrue(testSuccesful);
    }

    @Test
    public void test003_updateAssetTagShouldSerializeResponseCorrectly()  {
        AssetTag assetTag = createAssetTag();
        assetTag.setName("CategoryName".concat(UUID.randomUUID().toString().replaceAll("-","")));
        HyperIoTHttpRequest request = HyperIoTHttpRequestBuilder
                .put()
                .withUri(HyperIoTHttpUtils.SERVICE_BASE_URL.concat("/assets/tags"))
                .withAuthorizationAsHyperIoTAdmin()
                .withContentTypeHeader("application/json")
                .withJsonBody(assetTag)
                .build();
        HyperIoTHttpResponse response = HyperIoTHttpClient
                .hyperIoTHttpClient()
                .execute(request);
        List<String> expectProperty = assetTagProperty();
        expectProperty.addAll(hyperIoTAbstractEntityProperties());
        HyperIoTHttpResponseValidator testValidator = HyperIoTHttpResponseValidatorBuilder
                .validatorBuilder()
                .withStatusEqual(200)
                .containHyperIoTAbstractEntityFields()
                .containExactProperties(expectProperty)
                .containExactInnerProperties("owner",hyperIoTAssetOwnerImplProperty())
                .build();
        boolean testSuccesful = testValidator.validateResponse(response);
        Assert.assertTrue(testSuccesful);
    }

    @Test
    public void test004_deleteAssetTagShouldSerializeResponseCorrectly()  {
        AssetTag assetTag = createAssetTag();
        HyperIoTHttpRequest request = HyperIoTHttpRequestBuilder
                .delete()
                .withUri(HyperIoTHttpUtils.SERVICE_BASE_URL.concat("/assets/tags/".concat(String.valueOf(assetTag.getId()))))
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
    public void test005_findAllAssetTagShouldSerializeResponseCorrectly()  {
        AssetTag assetTag = createAssetTag();
        AssetTag assetTag1 = createAssetTag();
        HyperIoTHttpRequest request = HyperIoTHttpRequestBuilder
                .get()
                .withUri(HyperIoTHttpUtils.SERVICE_BASE_URL.concat("/assets/tags/all"))
                .withAuthorizationAsHyperIoTAdmin()
                .withContentTypeHeader("application/json")
                .build();
        HyperIoTHttpResponse response = HyperIoTHttpClient
                .hyperIoTHttpClient()
                .execute(request);
        List<String> expectProperty = assetTagProperty();
        expectProperty.addAll(hyperIoTAbstractEntityProperties());
        HyperIoTHttpResponseValidator testValidator = HyperIoTHttpResponseValidatorBuilder
                .validatorBuilder()
                .withStatusEqual(200)
                .containHyperIoTAbstractEntityFields()
                .containExactProperties(expectProperty)
                .containExactInnerProperties("owner",hyperIoTAssetOwnerImplProperty())
                .build();
        boolean testSuccesful = testValidator.validateResponse(response);
        Assert.assertTrue(testSuccesful);
    }

    @Test
    public void test006_findAllAssetTagPaginatedShouldSerializeResponseCorrectly()  {
        AssetTag assetTag = createAssetTag();
        AssetTag assetTag1 = createAssetTag();
        HyperIoTHttpRequest request = HyperIoTHttpRequestBuilder
                .get()
                .withUri(HyperIoTHttpUtils.SERVICE_BASE_URL.concat("/assets/tags"))
                .withAuthorizationAsHyperIoTAdmin()
                .withParameter("delta",String.valueOf(10))
                .withParameter("page",String.valueOf(1))
                .withContentTypeHeader("application/json")
                .build();
        HyperIoTHttpResponse response = HyperIoTHttpClient
                .hyperIoTHttpClient()
                .execute(request);
        List<String> expectedProperty = assetTagProperty();
        expectedProperty.addAll(hyperIoTAbstractEntityProperties());
        HyperIoTHttpResponseValidator testValidator = HyperIoTHttpResponseValidatorBuilder
                .validatorBuilder()
                .withStatusEqual(200)
                .containExactHyperIoTPaginatedProperties()
                .containHyperIoTAbstractEntityFields("results")
                .containExactInnerProperties("results", expectedProperty)
                .containExactInnerProperties("results.owner",hyperIoTAssetOwnerImplProperty())
                .build();
        boolean testSuccesful = testValidator.validateResponse(response);
        Assert.assertTrue(testSuccesful);
    }

    @Test
    public void test007_getAssetTagResourceListShouldSerializeResponseCorrectly(){
        AssetTag assetTag = createAssetTag();
        AssetTagRepository assetTagRepository = getOsgiService(AssetTagRepository.class);
        HyperIoTAssetTagManager assetTagManager = getOsgiService(HyperIoTAssetTagManager.class);
        String resourceName ="it.acsoftware.hyperiot.resource.test.example";
        long resourceId = 100;
        //Add asset tag resource to tag.
        assetTagRepository.executeTransaction(TransactionType.Required, (entityManager) ->{
            assetTagManager.addAssetTag(resourceName, resourceId, assetTag.getId());
        });
        HyperIoTHttpRequest request = HyperIoTHttpRequestBuilder
                .get()
                .withUri(HyperIoTHttpUtils.SERVICE_BASE_URL.concat(String.format("/assets/tags/all/%s/%s",resourceName,resourceId)))
                .withAuthorizationAsHyperIoTAdmin()
                .withContentTypeHeader("application/json")
                .build();
        System.out.println(request.getURI().toString());
        HyperIoTHttpResponse response = HyperIoTHttpClient
                .hyperIoTHttpClient()
                .execute(request);
        List<String> expectedProperties = assetTagResourceProperties();
        expectedProperties.addAll(hyperIoTAbstractEntityProperties());
        List<String> expectedInnerTagProperties = assetTagProperty();
        expectedInnerTagProperties.addAll(hyperIoTAbstractEntityProperties());
        HyperIoTHttpResponseValidator testValidator = HyperIoTHttpResponseValidatorBuilder
                .validatorBuilder()
                .withStatusEqual(200)
                .containExactProperties(expectedProperties)
                .containExactInnerProperties("tag",expectedInnerTagProperties)
                .build();
        boolean testSuccesful = testValidator.validateResponse(response);
        Assert.assertTrue(testSuccesful);

    }


    /*
     *
     *
     * UTILITY METHODS
     *
     *
     */
    private List<String> assetTagResourceProperties(){
        List<String> hyperIoTAbstractEntityFields = new ArrayList<>();
        hyperIoTAbstractEntityFields.add("resourceName");
        hyperIoTAbstractEntityFields.add("resourceId");
        hyperIoTAbstractEntityFields.add("tag");
        return hyperIoTAbstractEntityFields;
    }

    private List<String> hyperIoTAbstractEntityProperties(){
        List<String> hyperIoTAbstractEntityFields = new ArrayList<>();
        hyperIoTAbstractEntityFields.add("id");
        hyperIoTAbstractEntityFields.add("entityCreateDate");
        hyperIoTAbstractEntityFields.add("entityModifyDate");
        hyperIoTAbstractEntityFields.add("categoryIds");
        hyperIoTAbstractEntityFields.add("tagIds");
        hyperIoTAbstractEntityFields.add("entityVersion");
        return hyperIoTAbstractEntityFields;
    }


    private List<String> assetTagProperty(){
        List<String> propertyList = new LinkedList<>();
        propertyList.add("name");
        propertyList.add("owner");
        propertyList.add("color");
        propertyList.add("description");
        return propertyList;
    }

    private List<String> hyperIoTAssetOwnerImplProperty(){
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


    private AssetTag createAssetTag() {
        AssetTagRestApi assetTagRestApi = getOsgiService(AssetTagRestApi.class);
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");

        Company company = createCompany((HUser) adminUser);
        Assert.assertNotEquals(0, company.getId());

        AssetTag assetTag = new AssetTag();
        assetTag.setName("Tag" + java.util.UUID.randomUUID());
        HyperIoTAssetOwnerImpl owner = new HyperIoTAssetOwnerImpl();
        owner.setOwnerResourceName(companyResourceName);
        owner.setOwnerResourceId(company.getId());
        owner.setUserId(adminUser.getId());
        assetTag.setOwner(owner);
        this.impersonateUser(assetTagRestApi, adminUser);
        Response restResponse = assetTagRestApi.saveAssetTag(assetTag);
        Assert.assertEquals(200, restResponse.getStatus());
        Assert.assertNotEquals(0,
                ((AssetTag) restResponse.getEntity()).getId());
        Assert.assertEquals(assetTag.getName(), ((AssetTag) restResponse.getEntity()).getName());
        Assert.assertEquals(companyResourceName,
                ((AssetTag) restResponse.getEntity()).getOwner().getOwnerResourceName());
        Assert.assertEquals((Long)company.getId(),
                ((AssetTag) restResponse.getEntity()).getOwner().getOwnerResourceId());
        Assert.assertEquals(adminUser.getId(),
                ((AssetTag) restResponse.getEntity()).getOwner().getUserId());
        return assetTag;
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
        AssetTagRestApi assetTagRestApi = getOsgiService(AssetTagRestApi.class);
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        this.impersonateUser(assetTagRestApi, adminUser);
        Response restResponse = assetTagRestApi.findAllAssetTag();
        List<AssetTag> listTags = restResponse.readEntity(new GenericType<List<AssetTag>>() {
        });
        if (!listTags.isEmpty()) {
            Assert.assertFalse(listTags.isEmpty());
            for (AssetTag assetTag : listTags) {
                this.impersonateUser(assetTagRestApi, adminUser);
                Response restResponse1 = assetTagRestApi.deleteAssetTag(assetTag.getId());
                Assert.assertEquals(200, restResponse1.getStatus());
            }
        }
    }
}
