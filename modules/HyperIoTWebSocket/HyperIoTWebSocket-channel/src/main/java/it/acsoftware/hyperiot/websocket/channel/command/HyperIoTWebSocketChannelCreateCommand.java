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

import it.acsoftware.hyperiot.websocket.api.channel.HyperIoTWebSocketChannelCommand;
import it.acsoftware.hyperiot.websocket.api.channel.HyperIoTWebSocketChannelManager;
import it.acsoftware.hyperiot.websocket.api.channel.HyperIoTWebSocketChannelRole;
import it.acsoftware.hyperiot.websocket.api.channel.HyperIoTWebSocketChannelSession;
import it.acsoftware.hyperiot.websocket.channel.role.HyperIoTWebSocketChannelRoleManager;
import it.acsoftware.hyperiot.websocket.channel.util.HyperIoTWebSocketChannelConstants;
import it.acsoftware.hyperiot.websocket.model.message.HyperIoTWebSocketMessage;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Component(service = HyperIoTWebSocketChannelCommand.class, property = {
        HyperIoTWebSocketChannelConstants.COMMAND_OSGI_FILTER + "=" + HyperIoTWebSocketChannelCommandType.CREATE_CHANNEL_COMMAND
}, immediate = true)
public class HyperIoTWebSocketChannelCreateCommand extends HyperIoTWebSocketChannelAbstractCommand implements HyperIoTWebSocketChannelCommand {
    private static Logger log = LoggerFactory.getLogger(HyperIoTWebSocketChannelCreateCommand.class);

    @Override
    public void execute(HyperIoTWebSocketChannelSession userSession, HyperIoTWebSocketMessage message, String channelId, HyperIoTWebSocketChannelManager channelManager) {
        checkRequiredParameters(message, HyperIoTWebSocketChannelConstants.CHANNEL_ID_PARAM, HyperIoTWebSocketChannelConstants.CHANNEL_NAME_PARAM, HyperIoTWebSocketChannelConstants.CHANNEL_MAX_PARTECIPANTS_PARAM, HyperIoTWebSocketChannelConstants.CHANNEL_TYPE_PARAM);
        HyperIoTWebSocketChannelRole ownerRole = HyperIoTWebSocketChannelRoleManager.getHyperIoTWebSocketChannelRole(HyperIoTWebSocketChannelConstants.CHANNEL_ROLE_OWNER);
        String channelName = message.getParams().get(HyperIoTWebSocketChannelConstants.CHANNEL_NAME_PARAM);
        String newChannelId = message.getParams().get(HyperIoTWebSocketChannelConstants.CHANNEL_ID_PARAM);
        Map<String, Object> params = new HashMap<>();
        params.putAll(message.getParams());
        int maxPartecipants = -1;
        try {
            maxPartecipants = Integer.parseInt(message.getParams().get(HyperIoTWebSocketChannelConstants.CHANNEL_MAX_PARTECIPANTS_PARAM));
        } catch (NumberFormatException e) {
            log.error("Impossible to parse number of max partecipants. Channel will have unlimited number of partecipants");
        }
        String channelType = message.getParams().get(HyperIoTWebSocketChannelConstants.CHANNEL_TYPE_PARAM);
        channelManager.createChannel(channelType, channelName, newChannelId, maxPartecipants, params, userSession, HyperIoTWebSocketChannelRoleManager.newRoleSet(ownerRole));
    }
}
