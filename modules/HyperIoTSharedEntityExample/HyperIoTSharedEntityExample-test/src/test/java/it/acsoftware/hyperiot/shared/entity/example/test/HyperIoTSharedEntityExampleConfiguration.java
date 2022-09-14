package it.acsoftware.hyperiot.shared.entity.example.test;

import it.acsoftware.hyperiot.base.test.HyperIoTTestConfigurationBuilder;
import it.acsoftware.hyperiot.huser.model.HUser;
import it.acsoftware.hyperiot.shared.entity.example.model.SharedEntityExample;
import it.acsoftware.hyperiot.shared.entity.model.SharedEntity;
import org.ops4j.pax.exam.ConfigurationFactory;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.karaf.options.ConfigurationPointer;
import org.ops4j.pax.exam.karaf.options.KarafDistributionConfigurationFileExtendOption;

public class HyperIoTSharedEntityExampleConfiguration implements ConfigurationFactory {

    static final String hyperIoTException = "it.acsoftware.hyperiot.base.exception.";
    static final String sharedEntityResourceName = SharedEntity.class.getName();
    static final String entityExampleResourceName = SharedEntityExample.class.getName();
    static final String hUserResourceName = HUser.class.getName();

    static final int defaultDelta = 10;
    static final int defaultPage = 1;

    protected static Option[] getBaseConfiguration() {
        return new Option[]{
                new KarafDistributionConfigurationFileExtendOption(
                        new ConfigurationPointer("etc/org.apache.karaf.features.cfg", "featuresRepositories"),
                        ",mvn:it.acsoftware.hyperiot.shared.entity.example/HyperIoTSharedEntityExample-features/" + HyperIoTTestConfigurationBuilder.getHyperIoTRuntimeVersion()
                                + "/xml/features"),
                new KarafDistributionConfigurationFileExtendOption(
                        new ConfigurationPointer("etc/org.apache.karaf.features.cfg", "featuresBoot"),
                        ",hyperiot-sharedentityexample"
                )
        };
    }

    @Override
    public Option[] createConfiguration() {
        return HyperIoTTestConfigurationBuilder.createStandardConfiguration()
                .append(getBaseConfiguration())
                .withCodeCoverage("it.acsoftware.hyperiot.shared.entity.example.*")
                .withDebug("5005",false)
                .build();
    }
}
