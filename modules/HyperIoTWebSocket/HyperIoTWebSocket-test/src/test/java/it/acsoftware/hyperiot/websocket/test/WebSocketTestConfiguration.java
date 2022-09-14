package it.acsoftware.hyperiot.websocket.test;

import it.acsoftware.hyperiot.base.test.HyperIoTTestConfigurationBuilder;
import org.ops4j.pax.exam.ConfigurationFactory;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.karaf.options.ConfigurationPointer;
import org.ops4j.pax.exam.karaf.options.KarafDistributionConfigurationFileExtendOption;

/**
 * @author Aristide Cittadino WebSocketTestConfiguration
 * Used for setting test global configs with ConfigurationFactory.
 * This class is defined as SPI inside META-INF/services/org.ops4j.pax.exam.ConfigurationFactory
 */

public class WebSocketTestConfiguration implements ConfigurationFactory {

    @Override
    public Option[] createConfiguration() {
        Option[] customOptions = {
                new KarafDistributionConfigurationFileExtendOption(
                        new ConfigurationPointer("etc/org.apache.karaf.features.cfg", "featuresRepositories"),
                        ",mvn:it.acsoftware.hyperiot.kafka.connector/HyperIoTKafkaConnector-features/" + HyperIoTTestConfigurationBuilder.getHyperIoTRuntimeVersion()
                                + "/xml/features"),
                new KarafDistributionConfigurationFileExtendOption(
                        new ConfigurationPointer("etc/org.apache.karaf.features.cfg", "featuresBoot"),
                        ",hyperiot-kafkaconnector"
                ),
                new KarafDistributionConfigurationFileExtendOption(
                        new ConfigurationPointer("etc/org.apache.karaf.features.cfg", "featuresRepositories"),
                        ",mvn:it.acsoftware.hyperiot.zookeeper.connector/HyperIoTZookeeperConnector-features/" + HyperIoTTestConfigurationBuilder.getHyperIoTRuntimeVersion()
                                + "/xml/features"),
                new KarafDistributionConfigurationFileExtendOption(
                        new ConfigurationPointer("etc/org.apache.karaf.features.cfg", "featuresBoot"),
                        ",hyperiot-zookeeperconnector-websocket"
                ),
                new KarafDistributionConfigurationFileExtendOption(
                        new ConfigurationPointer("etc/org.apache.karaf.features.cfg",
                                "featuresRepositories"),
                        ",mvn:it.acsoftware.hyperiot.websocket/HyperIoTWebSocket-features/"
                                + HyperIoTTestConfigurationBuilder.getHyperIoTRuntimeVersion() + "/xml/features"),
                new KarafDistributionConfigurationFileExtendOption(
                        new ConfigurationPointer("etc/org.apache.karaf.features.cfg", "featuresBoot"),
                        ",hyperiot-websocket-test")};
        return HyperIoTTestConfigurationBuilder.createStandardConfiguration()
                .withCodeCoverage("it.acsoftware.hyperiot.websocket.*")
                .append(customOptions)
                .withDebug("5005",false)
                .build();
    }
}