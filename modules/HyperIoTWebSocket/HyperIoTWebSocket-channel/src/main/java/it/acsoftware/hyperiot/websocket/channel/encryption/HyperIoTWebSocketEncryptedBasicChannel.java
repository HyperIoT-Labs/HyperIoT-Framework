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

package it.acsoftware.hyperiot.websocket.channel.encryption;

import it.acsoftware.hyperiot.websocket.api.channel.HyperIoTWebSocketChannelClusterMessageBroker;
import it.acsoftware.hyperiot.websocket.api.channel.HyperIoTWebSocketChannelSession;
import it.acsoftware.hyperiot.websocket.channel.HyperIoTWebSocketBasicChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author Aristide Cittadino
 */
public abstract class HyperIoTWebSocketEncryptedBasicChannel extends HyperIoTWebSocketBasicChannel {
    private static Logger log = LoggerFactory.getLogger(HyperIoTWebSocketEncryptedBasicChannel.class);

    public HyperIoTWebSocketEncryptedBasicChannel(String channelName, String channelId, int maxPartecipants, Map<String, Object> channelParams, HyperIoTWebSocketChannelClusterMessageBroker clusterMessageBroker) {
        super(channelName, channelId, maxPartecipants, channelParams, clusterMessageBroker);
        initChannelEncryption();
    }

    protected HyperIoTWebSocketEncryptedBasicChannel() {
        super();
    }

    @Override
    protected void partecipantJoined(HyperIoTWebSocketChannelSession userSession) {
        super.partecipantJoined(userSession);
        this.setupPartecipantEncryptedSession(userSession);
    }

    protected abstract void initChannelEncryption();

    protected abstract void setupPartecipantEncryptedSession(HyperIoTWebSocketChannelSession session);

}
