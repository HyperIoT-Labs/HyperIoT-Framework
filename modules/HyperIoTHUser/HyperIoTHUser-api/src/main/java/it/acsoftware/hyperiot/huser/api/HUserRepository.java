package it.acsoftware.hyperiot.huser.api;

import it.acsoftware.hyperiot.base.api.entity.HyperIoTAuthenticable;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTBaseRepository;

import it.acsoftware.hyperiot.huser.model.HUser;

/**
 *
 * @author Aristide Cittadino Interface for User Repository. This interface is
 *         used for CRUD operations, and to interact with the persistence layer.
 *
 */
public interface HUserRepository extends HyperIoTBaseRepository<HUser> {

	HUser changePassword(HUser user, String newPassword, String passwordConfirm);
	/**
	 * Find a user with admin role via query
	 *
	 * @return the user with admin role
	 */
	HUser findHAdmin();

	/**
	 * Find an existing user by username via query
	 *
	 * @param username parameter required to find a user
	 * @return the user with username entered
	 */
	HUser findByUsername(String username);

	/**
	 * Find an existing user by email via query
	 *
	 * @param email parameter required to find a user
	 * @return the user with email entered
	 */
	HUser findByEmail(String email);

	/**
	 *
	 * @param email Email for activation
	 * @param activationCode Activation Code
	 */
	void activateUser(String email, String activationCode);

    /**
     * Checks whether a username already exists inside database
     * @param hyperIoTAuthenticable
     * @return
     */
    boolean screeNameAlreadyExists(HyperIoTAuthenticable hyperIoTAuthenticable);
}
