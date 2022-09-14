package it.acsoftware.hyperiot.asset.tag.api;

import it.acsoftware.hyperiot.asset.tag.model.AssetTag;
import it.acsoftware.hyperiot.asset.tag.model.AssetTagResource;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTBaseRepository;

import java.util.List;

/**
 * 
 * @author Aristide Cittadino Interface component for AssetTag Repository. It is
 *         used for CRUD operations, and to interact with the persistence layer.
 *
 */
public interface AssetTagRepository extends HyperIoTBaseRepository<AssetTag> {
	public AssetTagResource findAssetTagResource(String resourceName, long resourceId, long tagId);
	List<AssetTagResource> getAssetTagResourceList(String resourceName, long resourceId);
}
