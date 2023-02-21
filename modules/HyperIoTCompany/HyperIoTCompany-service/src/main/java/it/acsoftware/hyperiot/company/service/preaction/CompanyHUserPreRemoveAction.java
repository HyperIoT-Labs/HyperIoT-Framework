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

package it.acsoftware.hyperiot.company.service.preaction;


import it.acsoftware.hyperiot.base.api.entity.HyperIoTBaseEntity;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTPreRemoveAction;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTQuery;
import it.acsoftware.hyperiot.company.api.CompanySystemApi;
import it.acsoftware.hyperiot.company.model.Company;
import it.acsoftware.hyperiot.huser.model.HUser;
import it.acsoftware.hyperiot.query.util.filter.HyperIoTQueryBuilder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

@Component(service = HyperIoTPreRemoveAction.class, property = {"type=it.acsoftware.hyperiot.huser.model.HUser"})
public class CompanyHUserPreRemoveAction<T extends HyperIoTBaseEntity> implements HyperIoTPreRemoveAction<T>  {

    private static final Logger log = LoggerFactory.getLogger(CompanyHUserPreRemoveAction.class);

    private CompanySystemApi companySystemApi;

    @Override
    public void execute(T t) {
        HUser user = (HUser) t ;
        log.info("In CompanyHUserPreRemoveAction , delete company related to HUser with id {}", user.getId());
        HyperIoTQuery byUserId = HyperIoTQueryBuilder.newQuery().equals("HUserCreator.id", user.getId());
        Collection<Company> userCompanies = companySystemApi.findAll(byUserId, null) ;
        for (Company company : userCompanies) {
            log.info("In CompanyHUserPreRemoveAction, delete company with id : {} , owned by user with id : {} ", company.getId(), user.getId());
            this.companySystemApi.remove(company.getId(), null);
        }
    }

    @Reference
    public void setCompanySystemApi(CompanySystemApi companySystemApi){
        log.debug( "invoking setCompanySystemApi, returning: {}", this.companySystemApi);
        this.companySystemApi =  companySystemApi;
    }
}
