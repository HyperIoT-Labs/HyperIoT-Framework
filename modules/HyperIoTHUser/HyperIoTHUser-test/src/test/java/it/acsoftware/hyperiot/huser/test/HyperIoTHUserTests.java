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

package it.acsoftware.hyperiot.huser.test;

import it.acsoftware.hyperiot.authentication.service.rest.AuthenticationRestApi;
import it.acsoftware.hyperiot.base.action.util.HyperIoTActionsUtil;
import it.acsoftware.hyperiot.base.action.util.HyperIoTCrudAction;
import it.acsoftware.hyperiot.base.api.HyperIoTAction;
import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.base.api.HyperIoTUser;
import it.acsoftware.hyperiot.base.api.authentication.AuthenticationApi;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTPaginableResult;
import it.acsoftware.hyperiot.base.exception.*;
import it.acsoftware.hyperiot.base.model.HyperIoTBaseError;
import it.acsoftware.hyperiot.base.model.HyperIoTValidationError;
import it.acsoftware.hyperiot.base.service.rest.HyperIoTBaseRestApi;
import it.acsoftware.hyperiot.base.test.HyperIoTTestConfigurationBuilder;
import it.acsoftware.hyperiot.base.test.util.HyperIoTTestUtils;
import it.acsoftware.hyperiot.base.util.HyperIoTUtil;
import it.acsoftware.hyperiot.huser.api.HUserApi;
import it.acsoftware.hyperiot.huser.api.HUserRepository;
import it.acsoftware.hyperiot.huser.api.HUserSystemApi;
import it.acsoftware.hyperiot.huser.model.HUser;
import it.acsoftware.hyperiot.huser.model.HUserPasswordReset;
import it.acsoftware.hyperiot.huser.service.rest.HUserRestApi;
import it.acsoftware.hyperiot.huser.test.util.HyperIoTHUserTestUtils;
import it.acsoftware.hyperiot.permission.api.PermissionSystemApi;
import it.acsoftware.hyperiot.permission.model.Permission;
import it.acsoftware.hyperiot.permission.service.rest.PermissionRestApi;
import it.acsoftware.hyperiot.role.api.RoleRepository;
import it.acsoftware.hyperiot.role.api.RoleSystemApi;
import it.acsoftware.hyperiot.role.model.Role;
import it.acsoftware.hyperiot.role.service.rest.RoleRestApi;
import it.acsoftware.hyperiot.role.util.HyperIoTRoleConstants;
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

import javax.validation.ConstraintViolation;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static it.acsoftware.hyperiot.huser.test.HyperIoTHUserConfiguration.*;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HyperIoTHUserTests extends KarafTestSupport {

    //force global configuration
    public Option[] config() {
        return null;
    }

    public HyperIoTContext impersonateUser(HyperIoTBaseRestApi restApi, HyperIoTUser user) {
        return restApi.impersonate(user);
    }

    /*
     *
     *
     * REST API TESTS
     *
     *
     */

    @Test
    public void test000_hyperIoTFrameworkShouldBeInstalled() {
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
    public void test001_huserModuleShouldWork() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        this.impersonateUser(huserRestService, adminUser);
        Response restResponse = huserRestService.checkModuleWorking();
        Assert.assertEquals(200, restResponse.getStatus());
        Assert.assertEquals("HyperIoT HUser Module works!", restResponse.getEntity());
    }

    @Test
    public void test002_huserModuleShouldFailIfNotLogged() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        this.impersonateUser(huserRestService, null);
        Response restResponse = huserRestService.checkModuleWorking();
        Assert.assertEquals(200, restResponse.getStatus());
        Assert.assertEquals("HyperIoT HUser Module works!", restResponse.getEntity());
    }

    @Test
    public void test003_registerHUserShouldSuccess() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // the following call register a new HUser
        // response status code '200'
        HUser huser = new HUser();
        huser.setName("name" + java.util.UUID.randomUUID());
        huser.setLastname("lastname" + java.util.UUID.randomUUID());
        huser.setUsername(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
        huser.setEmail(java.util.UUID.randomUUID() + "@hyperiot.com");
        huser.setPassword("passwordPass&01");
        huser.setPasswordConfirm("passwordPass&01");
        Assert.assertNull(huser.getActivateCode());
        this.impersonateUser(huserRestService, null);
        Response restResponse = huserRestService.register(huser);
        Assert.assertEquals(200, restResponse.getStatus());
        Assert.assertFalse(((HUser) restResponse.getEntity()).isActive());
        Assert.assertEquals(huser.getActivateCode(), ((HUser) restResponse.getEntity()).getActivateCode());
        Assert.assertFalse(((HUser) restResponse.getEntity()).getActivateCode().isEmpty());
    }

    @Test
    public void test004_registerHUserShouldFailIfUsernameMalformed() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // a new user tries to register with the platform,
        // but username is malformed
        // response status code '422' HyperIoTValidationException
        HUser huser = new HUser();
        huser.setName("name" + java.util.UUID.randomUUID());
        huser.setLastname("lastname" + java.util.UUID.randomUUID());
        huser.setUsername("username&&&&");
        huser.setEmail(java.util.UUID.randomUUID() + "@hyperiot.com");
        huser.setPassword("passwordPass&01");
        huser.setPasswordConfirm("passwordPass&01");
        this.impersonateUser(huserRestService, null);
        Response restResponse = huserRestService.register(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("huser-username", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(huser.getUsername(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }

    @Test
    public void test005_registerHUserShouldFailIfUsernameIsNull() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // a new user tries to register with the platform,
        // but username is null
        // response status code '422' HyperIoTValidationException
        HUser huser = new HUser();
        huser.setName("name" + java.util.UUID.randomUUID());
        huser.setLastname("lastname" + java.util.UUID.randomUUID());
        huser.setUsername(null);
        huser.setEmail(java.util.UUID.randomUUID() + "@hyperiot.com");
        huser.setPassword("passwordPass&01");
        huser.setPasswordConfirm("passwordPass&01");
        this.impersonateUser(huserRestService, null);
        Response restResponse = huserRestService.register(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(2, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("huser-username", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getMessage().isEmpty());
        Assert.assertEquals("huser-username", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getField());
    }

    @Test
    public void test006_registerHUserShouldFailIfUsernameIsEmpty() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // a new user tries to register with the platform,
        // but username is empty
        // response status code '422' HyperIoTValidationException
        this.impersonateUser(huserRestService, null);
        HUser huser = new HUser();
        huser.setName("name" + java.util.UUID.randomUUID());
        huser.setLastname("lastname" + java.util.UUID.randomUUID());
        huser.setUsername("");
        huser.setEmail(java.util.UUID.randomUUID() + "@hyperiot.com");
        huser.setPassword("passwordPass&01");
        huser.setPasswordConfirm("passwordPass&01");
        Response restResponse = huserRestService.register(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(2, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("huser-username", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(huser.getUsername(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getMessage().isEmpty());
        Assert.assertEquals("huser-username", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getField());
        Assert.assertEquals(huser.getUsername(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getInvalidValue());
    }

    @Test
    public void test007_registerHUserShouldFailIfUsernameIsMaliciousCode() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // a new user tries to register with the platform,
        // but username is malicious code
        // response status code '422' HyperIoTValidationException
        HUser huser = new HUser();
        huser.setName("name" + java.util.UUID.randomUUID());
        huser.setLastname("lastname" + java.util.UUID.randomUUID());
        huser.setUsername("<script>console.log()</script>");
        huser.setEmail(java.util.UUID.randomUUID() + "@hyperiot.com");
        huser.setPassword("passwordPass&01");
        huser.setPasswordConfirm("passwordPass&01");
        this.impersonateUser(huserRestService, null);
        Response restResponse = huserRestService.register(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(2, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("huser-username", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(huser.getUsername(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getMessage().isEmpty());
        Assert.assertEquals("huser-username", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getField());
        Assert.assertEquals(huser.getUsername(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getInvalidValue());
    }

    @Test
    public void test008_registerHUserShouldFailIfEmailIsMalformed() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // a new user tries to register with the platform,
        // but email is malformed
        // response status code '422' HyperIoTValidationException
        HUser huser = new HUser();
        huser.setName("name" + java.util.UUID.randomUUID());
        huser.setLastname("lastname" + java.util.UUID.randomUUID());
        huser.setUsername(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
        huser.setEmail("wrongEmail");
        huser.setPassword("passwordPass&01");
        huser.setPasswordConfirm("passwordPass&01");
        this.impersonateUser(huserRestService, null);
        Response restResponse = huserRestService.register(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("huser-email", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(huser.getEmail(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }

    @Test
    public void test009_registerHUserShouldFailIfEmailIsMaliciousCode() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // a new user tries to register with the platform,
        // but email is malicious code
        // response status code '422' HyperIoTValidationException
        HUser huser = new HUser();
        huser.setName("name" + java.util.UUID.randomUUID());
        huser.setLastname("lastname" + java.util.UUID.randomUUID());
        huser.setUsername(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
        huser.setEmail("<script>console.log()</script>");
        huser.setPassword("passwordPass&01");
        huser.setPasswordConfirm("passwordPass&01");
        this.impersonateUser(huserRestService, null);
        Response restResponse = huserRestService.register(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(2, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("huser-email", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(huser.getEmail(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getMessage().isEmpty());
        Assert.assertEquals("huser-email", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getField());
        Assert.assertEquals(huser.getEmail(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getInvalidValue());
    }

    @Test
    public void test010_registerHUserShouldFailIfEmailIsNull() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // a new user tries to register with the platform,
        // but email is null
        // response status code '422' HyperIoTValidationException
        HUser huser = new HUser();
        huser.setName("name" + java.util.UUID.randomUUID());
        huser.setLastname("lastname" + java.util.UUID.randomUUID());
        huser.setUsername(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
        huser.setEmail(null);
        huser.setPassword("passwordPass&01");
        huser.setPasswordConfirm("passwordPass&01");
        this.impersonateUser(huserRestService, null);
        Response restResponse = huserRestService.register(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(2, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("huser-email", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getMessage().isEmpty());
        Assert.assertEquals("huser-email", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getField());
    }

    @Test
    public void test011_registerHUserShouldFailIfEmailIsEmpty() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // a new user tries to register with the platform,
        // but email is empty
        // response status code '422' HyperIoTValidationException
        HUser huser = new HUser();
        huser.setName("name" + java.util.UUID.randomUUID());
        huser.setLastname("lastname" + java.util.UUID.randomUUID());
        huser.setUsername(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
        huser.setEmail("");
        huser.setPassword("passwordPass&01");
        huser.setPasswordConfirm("passwordPass&01");
        this.impersonateUser(huserRestService, null);
        Response restResponse = huserRestService.register(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("huser-email", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(huser.getEmail(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }

    @Test
    public void test012_registerHUserShouldFailIfPasswordIsMalformed() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // a new user tries to register with the platform,
        // but password is malformed
        // response status code '422' HyperIoTValidationException
        HUser huser = new HUser();
        huser.setName("name" + java.util.UUID.randomUUID());
        huser.setLastname("lastname" + java.util.UUID.randomUUID());
        huser.setUsername(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
        huser.setEmail(java.util.UUID.randomUUID() + "@hyperiot.com");
        huser.setPassword("wrong");
        huser.setPasswordConfirm("passwordPass&01");
        this.impersonateUser(huserRestService, null);
        Response restResponse = huserRestService.register(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(4, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        boolean invalidPassword = false;
        boolean invalidPasswordConfirm = false;
        for (int i = 0; i < ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size(); i++) {
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-password")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-password", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPassword = true;
            }
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-passwordConfirm")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-passwordConfirm", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPasswordConfirm = true;
            }
        }
        Assert.assertTrue(invalidPassword);
        Assert.assertTrue(invalidPasswordConfirm);
    }

    @Test
    public void test013_registerHUserShouldFailIfPasswordIsMaliciousCode() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // a new user tries to register with the platform,
        // but password is malicious code
        // response status code '422' HyperIoTValidationException
        HUser huser = new HUser();
        huser.setName("name" + java.util.UUID.randomUUID());
        huser.setLastname("lastname" + java.util.UUID.randomUUID());
        huser.setUsername(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
        huser.setEmail(java.util.UUID.randomUUID() + "@hyperiot.com");
        huser.setPassword("<script>console.log()</script>");
        huser.setPasswordConfirm("passwordPass&01");
        this.impersonateUser(huserRestService, null);
        Response restResponse = huserRestService.register(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(5, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        boolean invalidPassword = false;
        boolean invalidPasswordConfirm = false;
        for (int i = 0; i < ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size(); i++) {
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-password")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-password", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPassword = true;
            }
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-passwordConfirm")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-passwordConfirm", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPasswordConfirm = true;
            }
        }
        Assert.assertTrue(invalidPassword);
        Assert.assertTrue(invalidPasswordConfirm);
    }

    @Test
    public void test014_registerHUserShouldFailIfPasswordIsNull() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // a new user tries to register with the platform,
        // but password is null
        // response status code '422' HyperIoTValidationException
        HUser huser = new HUser();
        huser.setName("name" + java.util.UUID.randomUUID());
        huser.setLastname("lastname" + java.util.UUID.randomUUID());
        huser.setUsername(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
        huser.setEmail(java.util.UUID.randomUUID() + "@hyperiot.com");
        huser.setPassword(null);
        huser.setPasswordConfirm("passwordPass&01");
        this.impersonateUser(huserRestService, null);
        Response restResponse = huserRestService.register(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(5, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        boolean invalidPassword = false;
        boolean invalidPasswordConfirm = false;
        for (int i = 0; i < ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size(); i++) {
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-password")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-password", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPassword = true;
            }
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-passwordConfirm")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-passwordConfirm", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPasswordConfirm = true;
            }
        }
        Assert.assertTrue(invalidPassword);
        Assert.assertTrue(invalidPasswordConfirm);
    }

    @Test
    public void test015_registerHUserShouldFailIfPasswordConfirmIsNull() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // a new user tries to register with the platform,
        // but password is null
        // response status code '422' HyperIoTValidationException
        HUser huser = new HUser();
        huser.setName("name" + java.util.UUID.randomUUID());
        huser.setLastname("lastname" + java.util.UUID.randomUUID());
        huser.setUsername(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
        huser.setEmail(java.util.UUID.randomUUID() + "@hyperiot.com");
        huser.setPassword("passwordPass&01");
        huser.setPasswordConfirm(null);
        this.impersonateUser(huserRestService, null);
        Response restResponse = huserRestService.register(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(4, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        boolean invalidPassword = false;
        boolean invalidPasswordConfirm = false;
        for (int i = 0; i < ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size(); i++) {
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-password")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-password", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPassword = true;
            }
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-passwordConfirm")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-passwordConfirm", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPasswordConfirm = true;
            }
        }
        Assert.assertTrue(invalidPassword);
        Assert.assertTrue(invalidPasswordConfirm);
    }

    @Test
    public void test016_registerHUserShouldFailIfPasswordIsEmpty() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // a new user tries to register with the platform,
        // but password is empty
        // response status code '422' HyperIoTValidationException
        HUser huser = new HUser();
        huser.setName("name" + java.util.UUID.randomUUID());
        huser.setLastname("lastname" + java.util.UUID.randomUUID());
        huser.setUsername(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
        huser.setEmail(java.util.UUID.randomUUID() + "@hyperiot.com");
        huser.setPassword("");
        huser.setPasswordConfirm("passwordPass&01");
        this.impersonateUser(huserRestService, null);
        Response restResponse = huserRestService.register(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(4, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        boolean invalidPassword = false;
        boolean invalidPasswordConfirm = false;
        for (int i = 0; i < ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size(); i++) {
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-password")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-password", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPassword = true;
            }
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-passwordConfirm")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-passwordConfirm", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPasswordConfirm = true;
            }
        }
        Assert.assertTrue(invalidPassword);
        Assert.assertTrue(invalidPasswordConfirm);
    }

    @Test
    public void test017_registerHUserShouldFailIfPasswordConfirmIsMalformed() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // a new user tries to register with the platform,
        // but passwordConfirm is malformed
        // response status code '422' HyperIoTValidationException
        HUser huser = new HUser();
        huser.setName("name" + java.util.UUID.randomUUID());
        huser.setLastname("lastname" + java.util.UUID.randomUUID());
        huser.setUsername(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
        huser.setEmail(java.util.UUID.randomUUID() + "@hyperiot.com");
        huser.setPassword("passwordPass&01");
        huser.setPasswordConfirm("wrong_ Password___");
        this.impersonateUser(huserRestService, null);
        Response restResponse = huserRestService.register(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(4, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        boolean invalidPassword = false;
        boolean invalidPasswordConfirm = false;
        for (int i = 0; i < ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size(); i++) {
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-password")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-password", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPassword = true;
            }
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-passwordConfirm")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-passwordConfirm", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPasswordConfirm = true;
            }
        }
        Assert.assertTrue(invalidPassword);
        Assert.assertTrue(invalidPasswordConfirm);
    }

    @Test
    public void test018_registerHUserShouldFailIfPasswordConfirmIsMaliciousCode() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // a new user tries to register with the platform,
        // but passwordConfirm is malicious code
        // response status code '422' HyperIoTValidationException
        HUser huser = new HUser();
        huser.setName("name" + java.util.UUID.randomUUID());
        huser.setLastname("lastname" + java.util.UUID.randomUUID());
        huser.setUsername(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
        huser.setEmail(java.util.UUID.randomUUID() + "@hyperiot.com");
        huser.setPassword("passwordPass&01");
        huser.setPasswordConfirm("<script>console.log()</script>");
        this.impersonateUser(huserRestService, null);
        Response restResponse = huserRestService.register(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(4, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        boolean invalidPassword = false;
        boolean invalidPasswordConfirm = false;
        for (int i = 0; i < ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size(); i++) {
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-password")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-password", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPassword = true;
            }
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-passwordConfirm")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-passwordConfirm", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPasswordConfirm = true;
            }
        }
        Assert.assertTrue(invalidPassword);
        Assert.assertTrue(invalidPasswordConfirm);
    }

    @Test
    public void test019_registerHUserShouldFailIfPasswordConfirmIsEmpty() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // a new user tries to register with the platform,
        // but passwordConfirm is empty
        // response status code '422' HyperIoTValidationException
        HUser huser = new HUser();
        huser.setName("name" + java.util.UUID.randomUUID());
        huser.setLastname("lastname" + java.util.UUID.randomUUID());
        huser.setUsername(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
        huser.setEmail(java.util.UUID.randomUUID() + "@hyperiot.com");
        huser.setPassword("passwordPass&01");
        huser.setPasswordConfirm("");
        this.impersonateUser(huserRestService, null);
        Response restResponse = huserRestService.register(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(4, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        boolean invalidPassword = false;
        boolean invalidPasswordConfirm = false;
        for (int i = 0; i < ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size(); i++) {
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-password")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-password", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPassword = true;
            }
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-passwordConfirm")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-passwordConfirm", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPasswordConfirm = true;
            }
        }
        Assert.assertTrue(invalidPassword);
        Assert.assertTrue(invalidPasswordConfirm);
    }

    @Test
    public void test020_saveHUserShouldWork() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // hadmin save HUser with the following call saveHUser
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = new HUser();
        huser.setName("name");
        huser.setLastname("lastname");
        huser.setUsername("TestUser" + UUID.randomUUID().toString().replaceAll("-", ""));
        huser.setEmail("testusername" + UUID.randomUUID().toString() + "@hyperiot.com");
        huser.setPassword("passwordPass&01");
        huser.setPasswordConfirm("passwordPass&01");
        this.impersonateUser(huserRestService, adminUser);
        Response restResponse = huserRestService.saveHUser(huser);
        Assert.assertEquals(200, restResponse.getStatus());
        Assert.assertNotEquals(0, ((HUser) restResponse.getEntity()).getId());
        Assert.assertFalse(((HUser) restResponse.getEntity()).isActive());
        Assert.assertNull(((HUser) restResponse.getEntity()).getActivateCode());
    }

    @Test
    public void test021_saveHUserShouldFailIfNotLogged() {
        HUserRestApi hUserRestApi = getOsgiService(HUserRestApi.class);
        // the following call tries to save HUser, but HUser is not logged
        // response status code '403' HyperIoTUnauthorizedException
        HUser huser = new HUser();
        huser.setName("name");
        huser.setLastname("lastname");
        huser.setUsername("TestUser" + UUID.randomUUID().toString().replaceAll("-", ""));
        huser.setEmail("testusername" + UUID.randomUUID().toString() + "@hyperiot.com");
        huser.setPassword("passwordPass&01");
        huser.setPasswordConfirm("passwordPass&01");
        this.impersonateUser(hUserRestApi, null);
        Response restResponse = hUserRestApi.saveHUser(huser);
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test022_saveHUserShouldFailIfNameIsNull() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // hadmin tries to save HUser with the following call saveHUser, but
        // name is null
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = new HUser();
        huser.setName(null);
        huser.setLastname("lastname");
        huser.setUsername("TestUser" + UUID.randomUUID().toString().replaceAll("-", ""));
        huser.setEmail("testusername" + UUID.randomUUID().toString() + "@hyperiot.com");
        huser.setPassword("passwordPass&01");
        huser.setPasswordConfirm("passwordPass&01");
        this.impersonateUser(huserRestService, adminUser);
        Response restResponse = huserRestService.saveHUser(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(2, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("huser-name", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getMessage().isEmpty());
        Assert.assertEquals("huser-name", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getField());
    }

    @Test
    public void test023_saveHUserShouldFailIfNameIsEmpty() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // hadmin tries to save HUser with the following call saveHUser, but
        // name is empty
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = new HUser();
        huser.setName("");
        huser.setLastname("lastname");
        huser.setUsername("TestUser" + UUID.randomUUID().toString().replaceAll("-", ""));
        huser.setEmail("testusername" + UUID.randomUUID().toString() + "@hyperiot.com");
        huser.setPassword("passwordPass&01");
        huser.setPasswordConfirm("passwordPass&01");
        this.impersonateUser(huserRestService, adminUser);
        Response restResponse = huserRestService.saveHUser(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("huser-name", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(huser.getName(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }

    @Test
    public void test024_saveHUserShouldFailIfNameIsMaliciousCode() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // hadmin tries to save HUser with the following call saveHUser, but
        // name is malicious code
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = new HUser();
        huser.setName("</script>");
        huser.setLastname("lastname");
        huser.setUsername("TestUser" + UUID.randomUUID().toString().replaceAll("-", ""));
        huser.setEmail("testusername" + UUID.randomUUID().toString() + "@hyperiot.com");
        huser.setPassword("passwordPass&01");
        huser.setPasswordConfirm("passwordPass&01");
        this.impersonateUser(huserRestService, adminUser);
        Response restResponse = huserRestService.saveHUser(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("huser-name", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(huser.getName(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }

    @Test
    public void test025_saveHUserShouldFailIfLastnameIsNull() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // hadmin tries to save HUser with the following call saveHUser, but
        // lastname is null
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = new HUser();
        huser.setName("name");
        huser.setLastname(null);
        huser.setUsername("TestUser" + UUID.randomUUID().toString().replaceAll("-", ""));
        huser.setEmail("testusername" + UUID.randomUUID().toString() + "@hyperiot.com");
        huser.setPassword("passwordPass&01");
        huser.setPasswordConfirm("passwordPass&01");
        this.impersonateUser(huserRestService, adminUser);
        Response restResponse = huserRestService.saveHUser(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(2, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("huser-lastname", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getMessage().isEmpty());
        Assert.assertEquals("huser-lastname", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getField());
    }

    @Test
    public void test026_saveHUserShouldFailIfLastnameIsEmpty() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // hadmin tries to save HUser with the following call saveHUser, but
        // lastname is empty
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = new HUser();
        huser.setName("name");
        huser.setLastname("");
        huser.setUsername("TestUser" + UUID.randomUUID().toString().replaceAll("-", ""));
        huser.setEmail("testusername" + UUID.randomUUID().toString() + "@hyperiot.com");
        huser.setPassword("passwordPass&01");
        huser.setPasswordConfirm("passwordPass&01");
        this.impersonateUser(huserRestService, adminUser);
        Response restResponse = huserRestService.saveHUser(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("huser-lastname", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(huser.getLastname(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }

    @Test
    public void test027_saveHUserShouldFailIfLastnameIsMaliciousCode() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // hadmin tries to save HUser with the following call saveHUser, but
        // lastname is malicious code
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        this.impersonateUser(huserRestService, adminUser);
        HUser huser = new HUser();
        huser.setName("name");
        huser.setLastname("javascript:");
        huser.setUsername("TestUser" + UUID.randomUUID().toString().replaceAll("-", ""));
        huser.setEmail("testusername" + UUID.randomUUID().toString() + "@hyperiot.com");
        huser.setPassword("passwordPass&01");
        huser.setPasswordConfirm("passwordPass&01");
        Response restResponse = huserRestService.saveHUser(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("huser-lastname", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(huser.getLastname(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }

    @Test
    public void test028_saveHUserShouldFailIfUsernameIsMalformed() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // hadmin tries to save HUser with the following call saveHUser, but
        // username is malformed
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = new HUser();
        huser.setName("name");
        huser.setLastname("lastname");
        huser.setUsername("&%/&%/");
        huser.setEmail("testusername" + UUID.randomUUID().toString() + "@hyperiot.com");
        huser.setPassword("passwordPass&01");
        huser.setPasswordConfirm("passwordPass&01");
        this.impersonateUser(huserRestService, adminUser);
        Response restResponse = huserRestService.saveHUser(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("huser-username", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(huser.getUsername(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }

    @Test
    public void test029_saveHUserShouldFailIfUsernameIsMaliciousCode() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // hadmin tries to save HUser with the following call saveHUser, but
        // username is malicious code
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = new HUser();
        huser.setName("name");
        huser.setLastname("lastname");
        huser.setUsername("eval(test malicious code)");
        huser.setEmail("testusername" + UUID.randomUUID().toString() + "@hyperiot.com");
        huser.setPassword("passwordPass&01");
        huser.setPasswordConfirm("passwordPass&01");
        this.impersonateUser(huserRestService, adminUser);
        Response restResponse = huserRestService.saveHUser(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(2, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("huser-username", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(huser.getUsername(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getMessage().isEmpty());
        Assert.assertEquals("huser-username", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getField());
        Assert.assertEquals(huser.getUsername(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getInvalidValue());
    }

    @Test
    public void test030_saveHUserShouldFailIfUsernameIsNull() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // hadmin tries to save HUser with the following call saveHUser, but
        // username is null
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = new HUser();
        huser.setName("name");
        huser.setLastname("lastname");
        huser.setUsername(null);
        huser.setEmail("testusername" + UUID.randomUUID().toString() + "@hyperiot.com");
        huser.setPassword("passwordPass&01");
        huser.setPasswordConfirm("passwordPass&01");
        this.impersonateUser(huserRestService, adminUser);
        Response restResponse = huserRestService.saveHUser(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(2, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("huser-username", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getMessage().isEmpty());
        Assert.assertEquals("huser-username", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getField());
    }

    @Test
    public void test031_saveHUserShouldFailIfUsernameIsEmpty() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // hadmin tries to save HUser with the following call saveHUser, but
        // username is empty
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = new HUser();
        huser.setName("name");
        huser.setLastname("lastname");
        huser.setUsername("");
        huser.setEmail("testusername" + UUID.randomUUID().toString() + "@hyperiot.com");
        huser.setPassword("passwordPass&01");
        huser.setPasswordConfirm("passwordPass&01");
        this.impersonateUser(huserRestService, adminUser);
        Response restResponse = huserRestService.saveHUser(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(2, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("huser-username", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(huser.getUsername(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getMessage().isEmpty());
        Assert.assertEquals("huser-username", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getField());
        Assert.assertEquals(huser.getUsername(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getInvalidValue());
    }

    @Test
    public void test032_saveHUserShouldFailIfEmailIsMalformed() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // hadmin tries to save HUser with the following call saveHUser, but
        // email is malformed
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = new HUser();
        huser.setName("name");
        huser.setLastname("lastname");
        huser.setUsername("TestUser" + UUID.randomUUID().toString().replaceAll("-", ""));
        huser.setEmail("wrongEmail");
        huser.setPassword("passwordPass&01");
        huser.setPasswordConfirm("passwordPass&01");
        this.impersonateUser(huserRestService, adminUser);
        Response restResponse = huserRestService.saveHUser(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("huser-email", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(huser.getEmail(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }

    @Test
    public void test033_saveHUserShouldFailIfEmailIsMaliciousCode() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // hadmin tries to save HUser with the following call saveHUser, but
        // email is malicious code
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = new HUser();
        huser.setName("name");
        huser.setLastname("lastname");
        huser.setUsername("TestUser" + UUID.randomUUID().toString().replaceAll("-", ""));
        huser.setEmail("vbscript:");
        huser.setPassword("passwordPass&01");
        huser.setPasswordConfirm("passwordPass&01");
        this.impersonateUser(huserRestService, adminUser);
        Response restResponse = huserRestService.saveHUser(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(2, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("huser-email", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(huser.getEmail(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getMessage().isEmpty());
        Assert.assertEquals("huser-email", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getField());
        Assert.assertEquals(huser.getEmail(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getInvalidValue());
    }

    @Test
    public void test034_saveHUserShouldFailIfEmailIsNull() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // hadmin tries to save HUser with the following call saveHUser, but
        // email is null
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = new HUser();
        huser.setName("name");
        huser.setLastname("lastname");
        huser.setUsername("TestUser" + UUID.randomUUID().toString().replaceAll("-", ""));
        huser.setEmail(null);
        huser.setPassword("passwordPass&01");
        huser.setPasswordConfirm("passwordPass&01");
        this.impersonateUser(huserRestService, adminUser);
        Response restResponse = huserRestService.saveHUser(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(2, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("huser-email", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getMessage().isEmpty());
        Assert.assertEquals("huser-email", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getField());
    }

    @Test
    public void test035_saveHUserShouldFailIfEmailIsEmpty() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // hadmin tries to save HUser with the following call saveHUser, but
        // email is empty
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = new HUser();
        huser.setName("name");
        huser.setLastname("lastname");
        huser.setUsername("TestUser" + UUID.randomUUID().toString().replaceAll("-", ""));
        huser.setEmail("");
        huser.setPassword("passwordPass&01");
        huser.setPasswordConfirm("passwordPass&01");
        this.impersonateUser(huserRestService, adminUser);
        Response restResponse = huserRestService.saveHUser(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("huser-email", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(huser.getEmail(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }

    @Test
    public void test036_saveHUserShouldFailIfPasswordIsMalformed() {
        HUserRestApi hUserRestApi = getOsgiService(HUserRestApi.class);
        // hadmin tries to save HUser with the following call saveHUser, but
        // password is malformed
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = new HUser();
        huser.setName("name");
        huser.setLastname("lastname");
        huser.setUsername("TestUser" + UUID.randomUUID().toString().replaceAll("-", ""));
        huser.setEmail("testusername" + UUID.randomUUID().toString() + "@hyperiot.com");
        huser.setPassword("passwordMalformed");
        huser.setPasswordConfirm("passwordPass&01");
        this.impersonateUser(hUserRestApi, adminUser);
        Response restResponse = hUserRestApi.saveHUser(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(4, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        boolean invalidPassword = false;
        boolean invalidPasswordConfirm = false;
        for (int i = 0; i < ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size(); i++) {
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-password")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-password", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPassword = true;
            }
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-passwordConfirm")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-passwordConfirm", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPasswordConfirm = true;
            }
        }
        Assert.assertTrue(invalidPassword);
        Assert.assertTrue(invalidPasswordConfirm);
    }

    @Test
    public void test037_saveHUserShouldFailIfPasswordIsMaliciousCode() {
        HUserRestApi hUserRestApi = getOsgiService(HUserRestApi.class);
        // hadmin tries to save HUser with the following call saveHUser, but
        // password is malicious code
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = new HUser();
        huser.setName("name");
        huser.setLastname("lastname");
        huser.setUsername("TestUser" + UUID.randomUUID().toString().replaceAll("-", ""));
        huser.setEmail("testusername" + UUID.randomUUID().toString() + "@hyperiot.com");
        huser.setPassword("expression(malicious code)");
        huser.setPasswordConfirm("passwordPass&01");
        this.impersonateUser(hUserRestApi, adminUser);
        Response restResponse = hUserRestApi.saveHUser(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(5, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        boolean invalidPassword = false;
        boolean invalidPasswordConfirm = false;
        for (int i = 0; i < ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size(); i++) {
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-password")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-password", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPassword = true;
            }
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-passwordConfirm")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-passwordConfirm", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPasswordConfirm = true;
            }
        }
        Assert.assertTrue(invalidPassword);
        Assert.assertTrue(invalidPasswordConfirm);
    }

    @Test
    public void test038_saveHUserShouldFailIfPasswordIsNull() {
        HUserRestApi hUserRestApi = getOsgiService(HUserRestApi.class);
        // hadmin tries to save HUser with the following call saveHUser, but
        // password is empty
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = new HUser();
        huser.setName("name");
        huser.setLastname("lastname");
        huser.setUsername("TestUser" + UUID.randomUUID().toString().replaceAll("-", ""));
        huser.setEmail("testusername" + UUID.randomUUID().toString() + "@hyperiot.com");
        huser.setPassword(null);
        huser.setPasswordConfirm("passwordPass&01");
        this.impersonateUser(hUserRestApi, adminUser);
        Response restResponse = hUserRestApi.saveHUser(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(5, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        boolean invalidPassword = false;
        boolean invalidPasswordConfirm = false;
        for (int i = 0; i < ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size(); i++) {
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-password")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-password", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPassword = true;
            }
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-passwordConfirm")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-passwordConfirm", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPasswordConfirm = true;
            }
        }
        Assert.assertTrue(invalidPassword);
        Assert.assertTrue(invalidPasswordConfirm);
    }

    @Test
    public void test039_saveHUserShouldFailIfPasswordIsEmpty() {
        HUserRestApi hUserRestApi = getOsgiService(HUserRestApi.class);
        // hadmin tries to save HUser with the following call saveHUser, but
        // password is empty
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = new HUser();
        huser.setName("name");
        huser.setLastname("lastname");
        huser.setUsername("TestUser" + UUID.randomUUID().toString().replaceAll("-", ""));
        huser.setEmail("testusername" + UUID.randomUUID().toString() + "@hyperiot.com");
        huser.setPassword("");
        huser.setPasswordConfirm("passwordPass&01");
        this.impersonateUser(hUserRestApi, adminUser);
        Response restResponse = hUserRestApi.saveHUser(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(4, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        boolean invalidPassword = false;
        boolean invalidPasswordConfirm = false;
        for (int i = 0; i < ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size(); i++) {
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-password")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-password", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPassword = true;
            }
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-passwordConfirm")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-passwordConfirm", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPasswordConfirm = true;
            }
        }
        Assert.assertTrue(invalidPassword);
        Assert.assertTrue(invalidPasswordConfirm);
    }

    @Test
    public void test040_saveHUserShouldFailIfPasswordConfirmIsMalformed() {
        HUserRestApi hUserRestApi = getOsgiService(HUserRestApi.class);
        // hadmin tries to save HUser with the following call saveHUser, but
        // passwordConfirm is malformed
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = new HUser();
        huser.setName("name");
        huser.setLastname("lastname");
        huser.setUsername("TestUser" + UUID.randomUUID().toString().replaceAll("-", ""));
        huser.setEmail("testusername" + UUID.randomUUID().toString() + "@hyperiot.com");
        huser.setPassword("FirstUser09&");
        huser.setPasswordConfirm("passwordMalformed");
        this.impersonateUser(hUserRestApi, adminUser);
        Response restResponse = hUserRestApi.saveHUser(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(4, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        boolean invalidPassword = false;
        boolean invalidPasswordConfirm = false;
        for (int i = 0; i < ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size(); i++) {
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-password")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-password", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPassword = true;
            }
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-passwordConfirm")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-passwordConfirm", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPasswordConfirm = true;
            }
        }
        Assert.assertTrue(invalidPassword);
        Assert.assertTrue(invalidPasswordConfirm);
    }

    @Test
    public void test041_saveHUserShouldFailIfPasswordConfirmIsMaliciousCode() {
        HUserRestApi hUserRestApi = getOsgiService(HUserRestApi.class);
        // hadmin tries to save HUser with the following call saveHUser, but
        // passwordConfirm is malicious code
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = new HUser();
        huser.setName("name");
        huser.setLastname("lastname");
        huser.setUsername("TestUser" + UUID.randomUUID().toString().replaceAll("-", ""));
        huser.setEmail("testusername" + UUID.randomUUID().toString() + "@hyperiot.com");
        huser.setPassword("FirstUser09&");
        huser.setPasswordConfirm("<script>console.log()</script>");
        this.impersonateUser(hUserRestApi, adminUser);
        Response restResponse = hUserRestApi.saveHUser(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(4, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        boolean invalidPassword = false;
        boolean invalidPasswordConfirm = false;
        for (int i = 0; i < ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size(); i++) {
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-password")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-password", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPassword = true;
            }
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-passwordConfirm")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-passwordConfirm", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPasswordConfirm = true;
            }
        }
        Assert.assertTrue(invalidPassword);
        Assert.assertTrue(invalidPasswordConfirm);
    }

    @Test
    public void test042_findHUserShouldWork() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // hadmin find all HUser with the following call findHUser
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = createHUser();
        this.impersonateUser(huserRestService, adminUser);
        Response restResponse = huserRestService.findHUser(huser.getId());
        Assert.assertEquals(200, restResponse.getStatus());
        Assert.assertEquals(huser.getId(), ((HUser) restResponse.getEntity()).getId());
        Assert.assertEquals(huser.getUsername(), ((HUser) restResponse.getEntity()).getUsername());
    }

    @Test
    public void test043_findHUserShouldFailIfNotLogged() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // the following call tries to find HUser, but HUser is not logged
        // response status code '403' HyperIoTUnauthorizedException
        HUser huser = createHUser();
        this.impersonateUser(huserRestService, null);
        Response restResponse = huserRestService.findHUser(huser.getId());
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test044_findHUserShouldFailIfEntityNotFound() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // hadmin tries to find HUser, but entity not found
        // response status code '404' HyperIoTEntityNotFound
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        this.impersonateUser(huserRestService, adminUser);
        Response restResponse = huserRestService.findHUser(0);
        Assert.assertEquals(404, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test045_findAllHUserShouldWork() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // hadmin find all HUser with the following call findAllHUser
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = createHUser();
        this.impersonateUser(huserRestService, adminUser);
        Response restResponse = huserRestService.findAllHUser();
        List<HUser> listHUsers = restResponse.readEntity(new GenericType<List<HUser>>() {
        });
        Assert.assertNotEquals(0, listHUsers.size());
        Assert.assertEquals(2, listHUsers.size()); // contains huser and hadmin
        boolean huserFound = false;
        boolean adminFound = false;
        for (HUser user : listHUsers) {
            if (user.getId() == huser.getId()) {
                Assert.assertFalse(user.isAdmin());
                huserFound = true;
            }
            if (user.getId() == adminUser.getId()) {
                Assert.assertTrue(user.isAdmin());
                adminFound = true;
            }
        }
        Assert.assertTrue(huserFound);
        Assert.assertTrue(adminFound);
        Assert.assertEquals(200, restResponse.getStatus());
    }

    @Test
    public void test046_findAllHUserShouldFailIfNotLogged() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // the following call tries to find all HUser, but HUser is not logged
        // response status code '403' HyperIoTUnauthorizedException
        this.impersonateUser(huserRestService, null);
        Response restResponse = huserRestService.findAllHUser();
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test047_updateHUserShouldWork() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // hadmin updates HUser name with the following call updateHUser
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = createHUser();
        huser.setName("edited");
        this.impersonateUser(huserRestService, adminUser);
        Response restResponse = huserRestService.updateHUser(huser);
        Assert.assertEquals(200, restResponse.getStatus());
        Assert.assertEquals(huser.getId(), ((HUser) restResponse.getEntity()).getId());
        Assert.assertEquals("edited", ((HUser) restResponse.getEntity()).getName());
        Assert.assertEquals(huser.getEntityVersion() + 1,
                ((HUser) restResponse.getEntity()).getEntityVersion());
    }

    @Test
    public void test048_updateHUserShouldFailIfNotLogged() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // the following call tries to update HUser, but HUser is not logged
        // response status code '403' HyperIoTUnauthorizedException
        HUser huser = createHUser();
        huser.setName("edited failed");
        this.impersonateUser(huserRestService, null);
        Response restResponse = huserRestService.updateHUser(huser);
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test049_updateHUserShouldFailIfNameIsNull() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // hadmin tries to update HUser name with the following call updateHUser,
        // but name is null
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = createHUser();
        huser.setName(null);
        this.impersonateUser(huserRestService, adminUser);
        Response restResponse = huserRestService.updateHUser(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(2, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("huser-name", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getMessage().isEmpty());
        Assert.assertEquals("huser-name", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getField());
    }

    @Test
    public void test050_updateHUserShouldFailIfNameIsEmpty() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // hadmin tries to update HUser name with the following call updateHUser,
        // but name is empty
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = createHUser();
        huser.setName("");
        this.impersonateUser(huserRestService, adminUser);
        Response restResponse = huserRestService.updateHUser(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("huser-name", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(huser.getName(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }

    @Test
    public void test051_updateHUserShouldFailIfNameIsMaliciousCode() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // hadmin tries to update HUser name with the following call updateHUser,
        // but name is malicious code
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = createHUser();
        huser.setName("</script>");
        this.impersonateUser(huserRestService, adminUser);
        Response restResponse = huserRestService.updateHUser(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("huser-name", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(huser.getName(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }

    @Test
    public void test052_updateHUserShouldFailIfLastnameIsNull() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // hadmin tries to update HUser lastname with the following call updateHUser,
        // but lastname is null
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = createHUser();
        huser.setLastname(null);
        this.impersonateUser(huserRestService, adminUser);
        Response restResponse = huserRestService.updateHUser(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(2, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("huser-lastname", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getMessage().isEmpty());
        Assert.assertEquals("huser-lastname", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getField());
    }

    @Test
    public void test053_updateHUserShouldFailIfLastnameIsEmpty() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // hadmin tries to update HUser lastname with the following call updateHUser,
        // but lastname is empty
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = createHUser();
        huser.setLastname("");
        this.impersonateUser(huserRestService, adminUser);
        Response restResponse = huserRestService.updateHUser(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("huser-lastname", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(huser.getLastname(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }

    @Test
    public void test054_updateHUserShouldFailIfLastnameIsMaliciousCode() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // hadmin tries to update HUser lastname with the following call updateHUser,
        // but lastname is malicious code
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = createHUser();
        huser.setLastname("javascript:");
        this.impersonateUser(huserRestService, adminUser);
        Response restResponse = huserRestService.updateHUser(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("huser-lastname", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(huser.getLastname(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }

    @Test
    public void test055_updateHUserShouldFailIfUsernameIsMalformed() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // hadmin tries to update HUser username with the following call updateHUser,
        // but username is malformed
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = createHUser();
        huser.setUsername("username&&&&&");
        this.impersonateUser(huserRestService, adminUser);
        Response restResponse = huserRestService.updateHUser(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("huser-username", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(huser.getUsername(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }

    @Test
    public void test056_updateHUserShouldFailIfUsernameIsMaliciousCode() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // hadmin tries to update HUser username with the following call updateHUser,
        // but username is malicious code
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = createHUser();
        huser.setUsername("vbscript:");
        this.impersonateUser(huserRestService, adminUser);
        Response restResponse = huserRestService.updateHUser(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(2, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("huser-username", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(huser.getUsername(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getMessage().isEmpty());
        Assert.assertEquals("huser-username", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getField());
        Assert.assertEquals(huser.getUsername(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getInvalidValue());
    }

    @Test
    public void test057_updateHUserShouldFailIfUsernameIsNull() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // hadmin tries to update HUser username with the following call updateHUser,
        // but username is null
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = createHUser();
        huser.setUsername(null);
        this.impersonateUser(huserRestService, adminUser);
        Response restResponse = huserRestService.updateHUser(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(2, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("huser-username", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getMessage().isEmpty());
        Assert.assertEquals("huser-username", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getField());
    }

    @Test
    public void test058_updateHUserShouldFailIfUsernameIsEmpty() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // hadmin tries to update HUser username with the following call updateHUser,
        // but username is empty
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = createHUser();
        huser.setUsername("");
        this.impersonateUser(huserRestService, adminUser);
        Response restResponse = huserRestService.updateHUser(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(2, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("huser-username", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(huser.getUsername(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getMessage().isEmpty());
        Assert.assertEquals("huser-username", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getField());
        Assert.assertEquals(huser.getUsername(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getInvalidValue());
    }

    @Test
    public void test059_updateHUserShouldFailIfEmailIsMalformed() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // hadmin tries to update HUser email with the following call updateHUser,
        // but email is malformed
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = createHUser();
        huser.setEmail("malformedEmail");
        this.impersonateUser(huserRestService, adminUser);
        Response restResponse = huserRestService.updateHUser(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("huser-email", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(huser.getEmail(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }

    @Test
    public void test060_updateHUserShouldFailIfEmailIsMaliciousCode() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // hadmin tries to update HUser email with the following call updateHUser,
        // but email is malicious code
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = createHUser();
        huser.setEmail("</script>");
        this.impersonateUser(huserRestService, adminUser);
        Response restResponse = huserRestService.updateHUser(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(2, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("huser-email", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(huser.getEmail(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getMessage().isEmpty());
        Assert.assertEquals("huser-email", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getField());
        Assert.assertEquals(huser.getEmail(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getInvalidValue());
    }

    @Test
    public void test061_updateHUserShouldFailIfEmailIsNull() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // hadmin tries to update HUser email with the following call updateHUser,
        // but email is null
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = createHUser();
        huser.setEmail(null);
        this.impersonateUser(huserRestService, adminUser);
        Response restResponse = huserRestService.updateHUser(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(2, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("huser-email", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getMessage().isEmpty());
        Assert.assertEquals("huser-email", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getField());
    }

    @Test
    public void test062_updateHUserShouldFailIfEmailIsEmpty() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // hadmin tries to update HUser email with the following call updateHUser,
        // but email is empty
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = createHUser();
        huser.setEmail("");
        this.impersonateUser(huserRestService, adminUser);
        Response restResponse = huserRestService.updateHUser(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("huser-email", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(huser.getEmail(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }

    @Test
    public void test063_updateHUserShouldNotUpdateHUserPassword() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // hadmin tries to change the HUser password specify a new Password with the following call updateHUser (huser)
        // Assert that updateHUser works (Response status code '200' OK)
        // Assert that huser's password not change ( HUser can change password only with changeHUserPassword/resetPassword rest service).
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = createHUser();
        String huserOldPassword = "passwordPass&01" ;
        Assert.assertTrue(HyperIoTUtil.passwordMatches( huserOldPassword, huser.getPassword()));
        String huserNewPassword = huserOldPassword.concat("!!");
        huser.setPassword(huserNewPassword);
        huser.setPasswordConfirm(huserOldPassword);
        this.impersonateUser(huserRestService, adminUser);
        Response restResponse = huserRestService.updateHUser(huser);
        Assert.assertEquals(200, restResponse.getStatus());
        HUser responseHUser = ((HUser)restResponse.getEntity());
        Assert.assertNotNull(responseHUser);
        Assert.assertEquals(responseHUser.getId(), huser.getId());
        Assert.assertFalse(HyperIoTUtil.passwordMatches(huserNewPassword, responseHUser.getPassword()));
        Assert.assertTrue(HyperIoTUtil.passwordMatches(huserOldPassword, responseHUser.getPassword()));
    }

    @Test
    public void test064_updateHUserShouldFailIfEntityNotFound() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // hadmin tries to update HUser, but entity not found
        // response status code '404' HyperIoTEntityNotFound
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser user = (HUser) authService.login("hadmin", "admin");
        HUser huser = new HUser(); // huser isn't stored in database
        this.impersonateUser(huserRestService, user);
        Response restResponse = huserRestService.updateHUser(huser);
        Assert.assertEquals(404, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test065_removeHUserShouldWork() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // in this call hadmin delete HUser
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = createHUser();
        this.impersonateUser(huserRestService, adminUser);
        Response restResponse = huserRestService.deleteHUser(huser.getId());
        Assert.assertEquals(200, restResponse.getStatus());
        Assert.assertNull(restResponse.getEntity());
    }

    @Test
    public void test066_removeHUserShouldFailIfNotLogged() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // this call tries to delete HUser, but HUser is not logged
        // response status code '403' HyperIoTUnauthorizedException
        HUser huser = createHUser();
        this.impersonateUser(huserRestService, null);
        Response restResponse = huserRestService.deleteHUser(huser.getId());
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test067_removeHUserShouldFailIfEntityNotFound() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // hadmin tries to delete HUser, but entity not found
        // response status code '404' HyperIoTEntityNotFound
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        this.impersonateUser(huserRestService, adminUser);
        Response restResponse = huserRestService.deleteHUser(0);
        Assert.assertEquals(404, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test068_loginHUserFailedIfUserIsNotActivated() {
        // HUser tries to log in but fails because he is not active
        // response status code '403' HyperIoTUserNotActivated
        HUser huser = registerHUser();
        AuthenticationRestApi authRestService = getOsgiService(AuthenticationRestApi.class);
        Response restResponse = authRestService.login(huser.getUsername(), huser.getPassword());
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUserNotActivated",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(0).isEmpty());
    }

    @Test
    public void test069_activateNewHUserShouldWork() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // the following call activateUser with his email and reset code
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HUser huser = registerHUser();

        //checks: huser is not active
        AuthenticationRestApi authRestService = getOsgiService(AuthenticationRestApi.class);
        Response restResponse = authRestService.login(huser.getUsername(), huser.getPassword());
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUserNotActivated",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(0).isEmpty());

        // Activate  huser
        Assert.assertFalse(huser.isActive());
        String activationCode = huser.getActivateCode();
        Assert.assertNotNull(activationCode);
        Response restResponseActivateUser = huserRestService.activate(huser.getEmail(), activationCode);
        Assert.assertEquals(200, restResponseActivateUser.getStatus());
        boolean userActivated;
        try {
            huser = (HUser) authService.login(huser.getUsername(), "passwordPass&01");
            userActivated = true;
        } catch (HyperIoTUserNotActivated ex) {
            userActivated = false;
            Assert.assertFalse(huser.isActive());
        }
        Assert.assertTrue(userActivated);
        Assert.assertTrue(huser.isActive());
    }

    @Test
    public void test070_activateUserShouldFailIfPasswordResetCodeIsWrong() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // the following call activateUser, but it fail because reset code is wrong
        // response status code '422' HyperIoTWrongUserActivationCode
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HUser huser = registerHUser();
        Assert.assertFalse(huser.isActive());
        String activationCode = huser.getActivateCode();
        Assert.assertNotNull(activationCode);
        Response restResponse = huserRestService.activate(huser.getEmail(), "wrongActivationCode");
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTWrongUserActivationCode",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().size());
        Assert.assertEquals("it.acsoftware.hyperiot.huser.error.activation.failed",
                ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(0));
        boolean userActivated = true;
        try {
            huser = (HUser) authService.login(huser.getUsername(), "passwordPass&01");
        } catch (HyperIoTUserNotActivated ex) {
            userActivated = false;
        }
        Assert.assertFalse(userActivated);
        Assert.assertFalse(huser.isActive());
    }

    @Test
    public void test071_activateUserShouldFailIfEntityNotFound() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // the following call activateUser, but it fail because user not found
        // response status code '404' HyperIoTEntityNotFound
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HUser huser = registerHUser();
        Assert.assertFalse(huser.isActive());
        String activationCode = huser.getActivateCode();
        Assert.assertNotNull(activationCode);
        Response restResponse = huserRestService.activate("wrongEmail", activationCode);
        Assert.assertEquals(404, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        // checks: user account isn't active
        boolean userActivated = true;
        try {
            huser = (HUser) authService.login(huser.getUsername(), "passwordPass&01");
        } catch (HyperIoTUserNotActivated ex) {
            userActivated = false;
        }
        Assert.assertFalse(userActivated);
        Assert.assertFalse(huser.isActive());
    }

    @Test
    public void test072_activateUserShouldFailIfUserAlreadyActivated() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // the following call activateUser, but it fail because user is already
        // activated
        // response status code '422' HyperIoTUserAlreadyActivated
        HUser huser = createHUser(); //huser is already activated
        String activationCode = huser.getActivateCode();
        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.activate(huser.getEmail(), activationCode);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUserAlreadyActivated",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().size());
        Assert.assertEquals("it.acsoftware.hyperiot.huser.error.already.activated",
                ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(0));
    }

    @Test
    public void test073_resetPasswordRequestShouldWork() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // HUser reset password with the following call resetPasswordRequest
        // response status code '200'
        HUser huser = createHUser();
        Assert.assertNull(huser.getPasswordResetCode());
        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.resetPasswordRequest(huser.getEmail());
        Assert.assertEquals(200, restResponse.getStatus());
        String sqlHUser = "select h.passwordresetcode from huser h where h.id=" + huser.getId();
        String resultHUser = executeCommand("jdbc:query hyperiot " + sqlHUser);
        String[] resetcode = resultHUser.split("\\n");
        Assert.assertFalse(resetcode[2].isEmpty());
    }

    @Test
    public void test074_resetPasswordRequestShouldFailIfMailNotFound() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // HUser reset password with the following call resetPasswordRequest,
        // but email not found in db
        // response status code '404' HyperIoTEntityNotFound
        HUser huser = createHUser();
        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.resetPasswordRequest("wrongEmail");
        Assert.assertEquals(404, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test075_resetPasswordShouldWork() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // HUser reset password with the following call resetPassword
        // response status code '200'
        HUser huser = requestResetPassword();
        HUserPasswordReset pwdReset = new HUserPasswordReset();
        pwdReset.setPassword("newPass01/");
        pwdReset.setPasswordConfirm("newPass01/");
        pwdReset.setEmail(huser.getEmail());
        String resetCode = huser.getPasswordResetCode();
        pwdReset.setResetCode(resetCode);
        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.resetPassword(pwdReset);
        Assert.assertEquals(200, restResponse.getStatus());
    }

    @Test
    public void test076_resetPasswordShouldFailIfResetCodeIsWrong() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // HUser reset password with the following call resetPassword, but
        // resetCode is wrong
        // response status code '422' HyperIoTWrongUserPasswordResetCode
        HUser huser = requestResetPassword();
        HUserPasswordReset pwdReset = new HUserPasswordReset();
        pwdReset.setPassword("newPass01/");
        pwdReset.setPasswordConfirm("newPass01/");
        pwdReset.setEmail(huser.getEmail());
        pwdReset.setResetCode("wrongResetCode");
        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.resetPassword(pwdReset);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTWrongUserPasswordResetCode",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(0).isEmpty());
    }

    @Test
    public void test077_resetPasswordShouldFailIfResetCodeIsNull() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // HUser reset password with the following call resetPassword, but
        // resetCode is null
        // response status code '422' HyperIoTWrongUserPasswordResetCode
        HUser huser = requestResetPassword();
        HUserPasswordReset pwdReset = new HUserPasswordReset();
        pwdReset.setPassword("newPass01/");
        pwdReset.setPasswordConfirm("newPass01/");
        pwdReset.setEmail(huser.getEmail());
        pwdReset.setResetCode(null);
        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.resetPassword(pwdReset);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTWrongUserPasswordResetCode",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(0).isEmpty());
    }


    @Test
    public void test078_resetPasswordShouldFailIfResetCodeIsEmpty() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // HUser reset password with the following call resetPassword, but
        // resetCode is empty
        // response status code '422' HyperIoTWrongUserPasswordResetCode
        HUser huser = requestResetPassword();
        HUserPasswordReset pwdReset = new HUserPasswordReset();
        pwdReset.setPassword("newPass01/");
        pwdReset.setPasswordConfirm("newPass01/");
        pwdReset.setEmail(huser.getEmail());
        pwdReset.setResetCode("");
        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.resetPassword(pwdReset);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTWrongUserPasswordResetCode",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(0).isEmpty());
    }

    @Test
    public void test079_resetPasswordShouldFailIfResetCodeIsMaliciousCode() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // HUser reset password with the following call resetPassword, but
        // resetCode is malicious code
        // response status code '422' HyperIoTWrongUserPasswordResetCode
        HUser huser = requestResetPassword();
        HUserPasswordReset pwdReset = new HUserPasswordReset();
        pwdReset.setPassword("newPass01/");
        pwdReset.setPasswordConfirm("newPass01/");
        pwdReset.setEmail(huser.getEmail());
        pwdReset.setResetCode("eval(malicious code)");
        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.resetPassword(pwdReset);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTWrongUserPasswordResetCode",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(0).isEmpty());
    }


    @Test
    public void test080_resetPasswordShouldFailIfMailNotFound() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // HUser reset password with the following call resetPassword, but
        // user email not found
        // response status code '404' HyperIoTEntityNotFound
        HUser huser = requestResetPassword();
        HUserPasswordReset pwdReset = new HUserPasswordReset();
        pwdReset.setPassword("newPass01/");
        pwdReset.setPasswordConfirm("newPass01/");
        pwdReset.setEmail("mailNotFound");
        String resetCode = huser.getPasswordResetCode();
        pwdReset.setResetCode(resetCode);
        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.resetPassword(pwdReset);
        Assert.assertEquals(404, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTEntityNotFound",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test081_resetPasswordShouldFailIfPasswordIsNull() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // HUser reset password with the following call resetPassword, but
        // password is null
        // response status code '500' HyperIoTRuntimeException
        HUser huser = requestResetPassword();
        HUserPasswordReset pwdReset = new HUserPasswordReset();
        pwdReset.setPassword(null);
        pwdReset.setPasswordConfirm("newPass01/");
        pwdReset.setEmail(huser.getEmail());
        String resetCode = huser.getPasswordResetCode();
        pwdReset.setResetCode(resetCode);
        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.resetPassword(pwdReset);
        Assert.assertEquals(500, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTRuntimeException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().size());
        Assert.assertEquals("it.acsoftware.hyperiot.error.huser.password.reset.not.null",
                ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(0));
    }

    @Test
    public void test082_resetPasswordShouldFailIfPasswordIsEmpty() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // HUser reset password with the following call resetPassword, but
        // password is empty
        // response status code '422' HyperIoTValidationException
        HUser huser = requestResetPassword();
        HUserPasswordReset pwdReset = new HUserPasswordReset();
        pwdReset.setPassword("");
        pwdReset.setPasswordConfirm("newPass01/");
        pwdReset.setEmail(huser.getEmail());
        String resetCode = huser.getPasswordResetCode();
        pwdReset.setResetCode(resetCode);
        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.resetPassword(pwdReset);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(4, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        boolean invalidPassword = false;
        boolean invalidPasswordConfirm = false;
        for (int i = 0; i < ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size(); i++) {
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-password")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-password", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPassword = true;
            }
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-passwordConfirm")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-passwordConfirm", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPasswordConfirm = true;
            }
        }
        Assert.assertTrue(invalidPassword);
        Assert.assertTrue(invalidPasswordConfirm);
    }

    @Test
    public void test083_resetPasswordShouldFailIfPasswordIsMaliciousCode() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // HUser reset password with the following call resetPassword, but
        // password is malicious code
        // response status code '422' HyperIoTValidationException
        HUser huser = requestResetPassword();
        HUserPasswordReset pwdReset = new HUserPasswordReset();
        pwdReset.setPassword("vbscript:");
        pwdReset.setPasswordConfirm("newPass01/");
        pwdReset.setEmail(huser.getEmail());
        String resetCode = huser.getPasswordResetCode();
        pwdReset.setResetCode(resetCode);
        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.resetPassword(pwdReset);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(5, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        boolean invalidPassword = false;
        boolean invalidPasswordConfirm = false;
        boolean passwordIsMaliciousCode = false ;
        for (int i = 0; i < ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size(); i++) {
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-password")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-password", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPassword = true;
            }
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-passwordConfirm")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-passwordConfirm", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPasswordConfirm = true;
            }
        }
        List<HyperIoTValidationError> validationError = ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors() ;
        for( HyperIoTValidationError error : validationError ){
            if(error.getField() != null && error.getField().equals("huser-password") && error.getInvalidValue() != null && error.getInvalidValue().equals("vbscript:")){
                passwordIsMaliciousCode = true ;
            }
        }
        Assert.assertTrue(invalidPassword);
        Assert.assertTrue(invalidPasswordConfirm);
        Assert.assertTrue(passwordIsMaliciousCode);
    }

    @Test
    public void test084_resetPasswordShouldFailIfPasswordConfirmIsNull() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // HUser reset password with the following call resetPassword, but
        // passwordConfirm is null
        // response status code '500' HyperIoTRuntimeException
        HUser huser = requestResetPassword();
        HUserPasswordReset pwdReset = new HUserPasswordReset();
        pwdReset.setPassword("newPass01/");
        pwdReset.setPasswordConfirm(null);
        pwdReset.setEmail(huser.getEmail());
        String resetCode = huser.getPasswordResetCode();
        pwdReset.setResetCode(resetCode);
        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.resetPassword(pwdReset);
        Assert.assertEquals(500, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTRuntimeException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().size());
        Assert.assertEquals("it.acsoftware.hyperiot.error.huser.password.reset.not.null",
                ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(0));
    }

    @Test
    public void test085_resetPasswordShouldFailIfPasswordConfirmIsEmpty() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // HUser reset password with the following call resetPassword, but
        // passwordConfirm is empty
        // response status code '422' HyperIoTValidationException
        HUser huser = requestResetPassword();
        HUserPasswordReset pwdReset = new HUserPasswordReset();
        pwdReset.setPassword("newPass01/");
        pwdReset.setPasswordConfirm("");
        pwdReset.setEmail(huser.getEmail());
        String resetCode = huser.getPasswordResetCode();
        pwdReset.setResetCode(resetCode);
        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.resetPassword(pwdReset);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(4, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        boolean invalidPassword = false;
        boolean invalidPasswordConfirm = false;
        for (int i = 0; i < ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size(); i++) {
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-password")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-password", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPassword = true;
            }
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-passwordConfirm")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-passwordConfirm", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPasswordConfirm = true;
            }
        }
        Assert.assertTrue(invalidPassword);
        Assert.assertTrue(invalidPasswordConfirm);
    }

    @Test
    public void test086_resetPasswordShouldFailIfPasswordConfirmIsMaliciousCode() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // HUser reset password with the following call resetPassword, but
        // passwordConfirm is malicious code
        // response status code '422' HyperIoTValidationException
        HUser huser = requestResetPassword();
        HUserPasswordReset pwdReset = new HUserPasswordReset();
        pwdReset.setPassword("newPass01/");
        pwdReset.setPasswordConfirm("vbscript:");
        pwdReset.setEmail(huser.getEmail());
        String resetCode = huser.getPasswordResetCode();
        pwdReset.setResetCode(resetCode);
        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.resetPassword(pwdReset);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(4, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        boolean invalidPassword = false;
        boolean invalidPasswordConfirm = false;
        for (int i = 0; i < ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size(); i++) {
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-password")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-password", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPassword = true;
            }
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-passwordConfirm")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-passwordConfirm", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPasswordConfirm = true;
            }
        }
        Assert.assertTrue(invalidPassword);
        Assert.assertTrue(invalidPasswordConfirm);
    }

    @Test
    public void test087_resetPasswordShouldFailIfPasswordAndPasswordConfirmAreDifferent() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // HUser reset password with the following call resetPassword, but
        // password and passwordConfirm are different
        // response status code '422' HyperIoTValidationException
        HUser huser = requestResetPassword();
        HUserPasswordReset pwdReset = new HUserPasswordReset();
        pwdReset.setPassword("newPass01/");
        pwdReset.setPasswordConfirm("newPass10/");
        pwdReset.setEmail(huser.getEmail());
        String resetCode = huser.getPasswordResetCode();
        pwdReset.setResetCode(resetCode);
        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.resetPassword(pwdReset);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(4, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        boolean invalidPassword = false;
        boolean invalidPasswordConfirm = false;
        for (int i = 0; i < ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size(); i++) {
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-password")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-password", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPassword = true;
            }
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-passwordConfirm")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-passwordConfirm", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPasswordConfirm = true;
            }
        }
        Assert.assertTrue(invalidPassword);
        Assert.assertTrue(invalidPasswordConfirm);
    }

    @Test
    public void test088_resetPasswordShouldFailIfPasswordIsMalformed() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // HUser reset password with the following call resetPassword, but
        // password is malformed
        // response status code '422' HyperIoTValidationException
        HUser huser = requestResetPassword();
        HUserPasswordReset pwdReset = new HUserPasswordReset();
        pwdReset.setPassword("new");
        pwdReset.setPasswordConfirm("newPass01/");
        pwdReset.setEmail(huser.getEmail());
        String resetCode = huser.getPasswordResetCode();
        pwdReset.setResetCode(resetCode);
        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.resetPassword(pwdReset);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(4, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        boolean invalidPassword = false;
        boolean invalidPasswordConfirm = false;
        for (int i = 0; i < ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size(); i++) {
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-password")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-password", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPassword = true;
            }
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-passwordConfirm")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-passwordConfirm", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPasswordConfirm = true;
            }
        }
        Assert.assertTrue(invalidPassword);
        Assert.assertTrue(invalidPasswordConfirm);
    }

    @Test
    public void test089_resetPasswordShouldFailIfPasswordConfirmIsMalformed() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // HUser reset password with the following call resetPassword, but
        // passwordConfirm is malformed
        // response status code '422' HyperIoTValidationException
        HUser huser = requestResetPassword();
        HUserPasswordReset pwdReset = new HUserPasswordReset();
        pwdReset.setPassword("newPass01/");
        pwdReset.setPasswordConfirm("new");
        pwdReset.setEmail(huser.getEmail());
        String resetCode = huser.getPasswordResetCode();
        pwdReset.setResetCode(resetCode);
        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.resetPassword(pwdReset);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(4, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        boolean invalidPassword = false;
        boolean invalidPasswordConfirm = false;
        for (int i = 0; i < ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size(); i++) {
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-password")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-password", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPassword = true;
            }
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-passwordConfirm")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-passwordConfirm", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPasswordConfirm = true;
            }
        }
        Assert.assertTrue(invalidPassword);
        Assert.assertTrue(invalidPasswordConfirm);
    }

    @Test
    public void test090_resetPasswordShouldFailIfResetCodeIsNullInDatabase() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // HUser tries to reset password with the following call resetPassword, but
        // resetCode is null in database
        // response status code '422' HyperIoTWrongUserPasswordResetCode
        HUser huser = createHUser();
        HUserPasswordReset pwdReset = new HUserPasswordReset();
        pwdReset.setPassword("newPass01/");
        pwdReset.setPasswordConfirm("newPass01/");
        pwdReset.setEmail(huser.getEmail());
        String resetCode = huser.getPasswordResetCode();
        pwdReset.setResetCode(resetCode);
        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.resetPassword(pwdReset);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTWrongUserPasswordResetCode",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(0).isEmpty());
    }

    @Test
    public void test091_findAllHUserPaginatedShouldWork() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // hadmin find all HUser with pagination with the following call findAllHUserPaginated
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        int delta = 10;
        int page = 1;
        List<HUser> husers = new ArrayList<>();
        int numbEntities = 8; // +1 account: adminUser
        for (int i = 0; i < numbEntities; i++) {
            HUser huser = createHUser();
            Assert.assertNotEquals(0, huser.getId());
            husers.add(huser);
        }
        Assert.assertEquals(numbEntities, husers.size());
        this.impersonateUser(huserRestService, adminUser);
        Response restResponse = huserRestService.findAllHUserPaginated(delta, page);
        HyperIoTPaginableResult<HUser> listHUsers = restResponse
                .readEntity(new GenericType<HyperIoTPaginableResult<HUser>>() {
                });
        Assert.assertFalse(listHUsers.getResults().isEmpty());
        Assert.assertEquals(numbEntities + 1, listHUsers.getResults().size());
        Assert.assertEquals(delta, listHUsers.getDelta());
        Assert.assertEquals(page, listHUsers.getCurrentPage());
        Assert.assertEquals(page, listHUsers.getNextPage());
        // delta is 10, page 1: 9 entities stored in database
        Assert.assertEquals(1, listHUsers.getNumPages());
        Assert.assertEquals(200, restResponse.getStatus());
    }

    @Test
    public void test092_findAllHUserPaginatedShouldWorkIfDeltaAndPageIsNull() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // hadmin find all HUser with pagination with the following call findAllHUserPaginated
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        Integer delta = null;
        Integer page = null;
        List<HUser> husers;
        husers = new ArrayList<>();
        int numbEntities = 4; // +1 account: adminUser
        for (int i = 0; i < numbEntities; i++) {
            HUser huser = createHUser();
            Assert.assertNotEquals(0, huser.getId());
            husers.add(huser);
        }
        Assert.assertEquals(numbEntities, husers.size());
        this.impersonateUser(huserRestService, adminUser);
        Response restResponse = huserRestService.findAllHUserPaginated(delta, page);
        HyperIoTPaginableResult<HUser> listHUsers = restResponse
                .readEntity(new GenericType<HyperIoTPaginableResult<HUser>>() {
                });
        Assert.assertFalse(listHUsers.getResults().isEmpty());
        Assert.assertEquals(numbEntities + 1, listHUsers.getResults().size());
        Assert.assertEquals(defaultDelta, listHUsers.getDelta());
        Assert.assertEquals(defaultPage, listHUsers.getCurrentPage());
        Assert.assertEquals(defaultPage, listHUsers.getNextPage());
        // default delta is 10, default page is 1: 5 entities stored in database
        Assert.assertEquals(1, listHUsers.getNumPages());
        Assert.assertEquals(200, restResponse.getStatus());
    }

    @Test
    public void test093_findAllHUserPaginatedShouldWorkIfDeltaIsLowerThanZero() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // hadmin find all HUser with pagination with the following call findAllHUserPaginated
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        int delta = -1;
        int page = 2;
        List<HUser> husers = new ArrayList<>();
        int numbEntities = 10; // +1 account: adminUser
        for (int i = 0; i < numbEntities; i++) {
            HUser huser = createHUser();
            Assert.assertNotEquals(0, huser.getId());
            husers.add(huser);
        }
        Assert.assertEquals(numbEntities, husers.size());
        this.impersonateUser(huserRestService, adminUser);
        Response restResponse = huserRestService.findAllHUserPaginated(delta, page);
        HyperIoTPaginableResult<HUser> listHUsers = restResponse
                .readEntity(new GenericType<HyperIoTPaginableResult<HUser>>() {
                });
        Assert.assertFalse(listHUsers.getResults().isEmpty());
        Assert.assertEquals(1, listHUsers.getResults().size());
        Assert.assertEquals(defaultDelta, listHUsers.getDelta());
        Assert.assertEquals(page, listHUsers.getCurrentPage());
        Assert.assertEquals(defaultPage, listHUsers.getNextPage());
        // default delta is 10, page is 2: 11 entities stored in database
        Assert.assertEquals(2, listHUsers.getNumPages());
        Assert.assertEquals(200, restResponse.getStatus());

        //checks with page = 1
        this.impersonateUser(huserRestService, adminUser);
        Response restResponsePage1 = huserRestService.findAllHUserPaginated(delta, 1);
        HyperIoTPaginableResult<HUser> listHUsersPage1 = restResponsePage1
                .readEntity(new GenericType<HyperIoTPaginableResult<HUser>>() {
                });
        Assert.assertFalse(listHUsersPage1.getResults().isEmpty());
        Assert.assertEquals(defaultDelta, listHUsersPage1.getResults().size());
        Assert.assertEquals(defaultDelta, listHUsersPage1.getDelta());
        Assert.assertEquals(defaultPage, listHUsersPage1.getCurrentPage());
        Assert.assertEquals(page, listHUsersPage1.getNextPage());
        // default delta is 10, page is 1: 11 entities stored in database
        Assert.assertEquals(2, listHUsersPage1.getNumPages());
        Assert.assertEquals(200, restResponsePage1.getStatus());
    }

    @Test
    public void test094_findAllHUserPaginatedShouldWorkIfDeltaIsZero() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // hadmin find all HUser with pagination with the following call findAllHUserPaginated
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        int delta = 0;
        int page = 2;
        List<HUser> husers = new ArrayList<>();
        int numbEntities = 14; // +1 account: adminUser
        for (int i = 0; i < numbEntities; i++) {
            HUser huser = createHUser();
            Assert.assertNotEquals(0, huser.getId());
            husers.add(huser);
        }
        Assert.assertEquals(numbEntities, husers.size());
        this.impersonateUser(huserRestService, adminUser);
        Response restResponse = huserRestService.findAllHUserPaginated(delta, page);
        HyperIoTPaginableResult<HUser> listHUsers = restResponse
                .readEntity(new GenericType<HyperIoTPaginableResult<HUser>>() {
                });
        Assert.assertFalse(listHUsers.getResults().isEmpty());
        Assert.assertEquals(5, listHUsers.getResults().size());
        Assert.assertEquals(defaultDelta, listHUsers.getDelta());
        Assert.assertEquals(page, listHUsers.getCurrentPage());
        Assert.assertEquals(defaultPage, listHUsers.getNextPage());
        // because delta is 10, page is 2: 15 entities stored in database
        Assert.assertEquals(2, listHUsers.getNumPages());
        Assert.assertEquals(200, restResponse.getStatus());

        //checks with page = 1
        this.impersonateUser(huserRestService, adminUser);
        Response restResponsePage1 = huserRestService.findAllHUserPaginated(delta, 1);
        HyperIoTPaginableResult<HUser> listHUsersPage1 = restResponsePage1
                .readEntity(new GenericType<HyperIoTPaginableResult<HUser>>() {
                });
        Assert.assertFalse(listHUsersPage1.getResults().isEmpty());
        Assert.assertEquals(defaultDelta, listHUsersPage1.getResults().size());
        Assert.assertEquals(defaultDelta, listHUsersPage1.getDelta());
        Assert.assertEquals(defaultPage, listHUsersPage1.getCurrentPage());
        Assert.assertEquals(page, listHUsersPage1.getNextPage());
        // default delta is 10, page is 2: 15 entities stored in database
        Assert.assertEquals(2, listHUsersPage1.getNumPages());
        Assert.assertEquals(200, restResponsePage1.getStatus());
    }

    @Test
    public void test095_findAllHUserPaginatedShouldWorkIfPageIsLowerThanZero() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // hadmin find all HUser with pagination with the following call findAllHUserPaginated
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        int delta = 6;
        int page = -1;
        List<HUser> husers = new ArrayList<>();
        int numbEntities = 9; // +1 account: adminUser
        for (int i = 0; i < numbEntities; i++) {
            HUser huser = createHUser();
            Assert.assertNotEquals(0, huser.getId());
            husers.add(huser);
        }
        Assert.assertEquals(numbEntities, husers.size());
        this.impersonateUser(huserRestService, adminUser);
        Response restResponse = huserRestService.findAllHUserPaginated(delta, page);
        HyperIoTPaginableResult<HUser> listHUsers = restResponse
                .readEntity(new GenericType<HyperIoTPaginableResult<HUser>>() {
                });
        Assert.assertFalse(listHUsers.getResults().isEmpty());
        Assert.assertEquals(delta, listHUsers.getResults().size());
        Assert.assertEquals(delta, listHUsers.getDelta());
        Assert.assertEquals(defaultPage, listHUsers.getCurrentPage());
        Assert.assertEquals(defaultPage + 1, listHUsers.getNextPage());
        // delta is 6, default page 1: 10 entities stored in database
        Assert.assertEquals(2, listHUsers.getNumPages());
        Assert.assertEquals(200, restResponse.getStatus());

        //checks with page = 2
        this.impersonateUser(huserRestService, adminUser);
        Response restResponsePage2 = huserRestService.findAllHUserPaginated(delta, 2);
        HyperIoTPaginableResult<HUser> listHUsersPage2 = restResponsePage2
                .readEntity(new GenericType<HyperIoTPaginableResult<HUser>>() {
                });
        Assert.assertFalse(listHUsersPage2.getResults().isEmpty());
        Assert.assertEquals(4, listHUsersPage2.getResults().size());
        Assert.assertEquals(delta, listHUsersPage2.getDelta());
        Assert.assertEquals(defaultPage + 1, listHUsersPage2.getCurrentPage());
        Assert.assertEquals(defaultPage, listHUsersPage2.getNextPage());
        // delta is 6, page 2: 10 entities stored in database
        Assert.assertEquals(2, listHUsersPage2.getNumPages());
        Assert.assertEquals(200, restResponsePage2.getStatus());
    }

    @Test
    public void test096_findAllHUserPaginatedShouldWorkIfPageIsZero() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // hadmin find all HUser with pagination with the following call findAllHUser
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        int delta = 7;
        int page = 0;
        List<HUser> husers = new ArrayList<>();
        int numbEntities = 6; // +1 account: adminUser
        for (int i = 0; i < numbEntities; i++) {
            HUser huser = createHUser();
            Assert.assertNotEquals(0, huser.getId());
            husers.add(huser);
        }
        Assert.assertEquals(numbEntities, husers.size());
        this.impersonateUser(huserRestService, adminUser);
        Response restResponse = huserRestService.findAllHUserPaginated(delta, page);
        HyperIoTPaginableResult<HUser> listHUsers = restResponse
                .readEntity(new GenericType<HyperIoTPaginableResult<HUser>>() {
                });
        Assert.assertFalse(listHUsers.getResults().isEmpty());
        Assert.assertEquals(delta, listHUsers.getResults().size());
        Assert.assertEquals(delta, listHUsers.getDelta());
        Assert.assertEquals(defaultPage, listHUsers.getCurrentPage());
        Assert.assertEquals(defaultPage, listHUsers.getNextPage());
        // delta is 7, default page is 1: 7 entities stored in database
        Assert.assertEquals(1, listHUsers.getNumPages());
        Assert.assertEquals(200, restResponse.getStatus());
    }

    @Test
    public void test097_findAllHUserPaginatedShouldFailIfNotLogged() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // the following call tries to find all HUser with pagination, but HUser is
        // not logged
        // response status code '403' HyperIoTUnauthorizedException
        this.impersonateUser(huserRestService, null);
        Response restResponse = huserRestService.findAllHUserPaginated(defaultDelta, defaultPage);
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }


    @Test
    public void test098_changePasswordShouldWork() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // user changes his password with the following call changeHUserPassword
        // response status code '200'
        HUser huser = createHUser();
        String oldPassword = "passwordPass&01";
        String newPassword = "testPass01/";
        String passwordConfirm = "testPass01/";
        Assert.assertTrue(HyperIoTUtil.passwordMatches(oldPassword,huser.getPassword()));
        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.changeHUserPassword(huser.getId(), oldPassword, newPassword,
                passwordConfirm);
        Assert.assertEquals(200, restResponse.getStatus());
        Assert.assertTrue(HyperIoTUtil.passwordMatches(newPassword, ((HUser) restResponse.getEntity()).getPassword()));
        Assert.assertTrue(HyperIoTUtil.passwordMatches(passwordConfirm, ((HUser) restResponse.getEntity()).getPasswordConfirm()));
    }

    @Test
    public void test099_changePasswordShouldFailIfUserNotFound() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // user tries to change his password, with the following call changeHUserPassword,
        // but entity not found
        // response status code '403' HyperIoTUnauthorizedException
        HUser huser = createHUser();
        String oldPassword = "passwordPass&01";
        String newPassword = "testPass01/";
        String passwordConfirm = "testPass01/";
        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.changeHUserPassword(0, oldPassword, newPassword, passwordConfirm);
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test100_changePasswordShouldFailIfOldPasswordIsWrong() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // user tries to change his password, with the following call changeHUserPassword,
        // but oldPassword is wrong
        // response status code '500' HyperIoTRuntimeException
        HUser huser = createHUser();
        String oldPassword = "wrongPass";
        String newPassword = "testPass01/";
        String passwordConfirm = "testPass01/";
        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.changeHUserPassword(huser.getId(), oldPassword, newPassword,
                passwordConfirm);
        Assert.assertEquals(500, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTRuntimeException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().size());
        Assert.assertEquals("it.acsoftware.hyperiot.error.password.not.match",
                ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(0));
    }

    @Test
    public void test101_changePasswordShouldFailIfOldPasswordIsNull() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // user tries to change his password, with the following call changeHUserPassword,
        // but oldPassword is wrong
        // response status code '500' HyperIoTRuntimeException
        HUser huser = createHUser();
        String newPassword = "testPass01/";
        String passwordConfirm = "testPass01/";
        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.changeHUserPassword(huser.getId(), null, newPassword,
                passwordConfirm);
        Assert.assertEquals(500, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTRuntimeException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().size());
        Assert.assertEquals("it.acsoftware.hyperiot.error.password.not.null",
                ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(0));
    }


    @Test
    public void test102_changePasswordShouldFailIfOldPasswordIsEmpty() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // user tries to change his password, with the following call changeHUserPassword,
        // but oldPassword is empty
        // response status code '500' HyperIoTRuntimeException
        HUser huser = createHUser();
        String oldPassword = "";
        String newPassword = "testPass01/";
        String passwordConfirm = "testPass01/";
        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.changeHUserPassword(huser.getId(), oldPassword, newPassword,
                passwordConfirm);
        Assert.assertEquals(500, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTRuntimeException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().size());
        Assert.assertEquals("it.acsoftware.hyperiot.error.password.not.match",
                ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(0));
    }

    @Test
    public void test103_changePasswordShouldFailIfOldPasswordIsMaliciousCode() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // user tries to change password, with the following call changeHUserPassword,
        // but oldPassword is malicious code
        // response status code '500' HyperIoTRuntimeException
        HUser huser = createHUser();
        String oldPassword = "javascript:";
        String newPassword = "testPass01/";
        String passwordConfirm = "testPass01/";
        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.changeHUserPassword(huser.getId(), oldPassword, newPassword,
                passwordConfirm);
        Assert.assertEquals(500, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTRuntimeException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().size());
        Assert.assertEquals("it.acsoftware.hyperiot.error.password.not.match",
                ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(0));
    }

    @Test
    public void test104_changePasswordShouldFailIfNewPasswordIsNull() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // user tries to change his password, with the following call changeHUserPassword,
        // but newPassword is null
        // response status code '500' HyperIoTRuntimeException
        HUser huser = createHUser();
        String oldPassword = "passwordPass&01";
        String passwordConfirm = "testPass01/";
        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.changeHUserPassword(huser.getId(), oldPassword, null,
                passwordConfirm);
        Assert.assertEquals(500, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTRuntimeException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().size());
        Assert.assertEquals("it.acsoftware.hyperiot.error.password.not.null",
                ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(0));
    }

    @Test
    public void test105_changePasswordShouldFailIfNewPasswordIsEmpty() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // user tries to change his password, with the following call changeHUserPassword,
        // but newPassword is empty
        // response status code '422' HyperIoTValidationException
        HUser huser = createHUser();
        String oldPassword = "passwordPass&01";
        String newPassword = "";
        String passwordConfirm = "testPass01/";
        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.changeHUserPassword(huser.getId(), oldPassword, newPassword,
                passwordConfirm);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(4, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        boolean invalidPassword = false;
        boolean invalidPasswordConfirm = false;
        for (int i = 0; i < ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size(); i++) {
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-password")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-password", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPassword = true;
            }
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-passwordConfirm")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-passwordConfirm", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPasswordConfirm = true;
            }
        }
        Assert.assertTrue(invalidPassword);
        Assert.assertTrue(invalidPasswordConfirm);
    }

    @Test
    public void test106_changePasswordShouldFailIfNewPasswordIsMaliciousCode() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // user tries to change his password, with the following call changeHUserPassword,
        // but newPassword is malicious code
        // response status code '422' HyperIoTValidationException
        HUser huser = createHUser();
        String oldPassword = "passwordPass&01";
        String newPassword = "eval(malicious code)";
        String passwordConfirm = "testPass01/";
        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.changeHUserPassword(huser.getId(), oldPassword, newPassword,
                passwordConfirm);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(5, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        boolean invalidPassword = false;
        boolean invalidPasswordConfirm = false;
        boolean passwordIsMaliciousCode = false ;
        for (int i = 0; i < ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size(); i++) {
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-password")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-password", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPassword = true;
            }
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-passwordConfirm")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-passwordConfirm", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPasswordConfirm = true;
            }
        }
        List<HyperIoTValidationError> validationErrors = ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors();
        for( HyperIoTValidationError error : validationErrors){
            if(error.getField() != null && error.getField().equals("huser-password") && error.getInvalidValue() != null && error.getInvalidValue().equals("eval(malicious code)")){
                passwordIsMaliciousCode = true ;
            }
        }
        Assert.assertTrue(invalidPassword);
        Assert.assertTrue(invalidPasswordConfirm);
        Assert.assertTrue(passwordIsMaliciousCode);
    }

    @Test
    public void test107_changePasswordShouldFailIfPasswordConfirmIsNull() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // user tries to change his password, with the following call changeHUserPassword,
        // but passwordConfirm is null
        // response status code '500' HyperIoTRuntimeException
        HUser huser = createHUser();
        String oldPassword = "passwordPass&01";
        String newPassword = "testPass01/";
        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.changeHUserPassword(huser.getId(), oldPassword, newPassword,
                null);
        Assert.assertEquals(500, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTRuntimeException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().size());
        Assert.assertEquals("it.acsoftware.hyperiot.error.password.not.null",
                ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(0));
    }

    @Test
    public void test108_changePasswordShouldFailIfPasswordConfirmIsEmpty() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // user tries to change his password, with the following call changeHUserPassword,
        // but passwordConfirm is empty
        // response status code '422' HyperIoTValidationException
        HUser huser = createHUser();
        String oldPassword = "passwordPass&01";
        String newPassword = "testPass01/";
        String passwordConfirm = "";
        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.changeHUserPassword(huser.getId(), oldPassword, newPassword,
                passwordConfirm);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(4, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        boolean invalidPassword = false;
        boolean invalidPasswordConfirm = false;
        for (int i = 0; i < ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size(); i++) {
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-password")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-password", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPassword = true;
            }
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-passwordConfirm")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-passwordConfirm", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPasswordConfirm = true;
            }
        }
        Assert.assertTrue(invalidPassword);
        Assert.assertTrue(invalidPasswordConfirm);
    }

    @Test
    public void test109_changePasswordShouldFailIfPasswordConfirmIsMaliciousCode() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // user tries to change his password, with the following call changeHUserPassword,
        // but passwordConfirm is malicious code
        // response status code '422' HyperIoTValidationException
        HUser huser = createHUser();
        String oldPassword = "passwordPass&01";
        String newPassword = "testPass01/";
        String passwordConfirm = "javascript:";
        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.changeHUserPassword(huser.getId(), oldPassword, newPassword,
                passwordConfirm);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(4, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        boolean invalidPassword = false;
        boolean invalidPasswordConfirm = false;
        for (int i = 0; i < ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size(); i++) {
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-password")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-password", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPassword = true;
            }
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-passwordConfirm")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-passwordConfirm", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPasswordConfirm = true;
            }
        }
        Assert.assertTrue(invalidPassword);
        Assert.assertTrue(invalidPasswordConfirm);
    }

    @Test
    public void test110_changePasswordWithPermissionShouldWork() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // HUser, with permission, changes his password with the following call changeHUserPassword
        // response status code '200'
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(huserResourceName,
                HyperIoTCrudAction.UPDATE);
        HUser huser = createHUser(action);
        String oldPassword = "passwordPass&01";
        String newPassword = "testPass01/";
        String passwordConfirm = "testPass01/";
        Assert.assertTrue(HyperIoTUtil.passwordMatches(oldPassword,huser.getPassword()));
        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.changeHUserPassword(huser.getId(), oldPassword, newPassword,
                passwordConfirm);
        Assert.assertEquals(200, restResponse.getStatus());
        Assert.assertTrue(HyperIoTUtil.passwordMatches(newPassword, ((HUser) restResponse.getEntity()).getPassword()));
        Assert.assertTrue(HyperIoTUtil.passwordMatches(passwordConfirm, ((HUser) restResponse.getEntity()).getPasswordConfirm()));
        Assert.assertNotEquals(huser.getEntityVersion(),
                ((HUser) restResponse.getEntity()).getEntityVersion());
    }

    @Test
    public void test111_changePasswordWithPermissionShouldFailIfOldPasswordIsNull() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // HUser, with permission, tries to change his password with the
        // following call changePassword, but oldPassword is null
        // response status code '500' HyperIoTRuntimeException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(huserResourceName,
                HyperIoTCrudAction.UPDATE);
        HUser huser = createHUser(action);
        String newPassword = "testPass01/";
        String passwordConfirm = "testPass01/";
        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.changeHUserPassword(huser.getId(), null, newPassword,
                passwordConfirm);
        Assert.assertEquals(500, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTRuntimeException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().size());
        Assert.assertEquals("it.acsoftware.hyperiot.error.password.not.null",
                ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(0));
    }

    @Test
    public void test112_changePasswordWithPermissionShouldFailIfOldPasswordIsEmpty() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // HUser, with permission, tries to change his password with the
        // following call changePassword, but oldPassword is empty
        // response status code '500' HyperIoTRuntimeException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(huserResourceName,
                HyperIoTCrudAction.UPDATE);
        HUser huser = createHUser(action);
        String oldPassword = "";
        String newPassword = "testPass01/";
        String passwordConfirm = "testPass01/";
        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.changeHUserPassword(huser.getId(), oldPassword, newPassword,
                passwordConfirm);
        Assert.assertEquals(500, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTRuntimeException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().size());
        Assert.assertEquals("it.acsoftware.hyperiot.error.password.not.match",
                ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(0));
    }

    @Test
    public void test113_changePasswordWithPermissionShouldFailIfOldPasswordIsMaliciousCode() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // HUser, with permission, tries to change his password with the
        // following call changePassword, but oldPassword is malicious code
        // response status code '500' HyperIoTRuntimeException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(huserResourceName,
                HyperIoTCrudAction.UPDATE);
        HUser huser = createHUser(action);
        String oldPassword = "javascript:";
        String newPassword = "testPass01/";
        String passwordConfirm = "testPass01/";
        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.changeHUserPassword(huser.getId(), oldPassword, newPassword,
                passwordConfirm);
        Assert.assertEquals(500, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTRuntimeException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().size());
        Assert.assertEquals("it.acsoftware.hyperiot.error.password.not.match",
                ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(0));
    }

    @Test
    public void test114_changePasswordWithPermissionShouldFailIfNewPasswordIsNull() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // HUser, with permission, tries to change his password with the
        // following call changePassword, but newPassword is null
        // response status code '500' HyperIoTRuntimeException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(huserResourceName,
                HyperIoTCrudAction.UPDATE);
        HUser huser = createHUser(action);
        String oldPassword = "passwordPass&01";
        String passwordConfirm = "testPass01/";
        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.changeHUserPassword(huser.getId(), oldPassword, null,
                passwordConfirm);
        Assert.assertEquals(500, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTRuntimeException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().size());
        Assert.assertEquals("it.acsoftware.hyperiot.error.password.not.null",
                ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(0));
    }

    @Test
    public void test115_changePasswordWithPermissionShouldFailIfNewPasswordIsEmpty() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // HUser, with permission, tries to change his password with the
        // following call changePassword, but newPassword is empty
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(huserResourceName,
                HyperIoTCrudAction.UPDATE);
        HUser huser = createHUser(action);
        String oldPassword = "passwordPass&01";
        String newPassword = "";
        String passwordConfirm = "testPass01/";
        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.changeHUserPassword(huser.getId(), oldPassword, newPassword,
                passwordConfirm);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(4, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        boolean invalidPassword = false;
        boolean invalidPasswordConfirm = false;
        for (int i = 0; i < ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size(); i++) {
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-password")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-password", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPassword = true;
            }
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-passwordConfirm")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-passwordConfirm", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPasswordConfirm = true;
            }
        }
        Assert.assertTrue(invalidPassword);
        Assert.assertTrue(invalidPasswordConfirm);
    }

    @Test
    public void test116_changePasswordWithPermissionShouldFailIfNewPasswordIsMaliciousCode() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // HUser, with permission, tries to change his password with the
        // following call changePassword, but newPassword is malicious code
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(huserResourceName,
                HyperIoTCrudAction.UPDATE);
        HUser huser = createHUser(action);
        String oldPassword = "passwordPass&01";
        String newPassword = "javascript:";
        String passwordConfirm = "testPass01/";
        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.changeHUserPassword(huser.getId(), oldPassword, newPassword,
                passwordConfirm);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(5, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        boolean invalidPassword = false;
        boolean invalidPasswordConfirm = false;
        boolean passwordIsMaliciousCode = false ;
        for (int i = 0; i < ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size(); i++) {
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-password")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-password", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPassword = true;
            }
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-passwordConfirm")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-passwordConfirm", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPasswordConfirm = true;
            }
        }
        List<HyperIoTValidationError> validationError = ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors() ;
        for( HyperIoTValidationError error : validationError ){
            if(error.getField() != null && error.getField().equals("huser-password") && error.getInvalidValue() != null && error.getInvalidValue().equals("javascript:")){
                passwordIsMaliciousCode = true ;
            }
        }
        Assert.assertTrue(invalidPassword);
        Assert.assertTrue(invalidPasswordConfirm);
        Assert.assertTrue(passwordIsMaliciousCode);
    }

    @Test
    public void test117_changePasswordWithPermissionShouldFailIfPasswordConfirmIsNull() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // HUser, with permission, tries to change his password with the
        // following call changePassword, but passwordConfirm is null
        // response status code '500' HyperIoTRuntimeException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(huserResourceName,
                HyperIoTCrudAction.UPDATE);
        HUser huser = createHUser(action);
        String oldPassword = "passwordPass&01";
        String newPassword = "testPass01/";
        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.changeHUserPassword(huser.getId(), oldPassword, newPassword,
                null);
        Assert.assertEquals(500, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTRuntimeException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().size());
        Assert.assertEquals("it.acsoftware.hyperiot.error.password.not.null",
                ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(0));
    }

    @Test
    public void test118_changePasswordWithPermissionShouldFailIfPasswordConfirmIsEmpty() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // HUser, with permission, tries to change his password with the
        // following call changePassword, but passwordConfirm is empty
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(huserResourceName,
                HyperIoTCrudAction.UPDATE);
        HUser huser = createHUser(action);
        String oldPassword = "passwordPass&01";
        String newPassword = "testPass01/";
        String passwordConfirm = "";
        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.changeHUserPassword(huser.getId(), oldPassword, newPassword,
                passwordConfirm);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(4, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        boolean invalidPassword = false;
        boolean invalidPasswordConfirm = false;
        for (int i = 0; i < ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size(); i++) {
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-password")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-password", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPassword = true;
            }
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-passwordConfirm")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-passwordConfirm", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPasswordConfirm = true;
            }
        }
        Assert.assertTrue(invalidPassword);
        Assert.assertTrue(invalidPasswordConfirm);
    }

    @Test
    public void test119_changePasswordWithPermissionShouldFailIfPasswordConfirmIsMaliciousCode() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // HUser, with permission, tries to change his password with the
        // following call changePassword, but passwordConfirm is malicious code
        // response status code '422' HyperIoTValidationException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(huserResourceName,
                HyperIoTCrudAction.UPDATE);
        HUser huser = createHUser(action);
        String oldPassword = "passwordPass&01";
        String newPassword = "testPass01/";
        String passwordConfirm = "vbscript:";
        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.changeHUserPassword(huser.getId(), oldPassword, newPassword,
                passwordConfirm);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(4, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        boolean invalidPassword = false;
        boolean invalidPasswordConfirm = false;
        for (int i = 0; i < ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size(); i++) {
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-password")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-password", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPassword = true;
            }
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-passwordConfirm")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-passwordConfirm", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPasswordConfirm = true;
            }
        }
        Assert.assertTrue(invalidPassword);
        Assert.assertTrue(invalidPasswordConfirm);
    }

    @Test
    public void test120_changePasswordWithPermissionShouldFailIfEntityNotFound() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // HUser, with permission, tries to change his password with the
        // following call changePassword, but HUser id is zero
        // response status code '403' HyperIoTUnauthorizedException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(huserResourceName,
                HyperIoTCrudAction.UPDATE);
        HUser huser = createHUser(action);
        String oldPassword = "passwordPass&01";
        String newPassword = "testPass01/";
        String passwordConfirm = "testPass01/";
        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.changeHUserPassword(0, oldPassword, newPassword, passwordConfirm);
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test121_changePasswordWithoutPermissionShouldWork() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // HUser, without permission, changes his password with the following call changePassword
        // response status code '200'
        HUser huser = createHUser(null);
        String oldPassword = "passwordPass&01";
        String newPassword = "testPass01/";
        String passwordConfirm = "testPass01/";
        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.changeHUserPassword(huser.getId(), oldPassword, newPassword,
                passwordConfirm);
        Assert.assertEquals(200, restResponse.getStatus());
        Assert.assertTrue(HyperIoTUtil.passwordMatches(newPassword, ((HUser) restResponse.getEntity()).getPassword()));
        Assert.assertTrue(HyperIoTUtil.passwordMatches(passwordConfirm, ((HUser) restResponse.getEntity()).getPasswordConfirm()));
        Assert.assertNotEquals(HyperIoTUtil.getPasswordHash(oldPassword), ((HUser) restResponse.getEntity()).getPassword());
    }

    @Test
    public void test122_changePasswordWithoutPermissionShouldFailIfOldPasswordIsNull() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // HUser, without permission, tries to change his password with the
        // following call changePassword, but oldPassword is null
        // response status code '500' HyperIoTRuntimeException
        HUser huser = createHUser(null);
        String newPassword = "testPass01/";
        String passwordConfirm = "testPass01/";
        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.changeHUserPassword(huser.getId(), null, newPassword,
                passwordConfirm);
        Assert.assertEquals(500, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTRuntimeException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().size());
        Assert.assertEquals("it.acsoftware.hyperiot.error.password.not.null",
                ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(0));
    }

    @Test
    public void test123_changePasswordWithoutPermissionShouldFailIfOldPasswordIsEmpty() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // HUser, without permission, tries to change his password with the
        // following call changePassword, but oldPassword is empty
        // response status code '500' HyperIoTRuntimeException
        HUser huser = createHUser(null);
        String oldPassword = "";
        String newPassword = "testPass01/";
        String passwordConfirm = "testPass01/";
        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.changeHUserPassword(huser.getId(), oldPassword, newPassword,
                passwordConfirm);
        Assert.assertEquals(500, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTRuntimeException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().size());
        Assert.assertEquals("it.acsoftware.hyperiot.error.password.not.match",
                ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(0));
    }

    @Test
    public void test124_changePasswordWithoutPermissionShouldFailIfOldPasswordIsMaliciousCode() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // HUser, without permission, tries to change his password with the
        // following call changePassword, but oldPassword is malicious code
        // response status code '500' HyperIoTRuntimeException
        HUser huser = createHUser(null);
        String oldPassword = "javascript:";
        String newPassword = "testPass01/";
        String passwordConfirm = "testPass01/";
        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.changeHUserPassword(huser.getId(), oldPassword, newPassword,
                passwordConfirm);
        Assert.assertEquals(500, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTRuntimeException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().size());
        Assert.assertEquals("it.acsoftware.hyperiot.error.password.not.match",
                ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(0));
    }

    @Test
    public void test125_changePasswordWithoutPermissionShouldFailIfNewPasswordIsNull() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // HUser, without permission, tries to change his password with the
        // following call changePassword, but newPassword is null
        // response status code '500' HyperIoTRuntimeException
        HUser huser = createHUser(null);
        String oldPassword = "passwordPass&01";
        String passwordConfirm = "testPass01/";
        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.changeHUserPassword(huser.getId(), oldPassword, null,
                passwordConfirm);
        Assert.assertEquals(500, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTRuntimeException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().size());
        Assert.assertEquals("it.acsoftware.hyperiot.error.password.not.null",
                ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(0));
    }

    @Test
    public void test126_changePasswordWithoutPermissionShouldFailIfNewPasswordIsEmpty() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // HUser, without permission, tries to change his password with the
        // following call changePassword, but newPassword is empty
        // response status code '422' HyperIoTValidationException
        HUser huser = createHUser(null);
        String oldPassword = "passwordPass&01";
        String newPassword = "";
        String passwordConfirm = "testPass01/";
        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.changeHUserPassword(huser.getId(), oldPassword, newPassword,
                passwordConfirm);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(4, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        boolean invalidPassword = false;
        boolean invalidPasswordConfirm = false;
        for (int i = 0; i < ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size(); i++) {
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-password")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-password", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPassword = true;
            }
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-passwordConfirm")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-passwordConfirm", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPasswordConfirm = true;
            }
        }
        Assert.assertTrue(invalidPassword);
        Assert.assertTrue(invalidPasswordConfirm);
    }

    @Test
    public void test127_changePasswordWithoutPermissionShouldFailIfNewPasswordIsMaliciousCode() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // HUser, without permission, tries to change his password with the
        // following call changePassword, but newPassword is malicious code
        // response status code '422' HyperIoTValidationException
        HUser huser = createHUser(null);
        String oldPassword = "passwordPass&01";
        String newPassword = "javascript:";
        String passwordConfirm = "testPass01/";
        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.changeHUserPassword(huser.getId(), oldPassword, newPassword,
                passwordConfirm);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(5, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        boolean invalidPassword = false;
        boolean invalidPasswordConfirm = false;
        boolean passwordIsMaliciousCode = false ;
        for (int i = 0; i < ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size(); i++) {
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-password")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-password", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPassword = true;
            }
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-passwordConfirm")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-passwordConfirm", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPasswordConfirm = true;
            }
        }
        List<HyperIoTValidationError> validationError = ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors() ;
        for( HyperIoTValidationError error : validationError ){
            if(error.getField() != null && error.getField().equals("huser-password") && error.getInvalidValue() != null && error.getInvalidValue().equals("javascript:")){
                passwordIsMaliciousCode = true ;
            }
        }
        Assert.assertTrue(invalidPassword);
        Assert.assertTrue(invalidPasswordConfirm);
        Assert.assertTrue(passwordIsMaliciousCode);
    }

    @Test
    public void test128_changePasswordWithoutPermissionShouldFailIfPasswordConfirmIsNull() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // HUser, without permission, tries to change his password with the
        // following call changePassword, but passwordConfirm is null
        // response status code '500' HyperIoTRuntimeException
        HUser huser = createHUser(null);
        String oldPassword = "passwordPass&01";
        String newPassword = "testPass01/";
        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.changeHUserPassword(huser.getId(), oldPassword, newPassword,
                null);
        Assert.assertEquals(500, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTRuntimeException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().size());
        Assert.assertEquals("it.acsoftware.hyperiot.error.password.not.null",
                ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(0));
    }

    @Test
    public void test129_changePasswordWithoutPermissionShouldFailIfPasswordConfirmIsEmpty() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // HUser, without permission, tries to change his password with the
        // following call changePassword, but passwordConfirm is empty
        // response status code '422' HyperIoTValidationException
        HUser huser = createHUser(null);
        String oldPassword = "passwordPass&01";
        String newPassword = "testPass01/";
        String passwordConfirm = "";
        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.changeHUserPassword(huser.getId(), oldPassword, newPassword,
                passwordConfirm);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(4, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        boolean invalidPassword = false;
        boolean invalidPasswordConfirm = false;
        for (int i = 0; i < ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size(); i++) {
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-password")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-password", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPassword = true;
            }
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-passwordConfirm")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-passwordConfirm", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPasswordConfirm = true;
            }
        }
        Assert.assertTrue(invalidPassword);
        Assert.assertTrue(invalidPasswordConfirm);
    }

    @Test
    public void test130_changePasswordWithoutPermissionShouldFailIfPasswordConfirmIsMaliciousCode() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // HUser, without permission, tries to change his password with the
        // following call changePassword, but passwordConfirm is malicious code
        // response status code '422' HyperIoTValidationException
        HUser huser = createHUser(null);
        String oldPassword = "passwordPass&01";
        String newPassword = "testPass01/";
        String passwordConfirm = "vbscript:";
        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.changeHUserPassword(huser.getId(), oldPassword, newPassword,
                passwordConfirm);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(4, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        boolean invalidPassword = false;
        boolean invalidPasswordConfirm = false;
        for (int i = 0; i < ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size(); i++) {
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-password")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-password", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPassword = true;
            }
            if (((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField().contentEquals("huser-passwordConfirm")) {
                Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getMessage().isEmpty());
                Assert.assertEquals("huser-passwordConfirm", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(i).getField());
                invalidPasswordConfirm = true;
            }
        }
        Assert.assertTrue(invalidPassword);
        Assert.assertTrue(invalidPasswordConfirm);
    }

    @Test
    public void test131_changePasswordWithoutPermissionShouldFailIfEntityNotFound() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // HUser, without permission, tries to change his password with the
        // following call changePassword, but HUser id is zero
        // response status code '403' HyperIoTUnauthorizedException
        HUser huser = createHUser(null);
        String oldPassword = "passwordPass&01";
        String newPassword = "testPass01/";
        String passwordConfirm = "testPass01/";
        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.changeHUserPassword(0, oldPassword, newPassword, passwordConfirm);
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test132_changeAccountInfoShouldWork() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // HUser changes his account info with the following call changeAccountInfo
        // response status code '200'
        HUser huser = createHUser();
        huser.setName("temporaryNameUpdateAccount");
        huser.setLastname("temporaryLastnameUpdateAccount");
        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.updateAccountInfo(huser);
        Assert.assertEquals(200, restResponse.getStatus());
        Assert.assertEquals("temporaryNameUpdateAccount", ((HUser) restResponse.getEntity()).getName());
        Assert.assertEquals("temporaryLastnameUpdateAccount", ((HUser) restResponse.getEntity()).getLastname());
    }

    @Test
    public void test133_changeAccountInfoShouldFailIfNotLogged() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // the following call tries to change account info of HUser,
        // but huser is not logged
        // response status code '403' HyperIoTUnauthorizedException
        HUser huser = createHUser();
        huser.setName("temporaryNameUpdateAccount");
        huser.setLastname("temporaryLastnameUpdateAccount");
        this.impersonateUser(huserRestService, null);
        Response restResponse = huserRestService.updateAccountInfo(huser);
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test134_changeAccountInfoShouldFailIfUsernameIsDuplicated() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        HUserApi huserApi = getOsgiService(HUserApi.class);
        // HUser tries to change his account info with the following call updateAccountInfo,
        // but username is duplicated
        // response HyperIoTScreenNameAlreadyExistsException
        HUser huser = createHUser();
        HyperIoTContext ctx = huserRestService.impersonate(huser);
        huser.setName("name edited");
        huser.setLastname("lastname edited");
        huser.setUsername("hadmin");
        boolean usernameIsDuplicated = false;
        try {
            huserApi.updateAccountInfo(ctx, huser);
        } catch (HyperIoTScreenNameAlreadyExistsException ex) {
            Assert.assertEquals(hyperIoTException + "HyperIoTScreenNameAlreadyExistsException: Screen name already exists", ex.toString());
            Assert.assertFalse(ex.getMessage().isEmpty());
            Assert.assertEquals("username", ex.getFieldName());
            usernameIsDuplicated = true;
        }
        Assert.assertTrue(usernameIsDuplicated);
    }

    @Test
    public void test135_changeAccountInfoShouldFailIfEmailIsDuplicated() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // the following call tries to change account info of HUser,
        // but email is duplicated
        // response status code '409' HyperIoTDuplicateEntityException
        HUser huser = createHUser();
        huser.setEmail("hadmin@hyperiot.com");
        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.updateAccountInfo(huser);
        Assert.assertEquals(409, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTDuplicateEntityException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().size());
        Assert.assertEquals("email",
                ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(0));
    }

    @Test
    public void test136_changeAccountInfoShouldFailIfUserTryToChangeInfoOfAnotherUser() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // HUser 1 try to change account info of HUser with the following call
        // changeAccountInfo
        // response status code '403' HyperIoTUnauthorizedException
        HUser huser = createHUser();
        HUser huser1 = createHUser();
        this.impersonateUser(huserRestService, huser1);
        huser.setName("temporaryNameUpdateAccount");
        huser.setLastname("temporaryLastnameUpdateAccount");
        Response restResponse = huserRestService.updateAccountInfo(huser);
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test137_changeAccountInfoWithPermissionShouldWork() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // HUser changes his account info with Permission with the following call
        // changeAccountInfo
        // response status code '200'
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(huserResourceName,
                HyperIoTCrudAction.UPDATE);
        HUser huser = createHUser(action);
        huser.setName("updateNameAccount");
        huser.setLastname("updateLastnameAccount");
        huser.setEmail("updateemail@hyperiot.com");
        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.updateAccountInfo(huser);
        Assert.assertEquals(200, restResponse.getStatus());
        Assert.assertEquals("updateNameAccount", ((HUser) restResponse.getEntity()).getName());
        Assert.assertEquals("updateLastnameAccount", ((HUser) restResponse.getEntity()).getLastname());
        Assert.assertEquals("updateemail@hyperiot.com", ((HUser) restResponse.getEntity()).getEmail());
        Assert.assertNotEquals(huser.getEntityVersion(),
                ((HUser) restResponse.getEntity()).getEntityVersion());
    }

    @Test
    public void test138_changeAccountInfoWithPermissionShouldFailIfEmailIsDuplicated() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // HUser tries to change his account info with Permission with the following
        // call changeAccountInfo, but email is duplicated
        // response status code '409' HyperIoTDuplicateEntityException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(huserResourceName,
                HyperIoTCrudAction.UPDATE);
        HUser huser = createHUser(action);
        huser.setEmail("hadmin@hyperiot.com");
        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.updateAccountInfo(huser);
        Assert.assertEquals(409, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTDuplicateEntityException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().size());
        Assert.assertEquals("email",
                ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(0));
    }

    @Test
    public void test139_changeAccountInfoWithPermissionShouldFailIfUsernameIsDuplicated() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        HUserApi huserApi = getOsgiService(HUserApi.class);
        // HUser tries to change his account info with Permission with the following
        // call changeAccountInfo, but username is duplicated
        // response status code '422' HyperIoTDuplicateEntityException
        HyperIoTAction action = HyperIoTActionsUtil.getHyperIoTAction(huserResourceName,
                HyperIoTCrudAction.UPDATE);
        HUser huser = createHUser(action);
        HyperIoTContext ctx = huserRestService.impersonate(huser);
        huser.setUsername("hadmin");
        boolean usernameIsDuplicated = false;
        try {
            huserApi.updateAccountInfo(ctx, huser);
        } catch (HyperIoTScreenNameAlreadyExistsException ex) {
            Assert.assertEquals(hyperIoTException + "HyperIoTScreenNameAlreadyExistsException: Screen name already exists", ex.toString());
            Assert.assertFalse(ex.getMessage().isEmpty());
            Assert.assertEquals("username", ex.getFieldName());
            usernameIsDuplicated = true;
        }
        Assert.assertTrue(usernameIsDuplicated);
    }

    @Test
    public void test140_changeAccountInfoWithoutPermissionShouldWork() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // HUser, without Permission, changes his account info with
        // the following call changeAccountInfo
        // response status code '200'
        HUser huser = createHUser(null);
        huser.setName("updateNameAccount");
        huser.setLastname("updateLastnameAccount");
        this.impersonateUser(huserRestService, huser);
        Response restResponse = huserRestService.updateAccountInfo(huser);
        Assert.assertEquals(200, restResponse.getStatus());
        Assert.assertEquals("updateNameAccount", ((HUser) restResponse.getEntity()).getName());
        Assert.assertEquals("updateLastnameAccount", ((HUser) restResponse.getEntity()).getLastname());
    }

    @Test
    public void test141_saveHUserShouldFailIfUsernameIsDuplicated() {
        HUserRestApi hUserRestApi = getOsgiService(HUserRestApi.class);
        // hadmin tries to save HUser with the following call saveHUser, but
        // username is duplicated
        // response status code '422' HyperIoTScreenNameAlreadyExistsException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = createHUser();
        this.impersonateUser(hUserRestApi, adminUser);
        HUser huserDuplicated = new HUser();
        huserDuplicated.setName("name");
        huserDuplicated.setLastname("lastname");
        huserDuplicated.setUsername(huser.getUsername());
        huserDuplicated.setEmail("testusername" + UUID.randomUUID().toString() + "@hyperiot.com");
        huserDuplicated.setPassword("passwordPass&01");
        huserDuplicated.setPasswordConfirm("passwordPass&01");
        Response restResponse = hUserRestApi.saveHUser(huserDuplicated);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTScreenNameAlreadyExistsException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("username",
                ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
    }

    @Test
    public void test142_saveHUserShouldFailIfEmailIsDuplicated() {
        HUserRestApi hUserRestApi = getOsgiService(HUserRestApi.class);
        // hadmin tries to save HUser with the following call saveHUser, but
        // email is duplicated
        // response status code '409' HyperIoTDuplicateEntityException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = createHUser();
        this.impersonateUser(hUserRestApi, adminUser);
        HUser huserDuplicated = new HUser();
        huserDuplicated.setName("name");
        huserDuplicated.setLastname("lastname");
        huserDuplicated.setUsername("testusername" + UUID.randomUUID().toString().replaceAll("-", ""));
        huserDuplicated.setEmail(huser.getEmail());
        huserDuplicated.setPassword("passwordPass&01");
        huserDuplicated.setPasswordConfirm("passwordPass&01");
        Response restResponse = hUserRestApi.saveHUser(huserDuplicated);
        Assert.assertEquals(409, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTDuplicateEntityException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().size());
        Assert.assertEquals("email", ((HyperIoTBaseError) restResponse.getEntity()).getErrorMessages().get(0));
    }

    @Test
    public void test143_hadminActivateHUserAccountManually() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // hadmin activate HUser with the following call updateHUser.
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = registerHUser();
        Assert.assertFalse(huser.isActive());
        huser.setActive(true);
        this.impersonateUser(huserRestService, adminUser);
        Response restResponse = huserRestService.updateHUser(huser);
        Assert.assertEquals(200, restResponse.getStatus());
        Assert.assertTrue(huser.isActive());
        Assert.assertEquals(huser.isActive(), ((HUser) restResponse.getEntity()).isActive());
        Assert.assertEquals(huser.getEntityVersion() + 1,
                ((HUser) restResponse.getEntity()).getEntityVersion());
    }

    @Test
    public void test144_hadminDeactivateHUserAccountManually() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // hadmin deactivate HUser with the following call updateHUser.
        // response status code '200'
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = createHUser();
        Assert.assertTrue(huser.isActive());
        huser.setActive(false);
        this.impersonateUser(huserRestService, adminUser);
        Response restResponse = huserRestService.updateHUser(huser);
        Assert.assertEquals(200, restResponse.getStatus());
        Assert.assertFalse(huser.isActive());
        Assert.assertEquals(huser.isActive(), ((HUser) restResponse.getEntity()).isActive());
        Assert.assertEquals(huser.getEntityVersion() + 1,
                ((HUser) restResponse.getEntity()).getEntityVersion());
    }

    @Test
    public void test145_registerHUserShouldFailIfUsernameAlreadyExists() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // a new user tries to register with the platform,
        // but username already exists
        // response status code '422' HyperIoTValidationException
        HUser huserAlreadyStoredInDB = createHUser();
        HUser huser = new HUser();
        huser.setName("name" + java.util.UUID.randomUUID());
        huser.setLastname("lastname" + java.util.UUID.randomUUID());
        huser.setUsername(huserAlreadyStoredInDB.getUsername());
        huser.setEmail(java.util.UUID.randomUUID() + "@hyperiot.com");
        huser.setPassword("passwordPass&01");
        huser.setPasswordConfirm("passwordPass&01");
        this.impersonateUser(huserRestService, null);
        Response restResponse = huserRestService.register(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTScreenNameAlreadyExistsException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("username",
                ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(huserAlreadyStoredInDB.getUsername(),
                ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }


    @Test
    public void test146_saveHUserShouldFailIfUsernameAlreadyExists() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // hadmin tries to save HUser with the following call saveHUser,
        // but username already exists
        // response status code '422' HyperIoTScreenNameAlreadyExistsException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huserAlreadyStoredInDB = createHUser();
        HUser huser = new HUser();
        huser.setName("name");
        huser.setLastname("lastname");
        huser.setUsername(huserAlreadyStoredInDB.getUsername());
        huser.setEmail("testusername" + UUID.randomUUID().toString() + "@hyperiot.com");
        huser.setPassword("passwordPass&01");
        huser.setPasswordConfirm("passwordPass&01");
        this.impersonateUser(huserRestService, adminUser);
        Response restResponse = huserRestService.saveHUser(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTScreenNameAlreadyExistsException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("username",
                ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(huserAlreadyStoredInDB.getUsername(),
                ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }


    @Test
    public void test147_updateAccountInfoShouldFailIfUsernameAlreadyExists() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // hadmin tries to updateAccountInfo of huser with the following call updateAccountInfo,
        // but username already exists
        // response status code '422' HyperIoTScreenNameAlreadyExistsException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huserAlreadyStoredInDB = createHUser();
        HUser huser = createHUser();
        huser.setUsername(huserAlreadyStoredInDB.getUsername());
        this.impersonateUser(huserRestService, adminUser);
        Response restResponse = huserRestService.updateAccountInfo(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTScreenNameAlreadyExistsException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("username",
                ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(huserAlreadyStoredInDB.getUsername(),
                ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }

    @Test
    public void test148_registerHUserShouldFailIfNameIsGreaterThan255Chars() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // a new user tries to register with the platform,
        // but name is greater than 255 chars
        // response status code '422' HyperIoTValidationException
        HUser huser = new HUser();
        huser.setName(createStringFieldWithSpecifiedLenght(256));
        huser.setLastname("lastname" + java.util.UUID.randomUUID());
        huser.setUsername(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
        huser.setEmail(java.util.UUID.randomUUID() + "@hyperiot.com");
        huser.setPassword("passwordPass&01");
        huser.setPasswordConfirm("passwordPass&01");
        this.impersonateUser(huserRestService, null);
        Response restResponse = huserRestService.register(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("huser-name", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(huser.getName(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }

    @Test
    public void test149_registerHUserShouldFailIfLastNameIsGreaterThan255Chars() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // a new user tries to register with the platform,
        // but last name is greater than 255 chars
        // response status code '422' HyperIoTValidationException
        HUser huser = new HUser();
        huser.setName("name"+ java.util.UUID.randomUUID());
        huser.setLastname(createStringFieldWithSpecifiedLenght(256));
        huser.setUsername(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
        huser.setEmail(java.util.UUID.randomUUID() + "@hyperiot.com");
        huser.setPassword("passwordPass&01");
        huser.setPasswordConfirm("passwordPass&01");
        this.impersonateUser(huserRestService, null);
        Response restResponse = huserRestService.register(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("huser-lastname", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(huser.getLastname(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }

    @Test
    public void test150_registerHUserShouldFailIfUsernameIsGreaterThan255Chars() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // a new user tries to register with the platform,
        // but user name is greater than 255 chars
        // response status code '422' HyperIoTValidationException
        HUser huser = new HUser();
        huser.setName("name"+ java.util.UUID.randomUUID());
        huser.setLastname("lastname"+ java.util.UUID.randomUUID());
        huser.setUsername(createStringFieldWithSpecifiedLenght(256));
        huser.setEmail(java.util.UUID.randomUUID() + "@hyperiot.com");
        huser.setPassword("passwordPass&01");
        huser.setPasswordConfirm("passwordPass&01");
        this.impersonateUser(huserRestService, null);
        Response restResponse = huserRestService.register(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("huser-username", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(huser.getUsername(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }

    @Test
    public void test151_registerHUserShouldFailIfImagePathIsMaliciousCode() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // a new user tries to register with the platform,
        // but image path is malicious code
        // response status code '422' HyperIoTValidationException
        HUser huser = new HUser();
        huser.setName("name"+ java.util.UUID.randomUUID());
        huser.setLastname("lastname"+ java.util.UUID.randomUUID());
        huser.setUsername(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
        huser.setEmail(java.util.UUID.randomUUID() + "@hyperiot.com");
        huser.setPassword("passwordPass&01");
        huser.setPasswordConfirm("passwordPass&01");
        huser.setImagePath("javascript:");
        this.impersonateUser(huserRestService, null);
        Response restResponse = huserRestService.register(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("huser-imagepath", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(huser.getImagePath(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }

    @Test
    public void test152_registerHUserShouldFailIfImagePathIsGreaterThan255Chars() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // a new user tries to register with the platform,
        // but image path is greater than 255 chars.
        // response status code '422' HyperIoTValidationException
        HUser huser = new HUser();
        huser.setName("name"+ java.util.UUID.randomUUID());
        huser.setLastname("lastname"+ java.util.UUID.randomUUID());
        huser.setUsername(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
        huser.setEmail(java.util.UUID.randomUUID() + "@hyperiot.com");
        huser.setPassword("passwordPass&01");
        huser.setPasswordConfirm("passwordPass&01");
        huser.setImagePath(createStringFieldWithSpecifiedLenght(256));
        this.impersonateUser(huserRestService, null);
        Response restResponse = huserRestService.register(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("huser-imagepath", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(huser.getImagePath(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }

    @Test
    public void test153_registerHUserShouldFailIfEmailIsGreaterThan255Chars() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // a new user tries to register with the platform,
        // but email is greater than 255 chars.
        // response status code '422' HyperIoTValidationException
        HUser huser = new HUser();
        huser.setName("name"+ java.util.UUID.randomUUID());
        huser.setLastname("lastname"+ java.util.UUID.randomUUID());
        huser.setUsername(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
        huser.setEmail(createStringFieldWithSpecifiedLenght(256));
        huser.setPassword("passwordPass&01");
        huser.setPasswordConfirm("passwordPass&01");
        this.impersonateUser(huserRestService, null);
        Response restResponse = huserRestService.register(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        HyperIoTBaseError error = (HyperIoTBaseError) restResponse.getEntity();
        error.getValidationErrors().forEach((error1)-> System.out.println(error1.getMessage()));
        Assert.assertEquals(2, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("huser-email", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(huser.getEmail(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getMessage().isEmpty());
        Assert.assertEquals("huser-email", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getField());
        Assert.assertEquals(huser.getEmail(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getInvalidValue());
    }

    @Test
    public void test154_updateHUserShouldFailIfNameIsGreaterThan255Chars() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // hadmin tries to update HUser name with the following call updateHUser,
        // but name is greater than 255 chars
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = createHUser();
        huser.setName(createStringFieldWithSpecifiedLenght(256));
        this.impersonateUser(huserRestService, adminUser);
        Response restResponse = huserRestService.updateHUser(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("huser-name", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(huser.getName(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }

    @Test
    public void test155_updateHUserShouldFailIfLastNameIsGreaterThan255Chars() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // hadmin tries to update HUser name with the following call updateHUser,
        // but last name  greater than 255 chars
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = createHUser();
        huser.setLastname(createStringFieldWithSpecifiedLenght(256));
        this.impersonateUser(huserRestService, adminUser);
        Response restResponse = huserRestService.updateHUser(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("huser-lastname", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(huser.getLastname(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }

    @Test
    public void test156_updateHUserShouldFailIfUsernameIsGreaterThan255Chars() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // hadmin tries to update HUser name with the following call updateHUser,
        // but username is greater than 255 chars
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = createHUser();
        huser.setUsername(createStringFieldWithSpecifiedLenght(256));
        this.impersonateUser(huserRestService, adminUser);
        Response restResponse = huserRestService.updateHUser(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("huser-username", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(huser.getUsername(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }

    @Test
    public void test157_updateHUserShouldFailIfImagePathIsGreaterThan255Chars() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // hadmin tries to update HUser name with the following call updateHUser,
        // but image path  is greater than 255 chars
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = createHUser();
        huser.setImagePath(createStringFieldWithSpecifiedLenght(256));
        this.impersonateUser(huserRestService, adminUser);
        Response restResponse = huserRestService.updateHUser(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("huser-imagepath", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(huser.getImagePath(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }

    @Test
    public void test158_updateHUserShouldFailIfImagePathIsMaliciousCode() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // hadmin tries to update HUser name with the following call updateHUser,
        // but image path  is malicious code
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = createHUser();
        huser.setImagePath("javascript:");
        this.impersonateUser(huserRestService, adminUser);
        Response restResponse = huserRestService.updateHUser(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(1, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("huser-imagepath", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(huser.getImagePath(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
    }

    @Test
    public void test159_updateHUserShouldFailIfEmailIsGreaterThan255Chars() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // hadmin tries to update HUser name with the following call updateHUser,
        // but email is malicious code
        // response status code '422' HyperIoTValidationException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = createHUser();
        huser.setEmail(createStringFieldWithSpecifiedLenght(256));
        this.impersonateUser(huserRestService, adminUser);
        Response restResponse = huserRestService.updateHUser(huser);
        Assert.assertEquals(422, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTValidationException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        Assert.assertEquals(2, ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().size());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getMessage().isEmpty());
        Assert.assertEquals("huser-email", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getField());
        Assert.assertEquals(huser.getEmail(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(0).getInvalidValue());
        Assert.assertFalse(((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getMessage().isEmpty());
        Assert.assertEquals("huser-email", ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getField());
        Assert.assertEquals(huser.getEmail(), ((HyperIoTBaseError) restResponse.getEntity()).getValidationErrors().get(1).getInvalidValue());
    }

    @Test
    public void test160_shouldSaveHUserPersistEntityWithNullDeletionCode(){
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // hadmin save HUser with the following call, saveHUser
        // hadmin specify deletion code for HUser
        // Response status code '200' OK
        // Assert that huser's deletion code is null after the operation is complete. (Test relative to initial state)
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = new HUser();
        huser.setName("name");
        huser.setLastname("lastname");
        huser.setUsername("TestUser" + UUID.randomUUID().toString().replaceAll("-", ""));
        huser.setEmail("testusername" + UUID.randomUUID().toString() + "@hyperiot.com");
        huser.setPassword("passwordPass&01");
        huser.setPasswordConfirm("passwordPass&01");
        String deletionCode = UUID.randomUUID().toString();
        huser.setDeletionCode(deletionCode);
        this.impersonateUser(huserRestService, adminUser);
        Response restResponse = huserRestService.saveHUser(huser);
        Assert.assertEquals(200, restResponse.getStatus());
        Assert.assertNotEquals(0, ((HUser) restResponse.getEntity()).getId());
        Assert.assertNull(huser.getDeletionCode());
    }

    @Test
    public void test161_shouldUpdateHUserNotUpdateHUserDeletionCode(){
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // hadmin update HUser with the following call, updateHUser
        // hadmin specify a not null deletion code
        // Response status code 200 'OK'
        // Assert that huser's deletion code isn't change after the operation is complete.
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = createHUser();
        Assert.assertNotEquals(0, huser.getId());
        String huserDeletionCode = UUID.randomUUID().toString();
        //Set user deletion code directly from HUserRepository such that we need to know the deletion code to perform request(Raw deletion code is sent to user through email).
        HUserRepository hUserRepository = getOsgiService(HUserRepository.class);
        HUser user = hUserRepository.find(huser.getId(), null);
        Assert.assertEquals(user.getId(), huser.getId());
        Assert.assertNull(huser.getDeletionCode());
        user.setDeletionCode(HyperIoTUtil.encodeRawString(huserDeletionCode));
        HUser userWithDeletionCode = hUserRepository.update(user);
        Assert.assertNotNull(userWithDeletionCode.getDeletionCode());
        Assert.assertTrue(HyperIoTUtil.matchesEncoding(huserDeletionCode, userWithDeletionCode.getDeletionCode()));
        this.impersonateUser(huserRestService, adminUser);
        String newDeletionCode = "NewDeletionCode";
        Assert.assertNotEquals(newDeletionCode, huserDeletionCode);
        huser.setDeletionCode(newDeletionCode);
        Response restResponse = huserRestService.updateHUser(huser);
        Assert.assertEquals(200, restResponse.getStatus());
        Assert.assertEquals(huser.getId(), ((HUser) restResponse.getEntity()).getId());
        Assert.assertTrue(HyperIoTUtil.matchesEncoding(huserDeletionCode, ((HUser) restResponse.getEntity()).getDeletionCode()));
        Assert.assertFalse(HyperIoTUtil.matchesEncoding(newDeletionCode, ((HUser) restResponse.getEntity()).getDeletionCode()));
        //Assert that deletion code not change
        HUser huserAfterUpdate = hUserRepository.find(huser.getId(), null);
        Assert.assertNotNull(huserAfterUpdate);
        Assert.assertTrue(HyperIoTUtil.matchesEncoding(huserDeletionCode, huserAfterUpdate.getDeletionCode()));
        Assert.assertFalse(HyperIoTUtil.matchesEncoding(newDeletionCode, huserAfterUpdate.getDeletionCode()));
    }

    @Test
    public void test162_shouldRequestAccountDeletionFailWhenHUserIsNotLogged(){
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // huser request account deletion code with the following call requestAccountDeletion
        // but huser is not logged
        // response status code '403' (HyperIoTUnauthorizedException)
        huserRestService.impersonate(null);
        Response restResponse = huserRestService.requestAccountDeletion();
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test163_shouldRequestAccountDeletionWorkWhenHUserIsRegisteredUser(){
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // huser request account deletion code with the following call requestAccountDeletion
        // huser isn't admin user
        // response status code '200' OK
        // After requestAccountDeletion assert that huser deletion code is not null.
        HUser hUser = createHUser();
        Assert.assertNotEquals(0, hUser.getId());
        //Assert that deletion code is null when the user is created.
        Assert.assertNull(hUser.getDeletionCode());
        huserRestService.impersonate(hUser);
        Response response = huserRestService.requestAccountDeletion();
        Assert.assertEquals(200, response.getStatus());
        Assert.assertNull(response.getEntity());
        //Assert that deletion code is not null
        HUserRepository hUserRepository = getOsgiService(HUserRepository.class);
        HUser hUserAfterRequest = hUserRepository.find(hUser.getId(), null);
        Assert.assertEquals(hUser.getId(), hUserAfterRequest.getId());
        Assert.assertNotNull(hUserAfterRequest.getDeletionCode());
    }


    @Test
    public void test164_shouldDeleteAccountFailIfUserIsNotLogged(){
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // huser request account deletion with the following call deleteAccount
        // huser is not logged
        // response status code '403' HyperIoTUnauthorizedExecption
        HUser hUser = createHUser();
        Assert.assertNotEquals(0, hUser.getId());
        String huserDeletionCode = UUID.randomUUID().toString();
        //Set user deletion code directly from HUserRepository such that we need to know the deletion code to perform request(Raw deletion code is sent to user through email).
        HUserRepository hUserRepository = getOsgiService(HUserRepository.class);
        HUser user = hUserRepository.find(hUser.getId(), null);
        Assert.assertEquals(user.getId(), hUser.getId());
        Assert.assertNull(hUser.getDeletionCode());
        user.setDeletionCode(HyperIoTUtil.encodeRawString(huserDeletionCode));
        HUser userWithDeletionCode = hUserRepository.update(user);
        Assert.assertNotNull(userWithDeletionCode.getDeletionCode());
        Assert.assertTrue(HyperIoTUtil.matchesEncoding(huserDeletionCode, userWithDeletionCode.getDeletionCode()));
        huserRestService.impersonate(null);
        Response restResponse = huserRestService.deleteAccount(hUser.getId(), huserDeletionCode);
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test165_shouldDeleteAccountFailWhenAdminUserDeleteSelfAccount(){
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // hadmin request to delete his account with the following call deleteAccount
        // hadmin is an HyperIoT admin
        // response status code '403' HyperIoTUnauthorizedException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        String adminUserDeletionCode = UUID.randomUUID().toString();
        //Set user deletion code directly from HUserRepository such that we need to know the deletion code to perform request(Raw deletion code is sent to user through email).
        HUserRepository hUserRepository = getOsgiService(HUserRepository.class);
        HUser admin = hUserRepository.find(adminUser.getId(), null);
        Assert.assertEquals(admin.getId(), adminUser.getId());
        admin.setDeletionCode(HyperIoTUtil.encodeRawString(adminUserDeletionCode));
        HUser adminUserWithDeletionCode = hUserRepository.update(admin);
        Assert.assertNotNull(adminUserWithDeletionCode.getDeletionCode());
        Assert.assertTrue(HyperIoTUtil.matchesEncoding(adminUserDeletionCode, adminUserWithDeletionCode.getDeletionCode()));
        huserRestService.impersonate(adminUser);
        Response restResponse = huserRestService.deleteAccount(adminUser.getId(), adminUserDeletionCode);
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        //Reset to null admin deletion code.
        admin.setDeletionCode(null);
        HUser adminWithNullDeletionCode = hUserRepository.update(admin);
        Assert.assertNull(adminWithNullDeletionCode.getDeletionCode());
    }


    @Test
    public void test166_shouldDeleteAccountFailWhenDefaultUserDeleteAdminAccount(){
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // huser request to delete hadmin account with the following call deleteAccount
        // but huser is a generic user
        // response status code '403' HyperIoTUnauthorizedException
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = createHUser();
        Assert.assertNotEquals(0, huser.getId());
        String adminUserDeletionCode = UUID.randomUUID().toString();
        //Set user deletion code directly from HUserRepository such that we need to know the deletion code to perform request(Raw deletion code is sent to user through email).
        HUserRepository hUserRepository = getOsgiService(HUserRepository.class);
        HUser admin = hUserRepository.find(adminUser.getId(), null);
        Assert.assertEquals(admin.getId(), adminUser.getId());
        admin.setDeletionCode(HyperIoTUtil.encodeRawString(adminUserDeletionCode));
        HUser adminUserWithDeletionCode = hUserRepository.update(admin);
        Assert.assertNotNull(adminUserWithDeletionCode.getDeletionCode());
        Assert.assertTrue(HyperIoTUtil.matchesEncoding(adminUserDeletionCode, adminUserWithDeletionCode.getDeletionCode()));
        huserRestService.impersonate(huser);
        Response restResponse = huserRestService.deleteAccount(adminUser.getId(), adminUserDeletionCode);
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
        //Reset to null admin deletion code.
        admin.setDeletionCode(null);
        HUser adminWithNullDeletionCode = hUserRepository.update(admin);
        Assert.assertNull(adminWithNullDeletionCode.getDeletionCode());
    }

    @Test
    public void test167_shouldDeleteAccountFailWhenDefaultUserDeleteSelfAccountWithEmptyDeletionCode(){
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // huser request to delete self account with the following call deleteAccount
        // but deletion code is empty
        // response status code '403' HyperIoTUnauthorizedException
        HUser huser = createHUser();
        Assert.assertNotEquals(0, huser.getId());
        String huserDeletionCode = UUID.randomUUID().toString();
        //Set user deletion code directly from HUserRepository such that we need to know the deletion code to perform request(Raw deletion code is sent to user through email).
        HUserRepository hUserRepository = getOsgiService(HUserRepository.class);
        HUser user = hUserRepository.find(huser.getId(), null);
        Assert.assertEquals(user.getId(), huser.getId());
        Assert.assertNull(huser.getDeletionCode());
        user.setDeletionCode(HyperIoTUtil.encodeRawString(huserDeletionCode));
        HUser userWithDeletionCode = hUserRepository.update(user);
        Assert.assertNotNull(userWithDeletionCode.getDeletionCode());
        Assert.assertTrue(HyperIoTUtil.matchesEncoding(huserDeletionCode, userWithDeletionCode.getDeletionCode()));
        huserRestService.impersonate(huser);
        Response restResponse = huserRestService.deleteAccount(huser.getId(), "");
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test168_shouldDeleteAccountFailWhenDefaultUserDeleteSelfAccountWithNullDeletionCode(){
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // huser request to delete self account with the following call deleteAccount
        // but deletion code is null
        // response status code '403' HyperIoTUnauthorizedException
        HUser huser = createHUser();
        Assert.assertNotEquals(0, huser.getId());
        String huserDeletionCode = UUID.randomUUID().toString();
        //Set user deletion code directly from HUserRepository such that we need to know the deletion code to perform request(Raw deletion code is sent to user through email).
        HUserRepository hUserRepository = getOsgiService(HUserRepository.class);
        HUser user = hUserRepository.find(huser.getId(), null);
        Assert.assertEquals(user.getId(), huser.getId());
        Assert.assertNull(huser.getDeletionCode());
        user.setDeletionCode(HyperIoTUtil.encodeRawString(huserDeletionCode));
        HUser userWithDeletionCode = hUserRepository.update(user);
        Assert.assertNotNull(userWithDeletionCode.getDeletionCode());
        Assert.assertTrue(HyperIoTUtil.matchesEncoding(huserDeletionCode, userWithDeletionCode.getDeletionCode()));
        huserRestService.impersonate(huser);
        Response restResponse = huserRestService.deleteAccount(huser.getId(), null);
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test169_shouldDeleteAccountFailWhenDefaultUserDeleteSelfAccountWithWrongDeletionCode(){
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // huser request to delete self account with the following call deleteAccount
        // but deletion code is wrong
        // response status code '403' HyperIoTUnauthorizedException
        HUser huser = createHUser();
        Assert.assertNotEquals(0, huser.getId());
        String huserDeletionCode = UUID.randomUUID().toString();
        //Set user deletion code directly from HUserRepository such that we need to know the deletion code to perform request(Raw deletion code is sent to user through email).
        HUserRepository hUserRepository = getOsgiService(HUserRepository.class);
        HUser user = hUserRepository.find(huser.getId(), null);
        Assert.assertEquals(user.getId(), huser.getId());
        Assert.assertNull(huser.getDeletionCode());
        user.setDeletionCode(HyperIoTUtil.encodeRawString(huserDeletionCode));
        HUser userWithDeletionCode = hUserRepository.update(user);
        Assert.assertNotNull(userWithDeletionCode.getDeletionCode());
        Assert.assertTrue(HyperIoTUtil.matchesEncoding(huserDeletionCode, userWithDeletionCode.getDeletionCode()));
        String wrongDeletionCode = "WrongDeletionCode";
        Assert.assertNotEquals(wrongDeletionCode, huserDeletionCode);
        huserRestService.impersonate(huser);
        Response restResponse = huserRestService.deleteAccount(huser.getId(), wrongDeletionCode);
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test170_shouldDeleteAccountFailWhenDefaultUserDeleteAccountOwnedByAnotherUser(){
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // huser request to delete huser2 account with the following call deleteAccount
        // but huser not own account of huser2
        // response status code '403' HyperIoTUnauthorizedException
        HUser huser = createHUser();
        Assert.assertNotEquals(0, huser.getId());
        HUser huser2 = createHUser();
        Assert.assertNotEquals(0, huser2.getId());
        String huser2DeletionCode = UUID.randomUUID().toString();
        //Set huser2 deletion code directly from HUserRepository such that we need to know the deletion code to perform request(Raw deletion code is sent to user through email).
        HUserRepository hUserRepository = getOsgiService(HUserRepository.class);
        HUser user2 = hUserRepository.find(huser2.getId(), null);
        Assert.assertEquals(user2.getId(), huser2.getId());
        Assert.assertNull(user2.getDeletionCode());
        user2.setDeletionCode(HyperIoTUtil.encodeRawString(huser2DeletionCode));
        HUser user2WithDeletionCode = hUserRepository.update(user2);
        Assert.assertNotNull(user2WithDeletionCode.getDeletionCode());
        Assert.assertTrue(HyperIoTUtil.matchesEncoding(huser2DeletionCode, user2WithDeletionCode.getDeletionCode()));
        //log as huser
        impersonateUser(huserRestService, huser);
        //Configure DeletionCode with deletion code of huser2
        Response restResponse = huserRestService.deleteAccount(huser2.getId(), huser2DeletionCode);
        Assert.assertEquals(403, restResponse.getStatus());
        Assert.assertEquals(hyperIoTException + "HyperIoTUnauthorizedException",
                ((HyperIoTBaseError) restResponse.getEntity()).getType());
    }

    @Test
    public void test171_shouldDeleteAccountWorkWhenDefaultUserDeleteSelfAccount(){
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // huser request to delete self account with the following call deleteAccount
        // deletion code is right
        // response status code '200' OK
        // Assert that the user is removed
        HUser huser = createHUser();
        Assert.assertNotEquals(0, huser.getId());
        String huserDeletionCode = UUID.randomUUID().toString();
        //Set user deletion code directly from HUserRepository such that we need to know the deletion code to perform request(Raw deletion code is sent to user through email).
        HUserRepository hUserRepository = getOsgiService(HUserRepository.class);
        HUser user = hUserRepository.find(huser.getId(), null);
        Assert.assertEquals(user.getId(), huser.getId());
        Assert.assertNull(huser.getDeletionCode());
        user.setDeletionCode(HyperIoTUtil.encodeRawString(huserDeletionCode));
        HUser userWithDeletionCode = hUserRepository.update(user);
        Assert.assertNotNull(userWithDeletionCode.getDeletionCode());
        Assert.assertTrue(HyperIoTUtil.matchesEncoding(huserDeletionCode, userWithDeletionCode.getDeletionCode()));
        huserRestService.impersonate(huser);
        Response restResponse = huserRestService.deleteAccount(huser.getId(), huserDeletionCode);
        Assert.assertEquals(200, restResponse.getStatus());
        boolean deleted = false ;
        try {
            hUserRepository.find(huser.getId(), null);
        } catch (HyperIoTNoResultException e){
            deleted = true ;
        }
        Assert.assertTrue(deleted);
    }

    @Test
    public void test172_shouldDeleteAccountWorkWhenAdminUserDeleteAccountOwnedByDefaultUser(){
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        // hadmin request to delete huser account with the following call deleteAccount
        // hadmin is an HyperIoT Admin
        // response status code '200' OK
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        HUser huser = createHUser();
        Assert.assertNotEquals(0, huser.getId());
        String huserDeletionCode = UUID.randomUUID().toString();
        //Set user deletion code directly from HUserRepository such that we need to know the deletion code to perform request(Raw deletion code is sent to user through email).
        HUserRepository hUserRepository = getOsgiService(HUserRepository.class);
        HUser user = hUserRepository.find(huser.getId(), null);
        Assert.assertEquals(user.getId(), huser.getId());
        Assert.assertNull(huser.getDeletionCode());
        user.setDeletionCode(HyperIoTUtil.encodeRawString(huserDeletionCode));
        HUser userWithDeletionCode = hUserRepository.update(user);
        Assert.assertNotNull(userWithDeletionCode.getDeletionCode());
        Assert.assertTrue(HyperIoTUtil.matchesEncoding(huserDeletionCode, userWithDeletionCode.getDeletionCode()));
        //Log as hadmin
        impersonateUser(huserRestService, adminUser);
        Response restResponse = huserRestService.deleteAccount(huser.getId(), huserDeletionCode);
        Assert.assertEquals(200, restResponse.getStatus());
        boolean deleted = false ;
        try {
            hUserRepository.find(huser.getId(), null);
        } catch (HyperIoTNoResultException e){
            deleted = true ;
        }
        Assert.assertTrue(deleted);
    }




    /*
     *
     * Utility methods: create new HUser and assigns permissions
     *
     */

    private String createStringFieldWithSpecifiedLenght(int length) {
        String symbol = "a";
        String field = String.format("%" + length + "s", " ").replaceAll(" ", symbol);
        Assert.assertEquals(length, field.length());
        return field;
    }


    private HUser createHUser(HyperIoTAction action) {
        HUserRestApi hUserRestApi = getOsgiService(HUserRestApi.class);
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        this.impersonateUser(hUserRestApi, adminUser);
        String username = "TestUser";
        HUser huser = new HUser();
        huser.setName("name");
        huser.setLastname("lastname");
        huser.setUsername(username + UUID.randomUUID().toString().replaceAll("-", ""));
        huser.setEmail("testusername" + UUID.randomUUID().toString() + "@hyperiot.com");
        huser.setPassword("passwordPass&01");
        huser.setPasswordConfirm("passwordPass&01");
        huser.setActive(true);
        huser.setAdmin(false);
        Response restResponse = hUserRestApi.saveHUser(huser);
        Assert.assertEquals(200, restResponse.getStatus());
        Assert.assertNotEquals(0, ((HUser) restResponse.getEntity()).getId());
        Assert.assertEquals("name", ((HUser) restResponse.getEntity()).getName());
        Assert.assertEquals("lastname", ((HUser) restResponse.getEntity()).getLastname());
        Assert.assertEquals(huser.getUsername(), ((HUser) restResponse.getEntity()).getUsername());
        Assert.assertEquals(huser.getEmail(), ((HUser) restResponse.getEntity()).getEmail());
        Assert.assertFalse(huser.isAdmin());
        Assert.assertTrue(huser.isActive());
        if (action != null) {
            Role role = createRole();
            huser.addRole(role);
            RoleRestApi roleRestApi = getOsgiService(RoleRestApi.class);
            Response restUserRole = roleRestApi.saveUserRole(role.getId(), huser.getId());
            Assert.assertEquals(200, restUserRole.getStatus());
            Assert.assertTrue(huser.hasRole(role));
            utilGrantPermission(huser, role, action);
        }
        return huser;
    }

    private Permission utilGrantPermission(HUser huser, Role role, HyperIoTAction action) {
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        PermissionRestApi permissionRestApi = getOsgiService(PermissionRestApi.class);
        if (action == null) {
            return null;
        } else {
            PermissionSystemApi permissionSystemApi = getOsgiService(PermissionSystemApi.class);
            Permission testPermission = permissionSystemApi.findByRoleAndResourceName(role, action.getResourceName());
            if (testPermission == null) {
                Permission permission = new Permission();
                permission.setName(huserResourceName + " assigned to huser_id " + huser.getId());
                permission.setActionIds(action.getActionId());
                permission.setEntityResourceName(action.getResourceName());
                permission.setRole(role);
                this.impersonateUser(permissionRestApi, adminUser);
                Response restResponse = permissionRestApi.savePermission(permission);
                testPermission = permission;
                Assert.assertEquals(200, restResponse.getStatus());
                Assert.assertNotEquals(0, ((Permission) restResponse.getEntity()).getId());
                Assert.assertEquals(testPermission.getName(), ((Permission) restResponse.getEntity()).getName());
                Assert.assertEquals(testPermission.getActionIds(), ((Permission) restResponse.getEntity()).getActionIds());
                Assert.assertEquals(testPermission.getEntityResourceName(), ((Permission) restResponse.getEntity()).getEntityResourceName());
                Assert.assertEquals(testPermission.getRole().getId(), ((Permission) restResponse.getEntity()).getRole().getId());
            } else {
                this.impersonateUser(permissionRestApi, adminUser);
                testPermission.addPermission(action);
                Response restResponseUpdate = permissionRestApi.updatePermission(testPermission);
                Assert.assertEquals(200, restResponseUpdate.getStatus());
                Assert.assertEquals(testPermission.getActionIds(), ((Permission) restResponseUpdate.getEntity()).getActionIds());
                Assert.assertEquals(testPermission.getEntityVersion() + 1,
                        ((Permission) restResponseUpdate.getEntity()).getEntityVersion());
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
        Assert.assertEquals("Description", ((Role) restResponse.getEntity()).getDescription());
        return role;
    }

    private HUser requestResetPassword() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        this.impersonateUser(huserRestService, adminUser);
        String username = "TestUser";
        HUser huser = new HUser();
        huser.setName("name");
        huser.setLastname("lastname");
        huser.setUsername(username + UUID.randomUUID().toString().replaceAll("-", ""));
        huser.setEmail("testusername" + UUID.randomUUID().toString() + "@hyperiot.com");
        huser.setPassword("passwordPass01/");
        huser.setPasswordConfirm("passwordPass01/");
        huser.setActive(true);
        huser.setAdmin(false);
        huser.setPasswordResetCode(java.util.UUID.randomUUID().toString());
        Response restResponse = huserRestService.saveHUser(huser);
        Assert.assertEquals(200, restResponse.getStatus());
        return huser;
    }

    private HUser registerHUser() {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        this.impersonateUser(huserRestService, null);
        HUser huser = new HUser();
        huser.setName("Mark");
        huser.setLastname("Norris");
        huser.setUsername(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
        huser.setEmail(java.util.UUID.randomUUID() + "@hyperiot.com");
        huser.setActive(false);
        huser.setPassword("passwordPass&01");
        huser.setPasswordConfirm("passwordPass&01");
        Assert.assertNull(huser.getActivateCode());
        Response restResponse = huserRestService.register(huser);
        Assert.assertEquals(200, restResponse.getStatus());
        return huser;
    }

    private HUser createHUser() {
        HUserRestApi hUserRestApi = getOsgiService(HUserRestApi.class);
        AuthenticationApi authService = getOsgiService(AuthenticationApi.class);
        HyperIoTUser adminUser = (HUser) authService.login("hadmin", "admin");
        this.impersonateUser(hUserRestApi, adminUser);
        String username = "TestUser";
        HUser huser = new HUser();
        huser.setName("name");
        huser.setLastname("lastname");
        huser.setUsername(username + UUID.randomUUID().toString().replaceAll("-", ""));
        huser.setEmail("testusername" + UUID.randomUUID().toString() + "@hyperiot.com");
        huser.setPassword("passwordPass&01");
        huser.setPasswordConfirm("passwordPass&01");
        huser.setActive(true);
        huser.setAdmin(false);
        Response restResponse = hUserRestApi.saveHUser(huser);
        Assert.assertEquals(200, restResponse.getStatus());
        return huser;
    }

    /*
     *
     *
     * SERVICE LAYER
     *
     *
     */

    @Test
    public void test001sl_findUserByUsernameWork() {
        HUserApi huserApi = getOsgiService(HUserApi.class);
        // HUser find user by username with the following call
        // findUserByUsername
        HyperIoTUser adminUser = huserApi.findUserByUsername("hadmin");
        Assert.assertEquals("hadmin", adminUser.getUsername());
        Assert.assertNotNull(adminUser);
    }

    @Test
    public void test002sl_findUserByUsernameFailIfUsernameNotFound() {
        HUserApi huserApi = getOsgiService(HUserApi.class);
        // HUser tries to find user by username with the following call
        // findUserByUsername, but user not found
        HyperIoTUser adminUser = huserApi.findUserByUsername("wrongUsername");
        Assert.assertNull(adminUser);
    }

    @Test
    public void test003sl_registerUserWork() {
        HUserApi huserApi = getOsgiService(HUserApi.class);
        // the following call register a new HUser
        HUserRestApi huserRestApi = getOsgiService(HUserRestApi.class);
        HUser huser = new HUser();
        huser.setName("name" + java.util.UUID.randomUUID());
        huser.setLastname("lastname" + java.util.UUID.randomUUID());
        huser.setUsername(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
        huser.setEmail(java.util.UUID.randomUUID() + "@hyperiot.com");
        huser.setPassword("passwordPass&01");
        huser.setPasswordConfirm("passwordPass&01");
        huserApi.registerUser(huser, null);
        HyperIoTContext ctx = huserRestApi.impersonate(huser);
        Assert.assertNotNull(ctx);
        Assert.assertEquals(ctx.getLoggedUsername(), huser.getUsername());
        Assert.assertNotEquals(0, huser.getId());
    }

    @Test
    public void test004sl_registerUserFailIfNameIsEmpty() throws HyperIoTValidationException {
        HUserApi huserApi = getOsgiService(HUserApi.class);
        // HUser tries to register with the platform, but name is empty
        // response HyperIoTValidationException
        HUserRestApi huserRestApi = getOsgiService(HUserRestApi.class);
        boolean validationException = false;
        boolean validationExceptionIsMustNotBeEmpty = false;
        try {
            HUser huser = new HUser();
            huser.setName("");
            huser.setLastname("lastname" + java.util.UUID.randomUUID());
            huser.setUsername(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
            huser.setEmail(java.util.UUID.randomUUID() + "@hyperiot.com");
            huser.setPassword("passwordPass&01");
            huser.setPasswordConfirm("passwordPass&01");
            HyperIoTContext ctx = huserRestApi.impersonate(huser);
            Assert.assertNotNull(ctx);
            huserApi.registerUser(huser, ctx);
        } catch (HyperIoTValidationException ex) {
            validationException = true;
            Assert.assertEquals(1, ex.getViolations().size());
            for (ConstraintViolation violation : ex.getViolations()) {
                if (violation.getMessageTemplate().contains("{javax.validation.constraints.NotEmpty.message}")) {
                    validationExceptionIsMustNotBeEmpty = true;
                }
            }
        }
        Assert.assertTrue(validationException);
        Assert.assertTrue(validationExceptionIsMustNotBeEmpty);
    }

    @Test
    public void test005sl_registerUserFailIfNameIsNull() throws HyperIoTValidationException {
        HUserApi huserApi = getOsgiService(HUserApi.class);
        // HUser tries to register with the platform, but name is null
        // response HyperIoTValidationException
        HUserRestApi huserRestApi = getOsgiService(HUserRestApi.class);
        boolean validationException = false;
        boolean validationExceptionIsMustNotBeEmpty = false;
        boolean validationExceptionIsMustNotBeNull = false;
        try {
            HUser huser = new HUser();
            huser.setName(null);
            huser.setLastname("lastname" + java.util.UUID.randomUUID());
            huser.setUsername(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
            huser.setEmail(java.util.UUID.randomUUID() + "@hyperiot.com");
            huser.setPassword("passwordPass&01");
            huser.setPasswordConfirm("passwordPass&01");
            HyperIoTContext ctx = huserRestApi.impersonate(huser);
            Assert.assertNotNull(ctx);
            huserApi.registerUser(huser, ctx);
        } catch (HyperIoTValidationException ex) {
            validationException = true;
            Assert.assertEquals(2, ex.getViolations().size());
            for (ConstraintViolation violation : ex.getViolations()) {
                if (violation.getMessageTemplate().contains("{javax.validation.constraints.NotNull.message}")) {
                    validationExceptionIsMustNotBeNull = true;
                }
                if (violation.getMessageTemplate().contains("{javax.validation.constraints.NotEmpty.message}")) {
                    validationExceptionIsMustNotBeEmpty = true;
                }
            }
        }
        Assert.assertTrue(validationException);
        Assert.assertTrue(validationExceptionIsMustNotBeNull);
        Assert.assertTrue(validationExceptionIsMustNotBeEmpty);
    }

    @Test
    public void test006sl_registerUserFailIfNameIsMaliciousCode() throws HyperIoTValidationException {
        HUserApi huserApi = getOsgiService(HUserApi.class);
        // HUser tries to register with the platform, but name is malicious code
        // response HyperIoTValidationException
        HUserRestApi huserRestApi = getOsgiService(HUserRestApi.class);
        boolean validationException = false;
        boolean validationExceptionIsMaliciousCode = false;
        HUser huser = new HUser();
        try {
            huser.setName("javascript:");
            huser.setLastname("lastname" + java.util.UUID.randomUUID());
            huser.setUsername(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
            huser.setEmail(java.util.UUID.randomUUID() + "@hyperiot.com");
            huser.setPassword("passwordPass&01");
            huser.setPasswordConfirm("passwordPass&01");
            HyperIoTContext ctx = huserRestApi.impersonate(huser);
            Assert.assertNotNull(ctx);
            huserApi.registerUser(huser, ctx);
        } catch (HyperIoTValidationException ex) {
            validationException = true;
            Assert.assertEquals(1, ex.getViolations().size());
            for (ConstraintViolation violation : ex.getViolations()) {
                Assert.assertEquals(huser.getName(), violation.getInvalidValue());
                if (huser.getName().contains((CharSequence) violation.getInvalidValue())) {
                    validationExceptionIsMaliciousCode = true;
                }
            }
        }
        Assert.assertTrue(validationException);
        Assert.assertTrue(validationExceptionIsMaliciousCode);
    }

    @Test
    public void test007sl_registerUserFailIfLastnameIsEmpty() throws HyperIoTValidationException {
        HUserApi huserApi = getOsgiService(HUserApi.class);
        // HUser tries to register with the platform, but lastname is empty
        // response HyperIoTValidationException
        HUserRestApi huserRestApi = getOsgiService(HUserRestApi.class);
        boolean validationException = false;
        boolean validationExceptionIsMustNotBeEmpty = false;
        try {
            HUser huser = new HUser();
            huser.setName("name" + java.util.UUID.randomUUID());
            huser.setLastname("");
            huser.setUsername(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
            huser.setEmail(java.util.UUID.randomUUID() + "@hyperiot.com");
            huser.setPassword("passwordPass&01");
            huser.setPasswordConfirm("passwordPass&01");
            HyperIoTContext ctx = huserRestApi.impersonate(huser);
            Assert.assertNotNull(ctx);
            huserApi.registerUser(huser, ctx);
        } catch (HyperIoTValidationException ex) {
            validationException = true;
            Assert.assertEquals(1, ex.getViolations().size());
            for (ConstraintViolation violation : ex.getViolations()) {
                if (violation.getMessageTemplate().contains("{javax.validation.constraints.NotEmpty.message}")) {
                    validationExceptionIsMustNotBeEmpty = true;
                }
            }
        }
        Assert.assertTrue(validationException);
        Assert.assertTrue(validationExceptionIsMustNotBeEmpty);
    }

    @Test
    public void test008sl_registerUserFailIfLastnameIsNull() throws HyperIoTValidationException {
        HUserApi huserApi = getOsgiService(HUserApi.class);
        // HUser tries to register with the platform, but lastname is null
        // response HyperIoTValidationException
        HUserRestApi huserRestApi = getOsgiService(HUserRestApi.class);
        boolean validationException = false;
        boolean validationExceptionIsMustNotBeEmpty = false;
        boolean validationExceptionIsMustNotBeNull = false;
        try {
            HUser huser = new HUser();
            huser.setName("name" + java.util.UUID.randomUUID());
            huser.setLastname(null);
            huser.setUsername(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
            huser.setEmail(java.util.UUID.randomUUID() + "@hyperiot.com");
            huser.setPassword("passwordPass&01");
            huser.setPasswordConfirm("passwordPass&01");
            HyperIoTContext ctx = huserRestApi.impersonate(huser);
            Assert.assertNotNull(ctx);
            huserApi.registerUser(huser, ctx);
        } catch (HyperIoTValidationException ex) {
            validationException = true;
            Assert.assertEquals(2, ex.getViolations().size());
            for (ConstraintViolation violation : ex.getViolations()) {
                if (violation.getMessageTemplate().contains("{javax.validation.constraints.NotEmpty.message}")) {
                    validationExceptionIsMustNotBeEmpty = true;
                }
                if (violation.getMessageTemplate().contains("{javax.validation.constraints.NotNull.message}")) {
                    validationExceptionIsMustNotBeNull = true;
                }
            }
        }
        Assert.assertTrue(validationException);
        Assert.assertTrue(validationExceptionIsMustNotBeEmpty);
        Assert.assertTrue(validationExceptionIsMustNotBeNull);
    }

    @Test
    public void test009sl_registerUserFailIfLastnameIsMaliciousCode() throws HyperIoTValidationException {
        HUserApi huserApi = getOsgiService(HUserApi.class);
        // HUser tries to register with the platform, but lastname is malicious code
        // response HyperIoTValidationException
        HUserRestApi huserRestApi = getOsgiService(HUserRestApi.class);
        boolean validationException = false;
        boolean validationExceptionIsMaliciousCode = false;
        HUser huser = new HUser();
        try {
            huser.setName("name" + java.util.UUID.randomUUID());
            huser.setLastname("vbscript:");
            huser.setUsername(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
            huser.setEmail(java.util.UUID.randomUUID() + "@hyperiot.com");
            huser.setPassword("passwordPass&01");
            huser.setPasswordConfirm("passwordPass&01");
            HyperIoTContext ctx = huserRestApi.impersonate(huser);
            Assert.assertNotNull(ctx);
            huserApi.registerUser(huser, ctx);
        } catch (HyperIoTValidationException ex) {
            validationException = true;
            Assert.assertEquals(1, ex.getViolations().size());
            for (ConstraintViolation violation : ex.getViolations()) {
                Assert.assertEquals(huser.getLastname(), violation.getInvalidValue());
                if (huser.getLastname().contains((CharSequence) violation.getInvalidValue())) {
                    validationExceptionIsMaliciousCode = true;
                }
            }
        }
        Assert.assertTrue(validationException);
        Assert.assertTrue(validationExceptionIsMaliciousCode);
    }

    @Test
    public void test010sl_registerUserFailIfEmailIsMalformed() throws HyperIoTValidationException {
        HUserApi huserApi = getOsgiService(HUserApi.class);
        // HUser tries to register with the platform, but email is malformed
        // response HyperIoTValidationException
        HUserRestApi huserRestApi = getOsgiService(HUserRestApi.class);
        boolean validationException = false;
        boolean validationExceptionIsEmailMustBeWellFormed = false;
        try {
            HUser huser = new HUser();
            huser.setName("name" + java.util.UUID.randomUUID());
            huser.setLastname("lastname" + java.util.UUID.randomUUID());
            huser.setUsername(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
            huser.setEmail("wrongMail");
            huser.setPassword("passwordPass&01");
            huser.setPasswordConfirm("passwordPass&01");
            HyperIoTContext ctx = huserRestApi.impersonate(huser);
            Assert.assertNotNull(ctx);
            huserApi.registerUser(huser, ctx);
        } catch (HyperIoTValidationException ex) {
            validationException = true;
            Assert.assertEquals(1, ex.getViolations().size());
            for (ConstraintViolation violation : ex.getViolations()) {
                if (violation.getMessageTemplate().contains("{javax.validation.constraints.Email.message}")) {
                    validationExceptionIsEmailMustBeWellFormed = true;
                }
            }
        }
        Assert.assertTrue(validationException);
        Assert.assertTrue(validationExceptionIsEmailMustBeWellFormed);
    }

    @Test
    public void test011sl_registerUserFailIfEmailIsNull() throws HyperIoTValidationException {
        HUserApi huserApi = getOsgiService(HUserApi.class);
        // HUser tries to register with the platform, but email is null
        // response HyperIoTValidationException
        HUserRestApi huserRestApi = getOsgiService(HUserRestApi.class);
        boolean validationException = false;
        boolean validationExceptionIsMustNotBeEmpty = false;
        boolean validationExceptionIsMustNotBeNull = false;
        try {
            HUser huser = new HUser();
            huser.setName("name" + java.util.UUID.randomUUID());
            huser.setLastname("lastname" + java.util.UUID.randomUUID());
            huser.setUsername(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
            huser.setEmail(null);
            huser.setPassword("passwordPass&01");
            huser.setPasswordConfirm("passwordPass&01");
            HyperIoTContext ctx = huserRestApi.impersonate(huser);
            Assert.assertNotNull(ctx);
            huserApi.registerUser(huser, ctx);
        } catch (HyperIoTValidationException ex) {
            validationException = true;
            Assert.assertEquals(2, ex.getViolations().size());
            for (ConstraintViolation violation : ex.getViolations()) {
                if (violation.getMessageTemplate().contains("{javax.validation.constraints.NotEmpty.message}")) {
                    validationExceptionIsMustNotBeEmpty = true;
                }
                if (violation.getMessageTemplate().contains("{javax.validation.constraints.NotNull.message}")) {
                    validationExceptionIsMustNotBeNull = true;
                }
            }
        }
        Assert.assertTrue(validationException);
        Assert.assertTrue(validationExceptionIsMustNotBeEmpty);
        Assert.assertTrue(validationExceptionIsMustNotBeNull);
    }

    @Test
    public void test012sl_registerUserFailIfEmailIsEmpty() throws HyperIoTValidationException {
        HUserApi huserApi = getOsgiService(HUserApi.class);
        // HUser tries to register with the platform, but email is empty
        // response HyperIoTValidationException
        HUserRestApi huserRestApi = getOsgiService(HUserRestApi.class);
        boolean validationException = false;
        boolean validationExceptionIsMustNotBeEmpty = false;
        try {
            HUser huser = new HUser();
            huser.setName("name" + java.util.UUID.randomUUID());
            huser.setLastname("lastname" + java.util.UUID.randomUUID());
            huser.setUsername(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
            huser.setEmail("");
            huser.setPassword("passwordPass&01");
            huser.setPasswordConfirm("passwordPass&01");
            HyperIoTContext ctx = huserRestApi.impersonate(huser);
            Assert.assertNotNull(ctx);
            huserApi.registerUser(huser, ctx);
        } catch (HyperIoTValidationException ex) {
            validationException = true;
            Assert.assertEquals(1, ex.getViolations().size());
            for (ConstraintViolation violation : ex.getViolations()) {
                if (violation.getMessageTemplate().contains("{javax.validation.constraints.NotEmpty.message}")) {
                    validationExceptionIsMustNotBeEmpty = true;
                }
            }
        }
        Assert.assertTrue(validationException);
        Assert.assertTrue(validationExceptionIsMustNotBeEmpty);
    }

    @Test
    public void test013sl_registerUserFailIfEmailIsMaliciousCode() throws HyperIoTValidationException {
        HUserApi huserApi = getOsgiService(HUserApi.class);
        // HUser tries to register with the platform, but email is malicious code
        // response HyperIoTValidationException
        HUserRestApi huserRestApi = getOsgiService(HUserRestApi.class);
        boolean validationException = false;
        HUser huser = new HUser();
        try {
            huser.setName("name" + java.util.UUID.randomUUID());
            huser.setLastname("lastname" + java.util.UUID.randomUUID());
            huser.setUsername(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
            huser.setEmail("eval(malicious code)");
            huser.setPassword("passwordPass&01");
            huser.setPasswordConfirm("passwordPass&01");
            HyperIoTContext ctx = huserRestApi.impersonate(huser);
            Assert.assertNotNull(ctx);
            huserApi.registerUser(huser, ctx);
        } catch (HyperIoTValidationException ex) {
            Assert.assertEquals(2, ex.getViolations().size());
            for (ConstraintViolation violation : ex.getViolations()) {
                Assert.assertEquals(huser.getEmail(), violation.getInvalidValue());
                validationException = true;
            }
        }
        Assert.assertTrue(validationException);
    }

    @Test
    public void test014sl_registerUserFailIfUsernameIsMalformed() throws HyperIoTValidationException {
        HUserApi huserApi = getOsgiService(HUserApi.class);
        // HUser tries to register with the platform, but username is malformed
        // response HyperIoTValidationException
        boolean validationException = false;
        boolean validationExceptionIsAllowedLettersNumbers = false;
        HUser huser = new HUser();
        try {
            huser.setName("name" + java.util.UUID.randomUUID());
            huser.setLastname("lastname" + java.util.UUID.randomUUID());
            huser.setUsername("%&/%&%&/");
            huser.setEmail(java.util.UUID.randomUUID() + "@hyperiot.com");
            huser.setPassword("passwordPass&01");
            huser.setPasswordConfirm("passwordPass&01");
            huserApi.registerUser(huser, null);
        } catch (HyperIoTValidationException ex) {
            validationException = true;
            Assert.assertEquals(1, ex.getViolations().size());
            for (ConstraintViolation violation : ex.getViolations()) {
                Assert.assertEquals(huser.getUsername(), violation.getInvalidValue());
                if (huser.getUsername().contains((CharSequence) violation.getInvalidValue())) {
                    validationExceptionIsAllowedLettersNumbers = true;
                }
            }
        }
        Assert.assertTrue(validationException);
        Assert.assertTrue(validationExceptionIsAllowedLettersNumbers);
    }

    @Test
    public void test015sl_registerUserFailIfUsernameIsNull() throws HyperIoTValidationException {
        HUserApi huserApi = getOsgiService(HUserApi.class);
        // HUser tries to register with the platform, but username is null
        // response HyperIoTValidationException
        boolean validationException = false;
        boolean validationExceptionIsMustNotBeEmpty = false;
        boolean validationExceptionIsMustNotBeNull = false;
        try {
            HUser huser = new HUser();
            huser.setName("name" + java.util.UUID.randomUUID());
            huser.setLastname("lastname" + java.util.UUID.randomUUID());
            huser.setUsername(null);
            huser.setEmail(java.util.UUID.randomUUID() + "@hyperiot.com");
            huser.setPassword("passwordPass&01");
            huser.setPasswordConfirm("passwordPass&01");
            huserApi.registerUser(huser, null);
        } catch (HyperIoTValidationException ex) {
            validationException = true;
            Assert.assertEquals(2, ex.getViolations().size());
            for (ConstraintViolation violation : ex.getViolations()) {
                if (violation.getMessageTemplate().contains("{javax.validation.constraints.NotEmpty.message}")) {
                    validationExceptionIsMustNotBeEmpty = true;
                }
                if (violation.getMessageTemplate().contains("{javax.validation.constraints.NotNull.message}")) {
                    validationExceptionIsMustNotBeNull = true;
                }
            }
        }
        Assert.assertTrue(validationException);
        Assert.assertTrue(validationExceptionIsMustNotBeEmpty);
        Assert.assertTrue(validationExceptionIsMustNotBeNull);
    }

    @Test
    public void test016sl_registerUserFailIfUsernameIsEmpty() throws HyperIoTValidationException {
        HUserApi huserApi = getOsgiService(HUserApi.class);
        // HUser tries to register with the platform, but username is null
        // response HyperIoTValidationException
        HUserRestApi huserRestApi = getOsgiService(HUserRestApi.class);
        boolean validationException = false;
        HUser huser = new HUser();
        try {
            huser.setName("name" + java.util.UUID.randomUUID());
            huser.setLastname("lastname" + java.util.UUID.randomUUID());
            huser.setUsername("");
            huser.setEmail(java.util.UUID.randomUUID() + "@hyperiot.com");
            huser.setPassword("passwordPass&01");
            huser.setPasswordConfirm("passwordPass&01");
            HyperIoTContext ctx = huserRestApi.impersonate(huser);
            Assert.assertNotNull(ctx);
            huserApi.registerUser(huser, ctx);
        } catch (HyperIoTValidationException ex) {
            Assert.assertEquals(2, ex.getViolations().size());
            for (ConstraintViolation violation : ex.getViolations()) {
                Assert.assertEquals(huser.getUsername(), violation.getInvalidValue());
                validationException = true;
            }
        }
        Assert.assertTrue(validationException);
    }

    @Test
    public void test017sl_registerUserFailIfUsernameIsMaliciousCode() throws HyperIoTValidationException {
        HUserApi huserApi = getOsgiService(HUserApi.class);
        // HUser tries to register with the platform, but username is malicious code
        // response HyperIoTValidationException
        HUserRestApi huserRestApi = getOsgiService(HUserRestApi.class);
        boolean validationException = false;
        HUser huser = new HUser();
        try {
            huser.setName("name" + java.util.UUID.randomUUID());
            huser.setLastname("lastname" + java.util.UUID.randomUUID());
            huser.setUsername("</script>");
            huser.setEmail(java.util.UUID.randomUUID() + "@hyperiot.com");
            huser.setPassword("passwordPass&01");
            huser.setPasswordConfirm("passwordPass&01");
            HyperIoTContext ctx = huserRestApi.impersonate(huser);
            Assert.assertNotNull(ctx);
            huserApi.registerUser(huser, ctx);
        } catch (HyperIoTValidationException ex) {
            Assert.assertEquals(2, ex.getViolations().size());
            for (ConstraintViolation violation : ex.getViolations()) {
                Assert.assertEquals(huser.getUsername(), violation.getInvalidValue());
                validationException = true;
            }
        }
        Assert.assertTrue(validationException);
    }

    @Test
    public void test018sl_registerUserFailIfPasswordIsMalformed() throws HyperIoTValidationException {
        HUserApi huserApi = getOsgiService(HUserApi.class);
        // HUser tries to register with the platform, but password is malformed
        // response HyperIoTValidationException
        HUserRestApi huserRestApi = getOsgiService(HUserRestApi.class);
        boolean validationException = false;
        HUser huser = new HUser();
        try {
            huser.setName("name" + java.util.UUID.randomUUID());
            huser.setLastname("lastname" + java.util.UUID.randomUUID());
            huser.setUsername(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
            huser.setEmail(java.util.UUID.randomUUID() + "@hyperiot.com");
            huser.setPassword("malformed");
            huser.setPasswordConfirm("passwordPass&01");
            HyperIoTContext ctx = huserRestApi.impersonate(huser);
            Assert.assertNotNull(ctx);
            huserApi.registerUser(huser, ctx);
        } catch (HyperIoTValidationException ex) {
            Assert.assertEquals(2, ex.getViolations().size());
            validationException = true;
        }
        Assert.assertTrue(validationException);
    }

    @Test
    public void test019sl_registerUserFailIfPasswordIsNull() throws HyperIoTValidationException {
        HUserApi huserApi = getOsgiService(HUserApi.class);
        // HUser tries to register with the platform, but password is null
        // response HyperIoTValidationException
        HUserRestApi huserRestApi = getOsgiService(HUserRestApi.class);
        boolean validationException = false;
        try {
            HUser huser = new HUser();
            huser.setName("name" + UUID.randomUUID());
            huser.setLastname("lastname" + UUID.randomUUID());
            huser.setUsername(UUID.randomUUID().toString().replaceAll("-", ""));
            huser.setEmail(UUID.randomUUID() + "@hyperiot.com");
            huser.setPassword(null);
            huser.setPasswordConfirm("passwordPass&01");
            HyperIoTContext ctx = huserRestApi.impersonate(huser);
            Assert.assertNotNull(ctx);
            huserApi.registerUser(huser, ctx);
        } catch (HyperIoTValidationException ex) {
            Assert.assertEquals(3, ex.getViolations().size());
            validationException = true;
        }
        Assert.assertTrue(validationException);
    }

    @Test
    public void test020sl_registerUserFailIfPasswordIsEmpty() throws HyperIoTValidationException {
        HUserApi huserApi = getOsgiService(HUserApi.class);
        // HUser tries to register with the platform, but password is empty
        // response HyperIoTValidationException
        HUserRestApi huserRestApi = getOsgiService(HUserRestApi.class);
        boolean validationException = false;
        HUser huser = new HUser();
        try {
            huser.setName("name" + java.util.UUID.randomUUID());
            huser.setLastname("lastname" + java.util.UUID.randomUUID());
            huser.setUsername(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
            huser.setEmail(java.util.UUID.randomUUID() + "@hyperiot.com");
            huser.setPassword("");
            huser.setPasswordConfirm("passwordPass&01");
            HyperIoTContext ctx = huserRestApi.impersonate(huser);
            Assert.assertNotNull(ctx);
            huserApi.registerUser(huser, ctx);
        } catch (HyperIoTValidationException ex) {
            Assert.assertEquals(2, ex.getViolations().size());
            validationException = true;
        }
        Assert.assertTrue(validationException);
    }

    @Test
    public void test021sl_registerUserFailIfPasswordIsMaliciousCode() throws HyperIoTValidationException {
        HUserApi huserApi = getOsgiService(HUserApi.class);
        // HUser tries to register with the platform, but password is malicious code
        // response HyperIoTValidationException
        HUserRestApi huserRestApi = getOsgiService(HUserRestApi.class);
        boolean validationException = false;
        HUser huser = new HUser();
        try {
            huser.setName("name" + java.util.UUID.randomUUID());
            huser.setLastname("lastname" + java.util.UUID.randomUUID());
            huser.setUsername(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
            huser.setEmail(java.util.UUID.randomUUID() + "@hyperiot.com");
            huser.setPassword("eval(malicious code)");
            huser.setPasswordConfirm("passwordPass&01");
            HyperIoTContext ctx = huserRestApi.impersonate(huser);
            Assert.assertNotNull(ctx);
            huserApi.registerUser(huser, ctx);
        } catch (HyperIoTValidationException ex) {
            Assert.assertEquals(3, ex.getViolations().size());
            validationException = true;
        }
        Assert.assertTrue(validationException);
    }

    @Test
    public void test022sl_registerUserFailIfPasswordConfirmIsMalformed() throws HyperIoTValidationException {
        HUserApi huserApi = getOsgiService(HUserApi.class);
        // HUser tries to register with the platform, but passwordConfirm is malformed
        // response HyperIoTValidationException
        HUserRestApi huserRestApi = getOsgiService(HUserRestApi.class);
        boolean validationException = false;
        HUser huser = new HUser();
        try {
            huser.setName("name" + java.util.UUID.randomUUID());
            huser.setLastname("lastname" + java.util.UUID.randomUUID());
            huser.setUsername(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
            huser.setEmail(java.util.UUID.randomUUID() + "@hyperiot.com");
            huser.setPassword("passwordPass&01");
            huser.setPasswordConfirm("malformed");
            HyperIoTContext ctx = huserRestApi.impersonate(huser);
            Assert.assertNotNull(ctx);
            huserApi.registerUser(huser, ctx);
        } catch (HyperIoTValidationException ex) {
            Assert.assertEquals(2, ex.getViolations().size());
            validationException = true;
        }
        Assert.assertTrue(validationException);
    }

    @Test
    public void test023sl_registerUserFailIfPasswordConfirmIsNull() throws HyperIoTValidationException {
        HUserApi huserApi = getOsgiService(HUserApi.class);
        // HUser tries to register with the platform, but passwordConfirm is null
        // response HyperIoTValidationException
        HUserRestApi huserRestApi = getOsgiService(HUserRestApi.class);
        boolean validationException = false;
        try {
            HUser huser = new HUser();
            huser.setName("name" + java.util.UUID.randomUUID());
            huser.setLastname("lastname" + java.util.UUID.randomUUID());
            huser.setUsername(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
            huser.setEmail(java.util.UUID.randomUUID() + "@hyperiot.com");
            huser.setPassword("passwordPass&01");
            huser.setPasswordConfirm(null);
            HyperIoTContext ctx = huserRestApi.impersonate(huser);
            Assert.assertNotNull(ctx);
            huserApi.registerUser(huser, ctx);
        } catch (HyperIoTValidationException ex) {
            Assert.assertEquals(2, ex.getViolations().size());
            validationException = true;
        }
        Assert.assertTrue(validationException);
    }

    @Test
    public void test024sl_registerUserFailIfPasswordConfirmIsEmpty() throws HyperIoTValidationException {
        HUserApi huserApi = getOsgiService(HUserApi.class);
        // HUser tries to register with the platform, but passwordConfirm is empty
        // response HyperIoTValidationException
        HUserRestApi huserRestApi = getOsgiService(HUserRestApi.class);
        boolean validationException = false;
        try {
            HUser huser = new HUser();
            huser.setName("name" + java.util.UUID.randomUUID());
            huser.setLastname("lastname" + java.util.UUID.randomUUID());
            huser.setUsername(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
            huser.setEmail(java.util.UUID.randomUUID() + "@hyperiot.com");
            huser.setPassword("passwordPass&01");
            huser.setPasswordConfirm("");
            HyperIoTContext ctx = huserRestApi.impersonate(huser);
            Assert.assertNotNull(ctx);
            huserApi.registerUser(huser, ctx);
        } catch (HyperIoTValidationException ex) {
            Assert.assertEquals(2, ex.getViolations().size());
            validationException = true;
        }
        Assert.assertTrue(validationException);
    }

    @Test
    public void test025sl_registerUserFailIfPasswordConfirmIsMaliciousCode() throws HyperIoTValidationException {
        HUserApi huserApi = getOsgiService(HUserApi.class);
        // HUser tries to register with the platform, but passwordConfirm is malicious code
        // response HyperIoTValidationException
        HUserRestApi huserRestApi = getOsgiService(HUserRestApi.class);
        boolean validationException = false;
        try {
            HUser huser = new HUser();
            huser.setName("name" + java.util.UUID.randomUUID());
            huser.setLastname("lastname" + java.util.UUID.randomUUID());
            huser.setUsername(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
            huser.setEmail(java.util.UUID.randomUUID() + "@hyperiot.com");
            huser.setPassword("passwordPass&01");
            huser.setPasswordConfirm("</script>");
            HyperIoTContext ctx = huserRestApi.impersonate(huser);
            Assert.assertNotNull(ctx);
            huserApi.registerUser(huser, ctx);
        } catch (HyperIoTValidationException ex) {
            Assert.assertEquals(2, ex.getViolations().size());
            validationException = true;
        }
        Assert.assertTrue(validationException);
    }

    @Test
    public void test026sl_registerUserFailIfPasswordIsNotEqualsPasswordConfirm() throws HyperIoTValidationException {
        HUserApi huserApi = getOsgiService(HUserApi.class);
        // HUser tries to register with the platform,
        // but password is not equals to passwordConfirm
        // response HyperIoTValidationException
        HUserRestApi huserRestApi = getOsgiService(HUserRestApi.class);
        boolean validationException = false;
        try {
            HUser huser = new HUser();
            huser.setName("name" + java.util.UUID.randomUUID());
            huser.setLastname("lastname" + java.util.UUID.randomUUID());
            huser.setUsername(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
            huser.setEmail(java.util.UUID.randomUUID() + "@hyperiot.com");
            huser.setPassword("passwordPass&01");
            huser.setPasswordConfirm("pAssw0rdPass&01");
            HyperIoTContext ctx = huserRestApi.impersonate(huser);
            Assert.assertNotNull(ctx);
            huserApi.registerUser(huser, ctx);
        } catch (HyperIoTValidationException ex) {
            Assert.assertEquals(2, ex.getViolations().size());
            validationException = true;
        }
        Assert.assertTrue(validationException);
    }

    @Test
    public void test027sl_changeAccountInfoShouldFailIfUsernameIsDuplicated() throws HyperIoTScreenNameAlreadyExistsException {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        HUserApi huserApi = getOsgiService(HUserApi.class);
        // HUser tries to change his account info with the following call updateAccountInfo,
        // but username is duplicated
        // response HyperIoTScreenNameAlreadyExistsException
        HUser huser = createHUser();
        HyperIoTContext ctx = huserRestService.impersonate(huser);
        huser.setName("name edited");
        huser.setLastname("lastname edited");
        huser.setUsername("hadmin");
        boolean usernameIsDuplicated = false;
        try {
            huserApi.updateAccountInfo(ctx, huser);
        } catch (HyperIoTScreenNameAlreadyExistsException ex) {
            Assert.assertEquals(hyperIoTException + "HyperIoTScreenNameAlreadyExistsException: Screen name already exists", ex.toString());
            Assert.assertFalse(ex.getMessage().isEmpty());
            Assert.assertEquals("username", ex.getFieldName());
            usernameIsDuplicated = true;
        }
        Assert.assertTrue(usernameIsDuplicated);
    }

    @Test
    public void test028sl_changeAccountInfoShouldFailIfEmailIsDuplicated() throws HyperIoTDuplicateEntityException {
        HUserRestApi huserRestService = getOsgiService(HUserRestApi.class);
        HUserApi huserApi = getOsgiService(HUserApi.class);
        // HUser tries to change his account info with the following call updateAccountInfo,
        // but email is duplicated
        // response HyperIoTDuplicateEntityException
        HUser huser = createHUser();
        HyperIoTContext ctx = huserRestService.impersonate(huser);
        huser.setName("name edited");
        huser.setLastname("lastname edited");
        huser.setEmail("hadmin@hyperiot.com");
        boolean emailIsDuplicated = false;
        try {
            huserApi.updateAccountInfo(ctx, huser);
        } catch (HyperIoTDuplicateEntityException ex) {
            Assert.assertEquals(hyperIoTException + "HyperIoTDuplicateEntityException: email", ex.toString());
            Assert.assertEquals("email", ex.getMessage());
            emailIsDuplicated = true;
        }
        Assert.assertTrue(emailIsDuplicated);
    }

    /*
     *
     *
     * SYSTEM SERVICE TESTS
     *
     *
     */

    @Test
    public void test001ssl_findUserByUsernameWork() {
        HUserSystemApi huserSystemApi = getOsgiService(HUserSystemApi.class);
        // HUser find user by username with the following call
        // findUserByUsername
        HyperIoTUser adminUser = huserSystemApi.findUserByUsername("hadmin");
        Assert.assertEquals("hadmin", adminUser.getUsername());
        Assert.assertNotNull(adminUser);
    }

    @Test
    public void test002ssl_findUserByUsernameFailIfUsernameNotFound() {
        HUserSystemApi huserSystemApi = getOsgiService(HUserSystemApi.class);
        // HUser tries to find user by username with the following call
        // findUserByUsername, but user not found
        HyperIoTUser user = huserSystemApi.findUserByUsername("wrongUsername");
        Assert.assertNull(user);
    }

    @Test
    public void test003ssl_registerUserWork() {
        HUserSystemApi huserSystemApi = getOsgiService(HUserSystemApi.class);
        // the following call register a new HUser
        HUserRestApi huserRestApi = getOsgiService(HUserRestApi.class);
        HUser huser = new HUser();
        huser.setName("name" + java.util.UUID.randomUUID());
        huser.setLastname("lastname" + java.util.UUID.randomUUID());
        huser.setUsername(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
        huser.setEmail(java.util.UUID.randomUUID() + "@hyperiot.com");
        huser.setPassword("passwordPass&01");
        huser.setPasswordConfirm("passwordPass&01");
        HyperIoTContext ctx = huserRestApi.impersonate(huser);
        huserSystemApi.registerUser(huser, ctx);
        Assert.assertEquals(ctx.getLoggedUsername(), huser.getUsername());
        Assert.assertNotNull(ctx);
        Assert.assertNotEquals(0, huser.getId());
    }

    @Test
    public void test004ssl_registerUserFailIfNameIsEmpty() throws HyperIoTValidationException {
        HUserSystemApi huserSystemApi = getOsgiService(HUserSystemApi.class);
        // HUser tries to register with the platform, but name is empty
        // response HyperIoTValidationException
        HUserRestApi huserRestApi = getOsgiService(HUserRestApi.class);
        boolean validationException = false;
        boolean validationExceptionIsMustNotBeEmpty = false;
        try {
            HUser huser = new HUser();
            huser.setName("");
            huser.setLastname("lastname" + java.util.UUID.randomUUID());
            huser.setUsername(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
            huser.setEmail(java.util.UUID.randomUUID() + "@hyperiot.com");
            huser.setPassword("passwordPass&01");
            huser.setPasswordConfirm("passwordPass&01");
            HyperIoTContext ctx = huserRestApi.impersonate(huser);
            Assert.assertNotNull(ctx);
            huserSystemApi.registerUser(huser, ctx);
        } catch (HyperIoTValidationException ex) {
            validationException = true;
            Assert.assertEquals(1, ex.getViolations().size());
            for (ConstraintViolation violation : ex.getViolations()) {
                if (violation.getMessageTemplate().contains("{javax.validation.constraints.NotEmpty.message}")) {
                    validationExceptionIsMustNotBeEmpty = true;
                }
            }
        }
        Assert.assertTrue(validationException);
        Assert.assertTrue(validationExceptionIsMustNotBeEmpty);
    }

    @Test
    public void test005ssl_registerUserFailIfNameIsNull() throws HyperIoTValidationException {
        HUserSystemApi huserSystemApi = getOsgiService(HUserSystemApi.class);
        // HUser tries to register with the platform, but name is null
        // response HyperIoTValidationException
        HUserRestApi huserRestApi = getOsgiService(HUserRestApi.class);
        boolean validationException = false;
        boolean validationExceptionIsMustNotBeEmpty = false;
        boolean validationExceptionIsMustNotBeNull = false;
        try {
            HUser huser = new HUser();
            huser.setName(null);
            huser.setLastname("lastname" + java.util.UUID.randomUUID());
            huser.setUsername(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
            huser.setEmail(java.util.UUID.randomUUID() + "@hyperiot.com");
            huser.setPassword("passwordPass&01");
            huser.setPasswordConfirm("passwordPass&01");
            HyperIoTContext ctx = huserRestApi.impersonate(huser);
            Assert.assertNotNull(ctx);
            huserSystemApi.registerUser(huser, ctx);
        } catch (HyperIoTValidationException ex) {
            validationException = true;
            Assert.assertEquals(2, ex.getViolations().size());
            for (ConstraintViolation violation : ex.getViolations()) {
                if (violation.getMessageTemplate().contains("{javax.validation.constraints.NotNull.message}")) {
                    validationExceptionIsMustNotBeNull = true;
                }
                if (violation.getMessageTemplate().contains("{javax.validation.constraints.NotEmpty.message}")) {
                    validationExceptionIsMustNotBeEmpty = true;
                }
            }
        }
        Assert.assertTrue(validationException);
        Assert.assertTrue(validationExceptionIsMustNotBeEmpty);
        Assert.assertTrue(validationExceptionIsMustNotBeNull);
    }

    @Test
    public void test006ssl_registerUserFailIfNameIsMaliciousCode() throws HyperIoTValidationException {
        HUserSystemApi huserSystemApi = getOsgiService(HUserSystemApi.class);
        // HUser tries to register with the platform, but name is malicious code
        // response HyperIoTValidationException
        HUserRestApi huserRestApi = getOsgiService(HUserRestApi.class);
        boolean validationException = false;
        boolean validationExceptionIsMaliciousCode = false;
        HUser huser = new HUser();
        try {
            huser.setName("eval(malicious code)");
            huser.setLastname("lastname" + java.util.UUID.randomUUID());
            huser.setUsername(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
            huser.setEmail(java.util.UUID.randomUUID() + "@hyperiot.com");
            huser.setPassword("passwordPass&01");
            huser.setPasswordConfirm("passwordPass&01");
            HyperIoTContext ctx = huserRestApi.impersonate(huser);
            Assert.assertNotNull(ctx);
            huserSystemApi.registerUser(huser, ctx);
        } catch (HyperIoTValidationException ex) {
            validationException = true;
            Assert.assertEquals(1, ex.getViolations().size());
            for (ConstraintViolation violation : ex.getViolations()) {
                Assert.assertEquals(huser.getName(), violation.getInvalidValue());
                if (huser.getName().contains((CharSequence) violation.getInvalidValue())) {
                    validationExceptionIsMaliciousCode = true;
                }
            }
        }
        Assert.assertTrue(validationException);
        Assert.assertTrue(validationExceptionIsMaliciousCode);
    }

    @Test
    public void test007ssl_registerUserFailIfLastnameIsEmpty() throws HyperIoTValidationException {
        HUserSystemApi huserSystemApi = getOsgiService(HUserSystemApi.class);
        // HUser tries to register with the platform, but lastname is empty
        // response HyperIoTValidationException
        HUserRestApi huserRestApi = getOsgiService(HUserRestApi.class);
        boolean validationException = false;
        boolean validationExceptionIsMustNotBeEmpty = false;
        try {
            HUser huser = new HUser();
            huser.setName("name" + java.util.UUID.randomUUID());
            huser.setLastname("");
            huser.setUsername(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
            huser.setEmail(java.util.UUID.randomUUID() + "@hyperiot.com");
            huser.setPassword("passwordPass&01");
            huser.setPasswordConfirm("passwordPass&01");
            HyperIoTContext ctx = huserRestApi.impersonate(huser);
            Assert.assertNotNull(ctx);
            huserSystemApi.registerUser(huser, ctx);
        } catch (HyperIoTValidationException ex) {
            validationException = true;
            Assert.assertEquals(1, ex.getViolations().size());
            for (ConstraintViolation violation : ex.getViolations()) {
                if (violation.getMessageTemplate().contains("{javax.validation.constraints.NotEmpty.message}")) {
                    validationExceptionIsMustNotBeEmpty = true;
                }
            }
        }
        Assert.assertTrue(validationException);
        Assert.assertTrue(validationExceptionIsMustNotBeEmpty);
    }

    @Test
    public void test008ssl_registerUserFailIfLastnameIsNull() throws HyperIoTValidationException {
        HUserSystemApi huserSystemApi = getOsgiService(HUserSystemApi.class);
        // HUser tries to register with the platform, but lastname is null
        // response HyperIoTValidationException
        HUserRestApi huserRestApi = getOsgiService(HUserRestApi.class);
        boolean validationException = false;
        boolean validationExceptionIsMustNotBeNull = false;
        boolean validationExceptionIsMustNotBeEmpty = false;
        try {
            HUser huser = new HUser();
            huser.setName("name" + java.util.UUID.randomUUID());
            huser.setLastname(null);
            huser.setUsername(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
            huser.setEmail(java.util.UUID.randomUUID() + "@hyperiot.com");
            huser.setPassword("passwordPass&01");
            huser.setPasswordConfirm("passwordPass&01");
            HyperIoTContext ctx = huserRestApi.impersonate(huser);
            Assert.assertNotNull(ctx);
            huserSystemApi.registerUser(huser, ctx);
        } catch (HyperIoTValidationException ex) {
            validationException = true;
            Assert.assertEquals(2, ex.getViolations().size());
            for (ConstraintViolation violation : ex.getViolations()) {
                if (violation.getMessageTemplate().contains("{javax.validation.constraints.NotNull.message}")) {
                    validationExceptionIsMustNotBeNull = true;
                }
                if (violation.getMessageTemplate().contains("{javax.validation.constraints.NotEmpty.message}")) {
                    validationExceptionIsMustNotBeEmpty = true;
                }
            }
        }
        Assert.assertTrue(validationException);
        Assert.assertTrue(validationExceptionIsMustNotBeNull);
        Assert.assertTrue(validationExceptionIsMustNotBeEmpty);
    }

    @Test
    public void test009ssl_registerUserFailIfLastnameIsMaliciousCode() throws HyperIoTValidationException {
        HUserSystemApi huserSystemApi = getOsgiService(HUserSystemApi.class);
        // HUser tries to register with the platform, but lastname is malicious code
        // response HyperIoTValidationException
        HUserRestApi huserRestApi = getOsgiService(HUserRestApi.class);
        boolean validationException = false;
        boolean validationExceptionIsMaliciousCode = false;
        HUser huser = new HUser();
        try {
            huser.setName("name" + java.util.UUID.randomUUID());
            huser.setLastname("javascript:");
            huser.setUsername(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
            huser.setEmail(java.util.UUID.randomUUID() + "@hyperiot.com");
            huser.setPassword("passwordPass&01");
            huser.setPasswordConfirm("passwordPass&01");
            HyperIoTContext ctx = huserRestApi.impersonate(huser);
            Assert.assertNotNull(ctx);
            huserSystemApi.registerUser(huser, ctx);
        } catch (HyperIoTValidationException ex) {
            validationException = true;
            Assert.assertEquals(1, ex.getViolations().size());
            for (ConstraintViolation violation : ex.getViolations()) {
                Assert.assertEquals(huser.getLastname(), violation.getInvalidValue());
                if (huser.getLastname().contains((CharSequence) violation.getInvalidValue())) {
                    validationExceptionIsMaliciousCode = true;
                }
            }
        }
        Assert.assertTrue(validationException);
        Assert.assertTrue(validationExceptionIsMaliciousCode);
    }

    @Test
    public void test010ssl_registerUserFailIfEmailIsMalformed() throws HyperIoTValidationException {
        HUserSystemApi huserSystemApi = getOsgiService(HUserSystemApi.class);
        // HUser tries to register with the platform, but email is malformed
        // response HyperIoTValidationException
        HUserRestApi huserRestApi = getOsgiService(HUserRestApi.class);
        boolean validationException = false;
        boolean validationExceptionIsEmailMustBeWellFormed = false;
        try {
            HUser huser = new HUser();
            huser.setName("name" + java.util.UUID.randomUUID());
            huser.setLastname("lastname" + java.util.UUID.randomUUID());
            huser.setUsername(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
            huser.setEmail("malformed");
            huser.setPassword("passwordPass&01");
            huser.setPasswordConfirm("passwordPass&01");
            HyperIoTContext ctx = huserRestApi.impersonate(huser);
            Assert.assertNotNull(ctx);
            huserSystemApi.registerUser(huser, ctx);
        } catch (HyperIoTValidationException ex) {
            validationException = true;
            Assert.assertEquals(1, ex.getViolations().size());
            for (ConstraintViolation violation : ex.getViolations()) {
                if (violation.getMessageTemplate().contains("{javax.validation.constraints.Email.message}")) {
                    validationExceptionIsEmailMustBeWellFormed = true;
                }
            }
        }
        Assert.assertTrue(validationException);
        Assert.assertTrue(validationExceptionIsEmailMustBeWellFormed);
    }

    @Test
    public void test011ssl_registerUserFailIfEmailIsNull() throws HyperIoTValidationException {
        HUserSystemApi huserSystemApi = getOsgiService(HUserSystemApi.class);
        // HUser tries to register with the platform, but email is null
        // response HyperIoTValidationException
        HUserRestApi huserRestApi = getOsgiService(HUserRestApi.class);
        boolean validationException = false;
        boolean validationExceptionIsMustNotBeNull = false;
        boolean validationExceptionIsMustNotBeEmpty = false;
        try {
            HUser huser = new HUser();
            huser.setName("name" + java.util.UUID.randomUUID());
            huser.setLastname("lastname" + java.util.UUID.randomUUID());
            huser.setUsername(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
            huser.setEmail(null);
            huser.setPassword("passwordPass&01");
            huser.setPasswordConfirm("passwordPass&01");
            HyperIoTContext ctx = huserRestApi.impersonate(huser);
            Assert.assertNotNull(ctx);
            huserSystemApi.registerUser(huser, ctx);
        } catch (HyperIoTValidationException ex) {
            validationException = true;
            Assert.assertEquals(2, ex.getViolations().size());
            for (ConstraintViolation violation : ex.getViolations()) {
                if (violation.getMessageTemplate().contains("{javax.validation.constraints.NotNull.message}")) {
                    validationExceptionIsMustNotBeNull = true;
                }
                if (violation.getMessageTemplate().contains("{javax.validation.constraints.NotEmpty.message}")) {
                    validationExceptionIsMustNotBeEmpty = true;
                }
            }
        }
        Assert.assertTrue(validationException);
        Assert.assertTrue(validationExceptionIsMustNotBeNull);
        Assert.assertTrue(validationExceptionIsMustNotBeEmpty);
    }

    @Test
    public void test012ssl_registerUserFailIfEmailIsEmpty() throws HyperIoTValidationException {
        HUserSystemApi huserSystemApi = getOsgiService(HUserSystemApi.class);
        // HUser tries to register with the platform, but email is empty
        // response HyperIoTValidationException
        HUserRestApi huserRestApi = getOsgiService(HUserRestApi.class);
        boolean validationException = false;
        boolean validationExceptionIsMustNotBeEmpty = false;
        try {
            HUser huser = new HUser();
            huser.setName("name" + java.util.UUID.randomUUID());
            huser.setLastname("lastname" + java.util.UUID.randomUUID());
            huser.setUsername(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
            huser.setEmail("");
            huser.setPassword("passwordPass&01");
            huser.setPasswordConfirm("passwordPass&01");
            HyperIoTContext ctx = huserRestApi.impersonate(huser);
            Assert.assertNotNull(ctx);
            huserSystemApi.registerUser(huser, ctx);
        } catch (HyperIoTValidationException ex) {
            validationException = true;
            Assert.assertEquals(1, ex.getViolations().size());
            for (ConstraintViolation violation : ex.getViolations()) {
                if (violation.getMessageTemplate().contains("{javax.validation.constraints.NotEmpty.message}")) {
                    validationExceptionIsMustNotBeEmpty = true;
                }
            }
        }
        Assert.assertTrue(validationException);
        Assert.assertTrue(validationExceptionIsMustNotBeEmpty);
    }

    @Test
    public void test013ssl_registerUserFailIfEmailIsMaliciousCode() throws HyperIoTValidationException {
        HUserSystemApi huserSystemApi = getOsgiService(HUserSystemApi.class);
        // HUser tries to register with the platform, but email is malicious code
        // response HyperIoTValidationException
        HUserRestApi huserRestApi = getOsgiService(HUserRestApi.class);
        boolean validationException = false;
        HUser huser = new HUser();
        try {
            huser.setName("name" + java.util.UUID.randomUUID());
            huser.setLastname("lastname" + java.util.UUID.randomUUID());
            huser.setUsername(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
            huser.setEmail("vbscript:");
            huser.setPassword("passwordPass&01");
            huser.setPasswordConfirm("passwordPass&01");
            HyperIoTContext ctx = huserRestApi.impersonate(huser);
            Assert.assertNotNull(ctx);
            huserSystemApi.registerUser(huser, ctx);
        } catch (HyperIoTValidationException ex) {
            Assert.assertEquals(2, ex.getViolations().size());
            for (ConstraintViolation violation : ex.getViolations()) {
                Assert.assertEquals(huser.getEmail(), violation.getInvalidValue());
                validationException = true;
            }
        }
        Assert.assertTrue(validationException);
    }

    @Test
    public void test014ssl_registerUserFailIfUsernameIsMalformed() throws HyperIoTValidationException {
        HUserSystemApi huserSystemApi = getOsgiService(HUserSystemApi.class);
        // HUser tries to register with the platform, but username is malformed
        // response HyperIoTValidationException
        HUserRestApi huserRestApi = getOsgiService(HUserRestApi.class);
        boolean validationException = false;
        boolean validationExceptionIsAllowedLettersNumbers = false;
        HUser huser = new HUser();
        try {
            huser.setName("name" + java.util.UUID.randomUUID());
            huser.setLastname("lastname" + java.util.UUID.randomUUID());
            huser.setUsername("%&/%&%&/");
            huser.setEmail(java.util.UUID.randomUUID() + "@hyperiot.com");
            huser.setPassword("passwordPass&01");
            huser.setPasswordConfirm("passwordPass&01");
            HyperIoTContext ctx = huserRestApi.impersonate(huser);
            Assert.assertNotNull(ctx);
            huserSystemApi.registerUser(huser, ctx);
        } catch (HyperIoTValidationException ex) {
            validationException = true;
            Assert.assertEquals(1, ex.getViolations().size());
            for (ConstraintViolation violation : ex.getViolations()) {
                Assert.assertEquals(huser.getUsername(), violation.getInvalidValue());
                if (huser.getUsername().contains((CharSequence) violation.getInvalidValue())) {
                    validationExceptionIsAllowedLettersNumbers = true;
                }
            }
        }
        Assert.assertTrue(validationException);
        Assert.assertTrue(validationExceptionIsAllowedLettersNumbers);
    }

    @Test
    public void test015ssl_registerUserFailIfUsernameIsNull() throws HyperIoTValidationException {
        HUserSystemApi huserSystemApi = getOsgiService(HUserSystemApi.class);
        // HUser tries to register with the platform, but username is null
        // response HyperIoTValidationException
        HUserRestApi huserRestApi = getOsgiService(HUserRestApi.class);
        boolean validationException = false;
        boolean validationExceptionIsMustNotBeEmpty = false;
        boolean validationExceptionIsMustNotBeNull = false;
        HUser huser = new HUser();
        try {
            huser.setName("name" + java.util.UUID.randomUUID());
            huser.setLastname("lastname" + java.util.UUID.randomUUID());
            huser.setUsername(null);
            huser.setEmail(java.util.UUID.randomUUID() + "@hyperiot.com");
            huser.setPassword("passwordPass&01");
            huser.setPasswordConfirm("passwordPass&01");
            HyperIoTContext ctx = huserRestApi.impersonate(huser);
            Assert.assertNotNull(ctx);
            huserSystemApi.registerUser(huser, ctx);
        } catch (HyperIoTValidationException ex) {
            validationException = true;
            Assert.assertEquals(2, ex.getViolations().size());
            for (ConstraintViolation violation : ex.getViolations()) {
                if (violation.getMessageTemplate().contains("{javax.validation.constraints.NotEmpty.message}")) {
                    validationExceptionIsMustNotBeEmpty = true;
                }
                if (violation.getMessageTemplate().contains("{javax.validation.constraints.NotNull.message}")) {
                    validationExceptionIsMustNotBeNull = true;
                }
            }
        }
        Assert.assertTrue(validationException);
        Assert.assertTrue(validationExceptionIsMustNotBeEmpty);
        Assert.assertTrue(validationExceptionIsMustNotBeNull);
    }

    @Test
    public void test016ssl_registerUserFailIfUsernameIsEmpty() throws HyperIoTValidationException {
        HUserSystemApi huserSystemApi = getOsgiService(HUserSystemApi.class);
        // HUser tries to register with the platform, but username is empty
        // response HyperIoTValidationException
        HUserRestApi huserRestApi = getOsgiService(HUserRestApi.class);
        boolean validationException = false;
        HUser huser = new HUser();
        try {
            huser.setName("name" + java.util.UUID.randomUUID());
            huser.setLastname("lastname" + java.util.UUID.randomUUID());
            huser.setUsername("");
            huser.setEmail(java.util.UUID.randomUUID() + "@hyperiot.com");
            huser.setPassword("passwordPass&01");
            huser.setPasswordConfirm("passwordPass&01");
            HyperIoTContext ctx = huserRestApi.impersonate(huser);
            Assert.assertNotNull(ctx);
            huserSystemApi.registerUser(huser, ctx);
        } catch (HyperIoTValidationException ex) {
            Assert.assertEquals(2, ex.getViolations().size());
            for (ConstraintViolation violation : ex.getViolations()) {
                Assert.assertEquals(huser.getUsername(), violation.getInvalidValue());
                validationException = true;
            }
        }
        Assert.assertTrue(validationException);
    }

    @Test
    public void test017ssl_registerUserFailIfUsernameIsMaliciousCode() throws HyperIoTValidationException {
        HUserSystemApi huserSystemApi = getOsgiService(HUserSystemApi.class);
        // HUser tries to register with the platform, but username is malicious code
        // response HyperIoTValidationException
        HUserRestApi huserRestApi = getOsgiService(HUserRestApi.class);
        boolean validationException = false;
        HUser huser = new HUser();
        try {
            huser.setName("name" + java.util.UUID.randomUUID());
            huser.setLastname("lastname" + java.util.UUID.randomUUID());
            huser.setUsername("</script>");
            huser.setEmail(java.util.UUID.randomUUID() + "@hyperiot.com");
            huser.setPassword("passwordPass&01");
            huser.setPasswordConfirm("passwordPass&01");
            HyperIoTContext ctx = huserRestApi.impersonate(huser);
            Assert.assertNotNull(ctx);
            huserSystemApi.registerUser(huser, ctx);
        } catch (HyperIoTValidationException ex) {
            Assert.assertEquals(2, ex.getViolations().size());
            for (ConstraintViolation violation : ex.getViolations()) {
                Assert.assertEquals(huser.getUsername(), violation.getInvalidValue());
                validationException = true;
            }
        }
        Assert.assertTrue(validationException);
    }

    @Test
    public void test018ssl_registerUserFailIfPasswordIsMalformed() throws HyperIoTValidationException {
        HUserSystemApi huserSystemApi = getOsgiService(HUserSystemApi.class);
        // HUser tries to register with the platform, but password is malformed
        // response HyperIoTValidationException
        HUserRestApi huserRestApi = getOsgiService(HUserRestApi.class);
        boolean validationException = false;
        try {
            HUser huser = new HUser();
            huser.setName("name" + java.util.UUID.randomUUID());
            huser.setLastname("lastname" + java.util.UUID.randomUUID());
            huser.setUsername(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
            huser.setEmail(java.util.UUID.randomUUID() + "@hyperiot.com");
            huser.setPassword("malformed");
            huser.setPasswordConfirm("passwordPass&01");
            HyperIoTContext ctx = huserRestApi.impersonate(huser);
            Assert.assertNotNull(ctx);
            huserSystemApi.registerUser(huser, ctx);
        } catch (HyperIoTValidationException ex) {
            Assert.assertEquals(2, ex.getViolations().size());
            validationException = true;
        }
        Assert.assertTrue(validationException);
    }

    @Test
    public void test019ssl_registerUserFailIfPasswordIsNull() throws HyperIoTValidationException {
        HUserSystemApi huserSystemApi = getOsgiService(HUserSystemApi.class);
        // HUser tries to register with the platform, but password is null
        // response HyperIoTValidationException
        HUserRestApi huserRestApi = getOsgiService(HUserRestApi.class);
        boolean validationException = false;
        try {
            HUser huser = new HUser();
            huser.setName("name" + java.util.UUID.randomUUID());
            huser.setLastname("lastname" + java.util.UUID.randomUUID());
            huser.setUsername(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
            huser.setEmail(java.util.UUID.randomUUID() + "@hyperiot.com");
            huser.setPassword(null);
            huser.setPasswordConfirm("passwordPass&01");
            HyperIoTContext ctx = huserRestApi.impersonate(huser);
            Assert.assertNotNull(ctx);
            huserSystemApi.registerUser(huser, ctx);
        } catch (HyperIoTValidationException ex) {
            Assert.assertEquals(3, ex.getViolations().size());
            validationException = true;
        }
        Assert.assertTrue(validationException);
    }

    @Test
    public void test020ssl_registerUserFailIfPasswordIsEmpty() throws HyperIoTValidationException {
        HUserSystemApi huserSystemApi = getOsgiService(HUserSystemApi.class);
        // HUser tries to register with the platform, but password is empty
        // response HyperIoTValidationException
        HUserRestApi huserRestApi = getOsgiService(HUserRestApi.class);
        boolean validationException = false;
        try {
            HUser huser = new HUser();
            huser.setName("name" + java.util.UUID.randomUUID());
            huser.setLastname("lastname" + java.util.UUID.randomUUID());
            huser.setUsername(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
            huser.setEmail(java.util.UUID.randomUUID() + "@hyperiot.com");
            huser.setPassword("");
            huser.setPasswordConfirm("passwordPass&01");
            HyperIoTContext ctx = huserRestApi.impersonate(huser);
            Assert.assertNotNull(ctx);
            huserSystemApi.registerUser(huser, ctx);
        } catch (HyperIoTValidationException ex) {
            Assert.assertEquals(2, ex.getViolations().size());
            validationException = true;
        }
        Assert.assertTrue(validationException);
    }

    @Test
    public void test021ssl_registerUserFailIfPasswordIsMaliciousCode() throws HyperIoTValidationException {
        HUserSystemApi huserSystemApi = getOsgiService(HUserSystemApi.class);
        // HUser tries to register with the platform, but password is malicious code
        // response HyperIoTValidationException
        HUserRestApi huserRestApi = getOsgiService(HUserRestApi.class);
        boolean validationException = false;
        HUser huser = new HUser();
        try {
            huser.setName("name" + java.util.UUID.randomUUID());
            huser.setLastname("lastname" + java.util.UUID.randomUUID());
            huser.setUsername(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
            huser.setEmail(java.util.UUID.randomUUID() + "@hyperiot.com");
            huser.setPassword("eval(malicious code)");
            huser.setPasswordConfirm("passwordPass&01");
            HyperIoTContext ctx = huserRestApi.impersonate(huser);
            Assert.assertNotNull(ctx);
            huserSystemApi.registerUser(huser, ctx);
        } catch (HyperIoTValidationException ex) {
            Assert.assertEquals(3, ex.getViolations().size());
            validationException = true;
        }
        Assert.assertTrue(validationException);
    }

    @Test
    public void test022ssl_registerUserFailIfPasswordConfirmIsMalformed() throws HyperIoTValidationException {
        HUserSystemApi huserSystemApi = getOsgiService(HUserSystemApi.class);
        // HUser tries to register with the platform, but passwordConfirm is malformed
        // response HyperIoTValidationException
        HUserRestApi huserRestApi = getOsgiService(HUserRestApi.class);
        boolean validationException = false;
        try {
            HUser huser = new HUser();
            huser.setName("name" + java.util.UUID.randomUUID());
            huser.setLastname("lastname" + java.util.UUID.randomUUID());
            huser.setUsername(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
            huser.setEmail(java.util.UUID.randomUUID() + "@hyperiot.com");
            huser.setPassword("passwordPass&01");
            huser.setPasswordConfirm("malformed");
            HyperIoTContext ctx = huserRestApi.impersonate(huser);
            Assert.assertNotNull(ctx);
            huserSystemApi.registerUser(huser, ctx);
        } catch (HyperIoTValidationException ex) {
            Assert.assertEquals(2, ex.getViolations().size());
            validationException = true;
        }
        Assert.assertTrue(validationException);
    }

    @Test
    public void test023ssl_registerUserFailIfPasswordConfirmIsNull() throws HyperIoTValidationException {
        HUserSystemApi huserSystemApi = getOsgiService(HUserSystemApi.class);
        // HUser tries to register with the platform, but passwordConfirm is null
        // response HyperIoTValidationException
        HUserRestApi huserRestApi = getOsgiService(HUserRestApi.class);
        boolean validationException = false;
        try {
            HUser huser = new HUser();
            huser.setName("name" + java.util.UUID.randomUUID());
            huser.setLastname("lastname" + java.util.UUID.randomUUID());
            huser.setUsername(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
            huser.setEmail(java.util.UUID.randomUUID() + "@hyperiot.com");
            huser.setPassword("passwordPass&01");
            huser.setPasswordConfirm(null);
            HyperIoTContext ctx = huserRestApi.impersonate(huser);
            Assert.assertNotNull(ctx);
            huserSystemApi.registerUser(huser, ctx);
        } catch (HyperIoTValidationException ex) {
            Assert.assertEquals(2, ex.getViolations().size());
            validationException = true;
        }
        Assert.assertTrue(validationException);
    }

    @Test
    public void test024ssl_registerUserFailIfPasswordConfirmIsEmpty() throws HyperIoTValidationException {
        HUserSystemApi huserSystemApi = getOsgiService(HUserSystemApi.class);
        // HUser tries to register with the platform, but passwordConfirm is empty
        // response HyperIoTValidationException
        HUserRestApi huserRestApi = getOsgiService(HUserRestApi.class);
        boolean validationException = false;
        try {
            HUser huser = new HUser();
            huser.setName("name" + java.util.UUID.randomUUID());
            huser.setLastname("lastname" + java.util.UUID.randomUUID());
            huser.setUsername(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
            huser.setEmail(java.util.UUID.randomUUID() + "@hyperiot.com");
            huser.setPassword("passwordPass&01");
            huser.setPasswordConfirm("");
            HyperIoTContext ctx = huserRestApi.impersonate(huser);
            Assert.assertNotNull(ctx);
            huserSystemApi.registerUser(huser, ctx);
        } catch (HyperIoTValidationException ex) {
            Assert.assertEquals(2, ex.getViolations().size());
            validationException = true;
        }
        Assert.assertTrue(validationException);
    }

    @Test
    public void test025ssl_registerUserFailIfPasswordConfirmIsMaliciousCode() throws HyperIoTValidationException {
        HUserSystemApi huserSystemApi = getOsgiService(HUserSystemApi.class);
        // HUser tries to register with the platform,
        // but passwordConfirm is malicious code
        // response HyperIoTValidationException
        HUserRestApi huserRestApi = getOsgiService(HUserRestApi.class);
        boolean validationException = false;
        try {
            HUser huser = new HUser();
            huser.setName("name" + java.util.UUID.randomUUID());
            huser.setLastname("lastname" + java.util.UUID.randomUUID());
            huser.setUsername(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
            huser.setEmail(java.util.UUID.randomUUID() + "@hyperiot.com");
            huser.setPassword("passwordPass&01");
            huser.setPasswordConfirm("javascript:");
            HyperIoTContext ctx = huserRestApi.impersonate(huser);
            Assert.assertNotNull(ctx);
            huserSystemApi.registerUser(huser, ctx);
        } catch (HyperIoTValidationException ex) {
            validationException = true;
            Assert.assertEquals(2, ex.getViolations().size());
        }
        Assert.assertTrue(validationException);
    }

    @Test
    public void test026ssl_registerUserFailIfPasswordIsNotEqualsPasswordConfirm() throws HyperIoTValidationException {
        HUserSystemApi huserSystemApi = getOsgiService(HUserSystemApi.class);
        // HUser tries to register with the platform,
        // but password is not equals to passwordConfirm
        // response HyperIoTValidationException
        HUserRestApi huserRestApi = getOsgiService(HUserRestApi.class);
        boolean validationException = false;
        try {
            HUser huser = new HUser();
            huser.setName("name" + java.util.UUID.randomUUID());
            huser.setLastname("lastname" + java.util.UUID.randomUUID());
            huser.setUsername(java.util.UUID.randomUUID().toString().replaceAll("-", ""));
            huser.setEmail(java.util.UUID.randomUUID() + "@hyperiot.com");
            huser.setPassword("passwordPass&01");
            huser.setPasswordConfirm("pAssw0rdPass&01");
            HyperIoTContext ctx = huserRestApi.impersonate(huser);
            Assert.assertNotNull(ctx);
            huserSystemApi.registerUser(huser, ctx);
        } catch (HyperIoTValidationException ex) {
            Assert.assertEquals(2, ex.getViolations().size());
            validationException = true;
        }
        Assert.assertTrue(validationException);
    }

    @Test
    public void test027ssl_findByEmailWork() {
        HUserSystemApi huserSystemApi = getOsgiService(HUserSystemApi.class);
        // HUser find user by email with the following call
        // findUserByEmail
        HyperIoTUser adminUser = huserSystemApi.findUserByEmail("hadmin@hyperiot.com");
        Assert.assertEquals("hadmin@hyperiot.com", adminUser.getEmail());
        Assert.assertNotNull(adminUser);
    }

    @Test
    public void test028ssl_findByEmailFailIfMailNotFound() {
        HUserSystemApi huserSystemApi = getOsgiService(HUserSystemApi.class);
        // HUser tries to find user by email with the following call
        // findUserByEmail, but email not found
        HyperIoTUser user = huserSystemApi.findUserByEmail("wrongMail");
        Assert.assertNull(user);
    }

    @Test
    public void test029ssl_loginWithEmailAddressShouldWork() {
        HUserSystemApi huserSystemApi = getOsgiService(HUserSystemApi.class);
        // hadmin authenticate with his email address and password
        // with the following call login
        HUser huser = huserSystemApi.login("hadmin@hyperiot.com", "admin");
        Assert.assertNotNull(huser);
        Assert.assertTrue(huser.isActive());
        Assert.assertNotEquals(0, huser.getId());
    }

    @Test
    public void test030ssl_loginWithEmailAddressShouldFailIfEmailIsNotFound() {
        HUserSystemApi huserSystemApi = getOsgiService(HUserSystemApi.class);
        // hadmin tries to authenticates with his email address and password
        // with the following call login, but email is wrong
        HUser huser = huserSystemApi.login("wrongEmail", "admin");
        Assert.assertNull(huser);
    }

    @Test
    public void test031ssl_loginWithEmailAddressShouldFailIfUserIsNotActivated() {
        HUserSystemApi huserSystemApi = getOsgiService(HUserSystemApi.class);
        // hadmin tries to authenticates with his email address and password
        // with the following call login, but huser is not activated
        HUser huser = registerHUser();
        boolean userIsActivated = true;
        try {
            huser = huserSystemApi.login(huser.getEmail(), "passwordPass&01");
        } catch (HyperIoTUserNotActivated ex) {
            userIsActivated = false;
            Assert.assertFalse(huser.isActive());
            Assert.assertNotEquals(0, huser.getId());
        }
        Assert.assertFalse(userIsActivated);
    }


    @Test
    public void test032ssl_loginWithEmailAddressShouldFailIfPasswordIsWrong() {
        HUserSystemApi huserSystemApi = getOsgiService(HUserSystemApi.class);
        // hadmin tries to authenticates with his email address and password
        // with the following call login, but password is wrong
        HUser huser = createHUser();
        Assert.assertTrue(huser.isActive());
        huser = huserSystemApi.login(huser.getEmail(), "wrong");
        Assert.assertNull(huser);
    }


    /*
     *
     *
     * UTILITY METHODS
     *
     *
     */


    @After
    public void afterTest() {
        HUserSystemApi hUserSystemApi = getOsgiService(HUserSystemApi.class);
        RoleSystemApi roleSystemApi = getOsgiService(RoleSystemApi.class);
        HyperIoTHUserTestUtils.truncateHUsers(hUserSystemApi);
        HyperIoTHUserTestUtils.truncateRoles(roleSystemApi);
    }


}
