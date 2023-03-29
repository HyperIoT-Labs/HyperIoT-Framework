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
