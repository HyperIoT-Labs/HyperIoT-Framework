package it.acsoftware.hyperiot.base.api.proxy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Author Aristide Cittadino
 * Annotation to use in addition to those the user will define which are related to pre-processing or post-processing
 * of HyperIoT Services methods
 */
@Target({ElementType.ANNOTATION_TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface HyperIoTInterceptorExecutor {
    Class<? extends HyperIoTMethodInterceptor> interceptor();
}
