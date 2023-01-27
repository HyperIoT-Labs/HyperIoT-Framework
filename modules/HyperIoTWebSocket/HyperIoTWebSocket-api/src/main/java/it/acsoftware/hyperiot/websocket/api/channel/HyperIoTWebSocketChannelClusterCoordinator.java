/*
 * Copyright 2019-2023 ACSoftware
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

import it.acsoftware.hyperiot.base.model.HyperIoTClusterNodeInfo;
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
    void notifyChannelAdded(HyperIoTClusterNodeInfo sourceNode,HyperIoTWebSocketChannel channel);

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
