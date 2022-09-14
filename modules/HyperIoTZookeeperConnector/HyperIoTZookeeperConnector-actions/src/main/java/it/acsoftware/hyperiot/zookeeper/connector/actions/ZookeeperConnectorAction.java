package it.acsoftware.hyperiot.zookeeper.connector.actions;

import it.acsoftware.hyperiot.base.action.HyperIoTActionName;

/**
 * @author Aristide Cittadino Model class that enumerate ZookeeperConnector Actions
 */
public enum ZookeeperConnectorAction implements HyperIoTActionName {

    CHECK_LEADERSHIP(Names.CHECK_LEADERSHIP);

    private String name;

    /**
     * Role Action with the specified name.
     *
     * @param name parameter that represent the ZookeeperConnector  action
     */
    private ZookeeperConnectorAction(String name) {
        this.name = name;
    }

    /**
     * Gets the name of ZookeeperConnector action
     */
    public String getName() {
        return name;
    }

    public class Names {
        public static final String CHECK_LEADERSHIP = "check_leadership";
    }

}
