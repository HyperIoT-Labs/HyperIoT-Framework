package it.acsoftware.hyperiot.shared.entity.example.actions;

import it.acsoftware.hyperiot.base.action.HyperIoTActionList;
import it.acsoftware.hyperiot.base.action.HyperIoTPermissionActivator;
import it.acsoftware.hyperiot.base.action.util.HyperIoTActionFactory;
import it.acsoftware.hyperiot.base.action.util.HyperIoTShareAction;
import it.acsoftware.hyperiot.shared.entity.example.model.SharedEntityExample;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Aristide Cittadino Model class that define a bundle activator and
 * register actions for SharedEntityExample
 */
public class SharedEntityExampleActionsBundleActivator extends HyperIoTPermissionActivator {

    /**
     * Return a list actions that have to be registerd as OSGi components
     */
    @Override
    public List<HyperIoTActionList> getActions() {
        // creates base Actions save,update,remove,find,findAll for the specified entity
        getLog().info( "Registering base CRUD actions for {}", SharedEntityExample.class.getName());
        ArrayList<HyperIoTActionList> actionsLists = new ArrayList<>();
        HyperIoTActionList actionList = HyperIoTActionFactory.createBaseCrudActionList(SharedEntityExample.class.getName(),
            SharedEntityExample.class.getName());
        actionList.addAction(HyperIoTActionFactory.createAction(SharedEntityExample.class.getName(),
            SharedEntityExample.class.getName(), HyperIoTShareAction.SHARE));
        actionsLists.add(actionList);
        return actionsLists;
    }

}
