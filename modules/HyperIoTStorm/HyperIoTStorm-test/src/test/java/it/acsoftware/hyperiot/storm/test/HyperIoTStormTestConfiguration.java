package it.acsoftware.hyperiot.storm.test;

import it.acsoftware.hyperiot.base.test.HyperIoTTestConfigurationBuilder;
import org.ops4j.pax.exam.ConfigurationFactory;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.karaf.options.ConfigurationPointer;
import org.ops4j.pax.exam.karaf.options.KarafDistributionConfigurationFileExtendOption;

public class HyperIoTStormTestConfiguration implements ConfigurationFactory {
    @Override
    public Option[] createConfiguration() {
        Option[] customOptions = {
                new KarafDistributionConfigurationFileExtendOption(
                        new ConfigurationPointer("etc/org.apache.karaf.features.cfg",
                                "featuresRepositories"),
                        ",mvn:it.acsoftware.hyperiot.storm/HyperIoTStorm-features/" + HyperIoTTestConfigurationBuilder.getHyperIoTRuntimeVersion() +
                                "/xml/features"),
                new KarafDistributionConfigurationFileExtendOption(
                        new ConfigurationPointer("etc/org.apache.karaf.features.cfg", "featuresBoot"),
                        ",hyperiot-storm"),
                new KarafDistributionConfigurationFileExtendOption(
                        new ConfigurationPointer("etc/org.apache.karaf.features.cfg",
                                "featuresRepositories"),
                        ",mvn:it.acsoftware.hyperiot.avro/HyperIoTAvro-features/" + HyperIoTTestConfigurationBuilder.getHyperIoTRuntimeVersion() +
                                "/xml/features"),
                new KarafDistributionConfigurationFileExtendOption(
                        new ConfigurationPointer("etc/org.apache.karaf.features.cfg", "featuresBoot"),
                        ",hyperiot-avro"),

        };
        return HyperIoTTestConfigurationBuilder.createStandardConfiguration()
                .append(customOptions)
                .withCodeCoverage("it.acsoftware.hyperiot.storm.*")
                .build();
    }
}
