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
