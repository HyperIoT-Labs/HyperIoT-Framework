package it.acsoftware.hyperiot.hadoopmanager.api;

import it.acsoftware.hyperiot.base.api.HyperIoTBaseApi;
import it.acsoftware.hyperiot.base.api.HyperIoTContext;

import java.io.File;
import java.io.IOException;

/**
 * 
 * @author Aristide Cittadino Interface component for HadoopManagerApi. This interface
 *         defines methods for additional operations.
 *
 */
public interface HadoopManagerApi extends HyperIoTBaseApi {

    /**
     * It copies file to HDFS
     * @param context HyperIoTContext
     * @param file file to copy
     * @param path HDFS path
     * @param deleteSource Whether delete file if it exists or not
     * @throws IOException IOException
     */
    void copyFile(HyperIoTContext context, File file, String path, boolean deleteSource) throws IOException;

    /**
     * It deletes file
     * @param context HyperIoTContext
     * @param path Path to file
     * @throws IOException IOException
     */
    void deleteFile(HyperIoTContext context, String path) throws IOException;

}