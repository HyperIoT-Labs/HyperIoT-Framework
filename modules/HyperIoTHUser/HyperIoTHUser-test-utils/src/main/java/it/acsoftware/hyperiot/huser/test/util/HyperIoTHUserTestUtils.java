/*
 * Copyright 2019-2023 ACSoftware
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package it.acsoftware.hyperiot.huser.test.util;

import it.acsoftware.hyperiot.base.api.HyperIoTAction;
import it.acsoftware.hyperiot.base.test.util.HyperIoTTestUtils;
import it.acsoftware.hyperiot.huser.api.HUserSystemApi;
import it.acsoftware.hyperiot.huser.model.HUser;
import it.acsoftware.hyperiot.huser.service.rest.HUserRestApi;
import it.acsoftware.hyperiot.role.api.RoleSystemApi;
import it.acsoftware.hyperiot.role.model.Role;
import it.acsoftware.hyperiot.role.service.rest.RoleRestApi;
import it.acsoftware.hyperiot.role.util.HyperIoTRoleConstants;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HyperIoTHUserTestUtils {
    public static void truncateHUsers(HUserSystemApi systemApi) {
        HyperIoTTestUtils.truncateTables(systemApi, huser -> !huser.getUsername().equalsIgnoreCase("hadmin"));
    }

    public static void truncateRoles(RoleSystemApi systemApi) {
        HyperIoTTestUtils.truncateTables(systemApi, (role -> !role.getName().equalsIgnoreCase(HyperIoTRoleConstants.ROLE_NAME_REGISTERED_USER)));
    }

    public static HUser createHUser(HyperIoTAction action, HUserRestApi hUserRestApi, RoleRestApi roleRestApi, HUser loggedUser) {
        String username = "TestUser";
        List<Object> roles = new ArrayList<>();
        HUser huser = createRandomHUser(username, false, true);
        hUserRestApi.saveHUser(huser);
        if (action != null) {
            Role role = createRole(roleRestApi, loggedUser);
            huser.addRole(role);
            roleRestApi.saveUserRole(role.getId(), huser.getId());
        }
        return huser;
    }

    public static HUser registerAndActivateNewUser(HUserRestApi hUserRestApi) {
        String username = "TestUser";
        HUser huser = createRandomHUser(username, false, true);
        hUserRestApi.register(huser);
        String activationCode = huser.getActivateCode();
        hUserRestApi.activate(huser.getEmail(), activationCode);
        return huser;
    }

    public static Role createRole(RoleRestApi roleRestApi, HUser loggedUser) {
        Role role = new Role();
        role.setName("Role" + java.util.UUID.randomUUID());
        role.setDescription("Description");
        roleRestApi.saveRole(role);
        return role;
    }

    private static HUser createRandomHUser(String username, boolean admin, boolean active) {
        HUser huser = new HUser();
        huser.setName("name");
        huser.setLastname("lastname");
        huser.setUsername(username + UUID.randomUUID().toString().replaceAll("-", ""));
        huser.setEmail("testusername" + UUID.randomUUID().toString() + "@hyperiot.com");
        huser.setPassword("passwordPass&01");
        huser.setPasswordConfirm("passwordPass&01");
        huser.setAdmin(admin);
        huser.setActive(active);
        return huser;
    }
}
