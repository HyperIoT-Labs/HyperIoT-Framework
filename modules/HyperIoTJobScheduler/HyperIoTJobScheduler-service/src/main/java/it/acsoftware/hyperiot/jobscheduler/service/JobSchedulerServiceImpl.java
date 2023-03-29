/*
 * Copyright 2019-2023 HyperIoT
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
