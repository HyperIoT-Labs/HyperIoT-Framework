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
