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

package it.acsoftware.hyperiot.websocket.channel.role;

import it.acsoftware.hyperiot.base.util.HyperIoTConstants;
import it.acsoftware.hyperiot.websocket.api.HyperIoTWebSocketCommand;
import it.acsoftware.hyperiot.websocket.api.channel.HyperIoTWebSocketChannelRole;
import it.acsoftware.hyperiot.websocket.channel.command.HyperIoTWebSocketChannelCommandType;
import it.acsoftware.hyperiot.websocket.channel.util.HyperIoTWebSocketChannelConstants;
import org.osgi.service.component.annotations.Component;

import java.util.Set;

@Component(service = HyperIoTWebSocketChannelRole.class, property = {
        HyperIoTConstants.OSGI_WEBSOCKET_CHANNEL_ROLE_NAME + "="+ HyperIoTWebSocketChannelConstants.CHANNEL_ROLE_OWNER
},immediate = true)
public class HyperIoTWebSocketChannelOwnerRole implements HyperIoTWebSocketChannelRole {

    @Override
    public Set<HyperIoTWebSocketCommand> getAllowedCmds() {
        return HyperIoTWebSocketChannelCommandType.allCmds;
    }

    @Override
    public boolean isOwner() {
        return true;
    }

    @Override
    public String getRoleName() {
        return HyperIoTWebSocketChannelConstants.CHANNEL_ROLE_OWNER;
    }
}
