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

package it.acsoftware.hyperiot.hadoopmanager.test;

import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.base.api.HyperIoTUser;
import it.acsoftware.hyperiot.base.service.rest.HyperIoTBaseRestApi;
import it.acsoftware.hyperiot.base.test.containers.HyperIoTDynamicContainersConfigurationBuilder;
import it.acsoftware.hyperiot.hadoopmanager.api.HadoopManagerSystemApi;
import org.apache.karaf.features.FeaturesService;
import org.apache.karaf.itests.KarafTestSupport;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

import java.io.File;
import java.io.IOException;

import static it.acsoftware.hyperiot.hadoopmanager.test.HyperIoTHadoopManagerConfiguration.jarName;
import static it.acsoftware.hyperiot.hadoopmanager.test.HyperIoTHadoopManagerConfiguration.jarPath;

/**
 * @author Aristide Cittadino Interface component for HadoopManager System Service.
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HyperIoTHadoopManagerTest extends KarafTestSupport {

    //force global configuration
    public Option[] config() {
        return null;
    }

    public HyperIoTContext impersonateUser(HyperIoTBaseRestApi restApi, HyperIoTUser user) {
        return restApi.impersonate(user);
    }

    @Before
    public void initContainer() {
        HyperIoTDynamicContainersConfigurationBuilder.getInstance()
                .withAutoStart()
                .withHadoopContainer()
                .build();
    }

    @Test
    public void test000_hyperIoTFrameworkShouldBeInstalled() {
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
        assertContains("HyperIoTHadoopManager-features ", features);
        String datasource = executeCommand("jdbc:ds-list");
//		System.out.println(executeCommand("bundle:list | grep HyperIoT"));
        assertContains("hyperiot", datasource);
    }


    @Test
    public void test001_checksIfFileHadoopManagerCfgExists() {
        // Test will be runs if docker image has been launched.
        // Please runs "docker-compose -f docker-compose-svil-hdfs-only.yml up"
        // checks if it.acsoftware.hyperiot.hadoopmanager.cfg exists.
        // If file not found HyperIoTHadoopManager-service bundle is in Waiting state
        String hyperIoTHadoopManagerService = executeCommand("bundle:list | grep HyperIoTHadoopManager-service");
        boolean fileCfgHadoopManagerFound = false;
        String fileConfigHadoopManager = executeCommand("config:list | grep it.acsoftware.hyperiot.hadoopmanager.cfg");
        if (hyperIoTHadoopManagerService.contains("Active")) {
            Assert.assertTrue(hyperIoTHadoopManagerService.contains("Active"));
            if (fileConfigHadoopManager.contains("it.acsoftware.hyperiot.hadoopmanager.cfg")) {
                Assert.assertTrue(fileConfigHadoopManager.contains("it.acsoftware.hyperiot.hadoopmanager.cfg"));
                fileCfgHadoopManagerFound = true;
            }
        }
        if (hyperIoTHadoopManagerService.contains("Waiting")) {
            Assert.assertTrue(hyperIoTHadoopManagerService.contains("Waiting"));
            if (fileConfigHadoopManager.isEmpty()) {
                Assert.assertTrue(fileConfigHadoopManager.isEmpty());
                Assert.assertFalse(fileCfgHadoopManagerFound);
                System.out.println("file etc/it.acsoftware.hyperiot.hadoopmanager.cfg not found...");
            }
        }
        Assert.assertTrue(fileCfgHadoopManagerFound);
    }


    @Test(expected = IOException.class)
    public void test002_copyFileShouldWork() throws IOException {
        // Test will be runs if docker image has been launched.
        // Please runs "docker-compose -f docker-compose-svil-hdfs-only.yml up"
        HadoopManagerSystemApi hadoopManagerSystemApi = getOsgiService(HadoopManagerSystemApi.class);
        File algorithmFile = new File(jarPath + jarName);
        hadoopManagerSystemApi.copyFile(algorithmFile, String.valueOf(algorithmFile), true);
    }


    @Test
    public void test003_copyFileShouldFailIfPathAlreadyExistsAsADirectory() {
        // Test will be runs if docker image has been launched.
        // Please runs "docker-compose -f docker-compose-svil-hdfs-only.yml up"
        HadoopManagerSystemApi hadoopManagerSystemApi = getOsgiService(HadoopManagerSystemApi.class);
        File algorithmFile = new File(jarPath + jarName);
        try {
            hadoopManagerSystemApi.copyFile(algorithmFile, jarPath, true);
        } catch (IOException e) {
            String msg = e.getMessage();
            Assert.assertTrue(msg.contains("/spark/jobs already exists as a directory"));
        }
    }


    @Test
    public void test004_deleteFileShouldWork() throws IOException {
        // Test will be runs if docker image has been launched.
        // Please runs "docker-compose -f docker-compose-svil-hdfs-only.yml up"
        HadoopManagerSystemApi hadoopManagerSystemApi = getOsgiService(HadoopManagerSystemApi.class);
        hadoopManagerSystemApi.deleteFile(jarPath + jarName);
    }


}

