package it.acsoftware.hyperiot.jobscheduler.actions;

import it.acsoftware.hyperiot.base.action.HyperIoTActionName;

/**
 * 
 * @author Aristide Cittadino Model class that enumerate JobScheduler Actions
 *
 */
public enum JobSchedulerAction implements HyperIoTActionName {
	
	//TO DO: add enumerations here
	ACTION_ENUM("action_enum");

	private String name;

     /**
	 * Role Action with the specified name.
	 * 
	 * @param name parameter that represent the JobScheduler  action
	 */
	private JobSchedulerAction(String name) {
		this.name = name;
	}

	/**
	 * Gets the name of JobScheduler action
	 */
	public String getName() {
		return name;
	}

}
