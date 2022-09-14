package it.acsoftware.hyperiot.websocket.api.channel;

import it.acsoftware.hyperiot.websocket.model.message.HyperIoTWebSocketMessage;

public interface HyperIoTWebSocketChannelCommand {
    void execute(HyperIoTWebSocketChannelSession userSession, HyperIoTWebSocketMessage message, String channelId, HyperIoTWebSocketChannelManager manager);
}
