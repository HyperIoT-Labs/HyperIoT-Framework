package it.acsoftware.hyperiot.authentication.test;

import it.acsoftware.hyperiot.base.api.authentication.AuthenticationApi;
import it.acsoftware.hyperiot.base.api.HyperIoTUser;
import it.acsoftware.hyperiot.base.test.HyperIoTTestConfigurationBuilder;
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

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HyperIoTAuthenticationServiceTest extends KarafTestSupport {

	//force global configuration
	public Option[] config() {
		return null;
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
		String datasource = executeCommand("jdbc:ds-list");
//		System.out.println(executeCommand("bundle:list | grep HyperIoT"));
		assertContains("hyperiot", datasource);
	}

	@Test
	public void test01_loginApiShouldSuccess() {
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser loginUser = (HUser) authService.login("hadmin", "admin");
		Assert.assertNotNull(authService);
		Assert.assertNotNull(loginUser);
	}

	@Test
	public void test02_loginApiShouldFailIfInsertingBadCredential() {
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser loginUser = (HUser) authService.login("wrongUsername", "wrongPassword");
		Assert.assertNotNull(authService);
		Assert.assertNull(loginUser);
	}

	@Test
	public void test03_loginApiShouldFailIfUserIsWrong() {
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser loginUser = (HUser) authService.login("wrongUsername", "admin");
		Assert.assertNotNull(authService);
		Assert.assertNull(loginUser);
	}

	@Test
	public void test04_loginApiShouldFailIfPasswordIsWrong() {
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser loginUser = (HUser) authService.login("hadmin", "wrongPassword");
		Assert.assertNotNull(authService);
		Assert.assertNull(loginUser);
	}

	@Test
	public void test05_generateTokenApiShouldSuccess() {
		AuthenticationApi authSystemService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser loginUser = (HUser) authSystemService.login("hadmin", "admin");
		String token = authSystemService.generateToken(loginUser);
		Assert.assertNotNull(authSystemService);
		Assert.assertNotNull(loginUser);
		Assert.assertNotNull(token);
	}

	@Test
	public void test06_loginApiShouldFailIfUserInsertingHashedPassword() {
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser loginUser = (HUser) authService.login("hadmin", "ISMvKXpXpadDiUoOSoAfww==");
		Assert.assertNotNull(authService);
		Assert.assertNull(loginUser);
	}

	@Test
	public void test07_loginApiWithEmailShouldSuccess() {
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser loginUser = (HUser) authService.login("hadmin@hyperiot.com", "admin");
		Assert.assertNotNull(authService);
		Assert.assertNotNull(loginUser);
	}

	@Test
	public void test08_loginApiWithEmailShouldFailIfPasswordIsWrong() {
		AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
		HyperIoTUser loginUser = (HUser) authService.login("hadmin@hyperiot.com", "wrong=");
		Assert.assertNotNull(authService);
		Assert.assertNull(loginUser);
	}

}
