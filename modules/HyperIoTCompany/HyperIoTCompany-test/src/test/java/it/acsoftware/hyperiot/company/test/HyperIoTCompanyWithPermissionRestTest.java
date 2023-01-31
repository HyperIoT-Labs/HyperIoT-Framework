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

import it.acsoftware.hyperiot.base.api.authentication.AuthenticationApi;
import it.acsoftware.hyperiot.base.action.HyperIoTActionName;
import it.acsoftware.hyperiot.base.action.util.HyperIoTActionsUtil;
import it.acsoftware.hyperiot.base.action.util.HyperIoTCrudAction;
import it.acsoftware.hyperiot.base.api.HyperIoTAction;
import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.base.api.HyperIoTUser;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTPaginableResult;
import it.acsoftware.hyperiot.base.model.HyperIoTBaseError;
import it.acsoftware.hyperiot.base.service.rest.HyperIoTBaseRestApi;
import it.acsoftware.hyperiot.base.test.HyperIoTTestConfigurationBuilder;
import it.acsoftware.hyperiot.base.util.HyperIoTConstants;
import it.acsoftware.hyperiot.company.model.Company;
import it.acsoftware.hyperiot.company.service.rest.CompanyRestApi;
import it.acsoftware.hyperiot.huser.model.HUser;
import it.acsoftware.hyperiot.huser.service.rest.HUserRestApi;
import it.acsoftware.hyperiot.osgi.util.filter.OSGiFilterBuilder;
import it.acsoftware.hyperiot.permission.api.PermissionSystemApi;
import it.acsoftware.hyperiot.permission.model.Permission;
import it.acsoftware.hyperiot.permission.service.rest.PermissionRestApi;
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
import org.ops4j.pax.exam.Configuration;
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
 * @author Aristide Cittadino Interface component for Company System Service.
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HyperIoTCompanyWithPermissionRestTest extends KarafTestSupport {

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
    public void test00_hyperIoTFrameworkShouldBeInstalled() {
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
    public void test01_companyModuleShouldWork() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // the following call checkModuleWorking checks if Company module working
        // correctly
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        this.impersonateUser(companyRestApi, adminUser);
        Response restResponse = companyRestApi.checkModuleWorking();
        Assert.assertEquals(200, restResponse.getStatus());
    }


    @Test
    public void test02_saveCompanyWithPermissionShouldWork() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // HUser, with permission, save Company with the following call saveCompany
        // response status code '200'
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.SAVE);
        huser = createHUser(action);
        Company company = new Company();
        company.setBusinessName("ACSoftware");
        company.setCity("Lamezia Terme");
        company.setInvoiceAddress("Lamezia Terme");
        company.setNation("Italy");
        company.setPostalCode("88046");
        company.setVatNumber("01234567890" + UUID.randomUUID().toString().replaceAll("-", ""));
        company.setHUserCreator(huser);
        this.impersonateUser(companyRestApi, huser);
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
    }


    @Test
    public void test03_saveCompanyWithoutPermissionShouldFail() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // HUser, without permission, tries to save Company with the following call saveCompany
        // response status code '403' HyperIoTUnauthorizedException
        huser = createHUser(null);
        Company company = new Company();
        company.setBusinessName("ACSoftware");
        company.setCity("Lamezia Terme");
        company.setInvoiceAddress("Lamezia Terme");
        company.setNation("Italy");
        company.setPostalCode("88046");
        company.setVatNumber("01234567890" + UUID.randomUUID().toString().replaceAll("-", ""));
        company.setHUserCreator(huser);
        this.impersonateUser(companyRestApi, null);
        Response restResponse = companyRestApi.saveCompany(company);
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }


    @Test
    public void test04_updateCompanyWithPermissionShouldWork() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // HUser, with permission, update Company with the following call updateCompany
        // response status code '200'
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.UPDATE);
        huser = createHUser(action);
        Company company = createCompany(huser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(huser.getId(), company.getHUserCreator().getId());
        company.setCity("Bologna");
        this.impersonateUser(companyRestApi, huser);
        Response restResponse = companyRestApi.updateCompany(company);
        Assert.assertEquals(200, restResponse.getStatus());
        Assert.assertEquals("Bologna", ((Company) restResponse.getEntity()).getCity());
        Assert.assertEquals(company.getEntityVersion() + 1,
                ((Company) restResponse.getEntity()).getEntityVersion());
    }


    @Test
    public void test05_updateCompanyWithoutPermissionShouldFail() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // HUser, without permission, tries to update Company with the following call updateCompany
        // response status code '403' HyperIoTUnauthorizedException
        huser = createHUser(null);
        Company company = createCompany(huser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(huser.getId(), company.getHUserCreator().getId());
        company.setBusinessName("Bologna");
        this.impersonateUser(companyRestApi, huser);
        Response restResponse = companyRestApi.updateCompany(company);
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }


    @Test
    public void test06_findCompanyWithPermissionShouldWork() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // HUser, with permission, find Company with the following call findCompany
        // response status code '200'
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.FIND);
        //huser = createHUser(action);
        huser = createHUser(action);
        Company company = createCompany(huser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(huser.getId(), company.getHUserCreator().getId());

        this.impersonateUser(companyRestApi, huser);
        Response restResponse = companyRestApi.findCompany(company.getId());
        Assert.assertEquals(200, restResponse.getStatus());
        Assert.assertEquals(company.getId(), ((Company) restResponse.getEntity()).getId());
        Assert.assertEquals(huser.getId(), ((Company) restResponse.getEntity()).getHUserCreator().getId());
    }


    @Test
    public void test07_findCompanyWithPermissionShouldFailIfEntityNotFound() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // HUser, with permission, tries to find Company with the following call findCompany,
        // but entity not found
        // response status code '404' HyperIoTEntityNotFound
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.FIND);
        huser = createHUser(action);
        this.impersonateUser(companyRestApi, huser);
        Response restResponse = companyRestApi.findCompany(0);
        Assert.assertEquals(404, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }


    @Test
    public void test08_findCompanyWithoutPermissionShouldFail() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // HUser, without permission, tries to find Company with the following call findCompany
        // response status code '403' HyperIoTUnauthorizedException
        huser = createHUser(null);
        Company company = createCompany(huser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(huser.getId(), company.getHUserCreator().getId());
        this.impersonateUser(companyRestApi, huser);
        Response restResponse = companyRestApi.findCompany(company.getId());
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }


    @Test
    public void test09_deleteCompanyWithPermissionShouldWork() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // HUser, with permission, delete Company with the following call deleteCompany
        // response status code '200'
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.REMOVE);
        huser = createHUser(action);
        Company company = createCompany(huser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(huser.getId(), company.getHUserCreator().getId());
        this.impersonateUser(companyRestApi, huser);
        Response restResponse = companyRestApi.deleteCompany(company.getId());
        Assert.assertEquals(200, restResponse.getStatus());
        Assert.assertNull(restResponse.getEntity());
    }


    @Test
    public void test10_deleteCompanyWithPermissionShouldFailIfEntityNotFound() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // HUser, with permission, tries to delete Company with the following call deleteCompany,
        // but entity not found
        // response status code '404' HyperIoTEntityNotFound
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.REMOVE);
        huser = createHUser(action);
        this.impersonateUser(companyRestApi, huser);
        Response restResponse = companyRestApi.deleteCompany(0);
        Assert.assertEquals(404, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }


    @Test
    public void test11_deleteCompanyWithoutPermissionShouldFail() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // HUser, without permission, tries to delete Company with the following call deleteCompany
        // response status code '403' HyperIoTUnauthorizedException
        huser = createHUser(null);
        Company company = createCompany(huser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(huser.getId(), company.getHUserCreator().getId());
        this.impersonateUser(companyRestApi, huser);
        Response restResponse = companyRestApi.deleteCompany(company.getId());
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }


    @Test
    public void test12_findAllCompanyWithPermissionShouldWork() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // HUser, with permission, find all Company with the following call findAllCompany
        // response status code '200'
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.FINDALL);
        huser = createHUser(action);
        Company company = createCompany(huser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(huser.getId(), company.getHUserCreator().getId());
        this.impersonateUser(companyRestApi, huser);
        Response restResponse = companyRestApi.findAllCompany();
        List<Company> listCompanies = restResponse.readEntity(new GenericType<List<Company>>() {
        });
        Assert.assertFalse(listCompanies.isEmpty());
        Assert.assertEquals(1, listCompanies.size());
        boolean companyFound = false;
        for (Company c : listCompanies) {
            if (company.getId() == c.getId()) {
                Assert.assertEquals(huser.getId(), c.getHUserCreator().getId());
                companyFound = true;
            }
        }
        Assert.assertTrue(companyFound);
        Assert.assertEquals(200, restResponse.getStatus());
    }


    @Test
    public void test13_findAllCompanyWithoutPermissionShouldFail() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // HUser, without permission, tries to find all Company with the following call findAllCompany
        // response status code '403' HyperIoTUnauthorizedException
        huser = createHUser(null);
        Company company = createCompany(huser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(huser.getId(), company.getHUserCreator().getId());
        this.impersonateUser(companyRestApi, huser);
        Response restResponse = companyRestApi.findAllCompany();
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }


    @Test
    public void test14_saveCompanyWithPermissionShouldFailIfBusinessNameIsNull() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // HUser, with permission, tries to save Company with the following call saveCompany,
        // but name is null
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.SAVE);
        huser = createHUser(action);
        Company company = new Company();
        company.setBusinessName(null);
        company.setCity("Lamezia Terme");
        company.setInvoiceAddress("Lamezia Terme");
        company.setNation("Italy");
        company.setPostalCode("88046");
        company.setVatNumber("01234567890" + UUID.randomUUID().toString().replaceAll("-", ""));
        company.setHUserCreator(huser);
        this.impersonateUser(companyRestApi, huser);
        Response restResponse = companyRestApi.saveCompany(company);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(2, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("company-businessname", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getMessage().isEmpty());
        Assert.assertEquals("company-businessname", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getField());
    }


    @Test
    public void test15_saveCompanyWithPermissionShouldFailIfBusinessNameIsEmpty() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // HUser, with permission, tries to save Company with the following call saveCompany,
        // but name is empty
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.SAVE);
        huser = createHUser(action);
        Company company = new Company();
        company.setBusinessName("");
        company.setCity("Lamezia Terme");
        company.setInvoiceAddress("Lamezia Terme");
        company.setNation("Italy");
        company.setPostalCode("88046");
        company.setVatNumber("01234567890" + UUID.randomUUID().toString().replaceAll("-", ""));
        company.setHUserCreator(huser);
        this.impersonateUser(companyRestApi, huser);
        Response restResponse = companyRestApi.saveCompany(company);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("company-businessname", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(company.getBusinessName(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }


    @Test
    public void test16_saveCompanyWithPermissionShouldFailIfBusinessNameIsMaliciousCode() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // HUser, with permission, tries to save Company with the following call saveCompany,
        // but name is malicious code
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.SAVE);
        huser = createHUser(action);
        Company company = new Company();
        company.setBusinessName("expression(malicious code)");
        company.setCity("Lamezia Terme");
        company.setInvoiceAddress("Lamezia Terme");
        company.setNation("Italy");
        company.setPostalCode("88046");
        company.setVatNumber("01234567890" + UUID.randomUUID().toString().replaceAll("-", ""));
        company.setHUserCreator(huser);
        this.impersonateUser(companyRestApi, huser);
        Response restResponse = companyRestApi.saveCompany(company);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("company-businessname", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(company.getBusinessName(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }


    @Test
    public void test17_saveCompanyWithPermissionShouldFailIfCityIsNull() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // HUser, with permission, tries to save Company with the following call saveCompany,
        // but city is null
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.SAVE);
        huser = createHUser(action);
        Company company = new Company();
        company.setBusinessName("ACSoftware");
        company.setCity(null);
        company.setInvoiceAddress("Lamezia Terme");
        company.setNation("Italy");
        company.setPostalCode("88046");
        company.setVatNumber("01234567890" + UUID.randomUUID().toString().replaceAll("-", ""));
        company.setHUserCreator(huser);
        this.impersonateUser(companyRestApi, huser);
        Response restResponse = companyRestApi.saveCompany(company);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(2, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("company-city", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getMessage().isEmpty());
        Assert.assertEquals("company-city", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getField());
    }


    @Test
    public void test18_saveCompanyWithPermissionShouldFailIfCityIsEmpty() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // HUser, with permission, tries to save Company with the following call saveCompany,
        // but city is empty
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.SAVE);
        huser = createHUser(action);
        Company company = new Company();
        company.setBusinessName("ACSoftware");
        company.setCity("");
        company.setInvoiceAddress("Lamezia Terme");
        company.setNation("Italy");
        company.setPostalCode("88046");
        company.setVatNumber("01234567890" + UUID.randomUUID().toString().replaceAll("-", ""));
        company.setHUserCreator(huser);
        this.impersonateUser(companyRestApi, huser);
        Response restResponse = companyRestApi.saveCompany(company);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("company-city", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(company.getCity(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }


    @Test
    public void test19_saveCompanyWithPermissionShouldFailIfCityIsMaliciousCode() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // HUser, with permission, tries to save Company with the following call saveCompany,
        // but city is malicious code
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.SAVE);
        huser = createHUser(action);
        Company company = new Company();
        company.setBusinessName("ACSoftware");
        company.setCity("src='malicious code'");
        company.setInvoiceAddress("Lamezia Terme");
        company.setNation("Italy");
        company.setPostalCode("88046");
        company.setVatNumber("01234567890" + UUID.randomUUID().toString().replaceAll("-", ""));
        company.setHUserCreator(huser);
        this.impersonateUser(companyRestApi, huser);
        Response restResponse = companyRestApi.saveCompany(company);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("company-city", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(company.getCity(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }


    @Test
    public void test20_saveCompanyWithPermissionShouldFailIfInvoiceAddressIsNull() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // HUser, with permission, tries to save Company with the following call saveCompany,
        // but invoice address is null
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.SAVE);
        huser = createHUser(action);
        Company company = new Company();
        company.setBusinessName("ACSoftware");
        company.setCity("Lamezia Terme");
        company.setInvoiceAddress(null);
        company.setNation("Italy");
        company.setPostalCode("88046");
        company.setVatNumber("01234567890" + UUID.randomUUID().toString().replaceAll("-", ""));
        company.setHUserCreator(huser);
        this.impersonateUser(companyRestApi, huser);
        Response restResponse = companyRestApi.saveCompany(company);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(2, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("company-invoiceaddress", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getMessage().isEmpty());
        Assert.assertEquals("company-invoiceaddress", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getField());
    }


    @Test
    public void test21_saveCompanyWithPermissionShouldFailIfInvoiceAddressIsEmpty() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // HUser, with permission, tries to save Company with the following call saveCompany,
        // but invoice address is empty
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.SAVE);
        huser = createHUser(action);
        Company company = new Company();
        company.setBusinessName("ACSoftware");
        company.setCity("Lamezia Terme");
        company.setInvoiceAddress("");
        company.setNation("Italy");
        company.setPostalCode("88046");
        company.setVatNumber("01234567890" + UUID.randomUUID().toString().replaceAll("-", ""));
        company.setHUserCreator(huser);
        this.impersonateUser(companyRestApi, huser);
        Response restResponse = companyRestApi.saveCompany(company);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("company-invoiceaddress", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(company.getInvoiceAddress(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }


    @Test
    public void test22_saveCompanyWithPermissionShouldFailIfInvoiceAddressIsMaliciousCode() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // HUser, with permission, tries to save Company with the following call saveCompany,
        // but invoice address is malicious code
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.SAVE);
        huser = createHUser(action);
        Company company = new Company();
        company.setBusinessName("ACSoftware");
        company.setCity("Lamezia Terme");
        company.setInvoiceAddress("<script malicious code>");
        company.setNation("Italy");
        company.setPostalCode("88046");
        company.setVatNumber("01234567890" + UUID.randomUUID().toString().replaceAll("-", ""));
        company.setHUserCreator(huser);
        this.impersonateUser(companyRestApi, huser);
        Response restResponse = companyRestApi.saveCompany(company);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("company-invoiceaddress", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(company.getInvoiceAddress(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }


    @Test
    public void test23_saveCompanyWithPermissionShouldFailIfNationIsNull() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // HUser, with permission, tries to save Company with the following call saveCompany,
        // but nation is null
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.SAVE);
        huser = createHUser(action);
        Company company = new Company();
        company.setBusinessName("ACSoftware");
        company.setCity("Lamezia Terme");
        company.setInvoiceAddress("Lamezia Terme");
        company.setNation(null);
        company.setPostalCode("88046");
        company.setVatNumber("01234567890" + UUID.randomUUID().toString().replaceAll("-", ""));
        company.setHUserCreator(huser);
        this.impersonateUser(companyRestApi, huser);
        Response restResponse = companyRestApi.saveCompany(company);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(2, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("company-nation", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getMessage().isEmpty());
        Assert.assertEquals("company-nation", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getField());
    }


    @Test
    public void test24_saveCompanyWithPermissionShouldFailIfNationIsEmpty() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // HUser, with permission, tries to save Company with the following call saveCompany,
        // but nation is empty
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.SAVE);
        huser = createHUser(action);
        Company company = new Company();
        company.setBusinessName("ACSoftware");
        company.setCity("Lamezia Terme");
        company.setInvoiceAddress("Lamezia Terme");
        company.setNation("");
        company.setPostalCode("88046");
        company.setVatNumber("01234567890" + UUID.randomUUID().toString().replaceAll("-", ""));
        company.setHUserCreator(huser);
        this.impersonateUser(companyRestApi, huser);
        Response restResponse = companyRestApi.saveCompany(company);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("company-nation", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(company.getNation(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }


    @Test
    public void test25_saveCompanyWithPermissionShouldFailIfNationIsMaliciousCode() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // HUser, with permission, tries to save Company with the following call saveCompany,
        // but nation is malicious code
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.SAVE);
        huser = createHUser(action);
        Company company = new Company();
        company.setBusinessName("ACSoftware");
        company.setCity("Lamezia Terme");
        company.setInvoiceAddress("Lamezia Terme");
        company.setNation("</script>");
        company.setPostalCode("88046");
        company.setVatNumber("01234567890" + UUID.randomUUID().toString().replaceAll("-", ""));
        company.setHUserCreator(huser);
        this.impersonateUser(companyRestApi, huser);
        Response restResponse = companyRestApi.saveCompany(company);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("company-nation", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(company.getNation(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }


    @Test
    public void test26_saveCompanyWithPermissionShouldFailIfPostalCodeIsNull() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // HUser, with permission, tries to save Company with the following call saveCompany,
        // but postal code is null
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.SAVE);
        huser = createHUser(action);
        Company company = new Company();
        company.setBusinessName("ACSoftware");
        company.setCity("Lamezia Terme");
        company.setInvoiceAddress("Lamezia Terme");
        company.setNation("Italy");
        company.setPostalCode(null);
        company.setVatNumber("01234567890" + UUID.randomUUID().toString().replaceAll("-", ""));
        company.setHUserCreator(huser);
        this.impersonateUser(companyRestApi, huser);
        Response restResponse = companyRestApi.saveCompany(company);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(2, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("company-postalcode", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getMessage().isEmpty());
        Assert.assertEquals("company-postalcode", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getField());
    }


    @Test
    public void test27_saveCompanyWithPermissionShouldFailIfPostalCodeIsEmpty() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // HUser, with permission, tries to save Company with the following call saveCompany,
        // but postal code is empty
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.SAVE);
        huser = createHUser(action);
        Company company = new Company();
        company.setBusinessName("ACSoftware");
        company.setCity("Lamezia Terme");
        company.setInvoiceAddress("Lamezia Terme");
        company.setNation("Italy");
        company.setPostalCode("");
        company.setVatNumber("01234567890" + UUID.randomUUID().toString().replaceAll("-", ""));
        company.setHUserCreator(huser);
        this.impersonateUser(companyRestApi, huser);
        Response restResponse = companyRestApi.saveCompany(company);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("company-postalcode", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(company.getPostalCode(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }


    @Test
    public void test28_saveCompanyWithPermissionShouldFailIfPostalCodeIsMaliciousCode() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // HUser, with permission, tries to save Company with the following call saveCompany,
        // but postal code is malicious code
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.SAVE);
        huser = createHUser(action);
        Company company = new Company();
        company.setBusinessName("ACSoftware");
        company.setCity("Lamezia Terme");
        company.setInvoiceAddress("Lamezia Terme");
        company.setNation("Italy");
        company.setPostalCode("<script>malicious code</script>");
        company.setVatNumber("01234567890" + UUID.randomUUID().toString().replaceAll("-", ""));
        company.setHUserCreator(huser);
        this.impersonateUser(companyRestApi, huser);
        Response restResponse = companyRestApi.saveCompany(company);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("company-postalcode", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(company.getPostalCode(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }


    @Test
    public void test29_saveCompanyWithPermissionShouldFailIfVatNumberIsNull() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // HUser, with permission, tries to save Company with the following call saveCompany,
        // but vat number is null
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.SAVE);
        huser = createHUser(action);
        Company company = new Company();
        company.setBusinessName("ACSoftware");
        company.setCity("Lamezia Terme");
        company.setInvoiceAddress("Lamezia Terme");
        company.setNation("Italy");
        company.setPostalCode("88046");
        company.setVatNumber(null);
        company.setHUserCreator(huser);
        this.impersonateUser(companyRestApi, huser);
        Response restResponse = companyRestApi.saveCompany(company);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(2, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("company-vatnumber", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getMessage().isEmpty());
        Assert.assertEquals("company-vatnumber", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getField());
    }


    @Test
    public void test30_saveCompanyWithPermissionShouldFailIfVatNumberIsEmpty() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // HUser, with permission, tries to save Company with the following call saveCompany,
        // but vat number is empty
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.SAVE);
        huser = createHUser(action);
        Company company = new Company();
        company.setBusinessName("ACSoftware");
        company.setCity("Lamezia Terme");
        company.setInvoiceAddress("Lamezia Terme");
        company.setNation("Italy");
        company.setPostalCode("88046");
        company.setVatNumber("");
        company.setHUserCreator(huser);
        this.impersonateUser(companyRestApi, huser);
        Response restResponse = companyRestApi.saveCompany(company);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("company-vatnumber", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(company.getVatNumber(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }


    @Test
    public void test31_saveCompanyWithPermissionShouldFailIfVatNumberIsMaliciousCode() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // HUser, with permission, tries to save Company with the following call saveCompany,
        // but vat number is malicious code
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.SAVE);
        huser = createHUser(action);
        Company company = new Company();
        company.setBusinessName("ACSoftware");
        company.setCity("Lamezia Terme");
        company.setInvoiceAddress("Lamezia Terme");
        company.setNation("Italy");
        company.setPostalCode("88046");
        company.setVatNumber("javascript:");
        company.setHUserCreator(huser);
        this.impersonateUser(companyRestApi, huser);
        Response restResponse = companyRestApi.saveCompany(company);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("company-vatnumber", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(company.getVatNumber(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }


    @Test
    public void test32_findCompanyShouldFailIfHUserTriesToFindAnotherUserCompanyShouldFail() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // huser, with permissions, tries to find Company with the following call findCompany,
        // but huser cannot search for another user's company
        // response status code '404' HyperIoTEntityNotFound
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.FIND);
        huser = createHUser(action);
        huser2 = createHUser(null);
        Company company2 = createCompany(huser2);
        Assert.assertNotEquals(0, company2.getId());
        Assert.assertEquals(huser2.getId(), company2.getHUserCreator().getId());
        this.impersonateUser(companyRestApi, huser2);
        Response restResponse = companyRestApi.findCompany(company2.getId());
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }


    @Test
    public void test33_updateCompanyWithPermissionShouldFailIfBusinessNameIsNull() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // HUser, with permission, tries to update Company with the following call updateCompany,
        // but business name is null
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.UPDATE);
        huser = createHUser(action);
        Company company = createCompany(huser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(huser.getId(), company.getHUserCreator().getId());
        company.setBusinessName(null);
        this.impersonateUser(companyRestApi, huser);
        Response restResponse = companyRestApi.updateCompany(company);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(2, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("company-businessname", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getMessage().isEmpty());
        Assert.assertEquals("company-businessname", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getField());
    }


    @Test
    public void test34_updateCompanyWithPermissionShouldFailIfBusinessNameIsEmpty() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // HUser, with permission, tries to update Company with the following call updateCompany,
        // but business name is empty
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.UPDATE);
        huser = createHUser(action);
        Company company = createCompany(huser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(huser.getId(), company.getHUserCreator().getId());
        company.setBusinessName("");
        this.impersonateUser(companyRestApi, huser);
        Response restResponse = companyRestApi.updateCompany(company);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("company-businessname", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(company.getBusinessName(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }


    @Test
    public void test35_updateCompanyWithPermissionShouldFailIfBusinessNameIsMaliciousCode() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // HUser, with permission, tries to update Company with the following call updateCompany,
        // but business name is malicious code
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.UPDATE);
        huser = createHUser(action);
        Company company = createCompany(huser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(huser.getId(), company.getHUserCreator().getId());
        company.setBusinessName("onload(malicious code)=");
        this.impersonateUser(companyRestApi, huser);
        Response restResponse = companyRestApi.updateCompany(company);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("company-businessname", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(company.getBusinessName(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }


    @Test
    public void test36_updateCompanyWithPermissionShouldFailIfCityIsNull() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // HUser, with permission, tries to update Company with the following call updateCompany,
        // but city is null
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.UPDATE);
        huser = createHUser(action);
        Company company = createCompany(huser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(huser.getId(), company.getHUserCreator().getId());
        company.setCity(null);
        this.impersonateUser(companyRestApi, huser);
        Response restResponse = companyRestApi.updateCompany(company);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(2, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("company-city", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getMessage().isEmpty());
        Assert.assertEquals("company-city", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getField());
    }


    @Test
    public void test37_updateCompanyWithPermissionShouldFailIfCityIsEmpty() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // HUser, with permission, tries to update Company with the following call updateCompany,
        // but city is empty
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.UPDATE);
        huser = createHUser(action);
        Company company = createCompany(huser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(huser.getId(), company.getHUserCreator().getId());
        company.setCity("");
        this.impersonateUser(companyRestApi, huser);
        Response restResponse = companyRestApi.updateCompany(company);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("company-city", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(company.getCity(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }


    @Test
    public void test38_updateCompanyWithPermissionShouldFailIfCityIsMaliciousCode() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // HUser, with permission, tries to update Company with the following call updateCompany,
        // but city is malicious code
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.UPDATE);
        huser = createHUser(action);
        Company company = createCompany(huser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(huser.getId(), company.getHUserCreator().getId());
        company.setCity("eval(malicious code)");
        this.impersonateUser(companyRestApi, huser);
        Response restResponse = companyRestApi.updateCompany(company);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("company-city", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(company.getCity(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }


    @Test
    public void test39_updateCompanyWithPermissionShouldFailIfInvoiceAddressIsNull() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // HUser, with permission, tries to update Company with the following call updateCompany,
        // but invoice address is null
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.UPDATE);
        huser = createHUser(action);
        Company company = createCompany(huser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(huser.getId(), company.getHUserCreator().getId());
        company.setInvoiceAddress(null);
        this.impersonateUser(companyRestApi, huser);
        Response restResponse = companyRestApi.updateCompany(company);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(2, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("company-invoiceaddress", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getMessage().isEmpty());
        Assert.assertEquals("company-invoiceaddress", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getField());
    }


    @Test
    public void test40_updateCompanyWithPermissionShouldFailIfInvoiceAddressIsEmpty() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // HUser, with permission, tries to update Company with the following call updateCompany,
        // but invoice address is empty
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.UPDATE);
        huser = createHUser(action);
        Company company = createCompany(huser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(huser.getId(), company.getHUserCreator().getId());
        company.setInvoiceAddress("");
        this.impersonateUser(companyRestApi, huser);
        Response restResponse = companyRestApi.updateCompany(company);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("company-invoiceaddress", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(company.getInvoiceAddress(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }


    @Test
    public void test41_updateCompanyWithPermissionShouldFailIfInvoiceAddressIsMaliciousCode() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // HUser, with permission, tries to update Company with the following call updateCompany,
        // but invoice address is malicious code
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.UPDATE);
        huser = createHUser(action);
        Company company = createCompany(huser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(huser.getId(), company.getHUserCreator().getId());
        company.setInvoiceAddress("expression(malicious code)");
        this.impersonateUser(companyRestApi, huser);
        Response restResponse = companyRestApi.updateCompany(company);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("company-invoiceaddress", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(company.getInvoiceAddress(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }


    @Test
    public void test42_updateCompanyWithPermissionShouldFailIfNationIsNull() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // HUser, with permission, tries to update Company with the following call updateCompany,
        // but nation is null
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.UPDATE);
        huser = createHUser(action);
        Company company = createCompany(huser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(huser.getId(), company.getHUserCreator().getId());
        company.setNation(null);
        this.impersonateUser(companyRestApi, huser);
        Response restResponse = companyRestApi.updateCompany(company);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(2, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("company-nation", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getMessage().isEmpty());
        Assert.assertEquals("company-nation", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getField());
    }


    @Test
    public void test43_updateCompanyWithPermissionShouldFailIfNationIsEmpty() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // HUser, with permission, tries to update Company with the following call updateCompany,
        // but nation is empty
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.UPDATE);
        huser = createHUser(action);
        Company company = createCompany(huser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(huser.getId(), company.getHUserCreator().getId());
        company.setNation("");
        this.impersonateUser(companyRestApi, huser);
        Response restResponse = companyRestApi.updateCompany(company);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("company-nation", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(company.getNation(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }


    @Test
    public void test44_updateCompanyWithPermissionShouldFailIfNationIsMaliciousCode() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // HUser, with permission, tries to update Company with the following call updateCompany,
        // but nation is malicious code
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.UPDATE);
        huser = createHUser(action);
        Company company = createCompany(huser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(huser.getId(), company.getHUserCreator().getId());
        company.setNation("javascript:");
        this.impersonateUser(companyRestApi, huser);
        Response restResponse = companyRestApi.updateCompany(company);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("company-nation", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(company.getNation(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }


    @Test
    public void test45_updateCompanyWithPermissionShouldFailIfPostalCodeIsNull() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // HUser, with permission, tries to update Company with the following call updateCompany,
        // but postal code is null
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.UPDATE);
        huser = createHUser(action);
        Company company = createCompany(huser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(huser.getId(), company.getHUserCreator().getId());
        company.setPostalCode(null);
        this.impersonateUser(companyRestApi, huser);
        Response restResponse = companyRestApi.updateCompany(company);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(2, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("company-postalcode", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getMessage().isEmpty());
        Assert.assertEquals("company-postalcode", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getField());
    }


    @Test
    public void test46_updateCompanyWithPermissionShouldFailIfPostalCodeIsEmpty() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // HUser, with permission, tries to update Company with the following call updateCompany,
        // but postal code is empty
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.UPDATE);
        huser = createHUser(action);
        Company company = createCompany(huser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(huser.getId(), company.getHUserCreator().getId());
        company.setPostalCode("");
        this.impersonateUser(companyRestApi, huser);
        Response restResponse = companyRestApi.updateCompany(company);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("company-postalcode", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(company.getPostalCode(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }


    @Test
    public void test47_updateCompanyWithPermissionShouldFailIfPostalCodeIsMaliciousCode() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // HUser, with permission, tries to update Company with the following call updateCompany,
        // but postal code is malicious code
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.UPDATE);
        huser = createHUser(action);
        Company company = createCompany(huser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(huser.getId(), company.getHUserCreator().getId());
        company.setPostalCode("vbscript:");
        this.impersonateUser(companyRestApi, huser);
        Response restResponse = companyRestApi.updateCompany(company);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("company-postalcode", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(company.getPostalCode(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }


    @Test
    public void test48_updateCompanyWithPermissionShouldFailIfVatNumberIsNull() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // HUser, with permission, tries to update Company with the following call updateCompany,
        // but vat number is null
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.UPDATE);
        huser = createHUser(action);
        Company company = createCompany(huser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(huser.getId(), company.getHUserCreator().getId());
        company.setVatNumber(null);
        this.impersonateUser(companyRestApi, huser);
        Response restResponse = companyRestApi.updateCompany(company);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(2, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("company-vatnumber", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getMessage().isEmpty());
        Assert.assertEquals("company-vatnumber", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getField());
    }


    @Test
    public void test49_updateCompanyWithPermissionShouldFailIfVatNumberIsEmpty() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // HUser, with permission, tries to update Company with the following call updateCompany,
        // but vat number is empty
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.UPDATE);
        huser = createHUser(action);
        Company company = createCompany(huser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(huser.getId(), company.getHUserCreator().getId());
        company.setVatNumber("");
        this.impersonateUser(companyRestApi, huser);
        Response restResponse = companyRestApi.updateCompany(company);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("company-vatnumber", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(company.getVatNumber(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }


    @Test
    public void test50_updateCompanyWithPermissionShouldFailIfVatNumberIsMaliciousCode() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // HUser, with permission, tries to update Company with the following call updateCompany,
        // but vat number is malicious code
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.UPDATE);
        huser = createHUser(action);
        Company company = createCompany(huser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(huser.getId(), company.getHUserCreator().getId());
        company.setVatNumber("</script>");
        this.impersonateUser(companyRestApi, huser);
        Response restResponse = companyRestApi.updateCompany(company);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("company-vatnumber", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(company.getVatNumber(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }


    @Test
    public void test51_saveCompanyWithPermissionShouldFailIfCompanyBelongsToAnotherUser() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // HUser, with permission, tries to save Company with the following call saveCompany,
        // but HUser belongs to another Company
        // response status code '403' HyperIoTUnauthorizedException
        huser2 = createHUser(null);
        Company company2 = createCompany(huser2);
        Assert.assertNotEquals(0, company2.getId());
        Assert.assertEquals(huser2.getId(), company2.getHUserCreator().getId());

        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.SAVE);
        huser = createHUser(action);
        Assert.assertNotEquals(huser.getId(), huser2.getId());
        Company company = new Company();
        company.setBusinessName("ACSoftware");
        company.setCity("Lamezia Terme");
        company.setInvoiceAddress("Lamezia Terme");
        company.setNation("Italy");
        company.setPostalCode("88046");
        company.setVatNumber("01234567890" + UUID.randomUUID().toString().replaceAll("-", ""));
        company.setHUserCreator(huser2);
        this.impersonateUser(companyRestApi, huser);
        Response restResponse = companyRestApi.saveCompany(company);
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }


    @Test
    public void test52_updateCompanyWithPermissionShouldFailIfEntityNotFound() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // HUser, with permission, tries to update Company with the following call updateCompany,
        // but entity not found
        // response status code '404' HyperIoTEntityNotFound
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.UPDATE);
        huser = createHUser(action);
        Company company = new Company();
        company.setCity("entity not found");
        this.impersonateUser(companyRestApi, huser);
        Response restResponse = companyRestApi.updateCompany(company);
        Assert.assertEquals(404, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }


    @Test
    public void test53_updateCompanyNotFoundWithoutPermissionShouldFail() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // HUser, without permission, tries to update Company not found with
        // the following call updateCompany
        // response status code '404' HyperIoTEntityNotFound
        huser = createHUser(null);
        // entity isn't stored in database
        Company company = new Company();
        company.setCity("Unauthorized");
        this.impersonateUser(companyRestApi, huser);
        Response restResponse = companyRestApi.updateCompany(company);
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }


    @Test
    public void test54_saveCompanyWithPermissionShouldFailIfEntityIsDuplicated() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // HUser, with permission, tries to save Company with the following call saveCompany,
        // but entity is duplicated
        // response status code '409' HyperIoTDuplicateEntityException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.SAVE);
        huser = createHUser(action);
        Company company = createCompany(huser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(huser.getId(), company.getHUserCreator().getId());

        Company duplicateCompany = new Company();
        duplicateCompany.setBusinessName("ACSoftware");
        duplicateCompany.setCity("Lamezia Terme");
        duplicateCompany.setInvoiceAddress("Lamezia Terme");
        duplicateCompany.setNation("Italy");
        duplicateCompany.setPostalCode("88046");
        duplicateCompany.setVatNumber(company.getVatNumber());
        duplicateCompany.setHUserCreator(huser);
        this.impersonateUser(companyRestApi, huser);
        Response restResponse = companyRestApi.saveCompany(duplicateCompany);
        Assert.assertEquals(409, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTDuplicateEntityException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().size());
        boolean vatNumberIsDuplicated = false;
        for (int i = 0; i < ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().size(); i++) {
            if (((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(i).contentEquals("vatNumber")) {
                vatNumberIsDuplicated = true;
                Assert.assertEquals("vatNumber",
                        ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(i));
            }
        }
        Assert.assertTrue(vatNumberIsDuplicated);
    }


    @Test
    public void test55_saveCompanyWithoutPermissionShouldFailAndEntityIsDuplicated() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // HUser, without permission, tries to save Company duplicated with the following call saveCompany
        // response status code '403' HyperIoTUnauthorizedException
        huser = createHUser(null);
        Company company = createCompany(huser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(huser.getId(), company.getHUserCreator().getId());
        Company duplicateCompany = new Company();
        duplicateCompany.setBusinessName("ACSoftware");
        duplicateCompany.setCity("Lamezia Terme");
        duplicateCompany.setInvoiceAddress("Lamezia Terme");
        duplicateCompany.setNation("Italy");
        duplicateCompany.setPostalCode("88046");
        duplicateCompany.setVatNumber(company.getVatNumber());
        duplicateCompany.setHUserCreator(huser);
        this.impersonateUser(companyRestApi, huser);
        Response restResponse = companyRestApi.saveCompany(duplicateCompany);
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }


    @Test
    public void test56_updateCompanyWithPermissionShouldFailIfEntityIsDuplicated() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // HUser, with permission, tries to update Company with the following call updateCompany,
        // but entity is duplicated
        // response status code '409' HyperIoTDuplicateEntityException
        huser = createHUser(null);
        Company company1 = createCompany(huser);
        Assert.assertNotEquals(0, company1.getId());
        Assert.assertEquals(huser.getId(), company1.getHUserCreator().getId());

        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.UPDATE);
        huser2 = createHUser(action);
        Company duplicateCompany = createCompany(huser2);
        Assert.assertNotEquals(0, duplicateCompany.getId());
        Assert.assertNotEquals(company1.getVatNumber(), duplicateCompany.getVatNumber());
        Assert.assertEquals(huser2.getId(), duplicateCompany.getHUserCreator().getId());

        duplicateCompany.setVatNumber(company1.getVatNumber());
        Assert.assertEquals(company1.getVatNumber(), duplicateCompany.getVatNumber());
        this.impersonateUser(companyRestApi, huser2);
        Response restResponse = companyRestApi.updateCompany(duplicateCompany);
        Assert.assertEquals(409, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTDuplicateEntityException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().size());
        boolean vatNumberIsDuplicated = false;
        for (int i = 0; i < ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().size(); i++) {
            if (((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(i).contentEquals("vatNumber")) {
                vatNumberIsDuplicated = true;
                Assert.assertEquals("vatNumber",
                        ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(i));
            }
        }
        Assert.assertTrue(vatNumberIsDuplicated);
    }


    @Test
    public void test57_updateCompanyWithoutPermissionShouldFailAndEntityIsDuplicated() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // HUser, without permission, tries to update Company duplicated with
        // the following call updateCompany
        // response status code '403' HyperIoTUnauthorizedException
        huser = createHUser(null);
        Company company1 = createCompany(huser);
        Assert.assertNotEquals(0, company1.getId());
        Assert.assertEquals(huser.getId(), company1.getHUserCreator().getId());

        huser2 = createHUser(null);
        Company duplicateCompany = createCompany(huser2);
        Assert.assertNotEquals(0, duplicateCompany.getId());
        Assert.assertEquals(huser2.getId(), duplicateCompany.getHUserCreator().getId());
        Assert.assertNotEquals(company1.getVatNumber(), duplicateCompany.getVatNumber());

        duplicateCompany.setVatNumber(company1.getVatNumber());
        Assert.assertEquals(company1.getVatNumber(), duplicateCompany.getVatNumber());

        this.impersonateUser(companyRestApi, huser2);
        Response restResponse = companyRestApi.updateCompany(duplicateCompany);
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }


    @Test
    public void test58_findAllCompanyPaginatedWithPermissionShouldWork() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // In this following call findAllCompany, HUser, with permission,
        // find all Companies with pagination
        // response status code '200'
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.FINDALL);
        huser = createHUser(action);
        int delta = 5;
        int page = 2;
        List<Company> companies = new ArrayList<>();
        int numbEntities = 9;
        for (int i = 0; i < numbEntities; i++) {
            Company company = createCompany(huser);
            Assert.assertNotEquals(0, company.getId());
            Assert.assertEquals(huser.getId(), company.getHUserCreator().getId());
            companies.add(company);
        }
        Assert.assertEquals(numbEntities, companies.size());
        this.impersonateUser(companyRestApi, huser);
        Response restResponse = companyRestApi.findAllCompanyPaginated(delta, page);
        HyperIoTPaginableResult<Company> listCompanies = restResponse
                .readEntity(new GenericType<HyperIoTPaginableResult<Company>>() {
                });
        Assert.assertFalse(listCompanies.getResults().isEmpty());
        Assert.assertEquals(4, listCompanies.getResults().size());
        Assert.assertEquals(delta, listCompanies.getDelta());
        Assert.assertEquals(page, listCompanies.getCurrentPage());
        Assert.assertEquals(defaultPage, listCompanies.getNextPage());
        // delta is 5, page 2: 9 entities stored in database
        Assert.assertEquals(2, listCompanies.getNumPages());
        Assert.assertEquals(200, restResponse.getStatus());

        //checks with page = 1
        this.impersonateUser(companyRestApi, huser);
        Response restResponsePage1 = companyRestApi.findAllCompanyPaginated(delta, 1);
        HyperIoTPaginableResult<Company> listCompaniesPage1 = restResponsePage1
                .readEntity(new GenericType<HyperIoTPaginableResult<Company>>() {
                });
        Assert.assertFalse(listCompaniesPage1.getResults().isEmpty());
        Assert.assertEquals(delta, listCompaniesPage1.getResults().size());
        Assert.assertEquals(delta, listCompaniesPage1.getDelta());
        Assert.assertEquals(defaultPage, listCompaniesPage1.getCurrentPage());
        Assert.assertEquals(page, listCompaniesPage1.getNextPage());
        // delta is 5, page is 1: 9 entities stored in database
        Assert.assertEquals(2, listCompaniesPage1.getNumPages());
        Assert.assertEquals(200, restResponsePage1.getStatus());
    }


    @Test
    public void test59_findAllCompanyPaginatedWithoutPermissionShouldFail() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // In this following call findAllCompany, HUser, without permission,
        // tries to find all Companies with pagination
        // response status code '403' HyperIoTUnauthorizedException
        huser = createHUser(null);
        int delta = 5;
        int page = 2;
        List<Company> companies = new ArrayList<>();
        for (int i = 0; i < delta; i++) {
            Company company = createCompany(huser);
            Assert.assertNotEquals(0, company.getId());
            Assert.assertEquals(huser.getId(), company.getHUserCreator().getId());
            companies.add(company);
        }
        Assert.assertEquals(delta, companies.size());
        this.impersonateUser(companyRestApi, huser);
        Response restResponse = companyRestApi.findAllCompanyPaginated(delta, page);
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }


    @Test
    public void test60_findAllCompanyPaginatedWithPermissionShouldWorkIfDeltaAndPageAreNull() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // In this following call findAllCompany, HUser, with permission,
        // find all Companies with pagination if delta and page are null
        // response status code '200'
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.FINDALL);
        huser = createHUser(action);
        Integer delta = null;
        Integer page = null;
        List<Company> companies = new ArrayList<>();
        int numbEntities = 8;
        for (int i = 0; i < numbEntities; i++) {
            Company company = createCompany(huser);
            Assert.assertNotEquals(0, company.getId());
            Assert.assertEquals(huser.getId(), company.getHUserCreator().getId());
            companies.add(company);
        }
        Assert.assertEquals(numbEntities, companies.size());
        this.impersonateUser(companyRestApi, huser);
        Response restResponse = companyRestApi.findAllCompanyPaginated(delta, page);
        HyperIoTPaginableResult<Company> listCompanies = restResponse
                .readEntity(new GenericType<HyperIoTPaginableResult<Company>>() {
                });
        Assert.assertFalse(listCompanies.getResults().isEmpty());
        Assert.assertEquals(numbEntities, listCompanies.getResults().size());
        Assert.assertEquals(defaultDelta, listCompanies.getDelta());
        Assert.assertEquals(defaultPage, listCompanies.getCurrentPage());
        Assert.assertEquals(defaultPage, listCompanies.getNextPage());
        // default delta is 10, default page is 1: 8 entities stored in database
        Assert.assertEquals(1, listCompanies.getNumPages());
        Assert.assertEquals(200, restResponse.getStatus());
    }


    @Test
    public void test61_findAllCompanyPaginatedWithPermissionShouldWorkIfDeltaIsLowerThanZero() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // In this following call findAllCompany, HUser, with permission,
        // find all Companies with pagination if delta is lower than zero
        // response status code '200'
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.FINDALL);
        huser = createHUser(action);
        int delta = -1;
        int page = 2;
        List<Company> companies = new ArrayList<>();
        int numbEntities = 11;
        for (int i = 0; i < numbEntities; i++) {
            Company company = createCompany(huser);
            Assert.assertNotEquals(0, company.getId());
            Assert.assertEquals(huser.getId(), company.getHUserCreator().getId());
            companies.add(company);
        }
        Assert.assertEquals(numbEntities, companies.size());
        this.impersonateUser(companyRestApi, huser);
        Response restResponsePage2 = companyRestApi.findAllCompanyPaginated(delta, page);
        HyperIoTPaginableResult<Company> listCompaniesPage2 = restResponsePage2
                .readEntity(new GenericType<HyperIoTPaginableResult<Company>>() {
                });
        Assert.assertFalse(listCompaniesPage2.getResults().isEmpty());
        Assert.assertEquals(1, listCompaniesPage2.getResults().size());
        Assert.assertEquals(defaultDelta, listCompaniesPage2.getDelta());
        Assert.assertEquals(page, listCompaniesPage2.getCurrentPage());
        Assert.assertEquals(defaultPage, listCompaniesPage2.getNextPage());
        // default delta is 10, page is 2: 11 entities stored in database
        Assert.assertEquals(2, listCompaniesPage2.getNumPages());
        Assert.assertEquals(200, restResponsePage2.getStatus());

        // checks with page = 1
        this.impersonateUser(companyRestApi, huser);
        Response restResponsePage1 = companyRestApi.findAllCompanyPaginated(delta, 1);
        HyperIoTPaginableResult<Company> listCompaniesPage1 = restResponsePage1
                .readEntity(new GenericType<HyperIoTPaginableResult<Company>>() {
                });
        Assert.assertFalse(listCompaniesPage1.getResults().isEmpty());
        Assert.assertEquals(defaultDelta, listCompaniesPage1.getResults().size());
        Assert.assertEquals(defaultDelta, listCompaniesPage1.getDelta());
        Assert.assertEquals(defaultPage, listCompaniesPage1.getCurrentPage());
        Assert.assertEquals(page, listCompaniesPage1.getNextPage());
        // default delta is 10, page is 1: 11 entities stored in database
        Assert.assertEquals(2, listCompaniesPage1.getNumPages());
        Assert.assertEquals(200, restResponsePage1.getStatus());
    }


    @Test
    public void test62_findAllCompanyPaginatedWithPermissionShouldWorkIfDeltaIsZero() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // In this following call findAllCompany, HUser, with permission,
        // find all Companies with pagination if delta is zero
        // response status code '200'
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.FINDALL);
        huser = createHUser(action);
        int delta = 0;
        int page = 3;
        List<Company> companies = new ArrayList<>();
        int numEntities = 24;
        for (int i = 0; i < numEntities; i++) {
            Company company = createCompany(huser);
            Assert.assertNotEquals(0, company.getId());
            Assert.assertEquals(huser.getId(), company.getHUserCreator().getId());
            companies.add(company);
        }
        Assert.assertEquals(numEntities, companies.size());
        this.impersonateUser(companyRestApi, huser);
        Response restResponse = companyRestApi.findAllCompanyPaginated(delta, page);
        HyperIoTPaginableResult<Company> listCompanies = restResponse
                .readEntity(new GenericType<HyperIoTPaginableResult<Company>>() {
                });
        Assert.assertFalse(listCompanies.getResults().isEmpty());
        Assert.assertEquals(4, listCompanies.getResults().size());
        Assert.assertEquals(defaultDelta, listCompanies.getDelta());
        Assert.assertEquals(page, listCompanies.getCurrentPage());
        Assert.assertEquals(defaultPage, listCompanies.getNextPage());
        // because delta is 10, page is 3: 24 entities stored in database
        Assert.assertEquals(3, listCompanies.getNumPages());
        Assert.assertEquals(200, restResponse.getStatus());

        //checks with page = 1
        this.impersonateUser(companyRestApi, huser);
        Response restResponsePage1 = companyRestApi.findAllCompanyPaginated(delta, 1);
        HyperIoTPaginableResult<Company> listCompaniesPage1 = restResponsePage1
                .readEntity(new GenericType<HyperIoTPaginableResult<Company>>() {
                });
        Assert.assertFalse(listCompaniesPage1.getResults().isEmpty());
        Assert.assertEquals(defaultDelta, listCompaniesPage1.getResults().size());
        Assert.assertEquals(defaultDelta, listCompaniesPage1.getDelta());
        Assert.assertEquals(defaultPage, listCompaniesPage1.getCurrentPage());
        Assert.assertEquals(defaultPage + 1, listCompaniesPage1.getNextPage());
        // default delta is 10, page is 1: 24 entities stored in database
        Assert.assertEquals(3, listCompaniesPage1.getNumPages());
        Assert.assertEquals(200, restResponsePage1.getStatus());

        //checks with page = 2
        this.impersonateUser(companyRestApi, huser);
        Response restResponsePage2 = companyRestApi.findAllCompanyPaginated(delta, 2);
        HyperIoTPaginableResult<Company> listCompaniesPage2 = restResponsePage2
                .readEntity(new GenericType<HyperIoTPaginableResult<Company>>() {
                });
        Assert.assertFalse(listCompaniesPage2.getResults().isEmpty());
        Assert.assertEquals(defaultDelta, listCompaniesPage2.getResults().size());
        Assert.assertEquals(defaultDelta, listCompaniesPage2.getDelta());
        Assert.assertEquals(defaultPage + 1, listCompaniesPage2.getCurrentPage());
        Assert.assertEquals(page, listCompaniesPage2.getNextPage());
        // default delta is 10, page is 2: 24 entities stored in database
        Assert.assertEquals(3, listCompaniesPage2.getNumPages());
        Assert.assertEquals(200, restResponsePage2.getStatus());
    }


    @Test
    public void test63_findAllCompanyPaginatedWithPermissionShouldWorkIfPageIsLowerThanZero() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // In this following call findAllCompany, HUser, with permission,
        // find all Companies with pagination if page is lower than zero
        // response status code '200'
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.FINDALL);
        huser = createHUser(action);
        int delta = 5;
        int page = -1;
        List<Company> companies = new ArrayList<>();
        for (int i = 0; i < delta; i++) {
            Company company = createCompany(huser);
            Assert.assertNotEquals(0, company.getId());
            Assert.assertEquals(huser.getId(), company.getHUserCreator().getId());
            companies.add(company);
        }
        Assert.assertEquals(delta, companies.size());
        this.impersonateUser(companyRestApi, huser);
        Response restResponse = companyRestApi.findAllCompanyPaginated(delta, page);
        HyperIoTPaginableResult<Company> listCompanies = restResponse
                .readEntity(new GenericType<HyperIoTPaginableResult<Company>>() {
                });
        Assert.assertFalse(listCompanies.getResults().isEmpty());
        Assert.assertEquals(delta, listCompanies.getResults().size());
        Assert.assertEquals(delta, listCompanies.getDelta());
        Assert.assertEquals(defaultPage, listCompanies.getCurrentPage());
        Assert.assertEquals(defaultPage, listCompanies.getNextPage());
        // delta is 5, default page 1: 5 entities stored in database
        Assert.assertEquals(1, listCompanies.getNumPages());
        Assert.assertEquals(200, restResponse.getStatus());
    }


    @Test
    public void test64_findAllCompanyPaginatedWithPermissionShouldWorkIfPageIsZero() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // In this following call findAllCompany, HUser, with permission,
        // find all Companies with pagination if page is zero
        // response status code '200'
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.FINDALL);
        huser = createHUser(action);
        int delta = 8;
        int page = 0;
        List<Company> companies = new ArrayList<>();
        for (int i = 0; i < defaultDelta; i++) {
            Company company = createCompany(huser);
            Assert.assertNotEquals(0, company.getId());
            Assert.assertEquals(huser.getId(), company.getHUserCreator().getId());
            companies.add(company);
        }
        Assert.assertEquals(defaultDelta, companies.size());
        this.impersonateUser(companyRestApi, huser);
        Response restResponsePage1 = companyRestApi.findAllCompanyPaginated(delta, page);
        HyperIoTPaginableResult<Company> listCompaniesPage1 = restResponsePage1
                .readEntity(new GenericType<HyperIoTPaginableResult<Company>>() {
                });
        Assert.assertFalse(listCompaniesPage1.getResults().isEmpty());
        Assert.assertEquals(delta, listCompaniesPage1.getResults().size());
        Assert.assertEquals(delta, listCompaniesPage1.getDelta());
        Assert.assertEquals(defaultPage, listCompaniesPage1.getCurrentPage());
        Assert.assertEquals(defaultPage + 1, listCompaniesPage1.getNextPage());
        // delta is 8, default page is 1: 10 entities stored in database
        Assert.assertEquals(2, listCompaniesPage1.getNumPages());
        Assert.assertEquals(200, restResponsePage1.getStatus());

        //checks with page = 2
        Response restResponsePage2 = companyRestApi.findAllCompanyPaginated(delta, 2);
        HyperIoTPaginableResult<Company> listCompaniesPage2 = restResponsePage2
                .readEntity(new GenericType<HyperIoTPaginableResult<Company>>() {
                });
        Assert.assertFalse(listCompaniesPage2.getResults().isEmpty());
        Assert.assertEquals(2, listCompaniesPage2.getResults().size());
        Assert.assertEquals(delta, listCompaniesPage2.getDelta());
        Assert.assertEquals(defaultPage + 1, listCompaniesPage2.getCurrentPage());
        Assert.assertEquals(defaultPage, listCompaniesPage2.getNextPage());
        // delta is 8, page is 2: 10 entities stored in database
        Assert.assertEquals(2, listCompaniesPage2.getNumPages());
        Assert.assertEquals(200, restResponsePage2.getStatus());
    }


    @Test
    public void test65_triesToDeleteCompanyOfAnotherHUserShouldFail() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // HUser, with REMOVE permission, tries to delete Company associated
        // with another HUser, this operation is unauthorized
        // response status code '403' HyperIoTUnauthorizedException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.REMOVE);
        huser = createHUser(action);
        huser2 = createHUser(null);
        Company company2 = createCompany(huser2);
        Assert.assertNotEquals(0, company2.getId());
        Assert.assertEquals(huser2.getId(), company2.getHUserCreator().getId());

        Assert.assertNotEquals(huser.getId(), huser2.getId());
        Assert.assertNotEquals(huser.getId(), company2.getHUserCreator().getId());

        this.impersonateUser(companyRestApi, huser);
        Response restResponseDeleteCompany = companyRestApi.deleteCompany(company2.getId());
        Assert.assertEquals(404, restResponseDeleteCompany.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
                ((HyperIoTBaseError) restResponseDeleteCompany.getEntity()).getType());
    }


    @Test
    public void test66_deleteCompanyWithPermissionNotDeleteInCascadeHUserShouldWord() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // HUser, with REMOVE permission, deletes his associated Company with
        // the following call deleteCompany, this call not delete in cascade
        // mode HUser
        // response status code '200'
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.REMOVE);
        huser = createHUser(action);
        Company company = createCompany(huser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(huser.getId(), company.getHUserCreator().getId());
        this.impersonateUser(companyRestApi, huser);
        Response restResponseDeleteCompany = companyRestApi.deleteCompany(company.getId());
        Assert.assertEquals(200, restResponseDeleteCompany.getStatus());
        // checks: HUser is still stored in database
        HUserRestApi hUserRestApi = getOsgiService(HUserRestApi.class);
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin@hyperiot.com", "admin");
        this.impersonateUser(hUserRestApi, adminUser);
        Response restResponse = hUserRestApi.findHUser(huser.getId());
        Assert.assertEquals(200, restResponse.getStatus());
    }


    @Test
    public void test67_huserTriesToRemoveRelationshipBetweenHUserAndCompanyShouldFail() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // HUser, with UPDATE permission, tries to setting the hUserCreator field to null and removes the relationship
        // between HUser and Company with the following call updateCompany. hUserCreator cannot be changed by update method
        // response status code '200'
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.UPDATE);
        huser = createHUser(action);
        Company company = createCompany(huser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(huser.getId(), company.getHUserCreator().getId());
        company.setHUserCreator(null);
        this.impersonateUser(companyRestApi, huser);
        Response restResponse = companyRestApi.updateCompany(company);
        Assert.assertEquals(200, restResponse.getStatus());
        Assert.assertEquals(huser.getId(), ((Company) restResponse.getEntity()).getHUserCreator().getId());
    }


    @Test
    public void test68_deleteRelationshipBetweenCompanyAndAnotherHUserShouldFail() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // HUser, with UPDATE permission, setting the hUserCreator field to null
        // and tries to removes the relationship between HUser and Company with
        // the following call updateCompany
        // response status code '403' HyperIoTUnauthorizedException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.UPDATE);
        huser = createHUser(action);
        huser2 = createHUser(null);
        Company company2 = createCompany(huser2);
        Assert.assertNotEquals(0, company2.getId());
        Assert.assertEquals(huser2.getId(), company2.getHUserCreator().getId());

        company2.setHUserCreator(null);
        this.impersonateUser(companyRestApi, huser);
        Response restResponse = companyRestApi.updateCompany(company2);
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }


    @Test
    public void test69_updateCompanyWithPermissionShouldFailIfCompanyBelongsToAnotherUser() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // HUser, with permission, tries to update Company with the following call updateCompany,
        // but huser2 belongs to another Company
        // response status code '403' HyperIoTUnauthorizedException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.UPDATE);
        huser = createHUser(action);
        Company company1 = createCompany(huser);
        Assert.assertEquals(huser.getId(), company1.getHUserCreator().getId());

        huser2 = createHUser(null);
        Company company2 = createCompany(huser2);

        Assert.assertEquals(huser2.getId(), company2.getHUserCreator().getId());
        Assert.assertNotEquals(huser.getId(), huser2.getId());
        Assert.assertNotEquals(huser2.getId(), company1.getHUserCreator().getId());

        // huser tries to set huser2 (associated with company2) inside company1
        company1.setHUserCreator(huser2);

        this.impersonateUser(companyRestApi, huser);
        Response restResponse = companyRestApi.updateCompany(company1);
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }


    // Company is Owned Resource: only huser or huser2 is able to find/findAll his entities
    @Test
    public void test70_hadminFindAllCompanyAssociatedWithHUserCreatorShouldSuccess() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin find all Company associated with hUserCreator with the following call findAllCompany
        // response status code '200'
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.FINDALL);
        huser = createHUser(action);
        Company company = createCompany(huser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(huser.getId(), company.getHUserCreator().getId());
        this.impersonateUser(companyRestApi, huser);
        Response restResponse = companyRestApi.findAllCompany();
        List<Company> listCompanies = restResponse.readEntity(new GenericType<List<Company>>() {
        });
        Assert.assertFalse(listCompanies.isEmpty());
        Assert.assertEquals(1, listCompanies.size());
        boolean companyFound = false;
        for (Company companies : listCompanies) {
            if (company.getId() == companies.getId()) {
                companyFound = true;
            }
        }
        Assert.assertTrue(companyFound);
        Assert.assertEquals(200, restResponse.getStatus());

        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        this.impersonateUser(companyRestApi, adminUser);
        Response responseByAdmin = companyRestApi.findAllCompany();
        List<Company> listCompanies1 = responseByAdmin.readEntity(new GenericType<List<Company>>() {
        });
        Assert.assertEquals(1, listCompanies1.size());
        Assert.assertEquals(200, responseByAdmin.getStatus());
    }


    // Company is Owned Resource: only huser or huser2 is able to find/findAll his entities
    @Test
    public void test71_hadminFindCompanyAssociatedWithHUserCreatorShouldSuccess() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin tries to find Company associated with hUserCreator with the following call findCompany
        // response status code '404' HyperIoTEntityNotFound
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.FIND);
        huser = createHUser(action);
        Company company = createCompany(huser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(huser.getId(), company.getHUserCreator().getId());

        this.impersonateUser(companyRestApi, huser);
        Response restResponse = companyRestApi.findCompany(company.getId());
        Assert.assertEquals(200, restResponse.getStatus());
        Assert.assertEquals(huser.getId(), ((Company) restResponse.getEntity()).getHUserCreator().getId());

        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        this.impersonateUser(companyRestApi, adminUser);
        Response responseByAdmin = companyRestApi.findCompany(company.getId());
        Assert.assertEquals(200, responseByAdmin.getStatus());
    }


    @Test
    public void test72_hadminUpdateCompanyAssociatedWithHUserCreatorShouldWork() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin update Company associated with hUserCreator with the following call updateCompany
        // response status code '200'
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.UPDATE);
        huser = createHUser(action);
        Company company = createCompany(huser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(huser.getId(), company.getHUserCreator().getId());

        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        company.setCity("Bologna");
        this.impersonateUser(companyRestApi, adminUser);
        Response restResponse = companyRestApi.updateCompany(company);
        Assert.assertEquals(200, restResponse.getStatus());
        Assert.assertEquals("Bologna", ((Company) restResponse.getEntity()).getCity());
        Assert.assertEquals(company.getEntityVersion() + 1,
                ((Company) restResponse.getEntity()).getEntityVersion());
    }


    @Test
    public void test73_hadminTriesToDeleteCompanyAssociatedWithHUserCreatorShouldSuccess() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin delete Company associated with hUserCreator with the following call deleteCompany
        // response status code '404'
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.REMOVE);
        huser = createHUser(action);
        Company company = createCompany(huser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(huser.getId(), company.getHUserCreator().getId());

        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        this.impersonateUser(companyRestApi, adminUser);
        Response restResponse = companyRestApi.deleteCompany(company.getId());
        Assert.assertEquals(200, restResponse.getStatus());
    }


    @Test
    public void test74_findAllCompanyShouldWorkIfListIsEmpty() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // huser find all Company with the following call findAllCompany
        // there are still no entities saved in the database, this call return an empty list
        // response status code '200'
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.FINDALL);
        huser = createHUser(action);
        this.impersonateUser(companyRestApi, huser);
        Response restResponse = companyRestApi.findAllCompany();
        List<Company> listCompanies = restResponse.readEntity(new GenericType<List<Company>>() {
        });
        Assert.assertTrue(listCompanies.isEmpty());
        Assert.assertEquals(0, listCompanies.size());
        Assert.assertEquals(200, restResponse.getStatus());
    }


    @Test
    public void test75_findAllCompanyPaginatedShouldWorkIfListIsEmpty() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // In this following call findAllCompany, huser find all Companies with pagination
        // there are still no entities saved in the database, this call return an empty list
        // response status code '200'
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.FINDALL);
        huser = createHUser(action);
        this.impersonateUser(companyRestApi, huser);
        Response restResponse = companyRestApi.findAllCompanyPaginated(defaultDelta, defaultPage);
        HyperIoTPaginableResult<Company> listCompanies = restResponse
                .readEntity(new GenericType<HyperIoTPaginableResult<Company>>() {
                });
        Assert.assertTrue(listCompanies.getResults().isEmpty());
        Assert.assertEquals(0, listCompanies.getResults().size());
        Assert.assertEquals(defaultDelta, listCompanies.getDelta());
        Assert.assertEquals(defaultPage, listCompanies.getCurrentPage());
        Assert.assertEquals(defaultPage, listCompanies.getNextPage());
        // default delta is 10, default page is 1: there are not entities stored in database
        Assert.assertEquals(0, listCompanies.getNumPages());
        Assert.assertEquals(200, restResponse.getStatus());
    }

    /*
     *
     *
     * UTILITY METHODS
     *
     *
     */

    private Company createCompany(HUser huser) {
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
        company.setVatNumber("01234567890" + java.util.UUID.randomUUID().toString().replaceAll("-", ""));
        company.setHUserCreator(huser);
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

    private HUser createHUser(HyperIoTAction action) {
        HUserRestApi hUserRestApi = getOsgiService(HUserRestApi.class);
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
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
        this.impersonateUser(hUserRestApi, adminUser);
        Response restResponse = hUserRestApi.saveHUser(huser);
        Assert.assertEquals(200, restResponse.getStatus());
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
            Assert.assertNull(action);
            return null;
        } else {
            PermissionSystemApi permissionSystemApi = getOsgiService(PermissionSystemApi.class);
            Permission testPermission = permissionSystemApi.findByRoleAndResourceName(role, action.getResourceName());
            if (testPermission == null) {
                Permission permission = new Permission();
                permission.setName(companyResourceName + " assigned to huser_id " + huser.getId());
                permission.setActionIds(action.getActionId());
                permission.setEntityResourceName(action.getResourceName());
                permission.setRole(role);
                this.impersonateUser(permissionRestApi, adminUser);
                Response restResponse = permissionRestApi.savePermission(permission);
                testPermission = permission;
                Assert.assertEquals(200, restResponse.getStatus());
            } else {
                this.impersonateUser(permissionRestApi, adminUser);
                testPermission.addPermission(action);
                Response restResponseUpdate = permissionRestApi.updatePermission(testPermission);
                Assert.assertEquals(200, restResponseUpdate.getStatus());
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
        role.setName("Role" + java.util.UUID.randomUUID());
        role.setDescription("Description");
        Response restResponse = roleRestApi.saveRole(role);
        Assert.assertEquals(200, restResponse.getStatus());
        Assert.assertNotEquals(0, ((Role) restResponse.getEntity()).getId());
        Assert.assertEquals(role.getName(), ((Role) restResponse.getEntity()).getName());
        Assert.assertEquals("Description", ((Role) restResponse.getEntity()).getDescription());
        return role;
    }

    private Permission addPermission(HUser huser, HyperIoTAction action){
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Role role = createRole();
        huser.addRole(role);
        RoleRestApi roleRestApi = getOsgiService(RoleRestApi.class);
        this.impersonateUser(roleRestApi, adminUser);
        Response restUserRole = roleRestApi.saveUserRole(role.getId(), huser.getId());
        Assert.assertEquals(200, restUserRole.getStatus());
        Assert.assertTrue(huser.hasRole(role));
        Permission permission = utilGrantPermission(huser, role, action);
        return permission;
    }


    // Company is Owned Resource: only huser or huser2 is able to find/findAll his entities
    private HUser huser;
    private HUser huser2;

    @After
    public void afterTest() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.FINDALL);
        HyperIoTAction action1 = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.REMOVE);
        if ((huser != null) && (huser.isActive())) {
            addPermission(huser, action);
            addPermission(huser, action1);
            this.impersonateUser(companyRestApi, huser);
            Response restResponse = companyRestApi.findAllCompany();
            List<Company> listCompanies = restResponse.readEntity(new GenericType<List<Company>>() {
            });
            if (!listCompanies.isEmpty()) {
                Assert.assertFalse(listCompanies.isEmpty());
                for (Company company : listCompanies) {
                    this.impersonateUser(companyRestApi, huser);
                    Response restResponse1 = companyRestApi.deleteCompany(company.getId());
                    Assert.assertEquals(200, restResponse1.getStatus());
                    Assert.assertNull(restResponse1.getEntity());
                }
            }
        }
        if ((huser2 != null) && (huser2.isActive())) {
            addPermission(huser2, action);
            addPermission(huser2, action1);
            this.impersonateUser(companyRestApi, huser2);
            Response restResponse = companyRestApi.findAllCompany();
            List<Company> listCompanies = restResponse.readEntity(new GenericType<List<Company>>() {
            });
            if (!listCompanies.isEmpty()) {
                Assert.assertFalse(listCompanies.isEmpty());
                for (Company company : listCompanies) {
                    this.impersonateUser(companyRestApi, huser2);
                    Response restResponse1 = companyRestApi.deleteCompany(company.getId());
                    Assert.assertEquals(200, restResponse1.getStatus());
                    Assert.assertNull(restResponse1.getEntity());
                }
            }
        }

        // Remove all roles and permissions (in cascade mode) created in every test
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        RoleRestApi roleRestService = getOsgiService(RoleRestApi.class);
        this.impersonateUser(roleRestService, adminUser);
        Response restResponseRole = roleRestService.findAllRoles();
        List<Role> listRoles = restResponseRole.readEntity(new GenericType<List<Role>>() {
        });
        if (!listRoles.isEmpty()) {
            Assert.assertFalse(listRoles.isEmpty());
            for (Role role : listRoles) {
                if (!role.getName().contains("RegisteredUser")) {
                    this.impersonateUser(roleRestService, adminUser);
                    Response restResponseRole1 = roleRestService.deleteRole(role.getId());
                    Assert.assertEquals(200, restResponseRole1.getStatus());
                    Assert.assertNull(restResponseRole1.getEntity());
                }
            }
        }
        // Remove all husers created in every test
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        this.impersonateUser(huserRestService, adminUser);
        Response restResponseUsers = huserRestService.findAllHUser();
        List<HUser> listHUsers = restResponseUsers.readEntity(new GenericType<List<HUser>>() {
        });
        if (!listHUsers.isEmpty()) {
            Assert.assertFalse(listHUsers.isEmpty());
            for (HUser huser : listHUsers) {
                if (!huser.isAdmin()) {
                    this.impersonateUser(huserRestService, adminUser);
                    Response restResponse1 = huserRestService.deleteHUser(huser.getId());
                    Assert.assertEquals(200, restResponse1.getStatus());
                    Assert.assertNull(restResponse1.getEntity());
                }
            }
        }
//        String sqlCompany = "select * from company";
//        String resultCompany = executeCommand("jdbc:query hyperiot " + sqlCompany);
//        System.out.println(resultCompany);
//
//        String sqlHUser = "select h.id, h.username, h.admin from huser h";
//        String resultHUser = executeCommand("jdbc:query hyperiot " + sqlHUser);
//        System.out.println(resultHUser);
//
//        String sqlRole = "select * from role";
//        String resultRole = executeCommand("jdbc:query hyperiot " + sqlRole);
//        System.out.println(resultRole);
//
//        String sqlPermission = "select * from permission";
//        String resultPermission = executeCommand("jdbc:query hyperiot " + sqlPermission);
//        System.out.println(resultPermission);
    }


}
