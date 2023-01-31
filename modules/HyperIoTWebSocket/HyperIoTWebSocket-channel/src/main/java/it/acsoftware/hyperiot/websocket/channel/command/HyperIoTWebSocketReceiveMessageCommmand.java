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

import it.acsoftware.hyperiot.base.exception.HyperIoTRuntimeException;
import it.acsoftware.hyperiot.websocket.api.HyperIoTWebSocketBasicCommandType;
import it.acsoftware.hyperiot.websocket.api.channel.HyperIoTWebSocketChannel;
import it.acsoftware.hyperiot.websocket.api.channel.HyperIoTWebSocketChannelCommand;
import it.acsoftware.hyperiot.websocket.api.channel.HyperIoTWebSocketChannelManager;
import it.acsoftware.hyperiot.websocket.api.channel.HyperIoTWebSocketChannelSession;
import it.acsoftware.hyperiot.websocket.channel.util.HyperIoTWebSocketChannelConstants;
import it.acsoftware.hyperiot.websocket.model.HyperIoTWebSocketUserInfo;
import it.acsoftware.hyperiot.websocket.model.message.HyperIoTWebSocketMessage;
import org.osgi.service.component.annotations.Component;

import java.util.Optional;

@Component(service = HyperIoTWebSocketChannelCommand.class, property = {
        HyperIoTWebSocketChannelConstants.COMMAND_OSGI_FILTER + "=" + HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND
})
public class HyperIoTWebSocketReceiveMessageCommmand extends HyperIoTWebSocketChannelAbstractCommand implements HyperIoTWebSocketChannelCommand {

    @Override
    public void execute(HyperIoTWebSocketChannelSession userSession, HyperIoTWebSocketMessage message, String channelId, HyperIoTWebSocketChannelManager channelManager) {
        //here user session is null since this message may arrive from different cluster node
        checkRequiredParameters(message, HyperIoTWebSocketChannelConstants.CHANNEL_MESSAGE_PARAM_PVT_MESSAGE_SENDER);
        HyperIoTWebSocketChannel channel = findChannelOrDie(channelId, channelManager);
        String messageSenderUserId = message.getParams().get(HyperIoTWebSocketChannelConstants.CHANNEL_MESSAGE_PARAM_PVT_MESSAGE_SENDER);
        Optional<HyperIoTWebSocketUserInfo> senderInfo = channel.findPartecipantInfoFromUserId(messageSenderUserId);
        if (senderInfo.isPresent()) {
            channel.deliverMessage(senderInfo.get(), message);
        } else {
            throw new HyperIoTRuntimeException("Sender not found, impossible to dispatch message to channel");
        }
    }

}
