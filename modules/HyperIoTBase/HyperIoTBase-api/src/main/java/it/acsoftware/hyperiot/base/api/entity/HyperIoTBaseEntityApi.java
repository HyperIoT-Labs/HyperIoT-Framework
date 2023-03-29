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

package it.acsoftware.hyperiot.base.api.entity;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.base.api.HyperIoTService;

/**
 * @param <T> parameter that indicates a generic class
 * @author Aristide Cittadino Interface component for HyperIoTBaseEntityApi.
 * This interface defines the methods for basic CRUD operations. This
 * methods are reusable by all entities that interact with the HyperIoT
 * platform.
 */
public interface HyperIoTBaseEntityApi<T extends HyperIoTBaseEntity> extends HyperIoTService {

    /**
     * Save an entity in database
     *
     * @param entity parameter that indicates a generic entity
     * @param ctx    user context of HyperIoT platform
     * @return entity saved
     */
    public T save(T entity, HyperIoTContext ctx);

    /**
     * Update an existing entity in database
     *
     * @param entity parameter that indicates a generic entity
     * @param ctx    user context of HyperIoT platform
     */
    public T update(T entity, HyperIoTContext ctx);

    /**
     * Remove an entity in database
     *
     * @param id  parameter that indicates a entity id
     * @param ctx user context of HyperIoT platform
     */
    public void remove(long id, HyperIoTContext ctx);

    /**
     *
     * @param id
     * @param ctx
     * @return
     */
    public T find(long id, HyperIoTContext ctx);


    /**
     * Find an existing entity in database
     * @param filter field-value pair which will be merged in "and" condition
     * @param ctx user context of HyperIoT platform
     * @return Entity if found
     */
    public T find(HashMap<String,Object> filter, HyperIoTContext ctx);


    /**
     * Find an existing entity in database
     * @param filter filter
     * @param ctx user context of HyperIoT platform
     * @return Entity if found
     */
    public T find(HyperIoTQuery filter, HyperIoTContext ctx);

    /**
     * Find all entity in database
     *
     * @param ctx user context of HyperIoT platform
     * @return Collection of entity
     */
    public Collection<T> findAll(HashMap<String,Object> filter,HyperIoTContext ctx);

    /**
     * Find all entity in database
     *
     * @param ctx   user context of HyperIoT platform
     * @param delta
     * @param page
     * @return Collection of entity
     */
    public HyperIoTPaginableResult<T> findAll(HashMap<String,Object> filter,HyperIoTContext ctx, int delta, int page);

    /**
     * Find all entity in database
     * @param filter filter
     * @param ctx user context of HyperIoT platform
     * @return Collection of entity
     */
    public Collection<T> findAll(HyperIoTQuery filter, HyperIoTContext ctx);

    /**
     * Find all entity in database
     *
     * @param ctx user context of HyperIoT platform
     * @param queryOrder parameter that define order's criteria
     * @return Collection of entity
     */
    public Collection<T> findAll(HyperIoTQuery filter, HyperIoTContext ctx, HyperIoTQueryOrder queryOrder);

    /**
     * Find all entity in database
     * @param filter filter
     * @param ctx   user context of HyperIoT platform
     * @param delta
     * @param page
     * @return Collection of entity
     */
    public HyperIoTPaginableResult<T> findAll(HyperIoTQuery filter, HyperIoTContext ctx, int delta, int page);

    /**
     * Find all entity in database
     * @param queryOrder parameter that define order's criteria
     * @param filter filter
     * @param ctx   user context of HyperIoT platform
     * @param delta
     * @param page
     * @return Collection of entity
     */
    public HyperIoTPaginableResult<T> findAll(HyperIoTQuery filter, HyperIoTContext ctx, int delta, int page,HyperIoTQueryOrder queryOrder);

    /**
     *
     * @param filter filter
     * @param ctx user context of HyperIoT platform
     * @return total number of entities based on the given filter.
     */
    public long countAll(HyperIoTQuery filter, HyperIoTContext ctx);

    /**
     * Return current entity type
     */
    public Class<T> getEntityType();


}
