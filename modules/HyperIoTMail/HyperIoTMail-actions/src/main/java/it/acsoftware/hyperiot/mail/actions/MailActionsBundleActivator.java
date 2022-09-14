package it.acsoftware.hyperiot.mail.actions;

import it.acsoftware.hyperiot.base.action.HyperIoTActionList;
import it.acsoftware.hyperiot.base.action.HyperIoTPermissionActivator;
import it.acsoftware.hyperiot.base.action.util.HyperIoTActionFactory;
import it.acsoftware.hyperiot.mail.model.Mail;
import it.acsoftware.hyperiot.mail.model.MailTemplate;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Aristide Cittadino Model class that define a bundle activator and
 * register actions for Mail
 */
public class MailActionsBundleActivator extends HyperIoTPermissionActivator {

    /**
     * Return a list actions that have to be registerd as OSGi components
     */
    @Override
    public List<HyperIoTActionList> getActions() {
        // creates base Actions save,update,remove,find,findAll for the specified entity
        getLog().info( "Registering base CRUD actions for {}", MailTemplate.class.getName());
        ArrayList<HyperIoTActionList> actionsLists = new ArrayList<>();
        HyperIoTActionList actionList = HyperIoTActionFactory.createBaseCrudActionList(MailTemplate.class.getName(),
            MailTemplate.class.getName());
        HyperIoTActionList mailActionList = HyperIoTActionFactory.createEmptyActionList(Mail.class.getName());
        mailActionList.addAction(HyperIoTActionFactory.createAction(Mail.class.getName(), Mail.class.getName(), HyperIoTMailAction.SEND_EMAIL));
        //TO DO: add more actions to actionList here...
        actionsLists.add(actionList);
        actionsLists.add(mailActionList);
        return actionsLists;
    }

}
