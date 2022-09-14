package it.acsoftware.hyperiot.base.api;

import org.apache.cxf.rs.security.jose.jwt.JwtToken;
import org.apache.cxf.security.SecurityContext;

public interface HyperIoTJwtContext extends HyperIoTContext, SecurityContext {

    /**
     * Returns a string indicating the name of the authenticated current user.
     */
    JwtToken getAuthenticationToken();

    /**
     * Returns a boolean value indicating that the logged user is included in a
     * role.
     */
    boolean loggedUserHasRole(String role);

    /**
     * @return the Java Type of the logged user since the platform can support multiple logged in entities
     */
    Class getIssuerType();
}
