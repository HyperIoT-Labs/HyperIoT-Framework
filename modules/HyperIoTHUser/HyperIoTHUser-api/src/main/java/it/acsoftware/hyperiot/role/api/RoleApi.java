package it.acsoftware.hyperiot.role.api;

import java.util.Collection;

import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTBaseEntityApi;
import it.acsoftware.hyperiot.role.model.Role;

/**
 *
 * @author Aristide Cittadino Interface component for RoleApi. This interface
 *         defines methods for additional operations.
 *
 */
public interface RoleApi extends HyperIoTBaseEntityApi<Role> {
	/**
	 * Collection of user roles
	 *
	 * @param userId parameter required to find all user roles
	 * @return collection of user roles
	 */
	public Collection<Role> getUserRoles(long userId, HyperIoTContext ctx);

	/**
	 * Save a user role
	 *
	 * @param userId parameter required to find an existing user
	 * @param roleId parameter required to save a user role
	 * @param ctx    user context of HyperIoT platform
	 * @return the user's role saved
	 */
	public Role saveUserRole(long userId, long roleId, HyperIoTContext ctx);

	/**
	 * Remove a user role
	 *
	 * @param userId parameter required to find an existing role
	 * @param roleId parameter required to delete a user role
	 * @param ctx    user context of HyperIoT platform
	 * @return the user's role deleted
	 */
	public Role removeUserRole(long userId, long roleId, HyperIoTContext ctx);
}
