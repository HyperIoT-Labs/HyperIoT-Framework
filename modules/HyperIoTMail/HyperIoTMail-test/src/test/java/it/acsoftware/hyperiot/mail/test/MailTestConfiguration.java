package it.acsoftware.hyperiot.mail.test;

import it.acsoftware.hyperiot.base.test.HyperIoTTestConfigurationBuilder;
import org.ops4j.pax.exam.ConfigurationFactory;
import org.ops4j.pax.exam.Option;

public class MailTestConfiguration implements ConfigurationFactory {

    @Override
    public Option[] createConfiguration() {
        // starts with HSQL
        return HyperIoTTestConfigurationBuilder.createStandardConfiguration().withCodeCoverage("it.acsoftware.hyperiot.mail.*").build();
    }
}
