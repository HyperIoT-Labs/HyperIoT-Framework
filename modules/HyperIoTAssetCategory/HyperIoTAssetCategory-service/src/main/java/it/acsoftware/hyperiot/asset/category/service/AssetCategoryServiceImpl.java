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

package it.acsoftware.hyperiot.asset.category.service;



import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import it.acsoftware.hyperiot.asset.category.api.AssetCategoryApi;
import it.acsoftware.hyperiot.asset.category.api.AssetCategorySystemApi;
import it.acsoftware.hyperiot.asset.category.model.AssetCategory;
import it.acsoftware.hyperiot.base.service.entity.HyperIoTBaseEntityServiceImpl;

/**
 *
 * @author Aristide Cittadino Implementation class of AssetCategoryApi
 *         interface. It is used to implement all additional methods in order to
 *         interact with the system layer.
 */
@Component(service = AssetCategoryApi.class, immediate = true)
public final class AssetCategoryServiceImpl extends HyperIoTBaseEntityServiceImpl<AssetCategory>
		implements AssetCategoryApi {
	/**
	 * Injecting the AssetCategorySystemApi
	 */
	private AssetCategorySystemApi systemService;

	/**
	 * Constructor for a AssetCategoryServiceImpl
	 */
	public AssetCategoryServiceImpl() {
		super(AssetCategory.class);
	}

	/**
	 *
	 * @return The current AssetCategorySystemApi
	 */
	protected AssetCategorySystemApi getSystemService() {
		getLog().debug( "invoking getSystemService, returning: {}" , this.systemService);
		return systemService;
	}

	/**
	 *
	 * @param assetCategorySystemService Injecting via OSGi DS current systemService
	 */
	@Reference
	protected void setSystemService(AssetCategorySystemApi assetCategorySystemService) {
        getLog().debug( "invoking setSystemService, setting: {}" , systemService);
		this.systemService = assetCategorySystemService;
	}

}
