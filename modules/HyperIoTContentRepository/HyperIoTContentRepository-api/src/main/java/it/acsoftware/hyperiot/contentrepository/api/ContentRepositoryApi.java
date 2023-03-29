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

package it.acsoftware.hyperiot.contentrepository.api;

import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.contentrepository.model.HyperIoTDocumentNode;
import java.io.InputStream;
import java.util.*;

/**
 * 
 * @author Aristide Cittadino Interface component for ContentRepositoryApi. This interface
 *         defines methods for additional operations.
 *
 */
public interface ContentRepositoryApi  {

    /**
     * Service add new file relative the folderPath specified in resource's document repository. (The resource is identified by the pair resourceClassName-resourceId)
     * @param ctx contain information about the loggedUser
     * @param folderPath the path in which the file will be add.
     * @param fileResourceName the name of the file that will be add.
     * @param resourceClassName the class name of the resource.
     * @param resourceId the identifier of the resource
     */
    void addFileToSubFolder(HyperIoTContext ctx, String resourceClassName, long resourceId, HyperIoTDocumentNode folderPath, InputStream fileResource, String fileResourceName);

    /**
     * Service add new folder relative the folderPath specified in resource's document repository. (The resource is identified by the pair resourceClassName-resourceId)
     * @param ctx contain information about the loggedUser
     * @param folderPath the path in which the folder, with name like @param folderName, will be add.
     * @param folderName the name of the folder that will be add.
     * @param resourceClassName the class name of the resource.
     * @param resourceId the identifier of the resource
     */
    void addFolderToResourceDocumentsRepository (HyperIoTContext ctx, String resourceClassName, long resourceId, HyperIoTDocumentNode folderPath, String folderName);

    /**
     * Service upload file relative the folderPath specified in resource's document repository. (The resource is identified by the pair resourceClassName-resourceId)
     * @param ctx contain information about the loggedUser
     * @param folderPath the path in which the file will be search.
     * @param fileName the name of the folder that will be retrieve.
     * @param resourceClassName the class name of the resource.
     * @param resourceId the identifier of the resource
     * @return the file as InputStream.
     */
    InputStream getResourceFile(HyperIoTContext ctx, String resourceClassName, long resourceId , HyperIoTDocumentNode folderPath , String fileName);

    void initializeResourceContentStore(HyperIoTContext ctx, String resourceClassName , long resourceId, Collection<String> subFolderPath);

    /**
     * Service remove all document related to the resource . (The resource is identified by the pair resourceClassName-resourceId)
     * @param ctx contain information about the loggedUser
     * @param resourceClassName the class name of the resource.
     * @param resourceId the identifier of the resource
     */
    void removeAllResourceDocuments(HyperIoTContext ctx, String resourceClassName , long resourceId);

    /**
     * Service remove folder contained in folderPath from resource's document repository. (The resource is identified by the pair resourceClassName-resourceId)
     * @param ctx contain information about the loggedUser
     * @param folderPath the path in which the folder ,with name @param folderName, will be search.
     * @param folderName the name of the folder that will be delete.
     * @param resourceClassName the class name of the resource.
     * @param resourceId the identifier of the resource
     */
    void removeFolderFromDocumentResource(HyperIoTContext ctx, String resourceClassName , long resourceId , HyperIoTDocumentNode folderPath, String folderName);

    /**
     * Service remove file contained in folderPath from resource's document repository. (The resource is identified by the pair resourceClassName-resourceId)
     * @param ctx contain information about the loggedUser
     * @param folderPath the path in which the file will be search.
     * @param fileName the name of the file that will be delete.
     * @param resourceClassName the class name of the resource.
     * @param resourceId the identifier of the resource
     */
    void removeFileFromDocumentResourceFolder(HyperIoTContext ctx, String resourceClassName, long resourceId, HyperIoTDocumentNode folderPath , String fileName);

    /**
     * Service retrieve the treeView of the resource's document repository. (The resource is identified by the pair resourceClassName-resourceId)
     * @param ctx contain information about the loggedUser
     * @param resourceClassName the class name of the resource.
     * @param resourceId the identifier of the resource
     * return ResourceDocumentsTreeView as HyperIoTDocumentNode Object.
     */
    List<HyperIoTDocumentNode> getResourceDocumentsTreeView(HyperIoTContext ctx, String resourceClassName, long resourceId);


    /**
     * Service rename document node contained in folderPath from resource's document repository. (The resource is identified by the pair resourceClassName-resourceId)
     * @param ctx contain information about the logged user
     * @param resourceClassName the class name of the resource.
     * @param resourceId the identifier of the resource
     * @param folderPath the path in which the document node will be search.
     * @param oldName the current name of the document node
     * @param newName the new name of the document node
     */
    void updateResourceDocumentNodeName(HyperIoTContext ctx, String resourceClassName, long resourceId , HyperIoTDocumentNode folderPath, String oldName, String newName);


}