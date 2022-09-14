package it.acsoftware.hyperiot.asset.tag.api;

import it.acsoftware.hyperiot.asset.tag.model.AssetTagResource;
import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTBaseEntityApi;

import it.acsoftware.hyperiot.asset.tag.model.AssetTag;

import java.util.List;

/**
 * 
 * @author Aristide Cittadino Interface component for AssetTagApi. This interface
 *         defines methods for additional operations.
 *
 */
public interface AssetTagApi extends HyperIoTBaseEntityApi<AssetTag> {

    List<AssetTagResource> getAssetTagResourceList(HyperIoTContext context, String resourceName, long resourceId);

}