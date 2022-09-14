package it.acsoftware.hyperiot.company.test;

import it.acsoftware.hyperiot.base.api.authentication.AuthenticationApi;
import it.acsoftware.hyperiot.base.action.HyperIoTActionName;
import it.acsoftware.hyperiot.base.action.util.HyperIoTActionsUtil;
import it.acsoftware.hyperiot.base.action.util.HyperIoTCrudAction;
import it.acsoftware.hyperiot.base.api.HyperIoTAction;
import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.base.api.HyperIoTUser;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTPaginableResult;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTQuery;
import it.acsoftware.hyperiot.base.exception.HyperIoTNoResultException;
import it.acsoftware.hyperiot.base.model.HyperIoTBaseError;
import it.acsoftware.hyperiot.base.service.rest.HyperIoTBaseRestApi;
import it.acsoftware.hyperiot.base.test.HyperIoTTestConfigurationBuilder;
import it.acsoftware.hyperiot.base.util.HyperIoTConstants;
import it.acsoftware.hyperiot.company.api.CompanySystemApi;
import it.acsoftware.hyperiot.company.model.Company;
import it.acsoftware.hyperiot.company.service.rest.CompanyRestApi;
import it.acsoftware.hyperiot.huser.model.HUser;
import it.acsoftware.hyperiot.huser.service.rest.HUserRestApi;
import it.acsoftware.hyperiot.osgi.util.filter.OSGiFilterBuilder;
import it.acsoftware.hyperiot.permission.api.PermissionSystemApi;
import it.acsoftware.hyperiot.permission.model.Permission;
import it.acsoftware.hyperiot.permission.service.rest.PermissionRestApi;
import it.acsoftware.hyperiot.query.util.filter.HyperIoTQueryBuilder;
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
public class HyperIoTCompanyRestTest extends KarafTestSupport {

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
    public void test02_saveCompanyShouldWork() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin save Company with the following call saveCompany
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = new Company();
        company.setBusinessName("ACSoftware");
        company.setCity("Lamezia Terme");
        company.setInvoiceAddress("Lamezia Terme");
        company.setNation("Italy");
        company.setPostalCode("88046");
        company.setVatNumber("01234567890" + java.util.UUID.randomUUID().toString().replaceAll("-", ""));
        company.setHUserCreator((HUser) adminUser);
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
        Assert.assertEquals(adminUser.getId(), ((Company) restResponse.getEntity()).getHUserCreator().getId());
    }


    @Test
    public void test03_saveCompanyShouldFailIfNotLogged() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // the following call tries to save Company, but HUser is not logged
        // response status code '403' HyperIoTUnauthorizedException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = new Company();
        company.setBusinessName("ACSoftware");
        company.setCity("Lamezia Terme");
        company.setInvoiceAddress("Lamezia Terme");
        company.setNation("Italy");
        company.setPostalCode("88046");
        company.setVatNumber("01234567890" + java.util.UUID.randomUUID().toString().replaceAll("-", ""));
        company.setHUserCreator((HUser) adminUser);
        this.impersonateUser(companyRestApi, null);
        Response restResponse = companyRestApi.saveCompany(company);
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }


    @Test
    public void test04_updateCompanyShouldWork() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin update Company with the following call updateCompany
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = createCompany((HUser) adminUser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(adminUser.getId(), company.getHUserCreator().getId());
        company.setCity("Bologna");
        this.impersonateUser(companyRestApi, adminUser);
        Response restResponse = companyRestApi.updateCompany(company);
        Assert.assertEquals(200, restResponse.getStatus());
        Assert.assertEquals("Bologna", ((Company) restResponse.getEntity()).getCity());
        Assert.assertEquals(company.getEntityVersion() + 1,
                ((Company) restResponse.getEntity()).getEntityVersion());
    }


    @Test
    public void test05_updateCompanyShouldFailIfNotLogged() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // the following call tries to update Company, but HUser is not logged
        // response status code '403' HyperIoTUnauthorizedException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = createCompany((HUser) adminUser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(adminUser.getId(), company.getHUserCreator().getId());
        company.setCity("Bologna");
        this.impersonateUser(companyRestApi, null);
        Response restResponse = companyRestApi.updateCompany(company);
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }


    @Test
    public void test06_findCompanyShouldWork() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin find Company with the following call findCompany
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = createCompany((HUser) adminUser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(adminUser.getId(), company.getHUserCreator().getId());
        this.impersonateUser(companyRestApi, adminUser);
        Response restResponse = companyRestApi.findCompany(company.getId());
        Assert.assertEquals(200, restResponse.getStatus());
        Assert.assertEquals(company.getId(), ((Company) restResponse.getEntity()).getId());
        Assert.assertEquals(adminUser.getId(), ((Company) restResponse.getEntity()).getHUserCreator().getId());
    }


    @Test
    public void test07_findCompanyShouldFailIfEntityNotFound() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin tries to find Company with the following call findCompany,
        // but entity not found
        // response status code '404' HyperIoTEntityNotFound
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        this.impersonateUser(companyRestApi, adminUser);
        Response restResponse = companyRestApi.findCompany(0);
        Assert.assertEquals(404, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }


    @Test
    public void test08_findCompanyShouldFailIfNotLogged() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // the following call tries to find Company, but HUser is not logged
        // response status code '403' HyperIoTUnauthorizedException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = createCompany((HUser) adminUser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(adminUser.getId(), company.getHUserCreator().getId());
        this.impersonateUser(companyRestApi, null);
        Response restResponse = companyRestApi.findCompany(company.getId());
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }


    @Test
    public void test09_findAllCompanyShouldWork() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin find all Company with the following call findAllCompany
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = createCompany((HUser) adminUser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(adminUser.getId(), company.getHUserCreator().getId());
        this.impersonateUser(companyRestApi, adminUser);
        Response restResponse = companyRestApi.findAllCompany();
        List<Company> listCompanies = restResponse.readEntity(new GenericType<List<Company>>() {
        });
        Assert.assertFalse(listCompanies.isEmpty());
        Assert.assertEquals(1, listCompanies.size());
        boolean companyFound = false;
        for (Company c : listCompanies) {
            if (company.getId() == c.getId()) {
                Assert.assertEquals(adminUser.getId(), c.getHUserCreator().getId());
                companyFound = true;
            }
        }
        Assert.assertTrue(companyFound);
        Assert.assertEquals(200, restResponse.getStatus());
    }


    @Test
    public void test10_findAllCompanyShouldFailIfNotLogged() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // the following call tries to find all Company, but HUser is not logged
        // response status code '403' HyperIoTUnauthorizedException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = createCompany((HUser) adminUser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(adminUser.getId(), company.getHUserCreator().getId());
        this.impersonateUser(companyRestApi, null);
        Response restResponse = companyRestApi.findAllCompany();
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }


    @Test
    public void test11_deleteCompanyShouldWork() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin delete Company with the following call deleteCompany
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = createCompany((HUser) adminUser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(adminUser.getId(), company.getHUserCreator().getId());
        this.impersonateUser(companyRestApi, adminUser);
        Response restResponse = companyRestApi.deleteCompany(company.getId());
        Assert.assertEquals(200, restResponse.getStatus());
        Assert.assertNull(restResponse.getEntity());
    }

    @Test
    public void test12_deleteCompanyShouldFailIfEntityNotFound() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin tries to delete Company with the following call deleteCompany,
        // but entity not found
        // response status code '404' HyperIoTEntityNotFound
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        this.impersonateUser(companyRestApi, adminUser);
        Response restResponse = companyRestApi.deleteCompany(0);
        Assert.assertEquals(404, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test13_deleteCompanyShouldFailIfNotLogged() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // the following call tries to find all Company, but HUser is not logged
        // response status code '403' HyperIoTUnauthorizedException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = createCompany((HUser) adminUser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(adminUser.getId(), company.getHUserCreator().getId());
        this.impersonateUser(companyRestApi, null);
        Response restResponse = companyRestApi.deleteCompany(company.getId());
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }


    @Test
    public void test14_saveCompanyShouldFailIfBusinessNameIsNull() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin tries to save Company with the following call saveCompany,
        // but business name is null
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = new Company();
        company.setBusinessName(null);
        company.setCity("Lamezia Terme");
        company.setInvoiceAddress("Lamezia Terme");
        company.setNation("Italy");
        company.setPostalCode("88046");
        company.setVatNumber("01234567890" + java.util.UUID.randomUUID().toString().replaceAll("-", ""));
        company.setHUserCreator((HUser) adminUser);
        this.impersonateUser(companyRestApi, adminUser);
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
    public void test15_saveCompanyShouldFailIfBusinessNameIsEmpty() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin tries to save Company with the following call saveCompany,
        // but business name is empty
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = new Company();
        company.setBusinessName("");
        company.setCity("Lamezia Terme");
        company.setInvoiceAddress("Lamezia Terme");
        company.setNation("Italy");
        company.setPostalCode("88046");
        company.setVatNumber("01234567890" + java.util.UUID.randomUUID().toString().replaceAll("-", ""));
        company.setHUserCreator((HUser) adminUser);
        this.impersonateUser(companyRestApi, adminUser);
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
    public void test16_saveCompanyShouldFailIfBusinessNameIsMaliciousCode() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin tries to save Company with the following call saveCompany,
        // but business name is malicious code
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = new Company();
        company.setBusinessName("expression(malicious code)");
        company.setCity("Lamezia Terme");
        company.setInvoiceAddress("Lamezia Terme");
        company.setNation("Italy");
        company.setPostalCode("88046");
        company.setVatNumber("01234567890" + java.util.UUID.randomUUID().toString().replaceAll("-", ""));
        company.setHUserCreator((HUser) adminUser);
        this.impersonateUser(companyRestApi, adminUser);
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
    public void test17_saveCompanyShouldFailIfCityIsNull() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin tries to save Company with the following call saveCompany,
        // but city is null
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = new Company();
        company.setBusinessName("ACSoftware");
        company.setCity(null);
        company.setInvoiceAddress("Lamezia Terme");
        company.setNation("Italy");
        company.setPostalCode("88046");
        company.setVatNumber("01234567890" + java.util.UUID.randomUUID().toString().replaceAll("-", ""));
        company.setHUserCreator((HUser) adminUser);
        this.impersonateUser(companyRestApi, adminUser);
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
    public void test18_saveCompanyShouldFailIfCityIsEmpty() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin tries to save Company with the following call saveCompany,
        // but city is empty
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = new Company();
        company.setBusinessName("ACSoftware");
        company.setCity("");
        company.setInvoiceAddress("Lamezia Terme");
        company.setNation("Italy");
        company.setPostalCode("88046");
        company.setVatNumber("01234567890" + java.util.UUID.randomUUID().toString().replaceAll("-", ""));
        company.setHUserCreator((HUser) adminUser);
        this.impersonateUser(companyRestApi, adminUser);
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
    public void test19_saveCompanyShouldFailIfCityIsMaliciousCode() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin tries to save Company with the following call saveCompany,
        // but city is malicious code
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = new Company();
        company.setBusinessName("ACSoftware");
        company.setCity("src='malicious code'");
        company.setInvoiceAddress("Lamezia Terme");
        company.setNation("Italy");
        company.setPostalCode("88046");
        company.setVatNumber("01234567890" + java.util.UUID.randomUUID().toString().replaceAll("-", ""));
        company.setHUserCreator((HUser) adminUser);
        this.impersonateUser(companyRestApi, adminUser);
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
    public void test20_saveCompanyShouldFailIfInvoiceAddressIsNull() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin tries to save Company with the following call saveCompany,
        // but invoice address is null
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = new Company();
        company.setBusinessName("ACSoftware");
        company.setCity("Lamezia Terme");
        company.setInvoiceAddress(null);
        company.setNation("Italy");
        company.setPostalCode("88046");
        company.setVatNumber("01234567890" + java.util.UUID.randomUUID().toString().replaceAll("-", ""));
        company.setHUserCreator((HUser) adminUser);
        this.impersonateUser(companyRestApi, adminUser);
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
    public void test21_saveCompanyShouldFailIfInvoiceAddressIsEmpty() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin tries to save Company with the following call saveCompany,
        // but invoice address is empty
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = new Company();
        company.setBusinessName("ACSoftware");
        company.setCity("Lamezia Terme");
        company.setInvoiceAddress("");
        company.setNation("Italy");
        company.setPostalCode("88046");
        company.setVatNumber("01234567890" + java.util.UUID.randomUUID().toString().replaceAll("-", ""));
        company.setHUserCreator((HUser) adminUser);
        this.impersonateUser(companyRestApi, adminUser);
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
    public void test22_saveCompanyShouldFailIfInvoiceAddressIsMaliciousCode() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin tries to save Company with the following call saveCompany,
        // but invoice address is malicious code
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = new Company();
        company.setBusinessName("ACSoftware");
        company.setCity("Lamezia Terme");
        company.setInvoiceAddress("<script malicious code>");
        company.setNation("Italy");
        company.setPostalCode("88046");
        company.setVatNumber("01234567890" + java.util.UUID.randomUUID().toString().replaceAll("-", ""));
        company.setHUserCreator((HUser) adminUser);
        this.impersonateUser(companyRestApi, adminUser);
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
    public void test23_saveCompanyShouldFailIfNationIsNull() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin tries to save Company with the following call saveCompany,
        // but nation is null
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = new Company();
        company.setBusinessName("ACSoftware");
        company.setCity("Lamezia Terme");
        company.setInvoiceAddress("Lamezia Terme");
        company.setNation(null);
        company.setPostalCode("88046");
        company.setVatNumber("01234567890" + java.util.UUID.randomUUID().toString().replaceAll("-", ""));
        company.setHUserCreator((HUser) adminUser);
        this.impersonateUser(companyRestApi, adminUser);
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
    public void test24_saveCompanyShouldFailIfNationIsEmpty() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin tries to save Company with the following call saveCompany,
        // but nation is empty
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = new Company();
        company.setBusinessName("ACSoftware");
        company.setCity("Lamezia Terme");
        company.setInvoiceAddress("Lamezia Terme");
        company.setNation("");
        company.setPostalCode("88046");
        company.setVatNumber("01234567890" + java.util.UUID.randomUUID().toString().replaceAll("-", ""));
        company.setHUserCreator((HUser) adminUser);
        this.impersonateUser(companyRestApi, adminUser);
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
    public void test25_saveCompanyShouldFailIfNationIsMaliciousCode() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin tries to save Company with the following call saveCompany,
        // but nation is malicious code
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = new Company();
        company.setBusinessName("ACSoftware");
        company.setCity("Lamezia Terme");
        company.setInvoiceAddress("Lamezia Terme");
        company.setNation("</script>");
        company.setPostalCode("88046");
        company.setVatNumber("01234567890" + java.util.UUID.randomUUID().toString().replaceAll("-", ""));
        company.setHUserCreator((HUser) adminUser);
        this.impersonateUser(companyRestApi, adminUser);
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
    public void test26_saveCompanyShouldFailIfPostalCodeIsNull() {
        // hadmin tries to save Company with the following call saveCompany,
        // but postal code is null
        // response status code '422' HyperIoTValidationException
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = new Company();
        company.setBusinessName("ACSoftware");
        company.setCity("Lamezia Terme");
        company.setInvoiceAddress("Lamezia Terme");
        company.setNation("Italy");
        company.setPostalCode(null);
        company.setVatNumber("01234567890" + java.util.UUID.randomUUID().toString().replaceAll("-", ""));
        company.setHUserCreator((HUser) adminUser);
        this.impersonateUser(companyRestApi, adminUser);
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
    public void test27_saveCompanyShouldFailIfPostalCodeIsEmpty() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin tries to save Company with the following call saveCompany,
        // but postal code is empty
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = new Company();
        company.setBusinessName("ACSoftware");
        company.setCity("Lamezia Terme");
        company.setInvoiceAddress("Lamezia Terme");
        company.setNation("Italy");
        company.setPostalCode("");
        company.setVatNumber("01234567890" + java.util.UUID.randomUUID().toString().replaceAll("-", ""));
        company.setHUserCreator((HUser) adminUser);
        this.impersonateUser(companyRestApi, adminUser);
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
    public void test28_saveCompanyShouldFailIfPostalCodeIsMaliciousCode() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin tries to save Company with the following call saveCompany,
        // but postal code is malicious code
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = new Company();
        company.setBusinessName("ACSoftware");
        company.setCity("Lamezia Terme");
        company.setInvoiceAddress("Lamezia Terme");
        company.setNation("Italy");
        company.setPostalCode("<script>malicious code</script>");
        company.setVatNumber("01234567890" + java.util.UUID.randomUUID().toString().replaceAll("-", ""));
        company.setHUserCreator((HUser) adminUser);
        this.impersonateUser(companyRestApi, adminUser);
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
    public void test29_saveCompanyShouldFailIfVatNumberIsNull() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin tries to save Company with the following call saveCompany,
        // but vat number is null
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = new Company();
        company.setBusinessName("ACSoftware");
        company.setCity("Lamezia Terme");
        company.setInvoiceAddress("Lamezia Terme");
        company.setNation("Italy");
        company.setPostalCode("88046");
        company.setVatNumber(null);
        company.setHUserCreator((HUser) adminUser);
        this.impersonateUser(companyRestApi, adminUser);
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
    public void test30_saveCompanyShouldFailIfVatNumberIsEmpty() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin tries to save Company with the following call saveCompany,
        // but vat number is empty
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = new Company();
        company.setBusinessName("ACSoftware");
        company.setCity("Lamezia Terme");
        company.setInvoiceAddress("Lamezia Terme");
        company.setNation("Italy");
        company.setPostalCode("88046");
        company.setVatNumber("");
        company.setHUserCreator((HUser) adminUser);
        this.impersonateUser(companyRestApi, adminUser);
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
    public void test31_saveCompanyShouldFailIfVatNumberIsMaliciousCode() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin tries to save Company with the following call saveCompany,
        // but vat number is malicious code
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = new Company();
        company.setBusinessName("ACSoftware");
        company.setCity("Lamezia Terme");
        company.setInvoiceAddress("Lamezia Terme");
        company.setNation("Italy");
        company.setPostalCode("88046");
        company.setVatNumber("eval(malicious code)");
        company.setHUserCreator((HUser) adminUser);
        this.impersonateUser(companyRestApi, adminUser);
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
    public void test32_hadminFindCompanyAssociatedWithAdminShouldSuccess() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin tries to find all Company associated with hUserCreator with the following call findCompany.
        // Company is Owned Resource: only huser able to find his entities
        // response status code '404' HyperIoTEntityNotFound
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        huser = createHUser(null);
        Company company = createCompany(huser);
        this.impersonateUser(companyRestApi, adminUser);
        Response restResponse = companyRestApi.findCompany(company.getId());
        Assert.assertEquals(200, restResponse.getStatus());
    }


    @Test
    public void test33_updateCompanyShouldFailIfBusinessNameIsNull() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin tries to update Company with the following call updateCompany,
        // but business name is null
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = createCompany((HUser) adminUser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(adminUser.getId(), company.getHUserCreator().getId());
        company.setBusinessName(null);
        this.impersonateUser(companyRestApi, adminUser);
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
    public void test34_updateCompanyShouldFailIfBusinessNameIsEmpty() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin tries to update Company with the following call updateCompany,
        // but business name is empty
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = createCompany((HUser) adminUser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(adminUser.getId(), company.getHUserCreator().getId());
        company.setBusinessName("");
        this.impersonateUser(companyRestApi, adminUser);
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
    public void test35_updateCompanyShouldFailIfBusinessNameIsMaliciousCode() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin tries to update Company with the following call updateCompany,
        // but business name is malicious code
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = createCompany((HUser) adminUser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(adminUser.getId(), company.getHUserCreator().getId());
        company.setBusinessName("<script malicious code>");
        this.impersonateUser(companyRestApi, adminUser);
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
    public void test36_updateCompanyShouldFailIfCityIsNull() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin tries to update Company with the following call updateCompany,
        // but city is null
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = createCompany((HUser) adminUser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(adminUser.getId(), company.getHUserCreator().getId());
        company.setCity(null);
        this.impersonateUser(companyRestApi, adminUser);
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
    public void test37_updateCompanyShouldFailIfCityIsEmpty() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin tries to update Company with the following call updateCompany,
        // but city is empty
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = createCompany((HUser) adminUser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(adminUser.getId(), company.getHUserCreator().getId());
        company.setCity("");
        this.impersonateUser(companyRestApi, adminUser);
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
    public void test38_updateCompanyShouldFailIfCityIsMaliciousCode() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin tries to update Company with the following call updateCompany,
        // but city is malicious code
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = createCompany((HUser) adminUser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(adminUser.getId(), company.getHUserCreator().getId());
        company.setCity("</script>");
        this.impersonateUser(companyRestApi, adminUser);
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
    public void test39_updateCompanyShouldFailIfInvoiceAddressIsNull() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin tries to update Company with the following call updateCompany,
        // but invoice address is null
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = createCompany((HUser) adminUser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(adminUser.getId(), company.getHUserCreator().getId());
        company.setInvoiceAddress(null);
        this.impersonateUser(companyRestApi, adminUser);
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
    public void test40_updateCompanyShouldFailIfInvoiceAddressIsEmpty() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin tries to update Company with the following call updateCompany,
        // but invoice address is empty
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = createCompany((HUser) adminUser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(adminUser.getId(), company.getHUserCreator().getId());
        company.setInvoiceAddress("");
        this.impersonateUser(companyRestApi, adminUser);
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
    public void test41_updateCompanyShouldFailIfInvoiceAddressIsMaliciousCode() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin tries to update Company with the following call updateCompany,
        // but invoice address is malicious code
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = createCompany((HUser) adminUser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(adminUser.getId(), company.getHUserCreator().getId());
        company.setInvoiceAddress("vbscript:");
        this.impersonateUser(companyRestApi, adminUser);
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
    public void test42_updateCompanyShouldFailIfNationIsNull() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin tries to update Company with the following call updateCompany,
        // but nation is null
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = createCompany((HUser) adminUser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(adminUser.getId(), company.getHUserCreator().getId());
        company.setNation(null);
        this.impersonateUser(companyRestApi, adminUser);
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
    public void test43_updateCompanyShouldFailIfNationIsEmpty() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin tries to update Company with the following call updateCompany,
        // but nation is empty
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = createCompany((HUser) adminUser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(adminUser.getId(), company.getHUserCreator().getId());
        company.setNation("");
        this.impersonateUser(companyRestApi, adminUser);
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
    public void test44_updateCompanyShouldFailIfNationIsMaliciousCode() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin tries to update Company with the following call updateCompany,
        // but nation is malicious code
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = createCompany((HUser) adminUser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(adminUser.getId(), company.getHUserCreator().getId());
        company.setNation("onload(malicious code)=");
        this.impersonateUser(companyRestApi, adminUser);
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
    public void test45_updateCompanyShouldFailIfPostalCodeIsNull() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin tries to update Company with the following call updateCompany,
        // but postal code is null
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = createCompany((HUser) adminUser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(adminUser.getId(), company.getHUserCreator().getId());
        company.setPostalCode(null);
        this.impersonateUser(companyRestApi, adminUser);
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
    public void test46_updateCompanyShouldFailIfPostalCodeIsEmpty() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin tries to update Company with the following call updateCompany,
        // but postal code is empty
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = createCompany((HUser) adminUser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(adminUser.getId(), company.getHUserCreator().getId());
        company.setPostalCode("");
        this.impersonateUser(companyRestApi, adminUser);
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
    public void test47_updateCompanyShouldFailIfPostalCodeIsMaliciousCode() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin tries to update Company with the following call updateCompany,
        // but postal code is malicious code
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = createCompany((HUser) adminUser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(adminUser.getId(), company.getHUserCreator().getId());
        company.setPostalCode("expression(malicious code)");
        this.impersonateUser(companyRestApi, adminUser);
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
    public void test48_updateCompanyShouldFailIfVatNumberIsNull() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin tries to update Company with the following call updateCompany,
        // but vat number is null
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = createCompany((HUser) adminUser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(adminUser.getId(), company.getHUserCreator().getId());
        company.setVatNumber(null);
        this.impersonateUser(companyRestApi, adminUser);
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
    public void test49_updateCompanyShouldFailIfVatNumberIsEmpty() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin tries to update Company with the following call updateCompany,
        // but vat number is empty
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = createCompany((HUser) adminUser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(adminUser.getId(), company.getHUserCreator().getId());
        company.setVatNumber("");
        this.impersonateUser(companyRestApi, adminUser);
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
    public void test50_updateCompanyShouldFailIfVatNumberIsMaliciousCode() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin tries to update Company with the following call updateCompany,
        // but vat number is malicious code
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = createCompany((HUser) adminUser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(adminUser.getId(), company.getHUserCreator().getId());
        company.setVatNumber("eval(malicious code)");
        this.impersonateUser(companyRestApi, adminUser);
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
    public void test51_updateCompanyShouldFailIfEntityNotFound() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin tries to update Company with the following call updateCompany,
        // but entity not found
        // response status code '404' HyperIoTEntityNotFound
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = new Company();
        company.setCity("entity not found");
        this.impersonateUser(companyRestApi, adminUser);
        Response restResponse = companyRestApi.updateCompany(company);
        Assert.assertEquals(404, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }


    @Test
    public void test52_saveCompanyShouldFailIfEntityIsDuplicated() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin tries to save Company with the following call saveCompany,
        // but entity is duplicated
        // response status code '409' HyperIoTDuplicateEntityException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = createCompany((HUser) adminUser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(adminUser.getId(), company.getHUserCreator().getId());
        HUser huser = createHUser(null);
        Company duplicateCompany = new Company();
        duplicateCompany.setBusinessName("ACSoftware");
        duplicateCompany.setCity("Lamezia Terme");
        duplicateCompany.setInvoiceAddress("Lamezia Terme");
        duplicateCompany.setNation("Italy");
        duplicateCompany.setPostalCode("88046");
        duplicateCompany.setVatNumber(company.getVatNumber());
        duplicateCompany.setHUserCreator(huser);
        this.impersonateUser(companyRestApi, adminUser);
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
    public void test53_updateCompanyShouldFailIfEntityIsDuplicated() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin tries to update Company with the following call updateCompany,
        // but entity is duplicated
        // response status code '409' HyperIoTDuplicateEntityException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company1 = createCompany((HUser) adminUser);
        Assert.assertNotEquals(0, company1.getId());
        Assert.assertEquals(adminUser.getId(), company1.getHUserCreator().getId());

        huser = createHUser(null);
        Company duplicateCompany = createCompany(huser);
        Assert.assertNotEquals(0, duplicateCompany.getId());
        Assert.assertEquals(huser.getId(), duplicateCompany.getHUserCreator().getId());
        Assert.assertNotEquals(company1.getVatNumber(), duplicateCompany.getVatNumber());

        duplicateCompany.setVatNumber(company1.getVatNumber());
        Assert.assertEquals(company1.getVatNumber(), duplicateCompany.getVatNumber());

        this.impersonateUser(companyRestApi, adminUser);
        Response restResponse = companyRestApi.saveCompany(duplicateCompany);
        Assert.assertEquals(409, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTDuplicateEntityException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().size());
        Assert.assertEquals("vatNumber", ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(0));
    }


    @Test
    public void test54_findAllCompanyPaginatedShouldWork() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // In this following call findAllCompany, hadmin find all Companies with pagination
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        int delta = 5;
        int page = 2;
        List<Company> companies = new ArrayList<>();
        int numbEntities = 7;
        for (int i = 0; i < numbEntities; i++) {
            Company company = createCompany((HUser) adminUser);
            Assert.assertNotEquals(0, company.getId());
            Assert.assertEquals(adminUser.getId(), company.getHUserCreator().getId());
            companies.add(company);
        }
        Assert.assertEquals(numbEntities, companies.size());
        this.impersonateUser(companyRestApi, adminUser);
        Response restResponse = companyRestApi.findAllCompanyPaginated(delta, page);
        HyperIoTPaginableResult<Company> listCompanies = restResponse
                .readEntity(new GenericType<HyperIoTPaginableResult<Company>>() {
                });
        Assert.assertFalse(listCompanies.getResults().isEmpty());
        Assert.assertEquals(2, listCompanies.getResults().size());
        Assert.assertEquals(delta, listCompanies.getDelta());
        Assert.assertEquals(page, listCompanies.getCurrentPage());
        Assert.assertEquals(defaultPage, listCompanies.getNextPage());
        // delta is 5, page 2: 7 entities stored in database
        Assert.assertEquals(2, listCompanies.getNumPages());
        Assert.assertEquals(200, restResponse.getStatus());

        //checks with page = 1
        this.impersonateUser(companyRestApi, adminUser);
        Response restResponsePage1 = companyRestApi.findAllCompanyPaginated(delta, 1);
        HyperIoTPaginableResult<Company> listCompaniesPage1 = restResponsePage1
                .readEntity(new GenericType<HyperIoTPaginableResult<Company>>() {
                });
        Assert.assertFalse(listCompaniesPage1.getResults().isEmpty());
        Assert.assertEquals(delta, listCompaniesPage1.getResults().size());
        Assert.assertEquals(delta, listCompaniesPage1.getDelta());
        Assert.assertEquals(defaultPage, listCompaniesPage1.getCurrentPage());
        Assert.assertEquals(page, listCompaniesPage1.getNextPage());
        // delta is 5, page is 1: 7 entities stored in database
        Assert.assertEquals(2, listCompaniesPage1.getNumPages());
        Assert.assertEquals(200, restResponsePage1.getStatus());
    }


    @Test
    public void test55_findAllCompanyPaginatedShouldFailIfNotLogged() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // the following call tries to find all Companies with pagination,
        // but HUser is not logged
        // response status code '403' HyperIoTUnauthorizedException
        this.impersonateUser(companyRestApi, null);
        Response restResponse = companyRestApi.findAllCompanyPaginated(null, null);
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }


    @Test
    public void test56_findAllCompanyPaginatedShouldWorkIfDeltaAndPageAreNull() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // In this following call findAllCompany, hadmin find all Companies with pagination
        // if delta and page are null
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Integer delta = null;
        Integer page = null;
        List<Company> companies = new ArrayList<>();
        int numbEntities = 3;
        for (int i = 0; i < numbEntities; i++) {
            Company company = createCompany((HUser) adminUser);
            Assert.assertNotEquals(0, company.getId());
            Assert.assertEquals(adminUser.getId(), company.getHUserCreator().getId());
            companies.add(company);
        }
        Assert.assertEquals(numbEntities, companies.size());
        this.impersonateUser(companyRestApi, adminUser);
        Response restResponse = companyRestApi.findAllCompanyPaginated(delta, page);
        HyperIoTPaginableResult<Company> listCompanies = restResponse
                .readEntity(new GenericType<HyperIoTPaginableResult<Company>>() {
                });
        Assert.assertFalse(listCompanies.getResults().isEmpty());
        Assert.assertEquals(numbEntities, listCompanies.getResults().size());
        Assert.assertEquals(defaultDelta, listCompanies.getDelta());
        Assert.assertEquals(defaultPage, listCompanies.getCurrentPage());
        Assert.assertEquals(defaultPage, listCompanies.getNextPage());
        // default delta is 10, default page is 1: 3 entities stored in database
        Assert.assertEquals(1, listCompanies.getNumPages());
        Assert.assertEquals(200, restResponse.getStatus());
    }


    @Test
    public void test57_findAllCompanyPaginatedShouldWorkIfDeltaIsLowerThanZero() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // In this following call findAllCompany, hadmin find all Companies with pagination
        // if delta is lower than zero
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        int delta = -1;
        int page = 2;
        List<Company> companies = new ArrayList<>();
        int numbEntities = 13;
        for (int i = 0; i < numbEntities; i++) {
            Company company = createCompany((HUser) adminUser);
            Assert.assertNotEquals(0, company.getId());
            Assert.assertEquals(adminUser.getId(), company.getHUserCreator().getId());
            companies.add(company);
        }
        Assert.assertEquals(numbEntities, companies.size());
        this.impersonateUser(companyRestApi, adminUser);
        Response restResponsePage2 = companyRestApi.findAllCompanyPaginated(delta, page);
        HyperIoTPaginableResult<Company> listCompaniesPage2 = restResponsePage2
                .readEntity(new GenericType<HyperIoTPaginableResult<Company>>() {
                });
        Assert.assertFalse(listCompaniesPage2.getResults().isEmpty());
        Assert.assertEquals(3, listCompaniesPage2.getResults().size());
        Assert.assertEquals(defaultDelta, listCompaniesPage2.getDelta());
        Assert.assertEquals(page, listCompaniesPage2.getCurrentPage());
        Assert.assertEquals(defaultPage, listCompaniesPage2.getNextPage());
        // default delta is 10, page is 2: 13 entities stored in database
        Assert.assertEquals(2, listCompaniesPage2.getNumPages());
        Assert.assertEquals(200, restResponsePage2.getStatus());

        // checks with page = 1
        this.impersonateUser(companyRestApi, adminUser);
        Response restResponsePage1 = companyRestApi.findAllCompanyPaginated(delta, 1);
        HyperIoTPaginableResult<Company> listCompaniesPage1 = restResponsePage1
                .readEntity(new GenericType<HyperIoTPaginableResult<Company>>() {
                });
        Assert.assertFalse(listCompaniesPage1.getResults().isEmpty());
        Assert.assertEquals(defaultDelta, listCompaniesPage1.getResults().size());
        Assert.assertEquals(defaultDelta, listCompaniesPage1.getDelta());
        Assert.assertEquals(defaultPage, listCompaniesPage1.getCurrentPage());
        Assert.assertEquals(page, listCompaniesPage1.getNextPage());
        // default delta is 10, page is 1: 13 entities stored in database
        Assert.assertEquals(2, listCompaniesPage1.getNumPages());
        Assert.assertEquals(200, restResponsePage1.getStatus());
    }


    @Test
    public void test58_findAllCompanyPaginatedShouldWorkIfDeltaIsZero() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // In this following call findAllCompany, hadmin find all Companies with pagination
        // if delta is zero
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        int delta = 0;
        int page = 3;
        List<Company> companies = new ArrayList<>();
        int numEntities = 21;
        for (int i = 0; i < numEntities; i++) {
            Company company = createCompany((HUser) adminUser);
            Assert.assertNotEquals(0, company.getId());
            Assert.assertEquals(adminUser.getId(), company.getHUserCreator().getId());
            companies.add(company);
        }
        Assert.assertEquals(numEntities, companies.size());
        this.impersonateUser(companyRestApi, adminUser);
        Response restResponse = companyRestApi.findAllCompanyPaginated(delta, page);
        HyperIoTPaginableResult<Company> listCompanies = restResponse
                .readEntity(new GenericType<HyperIoTPaginableResult<Company>>() {
                });
        Assert.assertFalse(listCompanies.getResults().isEmpty());
        Assert.assertEquals(1, listCompanies.getResults().size());
        Assert.assertEquals(defaultDelta, listCompanies.getDelta());
        Assert.assertEquals(page, listCompanies.getCurrentPage());
        Assert.assertEquals(defaultPage, listCompanies.getNextPage());
        // because delta is 10, page is 3: 21 entities stored in database
        Assert.assertEquals(3, listCompanies.getNumPages());
        Assert.assertEquals(200, restResponse.getStatus());

        //checks with page = 1
        this.impersonateUser(companyRestApi, adminUser);
        Response restResponsePage1 = companyRestApi.findAllCompanyPaginated(delta, 1);
        HyperIoTPaginableResult<Company> listCompaniesPage1 = restResponsePage1
                .readEntity(new GenericType<HyperIoTPaginableResult<Company>>() {
                });
        Assert.assertFalse(listCompaniesPage1.getResults().isEmpty());
        Assert.assertEquals(defaultDelta, listCompaniesPage1.getResults().size());
        Assert.assertEquals(defaultDelta, listCompaniesPage1.getDelta());
        Assert.assertEquals(defaultPage, listCompaniesPage1.getCurrentPage());
        Assert.assertEquals(defaultPage + 1, listCompaniesPage1.getNextPage());
        // default delta is 10, page is 1: 21 entities stored in database
        Assert.assertEquals(3, listCompaniesPage1.getNumPages());
        Assert.assertEquals(200, restResponsePage1.getStatus());

        //checks with page = 2
        this.impersonateUser(companyRestApi, adminUser);
        Response restResponsePage2 = companyRestApi.findAllCompanyPaginated(delta, 2);
        HyperIoTPaginableResult<Company> listCompaniesPage2 = restResponsePage2
                .readEntity(new GenericType<HyperIoTPaginableResult<Company>>() {
                });
        Assert.assertFalse(listCompaniesPage2.getResults().isEmpty());
        Assert.assertEquals(defaultDelta, listCompaniesPage2.getResults().size());
        Assert.assertEquals(defaultDelta, listCompaniesPage2.getDelta());
        Assert.assertEquals(defaultPage + 1, listCompaniesPage2.getCurrentPage());
        Assert.assertEquals(page, listCompaniesPage2.getNextPage());
        // default delta is 10, page is 2: 21 entities stored in database
        Assert.assertEquals(3, listCompaniesPage2.getNumPages());
        Assert.assertEquals(200, restResponsePage2.getStatus());
    }


    @Test
    public void test59_findAllCompanyPaginatedShouldWorkIfPageIsLowerThanZero() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // In this following call findAllCompany, hadmin find all Companies with pagination
        // if page is lower than zero
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        int delta = 5;
        int page = -1;
        List<Company> companies = new ArrayList<>();
        for (int i = 0; i < delta; i++) {
            Company company = createCompany((HUser) adminUser);
            Assert.assertNotEquals(0, company.getId());
            Assert.assertEquals(adminUser.getId(), company.getHUserCreator().getId());
            companies.add(company);
        }
        Assert.assertEquals(delta, companies.size());
        this.impersonateUser(companyRestApi, adminUser);
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
    public void test60_findAllCompanyPaginatedShouldWorkIfPageIsZero() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // In this following call findAllCompany, hadmin find all Companies with pagination
        // if page is zero
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        int delta = 4;
        int page = 0;
        List<Company> companies = new ArrayList<>();
        for (int i = 0; i < defaultDelta; i++) {
            Company company = createCompany((HUser) adminUser);
            Assert.assertNotEquals(0, company.getId());
            Assert.assertEquals(adminUser.getId(), company.getHUserCreator().getId());
            companies.add(company);
        }
        Assert.assertEquals(defaultDelta, companies.size());
        this.impersonateUser(companyRestApi, adminUser);
        Response restResponsePage1 = companyRestApi.findAllCompanyPaginated(delta, page);
        HyperIoTPaginableResult<Company> listCompaniesPage1 = restResponsePage1
                .readEntity(new GenericType<HyperIoTPaginableResult<Company>>() {
                });
        Assert.assertFalse(listCompaniesPage1.getResults().isEmpty());
        Assert.assertEquals(delta, listCompaniesPage1.getResults().size());
        Assert.assertEquals(delta, listCompaniesPage1.getDelta());
        Assert.assertEquals(defaultPage, listCompaniesPage1.getCurrentPage());
        Assert.assertEquals(defaultPage + 1, listCompaniesPage1.getNextPage());
        // delta is 4, default page is 1: 10 entities stored in database
        Assert.assertEquals(3, listCompaniesPage1.getNumPages());
        Assert.assertEquals(200, restResponsePage1.getStatus());

        //checks with page = 2
        Response restResponsePage2 = companyRestApi.findAllCompanyPaginated(delta, 2);
        HyperIoTPaginableResult<Company> listCompaniesPage2 = restResponsePage2
                .readEntity(new GenericType<HyperIoTPaginableResult<Company>>() {
                });
        Assert.assertFalse(listCompaniesPage2.getResults().isEmpty());
        Assert.assertEquals(delta, listCompaniesPage2.getResults().size());
        Assert.assertEquals(delta, listCompaniesPage2.getDelta());
        Assert.assertEquals(defaultPage + 1, listCompaniesPage2.getCurrentPage());
        Assert.assertEquals(defaultPage + 2, listCompaniesPage2.getNextPage());
        // delta is 4, page is 2: 10 entities stored in database
        Assert.assertEquals(3, listCompaniesPage2.getNumPages());
        Assert.assertEquals(200, restResponsePage2.getStatus());

        //checks with page = 3
        Response restResponsePage3 = companyRestApi.findAllCompanyPaginated(delta, 3);
        HyperIoTPaginableResult<Company> listCompaniesPage3 = restResponsePage3
                .readEntity(new GenericType<HyperIoTPaginableResult<Company>>() {
                });
        Assert.assertFalse(listCompaniesPage3.getResults().isEmpty());
        Assert.assertEquals(2, listCompaniesPage3.getResults().size());
        Assert.assertEquals(delta, listCompaniesPage3.getDelta());
        Assert.assertEquals(defaultPage + 2, listCompaniesPage3.getCurrentPage());
        Assert.assertEquals(defaultPage, listCompaniesPage3.getNextPage());
        // delta is 4, page is 3: 10 entities stored in database
        Assert.assertEquals(3, listCompaniesPage3.getNumPages());
        Assert.assertEquals(200, restResponsePage3.getStatus());
    }


    @Test
    public void test61_deleteCompanyNotDeleteInCascadeHUserShouldWork() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin delete Company associated with HUser with the following
        // call deleteCompany this call not delete HUser in cascade mode
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin@hyperiot.com", "admin");
        huser = createHUser(null);
        Company company = createCompany(huser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(huser.getId(), company.getHUserCreator().getId());
        this.impersonateUser(companyRestApi, adminUser);
        Response restResponseDeleteCompany = companyRestApi.deleteCompany(company.getId());
        Assert.assertEquals(200, restResponseDeleteCompany.getStatus());

        // checks: HUser is still stored in database
        HUserRestApi hUserRestApi = getOsgiService(HUserRestApi.class);
        this.impersonateUser(hUserRestApi, adminUser);
        Response restResponse = hUserRestApi.findHUser(huser.getId());
        Assert.assertEquals(200, restResponse.getStatus());
    }


    @Test
    public void test62_hadminTriesToRemoveRelationshipBetweenHUserAndCompanyShouldFail() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin tries to setting the hUserCreator field to null and removes the relationship between HUser and
        // Company with the following call updateCompany. hUserCreator cannot be changed by update method
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin@hyperiot.com", "admin");
        huser = createHUser(null);
        Company company = createCompany(huser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(huser.getId(), company.getHUserCreator().getId());
        this.impersonateUser(companyRestApi, adminUser);
        company.setHUserCreator(null);
        Response restResponse = companyRestApi.updateCompany(company);
        Assert.assertEquals(200, restResponse.getStatus());
        Assert.assertEquals(huser.getId(), ((Company) restResponse.getEntity()).getHUserCreator().getId());
    }


    @Test
    public void test63_hadminFindAllCompanyAssociatedWithHUserCreatorShouldWork() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin tries to find all Company associated with hUserCreator with the following call findAllCompany
        // Company is Owned Resource: only huser able to find his entities
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");

        huser = createHUser(null);
        Company company = createCompany(huser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(huser.getId(), company.getHUserCreator().getId());

        this.impersonateUser(companyRestApi, adminUser);
        Response responseByAdmin = companyRestApi.findAllCompany();
        List<Company> listCompanies1 = responseByAdmin.readEntity(new GenericType<List<Company>>() {
        });
        Assert.assertFalse(listCompanies1.isEmpty());
        Assert.assertTrue(listCompanies1.size()>0);
        Assert.assertEquals(200, responseByAdmin.getStatus());
    }


    @Test
    public void test64_findAllCompanyShouldWorkIfListIsEmpty() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin find all Company with the following call findAllCompany
        // there are still no entities saved in the database, this call return an empty list
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        this.impersonateUser(companyRestApi, adminUser);
        Response restResponse = companyRestApi.findAllCompany();
        List<Company> listCompanies = restResponse.readEntity(new GenericType<List<Company>>() {
        });
        Assert.assertTrue(listCompanies.isEmpty());
        Assert.assertEquals(0, listCompanies.size());
        Assert.assertEquals(200, restResponse.getStatus());
    }


    @Test
    public void test65_findAllCompanyPaginatedShouldWorkIfListIsEmpty() {
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // In this following call findAllCompany, hadmin find all Companies with pagination
        // there are still no entities saved in the database, this call return an empty list
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        this.impersonateUser(companyRestApi, adminUser);
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

    @Test
    public void test66_saveCompanyShouldFailIfBusinessNameIsGreaterThan255Chars(){
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin tries to save Company with the following call saveCompany,
        // but business name is greater than 255 chars
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = new Company();
        company.setBusinessName(createStringFieldWithSpecifiedLenght(256));
        company.setCity("Lamezia Terme");
        company.setInvoiceAddress("Lamezia Terme");
        company.setNation("Italy");
        company.setPostalCode("88046");
        company.setVatNumber("01234567890" + java.util.UUID.randomUUID().toString().replaceAll("-", ""));
        company.setHUserCreator((HUser) adminUser);
        this.impersonateUser(companyRestApi, adminUser);
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
    public void test67_saveCompanyShouldFailIfInvoiceAddressIsGreaterThan255Chars(){
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin tries to save Company with the following call saveCompany,
        // but  invoiceAddress is greater than 255 chars
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = new Company();
        company.setBusinessName("ACSoftware");
        company.setCity("Lamezia Terme");
        company.setInvoiceAddress(createStringFieldWithSpecifiedLenght(256));
        company.setNation("Italy");
        company.setPostalCode("88046");
        company.setVatNumber("01234567890" + java.util.UUID.randomUUID().toString().replaceAll("-", ""));
        company.setHUserCreator((HUser) adminUser);
        this.impersonateUser(companyRestApi, adminUser);
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
    public void test68_saveCompanyShouldFailIfCityIsGreaterThan255Chars(){
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin tries to save Company with the following call saveCompany,
        // but  city is greater than 255 chars
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = new Company();
        company.setBusinessName("ACSoftware");
        company.setCity(createStringFieldWithSpecifiedLenght(256));
        company.setInvoiceAddress("Lamezia Terme");
        company.setNation("Italy");
        company.setPostalCode("88046");
        company.setVatNumber("01234567890" + java.util.UUID.randomUUID().toString().replaceAll("-", ""));
        company.setHUserCreator((HUser) adminUser);
        this.impersonateUser(companyRestApi, adminUser);
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
    public void test69_saveCompanyShouldFailIfPostalCodeIsGreaterThan255Chars(){
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin tries to save Company with the following call saveCompany,
        // but  postalcode is greater than 255 chars
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = new Company();
        company.setBusinessName("ACSoftware");
        company.setCity("Lamezia Terme");
        company.setInvoiceAddress("Lamezia Terme");
        company.setNation("Italy");
        company.setPostalCode(createStringFieldWithSpecifiedLenght(256));
        company.setVatNumber("01234567890" + java.util.UUID.randomUUID().toString().replaceAll("-", ""));
        company.setHUserCreator((HUser) adminUser);
        this.impersonateUser(companyRestApi, adminUser);
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
    public void test70_saveCompanyShouldFailIfNationIsGreaterThan255Chars(){
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin tries to save Company with the following call saveCompany,
        // but  nation is greater than 255 chars
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = new Company();
        company.setBusinessName("ACSoftware");
        company.setCity("Lamezia Terme");
        company.setInvoiceAddress("Lamezia Terme");
        company.setNation(createStringFieldWithSpecifiedLenght(256));
        company.setPostalCode("88046");
        company.setVatNumber("01234567890" + java.util.UUID.randomUUID().toString().replaceAll("-", ""));
        company.setHUserCreator((HUser) adminUser);
        this.impersonateUser(companyRestApi, adminUser);
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
    public void test71_saveCompanyShouldFailIfVatNumberIsGreaterThan255Chars(){
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin tries to save Company with the following call saveCompany,
        // but  vatNumber is greater than 255 chars
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = new Company();
        company.setBusinessName("ACSoftware");
        company.setCity("Lamezia Terme");
        company.setInvoiceAddress("Lamezia Terme");
        company.setNation("Italy");
        company.setPostalCode("88046");
        company.setVatNumber(createStringFieldWithSpecifiedLenght(256));
        company.setHUserCreator((HUser) adminUser);
        this.impersonateUser(companyRestApi, adminUser);
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
    public void test72_updateCompanyShouldFailIfBusinessNameIsGreaterThan255Chars(){
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin tries to update Company with the following call updateCompany,
        // but business name is greater than  255 chars
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = createCompany((HUser) adminUser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(adminUser.getId(), company.getHUserCreator().getId());
        company.setBusinessName(createStringFieldWithSpecifiedLenght(256));
        this.impersonateUser(companyRestApi, adminUser);
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
    public void test73_updateCompanyShouldFailIfInvoiceAddressIsGreaterThan255Chars(){
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin tries to update Company with the following call updateCompany,
        // but  invoice is greater than  255 chars
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = createCompany((HUser) adminUser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(adminUser.getId(), company.getHUserCreator().getId());
        company.setInvoiceAddress(createStringFieldWithSpecifiedLenght(256));
        this.impersonateUser(companyRestApi, adminUser);
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
    public void test74_updateCompanyShouldFailIfCityIsGreaterThan255Chars(){
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin tries to update Company with the following call updateCompany,
        // but  city is greater than  255 chars
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = createCompany((HUser) adminUser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(adminUser.getId(), company.getHUserCreator().getId());
        company.setCity(createStringFieldWithSpecifiedLenght(256));
        this.impersonateUser(companyRestApi, adminUser);
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
    public void test75_updateCompanyShouldFailIfPostalCodeIsGreaterThan255Chars(){
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin tries to update Company with the following call updateCompany,
        // but  postal code is greater than  255 chars
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = createCompany((HUser) adminUser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(adminUser.getId(), company.getHUserCreator().getId());
        company.setPostalCode(createStringFieldWithSpecifiedLenght(256));
        this.impersonateUser(companyRestApi, adminUser);
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
    public void test76_updateCompanyShouldFailIfNationIsGreaterThan255Chars(){
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin tries to update Company with the following call updateCompany,
        // but  nation  is greater than  255 chars
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = createCompany((HUser) adminUser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(adminUser.getId(), company.getHUserCreator().getId());
        company.setNation(createStringFieldWithSpecifiedLenght(256));
        this.impersonateUser(companyRestApi, adminUser);
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
    public void test77_updateCompanyShouldFailIfVatNumberIsGreaterThan255Chars(){
        CompanyRestApi companyRestApi = getOsgiService(CompanyRestApi.class);
        // hadmin tries to update Company with the following call updateCompany,
        // but  vat number is greater than  255 chars
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = createCompany((HUser) adminUser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(adminUser.getId(), company.getHUserCreator().getId());
        company.setVatNumber(createStringFieldWithSpecifiedLenght(256));
        this.impersonateUser(companyRestApi, adminUser);
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
    public void test78_removeHUserShouldRemoveHUserCompanies(){
        CompanySystemApi companySystemApi = getOsgiService(CompanySystemApi.class);
        //hUser create Company.
        //hadmin delete hUser
        //Assert that company, related to hUser, will be deleted
        HUser hUser = createHUser(null);
        Assert.assertNotEquals(0, hUser.getId());
        Company company = createCompany(hUser);
        Assert.assertNotEquals(0 , company.getId());
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUserRestApi hUserRestApi = getOsgiService(HUserRestApi.class);
        impersonateUser(hUserRestApi, adminUser);
        Response response = hUserRestApi.deleteHUser(hUser.getId());
        Assert.assertEquals(200, response.getStatus());
        boolean companyExist = true ;
        try{
            companySystemApi.find(company.getId(), null);
        } catch (HyperIoTNoResultException e ){
            companyExist = false;
        }
        Assert.assertFalse(companyExist);
    }

    /*
     *
     *
     * UTILITY METHODS
     *
     *
     */

    private String createStringFieldWithSpecifiedLenght(int length) {
        String symbol = "a";
        String field = String.format("%" + length + "s", " ").replaceAll(" ", symbol);
        Assert.assertEquals(length, field.length());
        return field;
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


    // Company is Owned Resource: only huser is able to find/findAll his entities
    private HUser huser;

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
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.FINDALL);
        HyperIoTAction action1 = HyperIoTActionsUtil.getHyperIoTAction(companyResourceName,
                HyperIoTCrudAction.REMOVE);
        if ((huser != null) && (huser.isActive())) {
            addPermission(huser, action);
            addPermission(huser, action1);
            this.impersonateUser(companyRestApi, huser);
            Response restResponse2 = companyRestApi.findAllCompany();
            List<Company> listCompanies1 = restResponse2.readEntity(new GenericType<List<Company>>() {
            });
            if (!listCompanies1.isEmpty()) {
                Assert.assertFalse(listCompanies1.isEmpty());
                for (Company company : listCompanies1) {
                    this.impersonateUser(companyRestApi, huser);
                    Response restResponse3 = companyRestApi.deleteCompany(company.getId());
                    Assert.assertEquals(200, restResponse3.getStatus());
                    Assert.assertNull(restResponse3.getEntity());
                }
            }
        }

        // Remove all roles and permissions (in cascade mode) created in every test
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

}
