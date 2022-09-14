package it.acsoftware.hyperiot.sparkmanager.actions;

import it.acsoftware.hyperiot.base.action.HyperIoTActionName;

/**
 * @author Aristide Cittadino Model class that enumerate SparkManager Actions
 */
public enum SparkManagerAction implements HyperIoTActionName {

    GET_JOB_STATUS(Names.GET_JOB_STATUS),
    KILL_JOB(Names.KILL_JOB),
    SUBMIT_JOB(Names.SUBMIT_JOB);

    private String name;

    /**
     * Role Action with the specified name.
     *
     * @param name parameter that represent the SparkManager  action
     */
    SparkManagerAction(String name) {
        this.name = name;
    }

    /**
     * Gets the name of SparkManager action
     */
    public String getName() {
        return name;
    }

    public class Names {
        public static final String GET_JOB_STATUS = "get_job_status";
        public static final String KILL_JOB = "kill_job";
        public static final String SUBMIT_JOB = "submit_job";
    }

}
