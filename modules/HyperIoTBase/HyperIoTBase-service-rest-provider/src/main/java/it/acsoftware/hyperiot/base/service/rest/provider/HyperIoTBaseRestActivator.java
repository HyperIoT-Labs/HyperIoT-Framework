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
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Hashtable;

/**
 * Registers manually the basic route from hyperiot endpoints
 */
public class HyperIoTBaseRestActivator implements BundleActivator {
    private Logger log = LoggerFactory.getLogger(HyperIoTBaseRestActivator.class.getName());


    @Override
    public void start(BundleContext context) throws Exception {
        log.info("Registering base REST resources at: {}", HyperIoTUtil.getHyperIoTBaseRestContext(context));
        //Registering manually in order to overidde base path
        Dictionary<String, Object> props = new Hashtable<>();
        props.put("service.exported.interfaces", "it.acsoftware.hyperiot.base.service.rest.provider.HyperIoTBaseRestService");
        props.put("service.exported.configs", "org.apache.cxf.rs");
        props.put("org.apache.cxf.rs.address", "/hyperiot");
        props.put("org.apache.cxf.rs.httpservice.context", HyperIoTUtil.getHyperIoTBaseRestContext(context));
        props.put("service.exported.intents", "jackson");
        props.put("service.exported.intents", "jwtAuthFilter");
        HyperIoTBaseRestService instance = new HyperIoTBaseRestService();
        context.registerService(HyperIoTBaseRestService.class.getName(), instance, props);
    }

    @Override
    public void stop(BundleContext context) throws Exception {

    }
}
