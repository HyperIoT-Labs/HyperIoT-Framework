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
import it.acsoftware.hyperiot.base.api.entity.HyperIoTPaginableResult;
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
public class HyperIoTSharedEntityExampleWithPermissionRestTest extends KarafTestSupport {

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
		ownerUser = createHUser(null);
		Assert.assertNotEquals(0, ownerUser.getId());
		this.impersonateUser(sharedEntityRestApi, ownerUser);
		Response restResponse = sharedEntityRestApi.checkModuleWorking();
		Assert.assertEquals(200, restResponse.getStatus());
		Assert.assertEquals("SharedEntity Module works!", restResponse.getEntity());
	}


	@Test
	public void test002_saveSharedEntityWithPermissionShouldWork() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// ownerUser, with permission, save SharedEntity with the following call saveSharedEntity
		// response status code '200'
		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(entityExampleResourceName,
				HyperIoTShareAction.SHARE);
		ownerUser = createHUser(action);

		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		// ownerUser share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = new SharedEntity();
		sharedEntity.setEntityId(entityExample.getId());
		sharedEntity.setEntityResourceName(entityExampleResourceName); // "it.acsoftware.hyperiot.shared.entity.example.HyperIoTSharedEntityExample"
		sharedEntity.setUserId(huser.getId());

		this.impersonateUser(sharedEntityRestApi, ownerUser);
		Response restResponse = sharedEntityRestApi.saveSharedEntity(sharedEntity);
		Assert.assertEquals(200, restResponse.getStatus());
		Assert.assertNotEquals(0, ((SharedEntity) restResponse.getEntity()).getEntityId());
		Assert.assertEquals(entityExample.getId(), ((SharedEntity) restResponse.getEntity()).getEntityId());
		Assert.assertEquals(huser.getId(), ((SharedEntity) restResponse.getEntity()).getUserId());
		Assert.assertEquals(entityExampleResourceName, ((SharedEntity) restResponse.getEntity()).getEntityResourceName());
	}


	@Test
	public void test003_saveSharedEntityWithoutPermissionShouldWork() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// ownerUser, without permission, tries to save SharedEntity with the following call saveSharedEntity
		// response status code '403' HyperIoTUnauthorizedException
		ownerUser = createHUser(null);

		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		// ownerUser share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = new SharedEntity();
		sharedEntity.setEntityId(entityExample.getId());
		sharedEntity.setEntityResourceName(entityExampleResourceName); // "it.acsoftware.hyperiot.shared.entity.example.HyperIoTSharedEntityExample"
		sharedEntity.setUserId(huser.getId());

		this.impersonateUser(sharedEntityRestApi, ownerUser);
		Response restResponse = sharedEntityRestApi.saveSharedEntity(sharedEntity);
		Assert.assertEquals(403, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	@Test
	public void test004_saveSharedEntityWithPermissionShouldFailIfEntityIsNotShared() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// ownerUser, with permission, tries to save SharedEntity with the following call saveSharedEntity,
		// but entity isn't shared
		// response status code '500' HyperIoTRuntimeException
		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(entityExampleResourceName,
				HyperIoTShareAction.SHARE);
		ownerUser = createHUser(action);

		Role role = createRole();
		Assert.assertNotEquals(0, role.getId());

		HUser huser = createHUser(null);

		SharedEntity sharedEntity = new SharedEntity();
		sharedEntity.setEntityId(role.getId());
		sharedEntity.setEntityResourceName("it.acsoftware.hyperiot.role.model.Role");
		sharedEntity.setUserId(huser.getId());

		this.impersonateUser(sharedEntityRestApi, ownerUser);
		Response restResponse = sharedEntityRestApi.saveSharedEntity(sharedEntity);
		Assert.assertEquals(500, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTRuntimeException",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
		Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().size());
	}


	@Test
	public void test005_findByPKWithPermissionShouldWork() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// ownerUser, with permission, find SharedEntity by primary key with the following call findByPK
		// response status code '200'
		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(sharedEntityResourceName,
				HyperIoTCrudAction.FIND);
		ownerUser = createHUser(action);

		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		// ownerUser share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = createSharedEntity(entityExample, ownerUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		this.impersonateUser(sharedEntityRestApi, ownerUser);
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
	public void test006_findByPKWithoutPermissionShouldFail() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// ownerUser, without permission, tries to find SharedEntity by primary key with the following call findByPK,
		// response status code '403' HyperIoTUnauthorizedException
		ownerUser = createHUser(null);

		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		// ownerUser share his entity with huser
		HUser huser = createHUser(null);

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


	@Test
	public void test007_findByPKWithPermissionShouldFailIfSharedEntityNotFound() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// ownerUser, with permission, tries to find SharedEntity by primary key with the following call findByPK,
		// but SharedEntity isn't stored in database
		// response status code '404' HyperIoTEntityNotFound
		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(sharedEntityResourceName,
				HyperIoTCrudAction.FIND);
		ownerUser = createHUser(action);

		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		// ownerUser share his entity with huser
		HUser huser = createHUser(null);

		// SharedEntity isn't stored in database
		SharedEntity sharedEntity = new SharedEntity();
		sharedEntity.setEntityId(entityExample.getId());
		sharedEntity.setEntityResourceName(entityExampleResourceName);
		sharedEntity.setUserId(huser.getId());

		this.impersonateUser(sharedEntityRestApi, ownerUser);
		Response restResponse = sharedEntityRestApi.findByPK(sharedEntity);
		Assert.assertEquals(404, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	@Test
	public void test008_findByPKWithPermissionShouldFailIfPKNotFound() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// ownerUser, with permission, tries to find SharedEntity by primary key with the following call findByPK,
		// but primary key not found
		// response status code '404' HyperIoTEntityNotFound
		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(sharedEntityResourceName,
				HyperIoTCrudAction.FIND);
		ownerUser = createHUser(action);

		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		// ownerUser share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = createSharedEntity(entityExample, ownerUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		sharedEntity.setEntityId(0);
		Assert.assertEquals(0, sharedEntity.getEntityId());

		this.impersonateUser(sharedEntityRestApi, ownerUser);
		Response restResponse = sharedEntityRestApi.findByPK(sharedEntity);
		Assert.assertEquals(404, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	@Test
	public void test009_findByPKWithPermissionShouldFailIfEntityResourceNameNotFound() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// ownerUser, with permission, tries to find SharedEntity by primary key with the following call findByPK,
		// but entityResourceName not found
		// response status code '404' HyperIoTEntityNotFound
		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(sharedEntityResourceName,
				HyperIoTCrudAction.FIND);
		ownerUser = createHUser(action);

		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		// ownerUser share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = createSharedEntity(entityExample, ownerUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		sharedEntity.setEntityResourceName("entity.resource.name.not.found");
		Assert.assertEquals("entity.resource.name.not.found", sharedEntity.getEntityResourceName());

		this.impersonateUser(sharedEntityRestApi, ownerUser);
		Response restResponse = sharedEntityRestApi.findByPK(sharedEntity);
		Assert.assertEquals(404, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	@Test
	public void test010_findByPKWithPermissionShouldFailIfHUserNotFound() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// ownerUser, with permission, tries to find SharedEntity by primary key with the following call findByPK,
		// but huser not found
		// response status code '404' HyperIoTEntityNotFound
		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(sharedEntityResourceName,
				HyperIoTCrudAction.FIND);
		ownerUser = createHUser(action);

		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		// ownerUser share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = createSharedEntity(entityExample, ownerUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		sharedEntity.setUserId(0);
		Assert.assertEquals(0, sharedEntity.getUserId());

		this.impersonateUser(sharedEntityRestApi, ownerUser);
		Response restResponse = sharedEntityRestApi.findByPK(sharedEntity);
		Assert.assertEquals(404, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	@Test
	public void test011_findByPKWithoutPermissionShouldFailAndSharedEntityNotFound() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// ownerUser, without permission, tries to find SharedEntity (not stored in database) by primary key
		// with the following call findByPK
		// response status code '403' HyperIoTUnauthorizedException
		ownerUser = createHUser(null);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		// ownerUser share his entity with huser
		HUser huser = createHUser(null);

		// SharedEntity isn't stored in database
		SharedEntity sharedEntity = new SharedEntity();
		sharedEntity.setEntityId(entityExample.getId());
		sharedEntity.setEntityResourceName(entityExampleResourceName);
		sharedEntity.setUserId(huser.getId());

		this.impersonateUser(sharedEntityRestApi, ownerUser);
		Response restResponse = sharedEntityRestApi.findByPK(sharedEntity);
		Assert.assertEquals(403, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	@Test
	public void test012_findByPKWithoutPermissionShouldFailAndPKNotFound() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// ownerUser, without permission, tries to find SharedEntity by primary key (PK not found)
		// with the following call findByPK
		// response status code '403' HyperIoTUnauthorizedException
		ownerUser = createHUser(null);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		// ownerUser share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = createSharedEntity(entityExample, ownerUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		sharedEntity.setEntityId(0);
		Assert.assertEquals(0, sharedEntity.getEntityId());

		this.impersonateUser(sharedEntityRestApi, ownerUser);
		Response restResponse = sharedEntityRestApi.findByPK(sharedEntity);
		Assert.assertEquals(403, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	@Test
	public void test013_findByPKWithoutPermissionShouldFailAndEntityResourceNameNotFound() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// ownerUser, without permission, tries to find SharedEntity by primary key (entityResourceName not found)
		// with the following call findByPK,
		// response status code '403' HyperIoTUnauthorizedException
		ownerUser = createHUser(null);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		// ownerUser share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = createSharedEntity(entityExample, ownerUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		sharedEntity.setEntityResourceName("entity.resource.name.not.found");
		Assert.assertEquals("entity.resource.name.not.found", sharedEntity.getEntityResourceName());

		this.impersonateUser(sharedEntityRestApi, ownerUser);
		Response restResponse = sharedEntityRestApi.findByPK(sharedEntity);
		Assert.assertEquals(403, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	@Test
	public void test014_findByPKWithoutPermissionShouldFailAndHUserNotFound() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// ownerUser, without permission, tries to find SharedEntity by primary key (huser not found)
		// with the following call findByPK,
		// response status code '403' HyperIoTUnauthorizedException
		ownerUser = createHUser(null);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		// ownerUser share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = createSharedEntity(entityExample, ownerUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		sharedEntity.setUserId(0);
		Assert.assertEquals(0, sharedEntity.getUserId());

		this.impersonateUser(sharedEntityRestApi, ownerUser);
		Response restResponse = sharedEntityRestApi.findByPK(sharedEntity);
		Assert.assertEquals(403, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	@Test
	public void test015_findAllSharedEntityWithPermissionShouldWorkIfSharedEntityListIsEmpty() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// ownerUser, with permission, find all SharedEntity with the following call findAllSharedEntity,
		// there are still no entities saved in the database, this call return an empty list
		// response status code '200'
		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(sharedEntityResourceName,
				HyperIoTCrudAction.FINDALL);
		ownerUser = createHUser(action);
		this.impersonateUser(sharedEntityRestApi, ownerUser);
		Response restResponse = sharedEntityRestApi.findAllSharedEntity();
		Assert.assertEquals(200, restResponse.getStatus());
		List<SharedEntity> sharedEntityList = restResponse.readEntity(new GenericType<List<SharedEntity>>() {
		});
		Assert.assertTrue(sharedEntityList.isEmpty());
		Assert.assertEquals(0, sharedEntityList.size());
	}


	@Test
	public void test016_findAllSharedEntityWithPermissionShouldWork() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// ownerUser, with permission, find all SharedEntity with the following call findAllSharedEntity
		// response status code '200'
		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(sharedEntityResourceName,
				HyperIoTCrudAction.FINDALL);
		ownerUser = createHUser(action);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		// ownerUser share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = createSharedEntity(entityExample, ownerUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		this.impersonateUser(sharedEntityRestApi, ownerUser);
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
	public void test017_findAllSharedEntityWithoutPermissionShouldFail() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// ownerUser, without permission, tries to find all SharedEntity with the following call findAllSharedEntity
		// response status code '403' HyperIoTUnauthorizedException
		ownerUser = createHUser(null);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		// ownerUser share his entity with huser
		HUser huser = createHUser(null);

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


	@Test
	public void test018_findByEntityWithPermissionShouldWork() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// ownerUser, with permission, find SharedEntity by entity with the following call findByEntity
		// response status code '200'
		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(sharedEntityResourceName,
				HyperIoTCrudAction.FIND);
		ownerUser = createHUser(action);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		// ownerUser share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = createSharedEntity(entityExample, ownerUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		this.impersonateUser(sharedEntityRestApi, ownerUser);
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
	public void test019_findByEntityWithoutPermissionShouldFail() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// ownerUser, without permission, tries to find SharedEntity by entity with the following call findByEntity
		// response status code '403' HyperIoTUnauthorizedException
		ownerUser = createHUser(null);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		// ownerUser share his entity with huser
		HUser huser = createHUser(null);

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


	@Test
	public void test020_findByEntityWithPermissionShouldWorkIfEntityResourceNameIsNull() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// ownerUser, with permission, tries to find SharedEntity by entity with the following call findByEntity,
		// if entityResourceName is null this call return an empty list
		// response status code '200'
		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(sharedEntityResourceName,
				HyperIoTCrudAction.FIND);
		ownerUser = createHUser(action);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		// ownerUser share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = createSharedEntity(entityExample, ownerUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		this.impersonateUser(sharedEntityRestApi, ownerUser);
		Response restResponse = sharedEntityRestApi
				.findByEntity(null, entityExample.getId());
		Assert.assertEquals(200, restResponse.getStatus());
		List<SharedEntity> sharedEntityList = restResponse.readEntity(new GenericType<List<SharedEntity>>() {
		});
		Assert.assertTrue(sharedEntityList.isEmpty());
		Assert.assertEquals(0, sharedEntityList.size());
	}


	@Test
	public void test021_findByEntityWithPermissionShouldWorkIfEntityIdIsZero() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// ownerUser, with permission, tries to find SharedEntity by entity with the following call findByEntity,
		// if entityId is zero this call return an empty list
		// response status code '200'
		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(sharedEntityResourceName,
				HyperIoTCrudAction.FIND);
		ownerUser = createHUser(action);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		// ownerUser share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = createSharedEntity(entityExample, ownerUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		this.impersonateUser(sharedEntityRestApi, ownerUser);
		Response restResponse = sharedEntityRestApi
				.findByEntity(sharedEntity.getEntityResourceName(), 0);
		Assert.assertEquals(200, restResponse.getStatus());
		List<SharedEntity> sharedEntityList = restResponse.readEntity(new GenericType<List<SharedEntity>>() {
		});
		Assert.assertTrue(sharedEntityList.isEmpty());
		Assert.assertEquals(0, sharedEntityList.size());
	}


	@Test
	public void test022_findByEntityResourceNameNotFoundWithoutPermissionShouldFail() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// ownerUser, without permission, tries to find SharedEntity by entity (entityResourceName is null)
		// with the following call findByEntity
		// response status code '403' HyperIoTUnauthorizedException
		ownerUser = createHUser(null);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		// ownerUser share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = createSharedEntity(entityExample, ownerUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		this.impersonateUser(sharedEntityRestApi, ownerUser);
		Response restResponse = sharedEntityRestApi
				.findByEntity(null, entityExample.getId());
		Assert.assertEquals(403, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	@Test
	public void test023_findByEntityIdNotFoundWithoutPermissionShouldFail() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// ownerUser, without permission, tries to find SharedEntity by entity (entityId is zero)
		// with the following call findByEntity
		// response status code '403' HyperIoTUnauthorizedException
		ownerUser = createHUser(null);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		// ownerUser share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = createSharedEntity(entityExample, ownerUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		this.impersonateUser(sharedEntityRestApi, ownerUser);
		Response restResponse = sharedEntityRestApi
				.findByEntity(sharedEntity.getEntityResourceName(), 0);
		Assert.assertEquals(403, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	@Test
	public void test024_findByUserWithPermissionShouldWork() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// ownerUser, with permission, find SharedEntity by huser with the following call findByUser
		// response status code '200'
		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(sharedEntityResourceName,
				HyperIoTCrudAction.FIND);
		ownerUser = createHUser(action);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		// ownerUser share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = createSharedEntity(entityExample, ownerUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		this.impersonateUser(sharedEntityRestApi, ownerUser);
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
	public void test025_findByUserWithoutPermissionShouldFail() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// ownerUser, without permission, tries to find SharedEntity by huser with the following call findByUser
		// response status code '403' HyperIoTUnauthorizedException
		ownerUser = createHUser(null);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		// ownerUser share his entity with huser
		HUser huser = createHUser(null);

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


	@Test
	public void test026_findByUserWithPermissionShouldWorkIfUserNotFound() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// ownerUser, with permission, tries to find SharedEntity by huser with the following call findByUser,
		// if huser not found this call return an empty list
		// response status code '200'
		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(sharedEntityResourceName,
				HyperIoTCrudAction.FIND);
		ownerUser = createHUser(action);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		// ownerUser share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = createSharedEntity(entityExample, ownerUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		this.impersonateUser(sharedEntityRestApi, ownerUser);
		Response restResponse = sharedEntityRestApi.findByUser(0);
		Assert.assertEquals(200, restResponse.getStatus());
		List<SharedEntity> sharedEntityList = restResponse.readEntity(new GenericType<List<SharedEntity>>() {
		});
		Assert.assertTrue(sharedEntityList.isEmpty());
		Assert.assertEquals(0, sharedEntityList.size());
	}


	@Test
	public void test027_findByUserWithoutPermissionShouldFailAndUserNotFound() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// ownerUser, without permission, tries to find SharedEntity by huser (huser not found)
		// with the following call findByUser,
		// response status code '403' HyperIoTUnauthorizedException
		ownerUser = createHUser(null);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		// ownerUser share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = createSharedEntity(entityExample, ownerUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		this.impersonateUser(sharedEntityRestApi, ownerUser);
		Response restResponse = sharedEntityRestApi.findByUser(0);
		Assert.assertEquals(403, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	@Test
	public void test028_getUsersWithPermissionShouldWork() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// ownerUser, with permission, find users shared by the entity with the following call getUsers,
		// response status code '200'
		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(sharedEntityResourceName,
				HyperIoTCrudAction.FIND);
		ownerUser = createHUser(action);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		// ownerUser share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = createSharedEntity(entityExample, ownerUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		this.impersonateUser(sharedEntityRestApi, ownerUser);
		Response restResponse = sharedEntityRestApi
				.getUsers(sharedEntity.getEntityResourceName(), sharedEntity.getEntityId());
		Assert.assertEquals(200, restResponse.getStatus());
		List<HUser> userList = restResponse.readEntity(new GenericType<List<HUser>>() {
		});
		Assert.assertFalse(userList.isEmpty());
		Assert.assertEquals(1, userList.size());
		Assert.assertEquals(huser.getId(), userList.get(0).getId());
		Assert.assertEquals(huser.getUsername(), userList.get(0).getUsername());
		Assert.assertEquals(huser.getEmail(), userList.get(0).getEmail());
	}


	@Test
	public void test029_getUsersWithoutPermissionShouldFail() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// ownerUser, without permission, tries to find users shared by the entity with the following call getUsers
		// response status code '200' HyperIoTUnauthorizedException
		ownerUser = createHUser(null);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		// ownerUser share his entity with huser
		HUser huser = createHUser(null);

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


	@Test
	public void test030_getUsersWithPermissionShouldWorkIfUserIsNotStoredInSharedEntity() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// ownerUser, with permission, find users shared by the entity with the following call getUsers,
		// user is not stored in SharedEntity table; this call return an empty list
		// response status code '200'
		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(sharedEntityResourceName,
				HyperIoTCrudAction.FIND);
		ownerUser = createHUser(action);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		this.impersonateUser(sharedEntityRestApi, ownerUser);
		Response restResponse = sharedEntityRestApi
				.getUsers(entityExampleResourceName, entityExample.getId());
		Assert.assertEquals(200, restResponse.getStatus());
		List<HUser> userList = restResponse.readEntity(new GenericType<List<HUser>>() {
		});
		Assert.assertTrue(userList.isEmpty());
		Assert.assertEquals(0, userList.size());
	}


	@Test
	public void test031_getUsersNotStoredInSharedEntityTableWithoutPermissionShouldFail() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// ownerUser, without permission, find users shared by the entity (user is not stored in SharedEntity table)
		// with the following call getUsers
		// response status code '403' HyperIoTUnauthorizedException
		ownerUser = createHUser(null);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		this.impersonateUser(sharedEntityRestApi, ownerUser);
		Response restResponse = sharedEntityRestApi
				.getUsers(entityExampleResourceName, entityExample.getId());
		Assert.assertEquals(403, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	@Test
	public void test032_deleteSharedEntityWithPermissionShouldWork() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// ownerUser, with permission, delete SharedEntity with the following call deleteSharedEntity
		// response status code '200'
		ownerUser = createHUser(null);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		// ownerUser share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = createSharedEntity(entityExample, ownerUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(entityExampleResourceName,
				HyperIoTShareAction.SHARE);
		addPermission(ownerUser, action);
		this.impersonateUser(sharedEntityRestApi, ownerUser);
		Response restResponse = sharedEntityRestApi.deleteSharedEntity(sharedEntity);
		Assert.assertEquals(200, restResponse.getStatus());
		Assert.assertNull(restResponse.getEntity());
	}


	@Test
	public void test033_deleteSharedEntityWithoutPermissionShouldFail() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// ownerUser, without permission, tries to delete SharedEntity with the following call deleteSharedEntity
		// response status code '403' HyperIoTUnauthorizedException
		ownerUser = createHUser(null);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		// ownerUser share his entity with huser
		HUser huser = createHUser(null);

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


	@Test
	public void test034_deleteSharedEntityWithPermissionShouldFailIfSharedEntityNotFound() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// ownerUser, with permission, tries to delete SharedEntity with the following call deleteSharedEntity
		// but SharedEntity isn't stored in database
		// response status code '404' HyperIoTEntityNotFound
		ownerUser = createHUser(null);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		// ownerUser share his entity with huser
		HUser huser = createHUser(null);

		// SharedEntity isn't stored in database
		SharedEntity sharedEntity = new SharedEntity();
		sharedEntity.setEntityId(entityExample.getId());
		sharedEntity.setEntityResourceName(entityExampleResourceName);
		sharedEntity.setUserId(huser.getId());

		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(entityExampleResourceName,
				HyperIoTShareAction.SHARE);
		addPermission(ownerUser, action);
		this.impersonateUser(sharedEntityRestApi, ownerUser);
		Response restResponse = sharedEntityRestApi.deleteSharedEntity(sharedEntity);
		Assert.assertEquals(404, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	@Test
	public void test035_deleteSharedEntityWithPermissionShouldFailIfPKNotFound() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// ownerUser, with permission, tries to delete SharedEntity with the following call deleteSharedEntity
		// but primary key not found
		// response status code '404' HyperIoTEntityNotFound
		ownerUser = createHUser(null);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		// ownerUser share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = createSharedEntity(entityExample, ownerUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		sharedEntity.setEntityId(0);
		Assert.assertEquals(0, sharedEntity.getEntityId());

		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(entityExampleResourceName,
				HyperIoTShareAction.SHARE);
		addPermission(ownerUser, action);
		this.impersonateUser(sharedEntityRestApi, ownerUser);
		Response restResponse = sharedEntityRestApi.deleteSharedEntity(sharedEntity);
		Assert.assertEquals(404, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	@Test
	public void test036_deleteSharedEntityWithPermissionShouldFailIfEntityResourceNameNotFound() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// ownerUser, with permission, tries to delete SharedEntity with the following call deleteSharedEntity
		// but entityResourceName not found
		// response status code '500' HyperIoTRuntimeException
		ownerUser = createHUser(null);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		// ownerUser share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = createSharedEntity(entityExample, ownerUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		sharedEntity.setEntityResourceName("entity.resource.name.not.found");
		Assert.assertEquals("entity.resource.name.not.found", sharedEntity.getEntityResourceName());

		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(entityExampleResourceName,
				HyperIoTShareAction.SHARE);
		addPermission(ownerUser, action);
		this.impersonateUser(sharedEntityRestApi, ownerUser);
		Response restResponse = sharedEntityRestApi.deleteSharedEntity(sharedEntity);
		Assert.assertEquals(500, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTRuntimeException",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
		Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().size());
		Assert.assertEquals("Entity " + sharedEntity.getEntityResourceName() + " is not a HyperIoTSharedEntity",
				((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(0));
	}


	@Test
	public void test037_deleteSharedEntityWithPermissionShouldFailIfUserNotFound() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// ownerUser, with permission, tries to delete SharedEntity with the following call deleteSharedEntity
		// but user not found
		// response status code '404' HyperIoTEntityNotFound
		ownerUser = createHUser(null);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		// ownerUser share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = createSharedEntity(entityExample, ownerUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		sharedEntity.setUserId(0);
		Assert.assertEquals(0, sharedEntity.getUserId());

		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(entityExampleResourceName,
				HyperIoTShareAction.SHARE);
		addPermission(ownerUser, action);
		this.impersonateUser(sharedEntityRestApi, ownerUser);
		Response restResponse = sharedEntityRestApi.deleteSharedEntity(sharedEntity);
		Assert.assertEquals(404, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	@Test
	public void test038_deleteSharedEntityWithPermissionShouldFailIfSharedEntityIsNull() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// ownerUser, with permission, tries to delete SharedEntity with the following call deleteSharedEntity
		// but shared entity is null
		// response status code '500' java.lang.NullPointerException
		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(entityExampleResourceName,
				HyperIoTShareAction.SHARE);
		ownerUser = createHUser(action);
		this.impersonateUser(sharedEntityRestApi, ownerUser);
		Response restResponse = sharedEntityRestApi.deleteSharedEntity(null);
		Assert.assertEquals(500, restResponse.getStatus());
		Assert.assertEquals("java.lang.NullPointerException",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	@Test
	public void test039_deleteSharedEntityShouldFailIfUser2IsNotUserOwnerOfEntity() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// huser, with permission, tries to remove SharedEntity with the following call deleteSharedEntity,
		// but the huser isn't the user owner of the entity
		// response status code '403' HyperIoTUnauthorizedException
		ownerUser = createHUser(null);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		// ownerUser share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = createSharedEntity(entityExample, ownerUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		// huser isn't the owner of the entity
		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(entityExampleResourceName,
				HyperIoTShareAction.SHARE);
		addPermission(huser, action);
		this.impersonateUser(sharedEntityRestApi, huser);
		Response restResponse = sharedEntityRestApi.deleteSharedEntity(sharedEntity);
		Assert.assertEquals(403, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	@Test
	public void test040_deleteSharedEntityNotFoundWithoutPermissionShouldFail() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// ownerUser, without permission, tries to delete SharedEntity (isn't stored in database)
		// with the following call deleteSharedEntity
		// response status code '403' HyperIoTUnauthorizedException
		ownerUser = createHUser(null);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		// ownerUser share his entity with huser
		HUser huser = createHUser(null);

		// SharedEntity isn't stored in database
		SharedEntity sharedEntity = new SharedEntity();
		sharedEntity.setEntityId(entityExample.getId());
		sharedEntity.setEntityResourceName(entityExampleResourceName);
		sharedEntity.setUserId(huser.getId());

		this.impersonateUser(sharedEntityRestApi, ownerUser);
		Response restResponse = sharedEntityRestApi.deleteSharedEntity(sharedEntity);
		Assert.assertEquals(403, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	@Test
	public void test041_deleteSharedEntityIfPKNotFoundWithoutPermissionShouldFail() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// ownerUser, without permission, tries to delete SharedEntity (primary key not found)
		// with the following call deleteSharedEntity
		// response status code '403' HyperIoTUnauthorizedException
		ownerUser = createHUser(null);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		// ownerUser share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = createSharedEntity(entityExample, ownerUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		sharedEntity.setEntityId(0);
		Assert.assertEquals(0, sharedEntity.getEntityId());

		this.impersonateUser(sharedEntityRestApi, ownerUser);
		Response restResponse = sharedEntityRestApi.deleteSharedEntity(sharedEntity);
		Assert.assertEquals(403, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	@Test
	public void test042_deleteSharedEntityIfResourceNameNotFoundWithoutPermissionShouldFail() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// ownerUser, without permission, tries to delete SharedEntity (entityResourceName not found)
		// with the following call deleteSharedEntity
		// response status code '500' HyperIoTRuntimeException
		ownerUser = createHUser(null);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		// ownerUser share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = createSharedEntity(entityExample, ownerUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		sharedEntity.setEntityResourceName("entity.resource.name.not.found");
		Assert.assertEquals("entity.resource.name.not.found", sharedEntity.getEntityResourceName());

		this.impersonateUser(sharedEntityRestApi, ownerUser);
		Response restResponse = sharedEntityRestApi.deleteSharedEntity(sharedEntity);
		Assert.assertEquals(500, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTRuntimeException",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
		Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().size());
		Assert.assertEquals("Entity " + sharedEntity.getEntityResourceName() + " is not a HyperIoTSharedEntity",
				((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(0));
	}


	@Test
	public void test043_deleteSharedEntityIfUserNotFoundWithoutPermissionShouldFail() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// ownerUser, without permission, tries to delete SharedEntity (user not found)
		// with the following call deleteSharedEntity
		// response status code '403' HyperIoTUnauthorizedException
		ownerUser = createHUser(null);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		// ownerUser share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = createSharedEntity(entityExample, ownerUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		sharedEntity.setUserId(0);
		Assert.assertEquals(0, sharedEntity.getUserId());

		this.impersonateUser(sharedEntityRestApi, ownerUser);
		Response restResponse = sharedEntityRestApi.deleteSharedEntity(sharedEntity);
		Assert.assertEquals(403, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	@Test
	public void test044_saveSharedEntityWithPermissionShouldFailIfHUserIdIsZero() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// ownerUser, with permission, tries to save SharedEntity with the following call saveSharedEntity,
		// but huser id is zero: isn't stored in database
		// response status code '404' HyperIoTEntityNotFound
		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(entityExampleResourceName,
				HyperIoTShareAction.SHARE);
		ownerUser = createHUser(action);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		//huser isn't stored in database: huser id is zero
		SharedEntity sharedEntity = new SharedEntity();
		sharedEntity.setEntityId(entityExample.getId());
		sharedEntity.setEntityResourceName(entityExampleResourceName);
		sharedEntity.setUserId(0); // huser: "ID: 0 \n User:null"

		this.impersonateUser(sharedEntityRestApi, ownerUser);
		Response restResponse = sharedEntityRestApi.saveSharedEntity(sharedEntity);
		Assert.assertEquals(404, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	@Test
	public void test045_saveSharedEntityWithPermissionShouldFailIfEntityResourceNameIsNull() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// ownerUser, with permission, tries to save SharedEntity with the following call saveSharedEntity,
		// but entityResourceName is null
		// response status code '422' HyperIoTValidationException
		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(entityExampleResourceName,
				HyperIoTShareAction.SHARE);
		ownerUser = createHUser(action);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		// ownerUser tries to share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = new SharedEntity();
		sharedEntity.setEntityId(entityExample.getId());
		sharedEntity.setEntityResourceName(null);
		sharedEntity.setUserId(huser.getId());

		this.impersonateUser(sharedEntityRestApi, ownerUser);
		Response restResponse = sharedEntityRestApi.saveSharedEntity(sharedEntity);
		Assert.assertEquals(403, restResponse.getStatus());
		Assert.assertEquals(0, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
	}


	@Test
	public void test046_saveSharedEntityWithPermissionShouldFailIfEntityResourceNameIsEmpty() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// ownerUser, with permission, tries to save SharedEntity with the following call saveSharedEntity,
		// but entityResourceName is empty
		// response status code '500' HyperIoTRuntimeException
		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(entityExampleResourceName,
				HyperIoTShareAction.SHARE);
		ownerUser = createHUser(action);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		// ownerUser tries to share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = new SharedEntity();
		sharedEntity.setEntityId(entityExample.getId());
		sharedEntity.setEntityResourceName("");
		sharedEntity.setUserId(huser.getId());

		this.impersonateUser(sharedEntityRestApi, ownerUser);
		Response restResponse = sharedEntityRestApi.saveSharedEntity(sharedEntity);
		Assert.assertEquals(500, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTRuntimeException",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
		Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().size());
		Assert.assertEquals("Entity " + sharedEntity.getEntityResourceName() + " is not a HyperIoTSharedEntity",
				((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(0));
	}


	@Test
	public void test047_saveSharedEntityWithPermissionShouldFailIfEntityResourceNameIsMaliciousCode() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// ownerUser, with permission, tries to save SharedEntity with the following call saveSharedEntity,
		// but entityResourceName is malicious code
		// response status code '500' HyperIoTRuntimeException
		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(entityExampleResourceName,
				HyperIoTShareAction.SHARE);
		ownerUser = createHUser(action);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		// ownerUser tries to share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = new SharedEntity();
		sharedEntity.setEntityId(entityExample.getId());
		sharedEntity.setEntityResourceName("javascript:");
		sharedEntity.setUserId(huser.getId());

		this.impersonateUser(sharedEntityRestApi, ownerUser);
		Response restResponse = sharedEntityRestApi.saveSharedEntity(sharedEntity);
		Assert.assertEquals(500, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTRuntimeException",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
		Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().size());
		Assert.assertEquals("Entity " + sharedEntity.getEntityResourceName() + " is not a HyperIoTSharedEntity",
				((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(0));
	}


	@Test
	public void test048_saveSharedEntityWithPermissionShouldFailIfEntityIdIsZero() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// ownerUser, with permission, tries to save SharedEntity with the following call saveSharedEntity,
		// but entityId is zero
		// response status code '404' HyperIoTEntityNotFound
		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(entityExampleResourceName,
				HyperIoTShareAction.SHARE);
		ownerUser = createHUser(action);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		// ownerUser tries to share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = new SharedEntity();
		sharedEntity.setEntityId(0);
		sharedEntity.setEntityResourceName(entityExampleResourceName);
		sharedEntity.setUserId(huser.getId());

		this.impersonateUser(sharedEntityRestApi, ownerUser);
		Response restResponse = sharedEntityRestApi.saveSharedEntity(sharedEntity);
		Assert.assertEquals(404, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	@Test
	public void test049_saveSharedEntityWithPermissionShouldFailIfEntityIsNotStoredInDatabase() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// ownerUser, with permission, tries to save SharedEntity with the following call saveSharedEntity,
		// but entity isn't stored in database
		// response status code '404' HyperIoTEntityNotFound
		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(entityExampleResourceName,
				HyperIoTShareAction.SHARE);
		ownerUser = createHUser(action);

		// hproject not stored in database
		SharedEntityExample entityExample = new SharedEntityExample();

		HUser huser = createHUser(null);

		SharedEntity sharedEntity = new SharedEntity();
		sharedEntity.setEntityId(entityExample.getId());
		sharedEntity.setEntityResourceName(entityExampleResourceName);
		sharedEntity.setUserId(huser.getId());

		this.impersonateUser(sharedEntityRestApi, ownerUser);
		Response restResponse = sharedEntityRestApi.saveSharedEntity(sharedEntity);
		Assert.assertEquals(404, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	@Test
	public void test050_saveSharedEntityWithPermissionShouldFailIfUser2IsNotOwnerOfEntity() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// huser2, with permission, tries to save SharedEntity with the following call saveSharedEntity,
		// but the huser2 isn't the user owner of the entity
		// response status code '403' HyperIoTUnauthorizedException
		ownerUser = createHUser(null);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		HUser huser2 = createHUser(null);

		SharedEntity sharedEntity = new SharedEntity();
		sharedEntity.setEntityId(entityExample.getId());
		sharedEntity.setEntityResourceName(entityExampleResourceName);
		sharedEntity.setUserId(huser2.getId());

		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(entityExampleResourceName,
				HyperIoTShareAction.SHARE);
		addPermission(huser2, action);
		this.impersonateUser(sharedEntityRestApi, huser2);
		Response restResponse = sharedEntityRestApi.saveSharedEntity(sharedEntity);
		Assert.assertEquals(403, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	@Test
	public void test051_saveSharedEntityWithoutPermissionShouldFailIfUser2IsNotOwnerOfEntity() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// huser2, without permission, tries to save SharedEntity with the following call saveSharedEntity,
		// but the huser2 isn't the user owner of the entity
		// response status code '403' HyperIoTUnauthorizedException
		ownerUser = createHUser(null);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		HUser huser2 = createHUser(null);

		SharedEntity sharedEntity = new SharedEntity();
		sharedEntity.setEntityId(entityExample.getId());
		sharedEntity.setEntityResourceName(entityExampleResourceName);
		sharedEntity.setUserId(huser2.getId());

		this.impersonateUser(sharedEntityRestApi, huser2);
		Response restResponse = sharedEntityRestApi.saveSharedEntity(sharedEntity);
		Assert.assertEquals(403, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	@Test
	public void test052_saveSharedEntityWithPermissionShouldFailIfEntityIsDuplicated() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// ownerUser, with permission, tries to save SharedEntity with the following call saveSharedEntity,
		// but SharedEntity is duplicated
		// response status code '422' HyperIoTDuplicateEntityException
		ownerUser = createHUser(null);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		// ownerUser share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = createSharedEntity(entityExample, ownerUser, huser);
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

		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(entityExampleResourceName,
				HyperIoTShareAction.SHARE);
		addPermission(ownerUser, action);
		this.impersonateUser(sharedEntityRestApi, ownerUser);
		Response restResponse = sharedEntityRestApi.saveSharedEntity(sharedEntityDuplicated);
		Assert.assertEquals(409, restResponse.getStatus());
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
	public void test053_triesToDuplicateSharedEntityWithoutPermissionShouldFail() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// ownerUser, without permission, tries to duplicate SharedEntity with the following call saveSharedEntity,
		// response status code '403' HyperIoTDuplicateEntityException
		ownerUser = createHUser(null);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		// ownerUser share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = createSharedEntity(entityExample, ownerUser, huser);
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

		this.impersonateUser(sharedEntityRestApi, ownerUser);
		Response restResponse = sharedEntityRestApi.saveSharedEntity(sharedEntityDuplicated);
		Assert.assertEquals(403, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	@Test
	public void test054_findAllSharedEntityPaginatedWithPermissionShouldWork() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// In this following call findAllSharedEntityPaginated, ownerUser find all SharedEntity with pagination
		// response status code '200'
		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(sharedEntityResourceName,
				HyperIoTCrudAction.FINDALL);
		ownerUser = createHUser(action);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());
		int delta = 5;
		int page = 2;

		// ownerUser share his entity with husers
		List<HUser> husers = new ArrayList<>();
		for (int i = 0; i < defaultDelta; i++) {
			HUser huser = createHUser(null);
			Assert.assertNotEquals(0, huser.getId());
			husers.add(huser);
		}
		Assert.assertEquals(defaultDelta, husers.size());

		List<SharedEntity> sharedEntities = new ArrayList<>();
		for (int i = 0; i < defaultDelta; i++) {
			SharedEntity sharedEntity = createSharedEntity(entityExample, ownerUser, husers.get(i));
			Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
			Assert.assertEquals(husers.get(i).getId(), sharedEntity.getUserId());
			Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());
			sharedEntities.add(sharedEntity);
		}
		Assert.assertEquals(defaultDelta, sharedEntities.size());
		this.impersonateUser(sharedEntityRestApi, ownerUser);
		Response restResponse = sharedEntityRestApi.findAllSharedEntityPaginated(delta, page);
		HyperIoTPaginableResult<SharedEntity> listSharedEntities = restResponse
				.readEntity(new GenericType<HyperIoTPaginableResult<SharedEntity>>() {
				});
		Assert.assertFalse(listSharedEntities.getResults().isEmpty());
		Assert.assertEquals(delta, listSharedEntities.getResults().size());
		Assert.assertEquals(delta, listSharedEntities.getDelta());
		Assert.assertEquals(page, listSharedEntities.getCurrentPage());
		Assert.assertEquals(defaultPage, listSharedEntities.getNextPage());
		// delta is 5, page 2: 10 entities stored in database
		Assert.assertEquals(2, listSharedEntities.getNumPages());
		Assert.assertEquals(200, restResponse.getStatus());
	}


	@Test
	public void test055_findAllSharedEntityPaginatedWithPermissionShouldWorkIfDeltaAndPageAreNull() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// In this following call findAllSharedEntityPaginated, ownerUser find all SharedEntity with pagination
		// if delta and page are null
		// response status code '200'
		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(sharedEntityResourceName,
				HyperIoTCrudAction.FINDALL);
		ownerUser = createHUser(action);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		Integer delta = null;
		Integer page = null;

		// ownerUser share his entities with husers
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
			SharedEntity sharedEntity = createSharedEntity(entityExample, ownerUser, husers.get(i));
			Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
			Assert.assertEquals(husers.get(i).getId(), sharedEntity.getUserId());
			Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());
			sharedEntities.add(sharedEntity);
		}
		Assert.assertEquals(numbEntities, sharedEntities.size());

		this.impersonateUser(sharedEntityRestApi, ownerUser);
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
	public void test056_findAllSharedEntityPaginatedWithPermissionShouldWorkIfDeltaIsLowerThanZero() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// In this following call findAllSharedEntityPaginated, ownerUser find all SharedEntity with pagination
		// if delta is lower than zero
		// response status code '200'
		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(sharedEntityResourceName,
				HyperIoTCrudAction.FINDALL);
		ownerUser = createHUser(action);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());
		int delta = -1;
		int page = 1;

		// ownerUser share his entities with husers
		List<HUser> husers = new ArrayList<>();
		int numbEntities = 14;
		for (int i = 0; i < numbEntities; i++) {
			HUser huser = createHUser(null);
			Assert.assertNotEquals(0, huser.getId());
			husers.add(huser);
		}
		Assert.assertEquals(numbEntities, husers.size());
		List<SharedEntity> sharedEntities = new ArrayList<>();
		for (int i = 0; i < numbEntities; i++) {
			SharedEntity sharedEntity = createSharedEntity(entityExample, ownerUser, husers.get(i));
			Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
			Assert.assertEquals(husers.get(i).getId(), sharedEntity.getUserId());
			Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());
			sharedEntities.add(sharedEntity);
		}
		Assert.assertEquals(numbEntities, sharedEntities.size());
		this.impersonateUser(sharedEntityRestApi, ownerUser);
		Response restResponsePage1 = sharedEntityRestApi.findAllSharedEntityPaginated(delta, page);
		HyperIoTPaginableResult<SharedEntity> listSharedEntitiesPage1 = restResponsePage1
				.readEntity(new GenericType<HyperIoTPaginableResult<SharedEntity>>() {
				});
		Assert.assertFalse(listSharedEntitiesPage1.getResults().isEmpty());
		Assert.assertEquals(defaultDelta, listSharedEntitiesPage1.getResults().size());
		Assert.assertEquals(defaultDelta, listSharedEntitiesPage1.getDelta());
		Assert.assertEquals(page, listSharedEntitiesPage1.getCurrentPage());
		Assert.assertEquals(page + 1, listSharedEntitiesPage1.getNextPage());
		// default delta is 10, page is 1: 14 entities stored in database
		Assert.assertEquals(2, listSharedEntitiesPage1.getNumPages());
		Assert.assertEquals(200, restResponsePage1.getStatus());

		//checks with page = 2
		this.impersonateUser(sharedEntityRestApi, ownerUser);
		Response restResponsePage2 = sharedEntityRestApi.findAllSharedEntityPaginated(delta, 2);
		HyperIoTPaginableResult<SharedEntity> listSharedEntitiesPage2 = restResponsePage2
				.readEntity(new GenericType<HyperIoTPaginableResult<SharedEntity>>() {
				});
		Assert.assertFalse(listSharedEntitiesPage2.getResults().isEmpty());
		Assert.assertEquals(numbEntities - defaultDelta, listSharedEntitiesPage2.getResults().size());
		Assert.assertEquals(defaultDelta, listSharedEntitiesPage2.getDelta());
		Assert.assertEquals(page + 1, listSharedEntitiesPage2.getCurrentPage());
		Assert.assertEquals(page, listSharedEntitiesPage2.getNextPage());
		// default delta is 10, page is 2: 14 entities stored in database
		Assert.assertEquals(2, listSharedEntitiesPage2.getNumPages());
		Assert.assertEquals(200, restResponsePage2.getStatus());
	}


	@Test
	public void test057_findAllSharedEntityPaginatedWithPermissionShouldWorkIfDeltaIsZero() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// In this following call findAllSharedEntityPaginated, ownerUser find all SharedEntity with pagination
		// if delta is zero
		// response status code '200'
		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(sharedEntityResourceName,
				HyperIoTCrudAction.FINDALL);
		ownerUser = createHUser(action);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());
		int delta = 0;
		int page = 2;

		// ownerUser share his entities with husers
		List<HUser> husers = new ArrayList<>();
		int numbEntities = 13;
		for (int i = 0; i < numbEntities; i++) {
			HUser huser = createHUser(null);
			Assert.assertNotEquals(0, huser.getId());
			husers.add(huser);
		}
		Assert.assertEquals(numbEntities, husers.size());
		List<SharedEntity> sharedEntities = new ArrayList<>();
		for (int i = 0; i < numbEntities; i++) {
			SharedEntity sharedEntity = createSharedEntity(entityExample, ownerUser, husers.get(i));
			Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
			Assert.assertEquals(husers.get(i).getId(), sharedEntity.getUserId());
			Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());
			sharedEntities.add(sharedEntity);
		}
		Assert.assertEquals(numbEntities, sharedEntities.size());
		this.impersonateUser(sharedEntityRestApi, ownerUser);
		Response restResponse = sharedEntityRestApi.findAllSharedEntityPaginated(delta, page);
		HyperIoTPaginableResult<SharedEntity> listSharedEntities = restResponse
				.readEntity(new GenericType<HyperIoTPaginableResult<SharedEntity>>() {
				});
		Assert.assertFalse(listSharedEntities.getResults().isEmpty());
		Assert.assertEquals(numbEntities - defaultDelta, listSharedEntities.getResults().size());
		Assert.assertEquals(defaultDelta, listSharedEntities.getDelta());
		Assert.assertEquals(page, listSharedEntities.getCurrentPage());
		Assert.assertEquals(defaultPage, listSharedEntities.getNextPage());
		// default delta is 10, page is 2: 13 entities stored in database
		Assert.assertEquals(2, listSharedEntities.getNumPages());
		Assert.assertEquals(200, restResponse.getStatus());

		//checks with page = 1
		this.impersonateUser(sharedEntityRestApi, ownerUser);
		Response restResponsePage1 = sharedEntityRestApi.findAllSharedEntityPaginated(delta, 1);
		HyperIoTPaginableResult<SharedEntity> listSharedEntitiesPage1 = restResponsePage1
				.readEntity(new GenericType<HyperIoTPaginableResult<SharedEntity>>() {
				});
		Assert.assertFalse(listSharedEntitiesPage1.getResults().isEmpty());
		Assert.assertEquals(defaultDelta, listSharedEntitiesPage1.getResults().size());
		Assert.assertEquals(defaultDelta, listSharedEntitiesPage1.getDelta());
		Assert.assertEquals(defaultPage, listSharedEntitiesPage1.getCurrentPage());
		Assert.assertEquals(page, listSharedEntitiesPage1.getNextPage());
		// default delta is 10, page is 1: 13 entities stored in database
		Assert.assertEquals(2, listSharedEntitiesPage1.getNumPages());
		Assert.assertEquals(200, restResponsePage1.getStatus());
	}


	@Test
	public void test058_findAllSharedEntityPaginatedWithPermissionShouldWorkIfPageIsLowerThanZero() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// In this following call findAllSharedEntityPaginated, ownerUser find all SharedEntity with pagination
		// if page is lower than zero
		// response status code '200'
		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(sharedEntityResourceName,
				HyperIoTCrudAction.FINDALL);
		ownerUser = createHUser(action);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		int delta = 7;
		int page = -1;

		// ownerUser share his entities with husers
		List<HUser> husers = new ArrayList<>();
		for (int i = 0; i < delta; i++) {
			HUser huser = createHUser(null);
			Assert.assertNotEquals(0, huser.getId());
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
		HyperIoTPaginableResult<SharedEntity> listSharedEntities = restResponse
				.readEntity(new GenericType<HyperIoTPaginableResult<SharedEntity>>() {
				});
		Assert.assertFalse(listSharedEntities.getResults().isEmpty());
		Assert.assertEquals(delta, listSharedEntities.getResults().size());
		Assert.assertEquals(delta, listSharedEntities.getDelta());
		Assert.assertEquals(defaultPage, listSharedEntities.getCurrentPage());
		Assert.assertEquals(defaultPage, listSharedEntities.getNextPage());
		// delta is 7, page 1: 7 entities stored in database
		Assert.assertEquals(1, listSharedEntities.getNumPages());
		Assert.assertEquals(200, restResponse.getStatus());
	}


	@Test
	public void test059_findAllSharedEntityPaginatedWithPermissionShouldWorkIfPageIsZero() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// In this following call findAllSharedEntityPaginated, ownerUser find all SharedEntity with pagination
		// if page is zero
		// response status code '200'
		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(sharedEntityResourceName,
				HyperIoTCrudAction.FINDALL);
		ownerUser = createHUser(action);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());
		int delta = 8;
		int page = 0;

		// ownerUser share his entities with husers
		List<HUser> husers = new ArrayList<>();
		for (int i = 0; i < defaultDelta; i++) {
			HUser huser = createHUser(null);
			Assert.assertNotEquals(0, huser.getId());
			husers.add(huser);
		}
		Assert.assertEquals(defaultDelta, husers.size());
		List<SharedEntity> sharedEntities = new ArrayList<>();
		for (int i = 0; i < defaultDelta; i++) {
			SharedEntity sharedEntity = createSharedEntity(entityExample, ownerUser, husers.get(i));
			Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
			Assert.assertEquals(husers.get(i).getId(), sharedEntity.getUserId());
			Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());
			sharedEntities.add(sharedEntity);
		}
		Assert.assertEquals(defaultDelta, sharedEntities.size());
		this.impersonateUser(sharedEntityRestApi, ownerUser);
		Response restResponse = sharedEntityRestApi.findAllSharedEntityPaginated(delta, page);
		HyperIoTPaginableResult<SharedEntity> listSharedEntities = restResponse
				.readEntity(new GenericType<HyperIoTPaginableResult<SharedEntity>>() {
				});
		Assert.assertFalse(listSharedEntities.getResults().isEmpty());
		Assert.assertEquals(delta, listSharedEntities.getResults().size());
		Assert.assertEquals(delta, listSharedEntities.getDelta());
		Assert.assertEquals(defaultPage, listSharedEntities.getCurrentPage());
		Assert.assertEquals(defaultPage + 1, listSharedEntities.getNextPage());
		// delta is 8, default page is 1: 10 entities stored in database
		Assert.assertEquals(2, listSharedEntities.getNumPages());
		Assert.assertEquals(200, restResponse.getStatus());

		//checks with page = 2
		this.impersonateUser(sharedEntityRestApi, ownerUser);
		Response restResponsePage2 = sharedEntityRestApi.findAllSharedEntityPaginated(delta, 2);
		HyperIoTPaginableResult<SharedEntity> listSharedEntitiesPage2 = restResponsePage2
				.readEntity(new GenericType<HyperIoTPaginableResult<SharedEntity>>() {
				});
		Assert.assertFalse(listSharedEntitiesPage2.getResults().isEmpty());
		Assert.assertEquals(defaultDelta - delta, listSharedEntitiesPage2.getResults().size());
		Assert.assertEquals(delta, listSharedEntitiesPage2.getDelta());
		Assert.assertEquals(defaultPage + 1, listSharedEntitiesPage2.getCurrentPage());
		Assert.assertEquals(defaultPage, listSharedEntitiesPage2.getNextPage());
		// delta is 8, page is 2: 10 entities stored in database
		Assert.assertEquals(2, listSharedEntitiesPage2.getNumPages());
		Assert.assertEquals(200, restResponsePage2.getStatus());
	}


	@Test
	public void test060_findAllSharedEntityPaginatedWithoutPermissionShouldFail() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// In this following call findAllSharedEntityPaginated, ownerUser, without permission,
		// tries to find all SharedEntity with pagination
		// response status code '403' HyperIoTUnauthorizedException
		ownerUser = createHUser(null);
		this.impersonateUser(sharedEntityRestApi, ownerUser);
		Response restResponse = sharedEntityRestApi.findAllSharedEntityPaginated(defaultDelta, defaultPage);
		Assert.assertEquals(403, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	@Test
	public void test061_deleteEntityExampleAndRemoveInCascadeSharedEntityTableRecords() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// ownerUser delete entityExample with the following call deleteSharedEntityExample;
		// this call delete in cascade mode SharedEntity because entityId is equals to entityExampleId
		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(entityExampleResourceName,
				HyperIoTCrudAction.REMOVE);
		ownerUser = createHUser(action);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		// ownerUser share his entity with huser
		HUser huser = createHUser(null);
		SharedEntity sharedEntity = createSharedEntity(entityExample, ownerUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		// this call delete in cascade mode record in SharedEntity table
		SharedEntityExampleRestApi entityRestApi = getOsgiService(SharedEntityExampleRestApi.class);
		this.impersonateUser(entityRestApi, ownerUser);
		Assert.assertEquals(sharedEntity.getEntityId(), entityExample.getId());
		Response responseDeleteEntity = entityRestApi.deleteSharedEntityExample(sharedEntity.getEntityId());
		Assert.assertEquals(200, responseDeleteEntity.getStatus());
		Assert.assertNull(responseDeleteEntity.getEntity());

		// checks: HUser is stored in database
		HyperIoTAction action2 = HyperIoTActionsUtil.getHyperIoTAction(hUserResourceName,
				HyperIoTCrudAction.FIND);
		addPermission(ownerUser, action2);
		HUserRestApi hUserRestApi = getOsgiService(HUserRestApi.class);
		this.impersonateUser(hUserRestApi, ownerUser);
		Assert.assertEquals(sharedEntity.getUserId(), huser.getId());
		Response responseFindHUser = hUserRestApi.findHUser(sharedEntity.getUserId());
		Assert.assertEquals(200, responseFindHUser.getStatus());
		Assert.assertEquals(huser.getId(), ((HUser) responseFindHUser.getEntity()).getId());

		// checks: SharedEntity isn't stored in database
		HyperIoTAction action3 = HyperIoTActionsUtil.getHyperIoTAction(sharedEntityResourceName,
				HyperIoTCrudAction.FIND);
		addPermission(ownerUser, action3);
		this.impersonateUser(sharedEntityRestApi, ownerUser);
		Response restResponse = sharedEntityRestApi.findByPK(sharedEntity);
		Assert.assertEquals(404, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	@Test
	public void test062_deleteHUserRemoveRecordInsideSharedEntityTable() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// ownerUser delete huser with call deleteHUser; this call remove in cascade record inside SharedEntity table
		// response status code '200'
		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(hUserResourceName,
				HyperIoTCrudAction.REMOVE);
		ownerUser = createHUser(action);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		// ownerUser share his entity with huser
		HUser huser = createHUser(null);
		SharedEntity sharedEntity = createSharedEntity(entityExample, ownerUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		// huser has been removed in SharedEntity table with deleteHUser call
		HUserRestApi hUserRestApi = getOsgiService(HUserRestApi.class);
		this.impersonateUser(hUserRestApi, ownerUser);
		Response responseDeleteHUser = hUserRestApi.deleteHUser(huser.getId());
		Assert.assertEquals(200, responseDeleteHUser.getStatus());
		Assert.assertNull(responseDeleteHUser.getEntity());

		// checks: record of SharedEntity has been deleted in database
		HyperIoTAction action2 = HyperIoTActionsUtil.getHyperIoTAction(sharedEntityResourceName,
				HyperIoTCrudAction.FIND);
		addPermission(ownerUser, action2);
		this.impersonateUser(sharedEntityRestApi, ownerUser);
		Response restResponse = sharedEntityRestApi.findByPK(sharedEntity);
		Assert.assertEquals(404, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	@Test
	public void test063_deleteSharedEntityNotDeleteInCascadeEntityExampleAndHUser() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// ownerUser delete SharedEntity with the following call deleteSharedEntity;
		// this call not delete in cascade mode entity example or huser
		// response status code '200'
		ownerUser = createHUser(null);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		// ownerUser share his entity with huser
		HUser huser = createHUser(null);
		SharedEntity sharedEntity = createSharedEntity(entityExample, ownerUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(entityExampleResourceName,
				HyperIoTShareAction.SHARE);
		addPermission(ownerUser, action);
		this.impersonateUser(sharedEntityRestApi, ownerUser);
		Response restResponse = sharedEntityRestApi.deleteSharedEntity(sharedEntity);
		Assert.assertEquals(200, restResponse.getStatus());
		Assert.assertNull(restResponse.getEntity());

		// checks: HUser is already stored in database
		HyperIoTAction action2 = HyperIoTActionsUtil.getHyperIoTAction(hUserResourceName,
				HyperIoTCrudAction.FIND);
		addPermission(ownerUser, action2);
		HUserRestApi hUserRestApi = getOsgiService(HUserRestApi.class);
		this.impersonateUser(hUserRestApi, ownerUser);
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Response responseFindHUser = hUserRestApi.findHUser(huser.getId());
		Assert.assertEquals(200, responseFindHUser.getStatus());
		Assert.assertEquals(huser.getId(), ((HUser) responseFindHUser.getEntity()).getId());

		// checks: SharedEntityExample is already stored in database
		HyperIoTAction action3 = HyperIoTActionsUtil.getHyperIoTAction(entityExampleResourceName,
				HyperIoTCrudAction.FIND);
		addPermission(ownerUser, action3);
		SharedEntityExampleRestApi entityExampleRestApi = getOsgiService(SharedEntityExampleRestApi.class);
		this.impersonateUser(entityExampleRestApi, ownerUser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Response responseFindEntityExample = entityExampleRestApi.findSharedEntityExample(sharedEntity.getEntityId());
		Assert.assertEquals(200, responseFindEntityExample.getStatus());
		Assert.assertEquals(entityExample.getId(), ((SharedEntityExample) responseFindEntityExample.getEntity()).getId());
	}


	@Test
	public void test064_huserFindSharedEntityExampleAfterSharedOperationShouldWork() {
		// ownerUser save SharedEntity with the following call saveSharedEntity, and
		// huser find entity example after shared entity operation
		// response status code '200'
		ownerUser = createHUser(null);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		// ownerUser share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = createSharedEntity(entityExample, ownerUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		SharedEntityExampleRestApi entityExampleRestApi = getOsgiService(SharedEntityExampleRestApi.class);
		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(entityExampleResourceName,
				HyperIoTCrudAction.FIND);
		addPermission(huser, action);
		// add specific permission with resourceId
		addSpecificPermission(huser, action, entityExample.getId());

		this.impersonateUser(entityExampleRestApi, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Response restResponse = entityExampleRestApi.findSharedEntityExample(sharedEntity.getEntityId());
		Assert.assertEquals(200, restResponse.getStatus());
		Assert.assertEquals(entityExample.getId(), ((SharedEntityExample) restResponse.getEntity()).getId());
		Assert.assertEquals(ownerUser.getId(), ((SharedEntityExample) restResponse.getEntity()).getUser().getId());
	}


	@Test
	public void test065_huserWithoutPermissionTriesToFindEntityExampleSharedAfterSharedOperationShouldFail() {
		// hadmin save SharedEntity with the following call saveSharedEntity.
		// huser, without permission, tries to find Entity Example after shared entity operation
		// response status code '403' HyperIoTUnauthorizedException
		ownerUser = createHUser(null);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		// ownerUser share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = createSharedEntity(entityExample, ownerUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		SharedEntityExampleRestApi entityExampleRestApi = getOsgiService(SharedEntityExampleRestApi.class);
		// huser tries to find Entity Example after shared operation
		this.impersonateUser(entityExampleRestApi, huser);
		Response restResponse = entityExampleRestApi.findSharedEntityExample(entityExample.getId());
		Assert.assertEquals(403, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	@Test
	public void test066_huserWithoutSpecificPermissionTriesToFindEntityExampleSharedAfterSharedOperationShouldFail() {
		// hadmin save SharedEntity with the following call saveSharedEntity.
		// huser, without specific permission, tries to find Entity Example after shared entity operation
		// response status code '403' HyperIoTUnauthorizedException
		ownerUser = createHUser(null);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		// ownerUser share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = createSharedEntity(entityExample, ownerUser, huser);
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
		addPermission(huser, action);
		this.impersonateUser(entityExampleRestApi, huser);
		Response restResponse = entityExampleRestApi.findSharedEntityExample(entityExample.getId());
		Assert.assertEquals(403, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	@Test
	public void test067_huser2NotInsertedInSharedEntityTableTriesToFindEntityExampleShouldFail() {
		// huser2, with permission, tries to find Entity Example shared with the following call findSharedEntityExample
		// huser2 has permission (FIND) and specific permission but it's unauthorized because isn't associated with
		// entity example shared
		// response status code '403' HyperIoTUnauthorizedException
		ownerUser = createHUser(null);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		// ownerUser share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = createSharedEntity(entityExample, ownerUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(entityExampleResourceName,
				HyperIoTCrudAction.FIND);
		// huser2 it's unauthorized: it isn't associated with entity example shared
		HUser huser2 = createHUser(null);
		addPermission(huser, action);
		// add specific permission with resourceId
		addSpecificPermission(huser, action, entityExample.getId());

		Assert.assertNotEquals(huser2.getId(), ownerUser.getId());
		Assert.assertNotEquals(huser2.getId(), huser.getId());

		SharedEntityExampleRestApi entityExampleRestApi = getOsgiService(SharedEntityExampleRestApi.class);
		this.impersonateUser(entityExampleRestApi, huser2);
		Response restResponse = entityExampleRestApi.findSharedEntityExample(entityExample.getId());
		Assert.assertEquals(404, restResponse.getStatus());
	}


	@Test
	public void test068_huserFindAllSharedEntityExampleAfterSharedOperationShouldWork() {
		// ownerUser save SharedEntity with the following call saveSharedEntity, and
		// huser find all entity example after shared entity operation
		// response status code '200'
		ownerUser = createHUser(null);
		SharedEntityExample entityExample1 = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample1.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample1.getUser().getId());

		SharedEntityExample entityExample2 = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample2.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample2.getUser().getId());

		// entity example is not shared
		SharedEntityExample entityExampleNotShared = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExampleNotShared.getId());
		Assert.assertEquals(ownerUser.getId(), entityExampleNotShared.getUser().getId());

		// ownerUser share his entities with huser
		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(entityExampleResourceName,
				HyperIoTCrudAction.FINDALL);
		HUser huser = createHUser(action);

		SharedEntity sharedEntity1 = createSharedEntity(entityExample1, ownerUser, huser);
		Assert.assertEquals(entityExample1.getId(), sharedEntity1.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity1.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity1.getEntityResourceName());

		// second SharedEntity
		SharedEntity sharedEntity2 = createSharedEntity(entityExample2, ownerUser, huser);
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
				Assert.assertEquals(ownerUser.getId(), se.getUser().getId());
				entityFound1 = true;
			}
			if (entityExample2.getId() == se.getId()) {
				Assert.assertEquals(ownerUser.getId(), se.getUser().getId());
				entityFound2 = true;
			}
		}
		Assert.assertTrue(entityFound1);
		Assert.assertTrue(entityFound2);
		Assert.assertEquals(200, responseFindAllEntityExample.getStatus());
	}


	@Test
	public void test069_huser2TriesToFindAllSharedEntityExampleShouldFail() {
		// ownerUser save SharedEntity with the following call saveSharedEntity, and
		// huser2 tries to find all shared entities example; this call return an empty list because
		// huser2 isn't associated in shared entity example
		// response status code '200'
		ownerUser = createHUser(null);
		SharedEntityExample entityExample1 = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample1.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample1.getUser().getId());

		SharedEntityExample entityExample2 = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample2.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample2.getUser().getId());

		// ownerUser share his entities with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity1 = createSharedEntity(entityExample1, ownerUser, huser);
		Assert.assertEquals(entityExample1.getId(), sharedEntity1.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity1.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity1.getEntityResourceName());

		// second SharedEntity
		SharedEntity sharedEntity2 = createSharedEntity(entityExample2, ownerUser, huser);
		Assert.assertEquals(entityExample2.getId(), sharedEntity2.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity2.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity2.getEntityResourceName());

		// huser2 is not associated with shared entity
		SharedEntityExampleRestApi entityExampleRestApi = getOsgiService(SharedEntityExampleRestApi.class);
		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(entityExampleResourceName,
				HyperIoTCrudAction.FINDALL);
		HUser huser2 = createHUser(action);
		this.impersonateUser(entityExampleRestApi, huser2);
		Response responseFindAllEntityExample = entityExampleRestApi.findAllSharedEntityExample();
		List<SharedEntityExample> listEntities = responseFindAllEntityExample.readEntity(new GenericType<List<SharedEntityExample>>() {
		});
		Assert.assertTrue(listEntities.isEmpty());
		Assert.assertEquals(0, listEntities.size());
		Assert.assertEquals(200, responseFindAllEntityExample.getStatus());
	}


	@Test
	public void test070_huserUpdateSharedEntityExampleAfterSharedOperationShouldWork() {
		// ownerUser save SharedEntity with the following call saveSharedEntity, and
		// huser update entity example after shared entity operation
		// response status code '200'
		ownerUser = createHUser(null);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		// ownerUser share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = createSharedEntity(entityExample, ownerUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(entityExampleResourceName,
				HyperIoTCrudAction.UPDATE);
		addPermission(huser, action);
		// add specific permission with resourceId
		addSpecificPermission(huser, action, entityExample.getId());

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
		Assert.assertEquals(ownerUser.getId(),
				((SharedEntityExample) restResponseUpdateEntityExample.getEntity()).getUser().getId());
	}


	@Test
	public void test071_huserWithoutPermissionTriesToUpdateEntitySharedAfterSharedOperationShouldFail() {
		// ownerUser save SharedEntity with the following call saveSharedEntity,
		// huser, without permission, tries to update Entity Example after shared entity operation
		// response status code '403' HyperIoTUnauthorizedException
		ownerUser = createHUser(null);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		// ownerUser share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = createSharedEntity(entityExample, ownerUser, huser);
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
	public void test072_huserWithoutSpecificPermissionTriesToUpdateEntitySharedAfterSharedOperationShouldFail() {
		// ownerUser save SharedEntity with the following call saveSharedEntity,
		// huser, without specific permission, tries to update Entity Example after shared entity operation
		// response status code '403' HyperIoTUnauthorizedException
		ownerUser = createHUser(null);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		// ownerUser share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = createSharedEntity(entityExample, ownerUser, huser);
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
		addPermission(huser, action);
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
	public void test073_huser2TriesToUpdateSharedEntityExampleShouldFail() {
		// ownerUser save SharedEntity with the following call saveSharedEntity, and
		// huser2 tries to update entity example but isn't associated in shared entity
		// response status code '403' HyperIoTUnauthorizedException
		ownerUser = createHUser(null);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		// ownerUser share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = createSharedEntity(entityExample, ownerUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		// huser2 tries to update entity example but isn't associated in shared entity
		SharedEntityExampleRestApi entityExampleRestApi = getOsgiService(SharedEntityExampleRestApi.class);
		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(entityExampleResourceName,
				HyperIoTCrudAction.UPDATE);
		HUser huser2 = createHUser(null);
		addPermission(huser2, action);
		// add specific permission with resourceId
		addSpecificPermission(huser2, action, entityExample.getId());

		entityExample.setDescription(huser2.getUsername() + " tries to edit description");
		this.impersonateUser(entityExampleRestApi, huser2);
		Response restResponseUpdateEntityExample = entityExampleRestApi.updateSharedEntityExample(entityExample);
		Assert.assertEquals(403, restResponseUpdateEntityExample.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
				((HyperIoTBaseError) restResponseUpdateEntityExample.getEntity()).getType());
	}


	@Test
	public void test074_huserDeleteSharedEntityExampleAfterSharedOperationShouldWork() {
		SharedEntityRestApi sharedEntityRestApi = getOsgiService(SharedEntityRestApi.class);
		// ownerUser save SharedEntity with the following call saveSharedEntity, and
		// huser delete entity example after shared operation
		// response status code '200'
		ownerUser = createHUser(null);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		// ownerUser share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = createSharedEntity(entityExample, ownerUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		SharedEntityExampleRestApi entityExampleRestApi = getOsgiService(SharedEntityExampleRestApi.class);
		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(entityExampleResourceName,
				HyperIoTCrudAction.REMOVE);
		addPermission(huser, action);
		// add specific permission with resourceId
		addSpecificPermission(huser, action, entityExample.getId());

		this.impersonateUser(entityExampleRestApi, huser);
		Response restResponseDeleteEntityExample = entityExampleRestApi.deleteSharedEntityExample(entityExample.getId());
		Assert.assertEquals(200, restResponseDeleteEntityExample.getStatus());
		Assert.assertNull(restResponseDeleteEntityExample.getEntity());
	}


	@Test
	public void test075_huserWithoutPermissionTriesToDeleteEntityExampleAfterSharedOperationShouldFail() {
		// ownerUser save SharedEntity with the following call saveSharedEntity, and
		// huser, without permission, tries to delete Entity Example after shared entity operation
		// response status code '403' HyperIoTUnauthorizedException
		ownerUser = createHUser(null);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		// ownerUser share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = createSharedEntity(entityExample, ownerUser, huser);
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
	public void test076_huserWithoutSpecificPermissionTriesToDeleteEntityExampleAfterSharedOperationShouldFail() {
		// ownerUser save SharedEntity with the following call saveSharedEntity, and
		// huser, without specific permission, tries to delete Entity Example after shared entity operation
		// response status code '403' HyperIoTUnauthorizedException
		ownerUser = createHUser(null);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		// ownerUser share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = createSharedEntity(entityExample, ownerUser, huser);
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
		addPermission(huser, action);
		this.impersonateUser(entityExampleRestApi, huser);
		Response restResponse = entityExampleRestApi.deleteSharedEntityExample(entityExample.getId());
		Assert.assertEquals(403, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	@Test
	public void test077_huser2TriesToDeleteSharedEntityShouldFailIfItIsNotOwnerOrInsertedInSameSharedEntity() {
		// ownerUser save SharedEntity with the following call saveSharedEntity, and
		// huser2 tries to delete entity example, after shared operation, with the following call deleteSharedEntityExample,
		// huser2 has permission (REMOVE) but it's unauthorized because isn't associated with shared entity
		// response status code '403' HyperIoTUnauthorizedException
		ownerUser = createHUser(null);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		// ownerUser share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = createSharedEntity(entityExample, ownerUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		SharedEntityExampleRestApi entityExampleRestApi = getOsgiService(SharedEntityExampleRestApi.class);
		// huser2 isn't associated in SharedEntity and isn't the owner hproject
		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(entityExampleResourceName,
				HyperIoTCrudAction.REMOVE);
		HUser huser2 = createHUser(action);
		addPermission(huser2, action);
		// add specific permission with resourceId
		addSpecificPermission(huser2, action, entityExample.getId());
		this.impersonateUser(entityExampleRestApi, huser2);
		Response restResponse = entityExampleRestApi.deleteSharedEntityExample(entityExample.getId());
		Assert.assertEquals(404, restResponse.getStatus());
	}


	@Test
	public void test078_huserUpdateSharedEntityExampleAfterSharedOperationShouldFailIfSetUserNull() {
		// ownerUser save SharedEntity with the following call saveSharedEntity; after shared operation
		// huser tries to set userId (of shared entity example) to null with the following call updateSharedEntityExample
		// response status code '422' HyperIoTValidationException
		ownerUser = createHUser(null);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		// ownerUser share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = createSharedEntity(entityExample, ownerUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		SharedEntityExampleRestApi entityExampleRestApi = getOsgiService(SharedEntityExampleRestApi.class);
		entityExample.setUser(null);

		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(entityExampleResourceName,
				HyperIoTCrudAction.UPDATE);
		addPermission(huser, action);
		// add specific permission with resourceId
		addSpecificPermission(huser, action, entityExample.getId());

		this.impersonateUser(entityExampleRestApi, huser);
		Response restResponse = entityExampleRestApi.updateSharedEntityExample(entityExample);
		Assert.assertEquals(422, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
		Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
		Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
		Assert.assertEquals("sharedentityexample-user", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
	}


	@Test
	public void test079_huser2TriesToBecomeNewOwnerOfSharedEntityExampleShouldFail() {
		// ownerUser save SharedEntity with the following call saveSharedEntity, and
		// huser2 tries to become the new owner of shared entity example:
		// huser2 isn't associated with hproject
		// response status code '403' HyperIoTUnauthorizedException
		ownerUser = createHUser(null);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		// ownerUser share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = createSharedEntity(entityExample, ownerUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		SharedEntityExampleRestApi entityExampleRestApi = getOsgiService(SharedEntityExampleRestApi.class);
		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(entityExampleResourceName,
				HyperIoTCrudAction.UPDATE);
		HUser huser2 = createHUser(null);
		addPermission(huser2, action);
		// add specific permission with resourceId
		addSpecificPermission(huser2, action, entityExample.getId());

		entityExample.setUser(huser2);
		this.impersonateUser(entityExampleRestApi, huser2);
		Response restResponse = entityExampleRestApi.updateSharedEntityExample(entityExample);
		Assert.assertEquals(403, restResponse.getStatus());
		Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
				((HyperIoTBaseError) restResponse.getEntity()).getType());
	}


	@Test
	public void test080_huserInsertedInSharedEntityTriesToBecomeNewOwnerOfSharedEntityExampleShouldFail() {
		// ownerUser save SharedEntity with the following call saveSharedEntity, after shared operation
		// huser tries to be owner of shared entity example with the following call updateSharedEntityExample
		// response status code '403' HyperIoTUnauthorizedException
		ownerUser = createHUser(null);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		// ownerUser share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = createSharedEntity(entityExample, ownerUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		// huser tries to be owner of shared entity example with the following call updateSharedEntityExample
		SharedEntityExampleRestApi entityExampleRestApi = getOsgiService(SharedEntityExampleRestApi.class);
		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(entityExampleResourceName,
				HyperIoTCrudAction.UPDATE);
		addPermission(huser, action);
		// add specific permission with resourceId
		addSpecificPermission(huser, action, entityExample.getId());

		entityExample.setUser(huser);
		this.impersonateUser(entityExampleRestApi, huser);
		// user on shared resource cannot change the owner
		Response restResponse = entityExampleRestApi.updateSharedEntityExample(entityExample);
		Assert.assertEquals(200, restResponse.getStatus());
		Assert.assertEquals(ownerUser.getId(), ((SharedEntityExample) restResponse.getEntity()).getUser().getId());
	}


	@Test
	public void test081_huserTriesToMakeHUser2NewOwnerOfSharedEntityExampleAfterSharedOperationShouldFail() {
		// ownerUser save SharedEntity with the following call saveSharedEntity, and
		// huser tries to make huser2 the new owner of shared entity example;
		// huser2 isn't associated in SharedEntity and isn't the owner of shared entity example
		// response status code '403' HyperIoTUnauthorizedException
		ownerUser = createHUser(null);
		SharedEntityExample entityExample = createSharedEntityExample(ownerUser);
		Assert.assertNotEquals(0, entityExample.getId());
		Assert.assertEquals(ownerUser.getId(), entityExample.getUser().getId());

		// ownerUser share his entity with huser
		HUser huser = createHUser(null);

		SharedEntity sharedEntity = createSharedEntity(entityExample, ownerUser, huser);
		Assert.assertEquals(entityExample.getId(), sharedEntity.getEntityId());
		Assert.assertEquals(huser.getId(), sharedEntity.getUserId());
		Assert.assertEquals(entityExampleResourceName, sharedEntity.getEntityResourceName());

		SharedEntityExampleRestApi entityExampleRestApi = getOsgiService(SharedEntityExampleRestApi.class);
		// huser2 isn't associated in SharedEntity and isn't the owner hproject
		HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(entityExampleResourceName,
				HyperIoTCrudAction.UPDATE);
		addPermission(huser, action);
		// add specific permission with resourceId
		addSpecificPermission(huser, action, entityExample.getId());

		HUser huser2 = createHUser(null);
		entityExample.setUser(huser2);
		this.impersonateUser(entityExampleRestApi, huser);
		// user on shared resource cannot change the owner
		Response restResponse = entityExampleRestApi.updateSharedEntityExample(entityExample);
		Assert.assertEquals(200, restResponse.getStatus());
		Assert.assertEquals(ownerUser.getId(), ((SharedEntityExample) restResponse.getEntity()).getUser().getId());
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
			Permission permission = utilGrantPermission(huser, role, action);
			Assert.assertNotEquals(0, permission.getId());
			Assert.assertEquals(entityExampleResourceName + " assigned to huser_id " + huser.getId(), permission.getName());
			Assert.assertEquals(action.getActionId(), permission.getActionIds());
			Assert.assertEquals(action.getCategory(), permission.getEntityResourceName());
			Assert.assertEquals(role.getId(), permission.getRole().getId());
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


	/*
	 *
	 *
	 * UTILITY METHODS: for testing specific permissions
	 *
	 *
	 */

	private Permission addSpecificPermission(HUser huser, HyperIoTAction action, long resourceId) {
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
		Role role = createRole();
		huser.addRole(role);
		RoleRestApi roleRestApi = getOsgiService(RoleRestApi.class);
		this.impersonateUser(roleRestApi, adminUser);
		Response restUserRole = roleRestApi.saveUserRole(role.getId(), huser.getId());
		Assert.assertEquals(200, restUserRole.getStatus());
		Assert.assertTrue(huser.hasRole(role));
		Permission permission = utilGrantSpecificPermission(huser, role, action, resourceId);
		return permission;
	}

	private Permission utilGrantSpecificPermission(HUser huser, Role role, HyperIoTAction action, long resourceId) {
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
		PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
		Permission permission = new Permission();
		permission.setName(entityExampleResourceName + " assigned to huser_id " + huser.getId());
		permission.setActionIds(action.getActionId());
		permission.setEntityResourceName(action.getResourceName());
		permission.setResourceId(resourceId);
		permission.setRole(role);
		this.impersonateUser(permissionRestApi, adminUser);
		Response restResponse = permissionRestApi.savePermission(permission);
		Assert.assertEquals(200, restResponse.getStatus());
		Assert.assertTrue(huser.hasRole(role.getId()));
		return permission;
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
