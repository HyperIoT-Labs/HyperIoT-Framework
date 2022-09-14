package it.acsoftware.hyperiot.base.command;

import it.acsoftware.hyperiot.base.model.authentication.context.HyperIoTContextFactory;
import it.acsoftware.hyperiot.base.api.HyperIoTContext;

import javax.security.auth.Subject;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Principal;
import java.util.Set;

/**
 * Author Aristide Cittadino
 * This class exposes utility methods for classes that must implement karaf command
 */
public class HyperIoTBaseCommand {

    /**
     * Return the (HyperIoTContext) security context
     * based on the current logged user.
     *
     * @return Current User Security Context
     */
    protected HyperIoTContext getSecurityContext() {
        AccessControlContext acc = AccessController.getContext();
        Subject subject = Subject.getSubject(acc);
        Set<Principal> principals = subject.getPrincipals();
        if (principals.size() > 0) {
            return HyperIoTContextFactory.createBasicContext(principals);
        }
        return null;
    }
}
