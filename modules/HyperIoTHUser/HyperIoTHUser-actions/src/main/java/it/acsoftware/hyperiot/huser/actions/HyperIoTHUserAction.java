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

package it.acsoftware.hyperiot.huser.actions;

import it.acsoftware.hyperiot.base.action.HyperIoTActionName;

/**
 * 
 * @author Aristide Cittadino Model class for HUser Actions. It is used to
 *         enumerate all possible actions for HUser.
 *
 */
public enum HyperIoTHUserAction implements HyperIoTActionName {

	VIEW_USERS_MANAGEMENT("view_users_management"),
	IMPERSONATE("impersonate");

	/**
	 * String name for HUser Action
	 */
	private String name;

	/**
	 * 
	 * @param name parameter that represent the user action
	 */
	private HyperIoTHUserAction(String name) {
		this.name = name;
	}

	/**
	 * Gets the name of user action
	 */
	public String getName() {
		return name;
	}

}
