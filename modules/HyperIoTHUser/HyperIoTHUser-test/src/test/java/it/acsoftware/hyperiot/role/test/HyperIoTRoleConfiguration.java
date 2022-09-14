package it.acsoftware.hyperiot.role.test;

import it.acsoftware.hyperiot.huser.model.HUser;
import it.acsoftware.hyperiot.role.model.Role;
import org.ops4j.pax.exam.Option;

public class HyperIoTRoleConfiguration {
    public static final String CODE_COVERAGE_PACKAGE_FILTER = "it.acsoftware.hyperiot.role.*";
    static final String hyperIoTException = "it.acsoftware.hyperiot.base.exception.";
    static final String roleResourceName = Role.class.getName();
    static final String huserResourceName = HUser.class.getName();

    static final String permissionAssetCategory = "it.acsoftware.hyperiot.asset.category.model.AssetCategory";
    static final String permissionAssetTag = "it.acsoftware.hyperiot.asset.tag.model.AssetTag";
    static final String nameRegisteredPermission = " RegisteredUser Permissions";

    static final int defaultDelta = 10;
    static final int defaultPage = 1;

}
