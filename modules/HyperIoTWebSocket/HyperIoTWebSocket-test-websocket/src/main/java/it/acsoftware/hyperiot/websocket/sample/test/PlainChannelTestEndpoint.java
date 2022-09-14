package it.acsoftware.hyperiot.websocket.sample.test;

import it.acsoftware.hyperiot.websocket.api.HyperIoTWebSocketEndPoint;
import it.acsoftware.hyperiot.websocket.api.HyperIoTWebSocketSession;
import it.acsoftware.hyperiot.websocket.api.channel.HyperIoTWebSocketChannelManager;
import it.acsoftware.hyperiot.websocket.channel.HyperIoTWebSocketBasicChannel;
import it.acsoftware.hyperiot.websocket.channel.manager.HyperIoTWebSocketChannelManagerFactory;
import it.acsoftware.hyperiot.websocket.channel.session.HyperIoTWebSocketChannelBasicSession;
import org.eclipse.jetty.websocket.api.Session;
import org.osgi.service.component.annotations.Component;

import java.util.concurrent.Executor;

@Component(service = HyperIoTWebSocketEndPoint.class, immediate = true)
public class PlainChannelTestEndpoint implements HyperIoTWebSocketEndPoint {
    private static HyperIoTWebSocketChannelManager manager;

    @Override
    public String getPath() {
        return "test-channel";
    }

    @Override
    public HyperIoTWebSocketSession getHandler(Session session) {
        //using basic session with no authentication
        return new HyperIoTWebSocketChannelBasicSession(session, false, getChannelManager());
    }

    @Override
    public Executor getExecutorForOpenConnections(Session s) {
        return HyperIoTWebSocketEndPoint.super.getExecutorForOpenConnections(s);
    }

    /**
     * Channel manager must be singleton inside each node.
     * @return
     */
    private static HyperIoTWebSocketChannelManager getChannelManager() {
        if (manager == null) {
            manager = HyperIoTWebSocketChannelManagerFactory.newDefaultChannelManagerFactory()
                    .loadFromOSGiContext("kafka-cluster-message-broker", "zookeeper-cluster-coordinator")
                    //channel class
                    .build(HyperIoTWebSocketBasicChannel.class);
        }
        return manager;
    }
}
