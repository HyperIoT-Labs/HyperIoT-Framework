package it.acsoftware.hyperiot.asset.category.api;

import it.acsoftware.hyperiot.asset.category.model.AssetCategory;
import it.acsoftware.hyperiot.asset.category.model.AssetCategoryResource;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTBaseRepository;

/**
 * 
 * @author Aristide Cittadino Interface component for AssetCategory Repository.
 *         It is used for CRUD operations, and to interact with the persistence
 *         layer.
 *
 */
public interface AssetCategoryRepository extends HyperIoTBaseRepository<AssetCategory> {
	public AssetCategoryResource findAssetCategoryResource(String resourceName, long resourceId,
			long categoryId);
}
