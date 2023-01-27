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

package it.acsoftware.hyperiot.company.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import it.acsoftware.hyperiot.base.action.HyperIoTActionName;
import it.acsoftware.hyperiot.base.api.HyperIoTAction;
import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.base.api.HyperIoTUser;
import it.acsoftware.hyperiot.base.api.authentication.AuthenticationApi;
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
import java.util.List;
import java.util.UUID;
import static it.acsoftware.hyperiot.company.test.HyperIoTCompanyConfiguration.*;

/**
 * @author Francesco Salerno
 *
 * This is a test class relative to Interface's test of AuthenticationRestApi
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HyperIoTCompanyRestInterfaceTest extends KarafTestSupport {

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
        String datasource = executeCommand("jdbc:ds-list");
//		System.out.println(executeCommand("bundle:list | grep HyperIoT"));
        assertContains("hyperiot", datasource);
    }

    @Test
    public void test001_saveCompanyShouldSerializeResponseCorrectly() throws JsonProcessingException {
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = new Company();
        company.setBusinessName("ACSoftware" + java.util.UUID.randomUUID().toString().replaceAll("-", ""));
        company.setCity("Lamezia Terme");
        company.setInvoiceAddress("Lamezia Terme");
        company.setNation("Italy");
        company.setPostalCode("88046");
        company.setVatNumber("01234567890" + java.util.UUID.randomUUID().toString().replaceAll("-", ""));
        company.setHUserCreator((HUser) adminUser);
        HyperIoTHttpRequest request = HyperIoTHttpRequestBuilder
                .post()
                .withUri(HyperIoTHttpUtils.SERVICE_BASE_URL.concat("/companies"))
                .withAuthorizationAsHyperIoTAdmin()
                .withContentTypeHeader("application/json")
                .withJsonBody(company)
                .build();
        HyperIoTHttpResponse response = HyperIoTHttpClient
                .hyperIoTHttpClient()
                .execute(request);
        List<String> expectedCompanyProperties = companyProperty();
        expectedCompanyProperties.addAll(hyperIoTAbstractEntityProperties());
        HyperIoTHttpResponseValidator testValidator = HyperIoTHttpResponseValidatorBuilder
                .validatorBuilder()
                .withStatusEqual(200)
                .containExactProperties(expectedCompanyProperties)
                .containExactInnerProperties("hUserCreator",companyHUserCreatorProperty())
                .build();
        boolean testSuccesful = testValidator.validateResponse(response);
        Assert.assertTrue(testSuccesful);
    }

    @Test
    public void test002_findCompanyShouldSerializeResponseCorrectly(){
        Company company = createCompany();
        Assert.assertNotEquals(0 , company.getId());
        HyperIoTHttpRequest request = HyperIoTHttpRequestBuilder
                .get()
                .withUri(HyperIoTHttpUtils.SERVICE_BASE_URL.concat("/companies/".concat(String.valueOf(company.getId()))))
                .withAuthorizationAsHyperIoTAdmin()
                .withContentTypeHeader("application/json")
                .build();
        HyperIoTHttpResponse response = HyperIoTHttpClient
                .hyperIoTHttpClient()
                .execute(request);
        List<String> expectedCompanyProperties = companyProperty();
        expectedCompanyProperties.addAll(hyperIoTAbstractEntityProperties());
        HyperIoTHttpResponseValidator testValidator = HyperIoTHttpResponseValidatorBuilder
                .validatorBuilder()
                .withStatusEqual(200)
                .containExactProperties(expectedCompanyProperties)
                .containExactInnerProperties("hUserCreator",companyHUserCreatorProperty())
                .build();
        boolean testSuccesful = testValidator.validateResponse(response);
        Assert.assertTrue(testSuccesful);
    }

    @Test
    public void test003_updateCompanyShouldSerializeResponseCorrectly(){
        Company company = createCompany();
        Assert.assertNotEquals(0, company.getId());
        String businessName = "Company".concat(UUID.randomUUID().toString().replaceAll("-",""));
        company.setBusinessName(businessName);
        HyperIoTHttpRequest request = HyperIoTHttpRequestBuilder
                .put()
                .withUri(HyperIoTHttpUtils.SERVICE_BASE_URL.concat("/companies"))
                .withAuthorizationAsHyperIoTAdmin()
                .withContentTypeHeader("application/json")
                .withJsonBody(company)
                .build();
        HyperIoTHttpResponse response = HyperIoTHttpClient
                .hyperIoTHttpClient()
                .execute(request);
        List<String> expectedCompanyProperties = companyProperty();
        expectedCompanyProperties.addAll(hyperIoTAbstractEntityProperties());
        HyperIoTHttpResponseValidator testValidator = HyperIoTHttpResponseValidatorBuilder
                .validatorBuilder()
                .withStatusEqual(200)
                .containExactProperties(expectedCompanyProperties)
                .containExactInnerProperties("hUserCreator",companyHUserCreatorProperty())
                .build();
        boolean testSuccesful = testValidator.validateResponse(response);
        Assert.assertTrue(testSuccesful);
    }

    @Test
    public void test004_deleteCompanyShouldSerializeResponseCorrectly(){
        Company company = createCompany();
        Assert.assertNotEquals(0, company.getId());
        HyperIoTHttpRequest request = HyperIoTHttpRequestBuilder
                .delete()
                .withUri(HyperIoTHttpUtils.SERVICE_BASE_URL.concat("/companies/").concat(String.valueOf(company.getId())))
                .withAuthorizationAsHyperIoTAdmin()
                .withContentTypeHeader("application/json")
                .build();
        HyperIoTHttpResponse response = HyperIoTHttpClient
                .hyperIoTHttpClient()
                .execute(request);
        HyperIoTHttpResponseValidator testValidator = HyperIoTHttpResponseValidatorBuilder
                .validatorBuilder()
                .withStatusEqual(200)
                .withCustomCriteria((hyperIoTHttpResponse -> hyperIoTHttpResponse.getResponseBody().isEmpty()))
                .build();
        boolean testSuccesful = testValidator.validateResponse(response);
        Assert.assertTrue(testSuccesful);
    }

    @Test
    public void test005_findAllCompanyShouldSerializeResponseCorrectly(){
        Company company1 = createCompany();
        Assert.assertNotEquals(0, company1.getId());
        Company company2 = createCompany();
        Assert.assertNotEquals(0, company2.getId());
        HyperIoTHttpRequest request = HyperIoTHttpRequestBuilder
                .get()
                .withUri(HyperIoTHttpUtils.SERVICE_BASE_URL.concat("/companies/all"))
                .withAuthorizationAsHyperIoTAdmin()
                .withContentTypeHeader("application/json")
                .build();
        HyperIoTHttpResponse response = HyperIoTHttpClient
                .hyperIoTHttpClient()
                .execute(request);
        List<String> expectedCompanyProperties = companyProperty();
        expectedCompanyProperties.addAll(hyperIoTAbstractEntityProperties());
        HyperIoTHttpResponseValidator testValidator = HyperIoTHttpResponseValidatorBuilder
                .validatorBuilder()
                .withStatusEqual(200)
                .containExactProperties(expectedCompanyProperties)
                .containExactInnerProperties("hUserCreator",companyHUserCreatorProperty())
                .build();
        boolean testSuccesful = testValidator.validateResponse(response);
        Assert.assertTrue(testSuccesful);
    }

    @Test
    public void test006_findAllCompanyPaginatedShouldSerializeResponseCorrectly(){
        Company company1 = createCompany();
        Assert.assertNotEquals(0, company1.getId());
        Company company2 = createCompany();
        Assert.assertNotEquals(0, company2.getId());
        HyperIoTHttpRequest request = HyperIoTHttpRequestBuilder
                .get()
                .withUri(HyperIoTHttpUtils.SERVICE_BASE_URL.concat("/companies"))
                .withParameter("delta",String.valueOf(defaultDelta))
                .withParameter("page",String.valueOf(defaultPage))
                .withContentTypeHeader("application/json")
                .withAuthorizationAsHyperIoTAdmin()
                .build();
        HyperIoTHttpResponse response = HyperIoTHttpClient
                .hyperIoTHttpClient()
                .execute(request);
        List<String> expectedCompanyProperties = companyProperty();
        expectedCompanyProperties.addAll(hyperIoTAbstractEntityProperties());
        HyperIoTHttpResponseValidator testValidator = HyperIoTHttpResponseValidatorBuilder
                .validatorBuilder()
                .withStatusEqual(200)
                .containExactHyperIoTPaginatedProperties()
                .containExactInnerProperties("results",expectedCompanyProperties)
                .containExactInnerProperties("results.hUserCreator",companyHUserCreatorProperty())
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


    private List<String> companyHUserCreatorProperty(){
        List<String> hyperIoTAbstractEntityFields = new ArrayList<>();
        hyperIoTAbstractEntityFields.add("id");
        hyperIoTAbstractEntityFields.add("entityCreateDate");
        hyperIoTAbstractEntityFields.add("entityModifyDate");
        hyperIoTAbstractEntityFields.add("admin");
        hyperIoTAbstractEntityFields.add("imagePath");
        return hyperIoTAbstractEntityFields;
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


    private List<String> companyProperty(){
        List<String> companyProperty = new ArrayList<>();
        companyProperty.add("businessName");
        companyProperty.add("invoiceAddress");
        companyProperty.add("city");
        companyProperty.add("postalCode");
        companyProperty.add("nation");
        companyProperty.add("vatNumber");
        companyProperty.add("hUserCreator");
        return companyProperty;
    }

    private Company createCompany() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        this.impersonateUser(companyRestApi, adminUser);
        Company company = new Company();
        company.setBusinessName("ACSoftware");
        company.setCity("Lamezia Terme");
        company.setInvoiceAddress("Lamezia Terme");
        company.setNation("Italy");
        company.setPostalCode("88046");
        company.setVatNumber("01234567890" + UUID.randomUUID().toString().replaceAll("-", ""));
        company.setHUserCreator((HUser) adminUser);
        Response restResponse = companyRestApi.saveCompany(company);
        Assert.assertEquals(200, restResponse.getStatus());
        Assert.assertNotEquals(0, ((Company) restResponse.getEntity()).getId());
        Assert.assertEquals("ACSoftware", ((Company) restResponse.getEntity()).getBusinessName());
        Assert.assertEquals("Lamezia Terme", ((Company) restResponse.getEntity()).getCity());
        Assert.assertEquals("Lamezia Terme", ((Company) restResponse.getEntity()).getInvoiceAddress());
        Assert.assertEquals("Italy", ((Company) restResponse.getEntity()).getNation());
        Assert.assertEquals("88046", ((Company) restResponse.getEntity()).getPostalCode());
        Assert.assertEquals(company.getVatNumber(), ((Company) restResponse.getEntity()).getVatNumber());
        Assert.assertEquals(adminUser.getId(), ((Company) restResponse.getEntity()).getHUserCreator().getId());
        return company;
    }


    @After
    public void afterTest() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        this.impersonateUser(companyRestApi, adminUser);
        Response restResponse = companyRestApi.findAllCompany();
        List<Company> listCompanies = restResponse.readEntity(new GenericType<List<Company>>() {
        });
        if (!listCompanies.isEmpty()) {
            Assert.assertFalse(listCompanies.isEmpty());
            for (Company company : listCompanies) {
                this.impersonateUser(companyRestApi, adminUser);
                Response restResponse1 = companyRestApi.deleteCompany(company.getId());
                Assert.assertEquals(200, restResponse1.getStatus());
                Assert.assertNull(restResponse1.getEntity());
            }
        }
    }



}
