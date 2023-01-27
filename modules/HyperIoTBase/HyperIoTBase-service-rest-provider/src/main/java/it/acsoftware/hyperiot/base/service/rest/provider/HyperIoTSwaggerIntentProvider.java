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

package it.acsoftware.hyperiot.base.service.rest.provider;

import it.acsoftware.hyperiot.base.util.HyperIoTUtil;
import org.apache.cxf.dosgi.common.api.IntentsProvider;
import org.apache.cxf.jaxrs.swagger.Swagger2Feature;
import org.osgi.service.component.annotations.Component;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Author Aristide Cittadino
 */
@Component(property = {"org.apache.cxf.dosgi.IntentName=swagger"}, immediate = true)
public class HyperIoTSwaggerIntentProvider implements IntentsProvider {
    private Logger log = LoggerFactory.getLogger(HyperIoTSwaggerIntentProvider.class.getName());
    private Swagger2Feature swagger2Feature = null;
    private List<?> intentList;

    public HyperIoTSwaggerIntentProvider() {
        this.swagger2Feature = createSwaggerFeature();
        this.intentList = Arrays.asList(swagger2Feature);
    }

    @Override
    public List<?> getIntents() {
        return intentList;
    }

    private Swagger2Feature createSwaggerFeature() {
        log.info( "Register HyperIoT Provider Swagger Intent");
        String contextRoot = HyperIoTUtil.getHyperIoTBaseRestContext(HyperIoTUtil.getBundleContext(this));
        Swagger2Feature swagger = new Swagger2Feature();
        swagger.setUsePathBasedConfig(true); // Necessary for OSGi
        swagger.setPrettyPrint(true);
        swagger.setBasePath(contextRoot);
        swagger.setSupportSwaggerUi(true);
        return swagger;
    }
}
