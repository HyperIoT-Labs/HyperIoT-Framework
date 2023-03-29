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
