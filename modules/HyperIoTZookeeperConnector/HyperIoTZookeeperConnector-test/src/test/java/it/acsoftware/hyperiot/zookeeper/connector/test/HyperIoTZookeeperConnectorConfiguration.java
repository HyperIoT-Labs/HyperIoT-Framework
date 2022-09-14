package it.acsoftware.hyperiot.zookeeper.connector.test;

import it.acsoftware.hyperiot.base.test.HyperIoTTestConfigurationBuilder;
import org.ops4j.pax.exam.ConfigurationFactory;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.karaf.options.ConfigurationPointer;
import org.ops4j.pax.exam.karaf.options.KarafDistributionConfigurationFileExtendOption;

public class HyperIoTZookeeperConnectorConfiguration implements ConfigurationFactory {

    protected static Option[] getBaseConfiguration() {
        return new Option[]{
                new KarafDistributionConfigurationFileExtendOption(
                        new ConfigurationPointer("etc/org.apache.karaf.features.cfg", "featuresRepositories"),
                        ",mvn:it.acsoftware.hyperiot.zookeeper.connector/HyperIoTZookeeperConnector-features/" + HyperIoTTestConfigurationBuilder.getHyperIoTRuntimeVersion()
                                + "/xml/features"),
                new KarafDistributionConfigurationFileExtendOption(
                    new ConfigurationPointer("etc/org.apache.karaf.features.cfg", "featuresBoot"),
                    ",hyperiot-zookeeperconnector"
                )
        };
    }

    @Override
    public Option[] createConfiguration() {
        return HyperIoTTestConfigurationBuilder.createStandardConfiguration()
                .append(getBaseConfiguration())
                .withCodeCoverage("it.acsoftware.hyperiot.zookeeper.connector.*")
                .build();
    }
}
