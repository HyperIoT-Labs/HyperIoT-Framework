package it.acsoftware.hyperiot.base.security.annotations.implementation;


import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.base.api.HyperIoTService;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTBaseEntity;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTBaseEntityApi;
import it.acsoftware.hyperiot.base.api.proxy.HyperIoTAfterMethodInterceptor;
import it.acsoftware.hyperiot.base.api.proxy.HyperIoTBeforeMethodInterceptor;
import it.acsoftware.hyperiot.base.exception.HyperIoTRuntimeException;
import it.acsoftware.hyperiot.base.exception.HyperIoTUnauthorizedException;
import it.acsoftware.hyperiot.base.security.annotations.AllowPermissionsOnReturn;
import org.osgi.service.component.annotations.Component;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author Aristide Cittadino
 * This class is the implementation of @AllowPermissionOnReturn annotation.
 * It simply verifies that the user has the permission to do some action on the specified entity returned by a method.
 * @see AllowPermissionInterceptor
 */
@Component(service = {HyperIoTAfterMethodInterceptor.class, AllowPermissionOnReturnInterceptor.class}, immediate = true)
public class AllowPermissionOnReturnInterceptor extends AbstractPermissionInterceptor implements HyperIoTAfterMethodInterceptor<AllowPermissionsOnReturn> {
    private static Logger log = LoggerFactory.getLogger(AllowPermissionOnReturnInterceptor.class.getName());

    @Override
    public void interceptMethod(HyperIoTService s, Method m, Object[] args, Object returnResult, AllowPermissionsOnReturn annotation) {
        log.debug( "Invoking interceptor @AllowPermission on method: {}", new Object[]{m.getName()});
        this.checkAnnotationIsOnHyperIoTApiClass(s, annotation);
        String[] actions = annotation.actions();
        if (s instanceof HyperIoTBaseEntityApi) {
            //we must check entity permissions
            HyperIoTBaseEntityApi<?> entityApi = (HyperIoTBaseEntityApi<?>) s;
            Class<?> entityType = entityApi.getEntityType();
            HyperIoTContext ctx = this.findHyperIoTContextInMethodParams(args);
            //TODO: we assume that an entity api with this annotation returns a result of entity, no collections. We should provide it
            if (returnResult instanceof HyperIoTBaseEntity) {
                HyperIoTBaseEntity entity = (HyperIoTBaseEntity) returnResult;
                boolean found = this.checkEntityPermission(ctx, entity, actions);
                if (!found)
                    throw new HyperIoTUnauthorizedException();
                return;
            }
            throw new HyperIoTRuntimeException(annotation.annotationType().getName() + " is incompatible with the return type of method " + m.getName());
        } else {
            //do nothing
        }
        throw new HyperIoTUnauthorizedException();
    }
}
