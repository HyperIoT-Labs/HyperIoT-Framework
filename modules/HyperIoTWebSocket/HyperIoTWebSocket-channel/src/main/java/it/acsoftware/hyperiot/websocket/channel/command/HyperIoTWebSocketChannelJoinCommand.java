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
