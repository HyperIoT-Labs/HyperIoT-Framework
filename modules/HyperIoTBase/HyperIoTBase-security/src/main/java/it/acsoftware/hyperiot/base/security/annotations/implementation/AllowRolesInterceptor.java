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

package it.acsoftware.hyperiot.base.security.annotations.implementation;

import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.base.api.HyperIoTService;
import it.acsoftware.hyperiot.base.api.proxy.HyperIoTBeforeMethodInterceptor;
import it.acsoftware.hyperiot.base.exception.HyperIoTRuntimeException;
import it.acsoftware.hyperiot.base.exception.HyperIoTUnauthorizedException;
import it.acsoftware.hyperiot.base.security.annotations.AllowRoles;
import it.acsoftware.hyperiot.base.security.util.HyperIoTSecurityUtil;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * @Author Aristide Cittadino
 * This class is the implementation of @AllowRoles annotation.
 * It simply verifies that the user has the a specific role before execute the invoked method.
 */
@Component(service = {HyperIoTBeforeMethodInterceptor.class, AllowRolesInterceptor.class}, immediate = true)
public class AllowRolesInterceptor extends AbstractPermissionInterceptor implements HyperIoTBeforeMethodInterceptor<AllowRoles> {
    private static Logger log = LoggerFactory.getLogger(AllowRolesInterceptor.class.getName());

    @Override
    public void interceptMethod(HyperIoTService s, Method m, Object[] args, AllowRoles annotation) {
        log.debug("Invoking interceptor @AllowRoles on method: {}", new Object[]{m.getName()});
        this.checkAnnotationIsOnHyperIoTApiClass(s, annotation);
        if (annotation.rolesNames() == null || annotation.rolesNames().length == 0)
            throw new HyperIoTRuntimeException("@AllowRoles needs at least one role name");
        String[] roles = annotation.rolesNames();
        HyperIoTContext ctx = this.findHyperIoTContextInMethodParams(args);
        if (!HyperIoTSecurityUtil.checkUserHasRoles(ctx, ctx.getLoggedUsername(), roles))
            throw new HyperIoTUnauthorizedException();
    }
}
