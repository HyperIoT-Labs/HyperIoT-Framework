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
