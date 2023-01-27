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

package it.acsoftware.hyperiot.base.security.annotations.implementation;

import it.acsoftware.hyperiot.base.api.*;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTBaseEntity;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTBaseEntityApi;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTBaseEntitySystemApi;
import it.acsoftware.hyperiot.base.exception.HyperIoTRuntimeException;
import it.acsoftware.hyperiot.base.security.util.HyperIoTSecurityUtil;
import it.acsoftware.hyperiot.base.util.HyperIoTConstants;
import it.acsoftware.hyperiot.base.util.HyperIoTUtil;
import it.acsoftware.hyperiot.osgi.util.filter.OSGiFilterBuilder;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author Aristide Cittadino
 * Abstract logic for interceptors
 */
public abstract class AbstractPermissionInterceptor {
    private Logger log = LoggerFactory.getLogger(AbstractPermissionInterceptor.class.getName());

    /**
     * Return current class name and action name registered as OSGi components
     *
     * @param className  parameter that indicates the class name
     * @param actionName parameter that indicates the action name
     * @return class name and action name registered as OSGi components
     */
    protected HyperIoTAction getAction(String className, String actionName) {
        log.debug(
            "Service getAction for {} and action {}", new Object[]{className, actionName});
        Collection<ServiceReference<HyperIoTAction>> serviceReferences;
        try {
            String actionFilter = OSGiFilterBuilder
                .createFilter(HyperIoTConstants.OSGI_ACTION_RESOURCE_NAME, className)
                .and(HyperIoTConstants.OSGI_ACTION_NAME, actionName).getFilter();
            log.debug(
                "Searching for OSGi registered action with filter: {}", actionFilter);
            serviceReferences = HyperIoTUtil.getBundleContext(this)
                .getServiceReferences(HyperIoTAction.class, actionFilter);
            if (serviceReferences.size() > 1) {
                log.error( "More OSGi action found for filter: {}", actionFilter);
                throw new HyperIoTRuntimeException();
            } else if (serviceReferences.size() == 0) {
                return null;
            }
            HyperIoTAction act = HyperIoTUtil.getBundleContext(this)
                .getService(serviceReferences.iterator().next());
            log.debug( "OSGi action found {}", act);
            return act;
        } catch (InvalidSyntaxException e) {
            log.error( "Invalid OSGi Syntax", e);
            throw new HyperIoTRuntimeException();
        }
    }

    /**
     * @param args method arguments
     * @return HyperIoTContext found as parameter
     */
    protected HyperIoTContext findHyperIoTContextInMethodParams(Object[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof HyperIoTContext)
                return (HyperIoTContext) args[i];
        }
        return null;
    }

    /**
     * Find first param with specific type inside a method
     *
     * @param type
     * @param args
     * @param <T>
     * @return
     */
    protected <T extends HyperIoTResource> T findObjectTypeInParams(Class<T> type, Object[] args) {
        for (int i = 0; i < args.length; i++) {
            if (type.isAssignableFrom(args[i].getClass())) {
                return (T) args[i];
            }
        }
        return null;
    }

    /**
     * @param paramName
     * @param m
     * @return
     */
    protected int findMethodParamIndexByName(String paramName, Method m) {
        Parameter[] parameters = m.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].getName().equals(paramName)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * @param ctx
     * @param entity
     * @param actions
     * @param <T>
     * @return
     */
    protected <T extends HyperIoTBaseEntityApi> boolean checkEntityPermission(HyperIoTContext ctx, HyperIoTBaseEntity entity, String[] actions) {
        try {
            boolean found = false;
            if (ctx != null && entity != null) {
                for (int i = 0; !found && i < actions.length; i++) {
                    found = found || HyperIoTSecurityUtil.checkPermission(ctx, entity,
                        this.getAction(entity.getResourceName(), actions[i]));
                }
            }
            return found;
        } catch (Throwable t) {
            log.error( t.getMessage(), t);
        }
        return false;
    }

    /**
     * @param s
     * @param a
     */
    protected void checkAnnotationIsOnHyperIoTApiClass(HyperIoTService s, Annotation a) {
        if (!(s instanceof HyperIoTBaseEntityApi) && !(s instanceof HyperIoTBaseApi))
            throw new HyperIoTRuntimeException("Annotation " + a.annotationType().getName() + " Is accepted only on Api Classes,not on System or other services!");
    }

    /**
     * @param s
     * @param a
     */
    protected void checkAnnotationIsOnHyperIoTSystemApiClass(HyperIoTService s, Annotation a) {
        if (!(s instanceof HyperIoTBaseEntitySystemApi) && !(s instanceof HyperIoTBaseSystemApi))
            throw new HyperIoTRuntimeException("Annotation " + a.annotationType().getName() + " Is accepted only on SystemApi Classes,not on generic Api or other services!");
    }
}
