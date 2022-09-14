package it.acsoftware.hyperiot.base.api;

/**
 * @author Aristide Cittadino
 * <p>
 * Class that implements the concept of action within the platform.
 * Every interaction the users has with HyperIoT it will be related to
 * actions. Each Action is managed by Permissions
 */
public interface HyperIoTAction {

    /**
     * Gets the resource name of action
     *
     * @return resource name
     */
    public String getResourceName();

    /**
     * Gets the action name
     *
     * @return action name
     */
    public String getActionName();

    /**
     * Gets the action id
     *
     * @return action id
     */
    public int getActionId();

    /**
     * @param actionId the action id to be set
     */
    public void setActionId(int actionId);

    /**
     * Gets category of action
     *
     * @return action category
     */
    public String getCategory();

    boolean isRegistered();

    void setRegistered(boolean registered);
}
