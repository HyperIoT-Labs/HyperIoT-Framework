package it.acsoftware.hyperiot.base.service.entity;

import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.base.api.HyperIoTOwnedChildResource;
import it.acsoftware.hyperiot.base.api.HyperIoTOwnedResource;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTBaseEntity;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTBaseEntityApi;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTBaseEntitySystemApi;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTQuery;
import it.acsoftware.hyperiot.query.util.filter.HyperIoTQueryBuilder;

import java.util.List;

/**
 *
 * @param <T> params indicate a generic hyperiot base entity class
 */
public abstract class HyperIoTOwnedChildBaseEntityServiceImpl <T extends HyperIoTBaseEntity> extends HyperIoTBaseEntityServiceImpl<T>
        implements HyperIoTBaseEntityApi<T> {


    @Override
    final HyperIoTQuery createFilterForOwnedOrSharedResource(HyperIoTQuery ownedResourceFilter, HyperIoTContext ctx) {
        ownedResourceFilter = super.createFilterForOwnedOrSharedResource(ownedResourceFilter,ctx);
        if(HyperIoTOwnedChildResource.class.isAssignableFrom(this.getEntityType())){
            Class<? extends HyperIoTOwnedResource> rootParentClass = getParentResourceClass();
            List<Long> entityIds = getSharedEntitySystemService().getEntityIdsSharedWithUser(rootParentClass.getName(), ctx.getLoggedEntityId(), ctx);
            if (entityIds != null && entityIds.size() > 0){
                ownedResourceFilter = ownedResourceFilter.or(HyperIoTQueryBuilder.newQuery().in(this.getRootParentFieldPath(), entityIds));
            }
        }
        return ownedResourceFilter;
    }

    /**
     * Constructor for HyperIoTBaseEntityServiceImpl
     *
     * @param type parameter that indicates a generic entity
     */
    public HyperIoTOwnedChildBaseEntityServiceImpl(Class<T> type) {
        super(type);
    }

    @Override
    protected abstract HyperIoTBaseEntitySystemApi<T> getSystemService();

    protected abstract String getRootParentFieldPath();

    protected abstract Class<? extends HyperIoTOwnedResource> getParentResourceClass();
}
