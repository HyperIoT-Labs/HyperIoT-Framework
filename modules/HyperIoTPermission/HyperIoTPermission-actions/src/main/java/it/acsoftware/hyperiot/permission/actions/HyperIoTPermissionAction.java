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

package it.acsoftware.hyperiot.permission.actions;

import it.acsoftware.hyperiot.base.action.HyperIoTActionName;

/**
 * 
 * @author Aristide Cittadino Model class that enumerate Permission Actions
 *
 */
public enum HyperIoTPermissionAction implements HyperIoTActionName {

	PERMISSION("permission"),
	LIST_ACTIONS("list_actions");

	/**
	 * String name for Permission Action
	 */
	private String name;

	/**
	 * Permission Action with the specified name
	 * 
	 * @param name parameter that represent the permission action
	 */
	private HyperIoTPermissionAction(String name) {
		this.name = name;
	}

	/**
	 * Gets the name of Permission Action
	 */
	public String getName() {
		return name;
	}

}
