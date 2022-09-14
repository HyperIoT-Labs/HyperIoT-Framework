package it.acsoftware.hyperiot.sparkmanager.api;

import it.acsoftware.hyperiot.base.api.HyperIoTBaseSystemApi;
import it.acsoftware.hyperiot.sparkmanager.model.SparkRestApiResponse;
import it.acsoftware.hyperiot.sparkmanager.model.SparkRestApiSubmissionRequest;

import java.io.File;

/**
 * 
 * @author Aristide Cittadino Interface component for %- projectSuffixUC SystemApi. This
 *         interface defines methods for additional operations.
 *
 */
public interface SparkManagerSystemApi extends HyperIoTBaseSystemApi {

    /**
     * It gets the status of a specific job
     * @param driverId Driver ID
     * @return SparkRestApiResponse
     */
    SparkRestApiResponse getStatus(String driverId);

    /**
     * It kills the specified job
     * @param driverId Driver ID
     * @return SparkRestApiResponse
     */
    SparkRestApiResponse kill(String driverId);

    /**
     * It submits spark job jar file
     * @param data Spark Job config in JSON format
     * @return SparkRestApiResponse
     */
    SparkRestApiResponse submitJob(SparkRestApiSubmissionRequest data);

}