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
