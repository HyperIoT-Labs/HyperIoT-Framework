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
        HyperIoTWebSocketChannelConstants.COMMAND_OSGI_FILTER + "=" + HyperIoTWebSocketChannelCommandType.UNBAN_USER_COMMAND
}, immediate = true)
public class HyperIoTWebSocketChannelUnbanCommand extends HyperIoTWebSocketChannelAbstractCommand implements HyperIoTWebSocketChannelRemoteCommand {

    @Override
    public void execute(HyperIoTWebSocketMessage message, String channelId, HyperIoTWebSocketChannelManager channelManager) {
        checkRequiredParameters(message, HyperIoTWebSocketChannelConstants.CHANNEL_MESSAGE_PARAM_BANNED_IP,HyperIoTWebSocketChannelConstants.CHANNEL_MESSAGE_PARAM_BANNED_USERNAME);
        HyperIoTWebSocketChannel channel = findChannelOrDie(channelId, channelManager);
        String senderUsername = message.getParams().get(HyperIoTWebSocketMessage.WS_MESSAGE_SENDER_PARAM_NAME);
        Optional<HyperIoTWebSocketUserInfo> bannerUserInfoOptional = channel.getPartecipantsInfo().stream().filter(userInfo -> userInfo.getUsername().equals(senderUsername)).findAny();
        if (bannerUserInfoOptional.isPresent()) {
            channelManager.unbanParticipant(channelId, bannerUserInfoOptional.get(), message);
        }
    }
}
