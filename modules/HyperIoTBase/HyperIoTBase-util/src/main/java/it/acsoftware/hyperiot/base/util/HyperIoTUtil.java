/*
 * Copyright 2019-2023 HyperIoT
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

package it.acsoftware.hyperiot.base.util;

import it.acsoftware.hyperiot.base.api.HyperIoTPostAction;
import it.acsoftware.hyperiot.base.api.HyperIoTPreAction;
import it.acsoftware.hyperiot.base.api.HyperIoTResource;
import it.acsoftware.hyperiot.osgi.util.filter.OSGiFilter;
import it.acsoftware.hyperiot.osgi.util.filter.OSGiFilterBuilder;
import org.apache.commons.lang3.ClassUtils;
import org.osgi.framework.*;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Aristide Cittadino Model class for HyperIoTUtil. It is a utility
 * class and implements the method to obtain a password encoded with the
 * MD5 algorithm.
 */
public final class HyperIoTUtil {
    private static Logger log = LoggerFactory.getLogger(HyperIoTUtil.class.getName());

    private static final PasswordEncoder encoder = new BCryptPasswordEncoder();
    private static Properties props;
    private static EntityManagerFactory emf;

    private BundleContext bundleContext;

    public static Properties getHyperIoTProperties(BundleContext context) {
        if (props == null) {
            ServiceReference<?> configurationAdminReference = context.getServiceReference(ConfigurationAdmin.class.getName());

            if (configurationAdminReference != null) {
                ConfigurationAdmin confAdmin = (ConfigurationAdmin) context.getService(configurationAdminReference);
                try {
                    Configuration configuration = confAdmin.getConfiguration("it.acsoftware.hyperiot");
                    if (configuration != null && configuration.getProperties() != null) {
                        Dictionary<String, Object> dict = configuration.getProperties();
                        List<String> keys = Collections.list(dict.keys());
                        Map<String, Object> dictCopy = keys.stream().collect(Collectors.toMap(Function.identity(), dict::get));
                        props = new Properties();
                        props.putAll(dictCopy);
                        log.debug("Loaded properties For HyperIoT: {}", props);
                        return props;
                    }
                } catch (IOException e) {
                    log.error("Impossible to find it.acsoftware.hyperiot.cfg, please create it!", e);
                    return null;
                }
            }
            log.error("Impossible to find it.acsoftware.hyperiot.cfg, please create it!");
            return null;
        }
        return props;
    }

    public static Object getHyperIoTProperty(String name) {
        return getHyperIoTProperties(getBundleContext(HyperIoTUtil.class)).getProperty(name);
    }

    public static Object getHyperIoTProperty(String name, String defaultValue) {
        return getHyperIoTProperties(getBundleContext(HyperIoTUtil.class)).getProperty(name, defaultValue);
    }

    /**
     * @return true if test mode is enabled inside the it.acsoftware.hyperiot.cfg
     * file
     */
    public static boolean isInTestMode() {
        if (props == null) HyperIoTUtil.getHyperIoTProperties(getBundleContext(HyperIoTUtil.class));

        return Boolean.parseBoolean(props.getProperty(HyperIoTConstants.HYPERIOT_PROPERTY_TEST_MODE, "false"));
    }

    /**
     * @return nodeID of this current node, if property is not set returns a random
     * UUID
     */
    public static String getNodeId() {
        if (props == null) HyperIoTUtil.getHyperIoTProperties(getBundleContext(HyperIoTUtil.class));

        return props.getProperty(HyperIoTConstants.HYPERIOT_PROPERTY_NODE_ID, UUID.randomUUID().toString());
    }

    /**
     * @return layer of this current node, if property is not set returns
     * "undefined_layer"
     */
    public static String getLayer() {
        if (props == null) HyperIoTUtil.getHyperIoTProperties(getBundleContext(HyperIoTUtil.class));

        return props.getProperty(HyperIoTConstants.HYPERIOT_PROPERTY_LAYER, "undefined_layer");
    }

    /**
     * @param password parameter that indicates the password to access the platform
     * @return password encoded with the BCrypt algorithm
     */
    public static String getPasswordHash(String password) {
        try {
            log.debug("Hashing password....");
            return Base64.getEncoder().encodeToString(MessageDigest.getInstance("MD5").digest(password.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            log.error(e.getMessage(), e);
            return Base64.getEncoder().encodeToString(password.getBytes());
        }
    }

    /**
     * @param rawPassword     parameter that represent raw version of the password
     * @param encodedPassword parameter that represent encoded version of the password
     * @return true when encodedPassword is encoded version of rawPassword
     */
    public static boolean passwordMatches(String rawPassword, String encodedPassword) {
        return getPasswordHash(rawPassword).equals(encodedPassword);
    }

    public static String encodeRawString(String rawString) {
        return encoder.encode(rawString);
    }

    public static boolean matchesEncoding(String rawString, String encodedString) {
        return encoder.matches(rawString, encodedString);
    }

    /**
     * @return layer of this current node, if property is not set returns
     * "localhost:8080"
     */
    public static String getHyperIoTUrl() {
        if (props == null) HyperIoTUtil.getHyperIoTProperties(getBundleContext(HyperIoTUtil.class));

        return props.getProperty(HyperIoTConstants.HYPERIOT_PROPERTY_SERVICES_URL, "localhost:8181");
    }

    /**
     * @return
     */
    public static String getActivateAccountUrl() {
        if (props == null) HyperIoTUtil.getHyperIoTProperties(getBundleContext(HyperIoTUtil.class));
        return props.getProperty(HyperIoTConstants.HYPERIOT_PROPERTY_ACTIVATE_ACCOUNT_URL, "localhost:4200/auth/activation");
    }

    public static boolean isAccountActivationEnabled() {
        if (props == null) HyperIoTUtil.getHyperIoTProperties(getBundleContext(HyperIoTUtil.class));
        //in test mode activation is always enabled
        return isInTestMode() || Boolean.parseBoolean(props.getProperty(HyperIoTConstants.HYPERIOT_PROPERTY_ACCOUNT_ACTIVATION_ENABLED, "false"));
    }

    /**
     * @return
     */
    public static String getFrontEndUrl() {
        if (props == null) HyperIoTUtil.getHyperIoTProperties(getBundleContext(HyperIoTUtil.class));

        return props.getProperty(HyperIoTConstants.HYPERIOT_PROPERTY_FRONTEND_URL, "localhost:4200");
    }

    /**
     * @return
     */
    public static String getPasswordResetUrl() {
        if (props == null) HyperIoTUtil.getHyperIoTProperties(getBundleContext(HyperIoTUtil.class));

        return props.getProperty(HyperIoTConstants.HYPERIOT_PROPERTY_PASSWORD_RESET_URL, "localhost:4200/auth/password-reset");
    }

    public static int getWebSocketOnOpenDispatchThreads(int defaultThreadNumber) {
        if (props == null) HyperIoTUtil.getHyperIoTProperties(getBundleContext(HyperIoTUtil.class));
        return Integer.parseInt(props.getProperty(HyperIoTConstants.HYPERIOT_PROPERTY_WEB_SOCKER_SERVICE_ON_OPEN_DISPATCH_THREADS, String.valueOf(defaultThreadNumber)));
    }

    public static int getWebSocketOnCloseDispatchThreads(int defaultThreadNumber) {
        if (props == null) HyperIoTUtil.getHyperIoTProperties(getBundleContext(HyperIoTUtil.class));
        return Integer.parseInt(props.getProperty(HyperIoTConstants.HYPERIOT_PROPERTY_WEB_SOCKER_SERVICE_ON_CLOSE_DISPATCH_THREADS, String.valueOf(defaultThreadNumber)));
    }

    public static int getWebSocketOnMessageDispatchThreads(int defaultThreadNumber) {
        if (props == null) HyperIoTUtil.getHyperIoTProperties(getBundleContext(HyperIoTUtil.class));
        return Integer.parseInt(props.getProperty(HyperIoTConstants.HYPERIOT_PROPERTY_WEB_SOCKER_SERVICE_ON_MESSAGE_DISPATCH_THREADS, String.valueOf(defaultThreadNumber)));
    }

    public static int getWebSocketOnErrorDispatchThreads(int defaultThreadNumber) {
        if (props == null) HyperIoTUtil.getHyperIoTProperties(getBundleContext(HyperIoTUtil.class));
        return Integer.parseInt(props.getProperty(HyperIoTConstants.HYPERIOT_PROPERTY_WEB_SOCKER_SERVICE_ON_ERROR_DISPATCH_THREADS, String.valueOf(defaultThreadNumber)));
    }

    /**
     * @return base rest context of rest services, default is: /hyperiot
     */
    public static String getHyperIoTBaseRestContext(BundleContext context) {
        if (props == null) HyperIoTUtil.getHyperIoTProperties(context);
        String baseRestUrl = props.getProperty(HyperIoTConstants.HYPERIOT_PROPERTY_BASE_REST_CONTEXT, "/hyperiot");
        if (!baseRestUrl.startsWith("/")) baseRestUrl = "/" + baseRestUrl;
        return baseRestUrl;
    }


    /**
     * Returns the BundleContext of the bundle which contains this component.
     *
     * @param instance parameter that indicates the instance contained by the bundle
     * @return The BundleContext of the bundle
     */
    public static BundleContext getBundleContext(Object instance) {
        BundleContext bundleContext = FrameworkUtil.getBundle(instance.getClass()).getBundleContext();
        return bundleContext;
    }

    /**
     * Returns the BundleContext of the bundle which contains this component.
     *
     * @param className parameter that indicates the class name of the bundle
     * @return The BundleContext of the bundle
     */
    public static BundleContext getBundleContext(Class<?> className) {
        BundleContext bundleContext = FrameworkUtil.getBundle(className).getBundleContext();
        return bundleContext;
    }

    /**
     * Return entity manager factory for a persistence unit name
     *
     * @param persistenceUnitName name of the persistence unit
     * @return
     */
    private EntityManagerFactory getEntityManagerFactory(String persistenceUnitName) {
        if (emf == null) {
            Bundle thisBundle = FrameworkUtil.getBundle(this.getClass());

            BundleContext context = thisBundle.getBundleContext();

            ServiceReference serviceReference = context.getServiceReference(PersistenceProvider.class.getName());
            PersistenceProvider persistenceProvider = (PersistenceProvider) context.getService(serviceReference);

            emf = persistenceProvider.createEntityManagerFactory(persistenceUnitName, null);
        }
        return emf;
    }

    /**
     * @param c Class or Interface of the needed service
     * @return Service instance
     */
    public static Object getService(Class<?> c) {
        BundleContext ctx = getBundleContext(c);
        ServiceReference<?> sr = ctx.getServiceReference(c);
        if (sr != null) return ctx.getService(sr);
        return null;
    }

    /**
     * @param c      Class
     * @param filter OSGi filter
     * @return array of references
     */
    public static ServiceReference[] getServices(Class<?> c, String filter) {
        BundleContext ctx = getBundleContext(c);
        ServiceReference<?> sr = ctx.getServiceReference(c);
        try {
            return ctx.getServiceReferences(c.getName(), filter);
        } catch (InvalidSyntaxException e) {
            log.error(e.getMessage(), e);
        }
        return new ServiceReference[]{};
    }

    /**
     * Execute post actions after another one (save, update or delete on entities)
     *
     * @param resource   Entity on which the first action was executed
     * @param postAction HyperIoTPostAction Post action instance
     */
    @SuppressWarnings("unchecked")
    public static void invokePostActions(HyperIoTResource resource, Class<? extends HyperIoTPostAction> postAction) {
        log.debug("Fetch post actions of type: {}", postAction);

        OSGiFilter osgiFilter = OSGiFilterBuilder.createFilter("type", resource.getClass().getName());

        //include OSGi filters for all interfaces implemented by resource and its superclasses
        String filter = ClassUtils.getAllInterfaces(resource.getClass()).stream().map(interfaceClass -> OSGiFilterBuilder.createFilter("type", interfaceClass.getName())).reduce(osgiFilter, OSGiFilter::or).getFilter();

        ServiceReference<? extends HyperIoTPostAction>[] serviceReferences = HyperIoTUtil.getServices(postAction, filter);
        if (serviceReferences == null) log.debug("There are not post actions of type {}", postAction);
        else {
            log.debug("{} post actions fetched", serviceReferences.length);
            for (ServiceReference<? extends HyperIoTPostAction> serviceReference : serviceReferences)
                try {
                    log.debug("Executing post action: {}", serviceReference);
                    HyperIoTPostAction hyperIoTPostAction = HyperIoTUtil.getBundleContext(postAction).getService(serviceReference);
                    hyperIoTPostAction.execute(resource);
                } catch (Throwable e) {
                    log.error(e.getMessage(), e);
                }
        }
    }

    /**
     * Execute post actions after another one (save, update or delete on entities)
     *
     * @param resource  Entity on which the first action was executed
     * @param preAction HyperIoTPostAction Post action instance
     */
    @SuppressWarnings("unchecked")
    public static void invokePreActions(HyperIoTResource resource, Class<? extends HyperIoTPreAction> preAction) {
        log.debug("Fetch pre actions of type: {}", preAction);

        OSGiFilter osgiFilter = OSGiFilterBuilder.createFilter("type", resource.getClass().getName());

        //include OSGi filters for all interfaces implemented by resource and its superclasses
        String filter = ClassUtils.getAllInterfaces(resource.getClass()).stream().map(interfaceClass -> OSGiFilterBuilder.createFilter("type", interfaceClass.getName())).reduce(osgiFilter, OSGiFilter::or).getFilter();

        ServiceReference<? extends HyperIoTPreAction>[] serviceReferences = HyperIoTUtil.getServices(preAction, filter);
        if (serviceReferences == null) log.debug("There are not pre actions of type {}", preAction);
        else {
            log.debug("{} pre actions fetched", serviceReferences.length);
            for (ServiceReference<? extends HyperIoTPreAction> serviceReference : serviceReferences) {
                //not managing exception explicitly so it can make the transaction fail if needed
                log.debug("Executing pre action: {}", serviceReference);
                HyperIoTPreAction hyperIoTPreAction = HyperIoTUtil.getBundleContext(preAction).getService(serviceReference);
                hyperIoTPreAction.execute(resource);
            }

        }
    }

}
