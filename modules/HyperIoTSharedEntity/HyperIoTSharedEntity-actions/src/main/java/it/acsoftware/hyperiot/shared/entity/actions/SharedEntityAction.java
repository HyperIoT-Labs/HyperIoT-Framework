package it.acsoftware.hyperiot.shared.entity.actions;

import it.acsoftware.hyperiot.base.action.HyperIoTActionName;

/**
 *
 * @author Aristide Cittadino Model class that enumerate SharedEntity Actions
 *
 */
public enum SharedEntityAction implements HyperIoTActionName {

	//TO DO: add enumerations here
	ACTION_ENUM("action_enum");

	private String name;

     /**
	 * Role Action with the specified name.
	 *
	 * @param name parameter that represent the SharedEntity  action
	 */
	private SharedEntityAction(String name) {
		this.name = name;
	}

	/**
	 * Gets the name of SharedEntity action
	 */
	public String getName() {
		return name;
	}

	

}
