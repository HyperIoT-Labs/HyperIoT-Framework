package it.acsoftware.hyperiot.jobscheduler.actions;

import it.acsoftware.hyperiot.base.action.HyperIoTActionList;
import it.acsoftware.hyperiot.base.action.HyperIoTPermissionActivator;
import it.acsoftware.hyperiot.base.action.util.HyperIoTActionFactory;
import it.acsoftware.hyperiot.jobscheduler.model.JobScheduler;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Aristide Cittadino Model class that define a bundle activator and
 * register actions for JobScheduler
 */
public class JobSchedulerActionsBundleActivator extends HyperIoTPermissionActivator {

    /**
     * Return a list actions that have to be registerd as OSGi components
     */
    @Override
    public List<HyperIoTActionList> getActions() {
        // creates base Actions save,update,remove,find,findAll for the specified entity
        getLog().info( "Registering base CRUD actions for {}", JobScheduler.class.getName());
        ArrayList<HyperIoTActionList> actionsLists = new ArrayList<>();
        HyperIoTActionList actionList = HyperIoTActionFactory.createBaseCrudActionList(JobScheduler.class.getName(),
            JobScheduler.class.getName());
        actionsLists.add(actionList);
        //TO DO: add more actions to actionList here...
        return actionsLists;
    }

}
