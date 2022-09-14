package it.acsoftware.hyperiot.blockchain.ethereum.connector.test;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.ConfigurationPointer;
import org.ops4j.pax.exam.karaf.options.KarafDistributionConfigurationFileExtendOption;
import org.ops4j.pax.exam.ConfigurationFactory;

import it.acsoftware.hyperiot.base.test.HyperIoTTestConfigurationBuilder;
/**
 * 
 * @author Aristide Cittadino EthereumConnectorTestConfiguration
 * Used for setting test global configs with ConfigurationFactory.
 * This class is defined as SPI inside META-INF/services/org.ops4j.pax.exam.ConfigurationFactory
 */

public class EthereumConnectorTestConfiguration implements ConfigurationFactory {

	@Override
    public Option[] createConfiguration() {
		Option[] customOptions = { new KarafDistributionConfigurationFileExtendOption(
				new ConfigurationPointer("etc/org.apache.karaf.features.cfg",
						"featuresRepositories"),
				",mvn:it.acsoftware.hyperiot.blockchain.ethereum.connector/HyperIoTEthereumConnector-features/"+ HyperIoTTestConfigurationBuilder.getHyperIoTRuntimeVersion() +
						"/xml/features"),
				new KarafDistributionConfigurationFileExtendOption(
						new ConfigurationPointer("etc/org.apache.karaf.features.cfg", "featuresBoot"),
						",hyperiot-ethereum-connector") };
		return HyperIoTTestConfigurationBuilder.createStandardConfiguration()
				.withCodeCoverage("it.acsoftware.hyperiot.blockchain.ethereum.connector.*")
				.append(customOptions)
				.build();
	}
}