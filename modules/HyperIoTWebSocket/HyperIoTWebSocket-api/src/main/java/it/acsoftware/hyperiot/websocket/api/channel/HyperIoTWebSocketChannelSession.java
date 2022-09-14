package it.acsoftware.hyperiot.websocket.api.channel;

import it.acsoftware.hyperiot.websocket.api.HyperIoTWebSocketSession;
import it.acsoftware.hyperiot.websocket.model.HyperIoTWebSocketUserInfo;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public interface HyperIoTWebSocketChannelSession extends HyperIoTWebSocketSession {

    void addSessionParam(String name, Object value);

    Object getSessionParam(String name);

    void removeSessionParam(String name);

    void updateEncryptionPolicyParams(Map<String, Object> params);

    void close(String message);

    HyperIoTWebSocketUserInfo getUserInfo();

    void processMessage(byte[] rawMessage);

    void addJoinedChannels(HyperIoTWebSocketChannel channel);

    void removeJoinedChannels(HyperIoTWebSocketChannel channel);

    void emptyJoinedChannels(HyperIoTWebSocketChannel channel);

    Set<HyperIoTWebSocketChannel> getJoinedChannels();
}
