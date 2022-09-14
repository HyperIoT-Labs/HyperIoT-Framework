package it.acsoftware.hyperiot.asset.category.actions;

import it.acsoftware.hyperiot.base.action.HyperIoTActionName;

/**
 * 
 * @author Aristide Cittadino Model class that enumerate AssetCategory Actions
 *
 */
public enum HyperIoTAssetCategoryAction implements HyperIoTActionName {
	
	//TO DO: add enumerations here
	ACTION_ENUM("action_enum");

	private String name;

     /**
	 * Role Action with the specified name.
	 * 
	 * @param name parameter that represent the AssetCategory  action
	 */
	private HyperIoTAssetCategoryAction(String name) {
		this.name = name;
	}

	/**
	 * Gets the name of AssetCategory action
	 */
	public String getName() {
		return name;
	}

}
