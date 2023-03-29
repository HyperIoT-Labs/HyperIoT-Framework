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

package it.acsoftware.hyperiot.asset.tag.service;

import it.acsoftware.hyperiot.asset.tag.api.AssetTagRepository;
import it.acsoftware.hyperiot.asset.tag.api.AssetTagSystemApi;
import it.acsoftware.hyperiot.asset.tag.model.AssetTag;
import it.acsoftware.hyperiot.asset.tag.model.AssetTagResource;
import it.acsoftware.hyperiot.base.action.util.HyperIoTActionsUtil;
import it.acsoftware.hyperiot.base.api.HyperIoTAction;
import it.acsoftware.hyperiot.base.service.entity.HyperIoTBaseEntitySystemServiceImpl;
import it.acsoftware.hyperiot.permission.api.PermissionSystemApi;
import it.acsoftware.hyperiot.role.util.HyperIoTRoleConstants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;


/**
 * @author Aristide Cittadino Implementation class of the AssetTagSystemApi
 * interface. This class is used to implements all additional methods to
 * interact with the persistence layer.
 */
@Component(service = AssetTagSystemApi.class, immediate = true)
public final class AssetTagSystemServiceImpl extends HyperIoTBaseEntitySystemServiceImpl<AssetTag>
        implements AssetTagSystemApi {

    /**
     * Injecting the AssetTagRepository to interact with persistence layer
     */
    private AssetTagRepository repository;

    private PermissionSystemApi permissionSystemApi;

    /**
     * Constructor for a AssetTagSystemServiceImpl
     */
    public AssetTagSystemServiceImpl() {
        super(AssetTag.class);
    }

    /**
     * Return the current repository
     */
    protected AssetTagRepository getRepository() {
        getLog().debug( "invoking getRepository, returning: {}" , this.repository);
        return repository;
    }

    /**
     * @param assetTagRepository The current value of AssetTagRepository to interact
     *                           with persistence layer
     */
    @Reference
    protected void setRepository(AssetTagRepository assetTagRepository) {
        getLog().debug( "invoking setRepository, setting: {}" , assetTagRepository);
        this.repository = assetTagRepository;
    }

    @Reference
    public void setPermissionSystemApi(PermissionSystemApi permissionSystemApi) {
        this.permissionSystemApi = permissionSystemApi;
    }

    @Activate
    public void onActivate() {
        this.checkRegisteredUserRoleExists();
    }

    private void checkRegisteredUserRoleExists() {
        String resourceName = AssetTag.class.getName();
        List<HyperIoTAction> actions = HyperIoTActionsUtil.getHyperIoTCrudActions(resourceName);
        this.permissionSystemApi
                .checkOrCreateRoleWithPermissions(HyperIoTRoleConstants.ROLE_NAME_REGISTERED_USER, actions);
    }


    @Override
    public List<AssetTagResource> getAssetTagResourceList(String resourceName, long resourceId) {
        // TODO add resourceName validation
        return repository.getAssetTagResourceList(resourceName, resourceId);
    }
}
