package it.acsoftware.hyperiot.shared.entity.example.actions;

import it.acsoftware.hyperiot.base.action.HyperIoTActionName;

/**
 * 
 * @author Aristide Cittadino Model class that enumerate SharedEntityExample Actions
 *
 */
public enum SharedEntityExampleAction implements HyperIoTActionName {
	
	//TO DO: add enumerations here
	ACTION_ENUM("action_enum");

	private String name;

     /**
	 * Role Action with the specified name.
	 * 
	 * @param name parameter that represent the SharedEntityExample  action
	 */
	private SharedEntityExampleAction(String name) {
		this.name = name;
	}

	/**
	 * Gets the name of SharedEntityExample action
	 */
	public String getName() {
		return name;
	}

}
