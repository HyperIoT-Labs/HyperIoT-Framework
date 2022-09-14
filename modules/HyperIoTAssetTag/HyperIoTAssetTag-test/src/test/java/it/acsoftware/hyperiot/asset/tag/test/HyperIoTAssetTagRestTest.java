package it.acsoftware.hyperiot.asset.tag.test;

import it.acsoftware.hyperiot.asset.tag.model.AssetTag;
import it.acsoftware.hyperiot.asset.tag.service.rest.AssetTagRestApi;
import it.acsoftware.hyperiot.base.api.authentication.AuthenticationApi;
import it.acsoftware.hyperiot.base.action.HyperIoTActionName;
import it.acsoftware.hyperiot.base.api.HyperIoTAction;
import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.base.api.HyperIoTUser;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTPaginableResult;
import it.acsoftware.hyperiot.base.model.HyperIoTAssetOwnerImpl;
import it.acsoftware.hyperiot.base.model.HyperIoTBaseError;
import it.acsoftware.hyperiot.base.service.rest.HyperIoTBaseRestApi;
import it.acsoftware.hyperiot.base.test.HyperIoTTestConfigurationBuilder;
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
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static it.acsoftware.hyperiot.asset.tag.test.HyperIoTAssetTagConfiguration.*;

/**
 * @author Aristide Cittadino Interface component for AssetTag System Service.
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HyperIoTAssetTagRestTest extends KarafTestSupport {

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
    public void test01_saveAssetTagShouldWork() {
        AssetTagRestApi assetTagRestApi = getOsgiService(AssetTagRestApi.class);
        // hadmin save AssetTag with the following call saveAssetTag
        // response status code '200'
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
    }

    @Test
    public void test02_saveAssetTagShouldFailIfNotLogged() {
        AssetTagRestApi assetTagRestApi = getOsgiService(AssetTagRestApi.class);
        // the following call tries to save AssetTag, but HUser is not logged
        // response status code '403' HyperIoTUnauthorizedException
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
        //user not logged
        this.impersonateUser(assetTagRestApi, null);
        Response restResponse = assetTagRestApi.saveAssetTag(assetTag);
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test03_updateAssetTagShouldWork() {
        AssetTagRestApi assetTagRestApi = getOsgiService(AssetTagRestApi.class);
        // hadmin update AssetTag with the following call updateAssetTag
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        AssetTag assetTag = createAssetTag();
        Assert.assertNotEquals(0, assetTag.getId());
        Date date = new Date();
        assetTag.setName("edited in date: " + date);
        this.impersonateUser(assetTagRestApi, adminUser);
        Response restResponse = assetTagRestApi.updateAssetTag(assetTag);
        Assert.assertEquals(200, restResponse.getStatus());
        Assert.assertEquals("edited in date: " + date,
                ((AssetTag) restResponse.getEntity()).getName());
        Assert.assertEquals(assetTag.getEntityVersion() + 1,
                (((AssetTag) restResponse.getEntity()).getEntityVersion()));
    }

    @Test
    public void test04_updateAssetTagShouldFailIfNotLogged() {
        AssetTagRestApi assetTagRestApi = getOsgiService(AssetTagRestApi.class);
        // the following call tries to update AssetTag, but HUser is not logged
        // response status code '403' HyperIoTUnauthorizedException
        AssetTag assetTag = createAssetTag();
        Assert.assertNotEquals(0, assetTag.getId());
        Date date = new Date();
        assetTag.setName("edited failed in date: " + date);
        this.impersonateUser(assetTagRestApi, null);
        Response restResponse = assetTagRestApi.updateAssetTag(assetTag);
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test05_findAssetTagShouldWork() {
        AssetTagRestApi assetTagRestApi = getOsgiService(AssetTagRestApi.class);
        // hadmin find AssetTag with the following call findAssetTag
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        AssetTag assetTag = createAssetTag();
        Assert.assertNotEquals(0, assetTag.getId());
        this.impersonateUser(assetTagRestApi, adminUser);
        Response restResponse = assetTagRestApi.findAssetTag(assetTag.getId());
        Assert.assertEquals(200, restResponse.getStatus());
        Assert.assertEquals(assetTag.getId(), ((AssetTag) restResponse.getEntity()).getId());
        Assert.assertEquals(assetTag.getName(), ((AssetTag) restResponse.getEntity()).getName());
        Assert.assertEquals(assetTag.getOwner().getOwnerResourceName(),
                ((AssetTag) restResponse.getEntity()).getOwner().getOwnerResourceName());
        Assert.assertEquals(assetTag.getOwner().getOwnerResourceId(),
                ((AssetTag) restResponse.getEntity()).getOwner().getOwnerResourceId());
        Assert.assertEquals(assetTag.getOwner().getUserId(),
                ((AssetTag) restResponse.getEntity()).getOwner().getUserId());
    }

    @Test
    public void test06_findAssetTagShouldFailIfNotLogged() {
        AssetTagRestApi assetTagRestApi = getOsgiService(AssetTagRestApi.class);
        // the following call tries to find AssetTag, but HUser is not logged
        // response status code '403' HyperIoTUnauthorizedException
        AssetTag assetTag = createAssetTag();
        Assert.assertNotEquals(0, assetTag.getId());
        this.impersonateUser(assetTagRestApi, null);
        Response restResponse = assetTagRestApi.findAssetTag(assetTag.getId());
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test07_findAssetTagShouldFailIfEntityNotFound() {
        AssetTagRestApi assetTagRestApi = getOsgiService(AssetTagRestApi.class);
        // hadmin tries to find AssetTag with the following call findAssetTag,
        // but entity not found
        // response status code '404' HyperIoTEntityNotFound
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        this.impersonateUser(assetTagRestApi, adminUser);
        Response restResponse = assetTagRestApi.findAssetTag(0);
        Assert.assertEquals(404, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test08_findAllAssetTagShouldWork() {
        AssetTagRestApi assetTagRestApi = getOsgiService(AssetTagRestApi.class);
        // hadmin find all AssetTag with the following call findAllAssetTag
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        AssetTag assetTag = createAssetTag();
        Assert.assertNotEquals(0, assetTag.getId());
        this.impersonateUser(assetTagRestApi, adminUser);
        Response restResponse = assetTagRestApi.findAllAssetTag();
        List<AssetTag> listTags = restResponse.readEntity(new GenericType<List<AssetTag>>() {
        });
        Assert.assertFalse(listTags.isEmpty());
        boolean assetTagFound = false;
        for (AssetTag tag : listTags) {
            if (tag.getId() == assetTag.getId()) {
                assetTagFound = true;
            }
        }
        Assert.assertTrue(assetTagFound);
        Assert.assertEquals(200, restResponse.getStatus());
    }

    @Test
    public void test09_findAllAssetTagShouldFailIfNotLogged() {
        AssetTagRestApi assetTagRestApi = getOsgiService(AssetTagRestApi.class);
        // the following call tries to find all AssetTag, but HUser is not logged
        // response status code '403' HyperIoTUnauthorizedException
        AssetTag assetTag = createAssetTag();
        Assert.assertNotEquals(0, assetTag.getId());
        this.impersonateUser(assetTagRestApi, null);
        Response restResponse = assetTagRestApi.findAllAssetTag();
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test10_deleteAssetTagShouldWork() {
        AssetTagRestApi assetTagRestApi = getOsgiService(AssetTagRestApi.class);
        // hadmin delete AssetTag with the following call deleteAssetTag
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        AssetTag assetTag = createAssetTag();
        Assert.assertNotEquals(0, assetTag.getId());
        this.impersonateUser(assetTagRestApi, adminUser);
        Response restResponse = assetTagRestApi.deleteAssetTag(assetTag.getId());
        Assert.assertEquals(200, restResponse.getStatus());
        Assert.assertNull(restResponse.getEntity());
    }

    @Test
    public void test11_deleteAssetTagShouldFailIfNotLogged() {
        AssetTagRestApi assetTagRestApi = getOsgiService(AssetTagRestApi.class);
        // the following call tries to delete AssetTag, but HUser is not logged
        // response status code '403' HyperIoTUnauthorizedException
        AssetTag assetTag = createAssetTag();
        Assert.assertNotEquals(0, assetTag.getId());
        this.impersonateUser(assetTagRestApi, null);
        Response restResponse = assetTagRestApi.deleteAssetTag(assetTag.getId());
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test12_deleteAssetTagShouldFailIfEntityNotFound() {
        AssetTagRestApi assetTagRestApi = getOsgiService(AssetTagRestApi.class);
        // hadmin tries to delete AssetTag with the following call deleteAssetTag,
        // but entity not found
        // response status code '404' HyperIoTEntityNotFound
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        this.impersonateUser(assetTagRestApi, adminUser);
        Response restResponse = assetTagRestApi.deleteAssetTag(0);
        Assert.assertEquals(404, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test13_saveAssetTagShouldFailIfNameIsNull() {
        AssetTagRestApi assetTagRestApi = getOsgiService(AssetTagRestApi.class);
        // hadmin tries to save AssetTag with the following call saveAssetTag,
        // but name is null
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = createCompany((HUser) adminUser);
        Assert.assertNotEquals(0, company.getId());
        AssetTag assetTag = new AssetTag();
        assetTag.setName(null);
        HyperIoTAssetOwnerImpl owner = new HyperIoTAssetOwnerImpl();
        owner.setOwnerResourceName(companyResourceName);
        owner.setOwnerResourceId(company.getId());
        owner.setUserId(adminUser.getId());
        assetTag.setOwner(owner);
        this.impersonateUser(assetTagRestApi, adminUser);
        Response restResponse = assetTagRestApi.saveAssetTag(assetTag);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(2, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("assettag-name", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getMessage().isEmpty());
        Assert.assertEquals("assettag-name", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getField());
    }

    @Test
    public void test14_saveAssetTagShouldFailIfNameIsEmpty() {
        AssetTagRestApi assetTagRestApi = getOsgiService(AssetTagRestApi.class);
        // hadmin tries to save AssetTag with the following call saveAssetTag,
        // but name is empty
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = createCompany((HUser) adminUser);
        Assert.assertNotEquals(0, company.getId());
        AssetTag assetTag = new AssetTag();
        assetTag.setName("");
        HyperIoTAssetOwnerImpl owner = new HyperIoTAssetOwnerImpl();
        owner.setOwnerResourceName(companyResourceName);
        owner.setOwnerResourceId(company.getId());
        owner.setUserId(adminUser.getId());
        assetTag.setOwner(owner);
        this.impersonateUser(assetTagRestApi, adminUser);
        Response restResponse = assetTagRestApi.saveAssetTag(assetTag);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("assettag-name",
                ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(assetTag.getName(),
                ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }

    @Test
    public void test15_saveAssetTagShouldFailIfNameIsMaliciousCode() {
        AssetTagRestApi assetTagRestApi = getOsgiService(AssetTagRestApi.class);
        // hadmin tries to save AssetTag with the following call saveAssetTag,
        // but name is malicious code
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = createCompany((HUser) adminUser);
        Assert.assertNotEquals(0, company.getId());
        AssetTag assetTag = new AssetTag();
        String maliciousCode = "javascript:";
        assetTag.setName(maliciousCode);
        HyperIoTAssetOwnerImpl owner = new HyperIoTAssetOwnerImpl();
        owner.setOwnerResourceName(companyResourceName);
        owner.setOwnerResourceId(company.getId());
        owner.setUserId(adminUser.getId());
        assetTag.setOwner(owner);
        this.impersonateUser(assetTagRestApi, adminUser);
        Response restResponse = assetTagRestApi.saveAssetTag(assetTag);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("assettag-name",
                ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(maliciousCode,
                ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }

    @Test
    public void test16_saveAssetTagShouldFailIfOwnerIsNull() {
        AssetTagRestApi assetTagRestApi = getOsgiService(AssetTagRestApi.class);
        // hadmin tries to save AssetTag with the following call saveAssetTag,
        // but owner is null
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        AssetTag assetTag = new AssetTag();
        assetTag.setName("Tag" + java.util.UUID.randomUUID());
        assetTag.setOwner(null);
        this.impersonateUser(assetTagRestApi, adminUser);
        Response restResponse = assetTagRestApi.saveAssetTag(assetTag);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("assettag-owner",
                ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
    }


    @Test
    public void test17_updateAssetTagShouldFailIfNameIsNull() {
        AssetTagRestApi assetTagRestApi = getOsgiService(AssetTagRestApi.class);
        // hadmin tries to update AssetTag with the following call updateAssetTag,
        // but name is null
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        AssetTag assetTag = createAssetTag();
        Assert.assertNotEquals(0, assetTag.getId());
        Assert.assertNotNull(assetTag.getName());

        assetTag.setName(null);
        this.impersonateUser(assetTagRestApi, adminUser);
        Response restResponse = assetTagRestApi.updateAssetTag(assetTag);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(2, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("assettag-name", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getMessage().isEmpty());
        Assert.assertEquals("assettag-name", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getField());
    }

    @Test
    public void test18_updateAssetTagShouldFailIfNameIsEmpty() {
        AssetTagRestApi assetTagRestApi = getOsgiService(AssetTagRestApi.class);
        // hadmin tries to update AssetTag with the following call updateAssetTag,
        // but name is empty
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        this.impersonateUser(assetTagRestApi, adminUser);
        AssetTag assetTag = createAssetTag();
        Assert.assertNotEquals(0, assetTag.getId());
        assetTag.setName("");
        Response restResponse = assetTagRestApi.updateAssetTag(assetTag);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("assettag-name", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(assetTag.getName(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }

    @Test
    public void test19_updateAssetTagShouldFailIfNameIsMaliciousCode() {
        AssetTagRestApi assetTagRestApi = getOsgiService(AssetTagRestApi.class);
        // hadmin tries to update AssetTag with the following call updateAssetTag,
        // but name is malicious code
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        this.impersonateUser(assetTagRestApi, adminUser);
        AssetTag assetTag = createAssetTag();
        Assert.assertNotEquals(0, assetTag.getId());
        String maliciousCode = "vbscript:";
        assetTag.setName(maliciousCode);
        Response restResponse = assetTagRestApi.updateAssetTag(assetTag);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("assettag-name", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(maliciousCode, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }

    @Test
    public void test20_updateAssetTagShouldFailIfOwnerIsNull() {
        AssetTagRestApi assetTagRestApi = getOsgiService(AssetTagRestApi.class);
        // hadmin tries to update AssetTag with the following call updateAssetTag,
        // but owner is null
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        AssetTag assetTag = createAssetTag();
        Assert.assertNotEquals(0, assetTag.getId());
        Assert.assertNotNull(assetTag.getOwner());
        assetTag.setOwner(null);
        this.impersonateUser(assetTagRestApi, adminUser);
        Response restResponse = assetTagRestApi.updateAssetTag(assetTag);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("assettag-owner",
                ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
    }

    @Test
    public void test21_saveAssetTagShouldFailIfEntityIsDuplicated() {
        AssetTagRestApi assetTagRestApi = getOsgiService(AssetTagRestApi.class);
        // hadmin tries to save AssetTag with the following call saveAssetTag,
        // but entity is duplicated
        // response status code '422' HyperIoTDuplicateEntityException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        AssetTag assetTag = createAssetTag();
        Assert.assertNotEquals(0, assetTag.getId());

        Company company = createCompany((HUser) adminUser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(adminUser.getId(), company.getHUserCreator().getId());

        AssetTag duplicateAssetTag = new AssetTag();
        duplicateAssetTag.setName(assetTag.getName());
        HyperIoTAssetOwnerImpl owner = new HyperIoTAssetOwnerImpl();
        owner.setOwnerResourceName(companyResourceName);
        owner.setOwnerResourceId(company.getId());
        owner.setUserId(adminUser.getId());
        duplicateAssetTag.setOwner(owner);

        Assert.assertEquals(assetTag.getName(), duplicateAssetTag.getName());

        this.impersonateUser(assetTagRestApi, adminUser);
        Response restResponse = assetTagRestApi.saveAssetTag(duplicateAssetTag);
        Assert.assertEquals(409, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTDuplicateEntityException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(3, ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().size());
        boolean nameIsDuplicated = false;
        boolean ownerresourcenameIsDuplicated = false;
        boolean ownerresourceidIsDuplicated = false;
        for (int i = 0; i < ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().size(); i++) {
            if (((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(i).contentEquals("name")) {
                Assert.assertEquals("name", ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(i));
                nameIsDuplicated = true;
            }
            if (((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(i).contentEquals("ownerresourcename")) {
                Assert.assertEquals("ownerresourcename", ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(i));
                ownerresourcenameIsDuplicated = true;
            }
            if (((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(i).contentEquals("ownerresourceid")) {
                Assert.assertEquals("ownerresourceid", ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(i));
                ownerresourceidIsDuplicated = true;
            }
        }
        Assert.assertTrue(nameIsDuplicated);
        Assert.assertTrue(ownerresourcenameIsDuplicated);
        Assert.assertTrue(ownerresourceidIsDuplicated);
    }

    @Test
    public void test22_updateAssetTagShouldFailIfEntityIsDuplicated() {
        AssetTagRestApi assetTagRestApi = getOsgiService(AssetTagRestApi.class);
        // hadmin tries to update AssetTag with the following call updateAssetTag,
        // but entity is duplicated
        // response status code '422' HyperIoTDuplicateEntityException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        AssetTag assetTag = createAssetTag();
        Assert.assertNotEquals(0, assetTag.getId());

        AssetTag duplicateAssetTag = createAssetTag();
        Assert.assertNotEquals(0, duplicateAssetTag.getId());

        duplicateAssetTag.setName(assetTag.getName());
        Assert.assertEquals(assetTag.getName(), duplicateAssetTag.getName());

        this.impersonateUser(assetTagRestApi, adminUser);
        Response restResponse = assetTagRestApi.updateAssetTag(duplicateAssetTag);
        Assert.assertEquals(409, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTDuplicateEntityException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(3, ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().size());
        boolean nameIsDuplicated = false;
        boolean ownerresourcenameIsDuplicated = false;
        boolean ownerresourceidIsDuplicated = false;
        for (int i = 0; i < ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().size(); i++) {
            if (((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(i).contentEquals("name")) {
                Assert.assertEquals("name", ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(i));
                nameIsDuplicated = true;
            }
            if (((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(i).contentEquals("ownerresourcename")) {
                Assert.assertEquals("ownerresourcename", ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(i));
                ownerresourcenameIsDuplicated = true;
            }
            if (((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(i).contentEquals("ownerresourceid")) {
                Assert.assertEquals("ownerresourceid", ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(i));
                ownerresourceidIsDuplicated = true;
            }
        }
        Assert.assertTrue(nameIsDuplicated);
        Assert.assertTrue(ownerresourcenameIsDuplicated);
        Assert.assertTrue(ownerresourceidIsDuplicated);
    }

    @Test
    public void test23_updateAssetTagShouldFailIfEntityNotFound() {
        AssetTagRestApi assetTagRestApi = getOsgiService(AssetTagRestApi.class);
        // hadmin tries to update AssetTag with the following call updateAssetTag,
        // but entity not found
        // response status code '404' HyperIoTEntityNotFound
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        //AssetTag isn't stored in database
        AssetTag assetTag = new AssetTag();
        assetTag.setName("AssetTag not found");
        this.impersonateUser(assetTagRestApi, adminUser);
        Response restResponse = assetTagRestApi.updateAssetTag(assetTag);
        Assert.assertEquals(404, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test24_findAllAssetTagPaginationShouldWork() {
        AssetTagRestApi assetTagRestApi = getOsgiService(AssetTagRestApi.class);
        // In this following call findAllAssetTag, hadmin find all AssetTag with pagination
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        int delta = 5;
        int page = 3;
        List<AssetTag> tags = new ArrayList<>();
        int numbEntities = 13;
        for (int i = 0; i < numbEntities; i++) {
            AssetTag assetTag = createAssetTag();
            Assert.assertNotEquals(0, assetTag.getId());
            tags.add(assetTag);
        }
        Assert.assertEquals(numbEntities, tags.size());
        this.impersonateUser(assetTagRestApi, adminUser);
        Response restResponse = assetTagRestApi.findAllAssetTagPaginated(delta, page);
        HyperIoTPaginableResult<AssetTag> listAssetTags = restResponse
                .readEntity(new GenericType<HyperIoTPaginableResult<AssetTag>>() {
                });
        Assert.assertFalse(listAssetTags.getResults().isEmpty());
        Assert.assertEquals(3, listAssetTags.getResults().size());
        Assert.assertEquals(delta, listAssetTags.getDelta());
        Assert.assertEquals(page, listAssetTags.getCurrentPage());
        Assert.assertEquals(defaultPage, listAssetTags.getNextPage());
        // delta is 5, page is 3: 13 entities stored in database
        Assert.assertEquals(3, listAssetTags.getNumPages());
        Assert.assertEquals(200, restResponse.getStatus());

        //checks with page = 1
        this.impersonateUser(assetTagRestApi, adminUser);
        Response restResponsePage1 = assetTagRestApi.findAllAssetTagPaginated(delta, 1);
        HyperIoTPaginableResult<AssetTag> listAssetTagsPage1 = restResponsePage1
                .readEntity(new GenericType<HyperIoTPaginableResult<AssetTag>>() {
                });
        Assert.assertFalse(listAssetTagsPage1.getResults().isEmpty());
        Assert.assertEquals(delta, listAssetTagsPage1.getResults().size());
        Assert.assertEquals(delta, listAssetTagsPage1.getDelta());
        Assert.assertEquals(defaultPage, listAssetTagsPage1.getCurrentPage());
        Assert.assertEquals(defaultPage + 1, listAssetTagsPage1.getNextPage());
        // delta is 5, page is 1: 13 entities stored in database
        Assert.assertEquals(3, listAssetTagsPage1.getNumPages());
        Assert.assertEquals(200, restResponsePage1.getStatus());

        //checks with page = 2
        this.impersonateUser(assetTagRestApi, adminUser);
        Response restResponsePage2 = assetTagRestApi.findAllAssetTagPaginated(delta, 2);
        HyperIoTPaginableResult<AssetTag> listAssetTagsPage2 = restResponsePage2
                .readEntity(new GenericType<HyperIoTPaginableResult<AssetTag>>() {
                });
        Assert.assertFalse(listAssetTagsPage2.getResults().isEmpty());
        Assert.assertEquals(delta, listAssetTagsPage2.getResults().size());
        Assert.assertEquals(delta, listAssetTagsPage2.getDelta());
        Assert.assertEquals(defaultPage + 1, listAssetTagsPage2.getCurrentPage());
        Assert.assertEquals(page, listAssetTagsPage2.getNextPage());
        // delta is 5, page is 2: 13 entities stored in database
        Assert.assertEquals(3, listAssetTagsPage2.getNumPages());
        Assert.assertEquals(200, restResponsePage2.getStatus());
    }

    @Test
    public void test25_findAllAssetTagPaginationShouldWorkIfDeltaAndPageAreNull() {
        AssetTagRestApi assetTagRestApi = getOsgiService(AssetTagRestApi.class);
        // In this following call findAllAssetTag, hadmin find all AssetTag with pagination
        // if delta and page are null
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Integer delta = null;
        Integer page = null;
        List<AssetTag> tags = new ArrayList<>();
        int numbEntities = 7;
        for (int i = 0; i < numbEntities; i++) {
            AssetTag assetTag = createAssetTag();
            Assert.assertNotEquals(0, assetTag.getId());
            tags.add(assetTag);
        }
        Assert.assertEquals(numbEntities, tags.size());
        this.impersonateUser(assetTagRestApi, adminUser);
        Response restResponse = assetTagRestApi.findAllAssetTagPaginated(delta, page);
        HyperIoTPaginableResult<AssetTag> listAssetTags = restResponse
                .readEntity(new GenericType<HyperIoTPaginableResult<AssetTag>>() {
                });
        Assert.assertFalse(listAssetTags.getResults().isEmpty());
        Assert.assertEquals(numbEntities, listAssetTags.getResults().size());
        Assert.assertEquals(defaultDelta, listAssetTags.getDelta());
        Assert.assertEquals(defaultPage, listAssetTags.getCurrentPage());
        Assert.assertEquals(defaultPage, listAssetTags.getNextPage());
        // default delta is 10, default page is 1: 7 entities stored in database
        Assert.assertEquals(1, listAssetTags.getNumPages());
        Assert.assertEquals(200, restResponse.getStatus());
    }

    @Test
    public void test26_findAllAssetTagPaginationShouldWorkIfDeltaIsLowerThanZero() {
        AssetTagRestApi assetTagRestApi = getOsgiService(AssetTagRestApi.class);
        // In this following call findAllAssetTag, hadmin find all AssetTag with pagination
        // if delta is lower than zero
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        int delta = -1;
        int page = 2;
        List<AssetTag> tags = new ArrayList<>();
        int numbEntities = 12;
        for (int i = 0; i < numbEntities; i++) {
            AssetTag assetTag = createAssetTag();
            Assert.assertNotEquals(0, assetTag.getId());
            tags.add(assetTag);
        }
        Assert.assertEquals(numbEntities, tags.size());
        this.impersonateUser(assetTagRestApi, adminUser);
        Response restResponsePage2 = assetTagRestApi.findAllAssetTagPaginated(delta, page);
        HyperIoTPaginableResult<AssetTag> listAssetTagsPage2 = restResponsePage2
                .readEntity(new GenericType<HyperIoTPaginableResult<AssetTag>>() {
                });
        Assert.assertFalse(listAssetTagsPage2.getResults().isEmpty());
        Assert.assertEquals(2, listAssetTagsPage2.getResults().size());
        Assert.assertEquals(defaultDelta, listAssetTagsPage2.getDelta());
        Assert.assertEquals(page, listAssetTagsPage2.getCurrentPage());
        Assert.assertEquals(defaultPage, listAssetTagsPage2.getNextPage());
        // default delta is 10, page is 2: 12 entities stored in database
        Assert.assertEquals(2, listAssetTagsPage2.getNumPages());
        Assert.assertEquals(200, restResponsePage2.getStatus());

        //checks with page = 1
        this.impersonateUser(assetTagRestApi, adminUser);
        Response restResponsePage1 = assetTagRestApi.findAllAssetTagPaginated(delta, 1);
        HyperIoTPaginableResult<AssetTag> listAssetTagsPage1 = restResponsePage1
                .readEntity(new GenericType<HyperIoTPaginableResult<AssetTag>>() {
                });
        Assert.assertFalse(listAssetTagsPage1.getResults().isEmpty());
        Assert.assertEquals(defaultDelta, listAssetTagsPage1.getResults().size());
        Assert.assertEquals(defaultDelta, listAssetTagsPage1.getDelta());
        Assert.assertEquals(defaultPage, listAssetTagsPage1.getCurrentPage());
        Assert.assertEquals(page, listAssetTagsPage1.getNextPage());
        // default delta is 10, page is 1: 12 entities stored in database
        Assert.assertEquals(2, listAssetTagsPage1.getNumPages());
        Assert.assertEquals(200, restResponsePage1.getStatus());
    }

    @Test
    public void test27_findAllAssetTagPaginationShouldWorkIfDeltaIsZero() {
        AssetTagRestApi assetTagRestApi = getOsgiService(AssetTagRestApi.class);
        // In this following call findAllAssetTag, hadmin find all AssetTag with pagination
        // if delta is zero
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        int delta = 0;
        int page = 3;
        List<AssetTag> tags = new ArrayList<>();
        int numbEntities = 23;
        for (int i = 0; i < numbEntities; i++) {
            AssetTag assetTag = createAssetTag();
            Assert.assertNotEquals(0, assetTag.getId());
            tags.add(assetTag);
        }
        Assert.assertEquals(numbEntities, tags.size());
        this.impersonateUser(assetTagRestApi, adminUser);
        Response restResponsePage3 = assetTagRestApi.findAllAssetTagPaginated(delta, page);
        HyperIoTPaginableResult<AssetTag> listAssetTagsPage3 = restResponsePage3
                .readEntity(new GenericType<HyperIoTPaginableResult<AssetTag>>() {
                });
        Assert.assertFalse(listAssetTagsPage3.getResults().isEmpty());
        Assert.assertEquals(3, listAssetTagsPage3.getResults().size());
        Assert.assertEquals(defaultDelta, listAssetTagsPage3.getDelta());
        Assert.assertEquals(page, listAssetTagsPage3.getCurrentPage());
        Assert.assertEquals(defaultPage, listAssetTagsPage3.getNextPage());
        // default delta is 10, page is 3: 23 entities stored in database
        Assert.assertEquals(3, listAssetTagsPage3.getNumPages());
        Assert.assertEquals(200, restResponsePage3.getStatus());

        //checks with page = 1
        this.impersonateUser(assetTagRestApi, adminUser);
        Response restResponsePage1 = assetTagRestApi.findAllAssetTagPaginated(delta, 1);
        HyperIoTPaginableResult<AssetTag> listAssetTagsPage1 = restResponsePage1
                .readEntity(new GenericType<HyperIoTPaginableResult<AssetTag>>() {
                });
        Assert.assertFalse(listAssetTagsPage1.getResults().isEmpty());
        Assert.assertEquals(defaultDelta, listAssetTagsPage1.getResults().size());
        Assert.assertEquals(defaultDelta, listAssetTagsPage1.getDelta());
        Assert.assertEquals(defaultPage, listAssetTagsPage1.getCurrentPage());
        Assert.assertEquals(defaultPage + 1, listAssetTagsPage1.getNextPage());
        // default delta is 10, page is 1: 23 entities stored in database
        Assert.assertEquals(3, listAssetTagsPage1.getNumPages());
        Assert.assertEquals(200, restResponsePage1.getStatus());

        //checks with page = 2
        this.impersonateUser(assetTagRestApi, adminUser);
        Response restResponsePage2 = assetTagRestApi.findAllAssetTagPaginated(delta, 2);
        HyperIoTPaginableResult<AssetTag> listAssetTagsPage2 = restResponsePage2
                .readEntity(new GenericType<HyperIoTPaginableResult<AssetTag>>() {
                });
        Assert.assertFalse(listAssetTagsPage2.getResults().isEmpty());
        Assert.assertEquals(defaultDelta, listAssetTagsPage2.getResults().size());
        Assert.assertEquals(defaultDelta, listAssetTagsPage2.getDelta());
        Assert.assertEquals(defaultPage + 1, listAssetTagsPage2.getCurrentPage());
        Assert.assertEquals(page, listAssetTagsPage2.getNextPage());
        // default delta is 10, page is 2: 23 entities stored in database
        Assert.assertEquals(3, listAssetTagsPage2.getNumPages());
        Assert.assertEquals(200, restResponsePage2.getStatus());
    }

    @Test
    public void test28_findAllAssetTagPaginationShouldWorkIfPageIsLowerThanZero() {
        AssetTagRestApi assetTagRestApi = getOsgiService(AssetTagRestApi.class);
        // In this following call findAllAssetTag, hadmin find all AssetTag with pagination
        // if page is lower than zero
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        int delta = 5;
        int page = -1;
        List<AssetTag> tags = new ArrayList<>();
        for (int i = 0; i < defaultDelta; i++) {
            AssetTag assetTag = createAssetTag();
            Assert.assertNotEquals(0, assetTag.getId());
            tags.add(assetTag);
        }
        Assert.assertEquals(defaultDelta, tags.size());
        this.impersonateUser(assetTagRestApi, adminUser);
        Response restResponse = assetTagRestApi.findAllAssetTagPaginated(delta, page);
        HyperIoTPaginableResult<AssetTag> listAssetTags = restResponse
                .readEntity(new GenericType<HyperIoTPaginableResult<AssetTag>>() {
                });
        Assert.assertFalse(listAssetTags.getResults().isEmpty());
        Assert.assertEquals(delta, listAssetTags.getResults().size());
        Assert.assertEquals(delta, listAssetTags.getDelta());
        Assert.assertEquals(defaultPage, listAssetTags.getCurrentPage());
        Assert.assertEquals(defaultPage + 1, listAssetTags.getNextPage());
        // delta is 5, default page is 1: 10 entities stored in database
        Assert.assertEquals(2, listAssetTags.getNumPages());
        Assert.assertEquals(200, restResponse.getStatus());

        //checks with page = 2
        this.impersonateUser(assetTagRestApi, adminUser);
        Response restResponsePage2 = assetTagRestApi.findAllAssetTagPaginated(delta, 2);
        HyperIoTPaginableResult<AssetTag> listAssetTagsPage2 = restResponsePage2
                .readEntity(new GenericType<HyperIoTPaginableResult<AssetTag>>() {
                });
        Assert.assertFalse(listAssetTagsPage2.getResults().isEmpty());
        Assert.assertEquals(delta, listAssetTagsPage2.getResults().size());
        Assert.assertEquals(delta, listAssetTagsPage2.getDelta());
        Assert.assertEquals(defaultPage + 1, listAssetTagsPage2.getCurrentPage());
        Assert.assertEquals(defaultPage, listAssetTagsPage2.getNextPage());
        // default delta is 10, page is 2: 22 entities stored in database
        Assert.assertEquals(2, listAssetTagsPage2.getNumPages());
        Assert.assertEquals(200, restResponsePage2.getStatus());
    }

    @Test
    public void test29_findAllAssetTagPaginationShouldWorkIfPageIsZero() {
        AssetTagRestApi assetTagRestApi = getOsgiService(AssetTagRestApi.class);
        // In this following call findAllAssetTag, hadmin find all AssetTag with pagination
        // if page is zero
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        int delta = 5;
        int page = 0;
        List<AssetTag> tags = new ArrayList<>();
        for (int i = 0; i < delta; i++) {
            AssetTag assetTag = createAssetTag();
            Assert.assertNotEquals(0, assetTag.getId());
            tags.add(assetTag);
        }
        Assert.assertEquals(delta, tags.size());
        this.impersonateUser(assetTagRestApi, adminUser);
        Response restResponse = assetTagRestApi.findAllAssetTagPaginated(delta, page);
        HyperIoTPaginableResult<AssetTag> listAssetTags = restResponse
                .readEntity(new GenericType<HyperIoTPaginableResult<AssetTag>>() {
                });
        Assert.assertFalse(listAssetTags.getResults().isEmpty());
        Assert.assertEquals(delta, listAssetTags.getResults().size());
        Assert.assertEquals(delta, listAssetTags.getDelta());
        Assert.assertEquals(defaultPage, listAssetTags.getCurrentPage());
        Assert.assertEquals(defaultPage, listAssetTags.getNextPage());
        // delta is 5, default page is 1: 5 entities stored in database
        Assert.assertEquals(1, listAssetTags.getNumPages());
        Assert.assertEquals(200, restResponse.getStatus());
    }

    @Test
    public void test30_findAllAssetTagPaginationShouldFailIfNotLogged() {
        AssetTagRestApi assetTagRestApi = getOsgiService(AssetTagRestApi.class);
        // the following call tries to find all AssetTag with pagination,
        // but HUser is not logged
        // response status code '403' HyperIoTUnauthorizedException
        this.impersonateUser(assetTagRestApi, null);
        Response restResponse = assetTagRestApi.findAllAssetTagPaginated(null, null);
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test31_saveAssetTagShouldFailIfDescriptionIsMaliciousCode() {
        AssetTagRestApi assetTagRestApi = getOsgiService(AssetTagRestApi.class);
        // hadmin tries to save AssetTag with the following call saveAssetTag,
        // but description is malicious code
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = createCompany((HUser) adminUser);
        Assert.assertNotEquals(0, company.getId());
        AssetTag assetTag = new AssetTag();
        String maliciousCode = "javascript:";
        assetTag.setDescription(maliciousCode);
        assetTag.setName("Tag" + java.util.UUID.randomUUID().toString().replaceAll("-", ""));
        HyperIoTAssetOwnerImpl owner = new HyperIoTAssetOwnerImpl();
        owner.setOwnerResourceName(companyResourceName);
        owner.setOwnerResourceId(company.getId());
        owner.setUserId(adminUser.getId());
        assetTag.setOwner(owner);
        this.impersonateUser(assetTagRestApi, adminUser);
        Response restResponse = assetTagRestApi.saveAssetTag(assetTag);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("assettag-description", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(maliciousCode,
                ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }

    @Test
    public void test32_saveAssetTagShouldFailIfDescriptionFieldIsGreaterThan255Chars() {
        AssetTagRestApi assetTagRestApi = getOsgiService(AssetTagRestApi.class);
        // hadmin tries to save AssetTag with the following call saveAssetTag,
        // but description's lenght is greater than 255 chars.
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = createCompany((HUser) adminUser);
        Assert.assertNotEquals(0, company.getId());
        AssetTag assetTag = new AssetTag();
        assetTag.setDescription(createStringFieldWithSpecifiedLenght(256));
        assetTag.setName("Tag" + java.util.UUID.randomUUID().toString().replaceAll("-", ""));
        HyperIoTAssetOwnerImpl owner = new HyperIoTAssetOwnerImpl();
        owner.setOwnerResourceName(companyResourceName);
        owner.setOwnerResourceId(company.getId());
        owner.setUserId(adminUser.getId());
        assetTag.setOwner(owner);
        this.impersonateUser(assetTagRestApi, adminUser);
        Response restResponse = assetTagRestApi.saveAssetTag(assetTag);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertEquals("assettag-description", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(assetTag.getDescription(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }

    @Test
    public void test33_saveAssetTagShouldFailIfColorIsMaliciousCode() {
        AssetTagRestApi assetTagRestApi = getOsgiService(AssetTagRestApi.class);
        // hadmin tries to save AssetTag with the following call saveAssetTag,
        // but color is malicious code
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = createCompany((HUser) adminUser);
        Assert.assertNotEquals(0, company.getId());
        AssetTag assetTag = new AssetTag();
        String maliciousCode = "javascript:";
        assetTag.setColor(maliciousCode);
        assetTag.setName("Tag" + java.util.UUID.randomUUID().toString().replaceAll("-", ""));
        HyperIoTAssetOwnerImpl owner = new HyperIoTAssetOwnerImpl();
        owner.setOwnerResourceName(companyResourceName);
        owner.setOwnerResourceId(company.getId());
        owner.setUserId(adminUser.getId());
        assetTag.setOwner(owner);
        this.impersonateUser(assetTagRestApi, adminUser);
        Response restResponse = assetTagRestApi.saveAssetTag(assetTag);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(2, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("assettag-color", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(maliciousCode, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
        Assert.assertEquals("assettag-color" , ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getField());
        Assert.assertEquals(maliciousCode, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getInvalidValue());
    }

    @Test
    public void test34_saveAssetTagShouldFailIfColorIsGreaterThan7Chars() {
        AssetTagRestApi assetTagRestApi = getOsgiService(AssetTagRestApi.class);
        // hadmin tries to save AssetTag with the following call saveAssetTag,
        // but color is greater than 7 chars
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = createCompany((HUser) adminUser);
        Assert.assertNotEquals(0, company.getId());
        AssetTag assetTag = new AssetTag();
        assetTag.setColor(createStringFieldWithSpecifiedLenght(8));
        assetTag.setName("Tag" + java.util.UUID.randomUUID().toString().replaceAll("-", ""));
        HyperIoTAssetOwnerImpl owner = new HyperIoTAssetOwnerImpl();
        owner.setOwnerResourceName(companyResourceName);
        owner.setOwnerResourceId(company.getId());
        owner.setUserId(adminUser.getId());
        assetTag.setOwner(owner);
        this.impersonateUser(assetTagRestApi, adminUser);
        Response restResponse = assetTagRestApi.saveAssetTag(assetTag);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("assettag-color", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(assetTag.getColor(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }

    @Test
    public void test35_saveAssetTagShouldFailIfColorIsLessThan3Chars() {
        AssetTagRestApi assetTagRestApi = getOsgiService(AssetTagRestApi.class);
        // hadmin tries to save AssetTag with the following call saveAssetTag,
        // but color is less than 3 chars
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Company company = createCompany((HUser) adminUser);
        Assert.assertNotEquals(0, company.getId());
        AssetTag assetTag = new AssetTag();
        assetTag.setColor(createStringFieldWithSpecifiedLenght(2));
        assetTag.setName("Tag" + java.util.UUID.randomUUID().toString().replaceAll("-", ""));
        HyperIoTAssetOwnerImpl owner = new HyperIoTAssetOwnerImpl();
        owner.setOwnerResourceName(companyResourceName);
        owner.setOwnerResourceId(company.getId());
        owner.setUserId(adminUser.getId());
        assetTag.setOwner(owner);
        this.impersonateUser(assetTagRestApi, adminUser);
        Response restResponse = assetTagRestApi.saveAssetTag(assetTag);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("assettag-color", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(assetTag.getColor(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }

    @Test
    public void test36_updateAssetTagShouldFailIfDescriptionIsMaliciousCode(){
        AssetTagRestApi assetTagRestApi = getOsgiService(AssetTagRestApi.class);
        // hadmin tries to update AssetTag with the following call updateAssetTag,
        // but description is malicious code
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        AssetTag tag = createAssetTag();
        String maliciousCode = "javascript:";
        tag.setDescription(maliciousCode);
        Response restResponse = assetTagRestApi.update(tag);
        this.impersonateUser(assetTagRestApi, adminUser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("assettag-description", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(maliciousCode,
                ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }

    @Test
    public void test37_updateAssetTagShouldFailIfDescriptionIsGreaterThan255Chars(){
        AssetTagRestApi assetTagRestApi = getOsgiService(AssetTagRestApi.class);
        // hadmin tries to update AssetTag with the following call updateAssetTag,
        // but description is greater than 255 chars
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        AssetTag tag = createAssetTag();
        tag.setDescription(createStringFieldWithSpecifiedLenght(256));
        this.impersonateUser(assetTagRestApi, adminUser);
        Response restResponse = assetTagRestApi.update(tag);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertEquals("assettag-description", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(tag.getDescription(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());

    }

    @Test
    public void test38_updateAssetTagShouldFailIfColorIsMaliciousCode(){
        AssetTagRestApi assetTagRestApi = getOsgiService(AssetTagRestApi.class);
        // hadmin tries to update AssetTag with the following call updateAssetTag,
        // but color is malicious code
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        AssetTag tag = createAssetTag();
        String maliciousCode = "javascript:";
        tag.setColor(maliciousCode);
        this.impersonateUser(assetTagRestApi , adminUser);
        Response restResponse = assetTagRestApi.update(tag);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(2, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("assettag-color", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(maliciousCode, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
        Assert.assertEquals("assettag-color" , ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getField());
        Assert.assertEquals(maliciousCode, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getInvalidValue());

    }

    @Test
    public void test39_updateAssetTagShouldFailIfColorIsLessThan3Chars(){
        AssetTagRestApi assetTagRestApi = getOsgiService(AssetTagRestApi.class);
        // hadmin tries to update AssetTag with the following call updateAssetTag,
        // but color is less than 3 chars
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        AssetTag tag = createAssetTag();
        tag.setColor(createStringFieldWithSpecifiedLenght(2));
        this.impersonateUser(assetTagRestApi , adminUser);
        Response restResponse = assetTagRestApi.update(tag);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("assettag-color", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(tag.getColor(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());

    }

    @Test
    public void test39_updateAssetTagShouldFailIfColorIsGreaterThan7Chars(){
        AssetTagRestApi assetTagRestApi = getOsgiService(AssetTagRestApi.class);
        // hadmin tries to update AssetTag with the following call updateAssetTag,
        // but color is greater than 7 chars
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        AssetTag tag = createAssetTag();
        tag.setColor(createStringFieldWithSpecifiedLenght(8));
        this.impersonateUser(assetTagRestApi , adminUser);
        Response restResponse = assetTagRestApi.update(tag);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("assettag-color", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(tag.getColor(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());

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
