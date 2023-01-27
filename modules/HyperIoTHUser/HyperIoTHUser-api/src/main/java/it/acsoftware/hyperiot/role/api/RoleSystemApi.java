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