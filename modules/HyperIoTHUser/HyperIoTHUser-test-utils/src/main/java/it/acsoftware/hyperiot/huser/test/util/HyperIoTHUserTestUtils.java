package it.acsoftware.hyperiot.huser.test.util;

import it.acsoftware.hyperiot.base.test.util.HyperIoTTestUtils;
import it.acsoftware.hyperiot.huser.api.HUserSystemApi;
import it.acsoftware.hyperiot.role.api.RoleSystemApi;
import it.acsoftware.hyperiot.role.util.HyperIoTRoleConstants;

public class HyperIoTHUserTestUtils {
    public static void truncateHUsers(HUserSystemApi systemApi) {
        HyperIoTTestUtils.truncateTables(systemApi, huser -> !huser.getUsername().equalsIgnoreCase("hadmin"));
    }

    public static void truncateRoles(RoleSystemApi systemApi) {
        HyperIoTTestUtils.truncateTables(systemApi, (role -> !role.getName().equalsIgnoreCase(HyperIoTRoleConstants.ROLE_NAME_REGISTERED_USER)));
    }
}
