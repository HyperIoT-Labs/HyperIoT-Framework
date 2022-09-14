package it.acsoftware.hyperiot.websocket.policy;

import org.eclipse.jetty.websocket.api.Session;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MaxPayloadBytesPolicy extends HyperIoTWebSocketAbstractPolicy {
    private static Logger log = LoggerFactory.getLogger(MaxPayloadBytesPolicy.class.getName());
    private int maxPayloadBytes;


    public MaxPayloadBytesPolicy(Session s, int maxPayloadBytes) {
        super(s);
        this.maxPayloadBytes = maxPayloadBytes;
    }

    @Override
    public boolean closeWebSocketOnFail() {
        return false;
    }

    @Override
    public boolean printWarningOnFail() {
        return true;
    }

    @Override
    public boolean sendWarningBackToClientOnFail() {
        return true;
    }

    @Override
    public boolean ignoreMessageOnFail() {
        return true;
    }

    @Override
    public boolean isSatisfied(Map<String, Object> params, byte[] payload) {
        log.debug( "Policy Max Payload bytes, current payload is: {}, max is {}", new Object[]{payload.length, maxPayloadBytes});
        if (payload.length > maxPayloadBytes) {
            return false;
        }
        return true;
    }
}
