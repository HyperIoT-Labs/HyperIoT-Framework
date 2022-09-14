package it.acsoftware.hyperiot.asset.tag.api;

import it.acsoftware.hyperiot.asset.tag.model.AssetTag;
import it.acsoftware.hyperiot.asset.tag.model.AssetTagResource;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTBaseEntitySystemApi;

import java.util.List;

/**
 * 
 * @author Aristide Cittadino Interface component for %- projectSuffixUC SystemApi. This
 *         interface defines methods for additional operations.
 *
 */
public interface AssetTagSystemApi extends HyperIoTBaseEntitySystemApi<AssetTag> {

    List<AssetTagResource> getAssetTagResourceList(String resourceName, long resourceId);

}