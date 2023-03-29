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

package it.acsoftware.hyperiot.huser.api;

import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTAuthenticable;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTBaseEntitySystemApi;
import it.acsoftware.hyperiot.huser.model.HUser;

/**
 *
 * @author Aristide Cittadino Interface for HUserSystemApi. This interface
 *         defines methods for additional operations.
 *
 */
public interface HUserSystemApi extends HyperIoTBaseEntitySystemApi<HUser> {
	/**
	 * This method allows a user registration that will be accessible by
	 * unregistered users
	 *
	 * @param u   parameter required to register a user
	 * @param ctx user context of HyperIoT platform
	 */
	void registerUser(HUser u, HyperIoTContext ctx);

	/**
	 * This method allows to find an existing user by username
	 *
	 * @param username parameter required to find a user
	 * @return the user researched
	 */
	HUser findUserByUsername(String username);

	/**
	 *
	 * @param username
	 * @param password
	 * @return
	 */
	HUser login(String username,String password);

	/**
	 * This method allows to find an existing user by email
	 *
	 * @param email parameter required to find a user
	 * @return the user researched
	 */
	HUser findUserByEmail(String email);

	/**
	 * Changes HUser password
	 *
	 * @param user
	 * @param newPassowrod
	 * @param passwordConfirm
	 * @return
	 */
	HUser changePassword(HUser user, String newPassowrod, String passwordConfirm);

	/**
	 *
	 * @param ctx user context of HyperIoT platform
	 * @param deletionCode the deletion code (raw String that must be encoded)
	 * @return User on which update the deletion code
	 */
	HUser changeDeletionCode(HyperIoTContext ctx, String deletionCode);

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
