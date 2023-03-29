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

package it.acsoftware.hyperiot.contentrepository.util;

import it.acsoftware.hyperiot.base.util.HyperIoTUtil;
import it.acsoftware.hyperiot.contentrepository.api.ContentRepositoryUtil;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component(service = ContentRepositoryUtil.class, immediate = true)
public class ContentRepositoryUtilImpl implements ContentRepositoryUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContentRepositoryUtilImpl.class.getName());

    private Properties properties;

    @Override
    public String getContentRepositoryDefaultUserId() {
        return properties.getProperty(ContentRepositoryConstants.CONTENT_REPOSITORY_DEFAULT_ADMIN_USER_ID);

    }

    @Override
    public String getContentRepositoryDefaultUserPassword() {
        return properties.getProperty(ContentRepositoryConstants.CONTENT_REPOSITORY_DEFAULT_ADMIN_PASSWORD);
    }

    @Override
    public String getContentRepositoryDefaultWorkspaceName() {
        return properties.getProperty(ContentRepositoryConstants.CONTENT_REPOSITORY_DEFAULT_WORKSPACE_NAME);
    }

    @Activate
    private void onActivate(){
        this.loadContentRepositoryConfiguration();
    }

    private void loadContentRepositoryConfiguration(){
        BundleContext context = HyperIoTUtil.getBundleContext(ContentRepositoryUtilImpl.class);
        LOGGER.debug( "Reading ContentRepository Properties from .cfg file");
        ServiceReference<?> configurationAdminReference = context.getServiceReference(ConfigurationAdmin.class.getName());
        if (configurationAdminReference != null) {
            ConfigurationAdmin confAdmin = (ConfigurationAdmin) context.getService(configurationAdminReference);
            try {
                Configuration configuration = confAdmin.getConfiguration(ContentRepositoryConstants.CONTENT_REPOSITORY_CONFIG_FILE_NAME);
                if (configuration != null && configuration.getProperties() != null) {
                    LOGGER.debug( "Reading properties for ContentRepository ....");
                    Dictionary<String, Object> dict = configuration.getProperties();
                    List<String> keys = Collections.list(dict.keys());
                    Map<String, Object> dictCopy = keys.stream().collect(Collectors.toMap(Function.identity(), dict::get));
                    properties = new Properties();
                    properties.putAll(dictCopy);
                } else
                    LOGGER.error( "Impossible to find Configuration admin reference, ContentRepository won't start!");
            } catch (IOException e) {
                LOGGER.error( "Impossible to find it.acsoftware.hyperiot.content.repository.cfg file, please create it!", e);
            }
        }
    }


}
