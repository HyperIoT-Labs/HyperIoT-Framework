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

import it.acsoftware.hyperiot.websocket.model.message.HyperIoTWebSocketMessage;

public interface HyperIoTWebSocketChannelRemoteCommand extends HyperIoTWebSocketChannelCommand {
    @Override
    default void execute(HyperIoTWebSocketChannelSession userSession, HyperIoTWebSocketMessage message, String channelId, HyperIoTWebSocketChannelManager manager) {
        //not passing user session since in remote mode, session might not be available
        execute(message, channelId, manager);
    }

    void execute(HyperIoTWebSocketMessage message, String channelId, HyperIoTWebSocketChannelManager manager);
}
