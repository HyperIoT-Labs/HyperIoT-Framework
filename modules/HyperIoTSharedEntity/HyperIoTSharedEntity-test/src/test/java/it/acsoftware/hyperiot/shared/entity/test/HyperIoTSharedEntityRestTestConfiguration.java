package it.acsoftware.hyperiot.shared.entity.test;

import it.acsoftware.hyperiot.base.test.HyperIoTTestConfigurationBuilder;
import org.ops4j.pax.exam.ConfigurationFactory;
import org.ops4j.pax.exam.Option;

public class HyperIoTSharedEntityRestTestConfiguration implements ConfigurationFactory {
    @Override
    public Option[] createConfiguration() {
        return HyperIoTTestConfigurationBuilder.createStandardConfiguration()
                .withCodeCoverage("it.acsoftware.hyperiot.shared.entity.*")
                .build();
    }
}
