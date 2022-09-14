package it.acsoftware.hyperiot.base.api.authentication;

import org.apache.cxf.rs.security.jose.jwt.JwtToken;

import it.acsoftware.hyperiot.base.api.HyperIoTBaseSystemApi;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTAuthenticable;

/**
 *
 * @author Aristide Cittadino Interface for System service for user login
 *
 */
public interface AuthenticationSystemApi extends HyperIoTBaseSystemApi {

	/**
	 * Method that verifies user credentials against database using all auth providers available
	 */
	public HyperIoTAuthenticable login(String username, String password);

	/**
	 * Method that verifies user credentials against database
	 */
	public HyperIoTAuthenticable login(String username, String password,String authProviderFilter);

	/**
	 * Checks wherever the user can access the requested topic
	 */
	public boolean userCanAccessTopic(String username, String topic);

	/**
	 *
	 * @param user user from which token must be generated
	 * @return JWT Token
	 */
	public String generateToken(HyperIoTAuthenticable user);

	/**
	 *
	 * @param token JWTToken Object
	 * @return JWT Token
	 */
	public String generateToken(JwtToken token);
}
