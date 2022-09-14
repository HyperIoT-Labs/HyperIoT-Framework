package it.acsoftware.hyperiot.zookeeper.connector.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HyperIoTZooKeeperData {
    private static Logger log = LoggerFactory.getLogger(HyperIoTZooKeeperData.class.getName());
    private static ObjectMapper mapper = new ObjectMapper();

    private Map<String, byte[]> params;

    public HyperIoTZooKeeperData() {
        this.params = new HashMap<>();
    }

    public void addParam(String key, byte[] o) {
        this.params.put(key, o);
    }

    public void removeParam(String key) {
        this.params.remove(key);
    }

    @JsonIgnore
    public byte[] getParam(String key) {
        return this.params.get(key);
    }

    public Map<String, byte[]> getParams() {
        return params;
    }

    @JsonIgnore
    public byte[] getBytes() {
        try {
            return this.toJson().getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return new byte[]{};
    }

    public String toJson() {
        try {
            return mapper.writeValueAsString(this);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public static HyperIoTZooKeeperData fromBytes(byte[] value) {
        try {
            return mapper.readValue(value, HyperIoTZooKeeperData.class);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
}
