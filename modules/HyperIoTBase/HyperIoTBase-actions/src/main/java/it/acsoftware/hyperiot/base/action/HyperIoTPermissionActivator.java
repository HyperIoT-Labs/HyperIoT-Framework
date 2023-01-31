/*
 * Copyright 2019-2023 ACSoftware
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

package it.acsoftware.hyperiot.base.action;

import it.acsoftware.hyperiot.base.api.HyperIoTAction;
import it.acsoftware.hyperiot.base.util.HyperIoTConstants;
import org.osgi.framework.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

/**
 * @author Aristide Cittadino Model class that define a bundle activator for a
 * generic entity of HyperIoT platform.
 */
public abstract class HyperIoTPermissionActivator implements BundleActivator {
    protected Logger log = LoggerFactory.getLogger(HyperIoTPermissionActivator.class.getName());

    /**
     * Registers a list of actions that have to be registered as OSGi components
     */
    public void registerActions() {
        log.debug("Invoking registerActions of {}", this.getClass().getSimpleName());
        BundleContext bc = this.getBundleContext();
        List<HyperIoTActionList> actionList = this.getActions();
        for (int i = 0; i < actionList.size(); i++) {
            HyperIoTActionList list = actionList.get(i);
            List<HyperIoTAction> actions = list.getList();
            if (list != null && actions.size() > 0) {
                //registering also the actionList in order to let oder moudles to add permission to same entity
                Dictionary<String, String> dictionary = new Hashtable<String, String>();
                dictionary.put(HyperIoTConstants.OSGI_ACTION_RESOURCE_NAME, list.getResourceName());
                bc.registerService(HyperIoTActionList.class, list, dictionary);
                for (int j = 0; j < actions.size(); j++) {
                    HyperIoTAction action = actions.get(j);
                    if (!action.isRegistered()) {
                        dictionary = new Hashtable<String, String>();
                        dictionary.put(HyperIoTConstants.OSGI_ACTION_RESOURCE_NAME, list.getResourceName());
                        dictionary.put(HyperIoTConstants.OSGI_ACTION_RESOURCE_CATEGORY, action.getCategory());
                        dictionary.put(HyperIoTConstants.OSGI_ACTION_NAME, action.getActionName());
                        action.setRegistered(true);
                        bc.registerService(HyperIoTAction.class, action, dictionary);
                        log.debug("Action Registered : {} - {} - {}", new Object[]{action.getActionName(), action.getResourceName(), action.getCategory()});
                    } else {
                        log.debug("Action Already Registered : {} - {} - {}", new Object[]{action.getActionName(), action.getResourceName(), action.getCategory()});
                    }
                }
            }
        }
    }

    /**
     * Releases an action that have to be registered as OSGi components
     */
    public void unregisterActions() {
        List<HyperIoTActionList> actionList = this.getActions();
        for (int i = 0; i < actionList.size(); i++) {
            HyperIoTActionList list = actionList.get(i);
            log.debug("Invoking unregisterActions of {}", list.getResourceName());
            BundleContext bc = this.getBundleContext();
            try {
                ServiceReference<?>[] actions;
                actions = bc.getAllServiceReferences(HyperIoTAction.class.getName(),
                    "(" + HyperIoTConstants.OSGI_ACTION_RESOURCE_NAME + "=" + list.getResourceName() + ")");
                for (int j = 0; j < actions.length; j++) {
                    bc.ungetService(actions[j]);
                    log.debug("Action Unregistered : {}", actions[j]);
                }
            } catch (InvalidSyntaxException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Returns the BundleContext of the bundle which contains this component.
     *
     * @return The BundleContext of the bundle containing this component.
     */
    protected BundleContext getBundleContext() {
        BundleContext bundleContext = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        return bundleContext;
    }

    @Override
    public void start(BundleContext context) throws Exception {
        log.debug("Invoking start bundle of {}", this.getClass().getName());
        this.registerActions();

    }

    @Override
    public void stop(BundleContext context) throws Exception {
        log.debug("Invoking stop bundle of {}", this.getClass().getName());
        this.unregisterActions();

    }

    /**
     * Gets list of actions
     *
     * @return List of actions
     */
    public abstract List<HyperIoTActionList> getActions();

    /**
     * @return
     */
    public Logger getLog() {
        return this.log;
    }
}
