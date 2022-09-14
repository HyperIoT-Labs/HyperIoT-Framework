package it.acsoftware.hyperiot.contentrepository.actions;

import java.util.ArrayList;
import java.util.List;
import it.acsoftware.hyperiot.base.action.HyperIoTActionList;
import it.acsoftware.hyperiot.base.action.HyperIoTPermissionActivator;

/**
 * 
 * @author Aristide Cittadino Model class that define a bundle activator and
 * register actions for ContentRepository
 *
 */
public class ContentRepositoryActionsBundleActivator extends HyperIoTPermissionActivator {
	
	/**
	 * Return a list actions that have to be registerd as OSGi components
	 */
	@Override
	public List<HyperIoTActionList> getActions() {
		// creates base Actions save,update,remove,find,findAll for the specified entity
		log.info("Registering base CRUD actions...");
		ArrayList<HyperIoTActionList> actionsLists = new ArrayList<>();
		//TO DO: add more actions to actionList here...
		return actionsLists;
	}

}
