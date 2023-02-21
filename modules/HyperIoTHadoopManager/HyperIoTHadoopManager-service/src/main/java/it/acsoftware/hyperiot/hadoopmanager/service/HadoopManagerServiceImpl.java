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

package it.acsoftware.hyperiot.hadoopmanager.service;

import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.base.security.annotations.AllowGenericPermissions;
import it.acsoftware.hyperiot.base.service.HyperIoTBaseServiceImpl;
import it.acsoftware.hyperiot.hadoopmanager.actions.HadoopManagerAction;
import it.acsoftware.hyperiot.hadoopmanager.api.HadoopManagerApi;
import it.acsoftware.hyperiot.hadoopmanager.api.HadoopManagerSystemApi;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.io.File;
import java.io.IOException;


/**
 * @author Aristide Cittadino Implementation class of HadoopManagerApi interface.
 * It is used to implement all additional methods in order to interact with the system layer.
 */
@Component(service = HadoopManagerApi.class, immediate = true)
public final class HadoopManagerServiceImpl extends HyperIoTBaseServiceImpl implements HadoopManagerApi {
    public static final String HADOOP_RESOURCE_NAME = "it.acsoftware.hyperiot.hadoopmanager.model.HadoopManager";
    /**
     * Injecting the HadoopManagerSystemApi
     */
    private HadoopManagerSystemApi systemService;

    /**
     * @return The current HadoopManagerSystemApi
     */
    protected HadoopManagerSystemApi getSystemService() {
        getLog().debug( "invoking getSystemService, returning: {}", this.systemService);
        return systemService;
    }

    /**
     * @param hadoopManagerSystemService Injecting via OSGi DS current systemService
     */
    @Reference
    protected void setSystemService(HadoopManagerSystemApi hadoopManagerSystemService) {
        this.systemService = hadoopManagerSystemService;
    }

    @Override
    @AllowGenericPermissions(actions = HadoopManagerAction.Names.COPY_FILE, resourceName = HADOOP_RESOURCE_NAME)
    public void copyFile(HyperIoTContext context, File file, String path, boolean deleteSource)
        throws IOException {
        systemService.copyFile(file, path, deleteSource);
    }

    @Override
    @AllowGenericPermissions(actions = HadoopManagerAction.Names.DELETE_FILE, resourceName = HADOOP_RESOURCE_NAME)
    public void deleteFile(HyperIoTContext context, String path) throws IOException {
        systemService.deleteFile(path);
    }

}
