package it.acsoftware.hyperiot.base.api;

/**
 * @author Aristide Cittadino Generic interface component for
 * HyperIoTProtectedResource. This interface defines the method for gets
 * the resource id of the protected entity of HyperIoT platform.
 */
public interface HyperIoTProtectedResource extends HyperIoTResource {

    /**
     * Gets the resource id of the protected entity of HyperIoT platform
     *
     * @return resource id of the protected entity
     */
    public String getResourceId();
}
