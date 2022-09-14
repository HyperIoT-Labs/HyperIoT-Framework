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
