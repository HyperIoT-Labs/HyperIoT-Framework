package it.acsoftware.hyperiot.shared.entity.example.service;



import it.acsoftware.hyperiot.base.action.util.HyperIoTActionsUtil;
import it.acsoftware.hyperiot.base.action.util.HyperIoTShareAction;
import it.acsoftware.hyperiot.base.api.HyperIoTAction;
import it.acsoftware.hyperiot.base.api.HyperIoTOwnershipResourceService;
import it.acsoftware.hyperiot.permission.api.PermissionSystemApi;
import it.acsoftware.hyperiot.role.util.HyperIoTRoleConstants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import it.acsoftware.hyperiot.shared.entity.example.api.SharedEntityExampleSystemApi;
import org.apache.aries.jpa.template.JpaTemplate;
import it.acsoftware.hyperiot.shared.entity.example.api.SharedEntityExampleRepository;
import it.acsoftware.hyperiot.shared.entity.example.model.SharedEntityExample;

import it.acsoftware.hyperiot.base.service.entity.HyperIoTBaseEntitySystemServiceImpl ;

import java.util.List;

/**
 *
 * @author Aristide Cittadino Implementation class of the SharedEntityExampleSystemApi
 *         interface. This  class is used to implements all additional
 *         methods to interact with the persistence layer.
 */
@Component(service = SharedEntityExampleSystemApi.class, immediate = true)
public final class SharedEntityExampleSystemServiceImpl extends HyperIoTBaseEntitySystemServiceImpl<SharedEntityExample>
		implements SharedEntityExampleSystemApi {


	private PermissionSystemApi permissionSystemApi;

	/**
	 * Injecting the SharedEntityExampleRepository to interact with persistence layer
	 */
	private SharedEntityExampleRepository repository;

	/**
	 * Constructor for a SharedEntityExampleSystemServiceImpl
	 */
	public SharedEntityExampleSystemServiceImpl() {
		super(SharedEntityExample.class);
	}

	/**
	 * Return the current repository
	 */
	protected SharedEntityExampleRepository getRepository() {
		getLog().debug( "invoking getRepository, returning: {}" , this.repository);
		return repository;
	}

	/**
	 * @param sharedEntityExampleRepository The current value of SharedEntityExampleRepository to interact with persistence layer
	 */
	@Reference
	protected void setRepository(SharedEntityExampleRepository sharedEntityExampleRepository) {
		getLog().debug( "invoking setRepository, setting: {}" , sharedEntityExampleRepository);
		this.repository = sharedEntityExampleRepository;
	}

	/**
	 * Return the current PermissionSystemApi
	 */
	protected PermissionSystemApi getPermissionSystemApi() {
		getLog().debug( "invoking getPermissionSystemApi, returning: {}" , this.permissionSystemApi);
		return permissionSystemApi;
	}

	/**
	 * @param permissionSystemApi The current value of PermissionSystemApi to interact with permission layer
	 */
	@Reference
	protected void setPermissionSystemApi(PermissionSystemApi permissionSystemApi) {
		getLog().debug( "invoking setPermissionSystemApi, setting: {}" , permissionSystemApi);
		this.permissionSystemApi = permissionSystemApi;
	}

	@Activate
	private void onActivate(){
		this.checkRegisteredUserRoleExists();
	}

	private void checkRegisteredUserRoleExists() {
		String sharedEntityExampleResourceName = SharedEntityExample.class.getName();
		List<HyperIoTAction> actions = HyperIoTActionsUtil.getHyperIoTCrudActions(sharedEntityExampleResourceName);
		actions.add(HyperIoTActionsUtil.getHyperIoTAction(sharedEntityExampleResourceName, HyperIoTShareAction.SHARE));
		this.permissionSystemApi.checkOrCreateRoleWithPermissions(HyperIoTRoleConstants.ROLE_NAME_REGISTERED_USER, actions);
	}


}
