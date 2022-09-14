package it.acsoftware.hyperiot.company.actions;

import it.acsoftware.hyperiot.base.action.HyperIoTActionName;

/**
 * 
 * @author Aristide Cittadino Model class that enumerate Company Actions
 *
 */
public enum CompanyAction implements HyperIoTActionName {
	
	//TO DO: add enumerations here
	ACTION_ENUM("action_enum");

	private String name;

     /**
	 * Role Action with the specified name.
	 * 
	 * @param name parameter that represent the Company  action
	 */
	private CompanyAction(String name) {
		this.name = name;
	}

	/**
	 * Gets the name of Company action
	 */
	public String getName() {
		return name;
	}

}
