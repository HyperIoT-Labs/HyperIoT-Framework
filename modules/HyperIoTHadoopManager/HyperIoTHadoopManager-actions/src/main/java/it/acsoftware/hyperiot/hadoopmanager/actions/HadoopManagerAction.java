package it.acsoftware.hyperiot.hadoopmanager.actions;

import it.acsoftware.hyperiot.base.action.HyperIoTActionName;

/**
 * @author Aristide Cittadino Model class that enumerate HadoopManager Actions
 */
public enum HadoopManagerAction implements HyperIoTActionName {

    COPY_FILE(Names.COPY_FILE),
    DELETE_FILE(Names.DELETE_FILE);

    private String name;

    /**
     * Role Action with the specified name.
     *
     * @param name parameter that represent the HadoopManager  action
     */
    private HadoopManagerAction(String name) {
        this.name = name;
    }

    /**
     * Gets the name of HadoopManager action
     */
    public String getName() {
        return name;
    }

    public class Names {
        public static final String COPY_FILE = "copy_file";
        public static final String DELETE_FILE = "delete_file";
    }

}
