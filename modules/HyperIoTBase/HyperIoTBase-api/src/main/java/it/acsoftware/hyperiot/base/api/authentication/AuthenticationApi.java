package it.acsoftware.hyperiot.base.api.authentication;

import it.acsoftware.hyperiot.base.api.HyperIoTBaseApi;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTAuthenticable;

/**
 *
 * @author Aristide Cittadino
 * Interface for user authentication with JWT
 *
 */
public interface AuthenticationApi extends HyperIoTBaseApi {
	/**
	 *
	 * @param username - Username of the logging user
	 * @param password - Password of the loggin user
	 * @return logged user, null if user doesn't exists or for invalid credentials
	 */
	public HyperIoTAuthenticable login(String username, String password);

	/**
	 *
	 * @param user user from which token must be generated
	 * @return JWTLoginResponseTLogin
	 */
	public String generateToken(HyperIoTAuthenticable user);
}
