package it.acsoftware.hyperiot.websocket.actions;

import it.acsoftware.hyperiot.base.action.HyperIoTActionList;
import it.acsoftware.hyperiot.base.action.HyperIoTPermissionActivator;
import it.acsoftware.hyperiot.base.action.util.HyperIoTActionFactory;
import it.acsoftware.hyperiot.websocket.api.channel.HyperIoTWebSocketChannel;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Aristide Cittadino Model class that define a bundle activator and
 * register actions for WebSocket
 */
public class WebSocketActionsBundleActivator extends HyperIoTPermissionActivator {

    /**
     * Return a list actions that have to be registerd as OSGi components
     */
    @Override
    public List<HyperIoTActionList> getActions() {
        // creates base Actions save,update,remove,find,findAll for the specified entity
        log.info("Registering base CRUD actions...");
        ArrayList<HyperIoTActionList> actionsLists = new ArrayList<>();

        HyperIoTActionList actionList = HyperIoTActionFactory.createEmptyActionList(HyperIoTWebSocketChannel.class.getName());
        actionList.addAction(HyperIoTActionFactory.createAction(HyperIoTWebSocketChannel.class.getName(), HyperIoTWebSocketChannel.class.getName(), WebSocketAction.CREATE_CHANNEL));
        actionsLists.add(actionList);
        //TO DO: add more actions to actionList here...
        return actionsLists;
    }

}
