package it.acsoftware.hyperiot.jobscheduler.service;



import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import it.acsoftware.hyperiot.jobscheduler.api.JobSchedulerSystemApi;
import it.acsoftware.hyperiot.jobscheduler.api.JobSchedulerApi;

import  it.acsoftware.hyperiot.base.service.HyperIoTBaseServiceImpl;

/**
 *
 * @author Aristide Cittadino Implementation class of JobSchedulerApi interface.
 *         It is used to implement all additional methods in order to interact with the system layer.
 */
@Component(service = JobSchedulerApi.class, immediate = true)
public final class JobSchedulerServiceImpl extends  HyperIoTBaseServiceImpl implements JobSchedulerApi {
	/**
	 * Injecting the JobSchedulerSystemApi
	 */
	private JobSchedulerSystemApi systemService;

	/**
	 *
	 * @return The current JobSchedulerSystemApi
	 */
	protected JobSchedulerSystemApi getSystemService() {
        getLog().debug( "invoking getSystemService, returning: {}" , this.systemService);
		return systemService;
	}

	/**
	 *
	 * @param jobSchedulerSystemService Injecting via OSGi DS current systemService
	 */
	@Reference
	protected void setSystemService(JobSchedulerSystemApi jobSchedulerSystemService) {
        getLog().debug( "invoking setSystemService, setting: {}" , systemService);
		this.systemService = jobSchedulerSystemService ;
	}

}
