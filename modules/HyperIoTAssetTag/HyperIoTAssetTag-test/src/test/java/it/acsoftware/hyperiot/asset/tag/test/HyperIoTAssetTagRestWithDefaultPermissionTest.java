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

import it.acsoftware.hyperiot.asset.tag.model.AssetTag;
import it.acsoftware.hyperiot.asset.tag.service.rest.AssetTagRestApi;
import it.acsoftware.hyperiot.base.api.authentication.AuthenticationApi;
import it.acsoftware.hyperiot.base.action.HyperIoTActionName;
import it.acsoftware.hyperiot.base.api.HyperIoTAction;
import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.base.api.HyperIoTUser;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTPaginableResult;
import it.acsoftware.hyperiot.base.model.HyperIoTAssetOwnerImpl;
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
import it.acsoftware.hyperiot.role.model.Role;
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
import java.util.*;

import static it.acsoftware.hyperiot.asset.tag.test.HyperIoTAssetTagConfiguration.*;

/**
 * @author Aristide Cittadino Interface component for AssetTag System Service.
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HyperIoTAssetTagRestWithDefaultPermissionTest extends KarafTestSupport {

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
    public void test001_assetTagModuleShouldWork() {
        AssetTagRestApi assetTagRestApi = getOsgiService(AssetTagRestApi.class);
        // the following call checkModuleWorking checks if AssetTag module working
        // correctly
        HUser huser = huserWithDefaultPermissionInHyperIoTFramework(true);
        Assert.assertNotEquals(0, huser.getId());
        Assert.assertTrue(huser.isActive());
        this.impersonateUser(assetTagRestApi, huser);
        Response restResponse = assetTagRestApi.checkModuleWorking();
        Assert.assertEquals(200, restResponse.getStatus());
        Assert.assertEquals("AssetTag Module works!", restResponse.getEntity());
    }


    // AssetTag action save: 1
    @Test
    public void test002_saveAssetTagWithDefaultPermissionShouldWork() {
        AssetTagRestApi assetTagRestApi = getOsgiService(AssetTagRestApi.class);
        // HUser, with default permission, save AssetTag with the following call saveAssetTag
        // response status code '200'
        HUser huser = huserWithDefaultPermissionInHyperIoTFramework(true);
        Assert.assertNotEquals(0, huser.getId());
        Assert.assertTrue(huser.isActive());

        Company company = createCompany(huser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(huser.getId(), company.getHUserCreator().getId());

        AssetTag assetTag = new AssetTag();
        assetTag.setName("Tag " + UUID.randomUUID());
        HyperIoTAssetOwnerImpl owner = new HyperIoTAssetOwnerImpl();
        owner.setOwnerResourceName(companyResourceName);
        owner.setOwnerResourceId(company.getId());
        owner.setUserId(huser.getId());
        assetTag.setOwner(owner);
        this.impersonateUser(assetTagRestApi, huser);
        Response restResponse = assetTagRestApi.saveAssetTag(assetTag);
        Assert.assertEquals(200, restResponse.getStatus());
        Assert.assertNotEquals(0,
                ((AssetTag) restResponse.getEntity()).getId());
        Assert.assertEquals(assetTag.getName(),
                ((AssetTag) restResponse.getEntity()).getName());
        Assert.assertEquals(assetTag.getOwner().getOwnerResourceName(),
                ((AssetTag) restResponse.getEntity()).getOwner().getOwnerResourceName());
        Assert.assertEquals(company.getId(),
                ((AssetTag) restResponse.getEntity()).getOwner().getOwnerResourceId().longValue());
        Assert.assertEquals(huser.getId(),
                ((AssetTag) restResponse.getEntity()).getOwner().getUserId());
    }


    // AssetTag action update: 2
    @Test
    public void test003_updateAssetTagWithDefaultPermissionShouldWork() {
        AssetTagRestApi assetTagRestApi = getOsgiService(AssetTagRestApi.class);
        // HUser, with default permission, update AssetTag with the following call updateAssetTag
        // response status code '200'
        HUser huser = huserWithDefaultPermissionInHyperIoTFramework(true);
        Assert.assertNotEquals(0, huser.getId());
        Assert.assertTrue(huser.isActive());

        AssetTag assetTag = createAssetTag(huser);
        Assert.assertNotEquals(0, assetTag.getId());

        Date date = new Date();
        assetTag.setName("name edited in date: " + date);

        this.impersonateUser(assetTagRestApi, huser);
        Response restResponse = assetTagRestApi.updateAssetTag(assetTag);
        Assert.assertEquals(200, restResponse.getStatus());
        Assert.assertEquals("name edited in date: " + date,
                ((AssetTag) restResponse.getEntity()).getName());
        Assert.assertEquals(assetTag.getEntityVersion() + 1,
                (((AssetTag) restResponse.getEntity()).getEntityVersion()));
    }


    // AssetTag action remove: 4
    @Test
    public void test004_deleteAssetTagWithDefaultPermissionShouldWork() {
        AssetTagRestApi assetTagRestApi = getOsgiService(AssetTagRestApi.class);
        // HUser, with default permission, delete AssetTag with the following call deleteAssetTag
        // response status code '200'
        HUser huser = huserWithDefaultPermissionInHyperIoTFramework(true);
        Assert.assertNotEquals(0, huser.getId());
        Assert.assertTrue(huser.isActive());

        AssetTag assetTag = createAssetTag(huser);
        Assert.assertNotEquals(0, assetTag.getId());

        this.impersonateUser(assetTagRestApi, huser);
        Response restResponse = assetTagRestApi.deleteAssetTag(assetTag.getId());
        Assert.assertEquals(200, restResponse.getStatus());
        Assert.assertNull(restResponse.getEntity());
    }


    // AssetTag action find: 8
    @Test
    public void test005_findAssetTagWithDefaultPermissionShouldWork() {
        AssetTagRestApi assetTagRestApi = getOsgiService(AssetTagRestApi.class);
        // HUser, with default permission, find AssetTag with the following call findAssetTag
        // response status code '200'
        HUser huser = huserWithDefaultPermissionInHyperIoTFramework(true);
        Assert.assertNotEquals(0, huser.getId());
        Assert.assertTrue(huser.isActive());

        AssetTag assetTag = createAssetTag(huser);
        Assert.assertNotEquals(0, assetTag.getId());

        this.impersonateUser(assetTagRestApi, huser);
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


    // AssetTag action find-all: 16
    @Test
    public void test006_findAllAssetTagWithDefaultPermissionShouldWork() {
        AssetTagRestApi assetTagRestApi = getOsgiService(AssetTagRestApi.class);
        // HUser, with default permission, find all AssetTag with the following
        // call findAllAssetTag
        // response status code '200'
        HUser huser = huserWithDefaultPermissionInHyperIoTFramework(true);
        Assert.assertNotEquals(0, huser.getId());
        Assert.assertTrue(huser.isActive());

        AssetTag assetTag = createAssetTag(huser);
        Assert.assertNotEquals(0, assetTag.getId());

        this.impersonateUser(assetTagRestApi, huser);
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
        Assert.assertEquals(200, restResponse.getStatus());
    }


    // AssetTag action find-all: 16
    @Test
    public void test007_findAllAssetTagPaginatedWithDefaultPermissionShouldWork() {
        AssetTagRestApi assetTagRestApi = getOsgiService(AssetTagRestApi.class);
        // In this following call findAllAssetTag, HUser, with default permission,
        // find all AssetTag with pagination
        // response status code '200'
        int delta = 5;
        int page = 1;
        HUser huser = huserWithDefaultPermissionInHyperIoTFramework(true);
        Assert.assertNotEquals(0, huser.getId());
        Assert.assertTrue(huser.isActive());

        List<AssetTag> tags = new ArrayList<>();
        int numEntities = 7;
        for (int i = 0; i < numEntities; i++) {
            AssetTag assetTag = createAssetTag(huser);
            Assert.assertNotEquals(0, assetTag.getId());
            tags.add(assetTag);
        }
        Assert.assertEquals(numEntities, tags.size());
        this.impersonateUser(assetTagRestApi, huser);
        Response restResponse = assetTagRestApi.findAllAssetTagPaginated(delta, page);
        HyperIoTPaginableResult<AssetTag> listAssetTags = restResponse
                .readEntity(new GenericType<HyperIoTPaginableResult<AssetTag>>() {
                });
        Assert.assertFalse(listAssetTags.getResults().isEmpty());
        Assert.assertEquals(delta, listAssetTags.getResults().size());
        Assert.assertEquals(delta, listAssetTags.getDelta());
        Assert.assertEquals(page, listAssetTags.getCurrentPage());
        Assert.assertEquals(page + 1, listAssetTags.getNextPage());
        // delta is 5, page 1: 7 entities stored in database
        Assert.assertEquals(2, listAssetTags.getNumPages());
        Assert.assertEquals(200, restResponse.getStatus());
    }



    /*
     *
     *
     * UTILITY METHODS
     *
     *
     */




    private AssetTag createAssetTag(HUser huser) {
        AssetTagRestApi assetTagRestApi = getOsgiService(AssetTagRestApi.class);
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");

        Company company = createCompany(huser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(huser.getId(), company.getHUserCreator().getId());

        AssetTag assetTag = new AssetTag();
        assetTag.setName("Tag" + UUID.randomUUID());
        HyperIoTAssetOwnerImpl owner = new HyperIoTAssetOwnerImpl();
        owner.setOwnerResourceName(companyResourceName);
        owner.setOwnerResourceId(company.getId());
        owner.setUserId(huser.getId());
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
        Assert.assertEquals(huser.getId(),
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
        company.setVatNumber("01234567890" + UUID.randomUUID().toString().replaceAll("-", ""));
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


    /*
     *
     *
     * TEST WITH DEFAULT PERMISSIONS
     *
     *
     */


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
            for (int i = 0; i < roles.size(); i++){
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
            boolean resourceNameAssetTag = false;
            for (int i = 0; i < listPermissions.size(); i++) {
                if (((Permission) ((ArrayList) listPermissions).get(i)).getEntityResourceName().contains(permissionAssetTag)) {
                    resourceNameAssetTag = true;
                    Assert.assertEquals("it.acsoftware.hyperiot.asset.tag.model.AssetTag", ((Permission) ((ArrayList) listPermissions).get(i)).getEntityResourceName());
                    Assert.assertEquals(permissionAssetTag + nameRegisteredPermission, ((Permission) ((ArrayList) listPermissions).get(i)).getName());
                    Assert.assertEquals(31, ((Permission) ((ArrayList) listPermissions).get(i)).getActionIds());
                    Assert.assertEquals(role.getName(), ((Permission) ((ArrayList) listPermissions).get(i)).getRole().getName());
                }
            }
            Assert.assertTrue(resourceNameAssetTag);
        }
        return huser;
    }


    @After
    public void afterTest() {
        AssetTagRestApi assetTagRestApi = getOsgiService(AssetTagRestApi.class);
        // Remove all assetTag created in every test
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
