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

package it.acsoftware.hyperiot.base.model.authentication.context;

import it.acsoftware.hyperiot.base.api.HyperIoTJwtContext;
import it.acsoftware.hyperiot.base.util.HyperIoTConstants;
import org.apache.cxf.rs.security.jose.jaxrs.JwtTokenSecurityContext;
import org.apache.cxf.rs.security.jose.jwt.JwtToken;

import java.security.Principal;

/**
 * @author Aristide Cittadino Model class for HyperIoTContextImpl. This class
 * implements methods to provide security information of user during
 * interactions with the HyperIoT platform.
 */
public class HyperIoTJwtContextImpl extends JwtTokenSecurityContext implements HyperIoTJwtContext {

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

    /**
     * if it is admin or not
     */
    private boolean admin;

    /**
     * @param jwt       JWT Token
     * @param roleClaim Role Claim
     */
    public HyperIoTJwtContextImpl(JwtToken jwt, String roleClaim) {
        super(jwt, roleClaim);
        this.issuerClassName = jwt.getClaims().getIssuer();
        this.loggedEntityId = (jwt.getClaims().getClaim("loggedEntityId") != null) ? (Long) jwt.getClaims().getClaim("loggedEntityId") : 0L;
        this.permissionImplementation = HyperIoTConstants.OSGI_PERMISSION_MANAGER_IMPLEMENTATION_DEFAULT;
        this.admin = (jwt.getClaims().getClaim("admin") != null) ? (Boolean) jwt.getClaims().getClaim("admin") : false;
    }

    /**
     * @param jwt                      JWT Token
     * @param roleClaim                Role Claim
     * @param permissionImplementation parameter that indicates which permissions implementation to use
     */
    public HyperIoTJwtContextImpl(JwtToken jwt, String roleClaim, String permissionImplementation) {
        this(jwt, roleClaim);
        this.permissionImplementation = permissionImplementation;
    }

    /**
     * Returns a object that contains the name of the current authenticated user. If
     * the user has not been authenticated, the method returns null.
     */
    @Override
    public Principal getUserPrincipal() {
        return super.getUserPrincipal();
    }

    /**
     * Returns a boolean value indicating that the authenticated user is included in
     * the role.
     */
    @Override
    public boolean isUserInRole(String role) {
        return super.isUserInRole(role);
    }

    /**
     * Returns a string indicating the name of the authenticated current user. If
     * the user has not been authenticated, the method returns null.
     */
    @Override
    public String getLoggedUsername() {
        return (super.getUserPrincipal() != null) ? super.getUserPrincipal().getName() : null;
    }

    /**
     * Returns a boolean value indicating that the user has logged in.
     */
    @Override
    public boolean isLoggedIn() {
        return super.getUserPrincipal() != null && super.getUserPrincipal().getName().length() > 0;
    }

    @Override
    public boolean isAdmin() {
        return admin;
    }

    /**
     * Returns a boolean value indicating that the logged user is included in a
     * role.
     */
    @Override
    public boolean loggedUserHasRole(String role) {
        return super.isUserInRole(role);
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

    /**
     * @return Authentication token
     */
    @Override
    public JwtToken getAuthenticationToken() {
        return (this.getToken() != null) ? this.getToken() : null;
    }

    /**
     * @return
     */
    @Override
    public boolean isSecure() {
        return true;
    }

    @Override
    public String getAuthenticationScheme() {
        return "JWT";
    }

    public long getLoggedEntityId() {
        return loggedEntityId;
    }

    @Override
    public Class getIssuerType() {
        if (this.issuerClassName != null && !this.issuerClassName.isEmpty()) {
            try {
                return Class.forName(this.issuerClassName);
            } catch (ClassNotFoundException e) {
                return null;
            }
        }
        return null;
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

}
