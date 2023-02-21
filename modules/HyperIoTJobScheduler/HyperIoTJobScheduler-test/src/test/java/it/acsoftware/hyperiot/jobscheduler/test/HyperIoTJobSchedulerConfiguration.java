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

package it.acsoftware.hyperiot.jobscheduler.test;

import it.acsoftware.hyperiot.base.test.HyperIoTTestConfigurationBuilder;
import org.ops4j.pax.exam.ConfigurationFactory;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.karaf.options.ConfigurationPointer;
import org.ops4j.pax.exam.karaf.options.KarafDistributionConfigurationFileExtendOption;

public class HyperIoTJobSchedulerConfiguration implements ConfigurationFactory {
    public static final String CODE_COVERAGE_PACKAGE_FILTER = "it.acsoftware.hyperiot.jobscheduler.*";
    protected static Option[] getBaseConfiguration() {
        return new Option[]{
                new KarafDistributionConfigurationFileExtendOption(
                        new ConfigurationPointer("etc/org.apache.karaf.features.cfg", "featuresRepositories"),
                        ",mvn:it.acsoftware.hyperiot.jobscheduler/JobScheduler-features/" + HyperIoTTestConfigurationBuilder.getHyperIoTRuntimeVersion()
                                + "/xml/features"),
                new KarafDistributionConfigurationFileExtendOption(
                        new ConfigurationPointer("etc/org.apache.karaf.features.cfg", "featuresRepositories"),
                        ",mvn:it.acsoftware.hyperiot.zookeeper.connector/HyperIoTZookeeperConnector-features/" + HyperIoTTestConfigurationBuilder.getHyperIoTRuntimeVersion()
                                + "/xml/features"),
                new KarafDistributionConfigurationFileExtendOption(
                        new ConfigurationPointer("etc/org.apache.karaf.features.cfg", "featuresBoot"),
                        ",hyperiot-jobscheduler,hyperiot-zookeeperconnector"
                )
        };
    }

    @Override
    public Option[] createConfiguration() {
        return HyperIoTTestConfigurationBuilder.createStandardConfiguration()
                .append(getBaseConfiguration())
                .withCodeCoverage(CODE_COVERAGE_PACKAGE_FILTER)
                .build();
    }
}
