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

package it.acsoftware.hyperiot.base.security.annotations.implementation;


import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.base.api.HyperIoTService;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTBaseEntity;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTBaseEntityApi;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTBaseEntitySystemApi;
import it.acsoftware.hyperiot.base.api.proxy.HyperIoTBeforeMethodInterceptor;
import it.acsoftware.hyperiot.base.exception.HyperIoTRuntimeException;
import it.acsoftware.hyperiot.base.exception.HyperIoTUnauthorizedException;
import it.acsoftware.hyperiot.base.security.annotations.AllowPermissions;
import it.acsoftware.hyperiot.base.util.HyperIoTUtil;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * @Author Aristide Cittadino
 * This class is the implementation of @AllowPermission annotation.
 * It simply verifies that the user has the permission to do some action on the specified entity.
 * It's a specific permission, this means that permission verification is based  on the resource name and action name but eventually on the specific permission
 * on the specific entity.
 * <p>
 * The method tries to automatically infers the entity type (in case of EntityApi).
 */
@Component(service = {HyperIoTBeforeMethodInterceptor.class, AllowPermissionInterceptor.class}, immediate = true)
public class AllowPermissionInterceptor extends AbstractPermissionInterceptor implements HyperIoTBeforeMethodInterceptor<AllowPermissions> {
    private static Logger log = LoggerFactory.getLogger(AllowPermissionInterceptor.class.getName());

    @Override
    public void interceptMethod(HyperIoTService s, Method m, Object[] args, AllowPermissions annotation) {
        log.debug("Invoking interceptor @AllowPermission on method: {}", new Object[]{m.getName()});
        this.checkAnnotationIsOnHyperIoTApiClass(s, annotation);
        String[] actions = annotation.actions();
        if (serviceIsRelatedToEntityService(s, m, args, annotation)) {
            //we must check entity permissions
            HyperIoTContext ctx = this.findHyperIoTContextInMethodParams(args);
            HyperIoTBaseEntity entity = null;
            if (annotation.checkById()) {
                int entityIdIndex = annotation.idParamIndex();
                if (entityIdIndex >= 0) {
                    if (!(args[entityIdIndex] instanceof Long))
                        throw new HyperIoTRuntimeException("Parameter with id: " + annotation.idParamIndex() + " on method " + m.getName() + " is not a valid id type, must be long!");
                    //using id finder
                    if (annotation.systemApiRef().isEmpty()) {
                        entity = ((HyperIoTBaseEntityApi<?>) s).find((long) args[entityIdIndex], ctx);
                    } else {
                        //if systemApiRef name is specified , retrieve the entity from the related system api
                        //it must be entity System api for sure since there's an id related to it
                        HyperIoTBaseEntitySystemApi entitySystemApi = this.findSystemApi(annotation.systemApiRef());
                        if (entitySystemApi != null) {
                            entity = entitySystemApi.find((long) args[entityIdIndex], ctx);
                        } else {
                            throw new HyperIoTRuntimeException("Entity System Api specified (" + annotation.systemApiRef() + ") not found!");
                        }
                    }
                } else {
                    throw new HyperIoTRuntimeException("Impossible to find parameter with name : " + annotation.idParamIndex());
                }
            } else {
                entity = this.findObjectTypeInParams(HyperIoTBaseEntity.class, args);
            }
            boolean found = this.checkEntityPermission(ctx, entity, actions);
            if (!found)
                throw new HyperIoTUnauthorizedException();
            return;

        } else {
            throw new UnsupportedOperationException("@AllowPermission is not allowed on Generic Api, please us @AllowGenericPermission instead");
        }
    }

    /**
     * Checks wether annotation is put on a method relate to an entity service or use a system entity api
     *
     * @param s
     * @param m
     * @param args
     * @param annotation
     * @return
     */
    private boolean serviceIsRelatedToEntityService(HyperIoTService s, Method m, Object[] args, AllowPermissions annotation) {
        if (s instanceof HyperIoTBaseEntityApi)
            return true;
        if (annotation.systemApiRef().isEmpty())
            return false;
        String systemApiRef = annotation.systemApiRef();
        HyperIoTBaseEntitySystemApi entitySystemApi = this.findSystemApi(annotation.systemApiRef());
        return entitySystemApi != null;
    }

    /**
     * returns the related system api
     * @param systemApiRef
     * @return
     */
    private HyperIoTBaseEntitySystemApi findSystemApi(String systemApiRef) {
        try {
            HyperIoTBaseEntitySystemApi baseApi = (HyperIoTBaseEntitySystemApi) HyperIoTUtil.getService(Class.forName(systemApiRef));
            return baseApi;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
}
