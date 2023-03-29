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

import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.base.api.HyperIoTUser;
import it.acsoftware.hyperiot.base.service.rest.HyperIoTBaseRestApi;
import it.acsoftware.hyperiot.base.test.http.*;
import it.acsoftware.hyperiot.base.test.http.matcher.HyperIoTHttpResponseValidator;
import it.acsoftware.hyperiot.base.test.http.matcher.HyperIoTHttpResponseValidatorBuilder;
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

import java.util.LinkedList;
import java.util.List;


/**
 * @author Francesco Salerno
 *
 * This is a test class relative to Interface's test of AuthenticationRestApi
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HyperIoTAuthenticationRestInterfaceTest extends KarafTestSupport {

    //force global configuration
    public Option[] config() {
        return null;
    }

    public HyperIoTContext impersonateUser(HyperIoTBaseRestApi restApi, HyperIoTUser user) {
        return restApi.impersonate(user);
    }

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
    public void test001_whoAmIShouldSerializeResponseCorrectly(){
        HyperIoTHttpRequest request = HyperIoTHttpRequestBuilder
                .get()
                .withUri(HyperIoTHttpUtils.SERVICE_BASE_URL.concat("/authentication/whoAmI"))
                .withAuthorizationAsHyperIoTAdmin()
                .build();
        HyperIoTHttpResponse response = HyperIoTHttpClient
                .hyperIoTHttpClient()
                .execute(request);
        HyperIoTHttpResponseValidator testValidator = HyperIoTHttpResponseValidatorBuilder
                .validatorBuilder()
                .withStatusEqual(200)
                .withCustomCriteria((hyperIoTHttpResponse -> response.getResponseBody().isEmpty()))
                .build();
        boolean testSuccesful = testValidator.validateResponse(response);
        Assert.assertTrue(testSuccesful);
    }

    @Test
    public void test002_loginShouldSerializeResponseCorrectly(){
        HyperIoTHttpRequest request = HyperIoTHttpRequestBuilder
                .post()
                .withUri(HyperIoTHttpUtils.SERVICE_BASE_URL.concat("/authentication/login"))
                .withParameter("username","hadmin")
                .withParameter("password","admin")
                .withContentTypeHeader("application/x-www-form-urlencoded")
                .build();
        HyperIoTHttpResponse response = HyperIoTHttpClient
                .hyperIoTHttpClient()
                .execute(request);
        HyperIoTHttpResponseValidator testValidator = HyperIoTHttpResponseValidatorBuilder
                .validatorBuilder()
                .withStatusEqual(200)
                .containExactProperties(loginProperty())
                .containExactInnerProperties("authenticable",authenticableFieldInnerProperty())
                .build();
        boolean testSuccesful = testValidator.validateResponse(response);
        Assert.assertTrue(testSuccesful);
    }

    /*
     *
     *
     * UTILITY METHODS
     *
     *
     */


    private List<String> loginProperty(){
        List<String> loginProperty = new LinkedList<>();
        loginProperty.add("token");
        loginProperty.add("authenticable");
        loginProperty.add("profile");
        return loginProperty;
    }

    private List<String> authenticableFieldInnerProperty(){
        //User password must not be serialized.
        List<String> authenticableProperty = new LinkedList<>();
        authenticableProperty.add("id");
        authenticableProperty.add("entityVersion");
        authenticableProperty.add("entityCreateDate");
        authenticableProperty.add("entityModifyDate");
        authenticableProperty.add("categoryIds");
        authenticableProperty.add("tagIds");
        authenticableProperty.add("name");
        authenticableProperty.add("lastname");
        authenticableProperty.add("username");
        authenticableProperty.add("admin");
        authenticableProperty.add("email");
        authenticableProperty.add("roles");
        authenticableProperty.add("imagePath");
        return authenticableProperty;
    }


}
