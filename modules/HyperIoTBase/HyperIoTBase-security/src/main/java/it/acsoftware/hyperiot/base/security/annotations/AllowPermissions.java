package it.acsoftware.hyperiot.base.security.annotations;

import it.acsoftware.hyperiot.base.api.proxy.HyperIoTInterceptorExecutor;
import it.acsoftware.hyperiot.base.security.annotations.implementation.AllowGenericPermissionInterceptor;
import it.acsoftware.hyperiot.base.security.annotations.implementation.AllowPermissionInterceptor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Author Aristide Cittadino
 * Annotation to limit method execution only to users which has specific permission
 */
@Target({ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME)
@HyperIoTInterceptorExecutor(interceptor = AllowPermissionInterceptor.class)
public @interface AllowPermissions {
    String[] actions() default {};
    boolean checkById() default false;
    int idParamIndex() default 0;
    String systemApiRef() default "";
}
