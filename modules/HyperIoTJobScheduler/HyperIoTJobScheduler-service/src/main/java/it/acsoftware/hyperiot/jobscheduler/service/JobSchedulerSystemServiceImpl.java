package it.acsoftware.hyperiot.jobscheduler.service;

import it.acsoftware.hyperiot.base.api.HyperIoTLeadershipRegistrar;
import it.acsoftware.hyperiot.base.exception.HyperIoTRuntimeException;
import it.acsoftware.hyperiot.base.service.HyperIoTBaseSystemServiceImpl;
import it.acsoftware.hyperiot.jobscheduler.api.HyperIoTJob;
import it.acsoftware.hyperiot.jobscheduler.api.JobSchedulerRepository;
import it.acsoftware.hyperiot.jobscheduler.api.JobSchedulerSystemApi;
import it.acsoftware.hyperiot.jobscheduler.leadership.registrar.JobSchedulerLeadershipRegistrar;
import it.acsoftware.hyperiot.zookeeper.connector.api.ZookeeperConnectorSystemApi;
import it.acsoftware.hyperiot.zookeeper.connector.util.HyperIoTZookeeperConstants;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * @author Aristide Cittadino Implementation class of the JobSchedulerSystemApi
 * interface. This  class is used to implements all additional
 * methods to interact with the persistence layer.
 */
@Component(service = JobSchedulerSystemApi.class, immediate = true)
public final class JobSchedulerSystemServiceImpl extends HyperIoTBaseSystemServiceImpl implements JobSchedulerSystemApi {
    private static String SQL_CREATE_TABLE_FILE = "it.acsoftware.jobscheduler.init.script";
    private HyperIoTLeadershipRegistrar jobSchedulerLeadershipRegistrar;
    private static Properties quartzProps;
    private static Properties jobschedulerProps;
    private Scheduler scheduler;
    private ZookeeperConnectorSystemApi zookeeperConnectorSystemApi;
    private JobSchedulerRepository repository;

    @Activate
    public void onActivate(BundleContext context) {
        try {
            laodProperties(context);
            //Checks wheter quartz tables exist and create themc
            this.repository.createQuartzTableIfNotExists(jobschedulerProps.getProperty(SQL_CREATE_TABLE_FILE, null));
            // Get the scheduler
            getLog().info("Get scheduler");
            StdSchedulerFactory stdSchedulerFactory = new StdSchedulerFactory(quartzProps);
            scheduler = stdSchedulerFactory.getScheduler();
            if (zookeeperConnectorSystemApi.isLeader(jobSchedulerLeadershipRegistrar.getLeadershipPath())) {
                getLog().info("Scheduler is on zk leader, start");
                scheduler.start();
            }
            addLeaderLatchListener();
        } catch (SchedulerException e) {
            getLog().error(e.getMessage(), e);
        }
    }

    @Deactivate
    public void onDeactivate() {
        try {
            if (scheduler != null)
                scheduler.shutdown();
        } catch (SchedulerException e) {
            getLog().error(e.getMessage(), e);
        }
    }

    @Override
    public void addJob(HyperIoTJob job) throws HyperIoTRuntimeException {
        JobDetail jobDetail = job.getJobDetail();
        if (jobDetail == null) {
            String errorMsg = "Could not add job: jobDetail was null";
            getLog().error(errorMsg);
            throw new HyperIoTRuntimeException(errorMsg);
        }
        JobKey jobKey = jobDetail.getKey();
        getLog().info("Adding job {} to scheduler", jobKey);
        try {
            if (!scheduler.checkExists(jobKey)) {
                // Add the the job to the store of scheduler
                scheduler.addJob(jobDetail, false);
                if (job.isActive()) {
                    // schedule the job if it has to be scheduled
                    schedule(job);
                }
            } else
                getLog().warn("Job {} already exists, it has not been added", jobKey);
        } catch (ParseException | SchedulerException e) {
            getLog().error("Could not schedule job {}: {}", new Object[]{jobKey, e.getMessage()});
            throw new HyperIoTRuntimeException(e);
        }
    }

    /**
     * This method adds a LeaderLatchListener
     */
    private void addLeaderLatchListener() {
        String leadershipPath = jobSchedulerLeadershipRegistrar.getLeadershipPath();
        zookeeperConnectorSystemApi.addListener(new LeaderLatchListener() {

            @Override
            public void isLeader() {
                getLog().info("This node has became a zk leader, start scheduler");
                try {
                    scheduler.start();
                } catch (SchedulerException e) {
                    getLog().error("Scheduler has not been started: {}", e.getMessage());
                }
            }

            @Override
            public void notLeader() {
                getLog().info("This node is not a zk leader anymore, standby scheduler");
                try {
                    scheduler.standby();
                } catch (SchedulerException e) {
                    getLog().error("Scheduler has not been paused: {}", e.getMessage());
                }
            }

        }, leadershipPath);
    }

    @Override
    public void deleteJob(HyperIoTJob job) throws HyperIoTRuntimeException {
        JobKey jobKey = job.getJobKey();
        if (jobKey == null) {
            String errorMsg = "Could not delete job: jobKey was null";
            getLog().error(errorMsg);
            throw new HyperIoTRuntimeException(errorMsg);
        }
        getLog().info("Removing job {} from scheduler", jobKey);
        try {
            if (!scheduler.checkExists(jobKey))
                getLog().warn("Job {} does not exist", jobKey);
            else
                deleteJobFromScheduler(job);
        } catch (SchedulerException e) {
            getLog().error("Job" + jobKey + " has not been removed: {}",
                    new Object[]{jobKey, e.getMessage()});
            throw new HyperIoTRuntimeException(e);
        }
    }

    private void laodProperties(BundleContext context) {
        if (quartzProps == null) {
            ServiceReference<?> configurationAdminReference = context
                    .getServiceReference(ConfigurationAdmin.class.getName());

            if (configurationAdminReference != null) {
                ConfigurationAdmin confAdmin = (ConfigurationAdmin) context
                        .getService(configurationAdminReference);
                try {
                    Configuration configuration = confAdmin
                            .getConfiguration("it.acsoftware.hyperiot.scheduler");
                    if (configuration != null && configuration.getProperties() != null) {
                        Dictionary<String, Object> dict = configuration.getProperties();
                        List<String> keys = Collections.list(dict.keys());
                        Map<String, Object> dictCopyQuartz = keys.stream().filter(key -> key.startsWith("org.quartz"))
                                .collect(Collectors.toMap(Function.identity(), dict::get));
                        Map<String, Object> dictCopyJobscheduler = keys.stream().filter(key -> !key.startsWith("org.quartz"))
                                .collect(Collectors.toMap(Function.identity(), dict::get));
                        quartzProps = new Properties();
                        jobschedulerProps = new Properties();
                        quartzProps.putAll(dictCopyQuartz);
                        jobschedulerProps.putAll(dictCopyJobscheduler);
                        getLog().debug("Loaded properties For HyperIoT: {}", quartzProps);
                        return;
                    }
                } catch (IOException e) {
                    getLog().error(
                            "Impossible to find it.acsoftware.hyperiot.scheduler.cfg, please create it!", e);
                }
            }
            getLog().error(
                    "Impossible to find it.acsoftware.hyperiot.scheduler.cfg, please create it!");
        }
    }

    /**
     * This method tells to quartz scheduler to schedule job
     *
     * @param job Job to be scheduled
     * @throws ParseException     ParseException
     * @throws SchedulerException SchedulerException
     */
    private void schedule(HyperIoTJob job)
            throws ParseException, SchedulerException {
        JobKey jobKey = job.getJobKey();
        getLog().info("Scheduling job {}", jobKey);
        String cronExpression = job.getCronExpression();
        // Validate cron expression
        CronExpression.validateExpression(cronExpression);
        // Create trigger of job
        // If job has to be updated, its trigger exists: update it too
        TriggerKey triggerKey = new TriggerKey(jobKey.getName(), jobKey.getGroup());
        TriggerBuilder triggerBuilder;
        Trigger oldTrigger = null;
        if (scheduler.checkExists(triggerKey)) {
            oldTrigger = scheduler.getTrigger(triggerKey);
            triggerBuilder = oldTrigger.getTriggerBuilder();
        } else {
            triggerBuilder = newTrigger().withIdentity(triggerKey);
        }
        Trigger trigger = triggerBuilder
                .withSchedule(cronSchedule(cronExpression).withMisfireHandlingInstructionDoNothing())
                .forJob(jobKey)
                .build();
        // Tell quartz to schedule the job using trigger set above
        if (oldTrigger == null)
            scheduler.scheduleJob(trigger);
        else
            scheduler.rescheduleJob(triggerKey, trigger);
    }

    /**
     * This method tells to quartz scheduler to remove job
     *
     * @param job Job to be removed
     */
    private void deleteJobFromScheduler(HyperIoTJob job) throws SchedulerException {
        JobKey jobKey = job.getJobKey();
        getLog().info("Unscheduling job {}", jobKey);
        if (scheduler.checkExists(jobKey)) {
            scheduler.deleteJob(jobKey);
            getLog().info("Job {} has been unscheduled successfully", jobKey);
        } else
            getLog().info("Job {} has not been scheduled yet", jobKey);
    }

    @Override
    public void updateJob(HyperIoTJob job) throws HyperIoTRuntimeException {
        JobDetail jobDetail = job.getJobDetail();
        if (jobDetail == null) {
            String errorMsg = "Could not update job: jobDetail was null";
            getLog().error(errorMsg);
            throw new HyperIoTRuntimeException(errorMsg);
        }
        JobKey jobKey = job.getJobKey();
        getLog().info("Updating job {} to scheduler", jobKey);
        try {
            if (scheduler.checkExists(jobKey)) {
                scheduler.addJob(jobDetail, true);
                if (job.isActive()) {
                    // schedule the job if it has to be scheduled
                    schedule(job);
                } else {
                    // check if the job has been scheduled previously. If so, unschedule it
                    unschedule(job);
                }
            } else
                getLog().warn("Job does not exists, it has been neither updated nor scheduled");
        } catch (ParseException | SchedulerException e) {
            getLog().error("Job {} has not been updated: ", new Object[]{jobKey, e.getMessage()});
            throw new HyperIoTRuntimeException(e);
        }
    }

    private void unschedule(HyperIoTJob job) throws SchedulerException {
        JobKey jobKey = job.getJobKey();
        TriggerKey triggerKey = new TriggerKey(jobKey.getName(), jobKey.getGroup());
        if (scheduler.checkExists(triggerKey)) {
            getLog().info("Unscheduling job {0}", jobKey);
            scheduler.unscheduleJob(triggerKey);
        } else {
            getLog().info("Job {0} has not been scheduled before, nothing to do", jobKey);
        }
    }

    @Reference
    public void setRepository(JobSchedulerRepository repository) {
        this.repository = repository;
    }

    /**
     * Injection of the related leadership registrar using basic osgi filter
     * @param jobSchedulerLeadershipRegistrar
     */
    @Reference(target = "(" + HyperIoTZookeeperConstants.ZOOKEEPER_LEADERSHIP_REGISTRAR_OSGI_FILTER + "=" + JobSchedulerLeadershipRegistrar.JOB_SCHEDULER_LEADRSHIP_REGISTRAR_OSGI_FITLER_VALUE + ")")
    public void setJobSchedulerLeadershipRegistrar(HyperIoTLeadershipRegistrar jobSchedulerLeadershipRegistrar) {
        this.jobSchedulerLeadershipRegistrar = jobSchedulerLeadershipRegistrar;
    }

    @Reference
    public void setZookeeperConnectorSystemApi(ZookeeperConnectorSystemApi zookeeperConnectorSystemApi) {
        this.zookeeperConnectorSystemApi = zookeeperConnectorSystemApi;
    }

}
