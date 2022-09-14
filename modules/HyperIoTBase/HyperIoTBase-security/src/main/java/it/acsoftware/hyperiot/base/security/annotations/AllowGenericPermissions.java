package it.acsoftware.hyperiot.base.security.annotations;

import it.acsoftware.hyperiot.base.api.proxy.HyperIoTInterceptorExecutor;
import it.acsoftware.hyperiot.base.security.annotations.implementation.AllowGenericPermissionInterceptor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Author Aristide Cittadino
 * Annotation to limit method execution only to users which has generic permission on the resource.
 * Generic permission means that the user owns the <resouceName> <ActionId> permission not the specific one
 * which is identified by the primary key of the resource.
 */
@Target({ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME)
@HyperIoTInterceptorExecutor(interceptor = AllowGenericPermissionInterceptor.class)
public @interface AllowGenericPermissions {
    /**
     *
     * @return List of actions to be verified by the permission system
     */
    String[] actions() default {};

    /**
     * Optional: if you want to force the resource name (in case you are managing a non-entity service)
     * @return
     */
    String resourceName() default "";

    /**
     * Optional: if you want to force the resource name given by a parameter name passed to the method
     * @return
     */
    String resourceParamName() default "";
}
