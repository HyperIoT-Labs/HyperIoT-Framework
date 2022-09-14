package it.acsoftware.hyperiot.websocket.channel.command;

import it.acsoftware.hyperiot.websocket.api.HyperIoTWebSocketBasicCommandType;
import it.acsoftware.hyperiot.websocket.api.channel.HyperIoTWebSocketChannelCommand;
import it.acsoftware.hyperiot.websocket.api.channel.HyperIoTWebSocketChannelManager;
import it.acsoftware.hyperiot.websocket.api.channel.HyperIoTWebSocketChannelRemoteCommand;
import it.acsoftware.hyperiot.websocket.channel.util.HyperIoTWebSocketChannelConstants;
import it.acsoftware.hyperiot.websocket.model.message.HyperIoTWebSocketMessage;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = {HyperIoTWebSocketChannelCommand.class, HyperIoTWebSocketChannelRemoteCommand.class}, property = {
        HyperIoTWebSocketChannelConstants.COMMAND_OSGI_FILTER + "=" + HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND
}, immediate = true)
public class HyperIoTWebSocketChannelReadMessageCommand extends HyperIoTWebSocketChannelAbstractCommand implements HyperIoTWebSocketChannelRemoteCommand {
    private static Logger log = LoggerFactory.getLogger(HyperIoTWebSocketChannelReadMessageCommand.class);

    @Override
    public void execute(HyperIoTWebSocketMessage message, String channelId, HyperIoTWebSocketChannelManager channelManager) {
        checkRequiredParameters(message, HyperIoTWebSocketChannelConstants.CHANNEL_ID_PARAM);
        channelManager.deliverMessage(message);
    }
}
