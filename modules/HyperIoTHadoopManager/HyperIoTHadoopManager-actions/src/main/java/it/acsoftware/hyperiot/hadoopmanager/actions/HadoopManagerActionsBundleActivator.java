package it.acsoftware.hyperiot.hadoopmanager.actions;

import it.acsoftware.hyperiot.base.action.HyperIoTActionList;
import it.acsoftware.hyperiot.base.action.HyperIoTPermissionActivator;
import it.acsoftware.hyperiot.base.action.util.HyperIoTActionFactory;

import it.acsoftware.hyperiot.hadoopmanager.model.HadoopManager;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Aristide Cittadino Model class that define a bundle activator and
 * register actions for HadoopManager
 *
 */
public class HadoopManagerActionsBundleActivator extends HyperIoTPermissionActivator {
	/**
	 * Return a list actions that have to be registerd as OSGi components
	 */
	@Override
	public List<HyperIoTActionList> getActions() {
        ArrayList<HyperIoTActionList> actionsLists = new ArrayList<>();
		HyperIoTActionList actionList = HyperIoTActionFactory.createEmptyActionList(HadoopManager.class.getName());
		actionList.addAction(HyperIoTActionFactory.createAction(HadoopManager.class.getName(),
				HadoopManager.class.getName(), HadoopManagerAction.COPY_FILE));
		actionList.addAction(HyperIoTActionFactory.createAction(HadoopManager.class.getName(),
				HadoopManager.class.getName(), HadoopManagerAction.DELETE_FILE));
		actionsLists.add(actionList);
		return actionsLists;
	}

}
