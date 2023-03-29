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

package it.acsoftware.hyperiot.shared.entity.example.test;

import it.acsoftware.hyperiot.base.action.HyperIoTActionList;
import it.acsoftware.hyperiot.base.api.authentication.AuthenticationApi;
import it.acsoftware.hyperiot.base.action.util.HyperIoTActionsUtil;
import it.acsoftware.hyperiot.base.action.util.HyperIoTCrudAction;
import it.acsoftware.hyperiot.base.action.util.HyperIoTShareAction;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTBaseEntity;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTPaginableResult;
import it.acsoftware.hyperiot.base.model.HyperIoTBaseError;
import it.acsoftware.hyperiot.huser.model.HUser;
import it.acsoftware.hyperiot.huser.service.rest.HUserRestApi;
import it.acsoftware.hyperiot.permission.api.PermissionSystemApi;
import it.acsoftware.hyperiot.permission.model.Permission;
import it.acsoftware.hyperiot.permission.service.rest.PermissionRestApi;
import it.acsoftware.hyperiot.role.api.RoleSystemApi;
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

import it.acsoftware.hyperiot.base.action.HyperIoTActionName;
import it.acsoftware.hyperiot.base.api.HyperIoTAction;
import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.base.api.HyperIoTUser;
import it.acsoftware.hyperiot.base.service.rest.HyperIoTBaseRestApi;
import it.acsoftware.hyperiot.base.test.HyperIoTTestConfigurationBuilder;
import it.acsoftware.hyperiot.base.util.HyperIoTConstants;
import it.acsoftware.hyperiot.osgi.util.filter.OSGiFilterBuilder;

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
public class HyperIoTSharedEntityExampleRestTest extends KarafTestSupport {

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
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
		this.impersonateUser(sharedEntityRestApi, adminUser);
		Response restResponse = sharedEntityRestApi.checkModuleWorking();
		Assert.assertEquals(200, restResponse.getStatus());
		Assert.assertEquals("SharedEntity Module works!", restResponse.getEntity());
	}


	@Test
	public void test002_saveSharedEntityShouldWork() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// hadmin save SharedEntity with the following call saveSharedEntity
		// response status code '200'
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
		Assert.assertTrue(adminUser.isAdmin());

		SharedEntityExample entityExample = createSharedEntityExample((HUser) adminUser);
		Assert.assertNotEquals(0, entityExample.getId());

		// adminUser share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = new SharedEntity();
		sharedEntity.setEntityId(entityExample.getId());
		sharedEntity.setEntityResourceName(entityExampleResourceName); // "it.acsoftware.hyperiot.shared.entity.example.HyperIoTSharedEntityExample"
		sharedEntity.setUserId(huser.getId());

		this.impersonateUser(sharedEntityRestApi, adminUser);
		Response restResponse = sharedEntityRestApi.saveSharedEntity(sharedEntity);
		Assert.assertEquals(200, restResponse.getStatus());
		Assert.assertEquals(entityExample.getId(), ((SharedEntity) restResponse.getEntity()).getEntityId());
		Assert.assertEquals(huser.getId(), ((SharedEntity) restResponse.getEntity()).getUserId());
		Assert.assertEquals(entityExampleResourceName, ((SharedEntity) restResponse.getEntity()).getEntityResourceName());
	}


	@Test
	public void test003_saveSharedEntityShouldFailIfNotLogged() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// the following call tries to save SharedEntity with the following call saveSharedEntity,
		// but huser is not logged
		// response status code '403' HyperIoTUnauthorizedException
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
		Assert.assertTrue(adminUser.isAdmin());

		SharedEntityExample entityExample = createSharedEntityExample((HUser) adminUser);
		Assert.assertNotEquals(0, entityExample.getId());

		HUser huser = createHUser(null);

		SharedEntity sharedEntity = new SharedEntity();
		sharedEntity.setEntityId(entityExample.getId());
		sharedEntity.setEntityResourceName(entityExampleResourceName);
		sharedEntity.setUserId(huser.getId());

		this.impersonateUser(sharedEntityRestApi, null);
		Response restResponse = sharedEntityRestApi.saveSharedEntity(sharedEntity);
		Assert.assertEquals(403, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	@Test
	public void test004_saveSharedEntityShouldFailIfEntityIsNotShared() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// hadmin tries to save SharedEntity with the following call saveSharedEntity,
		// but entity isn't shared
		// response status code '500' HyperIoTRuntimeException
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
		Assert.assertTrue(adminUser.isAdmin());

		Role role = createRole();
		Assert.assertNotEquals(0, role.getId());

		HUser huser = createHUser(null);

		SharedEntity sharedEntity = new SharedEntity();
		sharedEntity.setEntityId(role.getId());
		sharedEntity.setEntityResourceName("it.acsoftware.hyperiot.role.model.Role");
		sharedEntity.setUserId(huser.getId());

		this.impersonateUser(sharedEntityRestApi, adminUser);
		Response restResponse = sharedEntityRestApi.saveSharedEntity(sharedEntity);
		Assert.assertEquals(500, restResponse.getStatus());
		Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().size());
	}


	@Test
	public void test005_findByPKShouldWork() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// hadmin find SharedEntity by primary key with the following call findByPK
		// response status code '200'
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
		Assert.assertTrue(adminUser.isAdmin());

		SharedEntityExample entityExample = createSharedEntityExample((HUser) adminUser);
		Assert.assertNotEquals(0, entityExample.getId());

		// adminUser share his entity with huser
		HUser huser = createHUser(null);
		SharedEntity sharedEntity = createSharedEntity(entityExample, (HUser) adminUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		this.impersonateUser(sharedEntityRestApi, adminUser);
		Response restResponse = sharedEntityRestApi.findByPK(sharedEntity);
		Assert.assertEquals(200, restResponse.getStatus());
		Assert.assertEquals(sharedEntity.getEntityId(),
				((SharedEntity) restResponse.getEntity()).getEntityId());
		Assert.assertEquals(sharedEntity.getUserId(),
				((SharedEntity) restResponse.getEntity()).getUserId());
		Assert.assertEquals(sharedEntity.getEntityResourceName(),
				((SharedEntity) restResponse.getEntity()).getEntityResourceName());
	}


	@Test
	public void test006_findByPKShouldFailIfNotLogged() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// the following call tries to find SharedEntity by primary key with the following call findByPK,
		// but HUser is not logged
		// response status code '403' HyperIoTUnauthorizedException
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
		Assert.assertTrue(adminUser.isAdmin());

		SharedEntityExample entityExample = createSharedEntityExample((HUser) adminUser);
		Assert.assertNotEquals(0, entityExample.getId());

		HUser huser = createHUser(null);
		SharedEntity sharedEntity = createSharedEntity(entityExample, (HUser) adminUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		this.impersonateUser(sharedEntityRestApi, null);
		Response restResponse = sharedEntityRestApi.findByPK(sharedEntity);
		Assert.assertEquals(403, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	@Test
	public void test007_findByPKShouldFailIfSharedEntityNotFound() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// hadmin tries to find SharedEntity by primary key with the following call findByPK,
		// but SharedEntity isn't stored in database
		// response status code '404' HyperIoTEntityNotFound
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
		SharedEntityExample entityExample = createSharedEntityExample((HUser) adminUser);
		Assert.assertNotEquals(0, entityExample.getId());

		HUser huser = createHUser(null);

		// SharedEntity isn't stored in database
		SharedEntity sharedEntity = new SharedEntity();
		sharedEntity.setEntityId(entityExample.getId());
		sharedEntity.setEntityResourceName(entityExampleResourceName);
		sharedEntity.setUserId(huser.getId());

		this.impersonateUser(sharedEntityRestApi, adminUser);
		Response restResponse = sharedEntityRestApi.findByPK(sharedEntity);
		Assert.assertEquals(404, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	@Test
	public void test008_findByPKShouldFailIfPKNotFound() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// hadmin tries to find SharedEntity by primary key with the following call findByPK,
		// but primary key not found
		// response status code '404' HyperIoTEntityNotFound
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");

		SharedEntityExample entityExample = createSharedEntityExample((HUser) adminUser);
		Assert.assertNotEquals(0, entityExample.getId());

		// adminUser share his hproject with huser
		HUser huser = createHUser(null);
		SharedEntity sharedEntity = createSharedEntity(entityExample, (HUser) adminUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		sharedEntity.setEntityId(0);
		Assert.assertEquals(0, sharedEntity.getEntityId());

		this.impersonateUser(sharedEntityRestApi, adminUser);
		Response restResponse = sharedEntityRestApi.findByPK(sharedEntity);
		Assert.assertEquals(404, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	@Test
	public void test009_findByPKShouldFailIfEntityResourceNameNotFound() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// hadmin tries to find SharedEntity by primary key with the following call findByPK,
		// but entityResourceName not found
		// response status code '404' HyperIoTEntityNotFound
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");

		SharedEntityExample entityExample = createSharedEntityExample((HUser) adminUser);
		Assert.assertNotEquals(0, entityExample.getId());

		// adminUser share his hproject with huser
		HUser huser = createHUser(null);
		SharedEntity sharedEntity = createSharedEntity(entityExample, (HUser) adminUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		sharedEntity.setEntityResourceName("entity.resource.name.not.found");
		Assert.assertEquals("entity.resource.name.not.found", sharedEntity.getEntityResourceName());

		this.impersonateUser(sharedEntityRestApi, adminUser);
		Response restResponse = sharedEntityRestApi.findByPK(sharedEntity);
		Assert.assertEquals(404, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	@Test
	public void test010_findByPKShouldFailIfHUserNotFound() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// hadmin tries to find SharedEntity by primary key with the following call findByPK,
		// but huser not found
		// response status code '404' HyperIoTEntityNotFound
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");

		SharedEntityExample entityExample = createSharedEntityExample((HUser) adminUser);
		Assert.assertNotEquals(0, entityExample.getId());

		// adminUser share his hproject with huser
		HUser huser = createHUser(null);
		SharedEntity sharedEntity = createSharedEntity(entityExample, (HUser) adminUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		sharedEntity.setUserId(0);
		Assert.assertEquals(0, sharedEntity.getUserId());

		this.impersonateUser(sharedEntityRestApi, adminUser);
		Response restResponse = sharedEntityRestApi.findByPK(sharedEntity);
		Assert.assertEquals(404, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	@Test
	public void test011_findAllSharedEntityShouldWork() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// hadmin find all SharedEntity with the following call findAllSharedEntity
		// response status code '200'
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");

		SharedEntityExample entityExample = createSharedEntityExample((HUser) adminUser);
		Assert.assertNotEquals(0, entityExample.getId());

		// adminUser share his entity with huser
		HUser huser = createHUser(null);
		SharedEntity sharedEntity = createSharedEntity(entityExample, (HUser) adminUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		this.impersonateUser(sharedEntityRestApi, adminUser);
		Response restResponse = sharedEntityRestApi.findAllSharedEntity();
		Assert.assertEquals(200, restResponse.getStatus());
		List<SharedEntity> sharedEntityList = restResponse.readEntity(new GenericType<List<SharedEntity>>() {
		});
		Assert.assertFalse(sharedEntityList.isEmpty());
		Assert.assertEquals(1, sharedEntityList.size());
		boolean sharedEntityFound = false;
		for (SharedEntity se : sharedEntityList) {
			if (sharedEntity.getEntityId() == se.getEntityId()) {
				Assert.assertEquals(sharedEntity.getEntityId(), se.getEntityId());
				Assert.assertEquals(sharedEntity.getEntityResourceName(), se.getEntityResourceName());
				Assert.assertEquals(sharedEntity.getUserId(), se.getUserId());
				sharedEntityFound = true;
			}
		}
		Assert.assertTrue(sharedEntityFound);
	}


	@Test
	public void test012_findAllSharedEntityShouldWorkIfSharedEntityListIsEmpty() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// hadmin find all SharedEntity with the following call findAllSharedEntity,
		// there are still no entities saved in the database, this call return an empty list
		// response status code '200'
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");

		this.impersonateUser(sharedEntityRestApi, adminUser);
		Response restResponse = sharedEntityRestApi.findAllSharedEntity();
		Assert.assertEquals(200, restResponse.getStatus());
		List<SharedEntity> sharedEntityList = restResponse.readEntity(new GenericType<List<SharedEntity>>() {
		});
		Assert.assertTrue(sharedEntityList.isEmpty());
		Assert.assertEquals(0, sharedEntityList.size());
	}


	@Test
	public void test013_findAllSharedEntityShouldFailIfNotLogged() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// the following call tries to find all SharedEntity with the following call findAllSharedEntity,
		// but huser is not logged
		// response status code '403' HyperIoTUnauthorizedException
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
		Assert.assertTrue(adminUser.isAdmin());

		SharedEntityExample entityExample = createSharedEntityExample((HUser) adminUser);
		Assert.assertNotEquals(0, entityExample.getId());

		HUser huser = createHUser(null);
		SharedEntity sharedEntity = createSharedEntity(entityExample, (HUser) adminUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		this.impersonateUser(sharedEntityRestApi, null);
		Response restResponse = sharedEntityRestApi.findAllSharedEntity();
		Assert.assertEquals(403, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	@Test
	public void test014_findByEntityShouldWork() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// hadmin find SharedEntity by entity with the following call findByEntity
		// response status code '200'
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");

		SharedEntityExample entityExample = createSharedEntityExample((HUser) adminUser);
		Assert.assertNotEquals(0, entityExample.getId());

		// adminUser share his entity with huser
		HUser huser = createHUser(null);
		SharedEntity sharedEntity = createSharedEntity(entityExample, (HUser) adminUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		this.impersonateUser(sharedEntityRestApi, adminUser);
		Response restResponse = sharedEntityRestApi
				.findByEntity(sharedEntity.getEntityResourceName(), sharedEntity.getEntityId());
		Assert.assertEquals(200, restResponse.getStatus());
		List<SharedEntity> sharedEntityList = restResponse.readEntity(new GenericType<List<SharedEntity>>() {
		});
		Assert.assertFalse(sharedEntityList.isEmpty());
		Assert.assertEquals(1, sharedEntityList.size());
		Assert.assertEquals(sharedEntity.getEntityId(), sharedEntityList.get(0).getEntityId());
		Assert.assertEquals(sharedEntity.getUserId(), sharedEntityList.get(0).getUserId());
		Assert.assertEquals(sharedEntity.getEntityResourceName(), sharedEntityList.get(0).getEntityResourceName());
	}


	@Test
	public void test015_findByEntityShouldFailIfNotLogged() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// the following call tries to find SharedEntity by entity with the following call findByEntity,
		// but HUser is not logged
		// response status code '403' HyperIoTUnauthorizedException
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");

		SharedEntityExample entityExample = createSharedEntityExample((HUser) adminUser);
		Assert.assertNotEquals(0, entityExample.getId());

		// adminUser share his entity with huser
		HUser huser = createHUser(null);
		SharedEntity sharedEntity = createSharedEntity(entityExample, (HUser) adminUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		this.impersonateUser(sharedEntityRestApi, null);
		Response restResponse = sharedEntityRestApi
				.findByEntity(sharedEntity.getEntityResourceName(), sharedEntity.getEntityId());
		Assert.assertEquals(403, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	@Test
	public void test016_findByEntityShouldWorkIfEntityResourceNameIsNull() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// hadmin tries to find SharedEntity by entity with the following call findByEntity,
		// if entityResourceName is null this call return an empty list
		// response status code '200'
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");

		SharedEntityExample entityExample = createSharedEntityExample((HUser) adminUser);
		Assert.assertNotEquals(0, entityExample.getId());

		// adminUser share his entity with huser
		HUser huser = createHUser(null);
		SharedEntity sharedEntity = createSharedEntity(entityExample, (HUser) adminUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		this.impersonateUser(sharedEntityRestApi, adminUser);
		// entityResourceName is null
		Response restResponse = sharedEntityRestApi.findByEntity(null, sharedEntity.getId());
		Assert.assertEquals(200, restResponse.getStatus());
		List<SharedEntity> sharedEntityList = restResponse.readEntity(new GenericType<List<SharedEntity>>() {
		});
		Assert.assertTrue(sharedEntityList.isEmpty());
		Assert.assertEquals(0, sharedEntityList.size());
	}


	@Test
	public void test017_findByEntityShouldWorkIfEntityIdIsZero() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// hadmin tries to find SharedEntity by entity with the following call findByEntity,
		// if entityId is zero this call return an empty list
		// response status code '200'
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");

		SharedEntityExample entityExample = createSharedEntityExample((HUser) adminUser);
		Assert.assertNotEquals(0, entityExample.getId());

		// adminUser share his entity with huser
		HUser huser = createHUser(null);
		SharedEntity sharedEntity = createSharedEntity(entityExample, (HUser) adminUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		this.impersonateUser(sharedEntityRestApi, adminUser);
		Response restResponse = sharedEntityRestApi.findByEntity(sharedEntity.getEntityResourceName(), 0);
		Assert.assertEquals(200, restResponse.getStatus());
		List<SharedEntity> sharedEntityList = restResponse.readEntity(new GenericType<List<SharedEntity>>() {
		});
		Assert.assertTrue(sharedEntityList.isEmpty());
		Assert.assertEquals(0, sharedEntityList.size());
	}


	@Test
	public void test018_findByUserShouldWork() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// hadmin find SharedEntity by huser with the following call findByUser
		// response status code '200'
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");

		SharedEntityExample entityExample = createSharedEntityExample((HUser) adminUser);
		Assert.assertNotEquals(0, entityExample.getId());

		// adminUser share his entity with huser
		HUser huser = createHUser(null);
		SharedEntity sharedEntity = createSharedEntity(entityExample, (HUser) adminUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		this.impersonateUser(sharedEntityRestApi, adminUser);
		Response restResponse = sharedEntityRestApi.findByUser(huser.getId());
		Assert.assertEquals(200, restResponse.getStatus());
		List<SharedEntity> sharedEntityList = restResponse.readEntity(new GenericType<List<SharedEntity>>() {
		});
		Assert.assertFalse(sharedEntityList.isEmpty());
		Assert.assertEquals(1, sharedEntityList.size());
		Assert.assertEquals(sharedEntity.getEntityId(), sharedEntityList.get(0).getEntityId());
		Assert.assertEquals(sharedEntity.getUserId(), sharedEntityList.get(0).getUserId());
		Assert.assertEquals(sharedEntity.getEntityResourceName(), sharedEntityList.get(0).getEntityResourceName());
	}


	@Test
	public void test019_findByUserShouldFailIfNotLogged() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// the following call tries to find SharedEntity by huser with the following call findByUser,
		// but HUser is not logged
		// response status code '403' HyperIoTUnauthorizedException
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");

		SharedEntityExample entityExample = createSharedEntityExample((HUser) adminUser);
		Assert.assertNotEquals(0, entityExample.getId());

		// adminUser share his entity with huser
		HUser huser = createHUser(null);
		SharedEntity sharedEntity = createSharedEntity(entityExample, (HUser) adminUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		this.impersonateUser(sharedEntityRestApi, null);
		Response restResponse = sharedEntityRestApi.findByUser(huser.getId());
		Assert.assertEquals(403, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	@Test
	public void test020_findByUserShouldWorkIfUserNotFound() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// hadmin find SharedEntity by huser with the following call findByUser,
		// if huser not found this call return an empty list
		// response status code '200'
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");

		SharedEntityExample entityExample = createSharedEntityExample((HUser) adminUser);
		Assert.assertNotEquals(0, entityExample.getId());

		// adminUser share his entity with huser
		HUser huser = createHUser(null);
		SharedEntity sharedEntity = createSharedEntity(entityExample, (HUser) adminUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		this.impersonateUser(sharedEntityRestApi, adminUser);
		Response restResponse = sharedEntityRestApi.findByUser(0);
		Assert.assertEquals(200, restResponse.getStatus());
		List<SharedEntity> sharedEntityList = restResponse.readEntity(new GenericType<List<SharedEntity>>() {
		});
		Assert.assertTrue(sharedEntityList.isEmpty());
		Assert.assertEquals(0, sharedEntityList.size());
	}


	@Test
	public void test021_getUsersShouldWork() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// hadmin find users shared by the entity with the following call getUsers
		// response status code '200'
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");

		SharedEntityExample entityExample = createSharedEntityExample((HUser) adminUser);
		Assert.assertNotEquals(0, entityExample.getId());

		// adminUser share his entity with huser
		HUser huser = createHUser(null);
		SharedEntity sharedEntity = createSharedEntity(entityExample, (HUser) adminUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		this.impersonateUser(sharedEntityRestApi, adminUser);
		Response restResponse = sharedEntityRestApi
				.getUsers(sharedEntity.getEntityResourceName(), sharedEntity.getEntityId());
		Assert.assertEquals(200, restResponse.getStatus());
		List<HUser> huserList = restResponse.readEntity(new GenericType<List<HUser>>() {
		});
		Assert.assertFalse(huserList.isEmpty());
		Assert.assertEquals(1, huserList.size());
		Assert.assertEquals(huser.getId(), huserList.get(0).getId());
		Assert.assertEquals(huser.getUsername(), huserList.get(0).getUsername());
		Assert.assertEquals(huser.getEmail(), huserList.get(0).getEmail());
	}


	@Test
	public void test022_getUsersShouldFailIfNotLogged() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// the following call tries to find users shared by the entity with the following call getUsers,
		// but HUser is not logged
		// response status code '403' HyperIoTUnauthorizedException
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");

		SharedEntityExample entityExample = createSharedEntityExample((HUser) adminUser);
		Assert.assertNotEquals(0, entityExample.getId());

		// adminUser share his entity with huser
		HUser huser = createHUser(null);
		SharedEntity sharedEntity = createSharedEntity(entityExample, (HUser) adminUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		this.impersonateUser(sharedEntityRestApi, null);
		Response restResponse = sharedEntityRestApi
				.getUsers(sharedEntity.getEntityResourceName(), sharedEntity.getEntityId());
		Assert.assertEquals(403, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	@Test
	public void test023_getUsersShouldWorkIfUserIsNotStoredInSharedEntity() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// hadmin find users shared by the entity with the following call getUsers,
		// user is not stored in SharedEntity table; this call return an empty list
		// response status code '200'
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");

		SharedEntityExample entityExample = createSharedEntityExample((HUser) adminUser);
		Assert.assertNotEquals(0, entityExample.getId());

		this.impersonateUser(sharedEntityRestApi, adminUser);
		Response restResponse = sharedEntityRestApi
				.getUsers(entityExampleResourceName, entityExample.getId());
		Assert.assertEquals(200, restResponse.getStatus());
		List<HUser> userList = restResponse.readEntity(new GenericType<List<HUser>>() {
		});
		Assert.assertTrue(userList.isEmpty());
		Assert.assertEquals(0, userList.size());
	}


	@Test
	public void test024_deleteSharedEntityShouldWork() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// hadmin delete SharedEntity with the following call deleteSharedEntity
		// response status code '200'
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");

		SharedEntityExample entityExample = createSharedEntityExample((HUser) adminUser);
		Assert.assertNotEquals(0, entityExample.getId());

		// adminUser share his entity with huser
		HUser huser = createHUser(null);
		SharedEntity sharedEntity = createSharedEntity(entityExample, (HUser) adminUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		this.impersonateUser(sharedEntityRestApi, adminUser);
		Response restResponse = sharedEntityRestApi.deleteSharedEntity(sharedEntity);
		Assert.assertEquals(200, restResponse.getStatus());
		Assert.assertNull(restResponse.getEntity());
	}


	@Test
	public void test025_deleteSharedEntityShouldFailIfNotLogged() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// the following call tries to delete SharedEntity with the following call deleteSharedEntity,
		// but huser is not logged
		// response status code '403' HyperIoTUnauthorizedException
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");

		SharedEntityExample entityExample = createSharedEntityExample((HUser) adminUser);
		Assert.assertNotEquals(0, entityExample.getId());

		// adminUser share his entity with huser
		HUser huser = createHUser(null);
		SharedEntity sharedEntity = createSharedEntity(entityExample, (HUser) adminUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		this.impersonateUser(sharedEntityRestApi, null);
		Response restResponse = sharedEntityRestApi.deleteSharedEntity(sharedEntity);
		Assert.assertEquals(403, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	@Test
	public void test026_deleteSharedEntityShouldFailIfSharedEntityNotFound() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// hadmin tries to delete SharedEntity with the following call deleteSharedEntity,
		// but SharedEntity isn't stored in database
		// response status code '404' HyperIoTEntityNotFound
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");

		SharedEntityExample entityExample = createSharedEntityExample((HUser) adminUser);
		Assert.assertNotEquals(0, entityExample.getId());

		// adminUser share his entity with huser
		HUser huser = createHUser(null);

		// SharedEntity isn't stored in database
		SharedEntity sharedEntity = new SharedEntity();
		sharedEntity.setEntityId(entityExample.getId());
		sharedEntity.setEntityResourceName(entityExampleResourceName);
		sharedEntity.setUserId(huser.getId());

		this.impersonateUser(sharedEntityRestApi, adminUser);
		Response restResponse = sharedEntityRestApi.deleteSharedEntity(sharedEntity);
		Assert.assertEquals(404, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	@Test
	public void test027_deleteSharedEntityShouldFailIfPKNotFound() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// hadmin delete SharedEntity with the following call deleteSharedEntity,
		// but primary key not found
		// response status code '404' HyperIoTEntityNotFound
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");

		SharedEntityExample entityExample = createSharedEntityExample((HUser) adminUser);
		Assert.assertNotEquals(0, entityExample.getId());

		// adminUser share his entity with huser
		HUser huser = createHUser(null);
		SharedEntity sharedEntity = createSharedEntity(entityExample, (HUser) adminUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		sharedEntity.setEntityId(0);
		Assert.assertEquals(0, sharedEntity.getEntityId());

		this.impersonateUser(sharedEntityRestApi, adminUser);
		Response restResponse = sharedEntityRestApi.deleteSharedEntity(sharedEntity);
		Assert.assertEquals(404, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	@Test
	public void test028_deleteSharedEntityShouldFailIfEntityResourceNameNotFound() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// hadmin delete SharedEntity with the following call deleteSharedEntity,
		// but entityResourceName not found
		// response status code '500' HyperIoTRuntimeException
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");

		SharedEntityExample entityExample = createSharedEntityExample((HUser) adminUser);
		Assert.assertNotEquals(0, entityExample.getId());

		// adminUser share his entity with huser
		HUser huser = createHUser(null);
		SharedEntity sharedEntity = createSharedEntity(entityExample, (HUser) adminUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		sharedEntity.setEntityResourceName("entity.resource.name.not.found");
		Assert.assertEquals("entity.resource.name.not.found", sharedEntity.getEntityResourceName());

		this.impersonateUser(sharedEntityRestApi, adminUser);
		Response restResponse = sharedEntityRestApi.deleteSharedEntity(sharedEntity);
		Assert.assertEquals(500, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTRuntimeException",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
		Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().size());
		Assert.assertEquals("Entity " + sharedEntity.getEntityResourceName() + " is not a HyperIoTSharedEntity",
				((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(0));
	}


	@Test
	public void test029_deleteSharedEntityShouldFailIfUserNotFound() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// hadmin delete SharedEntity with the following call deleteSharedEntity,
		// but user not found
		// response status code '404' HyperIoTEntityNotFound
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");

		SharedEntityExample entityExample = createSharedEntityExample((HUser) adminUser);
		Assert.assertNotEquals(0, entityExample.getId());

		// adminUser share his entity with huser
		HUser huser = createHUser(null);
		SharedEntity sharedEntity = createSharedEntity(entityExample, (HUser) adminUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		sharedEntity.setUserId(0);
		Assert.assertEquals(0, sharedEntity.getUserId());

		this.impersonateUser(sharedEntityRestApi, adminUser);
		Response restResponse = sharedEntityRestApi.deleteSharedEntity(sharedEntity);
		Assert.assertEquals(404, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	@Test
	public void test030_deleteSharedEntityShouldFailIfSharedEntityIsNull() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// hadmin delete SharedEntity with the following call deleteSharedEntity,
		// but shared entity is null
		// response status code '500' java.lang.NullPointerException
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");

		this.impersonateUser(sharedEntityRestApi, adminUser);
		Response restResponse = sharedEntityRestApi.deleteSharedEntity(null);
		Assert.assertEquals(500, restResponse.getStatus());
		Assert.assertEquals("java.lang.NullPointerException",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	@Test
	public void test031_deleteSharedEntityShouldFailIfUserIsNotUserOwnerOfEntity() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// huser tries to remove SharedEntity with the following call deleteSharedEntity,
		// but the huser isn't the user owner of the entity
		// response status code '403' HyperIoTUnauthorizedException
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");

		SharedEntityExample entityExample = createSharedEntityExample((HUser) adminUser);
		Assert.assertNotEquals(0, entityExample.getId());

		// adminUser share his entity with huser
		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(entityExampleResourceName,
				HyperIoTShareAction.SHARE);
		HUser huser = createHUser(action);

		SharedEntity sharedEntity = createSharedEntity(entityExample, (HUser) adminUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		this.impersonateUser(sharedEntityRestApi, huser);
		Response restResponse = sharedEntityRestApi.deleteSharedEntity(sharedEntity);
		Assert.assertEquals(403, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	@Test
	public void test032_saveSharedEntityShouldFailIfHUserIdIsZero() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// hadmin tries to save SharedEntity with the following call saveSharedEntity,
		// but huser id is zero: isn't stored in database
		// response status code '404' HyperIoTEntityNotFound
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");

		SharedEntityExample entityExample = createSharedEntityExample((HUser) adminUser);
		Assert.assertNotEquals(0, entityExample.getId());

		//huser isn't stored in database: huser id is zero
		HUser huser = new HUser();
		SharedEntity sharedEntity = new SharedEntity();
		sharedEntity.setEntityId(entityExample.getId());
		sharedEntity.setEntityResourceName(entityExampleResourceName);
		sharedEntity.setUserId(huser.getId()); // huser: "ID: 0 \n User:null"

		this.impersonateUser(sharedEntityRestApi, adminUser);
		Response restResponse = sharedEntityRestApi.saveSharedEntity(sharedEntity);
		Assert.assertEquals(404, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	@Test
	public void test033_saveSharedEntityShouldFailIfEntityResourceNameIsNull() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// hadmin tries to save SharedEntity with the following call saveSharedEntity,
		// but entityResourceName is null
		// response status code '422' HyperIoTValidationException
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");

		SharedEntityExample entityExample = createSharedEntityExample((HUser) adminUser);
		Assert.assertNotEquals(0, entityExample.getId());

		// adminUser share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = new SharedEntity();
		sharedEntity.setEntityId(entityExample.getId());
		sharedEntity.setEntityResourceName(null);
		sharedEntity.setUserId(huser.getId());

		this.impersonateUser(sharedEntityRestApi, adminUser);
		Response restResponse = sharedEntityRestApi.saveSharedEntity(sharedEntity);
		Assert.assertEquals(403, restResponse.getStatus());
	}


	@Test
	public void test034_saveSharedEntityShouldFailIfEntityResourceNameIsEmpty() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// hadmin tries to save SharedEntity with the following call saveSharedEntity,
		// but entityResourceName is empty
		// response status code '500' HyperIoTRuntimeException
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");

		SharedEntityExample entityExample = createSharedEntityExample((HUser) adminUser);
		Assert.assertNotEquals(0, entityExample.getId());

		// adminUser share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = new SharedEntity();
		sharedEntity.setEntityId(entityExample.getId());
		sharedEntity.setEntityResourceName("");
		sharedEntity.setUserId(huser.getId());

		this.impersonateUser(sharedEntityRestApi, adminUser);
		Response restResponse = sharedEntityRestApi.saveSharedEntity(sharedEntity);
		Assert.assertEquals(500, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTRuntimeException",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
		Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().size());
		Assert.assertEquals("Entity " + sharedEntity.getEntityResourceName() + " is not a HyperIoTSharedEntity",
				((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(0));
	}


	@Test
	public void test035_saveSharedEntityShouldFailIfEntityResourceNameIsMaliciousCode() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// hadmin tries to save SharedEntity with the following call saveSharedEntity,
		// but entityResourceName is malicious code
		// response status code '500' HyperIoTRuntimeException
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");

		SharedEntityExample entityExample = createSharedEntityExample((HUser) adminUser);
		Assert.assertNotEquals(0, entityExample.getId());

		// adminUser share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = new SharedEntity();
		sharedEntity.setEntityId(entityExample.getId());
		sharedEntity.setEntityResourceName("javascript:");
		sharedEntity.setUserId(huser.getId());

		this.impersonateUser(sharedEntityRestApi, adminUser);
		Response restResponse = sharedEntityRestApi.saveSharedEntity(sharedEntity);
		Assert.assertEquals(500, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTRuntimeException",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
		Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().size());
		Assert.assertEquals("Entity " + sharedEntity.getEntityResourceName() + " is not a HyperIoTSharedEntity",
				((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(0));
	}


	@Test
	public void test036_saveSharedEntityShouldFailIfEntityIdIsZero() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// hadmin tries to save SharedEntity with the following call saveSharedEntity,
		// but entityId is zero
		// response status code '404' HyperIoTEntityNotFound
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");

		SharedEntityExample entityExample = createSharedEntityExample((HUser) adminUser);
		Assert.assertNotEquals(0, entityExample.getId());

		// adminUser share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = new SharedEntity();
		sharedEntity.setEntityId(0);
		sharedEntity.setEntityResourceName(entityExampleResourceName);
		sharedEntity.setUserId(huser.getId());

		this.impersonateUser(sharedEntityRestApi, adminUser);
		Response restResponse = sharedEntityRestApi.saveSharedEntity(sharedEntity);
		Assert.assertEquals(404, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	@Test
	public void test037_saveSharedEntityShouldFailIfEntityIsNotStoredInDatabase() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// hadmin tries to save SharedEntity with the following call saveSharedEntity,
		// but entity isn't stored in database
		// response status code '404' HyperIoTEntityNotFound
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");

		// SharedEntityExample isn't stored in database
		SharedEntityExample entityExample = new SharedEntityExample();

		// adminUser share his hproject with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = new SharedEntity();
		sharedEntity.setEntityId(entityExample.getId());
		sharedEntity.setEntityResourceName(entityExampleResourceName);
		sharedEntity.setUserId(huser.getId());

		this.impersonateUser(sharedEntityRestApi, adminUser);
		Response restResponse = sharedEntityRestApi.saveSharedEntity(sharedEntity);
		Assert.assertEquals(404, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	@Test
	public void test038_saveSharedEntityShouldFailIfUserIsNotUserOwnerOfEntity() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// huser tries to save SharedEntity with the following call saveSharedEntity,
		// but the huser isn't the user owner of the entity
		// response status code '403' HyperIoTUnauthorizedException
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");

		SharedEntityExample entityExample = createSharedEntityExample((HUser) adminUser);
		Assert.assertNotEquals(0, entityExample.getId());

		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(entityExampleResourceName,
				HyperIoTShareAction.SHARE);
		HUser huser = createHUser(action);

		SharedEntity sharedEntity = new SharedEntity();
		sharedEntity.setEntityId(entityExample.getId());
		sharedEntity.setEntityResourceName(entityExampleResourceName);
		sharedEntity.setUserId(huser.getId());

		this.impersonateUser(sharedEntityRestApi, huser);
		Response restResponse = sharedEntityRestApi.saveSharedEntity(sharedEntity);
		Assert.assertEquals(403, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	@Test
	public void test039_saveSharedEntityShouldFailIfEntityIsDuplicated() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// hadmin tries to save SharedEntity with the following call saveSharedEntity,
		// but SharedEntity is duplicated
		// response status code '422' HyperIoTDuplicateEntityException
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");

		SharedEntityExample entityExample = createSharedEntityExample((HUser) adminUser);
		Assert.assertNotEquals(0, entityExample.getId());

		// adminUser share his hproject with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = createSharedEntity(entityExample, (HUser) adminUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		SharedEntity sharedEntityDuplicated = new SharedEntity();
		sharedEntityDuplicated.setEntityId(sharedEntity.getEntityId());
		sharedEntityDuplicated.setEntityResourceName(sharedEntity.getEntityResourceName());
		sharedEntityDuplicated.setUserId(sharedEntity.getUserId());

		Assert.assertEquals(sharedEntity.getEntityId(), sharedEntityDuplicated.getEntityId());
		Assert.assertEquals(sharedEntity.getUserId(), sharedEntityDuplicated.getUserId());
		Assert.assertEquals(sharedEntity.getEntityResourceName(), sharedEntityDuplicated.getEntityResourceName());

		this.impersonateUser(sharedEntityRestApi, adminUser);
		Response restResponse = sharedEntityRestApi.saveSharedEntity(sharedEntityDuplicated);
		Assert.assertEquals(409, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTDuplicateEntityException",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
		Assert.assertEquals(3, ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().size());
		boolean entityResourceNameIsDuplicated = false;
		boolean entityIdIsDuplicated = false;
		boolean userIdIsDuplicated = false;
		for (int i = 0; i < ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().size(); i++) {
			if (((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(i).contentEquals("entityResourceName")) {
				Assert.assertEquals("entityResourceName", ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(i));
				entityResourceNameIsDuplicated = true;
			}
			if (((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(i).contentEquals("entityId")) {
				Assert.assertEquals("entityId", ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(i));
				entityIdIsDuplicated = true;
			}
			if (((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(i).contentEquals("userId")) {
				Assert.assertEquals("userId", ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(i));
				userIdIsDuplicated = true;
			}
		}
		Assert.assertTrue(entityResourceNameIsDuplicated);
		Assert.assertTrue(entityIdIsDuplicated);
		Assert.assertTrue(userIdIsDuplicated);
	}


	@Test
	public void test040_findAllSharedEntityPaginatedShouldWork() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// In this following call findAllSharedEntityPaginated, hadmin find all SharedEntity with pagination
		// response status code '200'
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
		int delta = 5;
		int page = 1;

		SharedEntityExample entityExample = createSharedEntityExample((HUser) adminUser);
		Assert.assertNotEquals(0, entityExample.getId());

		// adminUser share his entities with husers
		List<HUser> husers = new ArrayList<>();
		for (int i = 0; i < delta; i++) {
			HUser huser = createHUser(null);
			Assert.assertNotEquals(0, huser.getId());
			husers.add(huser);
		}
		Assert.assertEquals(delta, husers.size());

		List<SharedEntity> sharedEntities = new ArrayList<>();
		for (int i = 0; i < delta; i++) {
			SharedEntity sharedEntity = createSharedEntity(entityExample, (HUser) adminUser, husers.get(i));
			Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
			Assert.assertEquals(husers.get(i).getId(), sharedEntity.getUserId());
			Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());
			sharedEntities.add(sharedEntity);
		}
		Assert.assertEquals(delta, sharedEntities.size());
		this.impersonateUser(sharedEntityRestApi, adminUser);
		Response restResponse = sharedEntityRestApi.findAllSharedEntityPaginated(delta, page);
		HyperIoTPaginableResult<SharedEntity> listSharedEntities = restResponse
				.readEntity(new GenericType<HyperIoTPaginableResult<SharedEntity>>() {
				});
		Assert.assertFalse(listSharedEntities.getResults().isEmpty());
		Assert.assertEquals(delta, listSharedEntities.getResults().size());
		Assert.assertEquals(delta, listSharedEntities.getDelta());
		Assert.assertEquals(page, listSharedEntities.getCurrentPage());
		Assert.assertEquals(page, listSharedEntities.getNextPage());
		// delta is 5, page 1: 5 entities stored in database
		Assert.assertEquals(1, listSharedEntities.getNumPages());
		Assert.assertEquals(200, restResponse.getStatus());
	}


	@Test
	public void test041_findAllSharedEntityPaginatedShouldWorkIfDeltaAndPageAreNull() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// In this following call findAllSharedEntityPaginated, hadmin find all SharedEntity with pagination
		// if delta and page are null
		// response status code '200'
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
		Integer delta = null;
		Integer page = null;

		SharedEntityExample entityExample = createSharedEntityExample((HUser) adminUser);
		Assert.assertNotEquals(0, entityExample.getId());

		// adminUser share his entities with husers
		List<HUser> husers = new ArrayList<>();
		int numbEntities = 8;
		for (int i = 0; i < numbEntities; i++) {
			HUser huser = createHUser(null);
			Assert.assertNotEquals(0, huser.getId());
			husers.add(huser);
		}
		Assert.assertEquals(numbEntities, husers.size());

		List<SharedEntity> sharedEntities = new ArrayList<>();
		for (int i = 0; i < numbEntities; i++) {
			SharedEntity sharedEntity = createSharedEntity(entityExample, (HUser) adminUser, husers.get(i));
			Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
			Assert.assertEquals(husers.get(i).getId(), sharedEntity.getUserId());
			Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());
			sharedEntities.add(sharedEntity);
		}
		Assert.assertEquals(numbEntities, sharedEntities.size());

		this.impersonateUser(sharedEntityRestApi, adminUser);
		Response restResponse = sharedEntityRestApi.findAllSharedEntityPaginated(delta, page);
		HyperIoTPaginableResult<SharedEntity> listSharedEntities = restResponse
				.readEntity(new GenericType<HyperIoTPaginableResult<SharedEntity>>() {
				});
		Assert.assertFalse(listSharedEntities.getResults().isEmpty());
		Assert.assertEquals(numbEntities, listSharedEntities.getResults().size());
		Assert.assertEquals(defaultDelta, listSharedEntities.getDelta());
		Assert.assertEquals(defaultPage, listSharedEntities.getCurrentPage());
		Assert.assertEquals(defaultPage, listSharedEntities.getNextPage());
		// default delta is 10, default page is 1: 8 entities stored in database
		Assert.assertEquals(1, listSharedEntities.getNumPages());
		Assert.assertEquals(200, restResponse.getStatus());
	}


	@Test
	public void test042_findAllSharedEntityPaginatedShouldWorkIfDeltaIsLowerThanZero() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// In this following call findAllSharedEntityPaginated, hadmin find all SharedEntity with pagination
		// if delta is lower than zero
		// response status code '200'
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
		int delta = -1;
		int page = 2;

		SharedEntityExample entityExample = createSharedEntityExample((HUser) adminUser);
		Assert.assertNotEquals(0, entityExample.getId());

		// adminUser share his entities with husers
		List<HUser> husers = new ArrayList<>();
		int numbEntities = 12;
		for (int i = 0; i < numbEntities; i++) {
			HUser huser = createHUser(null);
			Assert.assertNotEquals(0, huser.getId());
			husers.add(huser);
		}
		Assert.assertEquals(numbEntities, husers.size());
		List<SharedEntity> sharedEntities = new ArrayList<>();
		for (int i = 0; i < numbEntities; i++) {
			SharedEntity sharedEntity = createSharedEntity(entityExample, (HUser) adminUser, husers.get(i));
			Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
			Assert.assertEquals(husers.get(i).getId(), sharedEntity.getUserId());
			Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());
			sharedEntities.add(sharedEntity);
		}
		Assert.assertEquals(numbEntities, sharedEntities.size());
		this.impersonateUser(sharedEntityRestApi, adminUser);
		Response restResponse = sharedEntityRestApi.findAllSharedEntityPaginated(delta, page);
		HyperIoTPaginableResult<SharedEntity> listSharedEntities = restResponse
				.readEntity(new GenericType<HyperIoTPaginableResult<SharedEntity>>() {
				});
		Assert.assertFalse(listSharedEntities.getResults().isEmpty());
		Assert.assertEquals(numbEntities - defaultDelta, listSharedEntities.getResults().size());
		Assert.assertEquals(defaultDelta, listSharedEntities.getDelta());
		Assert.assertEquals(page, listSharedEntities.getCurrentPage());
		Assert.assertEquals(defaultPage, listSharedEntities.getNextPage());
		// default delta is 10, page is 2: 12 entities stored in database
		Assert.assertEquals(2, listSharedEntities.getNumPages());
		Assert.assertEquals(200, restResponse.getStatus());

		//checks with page = 1
		this.impersonateUser(sharedEntityRestApi, adminUser);
		Response restResponsePage1 = sharedEntityRestApi.findAllSharedEntityPaginated(delta, 1);
		HyperIoTPaginableResult<SharedEntity> listSharedEntitiesPage1 = restResponsePage1
				.readEntity(new GenericType<HyperIoTPaginableResult<SharedEntity>>() {
				});
		Assert.assertFalse(listSharedEntitiesPage1.getResults().isEmpty());
		Assert.assertEquals(defaultDelta, listSharedEntitiesPage1.getResults().size());
		Assert.assertEquals(defaultDelta, listSharedEntitiesPage1.getDelta());
		Assert.assertEquals(defaultPage, listSharedEntitiesPage1.getCurrentPage());
		Assert.assertEquals(page, listSharedEntitiesPage1.getNextPage());
		// default delta is 10, page is 1: 12 entities stored in database
		Assert.assertEquals(2, listSharedEntitiesPage1.getNumPages());
		Assert.assertEquals(200, restResponsePage1.getStatus());
	}


	@Test
	public void test043_findAllSharedEntityPaginatedShouldWorkIfDeltaIsZero() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// In this following call findAllSharedEntityPaginated, hadmin find all SharedEntity with pagination
		// if delta is zero
		// response status code '200'
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
		int delta = 0;
		int page = 3;

		SharedEntityExample entityExample = createSharedEntityExample((HUser) adminUser);
		Assert.assertNotEquals(0, entityExample.getId());

		// adminUser share his entities with husers
		List<HUser> husers = new ArrayList<>();
		int numbEntities = 22;
		for (int i = 0; i < numbEntities; i++) {
			HUser huser = createHUser(null);
			Assert.assertNotEquals(0, huser.getId());
			husers.add(huser);
		}
		Assert.assertEquals(numbEntities, husers.size());
		List<SharedEntity> sharedEntities = new ArrayList<>();
		for (int i = 0; i < numbEntities; i++) {
			SharedEntity sharedEntity = createSharedEntity(entityExample, (HUser) adminUser, husers.get(i));
			Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
			Assert.assertEquals(husers.get(i).getId(), sharedEntity.getUserId());
			Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());
			sharedEntities.add(sharedEntity);
		}
		Assert.assertEquals(numbEntities, sharedEntities.size());
		this.impersonateUser(sharedEntityRestApi, adminUser);
		Response restResponse = sharedEntityRestApi.findAllSharedEntityPaginated(delta, page);
		HyperIoTPaginableResult<SharedEntity> listSharedEntities = restResponse
				.readEntity(new GenericType<HyperIoTPaginableResult<SharedEntity>>() {
				});
		Assert.assertFalse(listSharedEntities.getResults().isEmpty());
		Assert.assertEquals(numbEntities - (defaultDelta * 2), listSharedEntities.getResults().size());
		Assert.assertEquals(defaultDelta, listSharedEntities.getDelta());
		Assert.assertEquals(page, listSharedEntities.getCurrentPage());
		Assert.assertEquals(defaultPage, listSharedEntities.getNextPage());
		// default delta is 10, page is 3: 22 entities stored in database
		Assert.assertEquals(3, listSharedEntities.getNumPages());
		Assert.assertEquals(200, restResponse.getStatus());

		//checks with page = 1
		this.impersonateUser(sharedEntityRestApi, adminUser);
		Response restResponsePage1 = sharedEntityRestApi.findAllSharedEntityPaginated(delta, 1);
		HyperIoTPaginableResult<SharedEntity> listSharedEntitiesPage1 = restResponsePage1
				.readEntity(new GenericType<HyperIoTPaginableResult<SharedEntity>>() {
				});
		Assert.assertFalse(listSharedEntitiesPage1.getResults().isEmpty());
		Assert.assertEquals(defaultDelta, listSharedEntitiesPage1.getResults().size());
		Assert.assertEquals(defaultDelta, listSharedEntitiesPage1.getDelta());
		Assert.assertEquals(defaultPage, listSharedEntitiesPage1.getCurrentPage());
		Assert.assertEquals(defaultPage + 1, listSharedEntitiesPage1.getNextPage());
		// default delta is 10, page is 1: 22 entities stored in database
		Assert.assertEquals(3, listSharedEntitiesPage1.getNumPages());
		Assert.assertEquals(200, restResponsePage1.getStatus());

		//checks with page = 2
		this.impersonateUser(sharedEntityRestApi, adminUser);
		Response restResponsePage2 = sharedEntityRestApi.findAllSharedEntityPaginated(delta, 2);
		HyperIoTPaginableResult<SharedEntity> listSharedEntitiesPage2 = restResponsePage2
				.readEntity(new GenericType<HyperIoTPaginableResult<SharedEntity>>() {
				});
		Assert.assertFalse(listSharedEntitiesPage2.getResults().isEmpty());
		Assert.assertEquals(defaultDelta, listSharedEntitiesPage2.getResults().size());
		Assert.assertEquals(defaultDelta, listSharedEntitiesPage2.getDelta());
		Assert.assertEquals(defaultPage + 1, listSharedEntitiesPage2.getCurrentPage());
		Assert.assertEquals(page, listSharedEntitiesPage2.getNextPage());
		// default delta is 10, page is 2: 22 entities stored in database
		Assert.assertEquals(3, listSharedEntitiesPage2.getNumPages());
		Assert.assertEquals(200, restResponsePage2.getStatus());
	}


	@Test
	public void test044_findAllSharedEntityPaginatedShouldWorkIfPageIsLowerThanZero() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// In this following call findAllSharedEntityPaginated, hadmin find all SharedEntity with pagination
		// if page is lower than zero
		// response status code '200'
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
		int delta = 5;
		int page = -1;

		SharedEntityExample entityExample = createSharedEntityExample((HUser) adminUser);
		Assert.assertNotEquals(0, entityExample.getId());

		// adminUser share his entities with husers
		List<HUser> husers = new ArrayList<>();
		for (int i = 0; i < defaultDelta; i++) {
			HUser huser = createHUser(null);
			Assert.assertNotEquals(0, huser.getId());
			husers.add(huser);
		}
		Assert.assertEquals(defaultDelta, husers.size());
		List<SharedEntity> sharedEntities = new ArrayList<>();
		for (int i = 0; i < defaultDelta; i++) {
			SharedEntity sharedEntity = createSharedEntity(entityExample, (HUser) adminUser, husers.get(i));
			Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
			Assert.assertEquals(husers.get(i).getId(), sharedEntity.getUserId());
			Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());
			sharedEntities.add(sharedEntity);
		}
		Assert.assertEquals(defaultDelta, sharedEntities.size());
		this.impersonateUser(sharedEntityRestApi, adminUser);
		Response restResponse = sharedEntityRestApi.findAllSharedEntityPaginated(delta, page);
		HyperIoTPaginableResult<SharedEntity> listSharedEntities = restResponse
				.readEntity(new GenericType<HyperIoTPaginableResult<SharedEntity>>() {
				});
		Assert.assertFalse(listSharedEntities.getResults().isEmpty());
		Assert.assertEquals(delta, listSharedEntities.getResults().size());
		Assert.assertEquals(delta, listSharedEntities.getDelta());
		Assert.assertEquals(defaultPage, listSharedEntities.getCurrentPage());
		Assert.assertEquals(defaultPage + 1, listSharedEntities.getNextPage());
		// delta is 5, page 1: 10 entities stored in database
		Assert.assertEquals(2, listSharedEntities.getNumPages());
		Assert.assertEquals(200, restResponse.getStatus());
	}


	@Test
	public void test045_findAllSharedEntityPaginatedShouldWorkIfPageIsZero() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// In this following call findAllSharedEntityPaginated, hadmin find all SharedEntity with pagination
		// if page is zero
		// response status code '200'
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
		int delta = 5;
		int page = 0;

		SharedEntityExample entityExample = createSharedEntityExample((HUser) adminUser);
		Assert.assertNotEquals(0, entityExample.getId());

		// adminUser share his entities with husers
		List<HUser> husers = new ArrayList<>();
		for (int i = 0; i < delta; i++) {
			HUser huser = createHUser(null);
			Assert.assertNotEquals(0, huser.getId());
			husers.add(huser);
		}
		Assert.assertEquals(delta, husers.size());
		List<SharedEntity> sharedEntities = new ArrayList<>();
		for (int i = 0; i < delta; i++) {
			SharedEntity sharedEntity = createSharedEntity(entityExample, (HUser) adminUser, husers.get(i));
			Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
			Assert.assertEquals(husers.get(i).getId(), sharedEntity.getUserId());
			Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());
			sharedEntities.add(sharedEntity);
		}
		Assert.assertEquals(delta, sharedEntities.size());
		this.impersonateUser(sharedEntityRestApi, adminUser);
		Response restResponse = sharedEntityRestApi.findAllSharedEntityPaginated(delta, page);
		HyperIoTPaginableResult<SharedEntity> listSharedEntities = restResponse
				.readEntity(new GenericType<HyperIoTPaginableResult<SharedEntity>>() {
				});
		Assert.assertFalse(listSharedEntities.getResults().isEmpty());
		Assert.assertEquals(delta, listSharedEntities.getResults().size());
		Assert.assertEquals(delta, listSharedEntities.getDelta());
		Assert.assertEquals(defaultPage, listSharedEntities.getCurrentPage());
		Assert.assertEquals(defaultPage, listSharedEntities.getNextPage());
		// delta is 5, default page is 1: 5 entities stored in database
		Assert.assertEquals(1, listSharedEntities.getNumPages());
		Assert.assertEquals(200, restResponse.getStatus());
	}


	@Test
	public void test046_findAllSharedEntityPaginatedShouldFailIfNotLogged() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// the following call tries to find all SharedEntity with pagination,
		// but huser is not logged
		// response status code '403' HyperIoTUnauthorizedException
		this.impersonateUser(sharedEntityRestApi, null);
		Response restResponse = sharedEntityRestApi.findAllSharedEntityPaginated(defaultDelta, defaultPage);
		Assert.assertEquals(403, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	@Test
	public void test047_deleteEntityExampleAndRemoveInCascadeSharedEntity() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// hadmin delete entityExample with the following call deleteSharedEntityExample;
		// this call delete in cascade mode SharedEntity because entityId is equals to entityExampleId
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");

		SharedEntityExample entityExample = createSharedEntityExample((HUser) adminUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(adminUser.getId(), entityExample.getUser().getId());

		// adminUser share his entity with huser
		HUser huser = createHUser(null);
		SharedEntity sharedEntity = createSharedEntity(entityExample, (HUser) adminUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		// this call delete in cascade mode record in SharedEntity table
		SharedEntityExampleRestApi entityRestApi = getOsgiService(SharedEntityExampleRestApi.class);
		this.impersonateUser(entityRestApi, adminUser);
		Assert.assertEquals(sharedEntity.getEntityId(), entityExample.getId());
		Response responseDeleteEntity = entityRestApi.deleteSharedEntityExample(sharedEntity.getEntityId());
		Assert.assertEquals(200, responseDeleteEntity.getStatus());
		Assert.assertNull(responseDeleteEntity.getEntity());

		// checks: HUser is stored in database
		HUserRestApi hUserRestApi = getOsgiService(HUserRestApi.class);
		this.impersonateUser(hUserRestApi, adminUser);
		Assert.assertEquals(sharedEntity.getUserId(), huser.getId());
		Response responseFindHUser = hUserRestApi.findHUser(sharedEntity.getUserId());
		Assert.assertEquals(200, responseFindHUser.getStatus());
		Assert.assertEquals(huser.getId(), ((HUser) responseFindHUser.getEntity()).getId());

		// checks: SharedEntity isn't stored in database
		this.impersonateUser(sharedEntityRestApi, adminUser);
		Response restResponse = sharedEntityRestApi.findByPK(sharedEntity);
		Assert.assertEquals(404, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	@Test
	public void test048_deleteHUserRemoveRecordInsideSharedEntityTable() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// hadmin delete huser with call deleteHUser; this call remove in cascade record inside SharedEntity table
		// response status code '200'
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");

		SharedEntityExample entityExample = createSharedEntityExample((HUser) adminUser);
		Assert.assertNotEquals(0, entityExample.getId());

		// adminUser share his entity with huser
		HUser huser = createHUser(null);
		SharedEntity sharedEntity = createSharedEntity(entityExample, (HUser) adminUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		// huser has been removed in SharedEntity table with deleteHUser call
		HUserRestApi hUserRestApi = getOsgiService(HUserRestApi.class);
		this.impersonateUser(hUserRestApi, adminUser);
		Response responseDeleteHUser = hUserRestApi.deleteHUser(huser.getId());
		Assert.assertEquals(200, responseDeleteHUser.getStatus());
		Assert.assertNull(responseDeleteHUser.getEntity());

		// checks: record of SharedEntity has been deleted in database
		this.impersonateUser(sharedEntityRestApi, adminUser);
		Response restResponse = sharedEntityRestApi.findByPK(sharedEntity);
		Assert.assertEquals(404, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	@Test
	public void test049_deleteSharedEntityNotDeleteInCascadeEntityExampleAndHUser() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// hadmin delete SharedEntity with the following call deleteSharedEntity;
		// this call not delete in cascade mode entity example or huser
		// response status code '200'
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");

		SharedEntityExample entityExample = createSharedEntityExample((HUser) adminUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(adminUser.getId(), entityExample.getUser().getId());

		// adminUser share his entity with huser
		HUser huser = createHUser(null);
		SharedEntity sharedEntity = createSharedEntity(entityExample, (HUser) adminUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		// this call (deleteSharedEntity) removes record in SharedEntity table
		this.impersonateUser(sharedEntityRestApi, adminUser);
		Response restResponse = sharedEntityRestApi.deleteSharedEntity(sharedEntity);
		Assert.assertEquals(200, restResponse.getStatus());
		Assert.assertNull(restResponse.getEntity());

		// checks: HUser is already stored in database
		HUserRestApi hUserRestApi = getOsgiService(HUserRestApi.class);
		this.impersonateUser(hUserRestApi, adminUser);
		Response responseFindHUser = hUserRestApi.findHUser(huser.getId());
		Assert.assertEquals(200, responseFindHUser.getStatus());
		Assert.assertEquals(huser.getId(), ((HUser) responseFindHUser.getEntity()).getId());

		// checks: SharedEntityExample is already stored in database
		SharedEntityExampleRestApi entityExampleRestApi = getOsgiService(SharedEntityExampleRestApi.class);
		this.impersonateUser(entityExampleRestApi, adminUser);
		Response responseFindEntityExample = entityExampleRestApi.findSharedEntityExample(sharedEntity.getEntityId());
		Assert.assertEquals(200, responseFindEntityExample.getStatus());
		Assert.assertEquals(entityExample.getId(), ((SharedEntityExample) responseFindEntityExample.getEntity()).getId());
	}


	@Test
	public void test050_huserFindEntityExampleSharedAfterSharedOperationShouldWork() {
		// hadmin save SharedEntity with the following call saveSharedEntity, and
		// huser find Entity Example after shared entity operation
		// response status code '200'
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");

		SharedEntityExample entityExample = createSharedEntityExample((HUser) adminUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(adminUser.getId(), entityExample.getUser().getId());

		// adminUser share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = createSharedEntity(entityExample, (HUser) adminUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		SharedEntityExampleRestApi entityExampleRestApi = getOsgiService(SharedEntityExampleRestApi.class);
		// huser find Entity Example after shared operation
		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(entityExampleResourceName,
				HyperIoTCrudAction.FIND);
		addPermission(huser, action, 0);
		// add specific permission with resourceId
		addPermission(huser, action, entityExample.getId());
		this.impersonateUser(entityExampleRestApi, huser);
		Response responseFindEntityExample = entityExampleRestApi.findSharedEntityExample(entityExample.getId());
		Assert.assertEquals(200, responseFindEntityExample.getStatus());
		Assert.assertEquals(entityExample.getId(), ((SharedEntityExample) responseFindEntityExample.getEntity()).getId());
		Assert.assertEquals(adminUser.getId(), ((SharedEntityExample) responseFindEntityExample.getEntity()).getUser().getId());
	}


	@Test
	public void test051_huserWithoutPermissionTriesToFindEntityExampleSharedAfterSharedOperationShouldFail() {
		// hadmin save SharedEntity with the following call saveSharedEntity.
		// huser, without permission, tries to find Entity Example after shared entity operation
		// response status code '403' HyperIoTUnauthorizedException
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");

		SharedEntityExample entityExample = createSharedEntityExample((HUser) adminUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(adminUser.getId(), entityExample.getUser().getId());

		// adminUser share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = createSharedEntity(entityExample, (HUser) adminUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		SharedEntityExampleRestApi entityExampleRestApi = getOsgiService(SharedEntityExampleRestApi.class);
		this.impersonateUser(entityExampleRestApi, huser);
		Response restResponse = entityExampleRestApi.findSharedEntityExample(entityExample.getId());
		Assert.assertEquals(403, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	@Test
	public void test052_huserWithoutSpecificPermissionTriesToFindEntityExampleSharedAfterSharedOperationShouldFail() {
		// hadmin save SharedEntity with the following call saveSharedEntity.
		// huser, without specific permission, tries to find Entity Example after shared entity operation
		// response status code '403' HyperIoTUnauthorizedException
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");

		SharedEntityExample entityExample = createSharedEntityExample((HUser) adminUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(adminUser.getId(), entityExample.getUser().getId());

		// adminUser share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = createSharedEntity(entityExample, (HUser) adminUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		//Create a specific role and after that add the permission to this specific role
		Role specificRole = createRole();
		//Add specific permission to SharedEntityExample saved before
		PermissionSystemApi permissionSystemService = getOsgiService(PermissionSystemApi.class);
		List<HyperIoTAction> sharedEntityAction = HyperIoTActionsUtil.getHyperIoTCrudActions(entityExampleResourceName);
		permissionSystemService.checkOrCreateRoleWithPermissionsSpecificToEntity(specificRole.getName(),entityExample.getId(),sharedEntityAction);

		SharedEntityExampleRestApi entityExampleRestApi = getOsgiService(SharedEntityExampleRestApi.class);
		// huser tries to find Entity Example after shared operation
		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(entityExampleResourceName,
				HyperIoTCrudAction.FIND);
		addPermission(huser, action, 0);
		this.impersonateUser(entityExampleRestApi, huser);
		Response restResponse = entityExampleRestApi.findSharedEntityExample(entityExample.getId());
		Assert.assertEquals(403, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	@Test
	public void test053_huserFindAllEntitiesSharedAfterSharedOperation() {
		// hadmin save SharedEntity with the following call saveSharedEntity, and
		// huser find all Entity Example after shared entity operation
		// response status code '200'
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");

		SharedEntityExample entityExample1 = createSharedEntityExample((HUser) adminUser);
		Assert.assertNotEquals(0, entityExample1.getId());
		Assert.assertEquals(adminUser.getId(), entityExample1.getUser().getId());

		SharedEntityExample entityExample2 = createSharedEntityExample((HUser) adminUser);
		Assert.assertNotEquals(0, entityExample2.getId());
		Assert.assertEquals(adminUser.getId(), entityExample2.getUser().getId());

		// adminUser share his entity with huser
		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(entityExampleResourceName,
				HyperIoTCrudAction.FINDALL);
		HUser huser = createHUser(action);

		SharedEntity sharedEntity1 = createSharedEntity(entityExample1, (HUser) adminUser, huser);
		Assert.assertEquals(entityExample1.getId(), sharedEntity1.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity1.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity1.getEntityResourceName());

		// second SharedEntity
		SharedEntity sharedEntity2 = createSharedEntity(entityExample2, (HUser) adminUser, huser);
		Assert.assertEquals(entityExample2.getId(), sharedEntity2.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity2.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity2.getEntityResourceName());

		SharedEntityExampleRestApi entityExampleRestApi = getOsgiService(SharedEntityExampleRestApi.class);
		this.impersonateUser(entityExampleRestApi, huser);
		Response responseFindAllEntityExample = entityExampleRestApi.findAllSharedEntityExample();
		List<SharedEntityExample> listEntities = responseFindAllEntityExample.readEntity(new GenericType<List<SharedEntityExample>>() {
		});
		Assert.assertFalse(listEntities.isEmpty());
		Assert.assertEquals(2, listEntities.size());
		// entityExample3 isn't not shared
		boolean entityFound1 = false;
		boolean entityFound2 = false;
		for (SharedEntityExample se : listEntities) {
			if (entityExample1.getId() == se.getId()) {
				Assert.assertEquals(adminUser.getId(), se.getUser().getId());
				entityFound1 = true;
			}
			if (entityExample2.getId() == se.getId()) {
				Assert.assertEquals(adminUser.getId(), se.getUser().getId());
				entityFound2 = true;
			}
		}
		Assert.assertTrue(entityFound1);
		Assert.assertTrue(entityFound2);
		Assert.assertEquals(200, responseFindAllEntityExample.getStatus());
	}


	@Test
	public void test054_huserUpdateEntitySharedAfterSharedOperationShouldWork() {
		// hadmin save SharedEntity with the following call saveSharedEntity,
		// after shared operation huser update Entity Example
		// response status code '200'
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");

		SharedEntityExample entityExample = createSharedEntityExample((HUser) adminUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(adminUser.getId(), entityExample.getUser().getId());

		// adminUser share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = createSharedEntity(entityExample, (HUser) adminUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(entityExampleResourceName,
				HyperIoTCrudAction.UPDATE);
		addPermission(huser, action, 0);
		// add specific permission with resourceId
		addPermission(huser, action, entityExample.getId());

		SharedEntityExampleRestApi entityExampleRestApi = getOsgiService(SharedEntityExampleRestApi.class);
		entityExample.setDescription("Description edited by huser: " + huser.getUsername());
		this.impersonateUser(entityExampleRestApi, huser);
		Response restResponseUpdateEntityExample = entityExampleRestApi.updateSharedEntityExample(entityExample);
		Assert.assertEquals(200, restResponseUpdateEntityExample.getStatus());
		Assert.assertEquals(entityExample.getId(),
				((SharedEntityExample) restResponseUpdateEntityExample.getEntity()).getId());
		Assert.assertEquals("Description edited by huser: " + huser.getUsername(),
				((SharedEntityExample) restResponseUpdateEntityExample.getEntity()).getDescription());
		Assert.assertEquals(entityExample.getEntityVersion() + 1,
				(((SharedEntityExample) restResponseUpdateEntityExample.getEntity()).getEntityVersion()));
		Assert.assertEquals(adminUser.getId(),
				((SharedEntityExample) restResponseUpdateEntityExample.getEntity()).getUser().getId());
	}


	@Test
	public void test055_huserWithoutPermissionTriesToUpdateEntitySharedAfterSharedOperationShouldFail() {
		// hadmin save SharedEntity with the following call saveSharedEntity,
		// huser, without permission, tries to update Entity Example after shared entity operation
		// response status code '403' HyperIoTUnauthorizedException
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");

		SharedEntityExample entityExample = createSharedEntityExample((HUser) adminUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(adminUser.getId(), entityExample.getUser().getId());

		// adminUser share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = createSharedEntity(entityExample, (HUser) adminUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		SharedEntityExampleRestApi entityExampleRestApi = getOsgiService(SharedEntityExampleRestApi.class);
		entityExample.setDescription("Description edited by huser: " + huser.getUsername());
		this.impersonateUser(entityExampleRestApi, huser);
		Response restResponse = entityExampleRestApi.updateSharedEntityExample(entityExample);
		Assert.assertEquals(403, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	@Test
	public void test056_huserWithoutSpecificPermissionTriesToUpdateEntitySharedAfterSharedOperationShouldFail() {
		// hadmin save SharedEntity with the following call saveSharedEntity,
		// huser, without specific permission, tries to update Entity Example after shared entity operation
		// response status code '403' HyperIoTUnauthorizedException
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");

		SharedEntityExample entityExample = createSharedEntityExample((HUser) adminUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(adminUser.getId(), entityExample.getUser().getId());

		// adminUser share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = createSharedEntity(entityExample, (HUser) adminUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		//Create a specific role and after that add the permission to this specific role
		Role specificRole = createRole();
		//Add specific permission to SharedEntityExample saved before
		PermissionSystemApi permissionSystemService = getOsgiService(PermissionSystemApi.class);
		List<HyperIoTAction> sharedEntityAction = HyperIoTActionsUtil.getHyperIoTCrudActions(entityExampleResourceName);
		permissionSystemService.checkOrCreateRoleWithPermissionsSpecificToEntity(specificRole.getName(),entityExample.getId(),sharedEntityAction);

		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(entityExampleResourceName,
				HyperIoTCrudAction.UPDATE);
		addPermission(huser, action, 0);
		// specific permission isn't assigned

		SharedEntityExampleRestApi entityExampleRestApi = getOsgiService(SharedEntityExampleRestApi.class);
		entityExample.setDescription("Description edited by huser: " + huser.getUsername());
		this.impersonateUser(entityExampleRestApi, huser);
		Response restResponse = entityExampleRestApi.updateSharedEntityExample(entityExample);
		Assert.assertEquals(403, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	@Test
	public void test057_huserDeleteEntityExampleAfterSharedOperationShouldWork() {
		// hadmin save SharedEntity with the following call saveSharedEntity, and
		// huser delete Entity Example after shared entity operation
		// response status code '200'
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");

		SharedEntityExample entityExample = createSharedEntityExample((HUser) adminUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(adminUser.getId(), entityExample.getUser().getId());

		// adminUser share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = createSharedEntity(entityExample, (HUser) adminUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		SharedEntityExampleRestApi entityExampleRestApi = getOsgiService(SharedEntityExampleRestApi.class);
		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(entityExampleResourceName,
				HyperIoTCrudAction.REMOVE);
		addPermission(huser, action, 0);
		// add specific permission with resourceId
		addPermission(huser, action, entityExample.getId());
		this.impersonateUser(entityExampleRestApi, huser);
		Response restResponseDeleteEntityExample = entityExampleRestApi.deleteSharedEntityExample(entityExample.getId());
		Assert.assertEquals(200, restResponseDeleteEntityExample.getStatus());
		Assert.assertNull(restResponseDeleteEntityExample.getEntity());
	}


	@Test
	public void test058_huserWithoutPermissionTriesToDeleteEntityExampleAfterSharedOperationShouldFail() {
		// hadmin save SharedEntity with the following call saveSharedEntity, and
		// huser, without permission, tries to delete Entity Example after shared entity operation
		// response status code '403' HyperIoTUnauthorizedException
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");

		SharedEntityExample entityExample = createSharedEntityExample((HUser) adminUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(adminUser.getId(), entityExample.getUser().getId());

		// adminUser share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = createSharedEntity(entityExample, (HUser) adminUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		SharedEntityExampleRestApi entityExampleRestApi = getOsgiService(SharedEntityExampleRestApi.class);
		this.impersonateUser(entityExampleRestApi, huser);
		Response restResponse = entityExampleRestApi.deleteSharedEntityExample(entityExample.getId());
		Assert.assertEquals(403, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	@Test
	public void test059_huserWithoutSpecificPermissionTriesToDeleteEntityExampleAfterSharedOperationShouldFail() {
		// hadmin save SharedEntity with the following call saveSharedEntity, and
		// huser, without specific permission, tries to delete Entity Example after shared entity operation
		// response status code '403' HyperIoTUnauthorizedException
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");

		SharedEntityExample entityExample = createSharedEntityExample((HUser) adminUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(adminUser.getId(), entityExample.getUser().getId());

		// adminUser share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = createSharedEntity(entityExample, (HUser) adminUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		//Create a specific role and after that add the permission to this specific role
		Role specificRole = createRole();
		//Add specific permission to SharedEntityExample saved before
		PermissionSystemApi permissionSystemService = getOsgiService(PermissionSystemApi.class);
		List<HyperIoTAction> sharedEntityAction = HyperIoTActionsUtil.getHyperIoTCrudActions(entityExampleResourceName);
		permissionSystemService.checkOrCreateRoleWithPermissionsSpecificToEntity(specificRole.getName(),entityExample.getId(),sharedEntityAction);

		SharedEntityExampleRestApi entityExampleRestApi = getOsgiService(SharedEntityExampleRestApi.class);
		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(entityExampleResourceName,
				HyperIoTCrudAction.REMOVE);
		addPermission(huser, action, 0);
		this.impersonateUser(entityExampleRestApi, huser);
		Response restResponse = entityExampleRestApi.deleteSharedEntityExample(entityExample.getId());
		Assert.assertEquals(403, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	@Test
	public void test060_huser2TriesToDeleteEntityExampleShouldFailIfItIsNotAssociatedWithSharedEntity() {
		// adminUser save SharedEntity with the following call saveSharedEntity, and
		// huser2 tries to delete entity example, after shared operation, with the following call deleteSharedEntityExample,
		// huser2 has permission (REMOVE) but it's unauthorized because isn't associated with shared entity
		// response status code '403' HyperIoTUnauthorizedException
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");

		SharedEntityExample entityExample = createSharedEntityExample((HUser) adminUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(adminUser.getId(), entityExample.getUser().getId());

		// adminUser share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = createSharedEntity(entityExample, (HUser) adminUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		Assert.assertNotEquals(huser.getId(), entityExample.getUser().getId());

		SharedEntityExampleRestApi entityExampleRestApi = getOsgiService(SharedEntityExampleRestApi.class);
		// huser2 isn't associated in SharedEntity and isn't the owner hproject
		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(entityExampleResourceName,
				HyperIoTCrudAction.REMOVE);
		HUser huser2 = createHUser(action);
		addPermission(huser2, action, 0);
		// add specific permission with resourceId
		addPermission(huser2, action, entityExample.getId());
		this.impersonateUser(entityExampleRestApi, huser2);
		Response restResponse = entityExampleRestApi.deleteSharedEntityExample(entityExample.getId());
		Assert.assertEquals(404, restResponse.getStatus());
	}


	@Test
	public void test061_huserInsertedInSharedEntityTriesToBecomeNewOwnerResourceShouldFail() {
		// hadmin save SharedEntity with the following call saveSharedEntity, after shared operation
		// huser tries to be owner of entity example with the following call updateSharedEntityExample
		// response status code '403' HyperIoTUnauthorizedException
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");

		SharedEntityExample entityExample = createSharedEntityExample((HUser) adminUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(adminUser.getId(), entityExample.getUser().getId());

		// adminUser share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = createSharedEntity(entityExample, (HUser) adminUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		SharedEntityExampleRestApi entityExampleRestApi = getOsgiService(SharedEntityExampleRestApi.class);
		entityExample.setUser(huser);

		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(entityExampleResourceName,
				HyperIoTCrudAction.UPDATE);
		addPermission(huser, action, 0);
		// add specific permission with resourceId
		addPermission(huser, action, entityExample.getId());

		// user on shared resource cannot change the owner
		this.impersonateUser(entityExampleRestApi, huser);
		Response restResponse = entityExampleRestApi.updateSharedEntityExample(entityExample);
		Assert.assertEquals(200, restResponse.getStatus());
		Assert.assertEquals(adminUser.getId(), ((SharedEntityExample) restResponse.getEntity()).getUser().getId());
	}


	@Test
	public void test062_huserTriesToMakeHUser2NewOwnerOfEntitySharedAfterSharedOperationShouldFail() {
		// ownerUser save SharedEntity with the following call saveSharedEntity, and
		// huser tries to make huser2 new owner of entity example with the call updateSharedEntityExample;
		// huser is associated with entity example, but that isn't an allowed operation
		// response status code '403' HyperIoTUnauthorizedException
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");

		SharedEntityExample entityExample = createSharedEntityExample((HUser) adminUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(adminUser.getId(), entityExample.getUser().getId());

		// adminUser share his hproject with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = createSharedEntity(entityExample, (HUser) adminUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		SharedEntityExampleRestApi entityExampleRestApi = getOsgiService(SharedEntityExampleRestApi.class);
		// huser2 isn't associated in SharedEntity and isn't the owner hproject
		HUser huser2 = createHUser(null);
		entityExample.setUser(huser2);

		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(entityExampleResourceName,
				HyperIoTCrudAction.UPDATE);
		addPermission(huser, action, 0);
		// add specific permission with resourceId
		addPermission(huser, action, entityExample.getId());

		// user on shared resource cannot change the owner
		this.impersonateUser(entityExampleRestApi, huser);
		Response restResponse = entityExampleRestApi.updateSharedEntityExample(entityExample);
		Assert.assertEquals(200, restResponse.getStatus());
		Assert.assertEquals(adminUser.getId(), ((SharedEntityExample) restResponse.getEntity()).getUser().getId());
	}


	/*
	 *
	 *
	 * UTILITY METHODS
	 *
	 *
	 */


	private HUser createHUser(HyperIoTAction action) {
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
		huser.setEmail("testusername" + UUID.randomUUID().toString() + "@hyperiot.com");
		huser.setPassword("passwordPass&01");
		huser.setPasswordConfirm("passwordPass&01");
		huser.setAdmin(false);
		huser.setActive(true);
		Response restResponse = hUserRestApi.saveHUser(huser);
		Assert.assertEquals(200, restResponse.getStatus());
		Assert.assertNotEquals(0, ((HUser) restResponse.getEntity()).getId());
		Assert.assertEquals("name", ((HUser) restResponse.getEntity()).getName());
		Assert.assertEquals("lastname", ((HUser) restResponse.getEntity()).getLastname());
		Assert.assertEquals(huser.getUsername(), ((HUser) restResponse.getEntity()).getUsername());
		Assert.assertEquals(huser.getEmail(), ((HUser) restResponse.getEntity()).getEmail());
		Assert.assertFalse(huser.isAdmin());
		Assert.assertTrue(huser.isActive());
		Assert.assertTrue(roles.isEmpty());
		if (action != null) {
			Role role = createRole();
			huser.addRole(role);
			RoleRestApi roleRestApi = getOsgiService(RoleRestApi.class);
			Response restUserRole = roleRestApi.saveUserRole(role.getId(), huser.getId());
			Assert.assertEquals(200, restUserRole.getStatus());
			Assert.assertTrue(huser.hasRole(role));
			roles = Arrays.asList(huser.getRoles().toArray());
			Assert.assertFalse(roles.isEmpty());
			Permission permission = utilGrantPermission(huser, role, action, 0);
			Assert.assertNotEquals(0, permission.getId());
			Assert.assertEquals(entityExampleResourceName + " assigned to huser_id " + huser.getId(), permission.getName());
			Assert.assertEquals(action.getActionId(), permission.getActionIds());
			Assert.assertEquals(action.getCategory(), permission.getEntityResourceName());
			Assert.assertEquals(role.getId(), permission.getRole().getId());
		}
		return huser;
	}

	private Permission utilGrantPermission(HUser huser, Role role, HyperIoTAction action, long resourceId) {
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
				if (resourceId > 0)
					permission.setResourceId(resourceId);
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

	private Permission addPermission(HUser huser, HyperIoTAction action, long resourceId) {
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
		Role role = createRole();
		huser.addRole(role);
		RoleRestApi roleRestApi = getOsgiService(RoleRestApi.class);
		this.impersonateUser(roleRestApi, adminUser);
		Response restUserRole = roleRestApi.saveUserRole(role.getId(), huser.getId());
		Assert.assertEquals(200, restUserRole.getStatus());
		Assert.assertTrue(huser.hasRole(role));
		Permission permission = utilGrantPermission(huser, role, action, resourceId);
		return permission;
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
		Assert.assertEquals(role.getDescription(), ((Role) restResponse.getEntity()).getDescription());
		return role;
	}


	private SharedEntityExample createSharedEntityExample(HUser huser) {
		SharedEntityExampleRestApi sharedEntityExampleRestApi = getOsgiService(SharedEntityExampleRestApi.class);
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
		Assert.assertNotNull(adminUser);
		Assert.assertTrue(adminUser.isAdmin());

		SharedEntityExample entityExample = new SharedEntityExample();
		entityExample.setName("Shared entity example " + java.util.UUID.randomUUID());
		entityExample.setDescription("Shared entity example of user: " + huser.getUsername());
		if (huser != null) {
			entityExample.setUser(huser);
		} else {
			huser = (HUser)adminUser;
			entityExample.setUser(huser);
		}
		this.impersonateUser(sharedEntityExampleRestApi, adminUser);
		Response restResponse = sharedEntityExampleRestApi.saveSharedEntityExample(entityExample);
		Assert.assertEquals(200, restResponse.getStatus());
		Assert.assertNotEquals(0, ((SharedEntityExample) restResponse.getEntity()).getId());
		Assert.assertEquals(entityExample.getName(), ((SharedEntityExample) restResponse.getEntity()).getName());
		Assert.assertEquals("Shared entity example of user: " + huser.getUsername(),
				((SharedEntityExample) restResponse.getEntity()).getDescription());
		Assert.assertEquals(huser.getId(), ((SharedEntityExample) restResponse.getEntity()).getUser().getId());
		return entityExample;
	}


	private SharedEntity createSharedEntity(HyperIoTBaseEntity hyperIoTBaseEntity, HUser ownerUser, HUser huser) {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);

		SharedEntity sharedEntity = new SharedEntity();
		sharedEntity.setEntityId(hyperIoTBaseEntity.getId());
		sharedEntity.setEntityResourceName(hyperIoTBaseEntity.getResourceName()); // "it.acsoftware.hyperiot.shared.entity.example.HyperIoTSharedEntityExample"
		sharedEntity.setUserId(huser.getId());

		if (ownerUser == null) {
			AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
			ownerUser = (HUser) authService.login("hadmin", "admin");
			Assert.assertTrue(ownerUser.isAdmin());
		}
		this.impersonateUser(sharedEntityRestApi, ownerUser);
		Response restResponse = sharedEntityRestApi.saveSharedEntity(sharedEntity);
		Assert.assertEquals(200, restResponse.getStatus());
		Assert.assertEquals(hyperIoTBaseEntity.getId(), ((SharedEntity) restResponse.getEntity()).getEntityId());
		Assert.assertEquals(huser.getId(), ((SharedEntity) restResponse.getEntity()).getUserId());
		Assert.assertEquals(hyperIoTBaseEntity.getResourceName(), ((SharedEntity) restResponse.getEntity()).getEntityResourceName());
		return sharedEntity;
	}


	@After
	public void afterTest() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");

		this.impersonateUser(sharedEntityRestApi, adminUser);
		Response restResponse = sharedEntityRestApi.findAllSharedEntity();
		Assert.assertEquals(200, restResponse.getStatus());
		List<SharedEntity> sharedEntityList = restResponse.readEntity(new GenericType<List<SharedEntity>>() {
		});
		if (!sharedEntityList.isEmpty()) {
			Assert.assertFalse(sharedEntityList.isEmpty());
			for (SharedEntity sharedEntity : sharedEntityList) {
				this.impersonateUser(sharedEntityRestApi, adminUser);
				Response restResponse1 = sharedEntityRestApi.deleteSharedEntity(sharedEntity);
				Assert.assertEquals(200, restResponse1.getStatus());
				Assert.assertNull(restResponse1.getEntity());
			}
		}

		SharedEntityExampleRestApi entityExampleRestApi = getOsgiService(SharedEntityExampleRestApi.class);
		this.impersonateUser(entityExampleRestApi, adminUser);
		Response responseFindAllEntityExample = entityExampleRestApi.findAllSharedEntityExample();
		List<SharedEntityExample> listEntities = responseFindAllEntityExample.readEntity(new GenericType<List<SharedEntityExample>>() {
		});
		if (!listEntities.isEmpty()) {
			Assert.assertFalse(listEntities.isEmpty());
			for (SharedEntityExample sharedEntityExample : listEntities) {
				this.impersonateUser(entityExampleRestApi, adminUser);
				Response restResponse1 = entityExampleRestApi.deleteSharedEntityExample(sharedEntityExample.getId());
				Assert.assertEquals(200, restResponse1.getStatus());
				Assert.assertNull(restResponse1.getEntity());
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
                this.impersonateUser(roleRestService, adminUser);
                if (!role.getName().contains("RegisteredUser")) {
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
