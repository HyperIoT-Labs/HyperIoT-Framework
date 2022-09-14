package it.acsoftware.hyperiot.websocket.api.channel;

import it.acsoftware.hyperiot.websocket.api.HyperIoTWebSocketCommand;

import java.util.Set;

public interface HyperIoTWebSocketChannelRole {
    String getRoleName();

    Set<HyperIoTWebSocketCommand> getAllowedCmds();

    default boolean isOwner() {
        return false;
    }
}
