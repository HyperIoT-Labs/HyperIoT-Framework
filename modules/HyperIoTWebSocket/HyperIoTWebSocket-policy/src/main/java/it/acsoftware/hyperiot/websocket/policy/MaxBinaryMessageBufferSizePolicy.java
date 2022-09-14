package it.acsoftware.hyperiot.websocket.policy;

import org.eclipse.jetty.websocket.api.Session;

import java.util.Map;

public class MaxBinaryMessageBufferSizePolicy extends HyperIoTWebSocketAbstractPolicy  {
    private int maxBinaryMessageBufferSize;

    public MaxBinaryMessageBufferSizePolicy(Session s, int maxBinaryMessageBufferSize) {
        super(s);
        this.maxBinaryMessageBufferSize = maxBinaryMessageBufferSize;
    }

    public int getMaxBinaryMessageBufferSize() {
        return maxBinaryMessageBufferSize;
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
