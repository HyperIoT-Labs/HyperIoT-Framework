# Custom Permission System Selection [](id=custom-permission-system-selection)

The system is already set up to have different permission systems. The usage patterns are yet to be defined but the management logic is currently already implemented.

In particular, the filter that validates the JWT token , if successful, creates a HyperIoTContext object valued with the implementation of the permission system to be used.

Different logics could be envisioned:

The possibility of defining different permission systems for issuers

The possibility of using a permission system indicated by the token itself based on the login performed

Currently the default one is selected as the permission system, i.e., using HyperIoT Framwork roles and permissions.

```
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
     * @param jwt       JWT Token
     * @param roleClaim Role Claim
     */
    public HyperIoTJwtContextImpl(JwtToken jwt, String roleClaim) {
        super(jwt, roleClaim);
        this.issuerClassName = jwt.getClaims().getIssuer();
        this.loggedEntityId = (jwt.getClaims().getClaim("loggedEntityId") != null) ? (Long) jwt.getClaims().getClaim("loggedEntityId") : 0L;
        this.permissionImplementation = HyperIoTConstants.OSGI_PERMISSION_MANAGER_IMPLEMENTATION_DEFAULT;
    }

    /**
     * @param jwt                      JWT Token
     * @param roleClaim                Role Claim
     * @param permissionImplementation parameter that indicates which permissions implementation to use
     */
    public HyperIoTJwtContextImpl(JwtToken jwt, String roleClaim, String permissionImplementation) {
        super(jwt, roleClaim);
        this.issuerClassName = jwt.getClaims().getIssuer();
        this.loggedEntityId = (Long) jwt.getClaims().getClaim("loggedEntityId");
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

```