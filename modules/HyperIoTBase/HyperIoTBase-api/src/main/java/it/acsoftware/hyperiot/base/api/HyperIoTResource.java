package it.acsoftware.hyperiot.base.api;

/**
 * @author Aristide Cittadino Generic interface component for HyperIoTResource.
 * This interface defines the method for gets the resource name of
 * entity of HyperIoT platform.
 */
public interface HyperIoTResource {
    /**
     * Gets the resource name of HyperIoT platform
     *
     * @return resource name of HyperIoT platform
     */
    default String getResourceName() {
        return this.getClass().getName();
    }
}
