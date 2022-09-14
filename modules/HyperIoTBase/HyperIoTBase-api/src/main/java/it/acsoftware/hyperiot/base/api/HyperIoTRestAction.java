package it.acsoftware.hyperiot.base.api;

import javax.ws.rs.core.Response;

/**
 * @author Aristide Cittadino Generic interface component for
 * HyperIoTRestAction. This interface defines the method to return the
 * response produces in the Rest services.
 */
@FunctionalInterface
public interface HyperIoTRestAction {
    /**
     * @return The current Rest action
     */
    public Response doAction();
}
