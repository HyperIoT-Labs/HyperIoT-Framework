package it.acsoftware.hyperiot.shared.entity.example.service;



import it.acsoftware.hyperiot.base.api.HyperIoTOwnershipResourceService;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTSharingEntityService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import it.acsoftware.hyperiot.shared.entity.example.api.SharedEntityExampleSystemApi;
import it.acsoftware.hyperiot.shared.entity.example.api.SharedEntityExampleApi;
import it.acsoftware.hyperiot.shared.entity.example.model.SharedEntityExample;
import it.acsoftware.hyperiot.base.service.entity.HyperIoTBaseEntityServiceImpl ;


/**
 *
 * @author Aristide Cittadino Implementation class of SharedEntityExampleApi interface.
 *         It is used to implement all additional methods in order to interact with the system layer.
 */
@Component(service = SharedEntityExampleApi.class, immediate = true)
public final class SharedEntityExampleServiceImpl extends HyperIoTBaseEntityServiceImpl<SharedEntityExample>
		implements SharedEntityExampleApi, HyperIoTOwnershipResourceService {
	/**
	 * Injecting the SharedEntityExampleSystemApi
	 */
	private SharedEntityExampleSystemApi systemService;

	/**
	 * Constructor for a SharedEntityExampleServiceImpl
	 */
	public SharedEntityExampleServiceImpl() {
		super(SharedEntityExample.class);
	}

	/**
	 *
	 * @return The current SharedEntityExampleSystemApi
	 */
	protected SharedEntityExampleSystemApi getSystemService() {
		getLog().debug( "invoking getSystemService, returning: {}" , this.systemService);
		return systemService;
	}

	/**
	 *
	 * @param sharedEntityExampleSystemService Injecting via OSGi DS current systemService
	 */
	@Reference
	protected void setSystemService(SharedEntityExampleSystemApi sharedEntityExampleSystemService) {
		getLog().debug( "invoking setSystemService, setting: {}" , systemService);
		this.systemService = sharedEntityExampleSystemService ;
	}

	@Override
	public String getOwnerFieldPath() {
		return "user.id";
	}
}
