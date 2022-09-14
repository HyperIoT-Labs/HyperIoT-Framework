package it.acsoftware.hyperiot.base.test;

import it.acsoftware.hyperiot.base.util.BuildProperties;

public class HyperIoTTestConfigurationBuilder {

    public static HyperIoTTestConfiguration createStandardConfiguration(String testSuiteName) {
        return new HyperIoTTestConfiguration(getKarafVersion(), getHyperIoTRuntimeVersion(), testSuiteName);
    }

    public static HyperIoTTestConfiguration createStandardConfiguration() {
        return new HyperIoTTestConfiguration(getKarafVersion(), getHyperIoTRuntimeVersion());
    }

    public static String getHyperIoTRuntimeVersion() {
        return BuildProperties.getHyperIoTVersion();
    }

    public static String getKarafVersion() {
        return BuildProperties.getHyperIoTKarafVersion();
    }

}
