package it.acsoftware.hyperiot.huser.actions;

import it.acsoftware.hyperiot.base.action.HyperIoTActionList;
import it.acsoftware.hyperiot.base.action.HyperIoTPermissionActivator;
import it.acsoftware.hyperiot.base.action.util.HyperIoTActionFactory;
import it.acsoftware.hyperiot.huser.model.HUser;
import it.acsoftware.hyperiot.role.actions.HyperIoTRoleAction;
import it.acsoftware.hyperiot.role.model.Role;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Aristide Cittadino Model class that define a bundle activator and
 * register action for HUser
 */
public class HUserActionsBundleActivator extends HyperIoTPermissionActivator {

    /**
     * Return a list actions that have to be registed as OSGi components
     */
    @Override
    public List<HyperIoTActionList> getActions() {
        getLog().info( "Registering base CRUD actions for {}", HUser.class.getName());
        ArrayList<HyperIoTActionList> actionsLists = new ArrayList<>();
        // creates base Actions save,update,remove,find,findAll for the specified entity
        HyperIoTActionList huserActionList = HyperIoTActionFactory
            .createBaseCrudActionList(HUser.class.getName(), HUser.class.getName());
        huserActionList.addAction(HyperIoTActionFactory.createAction(HUser.class.getName(),
            HUser.class.getName(), HyperIoTHUserAction.IMPERSONATE));
        actionsLists.add(huserActionList);
        getLog().info( "Registering base CRUD actions for {}", Role.class.getName());
        HyperIoTActionList roleActionList = HyperIoTActionFactory.createBaseCrudActionList(Role.class.getName(),
            Role.class.getName());
        log.info("Registering " + HyperIoTRoleAction.ASSIGN_MEMBERS.getName() + " action");
        roleActionList.addAction(HyperIoTActionFactory.createAction(Role.class.getName(), Role.class.getName(),
            HyperIoTRoleAction.ASSIGN_MEMBERS));
        log.info("Registering " + HyperIoTRoleAction.REMOVE_MEMBERS.getName() + " action");
        roleActionList.addAction(HyperIoTActionFactory.createAction(Role.class.getName(), Role.class.getName(),
            HyperIoTRoleAction.REMOVE_MEMBERS));
        log.info("Registering " + HyperIoTRoleAction.API_ACCESS.getName() + " action");
        roleActionList.addAction(
            HyperIoTActionFactory.createAction(Role.class.getName(), Role.class.getName(), HyperIoTRoleAction.API_ACCESS));
        log.info("Registering " + HyperIoTRoleAction.PERMISSION.getName() + " action");
        roleActionList.addAction(
            HyperIoTActionFactory.createAction(Role.class.getName(), Role.class.getName(), HyperIoTRoleAction.PERMISSION));
        actionsLists.add(roleActionList);
        return actionsLists;
    }

}
