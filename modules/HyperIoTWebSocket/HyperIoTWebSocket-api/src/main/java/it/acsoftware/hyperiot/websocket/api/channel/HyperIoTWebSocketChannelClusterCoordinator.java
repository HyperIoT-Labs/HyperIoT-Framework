package it.acsoftware.hyperiot.websocket.api.channel;

import it.acsoftware.hyperiot.websocket.model.HyperIoTWebSocketUserInfo;

import java.util.Map;
import java.util.Set;

public interface HyperIoTWebSocketChannelClusterCoordinator {
    /**
     * Connecting new peer means receive current channel list
     * @param channelManager
     * @return
     */
    Map<String, HyperIoTWebSocketChannel> connectNewPeer(HyperIoTWebSocketChannelManager channelManager);

    /**
     * Disconnects
     */
    void disconnectPeer();

    /**
     * @param channel
     */
    void notifyChannelAdded(HyperIoTWebSocketChannel channel);

    /**
     * @param channelId
     */
    void notifyChannelDeleted(String channelId);

    /**
     * @param channelId
     * @param partecipantInfo
     * @param roles
     */
    void notifyPartecipantAdded(String channelId, HyperIoTWebSocketUserInfo partecipantInfo, Set<HyperIoTWebSocketChannelRole> roles);

    /**
     * @param channelId
     * @param partecipantInfo
     */
    void notifyPartecipantGone(String channelId, HyperIoTWebSocketUserInfo partecipantInfo);

    /**
     * @param channelId
     * @param partecipantInfo
     */
    void notifyPartecipantDisconnected(String channelId, HyperIoTWebSocketUserInfo partecipantInfo);

    /**
     *
     * @return the current channel manager
     */
    HyperIoTWebSocketChannelManager getRegisteredWebSocketChannelManager();

}
