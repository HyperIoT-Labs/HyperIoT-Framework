package it.acsoftware.hyperiot.websocket.test.client;

import org.eclipse.jetty.websocket.client.WebSocketClient;

public class HyperIoTChannelWebSocketClient extends WebSocketClient {
    public void stopClient() throws Exception {
        this.stop();
    }
}
