package it.acsoftware.hyperiot.websocket.policy;

import org.eclipse.jetty.websocket.api.Session;

import java.util.Map;

public class MaxTextMessageBufferSizePolicy extends HyperIoTWebSocketAbstractPolicy {
    private int maxTextMessageBufferSize;

    public MaxTextMessageBufferSizePolicy(Session s, int maxTextMessageBufferSize) {
        super(s);
        this.maxTextMessageBufferSize = maxTextMessageBufferSize;
    }

    public int getMaxTextMessageBufferSize() {
        return maxTextMessageBufferSize;
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
