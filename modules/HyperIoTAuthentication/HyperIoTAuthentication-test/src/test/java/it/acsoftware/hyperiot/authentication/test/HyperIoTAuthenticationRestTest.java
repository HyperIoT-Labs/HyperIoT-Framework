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

import it.acsoftware.hyperiot.authentication.service.rest.AuthenticationRestApi;
import it.acsoftware.hyperiot.base.api.HyperIoTUser;
import it.acsoftware.hyperiot.base.api.authentication.AuthenticationApi;
import it.acsoftware.hyperiot.base.service.rest.HyperIoTBaseRestApi;
import org.apache.karaf.features.FeaturesService;
import org.apache.karaf.itests.KarafTestSupport;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

import javax.ws.rs.core.Response;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HyperIoTAuthenticationRestTest extends KarafTestSupport {

    //force global configuration
    public Option[] config() {
        return null;
    }

    public void impersonateUser(HyperIoTBaseRestApi restApi, HyperIoTUser user) {
        restApi.impersonate(user);
    }

    @Test
    public void test00_hyperIoTFrameworkShouldBeInstalled() {
        // assert on an available service
        assertServiceAvailable(FeaturesService.class, 0);
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
    public void test01_authenticationModuleShouldWork() {
        AuthenticationRestApi authRestService = getOsgiService(AuthenticationRestApi.class);
        // the following call checkModuleWorking checks if Authentication module working
        // correctly
        Response restResponse = authRestService.checkModuleWorking();
        Assert.assertEquals(200, restResponse.getStatus());
    }

    // TO DO: ADD TEST FOR REPOSITORY SYSTEM SERVICE AND SERVICE

    @Test
    public void test02_loginRestServiceShouldFailIfInsertingBadCredential() {
        AuthenticationRestApi authRestService = getOsgiService(AuthenticationRestApi.class);
        // HUser tries to authenticate with the following call login, but credentials
        // are bad
        Response restResponse = authRestService.login("wrongUser", "wrongPassword");
        Assert.assertEquals(401, restResponse.getStatus());
        Assert.assertEquals("Unauthorized", restResponse.getStatusInfo().getReasonPhrase());
    }

    @Test
    public void test03_loginRestServiceShouldFailIfPasswordIsWrong() {
        AuthenticationRestApi authRestService = getOsgiService(AuthenticationRestApi.class);
        // HUser tries to authenticate with the following call login, but password is
        // wrong
        Response restResponse = authRestService.login("hadmin", "wrongPassword");
        Assert.assertEquals(401, restResponse.getStatus());
        Assert.assertEquals("Unauthorized", restResponse.getStatusInfo().getReasonPhrase());
    }

    @Test
    public void test04_loginRestServiceShouldFailIfUserIsWrong() {
        AuthenticationRestApi authRestService = getOsgiService(AuthenticationRestApi.class);
        // HUser tries to authenticate with the following call login, but username is
        // wrong
        Response restResponse = authRestService.login("wrongUser", "admin");
        Assert.assertEquals(401, restResponse.getStatus());
        Assert.assertEquals("Unauthorized", restResponse.getStatusInfo().getReasonPhrase());
    }

    @Test
    public void test05_loginRestServiceShouldSuccess() {
        AuthenticationRestApi authRestService = getOsgiService(AuthenticationRestApi.class);
        // HUser authenticate with the following call login
        Response restResponse = authRestService.login("hadmin", "admin");
        Assert.assertEquals(200, restResponse.getStatus());
        String token = (String) restResponse.getEntity();
        Assert.assertFalse(token.isEmpty());
    }


    @Test
    public void test06_testWhoAmI() {
        AuthenticationRestApi authRestService = getOsgiService(AuthenticationRestApi.class);
        // Impersonating
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser user = (HyperIoTUser) authService.login("hadmin", "admin");
        this.impersonateUser(authRestService, user);
        Response restResponse = authRestService.whoAmI();
        Assert.assertEquals(200, restResponse.getStatus());
        Assert.assertEquals("hadmin", restResponse.getEntity());
    }


    @Test
    public void test07_loginRestServiceShouldFailIfUserInsertingHashedPassword() {
        AuthenticationRestApi authRestService = getOsgiService(AuthenticationRestApi.class);
        // HUser tries to authenticate with hashed password with the following call login
        Response restResponse = authRestService.login("hadmin", "ISMvKXpXpadDiUoOSoAfww==");
        Assert.assertEquals(401, restResponse.getStatus());
        Assert.assertEquals("Unauthorized", restResponse.getStatusInfo().getReasonPhrase());
    }

    @Test
    public void test08_loginWithEmailAddressShouldSuccess() {
        AuthenticationRestApi authRestService = getOsgiService(AuthenticationRestApi.class);
        // admin authenticates with the following call login
        Response restResponse = authRestService.login("hadmin@hyperiot.com", "admin");
        Assert.assertEquals(200, restResponse.getStatus());
        String token = (String) restResponse.getEntity();
        Assert.assertFalse(token.isEmpty());
    }

    @Test
    public void test09_loginWithEmailAddressShouldFailIfPasswordIsWrong() {
        AuthenticationRestApi authRestService = getOsgiService(AuthenticationRestApi.class);
        // admin authenticates with the following call login, but password is wrong
        Response restResponse = authRestService.login("hadmin@hyperiot.com", "wrong");
        Assert.assertEquals(401, restResponse.getStatus());
        Assert.assertEquals("Unauthorized", restResponse.getStatusInfo().getReasonPhrase());
    }


}
