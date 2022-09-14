package it.acsoftware.hyperiot.sparkmanager.actions;

import it.acsoftware.hyperiot.base.action.HyperIoTActionList;
import it.acsoftware.hyperiot.base.action.HyperIoTPermissionActivator;
import it.acsoftware.hyperiot.base.action.util.HyperIoTActionFactory;
import it.acsoftware.hyperiot.sparkmanager.model.SparkManager;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Aristide Cittadino Model class that define a bundle activator and
 * register actions for SparkManager
 */
public class SparkManagerActionsBundleActivator extends HyperIoTPermissionActivator {

    /**
     * Return a list actions that have to be registered as OSGi components
     */
    @Override
    public List<HyperIoTActionList> getActions() {
        getLog().info( "Registering base actions for {}", SparkManager.class.getName());
        ArrayList<HyperIoTActionList> actionsLists = new ArrayList<>();
        HyperIoTActionList actionList = HyperIoTActionFactory.createEmptyActionList(SparkManager.class.getName());
        actionList.addAction(HyperIoTActionFactory.createAction(SparkManager.class.getName(),
            SparkManager.class.getName(), SparkManagerAction.GET_JOB_STATUS));
        actionList.addAction(HyperIoTActionFactory.createAction(SparkManager.class.getName(),
            SparkManager.class.getName(), SparkManagerAction.KILL_JOB));
        actionList.addAction(HyperIoTActionFactory.createAction(SparkManager.class.getName(),
            SparkManager.class.getName(), SparkManagerAction.SUBMIT_JOB));
        actionsLists.add(actionList);
        return actionsLists;
    }

}
