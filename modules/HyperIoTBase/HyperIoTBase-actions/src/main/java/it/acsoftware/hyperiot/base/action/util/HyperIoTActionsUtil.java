package it.acsoftware.hyperiot.base.action.util;

import it.acsoftware.hyperiot.base.action.HyperIoTActionName;
import it.acsoftware.hyperiot.base.api.HyperIoTAction;
import it.acsoftware.hyperiot.base.exception.HyperIoTRuntimeException;
import it.acsoftware.hyperiot.base.util.HyperIoTConstants;
import it.acsoftware.hyperiot.base.util.HyperIoTUtil;
import it.acsoftware.hyperiot.osgi.util.filter.OSGiFilterBuilder;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Aristide Cittadino Class with some helper methods about Actions
 */
public class HyperIoTActionsUtil {
    private static Logger log = LoggerFactory.getLogger(HyperIoTActionsUtil.class.getName());

    /**
     * @param className className related to the Action
     * @param action    ActionName
     * @return the OSGi registered action
     */
    public static HyperIoTAction getHyperIoTAction(String className, HyperIoTActionName action) {
        log.debug(
            "Service getAction for {}", new Object[]{className, action.getName()});
        Collection<ServiceReference<HyperIoTAction>> serviceReferences;
        try {
            String actionFilter = OSGiFilterBuilder
                .createFilter(HyperIoTConstants.OSGI_ACTION_RESOURCE_NAME, className)
                .and(HyperIoTConstants.OSGI_ACTION_NAME, action.getName()).getFilter();
            log.debug(
                "Searching for OSGi registered action with filter: {}", actionFilter);
            serviceReferences = HyperIoTUtil.getBundleContext(HyperIoTActionName.class)
                .getServiceReferences(HyperIoTAction.class, actionFilter);
            if (serviceReferences.isEmpty()) {
                log.debug("No OSGi action found for filter: " + actionFilter);
                throw new HyperIoTRuntimeException();
            }
            HyperIoTAction act = (HyperIoTAction) HyperIoTUtil
                .getBundleContext(HyperIoTActionName.class)
                .getService(serviceReferences.iterator().next());
            log.debug( "OSGi action found {}", act);
            return act;
        } catch (InvalidSyntaxException e) {
            log.error( "Invalid OSGi Syntax", e);
            throw new HyperIoTRuntimeException();
        }
    }

    /**
     * @param className className related to the Action
     * @return the OSGi registered action
     */
    public static List<HyperIoTAction> getHyperIoTCrudActions(String className) {
        log.debug(
            "Service getAction for {}", className);
        List<HyperIoTAction> actions = new ArrayList<>();
        for (int i = 0; i < HyperIoTCrudAction.values().length; i++) {
            actions.add(getHyperIoTAction(className, HyperIoTCrudAction.values()[i]));
        }
        return actions;
    }

    public static List<HyperIoTAction> getHyperIoTActions() {
        log.debug( "Service getActions ");
        Collection<ServiceReference<HyperIoTAction>> serviceReferences;
        try {

            log.debug( "Searching for OSGi registered actions");
            serviceReferences = HyperIoTUtil.getBundleContext(HyperIoTActionName.class)
                .getServiceReferences(HyperIoTAction.class, null);
            List<HyperIoTAction> actions = serviceReferences.stream()
                .map(serviceReference -> (HyperIoTAction) HyperIoTUtil
                    .getBundleContext(HyperIoTActionName.class)
                    .getService(serviceReference))
                .collect(Collectors.toList());
            return actions;

        } catch (InvalidSyntaxException e) {
            log.error( "Invalid OSGi Syntax", e);
            throw new HyperIoTRuntimeException();
        }
    }

    /**
     * @param className className related to the Actions
     * @return the OSGi registered actions
     */
    public static List<HyperIoTAction> getHyperIoTActions(String className) {
        log.debug(
            "Service getActions for {}", className);
        Collection<ServiceReference<HyperIoTAction>> serviceReferences;
        try {
            String actionFilter = OSGiFilterBuilder
                .createFilter(HyperIoTConstants.OSGI_ACTION_RESOURCE_NAME, className).getFilter();
            log.debug( "Searching for OSGi registered action with filter: {}", actionFilter);
            serviceReferences = HyperIoTUtil.getBundleContext(HyperIoTActionName.class)
                .getServiceReferences(HyperIoTAction.class, actionFilter);
            if (serviceReferences.isEmpty()) {
                log.debug( "No OSGi action found for filter: " + actionFilter);
                throw new HyperIoTRuntimeException();
            }
            List<HyperIoTAction> hyperIoTActionList = new ArrayList<>();
            for (ServiceReference<HyperIoTAction> hyperIoTAction : serviceReferences) {
                hyperIoTActionList.add(HyperIoTUtil.getBundleContext(HyperIoTActionName.class)
                    .getService(hyperIoTAction));
            }
            return hyperIoTActionList;
        } catch (InvalidSyntaxException e) {
            log.error( "Invalid OSGi Syntax", e);
            throw new HyperIoTRuntimeException();
        }
    }

}
