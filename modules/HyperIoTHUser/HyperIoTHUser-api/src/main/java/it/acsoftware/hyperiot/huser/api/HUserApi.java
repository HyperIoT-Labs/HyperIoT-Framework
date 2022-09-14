package it.acsoftware.hyperiot.huser.api;

import it.acsoftware.hyperiot.base.api.HyperIoTAuthenticationProvider;
import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTBaseEntityApi;
import it.acsoftware.hyperiot.huser.model.HUser;

/**
 *
 * @author Aristide Cittadino Interface for HUserApi. This interface defines
 *         methods for additional operations.
 *
 */
public interface HUserApi extends HyperIoTBaseEntityApi<HUser>, HyperIoTAuthenticationProvider {
	/**
	 * Find an existing user by username
	 *
	 * @param username parameter required to find a user
	 * @return the user researched
	 */
	HUser findUserByUsername(String username);

	/**
	 *
	 * @param u   User to be registered
	 * @param ctx Context
	 */
	void registerUser(HUser u, HyperIoTContext ctx);

	/**
	 * Method for activating user
	 *
	 * @param email User email
	 */
	void activateUser(String email, String activationCode);

	/**
	 *
	 * @param email
	 */
	void passwordResetRequest(String email);

	/**
	 *
	 * @param email
	 * @param resetCode
	 * @param password
	 * @param passwordConfirm
	 */
	void resetPassword(String email, String resetCode, String password,
			String passwordConfirm);

	/**
	 *
	 * @param context
	 * @param userId
	 * @param oldPassword
	 * @param newPassowrod
	 * @param passwordConfirm
	 * @return
	 */
	HUser changePassword(HyperIoTContext context, long userId, String oldPassword,
			String newPassowrod, String passwordConfirm);

    /**
     *
     * @param context
     * @param userId
     * @param oldPassword
     * @param newPassowrod
     * @param passwordConfirm
     * @return
     */
    HUser adminChangePassword(HyperIoTContext context, long userId, String oldPassword,
                         String newPassowrod, String passwordConfirm);

	/**
	 *
	 * @param context
	 * @param user
	 * @return
	 */
	HUser updateAccountInfo(HyperIoTContext context,HUser user);

    /**
     *
     * @param context
     * @param user
     * @return
     */
    HUser adminUpdateAccountInfo(HyperIoTContext context,HUser user);

	void deleteAccountRequest(HyperIoTContext ctx);

	void deleteAccount(HyperIoTContext ctx, long userId, String deletionCode);

}
