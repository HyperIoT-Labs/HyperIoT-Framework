/*
 * Copyright 2019-2023 HyperIoT
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package it.acsoftware.hyperiot.websocket.channel.session;

import it.acsoftware.hyperiot.websocket.api.HyperIoTWebSocketBasicCommandType;
import it.acsoftware.hyperiot.websocket.api.channel.HyperIoTWebSocketChannel;
import it.acsoftware.hyperiot.websocket.api.channel.HyperIoTWebSocketChannelCommand;
import it.acsoftware.hyperiot.websocket.api.channel.HyperIoTWebSocketChannelManager;
import it.acsoftware.hyperiot.websocket.api.channel.HyperIoTWebSocketChannelSession;
import it.acsoftware.hyperiot.websocket.channel.command.HyperIoTWebSocketChannelCommandFactory;
import it.acsoftware.hyperiot.websocket.channel.util.HyperIoTWebSocketChannelConstants;
import it.acsoftware.hyperiot.websocket.compression.HyperIoTWebSocketCompression;
import it.acsoftware.hyperiot.websocket.encryption.HyperIoTWebSocketEncryption;
import it.acsoftware.hyperiot.websocket.model.HyperIoTWebSocketConstants;
import it.acsoftware.hyperiot.websocket.model.message.HyperIoTWebSocketMessage;
import it.acsoftware.hyperiot.websocket.model.message.HyperIoTWebSocketMessageType;
import it.acsoftware.hyperiot.websocket.session.HyperIoTWebSocketAbstractSession;
import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @Author Aristide Cittadino
 */
public class HyperIoTWebSocketChannelBasicSession extends HyperIoTWebSocketAbstractSession implements HyperIoTWebSocketChannelSession {
    private static Logger log = LoggerFactory.getLogger(HyperIoTWebSocketChannelBasicSession.class);

    private HyperIoTWebSocketChannelManager channelManager;
    private Map<String, Object> sessionParams;
    private Set<HyperIoTWebSocketChannel> joinedChannels;

    public HyperIoTWebSocketChannelBasicSession(Session session, boolean authenticationRequired, HyperIoTWebSocketChannelManager channelManager) {
        super(session, authenticationRequired);
        initUserSession(channelManager);
    }

    public HyperIoTWebSocketChannelBasicSession(Session session, boolean authenticated, HyperIoTWebSocketEncryption encryptionPolicy, HyperIoTWebSocketChannelManager channelManager) {
        super(session, authenticated, encryptionPolicy);
        initUserSession(channelManager);
    }

    public HyperIoTWebSocketChannelBasicSession(Session session, boolean authenticated, HyperIoTWebSocketCompression compressionPolicy, HyperIoTWebSocketChannelManager channelManager) {
        super(session, authenticated, compressionPolicy);
        initUserSession(channelManager);
    }

    public HyperIoTWebSocketChannelBasicSession(Session session, boolean authenticated, HyperIoTWebSocketEncryption encryptionPolicy, HyperIoTWebSocketCompression compressionPolicy, HyperIoTWebSocketChannelManager channelManager) {
        super(session, authenticated, encryptionPolicy, compressionPolicy);
        initUserSession(channelManager);
    }

    private void initUserSession(HyperIoTWebSocketChannelManager channelManager) {
        this.channelManager = channelManager;
        this.sessionParams = new HashMap<>();
        this.joinedChannels = new HashSet<>();
    }

    @Override
    public void initialize() {
        boolean closeSession = false;
        String closeMessage = null;

        if (this.isAuthenticationRequired() && !this.isAuthenticated()) {
            closeSession = true;
            closeMessage = "Not authenticated,closing session";
        }

        if (closeSession) {
            this.close(closeMessage);
            return;
        }

        this.onConnect();
    }

    @Override
    public void addSessionParam(String name, Object value) {
        this.sessionParams.put(name, value);
    }

    @Override
    public Object getSessionParam(String name) {
        return this.sessionParams.get(name);
    }

    @Override
    public void removeSessionParam(String name) {
        this.sessionParams.remove(name);
    }

    @Override
    public void onMessage(String message) {
        byte[] rawMessage = this.getMessageBroker().readRaw(message);
        this.processMessage(rawMessage);
    }

    public void processMessage(byte[] rawMessage) {
        String rawMessageStr = new String(rawMessage);
        HyperIoTWebSocketMessage wsMessage = HyperIoTWebSocketMessage.fromString(rawMessageStr);

        if (wsMessage == null) {
            String errorMessageStr = "Impossible to deserialize message:\n " + rawMessageStr;
            HyperIoTWebSocketMessage errorMessage = HyperIoTWebSocketMessage.createMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, errorMessageStr.getBytes(StandardCharsets.UTF_8), HyperIoTWebSocketMessageType.ERROR);
            this.sendRemote(errorMessage);
            return;
        }

        String channelId = null;
        if (wsMessage.getParams() != null && wsMessage.getParams().containsKey(HyperIoTWebSocketChannelConstants.CHANNEL_ID_PARAM))
            channelId = wsMessage.getParams().get(HyperIoTWebSocketChannelConstants.CHANNEL_ID_PARAM);

        //forcing sender to be the user associated with the current session
        if (wsMessage.getParams() == null)
            wsMessage.setParams(new HashMap<>());
        wsMessage.getParams().put(HyperIoTWebSocketMessage.WS_MESSAGE_SENDER_PARAM_NAME, this.getUserInfo().getUsername());
        HyperIoTWebSocketChannelCommand command = HyperIoTWebSocketChannelCommandFactory.createCommand(wsMessage.getCmd());
        try {
            command.execute(this, wsMessage, channelId, this.channelManager);
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
            String errorMessageStr = (t.getMessage() != null) ? t.getMessage() : "";
            HyperIoTWebSocketMessage errorMessage = HyperIoTWebSocketMessage.createMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, errorMessageStr.getBytes(StandardCharsets.UTF_8), HyperIoTWebSocketMessageType.ERROR);
            this.sendRemote(errorMessage);
        }
    }

    @Override
    public void dispose() {
        HyperIoTWebSocketMessage m = HyperIoTWebSocketMessage.createMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE.toString(), "Disconnecting...".getBytes(), HyperIoTWebSocketMessageType.DISCONNECTING);
        this.sendRemote(m);
        this.getMessageBroker().onCloseSession(this.getSession());
        super.dispose();
        this.onClose();
    }

    public void close(String closeMessage) {
        if (closeMessage == null)
            closeMessage = "";
        HyperIoTWebSocketMessage m = HyperIoTWebSocketMessage.createMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE.toString(), closeMessage.getBytes(), HyperIoTWebSocketMessageType.ERROR);
        this.sendRemote(m);
        //forcing to close connection
        log.info("Closing session because: {}", closeMessage);
        this.dispose();
    }

    public void addJoinedChannels(HyperIoTWebSocketChannel channel) {
        this.joinedChannels.add(channel);
    }

    public void removeJoinedChannels(HyperIoTWebSocketChannel channel) {
        this.joinedChannels.remove(channel);
    }

    public void emptyJoinedChannels(HyperIoTWebSocketChannel channel) {
        this.joinedChannels.clear();
    }

    public Set<HyperIoTWebSocketChannel> getJoinedChannels() {
        return Collections.unmodifiableSet(this.joinedChannels);
    }

    protected void sendConnectionOkMessage(String message) {
        if (message == null)
            message = "";
        HyperIoTWebSocketMessage m = HyperIoTWebSocketMessage.createMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE.toString(), message.getBytes(), HyperIoTWebSocketMessageType.CONNECTION_OK);
        HashMap<String, String> params = new HashMap<>();
        params.put(HyperIoTWebSocketConstants.WEB_SOCKET_USERNAME_PARAM, this.getUserInfo().getUsername());
        m.setParams(params);
        this.sendRemote(m);
    }

    protected void onConnect() {
        this.sendConnectionOkMessage("Connected!");
    }

    protected void onClose() {
        this.joinedChannels.parallelStream().forEach(channel -> {
            this.channelManager.leaveChannel(channel.getChannelId(), this);
        });
        HyperIoTWebSocketMessage m = HyperIoTWebSocketMessage.createMessage(null, "partecipant is disconnecting...".getBytes(), HyperIoTWebSocketMessageType.PARTICIPANT_GONE);
        this.sendRemote(m);
    }

    @Override
    public void updateEncryptionPolicyParams(Map<String, Object> params) {
        this.getMessageBroker().updateEncryptionPolicyParams(params);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HyperIoTWebSocketChannelBasicSession that = (HyperIoTWebSocketChannelBasicSession) o;
        return Objects.equals(getSession(), that.getSession()) && Objects.equals(this.getUserInfo(), that.getUserInfo());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSession(), this.getUserInfo());
    }

}
