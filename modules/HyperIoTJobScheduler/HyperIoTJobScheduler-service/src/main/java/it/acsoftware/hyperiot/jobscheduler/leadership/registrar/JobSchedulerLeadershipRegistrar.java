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

package it.acsoftware.hyperiot.jobscheduler.leadership.registrar;

import it.acsoftware.hyperiot.base.api.HyperIoTLeadershipRegistrar;
import it.acsoftware.hyperiot.base.util.HyperIoTUtil;
import it.acsoftware.hyperiot.zookeeper.connector.util.HyperIoTZookeeperConstants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true,property = {
        HyperIoTZookeeperConstants.ZOOKEEPER_LEADERSHIP_REGISTRAR_OSGI_FILTER+"="+JobSchedulerLeadershipRegistrar.JOB_SCHEDULER_LEADRSHIP_REGISTRAR_OSGI_FITLER_VALUE
})
public class JobSchedulerLeadershipRegistrar implements HyperIoTLeadershipRegistrar {
    private static Logger logger = LoggerFactory.getLogger(JobSchedulerLeadershipRegistrar.class);
    private static final String LEADERSHIP_PATH = "/"+HyperIoTUtil.getLayer() + "/jobs/quartz/executor";
    public static final String JOB_SCHEDULER_LEADRSHIP_REGISTRAR_OSGI_FITLER_VALUE = "hyperiot-quartz-leadership-registrar";

    @Override
    public String getLeadershipPath() {
        logger.info("*** HYPERIOT JOB SCHEUDLER CLUSTER SCHEDULER LEADERSHIP PATH: {}***",LEADERSHIP_PATH);
        return LEADERSHIP_PATH;
    }

}
