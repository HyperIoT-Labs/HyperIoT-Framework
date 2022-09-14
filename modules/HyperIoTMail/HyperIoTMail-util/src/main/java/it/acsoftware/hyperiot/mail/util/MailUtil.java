package it.acsoftware.hyperiot.mail.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import it.acsoftware.hyperiot.base.util.HyperIoTConstants;
import it.acsoftware.hyperiot.base.util.HyperIoTUtil;

/**
 * @author Aristide Cittadino Utility class which load mail property from cfg
 * file
 */
public class MailUtil {
    private static Logger log = LoggerFactory.getLogger(MailUtil.class);
    private static Properties props;

    /**
     * @return all mail properties
     */
    public static Properties getMailProperties() {
        if (props == null) {
            BundleContext context = HyperIoTUtil.getBundleContext(MailUtil.class);
            ServiceReference<?> configurationAdminReference = context
                .getServiceReference(ConfigurationAdmin.class.getName());

            if (configurationAdminReference != null) {
                ConfigurationAdmin confAdmin = (ConfigurationAdmin) context
                    .getService(configurationAdminReference);
                try {
                    Configuration configuration = confAdmin
                        .getConfiguration(HyperIoTConstants.HYPERIOT_MAIL_CONFIG_FILE_NAME);
                    if (configuration != null && configuration.getProperties() != null) {
                        Dictionary<String, Object> dict = configuration.getProperties();
                        List<String> keys = Collections.list(dict.keys());
                        Map<String, Object> dictCopy = keys.stream().collect(Collectors.toMap(Function.identity(), dict::get));
                        props = new Properties();
                        props.putAll(dictCopy);
                        log.debug("Loaded properties For HyperIoT: " + props);
                        return props;
                    }
                } catch (IOException e) {
                    log.error(
                        "Impossible to find it.acsoftware.hyperiot.cfg, please create it!", e);
                    return null;
                }
            }
            log.error(
                "Impossible to find it.acsoftware.hyperiot.cfg, please create it!");
            return null;
        }
        return props;
    }

    /**
     * @return SMTP hostname
     */
    public static String getSmtpHostname() {
        return getMailProperties().getProperty(MailConstants.MAIL_SMTP_HOST);
    }

    /**
     * @return SMTP username
     */
    public static String getUsername() {
        return getMailProperties().getProperty(MailConstants.MAIL_USERNAME);
    }

    /**
     * @return SMTP Auth
     */
    public static boolean getSmtpAuth() {
        return Boolean.parseBoolean(getMailProperties().getProperty(MailConstants.MAIL_SMTP_AUTH));
    }

    /**
     * @return Start TTLS Enabled
     */
    public static boolean getStartTTLSEnabled() {
        return Boolean.parseBoolean(getMailProperties().getProperty(MailConstants.MAIL_SMTP_START_TTLS_ENABLED));
    }

    /**
     * @return SMTP password
     */
    public static String getPassword() {
        return getMailProperties().getProperty(MailConstants.MAIL_PASSWORD);
    }

    /**
     * @return SMTP port
     */
    public static int getSmtpPort() {
        return Integer.parseInt(getMailProperties().getProperty(MailConstants.MAIL_SMTP_PORT));
    }

    /**
     * @return test recipients list
     */
    public static List<String> getTestRecipients() {
        return Arrays.asList(getMailProperties()
            .getProperty(MailConstants.MAIL_SMTP_TEST_RECIPIENTS).split(","));
    }

}
