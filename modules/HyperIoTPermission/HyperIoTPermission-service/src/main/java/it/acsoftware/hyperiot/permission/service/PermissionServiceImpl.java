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

package it.acsoftware.hyperiot.permission.service;

import it.acsoftware.hyperiot.base.api.HyperIoTAction;
import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.base.api.HyperIoTPermissionManager;
import it.acsoftware.hyperiot.base.exception.HyperIoTUnauthorizedException;
import it.acsoftware.hyperiot.base.service.entity.HyperIoTBaseEntityServiceImpl;
import it.acsoftware.hyperiot.permission.api.PermissionApi;
import it.acsoftware.hyperiot.permission.api.PermissionSystemApi;
import it.acsoftware.hyperiot.permission.model.Permission;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author Aristide Cittadino Implementation class of the PermissionApi. It is
 * used to implement all additional methods in order to interact with
 * the system layer.
 */
@Component(service = PermissionApi.class, immediate = true)
public class PermissionServiceImpl extends HyperIoTBaseEntityServiceImpl<Permission> implements PermissionApi {

    /**
     * Injecting the PermissionSystemService to use methods in PermissionSystemApi
     * interface
     */
    private PermissionSystemApi systemService;
    private HyperIoTPermissionManager permissionManager;

    /**
     * Constructor for a PermissionServiceImpl
     */
    public PermissionServiceImpl() {
        super(Permission.class);
    }

    /**
     * @return The current PermissionSystemService
     */
    public PermissionSystemApi getSystemService() {
        getLog().debug("invoking getSystemService, returning: {}", this.systemService);
        return systemService;
    }

    /**
     * @param systemService Injecting via OSGi DS current PermissionSystemService
     */
    @Reference
    protected void setSystemService(PermissionSystemApi systemService) {
        getLog().debug("invoking setSystemService, setting: {}", systemService);
        this.systemService = systemService;
    }

    @Reference
    public void setPermissionManager(HyperIoTPermissionManager permissionManager) {
        this.permissionManager = permissionManager;
    }


    @Override
    public Map<String, Map<String, Map<String, Boolean>>> entityPermissionMap(HyperIoTContext context, Map<String, List<Long>> entityPks) {
        if (context == null || context.getLoggedUsername() == null || context.getLoggedUsername().isBlank())
            throw new HyperIoTUnauthorizedException();
        return permissionManager.entityPermissionMap(context.getLoggedUsername(), entityPks);
    }

}
