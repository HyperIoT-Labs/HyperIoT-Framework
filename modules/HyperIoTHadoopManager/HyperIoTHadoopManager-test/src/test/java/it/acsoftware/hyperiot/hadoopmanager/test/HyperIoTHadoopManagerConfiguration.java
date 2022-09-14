package it.acsoftware.hyperiot.hadoopmanager.test;

import it.acsoftware.hyperiot.base.test.HyperIoTTestConfigurationBuilder;
import org.ops4j.pax.exam.ConfigurationFactory;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.karaf.options.ConfigurationPointer;
import org.ops4j.pax.exam.karaf.options.KarafDistributionConfigurationFileExtendOption;

import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.editConfigurationFilePut;

public class HyperIoTHadoopManagerConfiguration implements ConfigurationFactory {

    //jar file
    static final String jarName = "algorithm_test_copy_file_001.jar";
    static final String jarPath = "/spark/jobs/";
    private static final String hadoopUrl = "hdfs://localhost:8020";

    protected static Option[] getConfiguration() {
        return new Option[]{
                // HADOOPMANAGER file cfg
                editConfigurationFilePut("etc/it.acsoftware.hyperiot.hadoopmanager.cfg",
                        "it.acsoftware.hyperiot.hadoopmanager.defaultFS", hadoopUrl),
                new KarafDistributionConfigurationFileExtendOption(
                        new ConfigurationPointer("etc/org.apache.karaf.features.cfg", "featuresRepositories"),
                        ",mvn:it.acsoftware.hyperiot.hadoopmanager/HyperIoTHadoopManager-features/" + HyperIoTTestConfigurationBuilder.getHyperIoTRuntimeVersion()
                                + "/xml/features"),
                new KarafDistributionConfigurationFileExtendOption(
                        new ConfigurationPointer("etc/org.apache.karaf.features.cfg", "featuresBoot"),
                        ",hyperiot-hadoopmanager"
                )
        };
    }

    @Override
    public Option[] createConfiguration() {
        return HyperIoTTestConfigurationBuilder.createStandardConfiguration()
                .append(getConfiguration())
                .withCodeCoverage("it.acsoftware.hyperiot.hadoopmanager.*")
                .build();
    }
}
