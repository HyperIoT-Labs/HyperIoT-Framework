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

package it.acsoftware.hyperiot.websocket.api.channel;

import it.acsoftware.hyperiot.websocket.model.HyperIoTWebSocketUserInfo;
import it.acsoftware.hyperiot.websocket.model.message.HyperIoTWebSocketMessage;

import java.util.Collection;
import java.util.Map;
import java.util.Set;


public interface HyperIoTWebSocketChannelManager {
    HyperIoTWebSocketChannelClusterMessageBroker getClusterBroker();

    HyperIoTWebSocketChannel findChannel(String channelId);

    boolean channelExists(String channelId);

    Collection<HyperIoTWebSocketChannel> getAvailableChannels();

    void createChannel(String channelType, String channelName, String newChannelId, int maxPartecipants, Map<String, Object> params, HyperIoTWebSocketChannelSession ownerSession, Set<HyperIoTWebSocketChannelRole> roles);

    void joinChannel(String channelId, HyperIoTWebSocketChannelSession partecipantSession, Set<HyperIoTWebSocketChannelRole> roles);

    void leaveChannel(String channelId, HyperIoTWebSocketChannelSession partecipantSession);

    void kickParticipant(String channelId, HyperIoTWebSocketUserInfo kickerInfo, HyperIoTWebSocketMessage kickMessageCommand);

    void banParticipant(String channelId, HyperIoTWebSocketUserInfo bannerInfo, HyperIoTWebSocketMessage banMessageCommand);

    void unbanParticipant(String channelId, HyperIoTWebSocketUserInfo bannerInfo, HyperIoTWebSocketMessage unbanMessageCommand);

    void deleteChannel(HyperIoTWebSocketUserInfo userInfo, HyperIoTWebSocketChannel newChannel);

    void deliverMessage(HyperIoTWebSocketMessage message);

    void forwardMessage(String channelId, HyperIoTWebSocketMessage message);

    void onChannelAdded(HyperIoTWebSocketChannel channel);

    void onChannelRemoved(String channelId);

    void onPartecipantAdded(String channelId, HyperIoTWebSocketUserInfo partecipantInfo, Set<HyperIoTWebSocketChannelRole> roles);

    void onPartecipantGone(String channelId, HyperIoTWebSocketUserInfo partecipantInfo);

    void onPartecipantDisconnected(String channelId, HyperIoTWebSocketUserInfo partecipantInfo);
}
