package it.acsoftware.hyperiot.permission.test.util;

import it.acsoftware.hyperiot.base.api.HyperIoTAction;
import it.acsoftware.hyperiot.huser.model.HUser;
import it.acsoftware.hyperiot.permission.api.PermissionSystemApi;
import it.acsoftware.hyperiot.permission.model.Permission;
import it.acsoftware.hyperiot.permission.service.rest.PermissionRestApi;
import it.acsoftware.hyperiot.role.api.RoleSystemApi;
import it.acsoftware.hyperiot.role.model.Role;
import it.acsoftware.hyperiot.role.util.HyperIoTRoleConstants;
import org.junit.Assert;

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

    private Permission utilGrantPermission(PermissionRestApi permissionRestApi, PermissionSystemApi permissionSystemApi, HUser huser, Role role, HyperIoTAction action, long resourceId) {
        if (action == null) {
            return null;
        } else {
            Permission testPermission = permissionSystemApi.findByRoleAndResourceName(role, action.getResourceName());
            if (testPermission == null) {
                Permission permission = new Permission();
                permission.setName(resourceId + " assigned to huser_id " + huser.getId());
                permission.setActionIds(action.getActionId());
                permission.setEntityResourceName(action.getResourceName());
                if (resourceId > 0)
                    permission.setResourceId(resourceId);
                permission.setRole(role);
                testPermission = permission;
            } else {
                testPermission.addPermission(action);
                permissionRestApi.updatePermission(testPermission);
            }
            Assert.assertTrue(huser.hasRole(role.getId()));
            return testPermission;
        }
    }
}
