package it.acsoftware.hyperiot.websocket.api.channel;

import it.acsoftware.hyperiot.websocket.model.message.HyperIoTWebSocketMessage;

public interface HyperIoTWebSocketChannelClusterMessageBroker {
    void sendMessage(String channelId, HyperIoTWebSocketMessage message);
    void registerChannelManager(HyperIoTWebSocketChannelManager manager);
}
