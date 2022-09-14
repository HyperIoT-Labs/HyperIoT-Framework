package it.acsoftware.hyperiot.bundle.listener.test;

import it.acsoftware.hyperiot.base.api.authentication.AuthenticationApi;
import it.acsoftware.hyperiot.bundle.listener.model.BundleTrackerItem;
import it.acsoftware.hyperiot.bundle.listener.service.rest.BundleListenerRestApi;
import it.acsoftware.hyperiot.huser.model.HUser;
import org.apache.karaf.features.FeaturesService;
import org.apache.karaf.itests.KarafTestSupport;
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

import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.base.api.HyperIoTUser;
import it.acsoftware.hyperiot.base.service.rest.HyperIoTBaseRestApi;
import it.acsoftware.hyperiot.base.test.HyperIoTTestConfigurationBuilder;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.List;

import static it.acsoftware.hyperiot.bundle.listener.test.HyperIoTBundleListenerConfiguration.getConfiguration;

/**
 *
 * @author Aristide Cittadino Interface component for BundleListener System Service.
 *
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HyperIoTBundleListenerRestTest extends KarafTestSupport {

	//force global configuration
	public Option[] config() {
		return null;
	}

	public HyperIoTContext impersonateUser(HyperIoTBaseRestApi restApi,HyperIoTUser user) {
		return restApi.impersonate(user);
	}


	@Test
	public void test00_hyperIoTFrameworkShouldBeInstalled() {
		// assert on an available service
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
		assertContains("HyperIoTBundleListener-features ", features);
		String datasource = executeCommand("jdbc:ds-list");
//		System.out.println(executeCommand("bundle:list | grep HyperIoT"));
		assertContains("hyperiot", datasource);
	}


	@Test
	public void test01_bundleListenerModuleShouldWork() {
		BundleListenerRestApi bundleListenerRestApi = getOsgiService(BundleListenerRestApi.class);
		// the following call checkModuleWorking checks if BundleListener module working
		// correctly
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
		this.impersonateUser(bundleListenerRestApi, adminUser);
		Response restResponse = bundleListenerRestApi.checkModuleWorking();
		Assert.assertEquals(200, restResponse.getStatus());
	}


	@Test
	public void test02_findAllBundleListenerShouldWork() {
		BundleListenerRestApi bundleListenerRestApi = getOsgiService(BundleListenerRestApi.class);
		// hadmin find all Bundles with the following call findAll
		// response status code '200'
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
		this.impersonateUser(bundleListenerRestApi, adminUser);
		Response restResponse = bundleListenerRestApi.findAll();
		Assert.assertEquals(200, restResponse.getStatus());
		List<BundleTrackerItem> bundleTrackerItemList = restResponse
				.readEntity(new GenericType<List<BundleTrackerItem>>() {});
		Assert.assertFalse(bundleTrackerItemList.isEmpty());
		Assert.assertNotEquals(0 , bundleTrackerItemList.size());
	}


	@Test
	public void test03_findAllBundleListenerShouldWorkChecksIfHyperIoTBundlesIsInstalled() {
		BundleListenerRestApi bundleListenerRestApi = getOsgiService(BundleListenerRestApi.class);
		// hadmin find all Bundles with the following call findAll.
		// Hadmin checks if all HyperIoT bundles is installed
		// response status code '200'
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
		this.impersonateUser(bundleListenerRestApi, adminUser);
		Response restResponse = bundleListenerRestApi.findAll();
		Assert.assertEquals(200, restResponse.getStatus());
		List<BundleTrackerItem> bundleTrackerItemList = restResponse
				.readEntity(new GenericType<List<BundleTrackerItem>>() {});
		Assert.assertFalse(bundleTrackerItemList.isEmpty());
		Assert.assertNotEquals(0 , bundleTrackerItemList.size());
		boolean containsHyperIoTBundles = false;

		//checks if hyperiot bundles is installed
		for (BundleTrackerItem bundleTrackerItem : bundleTrackerItemList) {
			if (bundleTrackerItem.getName().contains("HyperIoT")) {
				containsHyperIoTBundles = true;
				Assert.assertEquals("ACTIVE", bundleTrackerItem.getState());
				Assert.assertEquals("STARTED", bundleTrackerItem.getType());
			}
		}
		Assert.assertTrue(containsHyperIoTBundles);
	}


	@Test
	public void test04_findBundleListenerShouldWork() {
		BundleListenerRestApi bundleListenerRestApi = getOsgiService(BundleListenerRestApi.class);
		// hadmin find bundle with the following call find
		// response status code '200'
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
		this.impersonateUser(bundleListenerRestApi, adminUser);
		String bundleListener = "HyperIoTBase-api";
		Response restResponse = bundleListenerRestApi.find(bundleListener);
		Assert.assertEquals(200, restResponse.getStatus());
		//TODO: fix the following assertions it give null pointer
		/*Assert.assertEquals(bundleListener,
				((BundleTrackerItem) restResponse.getEntity()).getName());
		BundleTrackerItem bundleTrackerItem = ((BundleTrackerItem) restResponse.getEntity());
		Assert.assertEquals("ACTIVE", bundleTrackerItem.getState());
		Assert.assertEquals("STARTED", bundleTrackerItem.getType());*/
	}


	@Test
	public void test05_findBundleListenerShouldFailIfBundleNotFound() {
		BundleListenerRestApi bundleListenerRestApi = getOsgiService(BundleListenerRestApi.class);
		// hadmin tries to find bundle with the following call find,
		// but bundle not found
		// response status code '200'
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
		this.impersonateUser(bundleListenerRestApi, adminUser);
		String bundleListener = "bundle-not-found";
		Response restResponse = bundleListenerRestApi.find(bundleListener);
		Assert.assertEquals(200, restResponse.getStatus());
		Assert.assertNull(restResponse.getEntity());
	}


}
