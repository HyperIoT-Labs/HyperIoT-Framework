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

package it.acsoftware.hyperiot.authentication.test;

import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.configureSecurity;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.editConfigurationFilePut;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.karafDistributionConfiguration;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.keepRuntimeFolder;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.logLevel;

import java.io.File;

import it.acsoftware.hyperiot.base.test.HyperIoTTestConfigurationBuilder;
import org.apache.karaf.features.FeaturesService;
import org.apache.karaf.itests.KarafTestSupport;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.ConfigurationPointer;
import org.ops4j.pax.exam.karaf.options.KarafDistributionConfigurationFileExtendOption;
import org.ops4j.pax.exam.karaf.options.KarafDistributionConfigurationFileReplacementOption;
import org.ops4j.pax.exam.karaf.options.LogLevelOption;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

import it.acsoftware.hyperiot.authentication.service.rest.AuthenticationRestApi;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
public class AuthenticationServiceRestTest extends KarafTestSupport {

	//force global configuration
	public Option[] config() {
		return null;
	}

	@Test
	public void installAuthenticationBundle() {
		// assert on an available service
		assertServiceAvailable(FeaturesService.class,0);
		String features = executeCommand("feature:list -i");
		assertContains("HyperIoTBase-features ", features);
		assertContains("HyperIoTPermission-features ", features);
//		assertContains("HyperIoTRole-features ", features);
		assertContains("HyperIoTHUser-features ", features);
		assertContains("HyperIoTAuthentication-features ", features);
		AuthenticationRestApi authRestService = getOsgiService(AuthenticationRestApi.class);
		javax.ws.rs.core.Response restResponse = authRestService.checkModuleWorking();
		Assert.assertNotNull(authRestService);
		Assert.assertTrue(restResponse.getStatus() == 200);
		System.out.println(restResponse.getEntity().toString());
	}

	@Test
	public void loginFailTest() {
		AuthenticationRestApi authRestService = getOsgiService(AuthenticationRestApi.class);
		javax.ws.rs.core.Response restResponse = authRestService.login("wrongUser", "wrongPassword");
		System.out.println(restResponse.toString());
		Assert.assertNotNull(authRestService);
		Assert.assertTrue(restResponse.getStatus() == 401);
	}

	@Test
	public void loginSuccessTest() {
		AuthenticationRestApi authRestService = getOsgiService(AuthenticationRestApi.class);
		javax.ws.rs.core.Response restResponse = authRestService.login("hadmin", "admin");
		System.out.println(restResponse.toString());
		Assert.assertNotNull(authRestService);
		Assert.assertTrue(restResponse.getStatus() == 200);
	}

}
