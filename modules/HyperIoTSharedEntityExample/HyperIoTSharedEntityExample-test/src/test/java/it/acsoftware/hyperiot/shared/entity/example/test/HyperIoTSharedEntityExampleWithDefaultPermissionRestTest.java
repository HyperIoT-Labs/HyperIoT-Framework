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

package it.acsoftware.hyperiot.shared.entity.example.test;

import it.acsoftware.hyperiot.base.api.authentication.AuthenticationApi;
import it.acsoftware.hyperiot.base.action.HyperIoTActionName;
import it.acsoftware.hyperiot.base.action.util.HyperIoTActionsUtil;
import it.acsoftware.hyperiot.base.action.util.HyperIoTCrudAction;
import it.acsoftware.hyperiot.base.action.util.HyperIoTShareAction;
import it.acsoftware.hyperiot.base.api.HyperIoTAction;
import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.base.api.HyperIoTUser;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTBaseEntity;
import it.acsoftware.hyperiot.base.model.HyperIoTBaseError;
import it.acsoftware.hyperiot.base.service.rest.HyperIoTBaseRestApi;
import it.acsoftware.hyperiot.base.test.HyperIoTTestConfigurationBuilder;
import it.acsoftware.hyperiot.base.util.HyperIoTConstants;
import it.acsoftware.hyperiot.huser.model.HUser;
import it.acsoftware.hyperiot.huser.service.rest.HUserRestApi;
import it.acsoftware.hyperiot.osgi.util.filter.OSGiFilterBuilder;
import it.acsoftware.hyperiot.permission.api.PermissionSystemApi;
import it.acsoftware.hyperiot.permission.model.Permission;
import it.acsoftware.hyperiot.permission.service.rest.PermissionRestApi;
import it.acsoftware.hyperiot.role.model.Role;
import it.acsoftware.hyperiot.role.service.rest.RoleRestApi;
import it.acsoftware.hyperiot.shared.entity.example.model.SharedEntityExample;
import it.acsoftware.hyperiot.shared.entity.example.service.rest.SharedEntityExampleRestApi;
import it.acsoftware.hyperiot.shared.entity.model.SharedEntity;
import it.acsoftware.hyperiot.shared.entity.service.rest.SharedEntityRestApi;
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

import static it.acsoftware.hyperiot.shared.entity.example.test.HyperIoTSharedEntityExampleConfiguration.*;

/**
 *
 * @author Aristide Cittadino Interface component for SharedEntityExample System Service.
 *
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HyperIoTSharedEntityExampleWithDefaultPermissionRestTest extends KarafTestSupport {

	//force global configuration
	public Option[] config() {
		return null;
	}

	public HyperIoTContext impersonateUser(HyperIoTBaseRestApi restApi,HyperIoTUser user) {
		return restApi.impersonate(user);
	}

	private HyperIoTAction getHyperIoTAction(String resourceName,
			HyperIoTActionName action, long timeout) {
		String actionFilter = OSGiFilterBuilder
				.createFilter(HyperIoTConstants.OSGI_ACTION_RESOURCE_NAME, resourceName)
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
		assertContains("HyperIoTSharedEntityExample-features", features);
		String datasource = executeCommand("jdbc:ds-list");
//		System.out.println(executeCommand("bundle:list | grep HyperIoT"));
		assertContains("hyperiot", datasource);
	}


	@Test
	public void test001_sharedEntityModuleShouldWork() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// the following call checkModuleWorking checks if SharedEntity module working
		// correctly
		ownerUser = huserWithDefaultPermissionInHyperIoTFramework(true);
		Assert.assertNotEquals(0, ownerUser.getId());
		Assert.assertTrue(ownerUser.isActive());
		this.impersonateUser(sharedEntityRestApi, ownerUser);
		Response restResponse = sharedEntityRestApi.checkModuleWorking();
		Assert.assertEquals(200, restResponse.getStatus());
		Assert.assertEquals("SharedEntity Module works!", restResponse.getEntity());
	}


	// SharedEntity action find: 8 not assigned in default permission
	@Test
	public void test002_findByPKWithDefaultPermissionShouldFail() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// ownerUser, with default permission, tries to find SharedEntity by primary key with the following call findByPK
		// ownerUser to find SharedEntity by primary key needs the "find shared-entity" permission
		// response status code '403' HyperIoTUnauthorizedException
		ownerUser = huserWithDefaultPermissionInHyperIoTFramework(true);
		Assert.assertNotEquals(0, ownerUser.getId());
		Assert.assertTrue(ownerUser.isActive());

		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		HUser huser = huserWithDefaultPermissionInHyperIoTFramework(true);
		Assert.assertNotEquals(0, huser.getId());
		Assert.assertTrue(huser.isActive());

		SharedEntity sharedEntity = createSharedEntity(entityExample, ownerUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		this.impersonateUser(sharedEntityRestApi, ownerUser);
		Response restResponse = sharedEntityRestApi.findByPK(sharedEntity);
		Assert.assertEquals(403, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	// SharedEntity action find: 8 not assigned in default permission
	@Test
	public void test003_findByEntityWithDefaultPermissionShouldFail() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// ownerUser, with default permission, tries to find SharedEntity by entity with the following call findByEntity
		// ownerUser to find SharedEntity by entity needs the "find shared-entity" permission
		// response status code '403' HyperIoTUnauthorizedException
		ownerUser = huserWithDefaultPermissionInHyperIoTFramework(true);
		Assert.assertNotEquals(0, ownerUser.getId());
		Assert.assertTrue(ownerUser.isActive());

		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		HUser huser = huserWithDefaultPermissionInHyperIoTFramework(true);
		Assert.assertNotEquals(0, huser.getId());
		Assert.assertTrue(huser.isActive());

		SharedEntity sharedEntity = createSharedEntity(entityExample, ownerUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		this.impersonateUser(sharedEntityRestApi, ownerUser);
		Response restResponse = sharedEntityRestApi
				.findByEntity(sharedEntity.getEntityResourceName(), sharedEntity.getEntityId());
		Assert.assertEquals(403, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	// SharedEntity action find: 8 not assigned in default permission
	@Test
	public void test004_findByUserWithDefaultPermissionShouldFail() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// ownerUser, with default permission, tries to find SharedEntity by huser with the following call findByUser
		// ownerUser to find SharedEntity by huser needs the "find shared-entity" permission
		// response status code '403' HyperIoTUnauthorizedException
		ownerUser = huserWithDefaultPermissionInHyperIoTFramework(true);
		Assert.assertNotEquals(0, ownerUser.getId());
		Assert.assertTrue(ownerUser.isActive());

		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		HUser huser = huserWithDefaultPermissionInHyperIoTFramework(true);
		Assert.assertNotEquals(0, huser.getId());
		Assert.assertTrue(huser.isActive());

		SharedEntity sharedEntity = createSharedEntity(entityExample, ownerUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		this.impersonateUser(sharedEntityRestApi, ownerUser);
		Response restResponse = sharedEntityRestApi.findByUser(huser.getId());
		Assert.assertEquals(403, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	// SharedEntity action find: 8 not assigned in default permission
	@Test
	public void test005_getUsersWithDefaultPermissionShouldFail() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// ownerUser, with default permission, tries to find users shared by the entity with the following call getUsers
		// ownerUser to find users shared by the entity needs the "find shared-entity" permission
		// response status code '403' HyperIoTUnauthorizedException
		ownerUser = huserWithDefaultPermissionInHyperIoTFramework(true);
		Assert.assertNotEquals(0, ownerUser.getId());
		Assert.assertTrue(ownerUser.isActive());

		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		HUser huser = huserWithDefaultPermissionInHyperIoTFramework(true);
		Assert.assertNotEquals(0, huser.getId());
		Assert.assertTrue(huser.isActive());

		SharedEntity sharedEntity = createSharedEntity(entityExample, ownerUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		this.impersonateUser(sharedEntityRestApi, ownerUser);
		Response restResponse = sharedEntityRestApi
				.getUsers(sharedEntity.getEntityResourceName(), sharedEntity.getEntityId());
		Assert.assertEquals(403, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	// SharedEntity action find-all: 16 not assigned in default permission
	@Test
	public void test006_findAllSharedEntityWithDefaultPermissionShouldFail() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// ownerUser, with default permission, tries to find all SharedEntity with the following call findAllSharedEntity
		// ownerUser to find all SharedEntity needs the "find-all shared-entity" permission
		// response status code '403' HyperIoTUnauthorizedException
		ownerUser = huserWithDefaultPermissionInHyperIoTFramework(true);
		Assert.assertNotEquals(0, ownerUser.getId());
		Assert.assertTrue(ownerUser.isActive());

		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		HUser huser = huserWithDefaultPermissionInHyperIoTFramework(true);
		Assert.assertNotEquals(0, huser.getId());
		Assert.assertTrue(huser.isActive());

		SharedEntity sharedEntity = createSharedEntity(entityExample, ownerUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		this.impersonateUser(sharedEntityRestApi, ownerUser);
		Response restResponse = sharedEntityRestApi.findAllSharedEntity();
		Assert.assertEquals(403, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	// SharedEntity action find-all: 16 not assigned in default permission
	@Test
	public void test007_findAllSharedEntityPaginatedWithDefaultPermissionShouldFail() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// ownerUser, with default permission, tries to find all SharedEntity with pagination with the following call findAllSharedEntityPaginated
		// ownerUser to find all SharedEntity needs the "find-all shared-entity" permission
		// response status code '403' HyperIoTUnauthorizedException
		int delta = 5;
		int page = 1;
		ownerUser = huserWithDefaultPermissionInHyperIoTFramework(true);
		Assert.assertNotEquals(0, ownerUser.getId());
		Assert.assertTrue(ownerUser.isActive());

		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		List<HUser> husers = new ArrayList<>();
		for (int i = 0; i < delta; i++) {
			HUser huser = huserWithDefaultPermissionInHyperIoTFramework(true);
			Assert.assertNotEquals(0, huser.getId());
			Assert.assertTrue(huser.isActive());
			husers.add(huser);
		}
		Assert.assertEquals(delta, husers.size());
		List<SharedEntity> sharedEntities = new ArrayList<>();
		for (int i = 0; i < delta; i++) {
			SharedEntity sharedEntity = createSharedEntity(entityExample, ownerUser, husers.get(i));
			Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
			Assert.assertEquals(husers.get(i).getId(), sharedEntity.getUserId());
			Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());
			sharedEntities.add(sharedEntity);
		}
		Assert.assertEquals(delta, sharedEntities.size());
		this.impersonateUser(sharedEntityRestApi, ownerUser);
		Response restResponse = sharedEntityRestApi.findAllSharedEntityPaginated(delta, page);
		Assert.assertEquals(403, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	// SharedEntity action share: 32 not assigned in default permission
	@Test
	public void test008_saveSharedEntityWithDefaultPermissionShouldFail() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// ownerUser, with default permission, tries to save SharedEntity with the following call saveSharedEntity
		// ownerUser to save SharedEntity needs the "shared" permission
		// response status code '403' HyperIoTUnauthorizedException
		ownerUser = huserWithDefaultPermissionInHyperIoTFramework(true);
		Assert.assertNotEquals(0, ownerUser.getId());
		Assert.assertTrue(ownerUser.isActive());

		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		HUser huser = huserWithDefaultPermissionInHyperIoTFramework(true);
		Assert.assertNotEquals(0, huser.getId());
		Assert.assertTrue(huser.isActive());

		SharedEntity sharedEntity = new SharedEntity();
		sharedEntity.setEntityId(entityExample.getId());
		sharedEntity.setEntityResourceName(entityExampleResourceName);
		sharedEntity.setUserId(huser.getId());

		this.impersonateUser(sharedEntityRestApi, ownerUser);
		Response restResponse = sharedEntityRestApi.saveSharedEntity(sharedEntity);
		Assert.assertEquals(403, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	// SharedEntity action share: 32 not assigned in default permission
	@Test
	public void test009_deleteSharedEntityWithDefaultPermissionShouldFail() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// ownerUser, with default permission, tries to delete SharedEntity with the following call deleteSharedEntity
		// ownerUser to delete SharedEntity needs the "shared" permission
		// response status code '403' HyperIoTUnauthorizedException
		ownerUser = huserWithDefaultPermissionInHyperIoTFramework(true);
		Assert.assertNotEquals(0, ownerUser.getId());
		Assert.assertTrue(ownerUser.isActive());

		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		HUser huser = huserWithDefaultPermissionInHyperIoTFramework(true);
		Assert.assertNotEquals(0, huser.getId());
		Assert.assertTrue(huser.isActive());

		SharedEntity sharedEntity = createSharedEntity(entityExample, ownerUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		this.impersonateUser(sharedEntityRestApi, ownerUser);
		Response restResponse = sharedEntityRestApi.deleteSharedEntity(sharedEntity);
		Assert.assertEquals(403, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	/*
	 *
	 *
	 * UTILITY METHODS
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
			Assert.assertEquals(3, listPermissions.size());
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
				permission.setName(entityExampleResourceName + " assigned to huser_id " + huser.getId());
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
		role.setName("Role" + UUID.randomUUID());
		role.setDescription("Description");
		Response restResponse = roleRestApi.saveRole(role);
		Assert.assertEquals(200, restResponse.getStatus());
		Assert.assertNotEquals(0, ((Role) restResponse.getEntity()).getId());
		Assert.assertEquals(role.getName(), ((Role) restResponse.getEntity()).getName());
		Assert.assertEquals(role.getDescription(), ((Role) restResponse.getEntity()).getDescription());
		return role;
	}

	private SharedEntityExample createSharedEntityExample(HUser ownerUser) {
		SharedEntityExampleRestApi sharedEntityExampleRestApi = getOsgiService(SharedEntityExampleRestApi.class);
		// ownerUser create his entity example
		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(entityExampleResourceName,
				HyperIoTCrudAction.SAVE);
		addPermission(ownerUser, action);

		SharedEntityExample entityExample = new SharedEntityExample();
		entityExample.setName("Shared entity example " + UUID.randomUUID());
		entityExample.setDescription("Property of user: " + ownerUser.getUsername());
		entityExample.setUser(ownerUser);
		this.impersonateUser(sharedEntityExampleRestApi, ownerUser);
		Response restResponse = sharedEntityExampleRestApi.saveSharedEntityExample(entityExample);
		Assert.assertEquals(200, restResponse.getStatus());
		Assert.assertNotEquals(0, ((SharedEntityExample) restResponse.getEntity()).getId());
		Assert.assertEquals(entityExample.getName(), ((SharedEntityExample) restResponse.getEntity()).getName());
		Assert.assertEquals("Property of user: " + ownerUser.getUsername(),
				((SharedEntityExample) restResponse.getEntity()).getDescription());
		Assert.assertEquals(ownerUser.getId(), ((SharedEntityExample) restResponse.getEntity()).getUser().getId());
		removePermission(ownerUser, action);
		return entityExample;
	}

	private SharedEntity createSharedEntity(HyperIoTBaseEntity hyperIoTBaseEntity, HUser ownerUser, HUser huser) {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);

		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(hyperIoTBaseEntity.getResourceName(),
				HyperIoTShareAction.SHARE);
		addPermission(ownerUser, action);

		SharedEntity sharedEntity = new SharedEntity();
		sharedEntity.setEntityId(hyperIoTBaseEntity.getId());
		sharedEntity.setEntityResourceName(hyperIoTBaseEntity.getResourceName()); // "it.acsoftware.hyperiot.shared.entity.example.HyperIoTSharedEntityExample"
		sharedEntity.setUserId(huser.getId());

		this.impersonateUser(sharedEntityRestApi, ownerUser);
		Response restResponse = sharedEntityRestApi.saveSharedEntity(sharedEntity);
		Assert.assertEquals(200, restResponse.getStatus());
		Assert.assertEquals(hyperIoTBaseEntity.getId(), ((SharedEntity) restResponse.getEntity()).getEntityId());
		Assert.assertEquals(huser.getId(), ((SharedEntity) restResponse.getEntity()).getUserId());
		Assert.assertEquals(hyperIoTBaseEntity.getResourceName(), ((SharedEntity) restResponse.getEntity()).getEntityResourceName());
		removePermission(ownerUser, action);
		return sharedEntity;
	}

	private Permission addPermission(HUser huser, HyperIoTAction action){
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
		Role role = null;
		Permission permission = null;
		List<Object> roles = Arrays.asList(huser.getRoles().toArray());
		if (!roles.isEmpty()) {
			Assert.assertFalse(roles.isEmpty());
			if (action != null) {
				PermissionSystemApi permissionSystemApi = getOsgiService(PermissionSystemApi.class);
				for (int i = 0; i < roles.size(); i++) {
					role = ((Role) roles.get(i));
					permission = permissionSystemApi.findByRoleAndResourceName(role, action.getResourceName());
					if (permission == null) {
						role = createRole();
						huser.addRole(role);
						RoleRestApi roleRestApi = getOsgiService(RoleRestApi.class);
						this.impersonateUser(roleRestApi, adminUser);
						Response restUserRole = roleRestApi.saveUserRole(role.getId(), huser.getId());
						Assert.assertEquals(200, restUserRole.getStatus());
						Assert.assertTrue(huser.hasRole(role));
						permission = utilGrantPermission(huser, role, action);
						Assert.assertNotEquals(0, permission.getId());
						Assert.assertEquals(entityExampleResourceName + " assigned to huser_id " + huser.getId(), permission.getName());
						Assert.assertEquals(action.getActionId(), permission.getActionIds());
						Assert.assertEquals(action.getCategory(), permission.getEntityResourceName());
						Assert.assertEquals(role.getId(), permission.getRole().getId());
					} else {
						PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
						this.impersonateUser(permissionRestApi, adminUser);
						permission.addPermission(action);
						Response restResponseUpdate = permissionRestApi.updatePermission(permission);
						Assert.assertEquals(200, restResponseUpdate.getStatus());
					}
				}
			}
		} else {
			role = createRole();
			huser.addRole(role);
			RoleRestApi roleRestApi = getOsgiService(RoleRestApi.class);
			this.impersonateUser(roleRestApi, adminUser);
			Response restUserRole = roleRestApi.saveUserRole(role.getId(), huser.getId());
			Assert.assertEquals(200, restUserRole.getStatus());
			Assert.assertTrue(huser.hasRole(role));
			permission = utilGrantPermission(huser, role, action);
			Assert.assertNotEquals(0, permission.getId());
			Assert.assertEquals(entityExampleResourceName + " assigned to huser_id " + huser.getId(), permission.getName());
			Assert.assertEquals(action.getActionId(), permission.getActionIds());
			Assert.assertEquals(action.getCategory(), permission.getEntityResourceName());
			Assert.assertEquals(role.getId(), permission.getRole().getId());
		}
		return permission;
	}

	private void removePermission(HUser huser, HyperIoTAction action){
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
		Role role = null;
		List<Object> roles = Arrays.asList(huser.getRoles().toArray());
		if (!roles.isEmpty()) {
			Assert.assertFalse(roles.isEmpty());
			if (action != null) {
				PermissionSystemApi permissionSystemApi = getOsgiService(PermissionSystemApi.class);
				Permission permission = null;
				for (int i = 0; i < roles.size(); i++){
					role = ((Role) roles.get(i));
					permission = permissionSystemApi.findByRoleAndResourceName(role, action.getResourceName());
					if (permission != null) {
						PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
						this.impersonateUser(permissionRestApi, adminUser);
						permission.removePermission(action);
						if (permission.getActionIds() > 0) {
							Response restResponseUpdate = permissionRestApi.updatePermission(permission);
							Assert.assertEquals(200, restResponseUpdate.getStatus());
						}
						if (permission.getActionIds() == 0) {
							Response restResponseDelete = permissionRestApi.deletePermission(permission.getId());
							Assert.assertEquals(200, restResponseDelete.getStatus());
							huser.removeRole(permission.getRole());
							RoleRestApi roleRestApi = getOsgiService(RoleRestApi.class);
							this.impersonateUser(roleRestApi, adminUser);
							Response restUserRole = roleRestApi.deleteUserRole(role.getId(), huser.getId());
							Assert.assertEquals(200, restUserRole.getStatus());
							Assert.assertFalse(huser.hasRole(role));
						}
					} else {
						Assert.assertNull(permission);
					}
				}
			} else {
				Assert.assertNull(action);
			}
		}
	}

	// only ownerUser is able to find/findAll his entities
	private HUser ownerUser;

	@After
	public void afterTest() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		HyperIoTAction action1 = HyperIoTActionsUtil.getHyperIoTAction(sharedEntityResourceName,
				HyperIoTCrudAction.FINDALL);
		HyperIoTAction action2 = HyperIoTActionsUtil.getHyperIoTAction(entityExampleResourceName,
				HyperIoTShareAction.SHARE);

		if ((ownerUser != null) && (ownerUser.isActive())) {
			addPermission(ownerUser, action1);
			this.impersonateUser(sharedEntityRestApi, ownerUser);
			Response restResponse = sharedEntityRestApi.findAllSharedEntity();
			Assert.assertEquals(200, restResponse.getStatus());
			List<SharedEntity> sharedEntityList = restResponse.readEntity(new GenericType<List<SharedEntity>>() {
			});
			if (!sharedEntityList.isEmpty()) {
				Assert.assertFalse(sharedEntityList.isEmpty());
				addPermission(ownerUser, action2);
				for (SharedEntity sharedEntity : sharedEntityList) {
					this.impersonateUser(sharedEntityRestApi, ownerUser);
					Response restResponse1 = sharedEntityRestApi.deleteSharedEntity(sharedEntity);
					Assert.assertEquals(200, restResponse1.getStatus());
					Assert.assertNull(restResponse1.getEntity());
				}
				removePermission(ownerUser, action2);
			}
			removePermission(ownerUser, action1);

			HyperIoTAction action3 = HyperIoTActionsUtil.getHyperIoTAction(entityExampleResourceName,
					HyperIoTCrudAction.FINDALL);
			HyperIoTAction action4 = HyperIoTActionsUtil.getHyperIoTAction(entityExampleResourceName,
					HyperIoTCrudAction.REMOVE);
			SharedEntityExampleRestApi entityExampleRestApi = getOsgiService(SharedEntityExampleRestApi.class);
			addPermission(ownerUser, action3);
			addPermission(ownerUser, action4);
			this.impersonateUser(entityExampleRestApi, ownerUser);
			Response responseFindAllEntityExample = entityExampleRestApi.findAllSharedEntityExample();
			List<SharedEntityExample> listEntities = responseFindAllEntityExample.readEntity(new GenericType<List<SharedEntityExample>>() {
			});
			if (!listEntities.isEmpty()) {
				Assert.assertFalse(listEntities.isEmpty());
				for (SharedEntityExample sharedEntityExample : listEntities) {
					this.impersonateUser(entityExampleRestApi, ownerUser);
					Response restResponse1 = entityExampleRestApi.deleteSharedEntityExample(sharedEntityExample.getId());
					Assert.assertEquals(200, restResponse1.getStatus());
					Assert.assertNull(restResponse1.getEntity());
				}
			}
			removePermission(ownerUser, action3);
			removePermission(ownerUser, action4);
		}

		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");

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

		// Remove all users created in every test
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
	}


}
