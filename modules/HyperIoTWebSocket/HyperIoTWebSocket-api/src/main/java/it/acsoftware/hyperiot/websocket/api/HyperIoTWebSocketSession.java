package it.acsoftware.hyperiot.websocket.api;

import it.acsoftware.hyperiot.websocket.model.HyperIoTWebSocketUserInfo;
import it.acsoftware.hyperiot.websocket.model.message.HyperIoTWebSocketMessage;
import org.eclipse.jetty.websocket.api.Session;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Author Aristide Cittadino
 */
public interface HyperIoTWebSocketSession {

    /**
     * @return true if this websocket requires authentication
     */
    boolean isAuthenticationRequired();

    /**
     * Method which implements authentication for websocket
     */
    void authenticate();

    /**
     * Method which defines how anonymous users should be tracked
     */
    void authenticateAnonymous();

    /**
     * @return WebSocket Session
     */
    Session getSession();

    /**
     * Use this method to insert custom initialization code, on websocket open event
     */
    void initialize();

    /**
     * @param message
     */
    void onMessage(String message);

    /**
     *
     */
    void dispose();

    /**
     *
     */
    void sendRemote(HyperIoTWebSocketMessage m);

    /**
     * @param message
     */
    void close(String message);

    /**
     * @return
     */
    HyperIoTWebSocketUserInfo getUserInfo();

    /**
     * @return Web Socket Policy Params to check
     */
    default Map<String, Object> getPolicyParams() {
        return null;
    }

    /**
     * @return The Policy List for the created websocket instance
     */
    default List<HyperIoTWebSocketPolicy> getWebScoketPolicies() {
        return Collections.emptyList();
    }
}
