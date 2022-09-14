package it.acsoftware.hyperiot.asset.tag.service;

import it.acsoftware.hyperiot.asset.tag.api.AssetTagApi;
import it.acsoftware.hyperiot.asset.tag.api.AssetTagSystemApi;
import it.acsoftware.hyperiot.asset.tag.model.AssetTag;
import it.acsoftware.hyperiot.asset.tag.model.AssetTagResource;
import it.acsoftware.hyperiot.base.action.util.HyperIoTCrudAction;
import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.base.security.annotations.AllowGenericPermissions;
import it.acsoftware.hyperiot.base.service.entity.HyperIoTBaseEntityServiceImpl;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;


/**
 * @author Aristide Cittadino Implementation class of AssetTagApi interface.
 * It is used to implement all additional methods in order to interact with the system layer.
 */
@Component(service = AssetTagApi.class, immediate = true)
public final class AssetTagServiceImpl extends HyperIoTBaseEntityServiceImpl<AssetTag> implements AssetTagApi {
    /**
     * Injecting the AssetTagSystemApi
     */
    private AssetTagSystemApi systemService;

    /**
     * Constructor for a AssetTagServiceImpl
     */
    public AssetTagServiceImpl() {
        super(AssetTag.class);
    }

    /**
     * @return The current AssetTagSystemApi
     */
    protected AssetTagSystemApi getSystemService() {
        getLog().debug( "invoking getSystemService, returning: {}", this.systemService);
        return systemService;
    }

    /**
     * @param assetTagSystemService Injecting via OSGi DS current systemService
     */
    @Reference
    protected void setSystemService(AssetTagSystemApi assetTagSystemService) {
        getLog().debug( "invoking setSystemService, setting: {}", systemService);
        this.systemService = assetTagSystemService;
    }

    /**
     * Return list of resources with that tag
     * @param context
     * @param resourceName
     * @param resourceId
     * @return
     */
    @Override
    @AllowGenericPermissions(actions = HyperIoTCrudAction.Names.FINDALL)
    public List<AssetTagResource> getAssetTagResourceList(HyperIoTContext context, String resourceName, long resourceId) {
        return systemService.getAssetTagResourceList(resourceName, resourceId);
    }
}
