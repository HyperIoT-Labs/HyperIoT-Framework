package it.acsoftware.hyperiot.role.api;

import java.util.Collection;

import it.acsoftware.hyperiot.base.api.entity.HyperIoTBaseEntitySystemApi;
import it.acsoftware.hyperiot.role.model.Role;

/**
 * 
 * @author Aristide Cittadino Interface component for RoleSystemApi. This
 *         interface defines methods for additional operations.
 *
 */
public interface RoleSystemApi extends HyperIoTBaseEntitySystemApi<Role> {
	/**
	 * Collection of user roles
	 * 
	 * @param userId parameter required to find all user roles
	 * @return collection of user roles
	 */
	public Collection<Role> getUserRoles(long userId);

	/**
	 *
	 * @param name role Name
	 * @return Role
	 */
	public Role findByName(String name);
}