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

package it.acsoftware.hyperiot.sparkmanager.service;

import it.acsoftware.hyperiot.base.action.util.HyperIoTActionsUtil;
import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.base.exception.HyperIoTUnauthorizedException;
import it.acsoftware.hyperiot.base.security.annotations.AllowGenericPermissions;
import it.acsoftware.hyperiot.base.security.util.HyperIoTSecurityUtil;
import it.acsoftware.hyperiot.base.service.HyperIoTBaseServiceImpl;
import it.acsoftware.hyperiot.sparkmanager.actions.SparkManagerAction;
import it.acsoftware.hyperiot.sparkmanager.api.SparkManagerApi;
import it.acsoftware.hyperiot.sparkmanager.api.SparkManagerSystemApi;
import it.acsoftware.hyperiot.sparkmanager.model.SparkManager;
import it.acsoftware.hyperiot.sparkmanager.model.SparkRestApiResponse;
import it.acsoftware.hyperiot.sparkmanager.model.SparkRestApiSubmissionRequest;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;




/**
 * @author Aristide Cittadino Implementation class of SparkManagerApi interface.
 * It is used to implement all additional methods in order to interact with the system layer.
 */
@Component(service = SparkManagerApi.class, immediate = true)
public final class SparkManagerServiceImpl extends HyperIoTBaseServiceImpl implements SparkManagerApi {
    public static final String SPARK_MANAGER_RESOURCE_NAME = "it.acsoftware.hyperiot.sparkmanager.model.SparkManager";

    /**
     * Injecting the SparkManagerSystemApi
     */
    private SparkManagerSystemApi systemService;

    /**
     * @return The current SparkManagerSystemApi
     */
    protected SparkManagerSystemApi getSystemService() {
        getLog().debug( "invoking getSystemService, returning: {}", this.systemService);
        return systemService;
    }

    /**
     * @param sparkManagerSystemService Injecting via OSGi DS current systemService
     */
    @Reference
    protected void setSystemService(SparkManagerSystemApi sparkManagerSystemService) {
        getLog().debug( "invoking setSystemService, setting: {}", systemService);
        this.systemService = sparkManagerSystemService;
    }

    @Override
    @AllowGenericPermissions(actions = SparkManagerAction.Names.GET_JOB_STATUS, resourceName = SPARK_MANAGER_RESOURCE_NAME)
    public SparkRestApiResponse getStatus(HyperIoTContext context, String driverId) {
        return systemService.getStatus(driverId);
    }

    @Override
    @AllowGenericPermissions(actions = SparkManagerAction.Names.KILL_JOB, resourceName = SPARK_MANAGER_RESOURCE_NAME)
    public SparkRestApiResponse kill(HyperIoTContext context, String driverId) {
        return systemService.kill(driverId);
    }

    @Override
    @AllowGenericPermissions(actions = SparkManagerAction.Names.SUBMIT_JOB, resourceName = SPARK_MANAGER_RESOURCE_NAME)
    public SparkRestApiResponse submitJob(HyperIoTContext context, SparkRestApiSubmissionRequest data) {
        return systemService.submitJob(data);
    }

}
