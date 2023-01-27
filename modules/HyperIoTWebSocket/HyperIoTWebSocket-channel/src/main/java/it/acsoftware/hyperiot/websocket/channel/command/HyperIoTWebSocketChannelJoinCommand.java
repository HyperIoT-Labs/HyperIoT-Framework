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

package it.acsoftware.hyperiot.websocket.channel.command;

import it.acsoftware.hyperiot.websocket.api.channel.*;
import it.acsoftware.hyperiot.websocket.channel.role.HyperIoTWebSocketChannelRoleManager;
import it.acsoftware.hyperiot.websocket.channel.util.HyperIoTWebSocketChannelConstants;
import it.acsoftware.hyperiot.websocket.model.message.HyperIoTWebSocketMessage;
import org.osgi.service.component.annotations.Component;

@Component(service = HyperIoTWebSocketChannelCommand.class, property = {
        HyperIoTWebSocketChannelConstants.COMMAND_OSGI_FILTER + "=" + HyperIoTWebSocketChannelCommandType.JOIN_CHANNEL_COMMAND
},immediate = true)
public class HyperIoTWebSocketChannelJoinCommand extends HyperIoTWebSocketChannelAbstractCommand implements HyperIoTWebSocketChannelCommand {

    @Override
    public void execute(HyperIoTWebSocketChannelSession userSession, HyperIoTWebSocketMessage message, String channelId, HyperIoTWebSocketChannelManager channelManager) {
        HyperIoTWebSocketChannelRole participant = HyperIoTWebSocketChannelRoleManager.getHyperIoTWebSocketChannelRole(HyperIoTWebSocketChannelConstants.CHANNEL_ROLE_PARTECIPANT);
        HyperIoTWebSocketChannel channel = findChannelOrDie(channelId, channelManager);
        channelManager.joinChannel(channel.getChannelId(), userSession, HyperIoTWebSocketChannelRoleManager.newRoleSet(participant));
    }

}
