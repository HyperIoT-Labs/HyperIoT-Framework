package it.acsoftware.hyperiot.websocket.channel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.acsoftware.hyperiot.base.exception.HyperIoTRuntimeException;
import it.acsoftware.hyperiot.base.model.HyperIoTClusterNodeInfo;
import it.acsoftware.hyperiot.websocket.api.HyperIoTWebSocketBasicCommandType;
import it.acsoftware.hyperiot.websocket.api.HyperIoTWebSocketCommand;
import it.acsoftware.hyperiot.websocket.api.HyperIoTWebSocketSession;
import it.acsoftware.hyperiot.websocket.api.channel.HyperIoTWebSocketChannel;
import it.acsoftware.hyperiot.websocket.api.channel.HyperIoTWebSocketChannelClusterMessageBroker;
import it.acsoftware.hyperiot.websocket.api.channel.HyperIoTWebSocketChannelRole;
import it.acsoftware.hyperiot.websocket.api.channel.HyperIoTWebSocketChannelSession;
import it.acsoftware.hyperiot.websocket.channel.command.HyperIoTWebSocketChannelCommandType;
import it.acsoftware.hyperiot.websocket.channel.util.HyperIoTWebSocketChannelConstants;
import it.acsoftware.hyperiot.websocket.model.HyperIoTWebSocketConstants;
import it.acsoftware.hyperiot.websocket.model.HyperIoTWebSocketUserInfo;
import it.acsoftware.hyperiot.websocket.model.message.HyperIoTWebSocketMessage;
import it.acsoftware.hyperiot.websocket.model.message.HyperIoTWebSocketMessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @Author Aristide Cittadino
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class HyperIoTWebSocketBasicChannel implements HyperIoTWebSocketChannel, Serializable {
    @JsonIgnore
    private static Logger log = LoggerFactory.getLogger(HyperIoTWebSocketBasicChannel.class);

    private String channelId;
    private String channelName;
    private int maxPartecipants;

    private Map<String, Object> channelParams;

    private static ObjectMapper mapper = new ObjectMapper();

    //All session across the eventual cluster
    @JsonIgnore
    private Map<HyperIoTWebSocketUserInfo, Set<HyperIoTWebSocketChannelRole>> partecipantsInfo;

    //All session on the specific node
    @JsonIgnore
    private transient Map<HyperIoTWebSocketUserInfo, HyperIoTWebSocketChannelSession> partecipantsSessions;

    private HyperIoTWebSocketChannelClusterMessageBroker clusterMessageBroker;

    private List<String> bennedIps;

    private List<String> bannedUsernames;

    public HyperIoTWebSocketBasicChannel(String channelName, String channelId, int maxPartecipants, Map<String, Object> channelParams, HyperIoTWebSocketChannelClusterMessageBroker clusterMessageBroker) {
        this.channelName = channelName;
        this.channelId = channelId;
        this.maxPartecipants = maxPartecipants;
        this.partecipantsInfo = Collections.synchronizedMap(new HashMap<>());
        this.partecipantsSessions = new HashMap<>();
        this.bennedIps = Collections.synchronizedList(new ArrayList<>());
        this.bannedUsernames = Collections.synchronizedList(new ArrayList<>());
        this.channelParams = channelParams;
        defineClusterMessageBroker(clusterMessageBroker);
    }

    protected HyperIoTWebSocketBasicChannel() {
        this(null, null, 0, null, null);
    }

    public void defineClusterMessageBroker(HyperIoTWebSocketChannelClusterMessageBroker clusterMessageBroker) {
        this.clusterMessageBroker = clusterMessageBroker;
    }

    @Override
    public String getChannelId() {
        return channelId;
    }

    @Override
    public String getChannelName() {
        return channelName;
    }

    @Override
    public void addChannelParam(String name, Object value) {
        this.channelParams.put(name, value);
    }

    @Override
    public void removeChannelParam(String name) {
        this.channelParams.remove(name);
    }

    @Override
    public Map<String, Object> getChannelParams() {
        return Collections.unmodifiableMap(this.channelParams);
    }

    @Override
    public Object getChannelParam(String name) {
        return this.channelParams.get(name);
    }

    @Override
    public Optional<HyperIoTWebSocketUserInfo> findUserInfo(String usedId) {
        return partecipantsInfo.keySet().stream().filter(info -> info.getUsername().equalsIgnoreCase(usedId)).findAny();
    }

    public Set<HyperIoTWebSocketUserInfo> getPartecipantsInfo() {
        return Collections.unmodifiableSet(partecipantsInfo.keySet());
    }

    public Optional<HyperIoTWebSocketUserInfo> findPartecipantInfoFromUserId(String userId) {
        return partecipantsInfo.keySet().stream().filter(partecipantInfo -> partecipantInfo.getUsername().equalsIgnoreCase(userId)).findAny();
    }

    public void addPartecipantInfo(HyperIoTWebSocketUserInfo partecipantInfo, Set<HyperIoTWebSocketChannelRole> roles) {
        this.checkPartecipantsLimits();
        this.partecipantsInfo.put(partecipantInfo, Collections.unmodifiableSet(roles));
    }

    public synchronized void addPartecipantSession(HyperIoTWebSocketUserInfo partecipantInfo, Set<HyperIoTWebSocketChannelRole> roles, HyperIoTWebSocketChannelSession session) {
        this.checkPartecipantsLimits();
        if (!this.bennedIps.contains(partecipantInfo.getIpAddress()) && !this.bannedUsernames.contains(partecipantInfo.getUsername())) {
            addPartecipantInfo(partecipantInfo, roles);
            this.partecipantsSessions.put(partecipantInfo, session);
            //used to send a feedback message to the connecting user
            this.partecipantJoined(session);
        } else {
            throw new HyperIoTRuntimeException("Cannot join channel " + this.channelName + ", you have been banned!");
        }
    }

    @Override
    public HyperIoTWebSocketSession getPartecipantSession(HyperIoTWebSocketUserInfo partecipantInfo) {
        return this.partecipantsSessions.get(partecipantInfo);
    }

    @Override
    public boolean hasPartecipantSession(HyperIoTWebSocketUserInfo partecipantInfo) {
        return this.partecipantsSessions.containsKey(partecipantInfo);
    }

    public void kickPartecipant(HyperIoTWebSocketUserInfo kickerInfo, HyperIoTWebSocketMessage kickMessageCommand) {
        boolean kickerIsOnThisNode = kickerInfo.getClusterNodeInfo().isOnLocalNode();
        String usernameToKick = kickMessageCommand.getParams().get(HyperIoTWebSocketChannelConstants.CHANNEL_MESSAGE_PARAM_USER_TO_KICK);
        Optional<HyperIoTWebSocketUserInfo> toKick = this.partecipantsInfo.keySet().stream().filter(info -> info.getUsername().equalsIgnoreCase(usernameToKick)).findAny();
        //broadcast the message only if it is on the kicker node
        //done on the kicker node
        if (kickerIsOnThisNode) {
            //send command to other nodes in order to allign their state
            this.clusterMessageBroker.sendMessage(channelId, kickMessageCommand);
        }
        //might be null
        HyperIoTWebSocketSession kickedUserSession = this.partecipantsSessions.get(toKick.get());
        //each node will remove the info about the kicked user
        if (userHasPermission(kickerInfo, HyperIoTWebSocketChannelCommandType.KICK_USER)) {
            //each node will remove the info object of the kicked user
            if (toKick.isPresent()) {
                removePartecipant(toKick.get());
                //notify all local channel participant user has been kicked
                String kickMessageStr = (kickMessageCommand.getParams() != null && kickMessageCommand.getParams().containsKey(HyperIoTWebSocketChannelConstants.CHANNEL_MESSAGE_PARAM_KICK_MESSAGE)) ? kickMessageCommand.getParams().get(HyperIoTWebSocketChannelConstants.CHANNEL_MESSAGE_PARAM_KICK_MESSAGE) : "";
                HyperIoTWebSocketMessage kickMessageNotification = HyperIoTWebSocketMessage.createMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, kickMessageStr.getBytes(), HyperIoTWebSocketMessageType.PARTICIPANT_KICKED);
                //copying params from original message sender,kicker,channel
                kickMessageNotification.setParams(kickMessageCommand.getParams());
                //deliver kick message notification on the local node session
                //each node will notify its own users inside the channel since every node will receive the kick notification
                deliverMessage(null, kickMessageNotification);
                //send kick message to kicked user, since it is outside from the channel
                if (kickedUserSession != null) {
                    kickedUserSession.sendRemote(kickMessageNotification);
                }
            }
        } else {
            //it is sent only if the session is on the current node
            sendKOMessage(this.partecipantsSessions.get(kickerInfo), "You do not have permissions to perform kick!");
        }
    }

    public void banPartecipant(HyperIoTWebSocketUserInfo banner, HyperIoTWebSocketMessage banMessageCommand) {
        String usernameToBan = banMessageCommand.getParams().get(HyperIoTWebSocketChannelConstants.CHANNEL_MESSAGE_PARAM_USER_TO_KICK);
        Optional<HyperIoTWebSocketUserInfo> toBan = this.partecipantsInfo.keySet().stream().filter(info -> info.getUsername().equalsIgnoreCase(usernameToBan)).findAny();
        boolean bannerIsOnThisNode = banner.getClusterNodeInfo().isOnLocalNode();
        //broadcast the message only if it is on the kicker node
        //done on the kicker node
        if (bannerIsOnThisNode) {
            //send command to other nodes in order to allign their state
            this.clusterMessageBroker.sendMessage(channelId, banMessageCommand);
        }
        if (toBan.isPresent()) {
            if (userHasPermission(banner, HyperIoTWebSocketChannelCommandType.BAN_USER)) {
                this.kickPartecipant(banner, banMessageCommand);
                this.bennedIps.add(toBan.get().getIpAddress());
                this.bannedUsernames.add(toBan.get().getUsername());
            } else {
                sendKOMessage(this.partecipantsSessions.get(banner), "You do not have permissions to ban!");
            }
        }
    }

    public void unbanPartecipant(HyperIoTWebSocketUserInfo banner, HyperIoTWebSocketMessage unbanMessageCommand) {
        boolean bannerIsOnThisNode = banner.getClusterNodeInfo().isOnLocalNode();
        //broadcast the message only if it is on the kicker node
        //done on the kicker node
        if (bannerIsOnThisNode) {
            //send command to other nodes in order to allign their state
            this.clusterMessageBroker.sendMessage(channelId, unbanMessageCommand);
        }

        if (userHasPermission(banner, HyperIoTWebSocketChannelCommandType.BAN_USER)) {
            String ipAddress = unbanMessageCommand.getParams().get(HyperIoTWebSocketChannelConstants.CHANNEL_MESSAGE_PARAM_BANNED_IP);
            String bannedUsername = unbanMessageCommand.getParams().get(HyperIoTWebSocketChannelConstants.CHANNEL_MESSAGE_PARAM_BANNED_USERNAME);
            this.bennedIps.remove(ipAddress);
            this.bannedUsernames.remove(bannedUsername);
            if (bannerIsOnThisNode)
                sendOKMessage(this.partecipantsSessions.get(banner), "UNBANNED");
        }
    }

    public void removePartecipant(HyperIoTWebSocketUserInfo participantInfo) {
        this.partecipantsInfo.remove(participantInfo);
        if (this.partecipantsSessions.containsKey(participantInfo)) {
            HyperIoTWebSocketChannelSession session = this.partecipantsSessions.get(participantInfo);
            session.removeJoinedChannels(this);
            this.partecipantsSessions.remove(participantInfo);
        }
    }

    public void leaveChannel(HyperIoTWebSocketUserInfo participantInfo) {
        HyperIoTWebSocketSession leavingUserSession = this.partecipantsSessions.get(participantInfo);
        this.removePartecipant(participantInfo);
        this.sendOKMessage(leavingUserSession, "CHANNEL_LEAVED");
        HyperIoTWebSocketMessage m = HyperIoTWebSocketMessage.createMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, "".getBytes(StandardCharsets.UTF_8), HyperIoTWebSocketMessageType.PARTICIPANT_GONE);
        m.getParams().put(HyperIoTWebSocketChannelConstants.CHANNEL_ID_PARAM, this.channelId);
        m.getParams().put(HyperIoTWebSocketConstants.WEB_SOCKET_USERNAME_PARAM, participantInfo.getUsername());
        this.broadcastSystemMessage(participantInfo, m);
    }

    public Set<HyperIoTClusterNodeInfo> getPeers() {
        Set<HyperIoTClusterNodeInfo> nodeList = new HashSet<>();
        partecipantsInfo.keySet().stream().forEach(userInfo -> {
            if (!userInfo.getClusterNodeInfo().isOnLocalNode()) {
                nodeList.add(userInfo.getClusterNodeInfo());
            }
        });
        return nodeList;
    }

    /**
     * Broadcasts a message inside the channel
     *
     * @param senderInfo
     * @param message
     */
    private void broadcastSystemMessage(HyperIoTWebSocketUserInfo senderInfo, HyperIoTWebSocketMessage message) {
        message.setCmd(HyperIoTWebSocketBasicCommandType.READ_MESSAGE.toString());
        deliverMessage(senderInfo, message);
        //deliver message to other session in other cluster nodes
        try {
            this.clusterMessageBroker.sendMessage(channelId, message);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     * Send the user message through multiple session even across the cluster
     *
     * @param senderSession
     * @param message
     */
    public void exchangeMessage(HyperIoTWebSocketChannelSession senderSession, HyperIoTWebSocketMessage message) {
        //Change command from send to read
        HyperIoTWebSocketUserInfo sender = senderSession.getUserInfo();
        if (userHasPermission(sender, HyperIoTWebSocketBasicCommandType.SEND_MESSAGE)) {
            message.setCmd(HyperIoTWebSocketBasicCommandType.READ_MESSAGE.toString());
            message.getParams().put(HyperIoTWebSocketChannelConstants.CHANNEL_MESSAGE_PARAM_PVT_MESSAGE_SENDER, sender.getUsername());
            //deliver message to session that are on current node
            deliverMessage(sender, message);
            //deliver message to other session in other cluster nodes
            try {
                //automatically converts the message to read and spread to the cluster
                this.clusterMessageBroker.sendMessage(channelId, message);
            } catch (Exception e) {
                String messageStr = "Error while sending message:" + e.getMessage();
                HyperIoTWebSocketMessage errorMess = HyperIoTWebSocketMessage.createMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, messageStr.getBytes(StandardCharsets.UTF_8), HyperIoTWebSocketMessageType.ERROR);
                senderSession.sendRemote(errorMess);
            }
        } else {
            HyperIoTWebSocketMessage errorMess = HyperIoTWebSocketMessage.createMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, "Unauthorized to send message".getBytes(StandardCharsets.UTF_8), HyperIoTWebSocketMessageType.ERROR);
            senderSession.sendRemote(errorMess);
        }
    }

    /**
     * Deliver current user message on local sessions on the current node on which message has been received
     *
     * @param sender
     * @param message
     */
    public void deliverMessage(HyperIoTWebSocketUserInfo sender, HyperIoTWebSocketMessage message) {
        //avoiding concurrent modificaiton expcetion with unmodifiable set
        Collections.unmodifiableSet(partecipantsSessions.keySet()).parallelStream().forEach(userInfo -> {
            try {
                //not delivering to the sender eventually
                boolean isMessageSender = sender != null && userInfo.getUsername().equalsIgnoreCase(sender.getUsername());
                boolean hasSingleRecipient = message.getParams() != null && message.getParams().containsKey(HyperIoTWebSocketConstants.WEB_SOCKET_RECIPIENT_USER_PARAM);
                String recipient = hasSingleRecipient ? message.getParams().get(HyperIoTWebSocketConstants.WEB_SOCKET_RECIPIENT_USER_PARAM) : null;
                if ((!hasSingleRecipient && !isMessageSender) || (hasSingleRecipient && userInfo.getUsername().equals(recipient))) {
                    HyperIoTWebSocketChannelSession session = this.partecipantsSessions.get(userInfo);
                    session.sendRemote(message);
                }
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
            }
        });
    }

    @Override
    public void receiveMessageForServer(HyperIoTWebSocketChannelSession senderSession, HyperIoTWebSocketMessage message) {
        try {
            String serverResponse = processMessageOnServer(senderSession, message);
            //getting server response, send back to the client
            if (serverResponse != null) {
                HyperIoTWebSocketMessage responseMessage = HyperIoTWebSocketMessage.createMessage(null, serverResponse.getBytes("UTF8"), HyperIoTWebSocketMessageType.RESULT);
                senderSession.sendRemote(responseMessage);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            //sending back to the client the error message
            HyperIoTWebSocketMessage errMessage = HyperIoTWebSocketMessage.createMessage(null, e.getMessage().getBytes(), HyperIoTWebSocketMessageType.ERROR);
            senderSession.sendRemote(errMessage);
        }
    }

    protected String processMessageOnServer(HyperIoTWebSocketChannelSession senderSession, HyperIoTWebSocketMessage message) {
        //Method can be overridden in order to develop custom server logic
        return "";
    }

    public String toJson() {
        try {
            return mapper.writeValueAsString(this);
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
        }
        return "{}";
    }

    public boolean userHasPermission(HyperIoTWebSocketUserInfo user, HyperIoTWebSocketCommand commandType) {
        if (partecipantsInfo.get(user) != null)
            return partecipantsInfo.get(user).stream().filter(role -> role.getAllowedCmds().contains(commandType)).findAny().isPresent();
        return false;
    }

    private void checkPartecipantsLimits() {
        //limit is enabled if maxPartecipants is greater than 0
        if (this.maxPartecipants > 0 && this.partecipantsInfo.keySet().size() >= this.maxPartecipants)
            throw new HyperIoTRuntimeException("Cannot add partecipants to current channel:Maximum number of partecipants reached!");
    }

    /**
     * Can be customized with custom messages as an user get connected to a channel
     *
     * @param userSession
     */
    protected void partecipantJoined(HyperIoTWebSocketChannelSession userSession) {
        userSession.addJoinedChannels(this);
        sendOKMessage(userSession, "SUCCESFULLY_JOINED");
        HyperIoTWebSocketMessage participantJoined = HyperIoTWebSocketMessage.createMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, "".getBytes(StandardCharsets.UTF_8), HyperIoTWebSocketMessageType.PARTICIPANT_ADDED);
        participantJoined.getParams().put(HyperIoTWebSocketChannelConstants.CHANNEL_ID_PARAM, this.channelId);
        participantJoined.getParams().put(HyperIoTWebSocketConstants.WEB_SOCKET_USERNAME_PARAM, userSession.getUserInfo().getUsername());
        //broadcast to other participants
        broadcastSystemMessage(userSession.getUserInfo(), participantJoined);
    }

    /**
     * Sends ok message as response only if the current user has a session on the current node
     */
    protected void sendOKMessage(HyperIoTWebSocketSession session, String payload) {
        if (session != null) {
            HyperIoTWebSocketMessage message = HyperIoTWebSocketMessage.createMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, payload.getBytes(StandardCharsets.UTF_8), HyperIoTWebSocketMessageType.OK);
            session.sendRemote(message);
        }
    }

    /**
     * Sends ok message as response only if the current user has a session on the current node
     */
    protected void sendKOMessage(HyperIoTWebSocketSession session, String koMessage) {
        if (session != null) {
            HyperIoTWebSocketMessage message = HyperIoTWebSocketMessage.createMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, koMessage.getBytes(StandardCharsets.UTF_8), HyperIoTWebSocketMessageType.ERROR);
            session.sendRemote(message);
        }
    }
}
