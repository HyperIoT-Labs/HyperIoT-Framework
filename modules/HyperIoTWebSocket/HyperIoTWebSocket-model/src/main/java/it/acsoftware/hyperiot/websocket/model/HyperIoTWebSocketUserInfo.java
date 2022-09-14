package it.acsoftware.hyperiot.websocket.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.acsoftware.hyperiot.base.model.HyperIoTClusterNodeInfo;
import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class HyperIoTWebSocketUserInfo implements Serializable {
    @JsonIgnore
    private static Logger log = LoggerFactory.getLogger(HyperIoTWebSocketUserInfo.class);

    @JsonIgnore
    private static ObjectMapper mapper = new ObjectMapper();

    private String username;
    private HyperIoTClusterNodeInfo clusterNodeInfo;

    private String ipAddress;

    public HyperIoTWebSocketUserInfo(String username, HyperIoTClusterNodeInfo clusterNodeInfo, String ipAddress) {
        this.username = username;
        this.clusterNodeInfo = clusterNodeInfo;
        this.ipAddress = ipAddress;
    }

    private HyperIoTWebSocketUserInfo() {
    }

    public String getUsername() {
        return username;
    }


    public HyperIoTClusterNodeInfo getClusterNodeInfo() {
        return clusterNodeInfo;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HyperIoTWebSocketUserInfo that = (HyperIoTWebSocketUserInfo) o;
        return username.equals(that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    public static HyperIoTWebSocketUserInfo fromSession(String username, Session session) {
        HyperIoTClusterNodeInfo clusterInfo = HyperIoTClusterNodeInfo.getCurrentNodeNetworkInfo();
        String ipAddress = session.getRemote().getInetSocketAddress().getAddress().getHostAddress();
        HyperIoTWebSocketUserInfo userInfo = new HyperIoTWebSocketUserInfo(username, clusterInfo, ipAddress);
        return userInfo;

    }

    public static HyperIoTWebSocketUserInfo fromSession(Session session) {
        return fromSession(session.getUpgradeRequest().getUserPrincipal().getName(), session);
    }

    public static HyperIoTWebSocketUserInfo anonymous(Session session) {
        return fromSession("anonymous-" + UUID.randomUUID().toString(), session);
    }

    public static HyperIoTWebSocketUserInfo fromString(String message) {
        try {
            return mapper.readValue(message, HyperIoTWebSocketUserInfo.class);
        } catch (Throwable t) {
            log.debug("Error while parsing websocket user info: {}", new Object[]{t.getMessage()});
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
}
