package it.acsoftware.hyperiot.websocket.policy;

import org.eclipse.jetty.websocket.api.Session;

import java.util.Map;

public class MaxTextMessageSizePolicy extends HyperIoTWebSocketAbstractPolicy {
    private int maxTextMessageSizePolicy;

    public MaxTextMessageSizePolicy(Session s, int maxTextMessageSizePolicy) {
        super(s);
        this.maxTextMessageSizePolicy = maxTextMessageSizePolicy;
    }

    public int getMaxTextMessageSizePolicy() {
        return maxTextMessageSizePolicy;
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
