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

package it.acsoftware.hyperiot.sparkmanager.job;

import it.acsoftware.hyperiot.base.util.HyperIoTUtil;
import it.acsoftware.hyperiot.sparkmanager.util.SparkManagerUtil;
import it.acsoftware.hyperiot.sparkmanager.api.SparkManagerSystemApi;
import it.acsoftware.hyperiot.sparkmanager.model.SparkRestApiResponse;
import it.acsoftware.hyperiot.sparkmanager.model.SparkRestApiSubmissionRequest;
import org.quartz.*;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unused")
public abstract class HyperIoTSparkJob implements Job {

    protected static final Logger log = LoggerFactory.getLogger(HyperIoTSparkJob.class.getName());
    private static final String ERROR_MESSAGE = "Job {} is not going to be fired because of empty value for argument {}";

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDetail jobDetail = context.getJobDetail();
        JobKey jobKey = jobDetail.getKey();
        JobDataMap jobDataMap = jobDetail.getJobDataMap();
        try {
            log.info( "Preparing job {} for its execution", jobKey);
            // Create request body which will be sent to Spark hidden REST api
            SparkRestApiSubmissionRequest request = new SparkRestApiSubmissionRequest();
            // get some mandatory parameters
            String appResource = getJobArg(jobKey, jobDataMap, "appResource");
            if (appResource == null)
                return;
            String mainClass = getJobArg(jobKey, jobDataMap, "mainClass");
            if (mainClass == null)
                return;
            String sparkJarsProperty = getJobArg(jobKey, jobDataMap, "spark.jars");
            if (sparkJarsProperty == null)
                return;
            // get Spark job arguments
            String[] appArgs = getAppArgs(jobDetail);
            // set env variables
            Map<String, String> envVars = new HashMap<>();
            envVars.put("SPARK_ENV_LOADED", String.valueOf(SparkManagerUtil.getSparkEnvLoaded()));
            // get Spark properties
            Map<String, String> sparkProps = getSparkProperties( jobKey.toString(), sparkJarsProperty);

            request.setAction("CreateSubmissionRequest");
            request.setAppResource(appResource);
            request.setMainClass(mainClass);
            request.setClientSparkVersion(SparkManagerUtil.getSparkClientVersion());
            request.setAppArgs(appArgs);
            request.setEnvironmentVariables(envVars);
            request.setSparkProperties(sparkProps);

            SparkRestApiResponse response = submitJob(request);
            log.info( "Job submission request sent. Response: {}", response);
        } catch (Throwable e) {
            log.error( e.getMessage(), e);
            throw new JobExecutionException(e);
        }
    }

    /**
     * This method get specific job arguments
     * @param jobDetail jobDetail
     * @return String array containing job arguments
     */
    public abstract String[] getAppArgs(JobDetail jobDetail);

    private String getJobArg(JobKey jobKey, JobDataMap jobDataMap, String key) {
        if (jobDataMap.containsKey(key))
            return jobDataMap.getString(key);
        else {
            log.error( ERROR_MESSAGE, new Object[]{jobKey, key});
            return null;
        }
    }

    /**
     * It returns Spark properties. Parameters appName and sparkJarsProperty are related to job which will be executed
     * @param appName Job app name
     * @param sparkJarsProperty Jars which job needs
     * @return Map containing Spark properties
     */
    private Map<String, String> getSparkProperties(String appName,
                                                   String sparkJarsProperty) {

        Map<String, String> sparkProps = new HashMap<>();
        sparkProps.put("spark.jars", sparkJarsProperty);
        sparkProps.put("spark.driver.supervise", String.valueOf(SparkManagerUtil.getSparkDriverSupervise()));
        sparkProps.put("spark.app.name", appName); // This property is mandatory as the others, however if job define its name, the latter will override the former
        sparkProps.put("spark.submit.deployMode", SparkManagerUtil.getSparkSubmitDeployMode());
        sparkProps.put("spark.master", SparkManagerUtil.getSparkRestApiUrl());
        sparkProps.put("spark.jars.packages", "org.apache.spark:spark-avro_2.11:2.4.5");    // HyperIoT Spark jobs need avro dependency to read data on which they do computation
        return sparkProps;
    }

    /**
     * It submits Spark job asynchronously
     * @param request Request to be sent to Spark hidden REST api
     * @return SparkRestApiResponse
     */
    private SparkRestApiResponse submitJob(SparkRestApiSubmissionRequest request) {
        SparkManagerSystemApi sparkManagerSystemApi =
                (SparkManagerSystemApi) HyperIoTUtil.getService(SparkManagerSystemApi.class);
        return sparkManagerSystemApi.submitJob(request);
    }

}
