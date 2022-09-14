package it.acsoftware.hyperiot.sparkmanager.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SparkRestApiResponse {

    private String action;
    private String driverState;
    private String message;
    private boolean success;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDriverState() {
        return driverState;
    }

    public void setDriverState(String driverState) {
        this.driverState = driverState;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public String toString() {
        return "SparkRestApiResponse{" +
                "action='" + action + '\'' +
                ", driverState='" + driverState + '\'' +
                ", message='" + message + '\'' +
                ", success=" + success +
                '}';
    }
}
