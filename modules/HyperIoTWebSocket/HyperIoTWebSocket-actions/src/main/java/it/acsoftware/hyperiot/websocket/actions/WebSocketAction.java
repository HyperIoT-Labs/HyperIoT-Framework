package it.acsoftware.hyperiot.websocket.actions;

import it.acsoftware.hyperiot.base.action.HyperIoTActionName;

/**
 * 
 * @author Aristide Cittadino Model class that enumerate WebSocket Actions
 *
 */
public enum WebSocketAction implements HyperIoTActionName {
	
	//TO DO: add enumerations here
	CREATE_CHANNEL("CREATE_CHANNEL");

	private String name;

     /**
	 * Role Action with the specified name.
	 * 
	 * @param name parameter that represent the WebSocket  action
	 */
	private WebSocketAction(String name) {
		this.name = name;
	}

	/**
	 * Gets the name of WebSocket action
	 */
	public String getName() {
		return name;
	}

}
