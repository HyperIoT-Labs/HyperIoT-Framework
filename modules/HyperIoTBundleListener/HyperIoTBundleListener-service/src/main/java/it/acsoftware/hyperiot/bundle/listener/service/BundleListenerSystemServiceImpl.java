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

package it.acsoftware.hyperiot.bundle.listener.service;

import it.acsoftware.hyperiot.base.util.HyperIoTUtil;
import it.acsoftware.hyperiot.bundle.listener.model.BundleTrackerItem;
import org.osgi.service.component.annotations.Component;
import it.acsoftware.hyperiot.bundle.listener.api.BundleListenerSystemApi;

import  it.acsoftware.hyperiot.base.service.HyperIoTBaseSystemServiceImpl ;

import java.util.ArrayList;

/**
 * 
 * @author Aristide Cittadino Implementation class of the BundleListenerSystemApi
 *         interface. This  class is used to implements all additional
 *         methods to interact with the persistence layer.
 */
@Component(service = BundleListenerSystemApi.class, immediate = true)
public final class BundleListenerSystemServiceImpl extends HyperIoTBaseSystemServiceImpl   implements BundleListenerSystemApi {
    private BundleTracker bundleListenerBundleTracker;

    public BundleListenerSystemServiceImpl() throws Exception {
        super();
        this.bundleListenerBundleTracker = new BundleTracker();
        this.bundleListenerBundleTracker.start(HyperIoTUtil.getBundleContext(this.getClass()));
    }

    @Override
    public ArrayList<BundleTrackerItem> list() {
        return this.bundleListenerBundleTracker.list();
    }
    @Override
    public BundleTrackerItem get(String symbolicName) {
        return this.bundleListenerBundleTracker.get(symbolicName);
    }
}
