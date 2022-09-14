package it.acsoftware.hyperiot.websocket.policy;

import org.eclipse.jetty.websocket.api.Session;

import java.util.Map;

public class MaxBinaryMessageSizePolicy extends HyperIoTWebSocketAbstractPolicy {
    private int maxBinaryMessageSize;

    public MaxBinaryMessageSizePolicy(Session s, int maxBinaryMessageSize) {
        super(s);
        this.maxBinaryMessageSize = maxBinaryMessageSize;
    }

    public int getMaxBinaryMessageSize() {
        return maxBinaryMessageSize;
    }

    @Override
    public boolean isSatisfied(Map<String, Object> params, byte[] payload) {
        return true;
    }

    @Override
    public boolean closeWebSocketOnFail() {
        return false;
    }

    @Override
    public boolean printWarningOnFail() {
        return false;
    }

    @Override
    public boolean sendWarningBackToClientOnFail() {
        return false;
    }

    @Override
    public boolean ignoreMessageOnFail() {
        return false;
    }
}
