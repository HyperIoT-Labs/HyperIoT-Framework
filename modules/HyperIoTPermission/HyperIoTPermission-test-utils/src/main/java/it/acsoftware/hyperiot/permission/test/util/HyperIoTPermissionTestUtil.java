package it.acsoftware.hyperiot.permission.test.util;

import it.acsoftware.hyperiot.permission.api.PermissionSystemApi;
import it.acsoftware.hyperiot.role.api.RoleSystemApi;
import it.acsoftware.hyperiot.role.util.HyperIoTRoleConstants;

public class HyperIoTPermissionTestUtil {
    public static void dropPermissions(RoleSystemApi roleSystemApi, PermissionSystemApi permissionSystemApi) {
        roleSystemApi.findAll(null, null)
                .stream()
                .filter(role -> !role.getName().equals(HyperIoTRoleConstants.ROLE_NAME_REGISTERED_USER))
                .forEach(role -> {
                    permissionSystemApi.findByRole(role).stream().forEach(permission -> {
                        permissionSystemApi.remove(permission.getId(), null);
                    });
                });

    }
}
