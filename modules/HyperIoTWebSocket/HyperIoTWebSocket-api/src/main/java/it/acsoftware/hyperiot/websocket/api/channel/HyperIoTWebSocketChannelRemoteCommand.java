package it.acsoftware.hyperiot.websocket.api.channel;

import it.acsoftware.hyperiot.websocket.model.message.HyperIoTWebSocketMessage;

public interface HyperIoTWebSocketChannelRemoteCommand extends HyperIoTWebSocketChannelCommand {
    @Override
    default void execute(HyperIoTWebSocketChannelSession userSession, HyperIoTWebSocketMessage message, String channelId, HyperIoTWebSocketChannelManager manager) {
        //not passing user session since in remote mode, session might not be available
        execute(message, channelId, manager);
    }

    void execute(HyperIoTWebSocketMessage message, String channelId, HyperIoTWebSocketChannelManager manager);
}
