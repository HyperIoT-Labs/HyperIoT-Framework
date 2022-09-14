package it.acsoftware.hyperiot.hbase.connector.actions;

import it.acsoftware.hyperiot.base.action.HyperIoTActionList;
import it.acsoftware.hyperiot.base.action.HyperIoTPermissionActivator;
import it.acsoftware.hyperiot.base.action.util.HyperIoTActionFactory;
import it.acsoftware.hyperiot.hbase.connector.model.HBaseConnector;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Aristide Cittadino Model class that define a bundle activator and
 * register actions for HBaseConnector
 */
public class HBaseConnectorActionsBundleActivator extends HyperIoTPermissionActivator {

    /**
     * Return a list actions that have to be registerd as OSGi components
     */
    @Override
    public List<HyperIoTActionList> getActions() {
        // creates base Actions save,update,remove,find,findAll for the specified entity
        getLog().info( "Registering base CRUD actions for {}", HBaseConnector.class.getName());
        ArrayList<HyperIoTActionList> actionsLists = new ArrayList<>();
        HyperIoTActionList actionList = HyperIoTActionFactory.createEmptyActionList(HBaseConnector.class.getName());
        actionList.addAction(HyperIoTActionFactory.createAction(HBaseConnector.class.getName(),
            HBaseConnector.class.getName(), HBaseConnectorAction.CHECK_CONNECTION));
        actionList.addAction(HyperIoTActionFactory.createAction(HBaseConnector.class.getName(),
            HBaseConnector.class.getName(), HBaseConnectorAction.CREATE_TABLE));
        actionList.addAction(HyperIoTActionFactory.createAction(HBaseConnector.class.getName(),
            HBaseConnector.class.getName(), HBaseConnectorAction.DELETE_DATA));
        actionList.addAction(HyperIoTActionFactory.createAction(HBaseConnector.class.getName(),
            HBaseConnector.class.getName(), HBaseConnectorAction.DISABLE_TABLE));
        actionList.addAction(HyperIoTActionFactory.createAction(HBaseConnector.class.getName(),
            HBaseConnector.class.getName(), HBaseConnectorAction.DROP_TABLE));
        actionList.addAction(HyperIoTActionFactory.createAction(HBaseConnector.class.getName(),
            HBaseConnector.class.getName(), HBaseConnectorAction.ENABLE_TABLE));
        actionList.addAction(HyperIoTActionFactory.createAction(HBaseConnector.class.getName(),
            HBaseConnector.class.getName(), HBaseConnectorAction.INSERT_DATA));
        actionList.addAction(HyperIoTActionFactory.createAction(HBaseConnector.class.getName(),
            HBaseConnector.class.getName(), HBaseConnectorAction.READ_DATA));
        actionsLists.add(actionList);
        return actionsLists;
    }

}
