package it.acsoftware.hyperiot.base.action.util;

import it.acsoftware.hyperiot.base.action.HyperIoTActionName;

public enum HyperIoTShareAction implements HyperIoTActionName {
    SHARE(Names.SHARE);

    /**
     * String name for base action
     */
    private String name;

    /**
     * Base Action with the specified name
     *
     * @param name parameter that represent the base action
     */
    private HyperIoTShareAction(String name) {
        this.name = name;
    }

    /**
     * Gets the name of base action
     */
    public String getName() {
        return name;
    }

    public class Names {
        public static final String SHARE = "share";
    }
}
