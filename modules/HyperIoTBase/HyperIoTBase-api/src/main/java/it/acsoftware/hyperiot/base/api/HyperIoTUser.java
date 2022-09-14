package it.acsoftware.hyperiot.base.api;

import it.acsoftware.hyperiot.base.api.entity.HyperIoTAuthenticable;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTProtectedEntity;

import java.util.Set;

/**
 * @author Aristide Cittadino Generic interface component for HyperIoTUser. This
 * interface defines all methods getter and setter for HUser class.
 */
public interface HyperIoTUser extends HyperIoTProtectedEntity, HyperIoTAuthenticable {

    /**
     * Gets the HUser name
     *
     * @return a string that represents HUser name
     */
    public String getName();

    /**
     * Sets the HUser name
     *
     * @param name sets the HUser name
     */
    public void setName(String name);

    /**
     * Gets the HUser lastname
     *
     * @return a string that represents HUser lastname
     */
    public String getLastname();

    /**
     * Sets the HUser lastname
     *
     * @param lastName sets the HUser lastname
     */
    public void setLastname(String lastName);

    /**
     * Gets the HUser email
     *
     * @return a string that represents HUser email
     */
    public String getEmail();

    /**
     * Sets the HUser email
     *
     * @param email sets the HUser email
     */
    public void setEmail(String email);

    /**
     * Gets the HUser username
     *
     * @return a string that represents HUser username
     */
    public String getUsername();

    /**
     * Sets the HUser username
     *
     * @param username sets the HUser username
     */
    public void setUsername(String username);

    /**
     * Gets the HUser Role
     *
     * @return a list that represents the HUser Role
     */
    public Set<? extends HyperIoTRole> getRoles();

    /**
     * Find if a role has been found by id
     *
     * @param roleId parameter that indicates the Role id
     * @return true if a role has been found
     */
    public boolean hasRole(long roleId);

    /**
     * Find if a role has been found by role name
     *
     * @param roleName parameter that indicates the Role name
     * @return true if a role has been found
     */
    public boolean hasRole(String roleName);

    /**
     * Find if a role has been found
     *
     * @param role parameter that indicates the Role object
     * @return true if a role has been found
     */
    public boolean hasRole(HyperIoTRole role);

    /**
     * Assign a Role
     *
     * @param role parameter that indicates a role
     * @return true if a role has been assigned
     */
    public boolean addRole(HyperIoTRole role);

    /**
     * Remove a Role
     *
     * @param role parameter that indicates a role
     * @return true if a role has been removed
     */
    public boolean removeRole(HyperIoTRole role);

    /**
     * Gets if HUser is administrator
     *
     * @return true if HUser is administrator
     */
    public boolean isAdmin();
}
