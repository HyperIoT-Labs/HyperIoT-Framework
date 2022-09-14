package it.acsoftware.hyperiot.kafka.connector.model;

import java.util.HashMap;
import java.util.Objects;

public class ACLConfig {

    private String username;
    private HashMap<String, HyperIoTKafkaPermission> permissions;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public HashMap<String, HyperIoTKafkaPermission> getPermissions() {
        return permissions;
    }

    public void setPermissions(HashMap<String, HyperIoTKafkaPermission> permissions) {
        this.permissions = permissions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ACLConfig)) return false;
        ACLConfig aclConfig = (ACLConfig) o;
        return getUsername().equals(aclConfig.getUsername()) &&
                getPermissions().equals(aclConfig.getPermissions());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUsername(), getPermissions());
    }
}
