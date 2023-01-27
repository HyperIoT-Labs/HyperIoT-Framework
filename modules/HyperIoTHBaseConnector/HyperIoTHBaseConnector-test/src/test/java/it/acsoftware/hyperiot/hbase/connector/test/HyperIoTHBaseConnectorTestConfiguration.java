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

package it.acsoftware.hyperiot.hbase.connector.test;

import it.acsoftware.hyperiot.base.test.HyperIoTTestConfigurationBuilder;
import org.ops4j.pax.exam.ConfigurationFactory;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.karaf.options.ConfigurationPointer;
import org.ops4j.pax.exam.karaf.options.KarafDistributionConfigurationFileExtendOption;

import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.editConfigurationFilePut;


public class HyperIoTHBaseConnectorTestConfiguration implements ConfigurationFactory {
    static final String hbaseConnectorFileCFG = "etc/it.acsoftware.hyperiot.hbase.connector.cfg";

    @Override
    public Option[] createConfiguration() {
        //starts with HSQL
        Option[] customOptions = {
                // HBASE PROPERTIES
                editConfigurationFilePut(hbaseConnectorFileCFG,
                        "it.acsoftware.hyperiot.hbase.connector.client.scanner.max.result.size", "1000"),
                editConfigurationFilePut(hbaseConnectorFileCFG,
                        "it.acsoftware.hyperiot.hbase.connector.cluster.distributed", "false"),
                editConfigurationFilePut(hbaseConnectorFileCFG,
                        "it.acsoftware.hyperiot.hbase.connector.master", "16000"),
                editConfigurationFilePut(hbaseConnectorFileCFG,
                        "it.acsoftware.hyperiot.hbase.connector.master.hostname", "localhost"),
                editConfigurationFilePut(hbaseConnectorFileCFG,
                        "it.acsoftware.hyperiot.hbase.connector.master.info.port", "16010"),
                editConfigurationFilePut(hbaseConnectorFileCFG,
                        "it.acsoftware.hyperiot.hbase.connector.master.port", "16000"),
                editConfigurationFilePut(hbaseConnectorFileCFG,
                        "it.acsoftware.hyperiot.hbase.connector.regionserver.info.port", "16030"),
                editConfigurationFilePut(hbaseConnectorFileCFG,
                        "it.acsoftware.hyperiot.hbase.connector.regionserver.port", "16020"),
                editConfigurationFilePut(hbaseConnectorFileCFG,
                        "it.acsoftware.hyperiot.hbase.connector.zookeeper.quorum", "localhost"),
                editConfigurationFilePut(hbaseConnectorFileCFG,
                        "it.acsoftware.hyperiot.hbase.connector.await.termination", "1000"),
                editConfigurationFilePut(hbaseConnectorFileCFG,
                        "it.acsoftware.hyperiot.hbase.connector.core.pool.size", "10"),
                editConfigurationFilePut(hbaseConnectorFileCFG,
                        "it.acsoftware.hyperiot.hbase.connector.keep.alive.time", "0"),
                editConfigurationFilePut(hbaseConnectorFileCFG,
                        "it.acsoftware.hyperiot.hbase.connector.maximum.pool.size", "10"),
                new KarafDistributionConfigurationFileExtendOption(
                        new ConfigurationPointer("etc/org.apache.karaf.features.cfg",
                                "featuresRepositories"),
                        ",mvn:it.acsoftware.hyperiot.hbase.connector/HyperIoTHBaseConnector-features/" + HyperIoTTestConfigurationBuilder.getHyperIoTRuntimeVersion() +
                                "/xml/features"),
                new KarafDistributionConfigurationFileExtendOption(
                        new ConfigurationPointer("etc/org.apache.karaf.features.cfg", "featuresBoot"),
                        ",hyperiot-hbaseconnector")};
        return HyperIoTTestConfigurationBuilder.createStandardConfiguration()
                .append(customOptions)
                .withCodeCoverage("it.acsoftware.hyperiot.hbase.connector.*")
                .keepRuntime()
                .build();
    }
}
