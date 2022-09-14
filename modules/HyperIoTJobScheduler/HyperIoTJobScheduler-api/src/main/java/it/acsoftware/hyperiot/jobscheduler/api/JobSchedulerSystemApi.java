package it.acsoftware.hyperiot.jobscheduler.api;

import it.acsoftware.hyperiot.base.api.HyperIoTBaseSystemApi;

import it.acsoftware.hyperiot.base.exception.HyperIoTRuntimeException;

/**
 *
 * @author Aristide Cittadino Interface component for %- projectSuffixUC SystemApi. This
 *         interface defines methods for additional operations.
 *
 */
public interface JobSchedulerSystemApi extends HyperIoTBaseSystemApi {

    /**
     * This method adds job to be scheduled
     * @param job Job to be scheduled
     * @throws HyperIoTRuntimeException HyperIoTRuntimeException
     */
    void addJob(HyperIoTJob job) throws HyperIoTRuntimeException;

    /**
     * This method removes job from being scheduled
     * @param job Job to be removed
     * @throws HyperIoTRuntimeException HyperIoTRuntimeException
     */
    void deleteJob(HyperIoTJob job) throws HyperIoTRuntimeException;

    /**
     * This method update scheduling of given job
     * @param job Job to be scheduled
     * @throws HyperIoTRuntimeException HyperIoTRuntimeException
     */
    void updateJob(HyperIoTJob job) throws HyperIoTRuntimeException;

}
