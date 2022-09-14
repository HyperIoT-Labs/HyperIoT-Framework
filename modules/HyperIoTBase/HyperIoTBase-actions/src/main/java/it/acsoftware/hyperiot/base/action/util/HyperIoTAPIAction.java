package it.acsoftware.hyperiot.base.action.util;

import it.acsoftware.hyperiot.base.action.HyperIoTActionName;

/**
 * @author Aristide Cittadino Class that enumerate all CRUD base actions. Note
 * that all base actions are used by entities in the HyperIoT platform.
 */
public enum HyperIoTAPIAction implements HyperIoTActionName {
    GET("get"), POST("post"), PUT("put"), DELETE("delete"), HEAD("head"), OPTIONS("options");

    /**
     * String name for base action
     */
    private String name;

    /**
     * Base Action with the specified name
     *
     * @param name parameter that represent the base action
     */
    private HyperIoTAPIAction(String name) {
        this.name = name;
    }

    /**
     * Gets the name of base action
     */
    public String getName() {
        return name;
    }

}
