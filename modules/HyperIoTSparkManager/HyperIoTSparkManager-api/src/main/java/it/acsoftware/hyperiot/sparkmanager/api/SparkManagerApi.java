/*
 * Copyright 2019-2023 ACSoftware
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