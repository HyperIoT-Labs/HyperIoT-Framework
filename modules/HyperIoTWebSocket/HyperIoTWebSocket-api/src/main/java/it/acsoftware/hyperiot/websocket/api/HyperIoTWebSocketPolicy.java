package it.acsoftware.hyperiot.websocket.api;

import java.util.Map;

/**
 * Author Aristide Cittadino
 * Interface which maps the concept of WebSocket Policy in order to define rules in how the websocket should work.
 * Example policies are: max message per second, max message size, ecc...
 */
public interface HyperIoTWebSocketPolicy {

    boolean isSatisfied(Map<String, Object> params, byte[] payload);

    default boolean closeWebSocketOnFail() {
        return true;
    }

    default boolean printWarningOnFail() {
        return true;
    }

    default boolean sendWarningBackToClientOnFail() {
        return false;
    }

    default boolean ignoreMessageOnFail() {
        return false;
    }

}
