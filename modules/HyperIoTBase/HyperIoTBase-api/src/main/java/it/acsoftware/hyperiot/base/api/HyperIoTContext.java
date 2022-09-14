package it.acsoftware.hyperiot.base.api;

import org.apache.cxf.rs.security.jose.jwt.JwtToken;

import javax.ws.rs.core.SecurityContext;


/**
 * @author Aristide Cittadino Generic interface component for HyperIoTContext.
 * This interface defines methods to provide security information of
 * user during interactions with the HyperIoT platform.
 * This interfaces extends both cxf SecurityContext and jaxrs Security Context
 */
public interface HyperIoTContext extends SecurityContext {
    /**
     * Returns a string indicating the name of the authenticated current user.
     */
    String getLoggedUsername();

    /**
     * Returns a boolean value indicating that the user has logged in.
     */
    boolean isLoggedIn();

    /**
     * True if the logged user is admin
     * @return
     */
    boolean isAdmin();

    /**
     * Gets the current permissionImplementation
     */
    String getPermissionImplementation();

    /**
     * Sets the current permission implementation
     */
    void setPermissionImplementation(String implementation);


    /**
     * @return the id of the logged entity
     */
    public long getLoggedEntityId();

}
