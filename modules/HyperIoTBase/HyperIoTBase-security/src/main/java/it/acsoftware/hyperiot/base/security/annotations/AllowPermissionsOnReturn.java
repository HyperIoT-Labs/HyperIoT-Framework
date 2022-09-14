package it.acsoftware.hyperiot.base.security.annotations;

import it.acsoftware.hyperiot.base.api.proxy.HyperIoTInterceptorExecutor;
import it.acsoftware.hyperiot.base.security.annotations.implementation.AllowPermissionOnReturnInterceptor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Author Aristide Cittadino
 * Annotation to limit method execution only to users which has specific permission on method return
 */
@Target({ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME)
@HyperIoTInterceptorExecutor(interceptor = AllowPermissionOnReturnInterceptor.class)
public @interface AllowPermissionsOnReturn {
    String[] actions() default {};
}
