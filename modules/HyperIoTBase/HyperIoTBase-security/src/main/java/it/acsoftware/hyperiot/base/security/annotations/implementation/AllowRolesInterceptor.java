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
