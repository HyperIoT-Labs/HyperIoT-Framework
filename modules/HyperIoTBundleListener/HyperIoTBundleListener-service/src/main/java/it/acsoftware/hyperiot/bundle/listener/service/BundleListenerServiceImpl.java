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

package it.acsoftware.hyperiot.bundle.listener.service;

import java.util.ArrayList;


import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import it.acsoftware.hyperiot.bundle.listener.api.BundleListenerSystemApi;
import it.acsoftware.hyperiot.bundle.listener.api.BundleListenerApi;

import  it.acsoftware.hyperiot.base.service.HyperIoTBaseServiceImpl;
import it.acsoftware.hyperiot.bundle.listener.model.BundleTrackerItem;


/**
 *
 * @author Aristide Cittadino Implementation class of BundleListenerApi interface.
 *         It is used to implement all additional methods in order to interact with the system layer.
 */
@Component(service = BundleListenerApi.class, immediate = true)
public final class BundleListenerServiceImpl extends  HyperIoTBaseServiceImpl  implements BundleListenerApi {
	/**
	 * Injecting the BundleListenerSystemApi
	 */
	private BundleListenerSystemApi systemService;

	/**
	 *
	 * @return The current BundleListenerSystemApi
	 */
	protected BundleListenerSystemApi getSystemService() {
        getLog().debug( "invoking getSystemService, returning: {}" , this.systemService);
		return systemService;
	}

	/**
	 *
	 * @param bundleListenerSystemService Injecting via OSGi DS current systemService
	 */
	@Reference
	protected void setSystemService(BundleListenerSystemApi bundleListenerSystemService) {
        getLog().debug( "invoking setSystemService, setting: {}" , systemService);
		this.systemService = bundleListenerSystemService ;
	}

	@Override
	public ArrayList<BundleTrackerItem> list() {
		return this.systemService.list();
	}

	@Override
	public BundleTrackerItem get(String symbolicName) {
		return this.systemService.get(symbolicName);
	}
}
