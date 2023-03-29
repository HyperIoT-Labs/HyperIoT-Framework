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

package it.acsoftware.hyperiot.shared.entity.service;


import it.acsoftware.hyperiot.base.action.util.HyperIoTActionsUtil;
import it.acsoftware.hyperiot.base.action.util.HyperIoTCrudAction;
import it.acsoftware.hyperiot.base.action.util.HyperIoTShareAction;
import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.base.api.HyperIoTOwnedResource;
import it.acsoftware.hyperiot.base.api.HyperIoTResource;
import it.acsoftware.hyperiot.base.api.HyperIoTUser;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTBaseEntitySystemApi;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTQuery;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTSharedEntity;
import it.acsoftware.hyperiot.base.exception.HyperIoTEntityNotFound;
import it.acsoftware.hyperiot.base.exception.HyperIoTRuntimeException;
import it.acsoftware.hyperiot.base.exception.HyperIoTUnauthorizedException;
import it.acsoftware.hyperiot.base.exception.HyperIoTValidationException;
import it.acsoftware.hyperiot.base.security.annotations.AllowGenericPermissions;
import it.acsoftware.hyperiot.base.security.annotations.AllowPermissionsOnReturn;
import it.acsoftware.hyperiot.base.security.util.HyperIoTSecurityUtil;
import it.acsoftware.hyperiot.base.service.entity.HyperIoTBaseEntityServiceImpl;
import it.acsoftware.hyperiot.base.util.HyperIoTUtil;
import it.acsoftware.hyperiot.huser.api.HUserSystemApi;
import it.acsoftware.hyperiot.huser.model.HUser;
import it.acsoftware.hyperiot.shared.entity.api.SharedEntityApi;
import it.acsoftware.hyperiot.shared.entity.api.SharedEntitySystemApi;
import it.acsoftware.hyperiot.shared.entity.model.SharedEntity;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.persistence.NoResultException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


/**
 * @author Aristide Cittadino Implementation class of SharedEntityApi interface.
 * It is used to implement all additional methods in order to interact with the system layer.
 */
@Component(service = SharedEntityApi.class, immediate = true)
public final class SharedEntityServiceImpl extends HyperIoTBaseEntityServiceImpl<SharedEntity> implements SharedEntityApi {
    /**
     * Injecting the SharedEntitySystemApi
     */
    private SharedEntitySystemApi systemService;

    /**
     * Injecting the HUserSystemApi
     */
    private HUserSystemApi userSystemService;

    /**
     * Constructor for a SharedEntityServiceImpl
     */
    public SharedEntityServiceImpl() {
        super(SharedEntity.class);
    }

    /**
     * @return The current SharedEntitySystemApi
     */
    protected SharedEntitySystemApi getSystemService() {
        getLog().debug("invoking getSystemService, returning: {}", this.systemService);
        return systemService;
    }

    /**
     * @param sharedEntitySystemService Injecting via OSGi DS current systemService
     */
    @Reference
    protected void setSystemService(SharedEntitySystemApi sharedEntitySystemService) {
        getLog().debug("invoking setSystemService, setting: {}", systemService);
        this.systemService = sharedEntitySystemService;
    }

    /**
     * @param userSystemService Injecting via OSGi DS current systemService
     */
    @Reference
    protected void setUserSystemService(HUserSystemApi userSystemService) {
        getLog().debug("invoking setUserSystemService, setting: {}", userSystemService);
        this.userSystemService = userSystemService;
    }

    @Override
    public SharedEntity save(SharedEntity entity, HyperIoTContext ctx) {
        Class<?> entityClass = (entity.getEntityResourceName() != null)?getEntityClass(entity.getEntityResourceName()):null;
        //Custom check on permission system
        //check if the user has the share permission for the entity identified by entityResourceName
        if (entityClass == null || !HyperIoTSecurityUtil.checkPermission(ctx, entityClass.getName(), HyperIoTActionsUtil.getHyperIoTAction(entityClass.getName(), HyperIoTShareAction.SHARE))) {
            throw new HyperIoTUnauthorizedException();
        }
        if (entityClass == null || !HyperIoTSharedEntity.class.isAssignableFrom(entityClass)) {
            throw new HyperIoTRuntimeException("Entity " + entity.getEntityResourceName() + " is not a HyperIoTSharedEntity");
        }
        HyperIoTBaseEntitySystemApi<? extends HyperIoTSharedEntity> systemService = getEntitySystemService(entityClass);
        if(HyperIoTOwnedResource.class.isAssignableFrom(entityClass)) {
            HyperIoTResource resource;
            try {
                resource = systemService.find(entity.getEntityId(), ctx);
            } catch (NoResultException exception) {
                throw new HyperIoTEntityNotFound();
            }
            HUser user;
            try {
                user = this.userSystemService.find(ctx.getLoggedEntityId(), ctx);
            } catch (NoResultException exception) {
                throw new HyperIoTUnauthorizedException();
            }
            if (!HyperIoTSecurityUtil.checkUserOwnsResource(ctx, user, resource)) {
                throw new HyperIoTUnauthorizedException();
            }
        }
        String entityClassName = entityClass.getName();
        return doSave(entityClassName, entity, ctx, systemService);
    }


    private SharedEntity doSave(String entityClassName, SharedEntity entity, HyperIoTContext ctx, HyperIoTBaseEntitySystemApi<? extends HyperIoTSharedEntity> entitySystemService) {
        HyperIoTSharedEntity e;
        try {
            e = entitySystemService.find(entity.getEntityId(), ctx);
            // find the user
            this.userSystemService.find(entity.getUserId(), ctx);
        } catch (NoResultException ex) {
            throw new HyperIoTEntityNotFound();
        }

        //check if the user owner of the entity is the logged one
        HyperIoTUser u = e.getUserOwner();
        if (u.getId() != ctx.getLoggedEntityId()) {
            throw new HyperIoTUnauthorizedException();
        }
        //do not check save permission for SharedEntity entities because if the share permission for an HyperIoTSharedEntity
        //implicitly has the permission to save a SharedEntity
        return systemService.save(entity, ctx);
    }

    @Override
    public SharedEntity update(SharedEntity entity, HyperIoTContext ctx) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void remove(long id, HyperIoTContext ctx) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SharedEntity find(long id, HyperIoTContext ctx) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SharedEntity find(HashMap<String, Object> filter, HyperIoTContext ctx) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SharedEntity find(HyperIoTQuery filter, HyperIoTContext ctx) {
        throw new UnsupportedOperationException();
    }

    @Override
    //We leave class permission management because it is a custom behaviour
    public void removeByPK(String entityResourceName, long entityId, long userId, HyperIoTContext ctx) {
        this.getLog().debug(
                "Service Remove entity {} with primary key (entityResourceName: {}, entityId: {}, userId: {}) with context: {}",
                new Object[]{this.getEntityType().getSimpleName(), entityResourceName, entityId, userId, ctx});

        Class<?> entityClass = getEntityClass(entityResourceName);

        //check if the user has the share permission for the entity identified by entityResourceName
        if (!HyperIoTSecurityUtil.checkPermission(ctx, entityClass.getName(), HyperIoTActionsUtil.getHyperIoTAction(entityClass.getName(), HyperIoTShareAction.SHARE))) {
            throw new HyperIoTUnauthorizedException();
        } else {
            //get the system service of the entity identified by entityResourceName
            HyperIoTBaseEntitySystemApi<? extends HyperIoTSharedEntity> systemService = getEntitySystemService(entityClass);

            //find the entity
            HyperIoTSharedEntity e;
            try {
                e = systemService.find(entityId, ctx);
            } catch (NoResultException ex) {
                throw new HyperIoTEntityNotFound();
            }

            //check if the user owner of the entity is the logged one
            HyperIoTUser u = e.getUserOwner();
            if (u.getId() != ctx.getLoggedEntityId()) {
                throw new HyperIoTUnauthorizedException();
            }
        }

        SharedEntity entity;
        try {
            entity = this.getSystemService().findByPK(entityResourceName, entityId, userId, null, ctx);
        } catch (NoResultException var7) {
            throw new HyperIoTEntityNotFound();
        }

        if (entity != null) {
            getSystemService().removeByPK(entityResourceName, entityId, userId, ctx);
        } else {
            throw new HyperIoTEntityNotFound();
        }

    }

    @Override
    @AllowGenericPermissions(actions = HyperIoTCrudAction.Names.FIND)
    @AllowPermissionsOnReturn(actions = HyperIoTCrudAction.Names.FIND)
    public SharedEntity findByPK(String entityResourceName, long entityId, long userId, HashMap<String, Object> filter, HyperIoTContext ctx) {
        this.getLog().debug(
                "Service Find entity {} with primary key (entityResourceName: {}, entityId: {}, userId: {}) with context: {}",
                new Object[]{this.getEntityType().getSimpleName(), entityResourceName, entityId, userId, ctx});
        SharedEntity entity;
        try {
            entity = this.getSystemService().findByPK(entityResourceName, entityId, userId, filter, ctx);
        } catch (NoResultException var7) {
            throw new HyperIoTEntityNotFound();
        }
        if (entity != null) {
            return entity;
        } else {
            throw new HyperIoTEntityNotFound();
        }
    }

    @Override
    @AllowGenericPermissions(actions = HyperIoTCrudAction.Names.FIND)
    public List<SharedEntity> findByEntity(String entityResourceName, long entityId, HashMap<String, Object> filter, HyperIoTContext ctx) {
        this.getLog().debug("Service Find entity {} with entityResourceName {}, entityId {} with context: {}",
                new Object[]{this.getEntityType().getSimpleName(), entityResourceName, entityId, ctx});
        return this.getSystemService().findByEntity(entityResourceName, entityId, filter, ctx);
    }

    @Override
    @AllowGenericPermissions(actions = HyperIoTCrudAction.Names.FIND)
    public List<SharedEntity> findByUser(long userId, HashMap<String, Object> filter, HyperIoTContext ctx) {
        this.getLog().debug("Service Find entity {} with userId {} with context: {}", new Object[]{this.getEntityType().getSimpleName(), userId, ctx});
        return this.getSystemService().findByUser(userId, filter, ctx);
    }

    @Override
    @AllowGenericPermissions(actions = HyperIoTCrudAction.Names.FIND)
    public List<HyperIoTUser> getSharingUsers(String entityResourceName, long entityId, HyperIoTContext context) {
        this.getLog().debug("Service getSharingUsers {} with entityResourceName {}, entityId {} with context: {}",
                new Object[]{this.getEntityType().getSimpleName(), entityResourceName, entityId, context});
        return this.getSystemService().getSharingUsers(entityResourceName, entityId, context);
    }

    @Override
    @AllowGenericPermissions(actions = HyperIoTCrudAction.Names.FIND)
    public List<Long> getEntityIdsSharedWithUser(String entityResourceName, long userId, HyperIoTContext context) {
        this.getLog().debug("Service getEntityIdsSharedWithUser {} with entityResourceName {}, userId {} with context: {}",
                new Object[]{this.getEntityType().getSimpleName(), entityResourceName, userId, context});
        return this.getSystemService().getEntityIdsSharedWithUser(entityResourceName, userId, context);
    }

    private HyperIoTBaseEntitySystemApi<? extends HyperIoTSharedEntity> getEntitySystemService(Class<?> entityClass) {
        this.getLog().debug("Get system service of entity {}", new Object[]{this.getEntityType().getSimpleName()});

        Class<?> systemApiClass = null;
        try {
            systemApiClass = Class.forName(entityClass.getName().replace(".model.", ".api.") + "SystemApi");
        } catch (ClassNotFoundException e) {
        }

        HyperIoTBaseEntitySystemApi<? extends HyperIoTSharedEntity> systemApi = null;
        if (systemApiClass != null) {
            systemApi = (HyperIoTBaseEntitySystemApi<? extends HyperIoTSharedEntity>) HyperIoTUtil.getService(systemApiClass);
        }

        if (systemApi == null) {
            throw new HyperIoTRuntimeException("No such system service found for entity " + entityClass.getSimpleName());
        }
        return systemApi;
    }

    private Class<?> getEntityClass(String resourceName) {
        try {
            return Class.forName(resourceName);
        } catch (ClassNotFoundException e) {
            throw new HyperIoTRuntimeException("Entity " + resourceName + " is not a HyperIoTSharedEntity");
        }
    }

}
