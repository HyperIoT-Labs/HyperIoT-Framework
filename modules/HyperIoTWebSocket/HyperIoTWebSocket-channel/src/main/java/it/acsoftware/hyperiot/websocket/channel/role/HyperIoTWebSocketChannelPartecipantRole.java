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

package it.acsoftware.hyperiot.websocket.channel.role;

import it.acsoftware.hyperiot.base.util.HyperIoTConstants;
import it.acsoftware.hyperiot.websocket.api.HyperIoTWebSocketBasicCommandType;
import it.acsoftware.hyperiot.websocket.api.HyperIoTWebSocketCommand;
import it.acsoftware.hyperiot.websocket.api.channel.HyperIoTWebSocketChannelRole;
import it.acsoftware.hyperiot.websocket.channel.command.HyperIoTWebSocketChannelCommandType;
import it.acsoftware.hyperiot.websocket.channel.util.HyperIoTWebSocketChannelConstants;
import org.osgi.service.component.annotations.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component(service = HyperIoTWebSocketChannelRole.class, property = {
        HyperIoTConstants.OSGI_WEBSOCKET_CHANNEL_ROLE_NAME + "="+ HyperIoTWebSocketChannelConstants.CHANNEL_ROLE_PARTECIPANT
},immediate = true)
public class HyperIoTWebSocketChannelPartecipantRole implements HyperIoTWebSocketChannelRole {

    private static Set<HyperIoTWebSocketCommand> allowedCmds;

    static {
        Set<HyperIoTWebSocketCommand> commands = new HashSet<>();
        commands.add(HyperIoTWebSocketChannelCommandType.CREATE_CHANNEl);
        commands.add(HyperIoTWebSocketChannelCommandType.LEAVE_CHANNEL);
        commands.add(HyperIoTWebSocketChannelCommandType.JOIN_CHANNEL);
        commands.add(HyperIoTWebSocketChannelCommandType.SEND_PRIVATE_MESSAGE);
        commands.add(HyperIoTWebSocketBasicCommandType.READ_MESSAGE);
        commands.add(HyperIoTWebSocketBasicCommandType.SEND_MESSAGE);
        allowedCmds = Collections.unmodifiableSet(commands);
    }

    @Override
    public Set<HyperIoTWebSocketCommand> getAllowedCmds() {
        return allowedCmds;
    }

    @Override
    public String getRoleName() {
        return HyperIoTWebSocketChannelConstants.CHANNEL_ROLE_PARTECIPANT;
    }
}
