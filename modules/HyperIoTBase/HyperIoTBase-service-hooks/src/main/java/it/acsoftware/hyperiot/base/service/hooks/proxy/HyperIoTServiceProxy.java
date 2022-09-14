package it.acsoftware.hyperiot.base.service.hooks.proxy;

import it.acsoftware.hyperiot.base.api.HyperIoTService;
import it.acsoftware.hyperiot.base.api.proxy.HyperIoTAfterMethodInterceptor;
import it.acsoftware.hyperiot.base.api.proxy.HyperIoTBeforeMethodInterceptor;
import it.acsoftware.hyperiot.base.api.proxy.HyperIoTInterceptorExecutor;
import it.acsoftware.hyperiot.base.api.proxy.HyperIoTMethodInterceptor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @Author Aristide Cittadino
 * Implementation of HyperIoTService proxy.
 * This class will wrap every HyperIoTService. It implements the logic to intercept methods invocation.
 * It gives the possibility to the developer to customize pre-invocation or post-invocation logic on every HyperIoTService.
 */
public class HyperIoTServiceProxy<S extends HyperIoTService> implements InvocationHandler, Serializable {
    private static Logger log = LoggerFactory.getLogger(HyperIoTServiceProxy.class.getName());
    private S service;
    private ServiceRegistration<S> registration;

    public HyperIoTServiceProxy(S service) {
        this.service = service;
    }

    public ServiceRegistration<S> getRegistration() {
        return registration;
    }

    public void setRegistration(ServiceRegistration<S> registration) {
        this.registration = registration;
    }

    /**
     * Each invocation is wrapped between "intercept before" and "intercept after".
     *
     * @param proxy
     * @param method
     * @param args
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            executeInterceptorBeforeMethod(proxy, method, args);
            Object invoke = method.invoke(service, args);
            executeInterceptorAfterMethod(proxy, method, args, invoke);
            return invoke;
        } catch (IllegalAccessException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("Illegal access from proxy " + e.getMessage());
        } catch (InvocationTargetException e) {
            log.error("Invocation on proxy failed, please check exceptions!");
            throw e.getTargetException();
        } catch (NoSuchMethodException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("No Such method " + e.getMessage());
        }
    }

    /**
     * Loads all HyperIoTBeforeMethodInterceptor Components registered as OSGi services and execute them before method invocation
     *
     * @param proxy
     * @param method
     * @param args
     * @throws NoSuchMethodException
     */
    private void executeInterceptorBeforeMethod(Object proxy, Method method, Object[] args) throws NoSuchMethodException {
        this.executeInterceptor(proxy, method, args, null, HyperIoTBeforeMethodInterceptor.class);
    }

    /**
     * Loads all HyperIoTAfterMethodInterceptor Components registered as OSGi services and execute them after method invocation
     *
     * @param proxy
     * @param method
     * @param args
     * @param result
     * @throws NoSuchMethodException
     */
    private void executeInterceptorAfterMethod(Object proxy, Method method, Object[] args, Object result) throws NoSuchMethodException {
        this.executeInterceptor(proxy, method, args, result, HyperIoTAfterMethodInterceptor.class);
    }

    /**
     * Analyzes the method invocation searching for HyperIoTInterceptorExecutor Annotation.
     *
     * @param proxy
     * @param method
     * @param args
     * @param result
     * @param interceptorClass
     * @throws NoSuchMethodException
     */
    private void executeInterceptor(Object proxy, Method method, Object[] args, Object result, Class<? extends HyperIoTMethodInterceptor> interceptorClass) throws NoSuchMethodException {
        Annotation[] annotations = service.getClass().getMethod(method.getName(), method.getParameterTypes()).getDeclaredAnnotations();
        for (int i = 0; i < annotations.length; i++) {
            Annotation annotation = annotations[i];
            if (annotation.annotationType().isAnnotationPresent(HyperIoTInterceptorExecutor.class)) {
                HyperIoTInterceptorExecutor interceptorAnnotation = annotation.annotationType().getDeclaredAnnotation(HyperIoTInterceptorExecutor.class);
                Class<? extends HyperIoTMethodInterceptor> executor = interceptorAnnotation.interceptor();
                BundleContext ctx = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
                ServiceReference<? extends HyperIoTMethodInterceptor> methodInterceptorRef = ctx.getServiceReference(executor);
                HyperIoTMethodInterceptor interceptor = ctx.getService(methodInterceptorRef);
                //avoiding calling after method with before methods
                if (interceptorClass.isAssignableFrom(interceptor.getClass())) {
                    if (HyperIoTBeforeMethodInterceptor.class.isAssignableFrom(interceptor.getClass())) {
                        HyperIoTBeforeMethodInterceptor beforeInterceptor = (HyperIoTBeforeMethodInterceptor) interceptor;
                        beforeInterceptor.interceptMethod(service, method, args, annotation);
                    } else if (HyperIoTAfterMethodInterceptor.class.isAssignableFrom(interceptor.getClass())) {
                        HyperIoTAfterMethodInterceptor afterInterceptor = (HyperIoTAfterMethodInterceptor) interceptor;
                        afterInterceptor.interceptMethod(service, method, args, result, annotation);
                    }
                }
            }
        }
    }
}
