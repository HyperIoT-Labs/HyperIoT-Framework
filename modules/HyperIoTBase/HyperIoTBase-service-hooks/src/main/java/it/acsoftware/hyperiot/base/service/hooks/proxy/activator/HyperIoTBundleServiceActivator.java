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

package it.acsoftware.hyperiot.base.service.hooks.proxy.activator;

import it.acsoftware.hyperiot.base.service.hooks.proxy.HyperIoTServiceHooks;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.hooks.service.EventListenerHook;
import org.osgi.framework.hooks.service.FindHook;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author Aristide Cittadino
 * Bundle Activator for register hook in order to provide proxy service for HyperIoT Services
 */
public class HyperIoTBundleServiceActivator implements BundleActivator {
    private static Logger log = LoggerFactory.getLogger(HyperIoTBundleServiceActivator.class.getName());

    @Override
    public void start(BundleContext context) throws Exception {
        log.debug( "Registering Proxy hooks for HyperIoT Services ");
        try {
            context.registerService(new String[]{FindHook.class.getName(), EventListenerHook.class.getName()},
                new HyperIoTServiceHooks(context), null);
        } catch (Throwable t) {
            log.error( t.getMessage(), t);
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        log.debug( "Stopping bundle  for hyperiot services");
    }
}
