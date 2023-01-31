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

package it.acsoftware.hyperiot.authentication.test;

import it.acsoftware.hyperiot.base.api.authentication.AuthenticationSystemApi;
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
public class HyperIoTAuthenticationSystemServiceTest extends KarafTestSupport {

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
	public void test01_loginSystemApiShouldSuccess() {
		AuthenticationSystemApi authSystemService = getOsgiService(AuthenticationSystemApi.class);
		HyperIoTUser loginUser = (HUser) authSystemService.login("hadmin", "admin");
		Assert.assertNotNull(authSystemService);
		Assert.assertNotNull(loginUser);
	}

	@Test
	public void test02_loginSystemApiShouldFailIfInsertingBadCredential() {
		AuthenticationSystemApi authSystemService = getOsgiService(AuthenticationSystemApi.class);
		HyperIoTUser loginUser = (HUser) authSystemService.login("wrongUsername", "wrongPassword");
		Assert.assertNotNull(authSystemService);
		Assert.assertNull(loginUser);
	}

	@Test
	public void test03_loginSystemApiShouldFailIfUserIsWrong() {
		AuthenticationSystemApi authSystemService = getOsgiService(AuthenticationSystemApi.class);
		HyperIoTUser loginUser = (HUser) authSystemService.login("wrongUsername", "admin");
		Assert.assertNotNull(authSystemService);
		Assert.assertNull(loginUser);
	}

	@Test
	public void test04_loginSystemApiShouldFailIfPasswordIsWrong() {
		AuthenticationSystemApi authSystemService = getOsgiService(AuthenticationSystemApi.class);
		HyperIoTUser loginUser = (HUser) authSystemService.login("hadmin", "wrongPassword");
		Assert.assertNotNull(authSystemService);
		Assert.assertNull(loginUser);
	}

	@Test
	public void test05_generateTokenSystemApiShouldSuccess() {
		AuthenticationSystemApi authSystemService = getOsgiService(AuthenticationSystemApi.class);
		HyperIoTUser loginUser = (HUser) authSystemService.login("hadmin", "admin");
		String token = authSystemService.generateToken(loginUser);
		Assert.assertNotNull(authSystemService);
		Assert.assertNotNull(loginUser);
		Assert.assertNotNull(token);
	}

	@Test
	public void test06_loginSystemApiShouldFailIfUserInsertingHashedPassword() {
		AuthenticationSystemApi authSystemService = getOsgiService(AuthenticationSystemApi.class);
		HyperIoTUser loginUser = (HUser) authSystemService.login("hadmin", "ISMvKXpXpadDiUoOSoAfww==");
		Assert.assertNotNull(authSystemService);
		Assert.assertNull(loginUser);
	}

	@Test
	public void test07_loginSystemApiWithEmailShouldSuccess() {
		AuthenticationSystemApi authSystemService = getOsgiService(AuthenticationSystemApi.class);
		HyperIoTUser loginUser = (HUser) authSystemService.login("hadmin@hyperiot.com", "admin");
		Assert.assertNotNull(authSystemService);
		Assert.assertNotNull(loginUser);
	}

	@Test
	public void test08_loginSystemApiWithEmailShouldFailIfPasswordIsWrong() {
		AuthenticationSystemApi authSystemService = getOsgiService(AuthenticationSystemApi.class);
		HyperIoTUser loginUser = (HUser) authSystemService.login("hadmin@hyperiot.com", "wrong==");
		Assert.assertNotNull(authSystemService);
		Assert.assertNull(loginUser);
	}

}
