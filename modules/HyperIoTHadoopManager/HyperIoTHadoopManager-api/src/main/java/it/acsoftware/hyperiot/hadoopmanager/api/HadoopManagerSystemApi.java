package it.acsoftware.hyperiot.hadoopmanager.api;

import it.acsoftware.hyperiot.base.api.HyperIoTBaseSystemApi;

import java.io.File;
import java.io.IOException;

/**
 * 
 * @author Aristide Cittadino Interface component for %- projectSuffixUC SystemApi. This
 *         interface defines methods for additional operations.
 *
 */
public interface HadoopManagerSystemApi extends HyperIoTBaseSystemApi {

    /**
     * It copies file to HDFS
     * @param file file to copy
     * @param path HDFS path
     * @param deleteSource Whether delete file if it exists or not
     * @throws IOException IOException
     */
    void copyFile(File file, String path, boolean deleteSource) throws IOException;

    /**
     * It deletes file
     * @param path Path to file
     * @throws IOException IOException
     */
    void deleteFile(String path) throws IOException;

    /**
     * It deletes folder
     * @param path Path to folder
     */
    void deleteFolder(String path) throws IOException;

}