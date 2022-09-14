package it.acsoftware.hyperiot.contentrepository.api;

import it.acsoftware.hyperiot.base.api.HyperIoTBaseSystemApi;
import it.acsoftware.hyperiot.contentrepository.model.HyperIoTDocumentNode;

import java.io.InputStream;
import java.util.*;

/**
 * 
 * @author Aristide Cittadino Interface component for ContentRepositorySystemApi.
 * This interface defines methods for additional operations.
 *
 */
public interface ContentRepositorySystemApi extends HyperIoTBaseSystemApi {

    /**
     * Service add new file relative the folderPath specified in resource's document repository. (The resource is identified by the pair resourceClassName-resourceId)
     * @param folderPath the path in which the file will be add.
     * @param fileResourceName the name of the file that will be add.
     * @param resourceClassName the class name of the resource.
     * @param resourceId the identifier of the resource
     */
    void addFileToSubFolder(String resourceClassName, long resourceId, HyperIoTDocumentNode folderPath, InputStream fileResource, String fileResourceName);

    /**
     * Service add new folder relative the folderPath specified in resource's document repository. (The resource is identified by the pair resourceClassName-resourceId)
     * @param folderPath the path in which the folder will be add.
     * @param folderName the name of the folder that will be add.
     * @param resourceClassName the class name of the resource.
     * @param resourceId the identifier of the resource
     */
    void addFolderToResourceDocumentsRepository (String resourceClassName, long resourceId, HyperIoTDocumentNode folderPath, String folderName);

    /**
     * Service upload file relative the folderPath specified in resource's document repository. (The resource is identified by the pair resourceClassName-resourceId)
     * @param folderPath the path in which the file will be search.
     * @param fileName the name of the folder that will be retrieve.
     * @param resourceClassName the class name of the resource.
     * @param resourceId the identifier of the resource
     * @return the file as InputStream.
     */
    InputStream getResourceFile(String resourceClassName, long resourceId , HyperIoTDocumentNode folderPath , String fileName);

    void initializeResourceContentStore(String resourceClassName , long resourceId, Collection<String> subFolderPath);

    /**
     * Service remove all document related to the resource . (The resource is identified by the pair resourceClassName-resourceId)
     * @param resourceClassName the class name of the resource.
     * @param resourceId the identifier of the resource
     */
    void removeAllResourceDocuments(String resourceClassName , long resourceId);

    /**
     * Service remove folder contained in folderPath from resource's document repository. (The resource is identified by the pair resourceClassName-resourceId)
     * @param folderPath the path in which the folder ,with name @param folderName, will be search.
     * @param folderName the name of the folder that will be delete.
     * @param resourceClassName the class name of the resource.
     * @param resourceId the identifier of the resource
     */
    void removeFolderFromDocumentResource(String resourceClassName , long resourceId, HyperIoTDocumentNode folderPath, String folderName);

    /**
     * Service remove file contained in folderPath from resource's document repository. (The resource is identified by the pair resourceClassName-resourceId)
     * @param folderPath the path in which the file will be search.
     * @param fileName the name of the file that will be delete.
     * @param resourceClassName the class name of the resource.
     * @param resourceId the identifier of the resource
     */
    void removeFileFromDocumentResourceFolder(String resourceClassName, long resourceId , HyperIoTDocumentNode folderPath, String fileName);

    /**
     * Service retrieve the treeView of the resource's document repository. (The resource is identified by the pair resourceClassName-resourceId)
     * @param resourceClassName the class name of the resource.
     * @param resourceId the identifier of the resource
     * return ResourceDocumentsTreeView as HyperIoTDocumentNode Object.
     */
    List<HyperIoTDocumentNode> getResourceDocumentsTreeView( String resourceClassName, long resourceId);


    /**
     * Service rename document node contained in folderPath from resource's document repository. (The resource is identified by the pair resourceClassName-resourceId)
     * @param resourceClassName the class name of the resource.
     * @param resourceId the identifier of the resource
     * @param folderPath the path in which the document node will be search.
     * @param oldName the current name of the document node
     * @param newName the new name of the document node
     */
    void updateResourceDocumentNodeName(String resourceClassName, long resourceId , HyperIoTDocumentNode folderPath, String oldName, String newName);
}