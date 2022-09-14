package it.acsoftware.hyperiot.asset.category.service;

import java.util.List;


import it.acsoftware.hyperiot.base.action.util.HyperIoTActionsUtil;
import it.acsoftware.hyperiot.base.api.HyperIoTAction;
import it.acsoftware.hyperiot.permission.api.PermissionSystemApi;
import it.acsoftware.hyperiot.role.util.HyperIoTRoleConstants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import it.acsoftware.hyperiot.asset.category.api.AssetCategoryRepository;
import it.acsoftware.hyperiot.asset.category.api.AssetCategorySystemApi;
import it.acsoftware.hyperiot.asset.category.model.AssetCategory;
import it.acsoftware.hyperiot.base.service.entity.HyperIoTBaseEntitySystemServiceImpl;

/**
 *
 * @author Aristide Cittadino Implementation class of the AssetCategorySystemApi
 *         interface. This class is used to implements all additional methods to
 *         interact with the persistence layer.
 */
@Component(service = AssetCategorySystemApi.class, immediate = true)
public final class AssetCategorySystemServiceImpl extends HyperIoTBaseEntitySystemServiceImpl<AssetCategory>
		implements AssetCategorySystemApi {


	/**
	 * Injecting the AssetCategoryRepository to interact with persistence layer
	 */
	private AssetCategoryRepository repository;

	private PermissionSystemApi permissionSystemApi;

	/**
	 * Constructor for a AssetCategorySystemServiceImpl
	 */
	public AssetCategorySystemServiceImpl() {
		super(AssetCategory.class);
	}

	/**
	 * Return the current repository
	 */
	protected AssetCategoryRepository getRepository() {
		getLog().debug( "invoking getRepository, returning: {}" , this.repository);
		return repository;
	}

	/**
	 * @param assetCategoryRepository The current value of AssetCategoryRepository
	 *                                to interact with persistence layer
	 */
	@Reference
	protected void setRepository(AssetCategoryRepository assetCategoryRepository) {
		getLog().debug( "invoking setRepository, setting: {}" , assetCategoryRepository);
		this.repository = assetCategoryRepository;
	}

	@Reference
	public void setPermissionSystemApi(PermissionSystemApi permissionSystemApi) {
		this.permissionSystemApi = permissionSystemApi;
	}

	@Activate
	public void onActivate() {
		this.checkRegisteredUserRoleExists();
	}

	private void checkRegisteredUserRoleExists() {
		String resourceName = AssetCategory.class.getName();
		List<HyperIoTAction> actions = HyperIoTActionsUtil.getHyperIoTCrudActions(resourceName);
		this.permissionSystemApi.checkOrCreateRoleWithPermissions(HyperIoTRoleConstants.ROLE_NAME_REGISTERED_USER, actions);
	}

}
