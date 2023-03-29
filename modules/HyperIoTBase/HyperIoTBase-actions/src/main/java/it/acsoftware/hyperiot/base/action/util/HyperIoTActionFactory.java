/*
 * Copyright 2019-2023 HyperIoT
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package it.acsoftware.hyperiot.base.action.util;

import it.acsoftware.hyperiot.base.action.HyperIoTActionImpl;
import it.acsoftware.hyperiot.base.action.HyperIoTActionList;
import it.acsoftware.hyperiot.base.action.HyperIoTActionName;
import it.acsoftware.hyperiot.base.api.HyperIoTAction;
import it.acsoftware.hyperiot.base.util.HyperIoTConstants;
import it.acsoftware.hyperiot.osgi.util.filter.OSGiFilterBuilder;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;


/**
 * @author Aristide Cittadino Model class that registers base actions.
 */
public class HyperIoTActionFactory {
    protected static Logger log = LoggerFactory.getLogger(HyperIoTActionFactory.class.getName());

    /**
     * Return action that have to be registered as OSGi components
     *
     * @param category     parameter that indicates the action category
     * @param resourceName parameter that indicates the resource name of action
     * @param name         parameter that indicates action name
     * @return action created
     */
    public static HyperIoTAction createAction(String category, String resourceName,
                                              HyperIoTActionName name) {
        log.debug( "Invoking createAction with category {}  for resource {} with action name {}", new Object[]{category, resourceName, name.getName()});
        return new HyperIoTActionImpl(category, resourceName, name.getName());
    }

    /**
     * Return action that have to be registered as OSGi components
     *
     * @param category    parameter that indicates the action category
     * @param entityClass parameter that indicates the entity that invokes action
     * @param name        parameter that indicates action name
     * @return action created
     */
    public static HyperIoTAction createAction(String category,
                                              @SuppressWarnings("rawtypes") Class entityClass, HyperIoTActionName name) {
        log.debug( "Invoking createAction with category {}  for resource {} with action name {}", new Object[]{category, entityClass.getName(), name.getName()});
        return new HyperIoTActionImpl(category, entityClass.getName(), name.getName());
    }

    /**
     * Return a list actions that have to be registered as OSGi components
     *
     * @param category        parameter that indicates the action category
     * @param entityClassName parameter that indicates the entity that invokes
     *                        action
     * @return a list actions
     */
    public static HyperIoTActionList createBaseCrudActionList(String category,
                                                              String entityClassName) {
        log.debug( "Invoking createBaseCrudActionList with category {} for resource {}", new Object[]{category, entityClassName});
        HyperIoTActionList actionList = new HyperIoTActionList(entityClassName);
        actionList.addAction(HyperIoTActionFactory.createAction(category, entityClassName,
            HyperIoTCrudAction.SAVE));
        actionList.addAction(HyperIoTActionFactory.createAction(category, entityClassName,
            HyperIoTCrudAction.UPDATE));
        actionList.addAction(HyperIoTActionFactory.createAction(category, entityClassName,
            HyperIoTCrudAction.REMOVE));
        actionList.addAction(HyperIoTActionFactory.createAction(category, entityClassName,
            HyperIoTCrudAction.FIND));
        actionList.addAction(HyperIoTActionFactory.createAction(category, entityClassName,
            HyperIoTCrudAction.FINDALL));
        return actionList;
    }

    /**
     * Initialize a list of actions
     */
    public static HyperIoTActionList createEmptyActionList(String resourceName) {
        return new HyperIoTActionList(resourceName);
    }

    public static HyperIoTActionList getActionListFromResource(String resourceName, BundleContext ctx) {
        HyperIoTActionList list = createEmptyActionList(resourceName);
        try {
            String filter = OSGiFilterBuilder.createFilter(HyperIoTConstants.OSGI_ACTION_RESOURCE_NAME, resourceName).getFilter();
            Collection<ServiceReference<HyperIoTActionList>> actionsListRef = ctx.getServiceReferences(HyperIoTActionList.class, filter);
            if (actionsListRef != null && actionsListRef.size() > 0) {
                return ctx.getService(actionsListRef.iterator().next());
            }
        } catch (InvalidSyntaxException e) {
            log.error( e.getMessage(), e);
        }
        return list;
    }
}
