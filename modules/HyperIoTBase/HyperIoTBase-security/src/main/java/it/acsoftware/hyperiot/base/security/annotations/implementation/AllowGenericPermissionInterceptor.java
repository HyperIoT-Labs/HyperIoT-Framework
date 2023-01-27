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


import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.base.api.HyperIoTService;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTBaseEntityApi;
import it.acsoftware.hyperiot.base.api.proxy.HyperIoTBeforeMethodInterceptor;
import it.acsoftware.hyperiot.base.exception.HyperIoTRuntimeException;
import it.acsoftware.hyperiot.base.exception.HyperIoTUnauthorizedException;
import it.acsoftware.hyperiot.base.security.annotations.AllowGenericPermissions;
import it.acsoftware.hyperiot.base.security.util.HyperIoTSecurityUtil;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * @Author Aristide Cittadino
 * This class is the implementation of @AllowGenericPermission annotation.
 * It simply verifies that the user has the permission to do some action on the resource name (without id).
 * It's a generic permission, this means that permission verification is based only on the resource name and action name not on the entity id
 *
 * The method tries to automatically infers the entity type (in case of EntityApi).
 */
@Component(service = {HyperIoTBeforeMethodInterceptor.class, AllowGenericPermissionInterceptor.class}, immediate = true)
public class AllowGenericPermissionInterceptor extends AbstractPermissionInterceptor implements HyperIoTBeforeMethodInterceptor<AllowGenericPermissions> {
    private static Logger log = LoggerFactory.getLogger(AllowGenericPermissionInterceptor.class.getName());

    @Override
    public void interceptMethod(HyperIoTService s, Method m, Object[] args, AllowGenericPermissions annotation) {
        log.debug("Invoking interceptor @AllowPermission on method: {}", new Object[]{m.getName()});
        this.checkAnnotationIsOnHyperIoTApiClass(s, annotation);
        String[] actions = annotation.actions();
        String resourceName = null;
        HyperIoTContext ctx = this.findHyperIoTContextInMethodParams(args);
        if (s instanceof HyperIoTBaseEntityApi) {
            //we must check entity permissions
            HyperIoTBaseEntityApi<?> entityApi = (HyperIoTBaseEntityApi<?>) s;
            //User can customize resource name on which permissions must be checked
            if (annotation.resourceName().length() > 0) {
                resourceName = annotation.resourceName();
            } else if (annotation.resourceParamName().length() > 0) {
                int index = findMethodParamIndexByName(annotation.resourceParamName(), m);
                if (index > 0) {
                    resourceName = (String) args[index];
                } else {
                    throw new RuntimeException("Resource Name Param " + annotation.resourceParamName() + " Not exists!");
                }
            } else
                resourceName = entityApi.getEntityType().getName();
        } else {
            if (annotation.resourceName().length() == 0 && annotation.resourceParamName().length() == 0) {
                throw new HyperIoTRuntimeException("@AllowGenericPermission needs a resource name! with resourceName param or resourceParamName!");
            }
            if (annotation.resourceName().length() > 0) {
                resourceName = annotation.resourceName();
            } else {
                int index = findMethodParamIndexByName(annotation.resourceParamName(), m);
                if (index > 0) {
                    resourceName = (String) args[index];
                } else {
                    throw new RuntimeException("Resource Name Param " + annotation.resourceParamName() + " Not exists!");
                }
            }
        }
        if (ctx != null) {
            boolean found = false;
            for (int i = 0; !found && i < actions.length; i++) {
                found = found || HyperIoTSecurityUtil.checkPermission(ctx, resourceName,
                    this.getAction(resourceName, actions[i]));
            }
            if (!found)
                throw new HyperIoTUnauthorizedException();
            return;
        }
        throw new HyperIoTUnauthorizedException();
    }


}
