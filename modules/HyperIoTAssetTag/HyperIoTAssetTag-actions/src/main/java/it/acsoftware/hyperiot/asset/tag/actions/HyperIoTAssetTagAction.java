package it.acsoftware.hyperiot.asset.tag.actions;

import it.acsoftware.hyperiot.base.action.HyperIoTActionName;

/**
 * 
 * @author Aristide Cittadino Model class that enumerate AssetTag Actions
 *
 */
public enum HyperIoTAssetTagAction implements HyperIoTActionName {
	
	//TO DO: add enumerations here
	ACTION_ENUM("action_enum");

	private String name;

     /**
	 * Role Action with the specified name.
	 * 
	 * @param name parameter that represent the AssetTag  action
	 */
	private HyperIoTAssetTagAction(String name) {
		this.name = name;
	}

	/**
	 * Gets the name of AssetTag action
	 */
	public String getName() {
		return name;
	}

}
