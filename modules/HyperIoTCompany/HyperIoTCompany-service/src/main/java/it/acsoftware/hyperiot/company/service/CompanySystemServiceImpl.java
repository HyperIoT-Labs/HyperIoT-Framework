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



import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import it.acsoftware.hyperiot.base.service.entity.HyperIoTBaseEntitySystemServiceImpl ;
import it.acsoftware.hyperiot.company.api.CompanyRepository;
import it.acsoftware.hyperiot.company.api.CompanySystemApi;
import it.acsoftware.hyperiot.company.model.Company;

/**
 *
 * @author Aristide Cittadino Implementation class of the CompanySystemApi
 *         interface. This  class is used to implements all additional
 *         methods to interact with the persistence layer.
 */
@Component(service = CompanySystemApi.class, immediate = true)
public final class CompanySystemServiceImpl extends HyperIoTBaseEntitySystemServiceImpl<Company>   implements CompanySystemApi {

	/**
	 * Injecting the CompanyRepository to interact with persistence layer
	 */
	private CompanyRepository repository;

	/**
	 * Constructor for a CompanySystemServiceImpl
	 */
	public CompanySystemServiceImpl() {
		super(Company.class);
	}

	/**
	 * Return the current repository
	 */
	protected CompanyRepository getRepository() {
        getLog().debug( "invoking getRepository, returning: {}" , this.repository);
		return repository;
	}

	/**
	 * @param companyRepository The current value of CompanyRepository to interact with persistence layer
	 */
	@Reference
	protected void setRepository(CompanyRepository companyRepository) {
        getLog().debug( "invoking setRepository, setting: {}" , companyRepository);
		this.repository = companyRepository;
	}


}
