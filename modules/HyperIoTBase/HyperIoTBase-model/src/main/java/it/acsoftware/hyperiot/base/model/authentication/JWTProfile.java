package it.acsoftware.hyperiot.base.model.authentication;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Aristide Cittadino
 * Profile Object which stores all Users permissions specific for a resource
 */
public class JWTProfile {
    private Set<String> permissions;

    public JWTProfile() {
        super();
        permissions = new HashSet<String>();
    }

    /**
     * @param actions Action list
     */
    public void addPermissionInfo(List<String> actions) {
        permissions.addAll(actions);
    }

    /**
     * @return
     */
    public Set<String> getPermissions() {
        return permissions;
    }

}
