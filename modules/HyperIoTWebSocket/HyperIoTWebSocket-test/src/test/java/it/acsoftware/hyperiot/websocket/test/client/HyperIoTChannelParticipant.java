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

package it.acsoftware.hyperiot.websocket.test.client;

import it.acsoftware.hyperiot.websocket.api.HyperIoTWebSocketBasicCommandType;
import it.acsoftware.hyperiot.websocket.channel.HyperIoTWebSocketChannelType;
import it.acsoftware.hyperiot.websocket.channel.command.HyperIoTWebSocketChannelCommandType;
import it.acsoftware.hyperiot.websocket.channel.util.HyperIoTWebSocketChannelConstants;
import it.acsoftware.hyperiot.websocket.model.message.HyperIoTWebSocketMessageType;
import it.acsoftware.hyperiot.websocket.test.WebSocketUtils;
import org.awaitility.Awaitility;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Stack;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public abstract class HyperIoTChannelParticipant {
    private static Logger log = LoggerFactory.getLogger(HyperIoTChannelParticipant.class);

    private HyperIoTChannelWebSocketClient client;
    private ThreadPoolExecutor executor;
    private String websocketBaseUrl;
    private boolean verbose;
    private boolean connected;
    private String authToken;
    private HyperIoTChannelWebSocket webSocket;
    private Session currentSession;

    private String alias;

    private String username;

    //buffer used to check whether messages have been received or not
    private Stack<JSONObject> buffer;

    public HyperIoTChannelParticipant(String alias, String websocketBaseUrl, HyperIoTChannelWebSocketClient client, int numThreads, boolean verbose) {
        this.websocketBaseUrl = websocketBaseUrl;
        this.client = client;
        this.executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(numThreads);
        this.verbose = verbose;
        this.connected = false;
        this.buffer = new Stack<>();
        this.alias = alias;
        try {
            client.start();
            this.webSocket = new HyperIoTChannelWebSocket(HyperIoTChannelParticipant.this, executor);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void withAuthToken(String token) {
        this.authToken = token;
    }

    public void onConnected(Session s) {
        this.connected = true;
        this.currentSession = s;
    }

    public void awaitForMessage(String cmd, HyperIoTWebSocketMessageType messageType, String payload) {
        Awaitility.await().until(() -> {
            return this.checkMessageReceived(cmd, messageType.name(), payload);
        });
    }

    public String getUsername() {
        return username;
    }

    public boolean checkMessageReceived(String cmd, String type, String payload) {
        boolean found = false;
        while (!this.buffer.isEmpty() && !found) {
            JSONObject current = this.buffer.pop();
            //if payload is null is not relevant to the message identification
            byte[] decodedPayloadBytes = (current.getString("payload") != null) ? Base64.getDecoder().decode(current.getString("payload")) : new byte[]{};
            String decodedPayload = new String(decodedPayloadBytes);
            if (verbose) {
                System.out.println(alias + ": Payload is: " + decodedPayload);
                log.info(alias + " Payload is: " + decodedPayload);
            }
            boolean payloadFound = (payload != null) ? decodedPayload.equalsIgnoreCase(payload) : true;
            if (current.getString("cmd").equalsIgnoreCase(cmd) && current.getString("type").equalsIgnoreCase(type) && payloadFound) {
                found = true;
                if (current.get("type").equals("CONNECTION_OK")) {
                    this.username = current.getJSONObject("params").getString("username");
                }
            }
        }
        return found;
    }

    public boolean isConnected() {
        return connected;
    }

    public void onError(Session s, Throwable error) {
        error.printStackTrace();
    }

    public synchronized void onMessage(Session s, String message) {
        byte[] decodedMessage = Base64.getDecoder().decode(message.getBytes(StandardCharsets.UTF_8));
        message = new String(decodedMessage);
        if (verbose) {
            System.out.println(alias + " received message: " + message);
            log.info(alias + " Received message: {}", new Object[]{message});
        }
        JSONObject obj = new JSONObject(message);
        this.buffer.push(obj);
    }

    public void connectParticipant() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                if (verbose)
                    log.info(alias + " Connecting player to web socket.....");
                URI uri = null;
                try {
                    uri = new URI(websocketBaseUrl);
                } catch (URISyntaxException e) {
                    log.error(e.getMessage(), e);
                    return;
                }
                ClientUpgradeRequest request = new ClientUpgradeRequest();
                if (HyperIoTChannelParticipant.this.authToken != null)
                    request.setHeader("AUTHORIZATION", "JWT " + HyperIoTChannelParticipant.this.authToken);
                setupRequestHeaders(request);
                try {
                    client.connect(HyperIoTChannelParticipant.this.webSocket, uri, request);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        };
        this.executor.execute(r);
    }

    public void disconnect() {
        try {
            this.client.stop();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void createChannel(String channelName, String channelId, HyperIoTWebSocketChannelType channelType, String maxParticipants) {
        if (verbose)
            log.info(" CREATING CHANNEL " + channelId);
        HashMap<String, String> params = new HashMap<>();
        params.put(HyperIoTWebSocketChannelConstants.CHANNEL_ID_PARAM, channelName);
        params.put(HyperIoTWebSocketChannelConstants.CHANNEL_NAME_PARAM, channelName);
        params.put(HyperIoTWebSocketChannelConstants.CHANNEL_TYPE_PARAM, channelType.getTypeStr());
        params.put(HyperIoTWebSocketChannelConstants.CHANNEL_MAX_PARTECIPANTS_PARAM, maxParticipants);
        WebSocketUtils.sendMessage("", HyperIoTWebSocketChannelCommandType.CREATE_CHANNEL_COMMAND, HyperIoTWebSocketMessageType.APPLICATION.name(), params, verbose, this.currentSession);
    }

    public void joinChannel(String channelId) {
        if (verbose)
            log.info(alias + " JOIN CHANNEL " + channelId);
        HashMap<String, String> params = new HashMap<>();
        params.put(HyperIoTWebSocketChannelConstants.CHANNEL_ID_PARAM, channelId);
        WebSocketUtils.sendMessage("", HyperIoTWebSocketChannelCommandType.JOIN_CHANNEL_COMMAND, HyperIoTWebSocketMessageType.APPLICATION.name(), params, verbose, this.currentSession);
    }

    public void leaveChannel(String channelId) {
        if (verbose)
            log.info(alias + " LEAVE CHANNEL " + channelId);
        HashMap<String, String> params = new HashMap<>();
        params.put(HyperIoTWebSocketChannelConstants.CHANNEL_ID_PARAM, channelId);
        WebSocketUtils.sendMessage("", HyperIoTWebSocketChannelCommandType.LEAVE_CHANNEL_COMMAND, HyperIoTWebSocketMessageType.APPLICATION.name(), params, verbose, this.currentSession);
    }

    public void kickUser(String channelId, String username, String kicker, String kickMessage) {
        if (verbose)
            log.info(alias + " KICKING USER " + username + " FROM CHANNEL " + channelId);
        HashMap<String, String> params = new HashMap<>();
        params.put(HyperIoTWebSocketChannelConstants.CHANNEL_ID_PARAM, channelId);
        params.put(HyperIoTWebSocketChannelConstants.CHANNEL_MESSAGE_PARAM_USER_TO_KICK, username);
        params.put(HyperIoTWebSocketChannelConstants.CHANNEL_MESSAGE_PARAM_KICK_MESSAGE, kickMessage);
        WebSocketUtils.sendMessage("", HyperIoTWebSocketChannelCommandType.KICK_USER_COMMAND, HyperIoTWebSocketMessageType.APPLICATION.name(), params, verbose, this.currentSession);
    }

    public void banUser(String channelId, String username, String banner, String banMessage) {
        if (verbose)
            log.info(alias + " BANNING USER " + username + " FROM CHANNEL " + channelId);
        HashMap<String, String> params = new HashMap<>();
        params.put(HyperIoTWebSocketChannelConstants.CHANNEL_ID_PARAM, channelId);
        params.put(HyperIoTWebSocketChannelConstants.CHANNEL_MESSAGE_PARAM_USER_TO_KICK, username);
        params.put(HyperIoTWebSocketChannelConstants.CHANNEL_MESSAGE_PARAM_KICK_MESSAGE, banMessage);
        WebSocketUtils.sendMessage("", HyperIoTWebSocketChannelCommandType.BAN_USER_COMMAND, HyperIoTWebSocketMessageType.APPLICATION.name(), params, verbose, this.currentSession);
    }

    public void unbanUser(String channelId, String ip, String username) {
        if (verbose)
            log.info(alias + " UNBANNING USER " + username + " FROM CHANNEL " + channelId);
        HashMap<String, String> params = new HashMap<>();
        params.put(HyperIoTWebSocketChannelConstants.CHANNEL_ID_PARAM, channelId);
        params.put(HyperIoTWebSocketChannelConstants.CHANNEL_MESSAGE_PARAM_BANNED_IP, ip);
        params.put(HyperIoTWebSocketChannelConstants.CHANNEL_MESSAGE_PARAM_BANNED_USERNAME, username);
        WebSocketUtils.sendMessage("", HyperIoTWebSocketChannelCommandType.UNBAN_USER_COMMAND, HyperIoTWebSocketMessageType.APPLICATION.name(), params, verbose, this.currentSession);
    }

    public void deleteChannel(String channelId) {
        if (verbose)
            log.info(alias + " DELETE CHANNEL " + channelId);
        HashMap<String, String> params = new HashMap<>();
        params.put(HyperIoTWebSocketChannelConstants.CHANNEL_ID_PARAM, channelId);
        WebSocketUtils.sendMessage("", HyperIoTWebSocketChannelCommandType.DELETE_CHANNEL_COMMAND, HyperIoTWebSocketMessageType.APPLICATION.name(), params, verbose, this.currentSession);
    }

    public void sendMessage(String payload, String channelId) {
        HashMap<String, String> params = new HashMap<>();
        params.put(HyperIoTWebSocketChannelConstants.CHANNEL_ID_PARAM, channelId);
        WebSocketUtils.sendMessage(payload, HyperIoTWebSocketBasicCommandType.SEND_MESSAGE_COMMAND, HyperIoTWebSocketMessageType.APPLICATION.name(), params, verbose, this.currentSession);
    }

    protected abstract void setupRequestHeaders(ClientUpgradeRequest request);

}
