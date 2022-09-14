package it.acsoftware.hyperiot.gradle.plugins.workspace;

import org.gradle.api.initialization.Settings;

import org.gradle.api.provider.Property;

public class HyperIoTWorskpaceExtension {
    public static final String DEFAULT_MODULES_FOLDER = "modules";
    private String modulesFolder = DEFAULT_MODULES_FOLDER;

    public HyperIoTWorskpaceExtension(Settings settings) {
    }

    public String getModulesFolder() {
        return modulesFolder;
    }
}
