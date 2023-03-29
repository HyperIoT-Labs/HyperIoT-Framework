/*
 * Copyright 2019-2023 HyperIoT
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
