/*
 * Copyright 2019-2023 ACSoftware
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package it.acsoftware.hyperiot.base.service.hooks.proxy;

import it.acsoftware.hyperiot.base.api.HyperIoTService;
import org.osgi.framework.*;
import org.osgi.framework.hooks.service.EventListenerHook;
import org.osgi.framework.hooks.service.FindHook;
import org.osgi.framework.hooks.service.ListenerHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;
import java.util.*;

import static org.osgi.framework.ServiceEvent.*;

/**
 * @Author Aristide Cittadino
 * This Hook creates a proxy instance for every HyperIoTBaseService.
 * Then real implementation are obfuscated by these hooks.
 * Proxy instances are keept in sync with original ones with EventListenerHook
 */
public class HyperIoTServiceHooks implements EventListenerHook, FindHook {
    private static Logger log = LoggerFactory.getLogger(HyperIoTServiceHooks.class.getName());

    private BundleContext bc;
    private static final String PROXY = "it.acsoftware.hyperiot.base.service.proxy";

    public HyperIoTServiceHooks(BundleContext bc) {
        this.bc = bc;
        //getting already registered bundles
        this.loadAlreadyRegisteredServices(bc.getBundles());
    }

    /**
     * @param alreadyRegistereSerivces already registered bundle before this hook is started
     * @Aristide Cittadino
     * It can be possible that some HyperIoTService are loaded before this hook is loaded ex. HyperIoTAuthenticationSystemApi
     * This method is invoked at Hook startup and analyze already registered bundles , searching for HyperIoTService to proxy
     */
    private void loadAlreadyRegisteredServices(Bundle[] alreadyRegistereSerivces) {
        for (Bundle b : alreadyRegistereSerivces) {
            ServiceReference<?>[] references = b.getRegisteredServices();
            if (references != null) {
                for (ServiceReference serviceReference : references) {
                    String[] propertyKeys = serviceReference.getPropertyKeys();
                    Properties properties = buildProps(propertyKeys, serviceReference);
                    Bundle bundle = serviceReference.getBundle();
                    if (isHyperIoTService(serviceReference)) {
                        registerProxyService(bundle,
                                serviceReference,
                                properties,
                                this.getClass().getClassLoader(), new HyperIoTServiceProxy((HyperIoTService) bundle.getBundleContext().getService(serviceReference)));
                    }
                }
            }
        }
    }

    /**
     * Author Aristide Cittadino
     * This method is overridden in order to obfuscate HyperIoT Service which are not "proxy".
     * The goal is to mask real implementation in OSGi and expose only proxied services.
     * With this approach each HyperIoT Service can be wrapped by proxy and it can be customized
     * with HyperIoTInterceptors
     *
     * @param bc          Bundle Context
     * @param name        Component Name
     * @param filter      OSGi filter
     * @param allServices boolean to catch all services
     * @param references  found references
     */
    @Override
    public void find(BundleContext bc, String name, String filter,
                     boolean allServices, Collection references) {
        try {
            Iterator iterator = references.iterator();
            while (iterator.hasNext()) {
                ServiceReference sr = (ServiceReference) iterator.next();
                if (isHyperIoTService(sr)) {
                    iterator.remove();
                }
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    /**
     * @param event
     * @param listeners
     * @Author Aristide Cittadino
     * This method is overridden in order to intercept HyperIoT Service Component declaration and to register automatically proxied versions of that services.
     */
    @Override
    public void event(ServiceEvent event, Map<BundleContext, Collection<ListenerHook.ListenerInfo>> listeners) {
        try {
            final ServiceReference serviceReference = event.getServiceReference();
            String[] propertyKeys = serviceReference.getPropertyKeys();
            Properties properties = buildProps(propertyKeys, serviceReference);
            Bundle bundle = serviceReference.getBundle();
            if (isHyperIoTService(serviceReference)) {
                //no bundle will receive updates from this one because it's not the proxied one
                listeners.clear();
                switch (event.getType()) {
                    case REGISTERED: {
                        HyperIoTService service = (HyperIoTService) bundle.getBundleContext().getService(serviceReference);
                        if (service != null) {
                            registerProxyService(bundle,
                                    serviceReference,
                                    properties,
                                    this.getClass().getClassLoader(), new HyperIoTServiceProxy(service));
                        }
                        break;
                    }
                    case UNREGISTERING: {
                        unregisterProxyService(bundle, serviceReference.getClass(), properties);
                        break;
                    }
                    case MODIFIED:
                    case MODIFIED_ENDMATCH: {
                        HyperIoTService s = (HyperIoTService) bundle.getBundleContext().getService(serviceReference);
                        unregisterProxyService(bundle, serviceReference.getClass(), properties);
                        if (s != null) {
                            registerProxyService(bundle,
                                    serviceReference,
                                    properties,
                                    this.getClass().getClassLoader(), new HyperIoTServiceProxy(s));
                        }
                        break;
                    }
                }
            }
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
        }
    }

    /**
     * @param sr
     * @return true if the service reference represents an HyperIoTSerivce
     */
    private boolean isHyperIoTService(ServiceReference sr) {
        String[] interfaces = (String[]) sr.getProperty(
                "objectClass");
        boolean isProxy = sr.getProperty(PROXY) != null;
        for (String clazz : interfaces) {
            try {
                Class<?> c = Class.forName(clazz);
                boolean isHytService = HyperIoTService.class.isAssignableFrom(c);
                if (isHytService) {
                    return !isProxy;
                }
            } catch (Throwable ex) {
                log.debug(" class definition {} not found, because: {}, skipping HYT proxy for this service", new Object[]{clazz, ex.getMessage()});
            }
        }
        return false;
    }

    /**
     * Copies all the pros of a service
     *
     * @param propertyKeys
     * @param sr
     * @return
     */
    private Properties buildProps(String[] propertyKeys, ServiceReference sr) {
        Properties properties = new Properties();
        for (String string : propertyKeys) {
            properties.put(string,
                    sr.getProperty(string));
        }
        return properties;
    }

    /**
     * Convers string classes array to array of string
     *
     * @param interfaces
     * @return
     */
    private static String[] toString(Class<?>[] interfaces) {
        String[] names = new String[interfaces.length];
        int i = 0;
        for (Class clazz : interfaces) {
            names[i++] = clazz.getName();
        }
        return names;
    }

    /**
     * Converts array of string, representing class names, to array of classes objects
     *
     * @param interfaces
     * @param bl
     * @return
     */
    private static Class<?>[] toClass(String[] interfaces, Bundle bl) {
        List<Class<?>> names = new ArrayList<>();

        for (String clazz : interfaces) {
            try {
                Class<?> classObj = bl.loadClass(clazz);
                if (classObj != null)
                    names.add(classObj);
            } catch (ClassNotFoundException ex) {
                log.debug("Class {} not found in Bundle {}", new Object[]{clazz, bl.getSymbolicName()});
            }
        }
        Class<?>[] returnNames = new Class<?>[names.size()];
        return names.toArray(returnNames);
    }

    /**
     * Register the proxied version of an HyperIoTService
     *
     * @param bundleSource
     * @param serviceReference
     * @param prop
     * @param cl
     * @param proxy
     */
    private void registerProxyService(Bundle bundleSource,
                                      ServiceReference serviceReference, Dictionary prop, ClassLoader cl,
                                      HyperIoTServiceProxy proxy) {
        try {
            String[] interfaces = (String[]) serviceReference.getProperty(
                    "objectClass");
            Class<?>[] toClass = toClass(interfaces, bundleSource);
            prop.put(PROXY, true);
            Object hyperIoTServiceProxy = Proxy.newProxyInstance(
                    cl, toClass,
                    proxy);
            ServiceRegistration registration = bundleSource.getBundleContext().registerService(toString(toClass), hyperIoTServiceProxy, prop);
            proxy.setRegistration(registration);
        } catch (NoClassDefFoundError e) {
            log.error("NoClassDefFoundError by {} in source bundle {}", cl, bundleSource, e);
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
        }
    }

    /**
     * Unregister the proxied version of an HyperIoTService
     *
     * @param bundleSource
     * @param serviceClass
     * @param prop
     * @param <S>
     */
    private <S extends ServiceReference> void unregisterProxyService(Bundle bundleSource, Class<S> serviceClass, Dictionary prop) {
        try {
            Collection<ServiceReference<S>> references = bundleSource.getBundleContext().getServiceReferences(serviceClass, "(" + PROXY + "=true)");
            references.stream().forEach(ref -> {
                HyperIoTServiceProxy proxy = (HyperIoTServiceProxy) bundleSource.getBundleContext().getService(ref);
                proxy.getRegistration().unregister();
                log.debug("Unregistering {} as HyperIoTService ", new Object[]{proxy.getRegistration().getReference().getBundle().getSymbolicName()});
            });
        } catch (InvalidSyntaxException e) {
            log.error(e.getMessage(), e);
        }
    }

}
