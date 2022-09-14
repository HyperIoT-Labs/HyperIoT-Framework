package it.acsoftware.hyperiot.sparkmanager.api;

import it.acsoftware.hyperiot.base.api.HyperIoTBaseApi;
import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.sparkmanager.model.SparkRestApiResponse;
import it.acsoftware.hyperiot.sparkmanager.model.SparkRestApiSubmissionRequest;

import java.io.File;

/**
 * 
 * @author Aristide Cittadino Interface component for SparkManagerApi. This interface
 *         defines methods for additional operations.
 *
 */
public interface SparkManagerApi extends HyperIoTBaseApi {

    /**
     * It gets the status of a specific job
     * @param context HyperIoTContext
     * @param driverId Driver ID
     * @return SparkRestApiResponse
     */
    SparkRestApiResponse getStatus(HyperIoTContext context, String driverId);

    /**
     * It kills the specified job
     * @param context HyperIoTContext
     * @param driverId Driver ID
     * @return SparkRestApiResponse
     */
    SparkRestApiResponse kill(HyperIoTContext context, String driverId);

    /**
     * It submits spark job
     * @param context HyperIoTContext
     * @param data Spark Job config in JSON format
     * @return SparkRestApiResponse
     */
    SparkRestApiResponse submitJob(HyperIoTContext context, SparkRestApiSubmissionRequest data);

}