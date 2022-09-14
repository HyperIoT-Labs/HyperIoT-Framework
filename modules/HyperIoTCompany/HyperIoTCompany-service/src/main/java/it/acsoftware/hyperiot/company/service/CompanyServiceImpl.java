package it.acsoftware.hyperiot.company.service;

import it.acsoftware.hyperiot.base.api.HyperIoTOwnershipResourceService;
import it.acsoftware.hyperiot.base.service.entity.HyperIoTBaseEntityServiceImpl;
import it.acsoftware.hyperiot.company.api.CompanyApi;
import it.acsoftware.hyperiot.company.api.CompanySystemApi;
import it.acsoftware.hyperiot.company.model.Company;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;




/**
 *
 * @author Aristide Cittadino Implementation class of CompanyApi interface.
 *         It is used to implement all additional methods in order to interact with the system layer.
 */
@Component(service = CompanyApi.class, immediate = true)
public final class CompanyServiceImpl extends HyperIoTBaseEntityServiceImpl<Company>  implements CompanyApi, HyperIoTOwnershipResourceService {
	/**
	 * Injecting the CompanySystemApi
	 */
	private CompanySystemApi systemService;

	/**
	 * Constructor for a CompanyServiceImpl
	 */
	public CompanyServiceImpl() {
		super(Company.class);
	}

	/**
	 *
	 * @return The current CompanySystemApi
	 */
	protected CompanySystemApi getSystemService() {
        getLog().debug( "invoking getSystemService, returning: {}" ,this.systemService);
		return systemService;
	}

	/**
	 *
	 * @param companySystemService Injecting via OSGi DS current systemService
	 */
	@Reference
	protected void setSystemService(CompanySystemApi companySystemService) {
        getLog().debug( "invoking setSystemService, setting: {}" , systemService);
		this.systemService = companySystemService ;
	}

	@Override
	public String getOwnerFieldPath() {
		return "HUserCreator.id";
	}

}
