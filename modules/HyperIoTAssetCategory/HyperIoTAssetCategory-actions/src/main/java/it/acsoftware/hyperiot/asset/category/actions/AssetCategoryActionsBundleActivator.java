package it.acsoftware.hyperiot.asset.category.actions;

import it.acsoftware.hyperiot.asset.category.model.AssetCategory;
import it.acsoftware.hyperiot.base.action.HyperIoTActionList;
import it.acsoftware.hyperiot.base.action.HyperIoTPermissionActivator;
import it.acsoftware.hyperiot.base.action.util.HyperIoTActionFactory;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Aristide Cittadino Model class that define a bundle activator and
 * register actions for AssetCategory
 */
public class AssetCategoryActionsBundleActivator extends HyperIoTPermissionActivator {
    /**
     * Return a list actions that have to be registerd as OSGi components
     */
    @Override
    public List<HyperIoTActionList> getActions() {
        // creates base Actions save,update,remove,find,findAll for the specified entity
        getLog().info( "Registering base CRUD actions for {}", new Object[]{AssetCategory.class.getName()});
        ArrayList<HyperIoTActionList> actionsLists = new ArrayList<>();
        HyperIoTActionList actionList = HyperIoTActionFactory.createBaseCrudActionList(AssetCategory.class.getName(),
            AssetCategory.class.getName());
        actionsLists.add(actionList);
        //TO DO: add more actions to actionList here...
        return actionsLists;
    }

}
