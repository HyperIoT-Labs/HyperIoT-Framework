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
