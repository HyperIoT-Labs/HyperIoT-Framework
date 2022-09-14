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
