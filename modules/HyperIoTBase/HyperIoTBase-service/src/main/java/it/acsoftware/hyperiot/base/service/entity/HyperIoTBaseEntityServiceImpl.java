/*
 * Copyright 2019-2023 ACSoftware
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

import it.acsoftware.hyperiot.base.action.util.HyperIoTCrudAction;
import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.base.api.HyperIoTOwnershipResourceService;
import it.acsoftware.hyperiot.base.api.entity.*;
import it.acsoftware.hyperiot.base.exception.HyperIoTEntityNotFound;
import it.acsoftware.hyperiot.base.exception.HyperIoTUnauthorizedException;
import it.acsoftware.hyperiot.base.security.annotations.AllowGenericPermissions;
import it.acsoftware.hyperiot.base.security.annotations.AllowPermissions;
import it.acsoftware.hyperiot.base.security.annotations.AllowPermissionsOnReturn;
import it.acsoftware.hyperiot.base.service.HyperIoTBaseAbstractService;
import it.acsoftware.hyperiot.base.util.HyperIoTUtil;
import it.acsoftware.hyperiot.query.util.filter.HyperIoTQueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.NoResultException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;


/**
 * @param <T> parameter that indicates a generic class
 * @author Aristide Cittadino Model class for HyperIoTBaseEntityServiceImpl.
 * This class implements the methods for basic CRUD operations. This
 * methods are reusable by all entities in order to interact with the
 * system layer.
 */
public abstract class HyperIoTBaseEntityServiceImpl<T extends HyperIoTBaseEntity> extends HyperIoTBaseAbstractService
        implements HyperIoTBaseEntityApi<T> {
    private Logger log = LoggerFactory.getLogger(this.getClass().getName());
    /**
     * Generic class for HyperIoT platform
     */
    private final Class<T> type;

    /**
     * Constructor for HyperIoTBaseEntityServiceImpl
     *
     * @param type parameter that indicates a generic entity
     */
    public HyperIoTBaseEntityServiceImpl(Class<T> type) {
        this.type = type;
    }

    /**
     * Save an entity in database
     *
     * @param entity parameter that indicates a generic entity
     * @param ctx    user context of HyperIoT platform
     * @return entity saved
     */
    @AllowPermissions(actions = {HyperIoTCrudAction.Names.SAVE})
    public T save(T entity, HyperIoTContext ctx) {
        this.log.debug("Service Saving entity {}: {} with context: {}", new Object[]{this.type.getSimpleName(), entity, ctx});
        return this.getSystemService().save(entity, ctx);
    }

    /**
     * Update an existing entity in database
     *
     * @param entity parameter that indicates a generic entity
     * @param ctx    user context of HyperIoT platform
     */

    @AllowPermissions(actions = {HyperIoTCrudAction.Names.UPDATE})
    public T update(T entity, HyperIoTContext ctx) {
        this.log.debug("Service Updating entity entity {}: {} with context: {}", new Object[]{this.type.getSimpleName(), entity, ctx});
        if (entity.getId() > 0) {
            return this.getSystemService().update(entity, ctx);
        }
        throw new HyperIoTEntityNotFound();
    }


    /**
     * Remove an entity in database
     *
     * @param id  parameter that indicates a entity id
     * @param ctx user context of HyperIoT platform
     */
    @AllowPermissions(actions = HyperIoTCrudAction.Names.REMOVE, checkById = true)
    public void remove(long id, HyperIoTContext ctx) {
        this.log.debug("Service Removing entity {} with id {} with context: {}", new Object[]{this.type.getSimpleName(), id, ctx});
        HyperIoTBaseEntity entity = this.getSystemService().find(id, ctx);
        this.getSystemService().remove(entity.getId(), ctx);
    }

    /**
     * Find an existing entity in database
     *
     * @param id  parameter that indicates a entity id
     * @param ctx user context of HyperIoT platform
     * @return Entity if found
     */
    @AllowPermissions(actions = HyperIoTCrudAction.Names.FIND, checkById = true)
    public T find(long id, HyperIoTContext ctx) {
        HyperIoTQuery queryFilter = HyperIoTQueryBuilder.newQuery().equals("id", id);
        return this.find(queryFilter, ctx);
    }

    /**
     * @param filter filter field-value pair which will be merged in "and" condition
     * @param ctx    user context of HyperIoT platform
     * @return
     */
    @Override
    @AllowGenericPermissions(actions = {HyperIoTCrudAction.Names.FIND})
    @AllowPermissionsOnReturn(actions = {HyperIoTCrudAction.Names.FIND})
    public T find(HashMap<String, Object> filter, HyperIoTContext ctx) {
        HyperIoTQuery finalFilter = HyperIoTQueryBuilder.fromMapInAndCondition(filter);
        return this.find(finalFilter, ctx);
    }

    /**
     * @param filter filter
     * @param ctx    user context of HyperIoT platform
     * @return
     */
    @Override
    @AllowGenericPermissions(actions = {HyperIoTCrudAction.Names.FIND})
    @AllowPermissionsOnReturn(actions = {HyperIoTCrudAction.Names.FIND})
    public T find(HyperIoTQuery filter, HyperIoTContext ctx) {
        this.log.debug("Service Find entity {} with id {} with context: {}", new Object[]{this.type.getSimpleName(), filter, ctx});
        try {
            filter = this.createConditionForOwnedOrSharedResource(filter, ctx);
            T entity = this.getSystemService().find(filter, ctx);
            return entity;
        } catch (NoResultException e) {
            throw new HyperIoTEntityNotFound();
        }
    }


    /**
     * Find all entity in database
     *
     * @param ctx user context of HyperIoT platform
     * @return Collection of entity
     */
    @AllowGenericPermissions(actions = HyperIoTCrudAction.Names.FINDALL)
    public Collection<T> findAll(HyperIoTQuery filter, HyperIoTContext ctx) {
        this.log.debug(
                "Service Find all entities {} with context: {}", new Object[]{this.type.getSimpleName(), ctx});
        filter = this.createConditionForOwnedOrSharedResource(filter, ctx);
        return this.getSystemService().findAll(filter, ctx);
    }

    /**
     * Find all entity in database
     *
     * @param queryOrder  parameter that define order's criteria
     * @param ctx user context of HyperIoT platform
     * @return Collection of entity
     */
    @AllowGenericPermissions(actions = HyperIoTCrudAction.Names.FINDALL)
    public Collection<T> findAll(HyperIoTQuery filter, HyperIoTContext ctx,HyperIoTQueryOrder queryOrder) {
        this.log.debug(
                "Service Find all entities {} with context: {}", new Object[]{this.type.getSimpleName(), ctx});
        filter = this.createConditionForOwnedOrSharedResource(filter, ctx);
        return this.getSystemService().findAll(filter, ctx,queryOrder);
    }



    /**
     * @param filter filter
     * @param ctx    user context of HyperIoT platform
     * @param delta
     * @param page
     * @return
     */
    @Override
    @AllowGenericPermissions(actions = HyperIoTCrudAction.Names.FINDALL)
    public HyperIoTPaginableResult<T> findAll(HyperIoTQuery filter, HyperIoTContext ctx, int delta, int page) {
        this.log.debug(
                "Service Find all entities {} with context: {}", new Object[]{this.type.getSimpleName(), ctx});
        filter = this.createConditionForOwnedOrSharedResource(filter, ctx);
        return this.getSystemService().findAll(filter, ctx, delta, page);
    }

    /**
     * @param queryOrder parameters that define order's criteria
     * @param filter filter
     * @param ctx    user context of HyperIoT platform
     * @param delta
     * @param page
     * @return
     */
    @Override
    @AllowGenericPermissions(actions = HyperIoTCrudAction.Names.FINDALL)
    public HyperIoTPaginableResult<T> findAll(HyperIoTQuery filter, HyperIoTContext ctx, int delta, int page,HyperIoTQueryOrder queryOrder) {
        this.log.debug(
                "Service Find all entities {} with context: {} and with Order", new Object[]{this.type.getSimpleName(), ctx});
        filter = this.createConditionForOwnedOrSharedResource(filter, ctx);
        return this.getSystemService().findAll(filter, ctx, delta, page,queryOrder);
    }

    /**
     * @param filter
     * @param ctx    user context of HyperIoT platform
     * @return
     */
    @Override
    @AllowGenericPermissions(actions = HyperIoTCrudAction.Names.FINDALL)
    public Collection<T> findAll(HashMap<String, Object> filter, HyperIoTContext ctx) {
        HyperIoTQuery finalFilter = HyperIoTQueryBuilder.fromMapInAndCondition(filter);
        return this.getSystemService().findAll(finalFilter, ctx);
    }

    /**
     * @param filter
     * @param ctx    user context of HyperIoT platform
     * @param delta
     * @param page
     * @return
     */
    @Override
    @AllowGenericPermissions(actions = HyperIoTCrudAction.Names.FINDALL)
    public HyperIoTPaginableResult<T> findAll(HashMap<String, Object> filter, HyperIoTContext ctx, int delta, int page) {
        HyperIoTQuery finalFilter = HyperIoTQueryBuilder.fromMapInAndCondition(filter);
        return this.findAll(finalFilter, ctx, delta, page);
    }

    /**
     * @param initialFilter
     * @param ctx
     * @return
     */
    private HyperIoTQuery createConditionForOwnedOrSharedResource(HyperIoTQuery initialFilter, HyperIoTContext ctx) {
        try {
            //admins can see everything
            if (!ctx.isAdmin() && HyperIoTOwnershipResourceService.class.isAssignableFrom(this.getClass())) {
                HyperIoTOwnershipResourceService ownedRes = (HyperIoTOwnershipResourceService) this;
                HyperIoTQuery ownedResourceFilter = null;

                if (ctx.getLoggedEntityId() != 0) {
                    ownedResourceFilter = HyperIoTQueryBuilder.newQuery().equals(ownedRes.getOwnerFieldPath(), ctx.getLoggedEntityId());
                } else {
                    throw new HyperIoTUnauthorizedException();
                }

                ownedResourceFilter = this.createFilterForOwnedOrSharedResource(ownedResourceFilter,ctx);

                if (initialFilter == null)
                    initialFilter = ownedResourceFilter;
                else if (ownedResourceFilter != null) {
                    initialFilter = initialFilter.and(ownedResourceFilter);
                }
            }
            return initialFilter;
        } catch (NoResultException e) {
            throw new HyperIoTEntityNotFound();
        }
    }

    protected abstract HyperIoTBaseEntitySystemApi<T> getSystemService();

    /**
     * Return current entity type
     */
    @Override
    public Class<T> getEntityType() {
        return this.type;
    }

    /**
     *
     * @param filter filter
     * @param ctx user context of HyperIoT platform
     * @return
     */
    @Override
    @AllowGenericPermissions(actions = HyperIoTCrudAction.Names.FINDALL)
    public long countAll(HyperIoTQuery filter, HyperIoTContext ctx) {
        this.log.debug(
                "Service countAll entities {} with context: {}", new Object[]{this.type.getSimpleName(), ctx});
        filter = this.createConditionForOwnedOrSharedResource(filter, ctx);
        return this.getSystemService().countAll(filter);
    }

    /**
     * Retrieve from OSGi the SharedEntitySystemApi
     *
     * @return the SharedEntitySystemApi
     */
    protected HyperIoTSharingEntityService getSharedEntitySystemService() {
        HyperIoTSharingEntityService sharedEntitySystemService = (HyperIoTSharingEntityService) HyperIoTUtil.getService(HyperIoTSharingEntityService.class);
        return sharedEntitySystemService;
    }

    HyperIoTQuery createFilterForOwnedOrSharedResource(HyperIoTQuery ownedResourceFilter, HyperIoTContext ctx){
        if (HyperIoTSharedEntity.class.isAssignableFrom(this.getEntityType())) {
            //forcing the condition that user must own the entity or is shared with him
            List<Long> entityIds = getSharedEntitySystemService().getEntityIdsSharedWithUser(type.getName(), ctx.getLoggedEntityId(), ctx);
            if (entityIds != null && entityIds.size() > 0)
                ownedResourceFilter = ownedResourceFilter.or(HyperIoTQueryBuilder.newQuery().in("id", entityIds));
        }
        return ownedResourceFilter;
    }

}
