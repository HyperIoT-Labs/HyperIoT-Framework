package it.acsoftware.hyperiot.base.api.proxy;

import it.acsoftware.hyperiot.base.api.HyperIoTService;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Author Aristide Cittadino
 * @param <A> Annotation
 * Used for defining annotation for pre-processing execution after a method invocation
 */
public interface HyperIoTBeforeMethodInterceptor<A extends Annotation> extends HyperIoTMethodInterceptor<A>{
    /**
     *
     * @param destination HyperIoT Service which is going to be invoked
     * @param m Method
     * @param args Method arguments
     * @param annotation Annotation processed on the method which maps the Interceptor definition
     * @param <S> HyperIoT Service Type
     */
    <S extends HyperIoTService> void interceptMethod(S destination, Method m, Object[] args, A annotation);
}
