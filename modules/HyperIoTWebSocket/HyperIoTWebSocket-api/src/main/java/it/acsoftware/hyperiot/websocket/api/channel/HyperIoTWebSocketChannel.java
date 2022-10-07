package it.acsoftware.hyperiot.websocket.api.channel;

import it.acsoftware.hyperiot.base.model.HyperIoTClusterNodeInfo;
import it.acsoftware.hyperiot.websocket.api.HyperIoTWebSocketCommand;
import it.acsoftware.hyperiot.websocket.api.HyperIoTWebSocketSession;
import it.acsoftware.hyperiot.websocket.model.HyperIoTWebSocketUserInfo;
import it.acsoftware.hyperiot.websocket.model.message.HyperIoTWebSocketMessage;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface HyperIoTWebSocketChannel {

    String getChannelId();

    String getChannelName();

    void defineClusterMessageBroker(HyperIoTWebSocketChannelClusterMessageBroker clusterMessageBroker);

    Map<String, Object> getChannelParams();

    Object getChannelParam(String name);

    void addChannelParam(String name, Object value);

    void removeChannelParam(String name);

    Optional<HyperIoTWebSocketUserInfo> findUserInfo(String usedId);

    Set<HyperIoTWebSocketUserInfo> getPartecipantsInfo();

    Optional<HyperIoTWebSocketUserInfo> findPartecipantInfoFromUserId(String userId);

    void addPartecipantInfo(HyperIoTWebSocketUserInfo partecipantInfo, Set<HyperIoTWebSocketChannelRole> roles);

    void addPartecipantSession(HyperIoTWebSocketUserInfo partecipantInfo, Set<HyperIoTWebSocketChannelRole> roles, HyperIoTWebSocketChannelSession session);

    void leaveChannel(HyperIoTWebSocketUserInfo participantInfo);

    HyperIoTWebSocketSession getPartecipantSession(HyperIoTWebSocketUserInfo partecipantInfo);

    boolean hasPartecipantSession(HyperIoTWebSocketUserInfo partecipantInfo);

    void kickPartecipant(HyperIoTWebSocketUserInfo kickerInfo, HyperIoTWebSocketMessage kickMessageCommand);

    void banPartecipant(HyperIoTWebSocketUserInfo banner,  HyperIoTWebSocketMessage banMessageCommand);

    void unbanPartecipant(HyperIoTWebSocketUserInfo banner, HyperIoTWebSocketMessage unbanMessageCommand);

    void removePartecipant(HyperIoTWebSocketUserInfo partecipantInfo);

    boolean userHasPermission(HyperIoTWebSocketUserInfo user, HyperIoTWebSocketCommand commandType);

    Set<HyperIoTClusterNodeInfo> getPeers();

    /**
     * Send the message through multiple session even across the cluster on different nodes
     *
     * @param senderSession
     * @param message
     */
    void exchangeMessage(HyperIoTWebSocketChannelSession senderSession, HyperIoTWebSocketMessage message);

    /**
     * Deliver current message on local sessions on the current node on which message has been received
     *
     * @param sender
     * @param message
     */
    void deliverMessage(HyperIoTWebSocketUserInfo sender, HyperIoTWebSocketMessage message);

    /**
     * @param senderSession
     * @param message
     * @return
     */
    void receiveMessageForServer(HyperIoTWebSocketChannelSession senderSession, HyperIoTWebSocketMessage message);

    String toJson();
}
