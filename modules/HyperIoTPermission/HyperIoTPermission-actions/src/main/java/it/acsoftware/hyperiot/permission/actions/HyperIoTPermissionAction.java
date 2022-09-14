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
