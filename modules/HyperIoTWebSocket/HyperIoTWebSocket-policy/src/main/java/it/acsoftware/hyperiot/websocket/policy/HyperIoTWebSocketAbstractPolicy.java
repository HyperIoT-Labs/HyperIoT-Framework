package it.acsoftware.hyperiot.websocket.policy;

import it.acsoftware.hyperiot.websocket.api.HyperIoTWebSocketPolicy;
import org.eclipse.jetty.websocket.api.Session;

import java.util.Map;
import java.util.Objects;

public abstract class HyperIoTWebSocketAbstractPolicy implements HyperIoTWebSocketPolicy {
    private Session session;

    public HyperIoTWebSocketAbstractPolicy(Session s) {
        this.session = s;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HyperIoTWebSocketAbstractPolicy that = (HyperIoTWebSocketAbstractPolicy) o;
        return session.equals(that.session);
    }

    @Override
    public int hashCode() {
        return Objects.hash(session);
    }

    @Override
    public abstract boolean isSatisfied(Map<String, Object> params, byte[] payload);
}
