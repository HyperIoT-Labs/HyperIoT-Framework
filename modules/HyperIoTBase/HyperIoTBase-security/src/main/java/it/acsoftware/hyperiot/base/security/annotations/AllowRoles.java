package it.acsoftware.hyperiot.base.security.annotations;

import it.acsoftware.hyperiot.base.api.proxy.HyperIoTInterceptorExecutor;
import it.acsoftware.hyperiot.base.security.annotations.implementation.AllowRolesInterceptor;

import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Author Aristide Cittadino
 * Annotation to limit method execution only to users with certain roles
 */
@Target({ ElementType.METHOD })
@Retention(value = RetentionPolicy.RUNTIME)
@HyperIoTInterceptorExecutor(interceptor = AllowRolesInterceptor.class)
public @interface AllowRoles  {
    /**
     *
     * @return Role names array
     */
    String[] rolesNames() default {};
}
