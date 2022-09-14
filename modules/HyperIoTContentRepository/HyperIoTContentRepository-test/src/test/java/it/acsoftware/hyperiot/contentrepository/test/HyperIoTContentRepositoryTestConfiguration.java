package it.acsoftware.hyperiot.contentrepository.test;

import it.acsoftware.hyperiot.base.test.HyperIoTTestConfigurationBuilder;
import org.ops4j.pax.exam.ConfigurationFactory;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.karaf.options.ConfigurationPointer;
import org.ops4j.pax.exam.karaf.options.KarafDistributionConfigurationFileExtendOption;


public class HyperIoTContentRepositoryTestConfiguration implements ConfigurationFactory {

	@Override
	public Option[] createConfiguration() {
		Option[] customOptions = { new KarafDistributionConfigurationFileExtendOption(
				new ConfigurationPointer("etc/org.apache.karaf.features.cfg",
						"featuresRepositories"),
				",mvn:it.acsoftware.hyperiot.contentrepository/HyperIoTContentRepository-features/1.3.5" +
						"/xml/features"),
				new KarafDistributionConfigurationFileExtendOption(
						new ConfigurationPointer("etc/org.apache.karaf.features.cfg", "featuresBoot"),
						",hyperiot-contentrepository") };
		return HyperIoTTestConfigurationBuilder.createStandardConfiguration()
				.withCodeCoverage("it.acsoftware.hyperiot.contentrepository.*")
				.append(customOptions)
				.build();
	}
}