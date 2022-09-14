package it.acsoftware.hyperiot.permission.api;

import java.util.HashMap;
import java.util.List;

import it.acsoftware.hyperiot.base.api.HyperIoTAction;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTBaseEntityApi;
import it.acsoftware.hyperiot.permission.model.Permission;

/**
 * @author Aristide Cittadino Interface component for PermissionApi. This
 * inteface defines methods for additional operations.
 */
public interface PermissionApi extends HyperIoTBaseEntityApi<Permission> {

    /**
     * This method finds a list of all available permissions for HyperIoT platform
     */
    public HashMap<String, List<HyperIoTAction>> getAvailablePermissions();
}
