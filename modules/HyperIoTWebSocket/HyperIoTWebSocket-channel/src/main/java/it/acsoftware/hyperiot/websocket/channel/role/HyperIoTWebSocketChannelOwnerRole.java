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
