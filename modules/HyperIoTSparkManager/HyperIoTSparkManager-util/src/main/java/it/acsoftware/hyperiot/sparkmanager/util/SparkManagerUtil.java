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

package it.acsoftware.hyperiot.sparkmanager.util;

import it.acsoftware.hyperiot.base.util.HyperIoTUtil;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SparkManagerUtil {

    private static Logger log = LoggerFactory.getLogger(SparkManagerUtil.class.getName());

    private static Properties props;

    private static void loadSparkManagerConfiguration() {
        if (props == null) {
            BundleContext context = HyperIoTUtil.getBundleContext(SparkManagerUtil.class);
            log.debug("Reading SparkManager Properties from .cfg file");
            ServiceReference<?> configurationAdminReference = context.getServiceReference(ConfigurationAdmin.class.getName());
            if (configurationAdminReference != null) {
                ConfigurationAdmin confAdmin = (ConfigurationAdmin) context.getService(configurationAdminReference);
                try {
                    Configuration configuration = confAdmin.getConfiguration(SparkManagerConstants.SPARKMANAGER_CONFIG_FILE_NAME);
                    if (configuration != null && configuration.getProperties() != null) {
                        log.debug("Reading properties for SparkManager ....");
                        Dictionary<String, Object> dict = configuration.getProperties();
                        List<String> keys = Collections.list(dict.keys());
                        Map<String, Object> dictCopy = keys.stream().collect(Collectors.toMap(Function.identity(), dict::get));
                        props = new Properties();
                        props.putAll(dictCopy);
                    } else
                        log.error("Impossible to find Configuration admin reference, SparkManager won't start!");
                } catch (IOException e) {
                    log.error("Impossible to find it.acsoftware.hyperiot.sparkmanager.cfg, please create it!", e);
                }
            }
        }
    }

    public static String getSparkClientVersion() {
        loadSparkManagerConfiguration();
        final String DEFAULT_SPARKMANAGER_PROPERTY_SPARK_CLIENT_VERSION = "2.4.5";
        return props.getProperty(SparkManagerConstants.SPARKMANAGER_PROPERTY_SPARK_CLIENT_VERSION,
                DEFAULT_SPARKMANAGER_PROPERTY_SPARK_CLIENT_VERSION);
    }

    public static boolean getSparkDriverSupervise() {
        loadSparkManagerConfiguration();
        final String DEFAULT_SPARKMANAGER_PROPERTY_SPARK_DRIVER_SUPERVISE = "false";
        return Boolean.parseBoolean(props.getProperty(SparkManagerConstants.SPARKMANAGER_PROPERTY_SPARK_DRIVER_SUPERVISE,
                DEFAULT_SPARKMANAGER_PROPERTY_SPARK_DRIVER_SUPERVISE));
    }

    public static int getSparkEnvLoaded() {
        loadSparkManagerConfiguration();
        final String DEFAULT_SPARKMANAGER_PROPERTY_SPARK_ENV_LOADED = "1";
        return Integer.parseInt(props.getProperty(SparkManagerConstants.SPARKMANAGER_PROPERTY_SPARK_ENV_LOADED,
                DEFAULT_SPARKMANAGER_PROPERTY_SPARK_ENV_LOADED));
    }

    public static String getSparkMasterHostname() {
        loadSparkManagerConfiguration();
        final String DEFAULT_SPARKMANAGER_PROPERTY_SPARK_MASTER_HOSTNAME = "http://spark-master";
        return props.getProperty(SparkManagerConstants.SPARKMANAGER_PROPERTY_SPARK_MASTER_HOSTNAME,
                DEFAULT_SPARKMANAGER_PROPERTY_SPARK_MASTER_HOSTNAME);
    }

    public static int getSparkRestApiPort() {
        loadSparkManagerConfiguration();
        final String DEFAULT_SPARKMANAGER_PROPERTY_SPARK_REST_API_PORT = "6066";
        return Integer.parseInt(props.getProperty(SparkManagerConstants.SPARKMANAGER_PROPERTY_SPARK_REST_API_PORT,
                DEFAULT_SPARKMANAGER_PROPERTY_SPARK_REST_API_PORT));
    }

    public static String getSparkRestApiUrl() {
        loadSparkManagerConfiguration();
        return getSparkMasterHostname() + ":" + getSparkRestApiPort();
    }

    public static String getSparkSubmitDeployMode() {
        loadSparkManagerConfiguration();
        final String DEFAULT_SPARKMANAGER_PROPERTY_SPARK_SUBMIT_DEPLOY_MODE = "cluster";
        return props.getProperty(SparkManagerConstants.SPARKMANAGER_PROPERTY_SPARK_SUBMIT_DEPLOY_MODE,
                DEFAULT_SPARKMANAGER_PROPERTY_SPARK_SUBMIT_DEPLOY_MODE);
    }

}
