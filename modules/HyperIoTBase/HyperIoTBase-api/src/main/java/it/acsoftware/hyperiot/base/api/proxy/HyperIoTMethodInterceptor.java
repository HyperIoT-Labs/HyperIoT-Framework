package it.acsoftware.hyperiot.base.api.proxy;

import it.acsoftware.hyperiot.base.api.HyperIoTService;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Author Aristide Cittadino
 * @param <A>
 * Interface which maps the concept of HyperIoT method interceptor.
 * An HyperIoTMethodInterceptor is a method which can be invoked before and/or after an HyperIoT service method invocation.
 * It can be used to implement custom annotation for pre-processing or post-processing execution before methods are invoked inside *Api or *SystemApi classes.
 * NOTE: this capability works only on HyperIoTServices methods (no rest, you can use filters for that layer, no persistence)
 */
public interface HyperIoTMethodInterceptor<A extends Annotation> {

}
