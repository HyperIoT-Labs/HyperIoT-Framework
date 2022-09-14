package it.acsoftware.hyperiot.base.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.acsoftware.hyperiot.base.util.HyperIoTUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class HyperIoTClusterNodeInfo {
    @JsonIgnore
    private static Logger log = LoggerFactory.getLogger(HyperIoTClusterNodeInfo.class);

    @JsonIgnore
    private static ObjectMapper mapper = new ObjectMapper();

    @JsonIgnore
    private static HyperIoTClusterNodeInfo currentNodeNetworkInfo;

    static {
        currentNodeNetworkInfo = new HyperIoTClusterNodeInfo(HyperIoTUtil.getNodeId(), HyperIoTUtil.getLayer());
    }

    //tells on which node of the cluster (potentially) the user is connected to
    private String nodeId;
    private String layerId;

    public HyperIoTClusterNodeInfo(String nodeId, String layerId) {
        this.nodeId = nodeId;
        this.layerId = layerId;
    }

    private HyperIoTClusterNodeInfo(){}

    public String getNodeId() {
        return nodeId;
    }

    public String getLayerId() {
        return layerId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HyperIoTClusterNodeInfo that = (HyperIoTClusterNodeInfo) o;
        return nodeId.equals(that.nodeId) && layerId.equals(that.layerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeId, layerId);
    }

    @JsonIgnore
    public boolean isOnLocalNode() {
        return this.getNodeId().equalsIgnoreCase(HyperIoTUtil.getNodeId()) && this.getLayerId().equalsIgnoreCase(HyperIoTUtil.getLayer());
    }

    public static HyperIoTClusterNodeInfo fromString(String message) {
        try {
            return mapper.readValue(message, HyperIoTClusterNodeInfo.class);
        } catch (Throwable t) {
            log.debug("Error while parsing websocket network info: {}", new Object[]{t.getMessage()});
        }
        return null;
    }

    public String toJson() {
        try {
            return mapper.writeValueAsString(this);
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
        }
        return "{}";
    }

    public static HyperIoTClusterNodeInfo getCurrentNodeNetworkInfo() {
        return currentNodeNetworkInfo;
    }
}
