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

package it.acsoftware.hyperiot.jobscheduler.api;

import org.quartz.JobDetail;
import org.quartz.JobKey;

import java.util.Map;

/**
 * This interface put the attention on mandatory information required by Quartz scheduler
 */
public interface HyperIoTJob {

    /**
     * It returns implementation classname of org.quartz.Job interface
     * @return implementation classname of org.quartz.Job interface
     */
    String getClassName();

    /**
     * It returns job cron expression
     * @return job cron expression
     */
    String getCronExpression();

    /**
     * It returns detail of job, i.e. an instance of org.quartz.JobDetail class
     * @return Detail of job
     */
    JobDetail getJobDetail();

    /**
     * It returns identification of Job, i.e. an instance of org.quartz.JobKey class
     * @return Key of job
     */
    JobKey getJobKey();

    /**
     * It returns job parameters
     * @return Job parameters
     */
    Map<String, Object> getJobParams();

    /**
     * It tells if this job has to be scheduled or not
     * @return true if it is active, false otherwise
     */
    boolean isActive();

}
