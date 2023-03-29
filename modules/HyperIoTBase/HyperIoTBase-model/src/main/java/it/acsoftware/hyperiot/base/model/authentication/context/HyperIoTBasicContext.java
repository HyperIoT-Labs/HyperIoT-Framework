/*
 * Copyright 2019-2023 HyperIoT
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

package it.acsoftware.hyperiot.base.model.authentication.context;

import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.base.api.HyperIoTRole;
import it.acsoftware.hyperiot.base.model.authentication.principal.HyperIoTPrincipal;
import it.acsoftware.hyperiot.base.util.HyperIoTConstants;

import java.security.Principal;
import java.util.*;

public class HyperIoTBasicContext implements HyperIoTContext {

    private Set<Principal> loggedPrincipals;
    private Principal loggedUser;
    private List<HyperIoTRole> roles;
    /**
     * indicates which permissions implementation to use
     */
    private String permissionImplementation;
    /**
     * Since User and Devices can login inside the platform, it should necessary to identify which kind of entity is logged in
     */
    private String issuerClassName;

    /**
     * Logged User Id
     */
    private long loggedEntityId;

    public HyperIoTBasicContext(Set<Principal> loggedPrincipals) {
        this.setLoggedPrincipals(loggedPrincipals);
        this.permissionImplementation = HyperIoTConstants.OSGI_PERMISSION_MANAGER_IMPLEMENTATION_DEFAULT;
    }

    public HyperIoTBasicContext(Set<Principal> loggedPrincipals, String permissionImplementation) {
        this.setLoggedPrincipals(loggedPrincipals);
        this.permissionImplementation = permissionImplementation;
    }

    /**
     * @param loggedPrincipals
     */
    public void setLoggedPrincipals(Set<Principal> loggedPrincipals) {
        this.loggedPrincipals = loggedPrincipals;
        if (loggedPrincipals != null) {
            Iterator<Principal> it = loggedPrincipals.iterator();
            roles = new ArrayList<>();
            while (it.hasNext()) {
                Principal p = it.next();
                if (p instanceof HyperIoTPrincipal) {
                    this.loggedUser = (HyperIoTPrincipal) p;
                }

                if (p instanceof HyperIoTRole) {
                    roles.add((HyperIoTRole) p);
                }
            }
        } else {
            this.loggedPrincipals = new HashSet<>();
        }
    }

    /**
     * Returns a string indicating the name of the authenticated current user. If
     * the user has not been authenticated, the method returns null.
     */
    @Override
    public String getLoggedUsername() {
        return (loggedUser != null) ? loggedUser.getName() : null;
    }

    /**
     * Returns a boolean value indicating that the user has logged in.
     */
    @Override
    public boolean isLoggedIn() {
        return loggedUser != null && this.loggedUser.getName().length() > 0;
    }

    /**
     * If the logged principal is admin
     *
     * @return
     */
    @Override
    public boolean isAdmin() {
        if (loggedUser == null)
            return false;
        HyperIoTPrincipal p = (HyperIoTPrincipal) loggedUser;
        return p.isAdmin();
    }

    /**
     * Gets the current permissionImplementation
     */
    @Override
    public String getPermissionImplementation() {
        return this.permissionImplementation;
    }

    /**
     * Sets the current permission implementation
     *
     * @param implementation parameter that sets which permissions implementation to
     *                       use
     */
    @Override
    public void setPermissionImplementation(String implementation) {
        this.permissionImplementation = implementation;

    }

    public long getLoggedEntityId() {
        return loggedEntityId;
    }


    /**
     * Return information about user, if it is a user logged or not yet.
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("HyperIoT Context:");
        if (this.getLoggedUsername() != null)
            sb.append("Logged User:" + this.getLoggedUsername());
        else
            sb.append("Guest user");
        return sb.toString();
    }

    @Override
    public Principal getUserPrincipal() {
        return this.loggedUser;
    }

    @Override
    public boolean isUserInRole(String role) {
        if (this.roles == null || this.roles.size() == 0)
            return false;

        for (int i = 0; i < this.roles.size(); i++) {
            if (this.roles.get(i).getName().equalsIgnoreCase(role))
                return true;
        }
        return false;
    }

    @Override
    public boolean isSecure() {
        return true;
    }

    @Override
    public String getAuthenticationScheme() {
        return "Basic";
    }
}
