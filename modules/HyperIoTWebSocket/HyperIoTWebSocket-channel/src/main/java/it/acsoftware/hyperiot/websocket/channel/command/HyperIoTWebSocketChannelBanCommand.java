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

import it.acsoftware.hyperiot.websocket.api.channel.HyperIoTWebSocketChannel;
import it.acsoftware.hyperiot.websocket.api.channel.HyperIoTWebSocketChannelCommand;
import it.acsoftware.hyperiot.websocket.api.channel.HyperIoTWebSocketChannelManager;
import it.acsoftware.hyperiot.websocket.api.channel.HyperIoTWebSocketChannelRemoteCommand;
import it.acsoftware.hyperiot.websocket.channel.util.HyperIoTWebSocketChannelConstants;
import it.acsoftware.hyperiot.websocket.model.HyperIoTWebSocketUserInfo;
import it.acsoftware.hyperiot.websocket.model.message.HyperIoTWebSocketMessage;
import org.osgi.service.component.annotations.Component;

import java.util.Optional;

@Component(service = {HyperIoTWebSocketChannelCommand.class,HyperIoTWebSocketChannelRemoteCommand.class}, property = {
        HyperIoTWebSocketChannelConstants.COMMAND_OSGI_FILTER + "=" + HyperIoTWebSocketChannelCommandType.BAN_USER_COMMAND
}, immediate = true)
public class HyperIoTWebSocketChannelBanCommand extends HyperIoTWebSocketChannelAbstractCommand implements HyperIoTWebSocketChannelRemoteCommand {

    @Override
    public void execute(HyperIoTWebSocketMessage message, String channelId, HyperIoTWebSocketChannelManager channelManager) {
        checkRequiredParameters(message, HyperIoTWebSocketChannelConstants.CHANNEL_MESSAGE_PARAM_USER_TO_KICK, HyperIoTWebSocketChannelConstants.CHANNEL_MESSAGE_PARAM_KICK_MESSAGE);
        HyperIoTWebSocketChannel channel = findChannelOrDie(channelId, channelManager);
        String senderUsername = message.getParams().get(HyperIoTWebSocketMessage.WS_MESSAGE_SENDER_PARAM_NAME);
        Optional<HyperIoTWebSocketUserInfo> bannerUserInfoOptional = channel.getPartecipantsInfo().stream().filter(userInfo -> userInfo.getUsername().equals(senderUsername)).findAny();
        if (bannerUserInfoOptional.isPresent()) {
            channelManager.banParticipant(channelId, bannerUserInfoOptional.get(), message);
        }
    }
}
