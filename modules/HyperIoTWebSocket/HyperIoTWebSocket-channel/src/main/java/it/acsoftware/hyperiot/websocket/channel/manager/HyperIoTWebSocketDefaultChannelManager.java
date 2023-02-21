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

package it.acsoftware.hyperiot.websocket.channel.manager;

import it.acsoftware.hyperiot.base.exception.HyperIoTRuntimeException;
import it.acsoftware.hyperiot.websocket.api.HyperIoTWebSocketBasicCommandType;
import it.acsoftware.hyperiot.websocket.api.HyperIoTWebSocketSession;
import it.acsoftware.hyperiot.websocket.api.channel.*;
import it.acsoftware.hyperiot.websocket.channel.command.HyperIoTWebSocketChannelCommandType;
import it.acsoftware.hyperiot.websocket.channel.factory.HyperIoTWebSocketChannelFactory;
import it.acsoftware.hyperiot.websocket.channel.role.HyperIoTWebSocketChannelRoleManager;
import it.acsoftware.hyperiot.websocket.channel.util.HyperIoTWebSocketChannelConstants;
import it.acsoftware.hyperiot.websocket.model.HyperIoTWebSocketUserInfo;
import it.acsoftware.hyperiot.websocket.model.message.HyperIoTWebSocketMessage;
import it.acsoftware.hyperiot.websocket.model.message.HyperIoTWebSocketMessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HyperIoTWebSocketDefaultChannelManager<T extends HyperIoTWebSocketChannel> implements HyperIoTWebSocketChannelManager {
    private static Logger log = LoggerFactory.getLogger(HyperIoTWebSocketDefaultChannelManager.class);

    private ConcurrentHashMap<String, HyperIoTWebSocketChannel> channels;
    private HyperIoTWebSocketChannelClusterCoordinator coordinator;
    private HyperIoTWebSocketChannelClusterMessageBroker clusterBroker;
    private Class<T> channelClass;

    public HyperIoTWebSocketDefaultChannelManager(Class<T> channelClass, HyperIoTWebSocketChannelClusterCoordinator coordinator, HyperIoTWebSocketChannelClusterMessageBroker clusterBroker) {
        this.channels = new ConcurrentHashMap<>(1);
        //loading already create channels eventually on other cluster instances
        this.coordinator = coordinator;
        this.clusterBroker = clusterBroker;
        if (this.clusterBroker != null)
            this.clusterBroker.registerChannelManager(this);
        this.channels.putAll(coordinator.connectNewPeer(this));
        this.channelClass = channelClass;
    }

    public HyperIoTWebSocketChannelClusterMessageBroker getClusterBroker() {
        return clusterBroker;
    }

    @Override
    public HyperIoTWebSocketChannel findChannel(String channelId) {
        return channels.get(channelId);
    }

    @Override
    public boolean channelExists(String channelId) {
        return channels.containsKey(channelId);
    }

    @Override
    public Collection<HyperIoTWebSocketChannel> getAvailableChannels() {
        return Collections.unmodifiableCollection(channels.values());
    }

    @Override
    public void createChannel(String channelType, String channelName, String newChannelId, int maxPartecipants, Map<String, Object> params, HyperIoTWebSocketChannelSession ownerSession, Set<HyperIoTWebSocketChannelRole> roles) {
        try {
            HyperIoTWebSocketChannel newChannel = HyperIoTWebSocketChannelFactory.createChannelFromChannelType(channelType, newChannelId, channelName, maxPartecipants, params, this.clusterBroker);
            if(!this.channels.containsKey(newChannelId)) {
                this.channels.put(newChannelId, newChannel);
                //automatically join channel after creation
                notifyChannelCreated(newChannel, ownerSession, roles);
                joinChannel(newChannelId, ownerSession, roles);
            } else {
                throw new HyperIoTRuntimeException("Channel Already exists!");
            }
        } catch (Throwable t) {
            throw new HyperIoTRuntimeException(t.getMessage());
        }
    }

    @Override
    public void joinChannel(String channelId, HyperIoTWebSocketChannelSession partecipantSession, Set<HyperIoTWebSocketChannelRole> roles) {
        HyperIoTWebSocketChannel channel = findChannel(channelId);
        if (channel != null) {
            Set<HyperIoTWebSocketChannelRole> joinChannelRoles = HyperIoTWebSocketChannelRoleManager.newRoleSet(roles, defineJoinChannelRoles());
            channel.addPartecipantSession(partecipantSession.getUserInfo(), joinChannelRoles, partecipantSession);
            notifyPartecipantAdded(channel, partecipantSession, roles);
            return;
        }
        throw new HyperIoTRuntimeException("Channel not found!");
    }

    @Override
    public void leaveChannel(String channelId, HyperIoTWebSocketChannelSession partecipantSession) {
        HyperIoTWebSocketChannel channel = findChannel(channelId);
        if (channel != null) {
            channel.leaveChannel(partecipantSession.getUserInfo());
            notifyPartecipantGone(channel, partecipantSession);
            return;
        }
        throw new HyperIoTRuntimeException("Channel not found!");
    }

    @Override
    public void kickParticipant(String channelId, HyperIoTWebSocketUserInfo kickerInfo, HyperIoTWebSocketMessage kickMessageCommand) {
        HyperIoTWebSocketChannel channel = findChannel(channelId);
        if (channel != null) {
            channel.kickPartecipant(kickerInfo, kickMessageCommand);
            String usernameToKick = kickMessageCommand.getParams().get(HyperIoTWebSocketChannelConstants.CHANNEL_MESSAGE_PARAM_USER_TO_KICK);
            Optional<HyperIoTWebSocketUserInfo> toKick = channel.getPartecipantsInfo().stream().filter(info -> info.getUsername().equalsIgnoreCase(usernameToKick)).findAny();
            //send coordination message only from the node which owns the kicked user websocket session
            if (toKick.isPresent() && channel.hasPartecipantSession(toKick.get()))
                this.coordinator.notifyPartecipantGone(channelId, toKick.get());
            return;
        }
        throw new HyperIoTRuntimeException("Channel not found!");
    }

    @Override
    public void banParticipant(String channelId, HyperIoTWebSocketUserInfo bannerInfo, HyperIoTWebSocketMessage banMessageCommand) {
        HyperIoTWebSocketChannel channel = findChannel(channelId);
        if (channel != null) {
            channel.banPartecipant(bannerInfo, banMessageCommand);
            String usernameToKick = banMessageCommand.getParams().get(HyperIoTWebSocketChannelConstants.CHANNEL_MESSAGE_PARAM_USER_TO_KICK);
            Optional<HyperIoTWebSocketUserInfo> toBan = channel.getPartecipantsInfo().stream().filter(info -> info.getUsername().equalsIgnoreCase(usernameToKick)).findAny();
            //send coordination message only from the node which owns the kicked user websocket session
            if (toBan.isPresent() && channel.hasPartecipantSession(toBan.get()))
                this.coordinator.notifyPartecipantGone(channelId, toBan.get());
            return;
        }
        throw new HyperIoTRuntimeException("Channel not found!");
    }

    @Override
    public void unbanParticipant(String channelId, HyperIoTWebSocketUserInfo bannerInfo, HyperIoTWebSocketMessage unbanMessageCommand) {
        HyperIoTWebSocketChannel channel = findChannel(channelId);
        if (channel != null) {
            channel.unbanPartecipant(bannerInfo, unbanMessageCommand);
        }
        throw new HyperIoTRuntimeException("Channel not found!");
    }

    @Override
    public void deleteChannel(HyperIoTWebSocketUserInfo userInfo, HyperIoTWebSocketChannel toDeleteChannel) {
        if (toDeleteChannel != null && toDeleteChannel.userHasPermission(userInfo, HyperIoTWebSocketChannelCommandType.DELETE_CHANNEl)) {
            HyperIoTWebSocketSession s = toDeleteChannel.getPartecipantSession(userInfo);
            channels.remove(toDeleteChannel);
            coordinator.notifyChannelDeleted(toDeleteChannel.getChannelId());
            //only in this case channel manager sends message over sessions
            s.sendRemote(HyperIoTWebSocketMessage.createMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, "CHANNEL_DELETED".getBytes(StandardCharsets.UTF_8), HyperIoTWebSocketMessageType.OK));
        } else {
            throw new HyperIoTRuntimeException("Channel not found or you don't have permissions to delete it");
        }
    }

    @Override
    public void deliverMessage(HyperIoTWebSocketMessage message) {
        final String channelId = message.getParams().get(HyperIoTWebSocketChannelConstants.CHANNEL_ID_PARAM);
        final String sender = message.getParams().get(HyperIoTWebSocketMessage.WS_MESSAGE_SENDER_PARAM_NAME);
        HyperIoTWebSocketChannel channel = findChannel(channelId);
        Optional<HyperIoTWebSocketUserInfo> senderInfo = channel.findUserInfo(sender);
        if (channel != null) {
            if (message.getCmd().equals(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND))
                channel.deliverMessage(senderInfo.orElse(null), message);
        } else {
            log.error("Impossible to forward message to user from sender :" + sender + " and channel :" + channelId);
        }
    }

    @Override
    public void forwardMessage(String channelId, HyperIoTWebSocketMessage message) {
        this.clusterBroker.sendMessage(channelId, message);
    }

    //callbacks from cluster coordinator
    @Override
    public void onChannelAdded(HyperIoTWebSocketChannel channel) {
        channels.putIfAbsent(channel.getChannelId(), channel);
    }

    @Override
    public void onChannelRemoved(String channelId) {
        if (channels.containsKey(channelId)) {
            channels.remove(channelId);
        }
    }

    @Override
    public void onPartecipantAdded(String channelId, HyperIoTWebSocketUserInfo partecipantInfo, Set<HyperIoTWebSocketChannelRole> roles) {
        HyperIoTWebSocketChannel channel = findChannel(channelId);
        if (channel != null) {
            if (!partecipantInfo.getClusterNodeInfo().isOnLocalNode())
                channel.addPartecipantInfo(partecipantInfo, roles);
            return;
        }
        throw new HyperIoTRuntimeException("Channel cannot be null");
    }

    @Override
    public void onPartecipantGone(String channelId, HyperIoTWebSocketUserInfo partecipantInfo) {
        if (partecipantInfo.getClusterNodeInfo().isOnLocalNode())
            return;
        HyperIoTWebSocketChannel channel = findChannel(channelId);
        if (channel != null) {
            channel.removePartecipant(partecipantInfo);
            return;
        }
        throw new HyperIoTRuntimeException("Channel cannot be null");
    }

    @Override
    public void onPartecipantDisconnected(String channelId, HyperIoTWebSocketUserInfo partecipantInfo) {
        if (partecipantInfo.getClusterNodeInfo().isOnLocalNode())
            return;
        HyperIoTWebSocketChannel channel = findChannel(channelId);
        if (channel != null) {
            channel.removePartecipant(partecipantInfo);
            return;
        }
        throw new HyperIoTRuntimeException("Channel cannot be null");
    }

    /**
     * Roles to add to partecipants when they are joining a channel
     *
     * @return
     */
    protected Set<HyperIoTWebSocketChannelRole> defineJoinChannelRoles() {
        return Collections.emptySet();
    }

    /**
     * Roles to give to the channel owners when they create channel or they promote other users to be owner
     *
     * @return
     */
    protected Set<HyperIoTWebSocketChannelRole> defineChannelOwnerRoles() {
        return Collections.emptySet();
    }

    /**
     * @param channel
     * @param partecipantSession
     * @param roles
     */
    private void notifyChannelCreated(HyperIoTWebSocketChannel channel, HyperIoTWebSocketChannelSession partecipantSession, Set<HyperIoTWebSocketChannelRole> roles) {
        //notify inside the cluster
        coordinator.notifyChannelAdded(partecipantSession.getUserInfo().getClusterNodeInfo(),channel);
    }

    /**
     * @param channel
     * @param partecipantSession
     * @param roles
     */
    private void notifyPartecipantAdded(HyperIoTWebSocketChannel channel, HyperIoTWebSocketChannelSession partecipantSession, Set<HyperIoTWebSocketChannelRole> roles) {
        //notify inside the cluster
        coordinator.notifyPartecipantAdded(channel.getChannelId(), partecipantSession.getUserInfo(), roles);
    }

    /**
     * @param channel
     * @param partecipantSession
     */
    private void notifyPartecipantGone(HyperIoTWebSocketChannel channel, HyperIoTWebSocketChannelSession partecipantSession) {
        coordinator.notifyPartecipantGone(channel.getChannelId(), partecipantSession.getUserInfo());
    }
}
