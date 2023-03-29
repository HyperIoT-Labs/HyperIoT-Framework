/*
 * Copyright 2019-2023 HyperIoT
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

package it.acsoftware.hyperiot.asset.category.test;

import it.acsoftware.hyperiot.asset.category.model.AssetCategory;
import it.acsoftware.hyperiot.asset.category.service.rest.AssetCategoryRestApi;
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

import static it.acsoftware.hyperiot.asset.category.test.HyperIoTAssetCategoryConfiguration.*;


@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HyperIoTAssetCategoryRestWithDefaultPermissionTest extends KarafTestSupport {

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
    public void test001_assetCategoryModuleShouldWork() {
        AssetCategoryRestApi assetCategoryRestApi = getOsgiService(AssetCategoryRestApi.class);
        // the following call checkModuleWorking checks if AssetCategory module working
        // correctly
        HUser huser = huserWithDefaultPermissionInHyperIoTFramework(true);
        Assert.assertNotEquals(0, huser.getId());
        Assert.assertTrue(huser.isActive());
        this.impersonateUser(assetCategoryRestApi, huser);
        Response restResponse = assetCategoryRestApi.checkModuleWorking();
        Assert.assertEquals(200, restResponse.getStatus());
        Assert.assertEquals("AssetCategory Module works!", restResponse.getEntity());
    }


    // AssetCategory action save: 1
    @Test
    public void test002_saveAssetCategoryWithDefaultPermissionShouldWork() {
        AssetCategoryRestApi assetCategoryRestApi = getOsgiService(AssetCategoryRestApi.class);
        // HUser, with default permission, save AssetCategory with the following call saveAssetCategory
        // response status code '200'
        HUser huser = huserWithDefaultPermissionInHyperIoTFramework(true);
        Assert.assertNotEquals(0, huser.getId());
        Assert.assertTrue(huser.isActive());

        Company company = createCompany(huser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(huser.getId(), company.getHUserCreator().getId());

        AssetCategory assetCategory = new AssetCategory();
        assetCategory.setName("Category " + UUID.randomUUID());
        HyperIoTAssetOwnerImpl owner = new HyperIoTAssetOwnerImpl();
        owner.setOwnerResourceName(companyResourceName);
        owner.setOwnerResourceId(company.getId());
        owner.setUserId(huser.getId());
        assetCategory.setOwner(owner);

        this.impersonateUser(assetCategoryRestApi, huser);
        Response restResponse = assetCategoryRestApi.saveAssetCategory(assetCategory);
        Assert.assertEquals(200, restResponse.getStatus());
        Assert.assertNotEquals(0,
                ((AssetCategory) restResponse.getEntity()).getId());
        Assert.assertEquals(assetCategory.getName(), ((AssetCategory) restResponse.getEntity()).getName());
        Assert.assertEquals(companyResourceName,
                ((AssetCategory) restResponse.getEntity()).getOwner().getOwnerResourceName());
        Assert.assertEquals((Long)company.getId(),
                ((AssetCategory) restResponse.getEntity()).getOwner().getOwnerResourceId());
        Assert.assertEquals(huser.getId(),
                ((AssetCategory) restResponse.getEntity()).getOwner().getUserId());
    }


    // AssetCategory action update: 2
    @Test
    public void test003_updateAssetCategoryWithDefaultPermissionShouldWork() {
        AssetCategoryRestApi assetCategoryRestApi = getOsgiService(AssetCategoryRestApi.class);
        // HUser, with default permission, update AssetCategory with the following call updateAssetCategory
        // response status code '200'
        HUser huser = huserWithDefaultPermissionInHyperIoTFramework(true);
        Assert.assertNotEquals(0, huser.getId());
        Assert.assertTrue(huser.isActive());

        AssetCategory assetCategory = createAssetCategory(huser);
        Assert.assertNotEquals(0, assetCategory.getId());

        Date date = new Date();
        assetCategory.setName("name edited in date: " + date);

        this.impersonateUser(assetCategoryRestApi, huser);
        Response restResponse = assetCategoryRestApi.updateAssetCategory(assetCategory);
        Assert.assertEquals(200, restResponse.getStatus());
        Assert.assertEquals("name edited in date: " + date,
                ((AssetCategory) restResponse.getEntity()).getName());
        Assert.assertEquals(assetCategory.getEntityVersion() + 1,
                (((AssetCategory) restResponse.getEntity()).getEntityVersion()));
    }


    // AssetCategory action remove: 4
    @Test
    public void test004_deleteAssetCategoryWithDefaultPermissionShouldWork() {
        AssetCategoryRestApi assetCategoryRestApi = getOsgiService(AssetCategoryRestApi.class);
        // HUser, with default permission, delete AssetCategory with the following call deleteAssetCategory
        // response status code '200'
        HUser huser = huserWithDefaultPermissionInHyperIoTFramework(true);
        Assert.assertNotEquals(0, huser.getId());
        Assert.assertTrue(huser.isActive());

        AssetCategory assetCategory = createAssetCategory(huser);
        Assert.assertNotEquals(0, assetCategory.getId());

        this.impersonateUser(assetCategoryRestApi, huser);
        Response restResponse = assetCategoryRestApi.deleteAssetCategory(assetCategory.getId());
        Assert.assertEquals(200, restResponse.getStatus());
        Assert.assertNull(restResponse.getEntity());
    }


    // AssetCategory action find: 8
    @Test
    public void test005_findAssetCategoryWithDefaultPermissionShouldWork() {
        AssetCategoryRestApi assetCategoryRestApi = getOsgiService(AssetCategoryRestApi.class);
        // HUser, with default permission, find AssetCategory with the following call findAssetCategory
        // response status code '200'
        HUser huser = huserWithDefaultPermissionInHyperIoTFramework(true);
        Assert.assertNotEquals(0, huser.getId());
        Assert.assertTrue(huser.isActive());

        AssetCategory assetCategory = createAssetCategory(huser);
        Assert.assertNotEquals(0, assetCategory.getId());

        this.impersonateUser(assetCategoryRestApi, huser);
        Response restResponse = assetCategoryRestApi.findAssetCategory(assetCategory.getId());
        Assert.assertEquals(200, restResponse.getStatus());
        Assert.assertEquals(assetCategory.getId(), ((AssetCategory) restResponse.getEntity()).getId());
        Assert.assertEquals(assetCategory.getName(), ((AssetCategory) restResponse.getEntity()).getName());
        Assert.assertEquals(assetCategory.getOwner().getOwnerResourceName(),
                ((AssetCategory) restResponse.getEntity()).getOwner().getOwnerResourceName());
        Assert.assertEquals(assetCategory.getOwner().getOwnerResourceId(),
                ((AssetCategory) restResponse.getEntity()).getOwner().getOwnerResourceId());
        Assert.assertEquals(assetCategory.getOwner().getUserId(),
                ((AssetCategory) restResponse.getEntity()).getOwner().getUserId());
    }


    // AssetCategory action find-all: 16
    @Test
    public void test006_findAllAssetCategoriesWithDefaultPermissionShouldWork() {
        AssetCategoryRestApi assetCategoryRestApi = getOsgiService(AssetCategoryRestApi.class);
        // HUser, with default permission, find all AssetCategories with the following call findAllAssetCategories
        // response status code '200'
        HUser huser = huserWithDefaultPermissionInHyperIoTFramework(true);
        Assert.assertNotEquals(0, huser.getId());
        Assert.assertTrue(huser.isActive());

        AssetCategory assetCategory = createAssetCategory(huser);
        Assert.assertNotEquals(0, assetCategory.getId());

        this.impersonateUser(assetCategoryRestApi, huser);
        Response restResponse = assetCategoryRestApi.findAllAssetCategory();
        Assert.assertEquals(200, restResponse.getStatus());
        List<AssetCategory> listCategories = restResponse.readEntity(new GenericType<List<AssetCategory>>() {
        });
        Assert.assertFalse(listCategories.isEmpty());
        boolean assetCategoryFound = false;
        for (AssetCategory category : listCategories) {
            if (category.getId() == assetCategory.getId()) {
                assetCategoryFound = true;
            }
        }
        Assert.assertTrue(assetCategoryFound);
    }


    // AssetCategory action find-all: 16
    @Test
    public void test007_findAllAssetCategoriesPaginatedWithDefaultPermissionShouldWork() {
        AssetCategoryRestApi assetCategoryRestApi = getOsgiService(AssetCategoryRestApi.class);
        // In this following call findAllAssetCategories, HUser, with default permission,
        // find all AssetCategories with pagination
        // response status code '200'
        int delta = 5;
        int page = 4;
        HUser huser = huserWithDefaultPermissionInHyperIoTFramework(true);
        Assert.assertNotEquals(0, huser.getId());
        Assert.assertTrue(huser.isActive());
        List<AssetCategory> categories = new ArrayList<>();
        int numEntities = 16;
        for (int i = 0; i < numEntities; i++) {
            AssetCategory assetCategory = createAssetCategory(huser);
            Assert.assertNotEquals(0, assetCategory.getId());
            categories.add(assetCategory);
        }
        Assert.assertEquals(numEntities, categories.size());
        this.impersonateUser(assetCategoryRestApi, huser);
        Response restResponse = assetCategoryRestApi.findAllAssetCategoryPaginated(delta, page);
        HyperIoTPaginableResult<AssetCategory> listAssetCategories = restResponse
                .readEntity(new GenericType<HyperIoTPaginableResult<AssetCategory>>() {
                });
        Assert.assertFalse(listAssetCategories.getResults().isEmpty());
        Assert.assertEquals(1, listAssetCategories.getResults().size());
        Assert.assertEquals(delta, listAssetCategories.getDelta());
        Assert.assertEquals(page, listAssetCategories.getCurrentPage());
        Assert.assertEquals(1, listAssetCategories.getNextPage());
        // delta is 5, page 4: 16 entities stored in database
        Assert.assertEquals(4, listAssetCategories.getNumPages());
        Assert.assertEquals(200, restResponse.getStatus());

        //checks with page = 1
        this.impersonateUser(assetCategoryRestApi, huser);
        Response restResponsePage1 = assetCategoryRestApi.findAllAssetCategoryPaginated(delta, 1);
        HyperIoTPaginableResult<AssetCategory> listAssetCategoriesPage1 = restResponsePage1
                .readEntity(new GenericType<HyperIoTPaginableResult<AssetCategory>>() {
                });
        Assert.assertFalse(listAssetCategoriesPage1.getResults().isEmpty());
        Assert.assertEquals(delta, listAssetCategoriesPage1.getResults().size());
        Assert.assertEquals(delta, listAssetCategoriesPage1.getDelta());
        Assert.assertEquals(defaultPage, listAssetCategoriesPage1.getCurrentPage());
        Assert.assertEquals(defaultPage + 1, listAssetCategoriesPage1.getNextPage());
        // delta is 5, page 1: 16 entities stored in database
        Assert.assertEquals(4, listAssetCategoriesPage1.getNumPages());
        Assert.assertEquals(200, restResponsePage1.getStatus());

        //checks with page = 2
        this.impersonateUser(assetCategoryRestApi, huser);
        Response restResponsePage2 = assetCategoryRestApi.findAllAssetCategoryPaginated(delta, 2);
        HyperIoTPaginableResult<AssetCategory> listAssetCategoriesPage2 = restResponsePage2
                .readEntity(new GenericType<HyperIoTPaginableResult<AssetCategory>>() {
                });
        Assert.assertFalse(listAssetCategoriesPage2.getResults().isEmpty());
        Assert.assertEquals(delta, listAssetCategoriesPage2.getResults().size());
        Assert.assertEquals(delta, listAssetCategoriesPage2.getDelta());
        Assert.assertEquals(defaultPage + 1, listAssetCategoriesPage2.getCurrentPage());
        Assert.assertEquals(page - 1, listAssetCategoriesPage2.getNextPage());
        // delta is 5, page 2: 16 entities stored in database
        Assert.assertEquals(4, listAssetCategoriesPage2.getNumPages());
        Assert.assertEquals(200, restResponsePage2.getStatus());

        //checks with page = 3
        this.impersonateUser(assetCategoryRestApi, huser);
        Response restResponsePage3 = assetCategoryRestApi.findAllAssetCategoryPaginated(delta, 3);
        HyperIoTPaginableResult<AssetCategory> listAssetCategoriesPage3 = restResponsePage3
                .readEntity(new GenericType<HyperIoTPaginableResult<AssetCategory>>() {
                });
        Assert.assertFalse(listAssetCategoriesPage3.getResults().isEmpty());
        Assert.assertEquals(delta, listAssetCategoriesPage3.getResults().size());
        Assert.assertEquals(delta, listAssetCategoriesPage3.getDelta());
        Assert.assertEquals(page - 1, listAssetCategoriesPage3.getCurrentPage());
        Assert.assertEquals(page, listAssetCategoriesPage3.getNextPage());
        // delta is 5, page 3: 16 entities stored in database
        Assert.assertEquals(4, listAssetCategoriesPage3.getNumPages());
        Assert.assertEquals(200, restResponsePage3.getStatus());
    }


    /*
     *
     *
     * UTILITY METHODS
     *
     *
     */


    private AssetCategory createAssetCategory(HUser huser) {
        AssetCategoryRestApi assetCategoryRestApi = getOsgiService(AssetCategoryRestApi.class);
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");

        Company company = createCompany(huser);
        Assert.assertNotEquals(0, company.getId());
        Assert.assertEquals(huser.getId(), company.getHUserCreator().getId());

        AssetCategory assetCategory = new AssetCategory();
        assetCategory.setName("Category" + UUID.randomUUID());
        HyperIoTAssetOwnerImpl owner = new HyperIoTAssetOwnerImpl();
        owner.setOwnerResourceName(companyResourceName);
        owner.setOwnerResourceId(company.getId());
        owner.setUserId(huser.getId());
        assetCategory.setOwner(owner);
        this.impersonateUser(assetCategoryRestApi, adminUser);
        Response restResponse = assetCategoryRestApi.saveAssetCategory(assetCategory);
        Assert.assertEquals(200, restResponse.getStatus());
        Assert.assertNotEquals(0,
                ((AssetCategory) restResponse.getEntity()).getId());
        Assert.assertEquals(assetCategory.getName(), ((AssetCategory) restResponse.getEntity()).getName());
        Assert.assertEquals(companyResourceName,
                ((AssetCategory) restResponse.getEntity()).getOwner().getOwnerResourceName());
        Assert.assertEquals((Long)company.getId(),
                ((AssetCategory) restResponse.getEntity()).getOwner().getOwnerResourceId());
        Assert.assertEquals(huser.getId(),
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
            boolean resourceNameAssetCategory = false;
            for (int i = 0; i < listPermissions.size(); i++) {
                if (((Permission) ((ArrayList) listPermissions).get(i)).getEntityResourceName().contains(permissionAssetCategory)) {
                    resourceNameAssetCategory = true;
                    Assert.assertEquals("it.acsoftware.hyperiot.asset.category.model.AssetCategory", ((Permission) ((ArrayList) listPermissions).get(i)).getEntityResourceName());
                    Assert.assertEquals(permissionAssetCategory + nameRegisteredPermission, ((Permission) ((ArrayList) listPermissions).get(i)).getName());
                    Assert.assertEquals(31, ((Permission) ((ArrayList) listPermissions).get(i)).getActionIds());
                    Assert.assertEquals(role.getName(), ((Permission) ((ArrayList) listPermissions).get(i)).getRole().getName());
                }
            }
            Assert.assertTrue(resourceNameAssetCategory);
        }
        return huser;
    }


    @After
    public void afterTest() {
        AssetCategoryRestApi assetCategoryRestApi = getOsgiService(AssetCategoryRestApi.class);
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
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
