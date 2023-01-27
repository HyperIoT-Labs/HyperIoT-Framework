/*
 * Copyright 2019-2023 ACSoftware
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
