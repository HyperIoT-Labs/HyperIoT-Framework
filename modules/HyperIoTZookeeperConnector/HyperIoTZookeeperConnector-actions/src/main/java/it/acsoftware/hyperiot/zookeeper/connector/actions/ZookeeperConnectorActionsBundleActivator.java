package it.acsoftware.hyperiot.zookeeper.connector.actions;

import it.acsoftware.hyperiot.base.action.HyperIoTActionList;
import it.acsoftware.hyperiot.base.action.HyperIoTPermissionActivator;
import it.acsoftware.hyperiot.base.action.util.HyperIoTActionFactory;
import it.acsoftware.hyperiot.zookeeper.connector.model.ZookeeperConnector;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Aristide Cittadino Model class that define a bundle activator and
 * register actions for ZookeeperConnector
 */
public class ZookeeperConnectorActionsBundleActivator extends HyperIoTPermissionActivator {

    /**
     * Return a list actions that have to be registerd as OSGi components
     */
    @Override
    public List<HyperIoTActionList> getActions() {
        getLog().info( "Registering base actions for {}", ZookeeperConnector.class.getName());
        ArrayList<HyperIoTActionList> actionsLists = new ArrayList<>();
        HyperIoTActionList actionList = HyperIoTActionFactory.createEmptyActionList(ZookeeperConnector.class.getName());
        actionList.addAction(HyperIoTActionFactory.createAction(ZookeeperConnector.class.getName(),
            ZookeeperConnector.class.getName(), ZookeeperConnectorAction.CHECK_LEADERSHIP));
        actionsLists.add(actionList);
        return actionsLists;
    }

}
