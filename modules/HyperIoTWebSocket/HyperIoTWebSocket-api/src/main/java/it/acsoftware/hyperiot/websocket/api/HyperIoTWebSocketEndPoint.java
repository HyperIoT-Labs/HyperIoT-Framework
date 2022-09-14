package it.acsoftware.hyperiot.websocket.api;

import org.eclipse.jetty.websocket.api.Session;

import java.util.concurrent.Executor;

/**
 * Author Aristide Cittadino
 */
public interface HyperIoTWebSocketEndPoint {
    /**
     * @return Endpoint path
     */
    String getPath();

    /**
     * @param session
     * @return WebSocket Handler for specific Session
     */
    HyperIoTWebSocketSession getHandler(Session session);

    /**
     * This method should be used if you want to use different threads and executors with different types of opening connections
     * In order to avoid starvation.
     * Leave null for default Executor.
     *
     * @return
     */
    default Executor getExecutorForOpenConnections(Session s) {
        return null;
    }
}
