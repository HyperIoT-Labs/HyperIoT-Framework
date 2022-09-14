package it.acsoftware.hyperiot.websocket.channel.command;

import it.acsoftware.hyperiot.base.exception.HyperIoTRuntimeException;
import it.acsoftware.hyperiot.base.util.HyperIoTUtil;
import it.acsoftware.hyperiot.osgi.util.filter.OSGiFilter;
import it.acsoftware.hyperiot.osgi.util.filter.OSGiFilterBuilder;
import it.acsoftware.hyperiot.websocket.api.channel.HyperIoTWebSocketChannelCommand;
import it.acsoftware.hyperiot.websocket.api.channel.HyperIoTWebSocketChannelRemoteCommand;
import it.acsoftware.hyperiot.websocket.channel.util.HyperIoTWebSocketChannelConstants;
import org.osgi.framework.ServiceReference;

public class HyperIoTWebSocketChannelCommandFactory {

    private static <T extends HyperIoTWebSocketChannelCommand> HyperIoTWebSocketChannelCommand createCommand(String commandStr, Class<T> commandInterface) {
        OSGiFilter filter = OSGiFilterBuilder.createFilter(HyperIoTWebSocketChannelConstants.COMMAND_OSGI_FILTER, commandStr);
        ServiceReference<HyperIoTWebSocketChannelCommand>[] refs = HyperIoTUtil.getServices(commandInterface, filter.getFilter());
        if (refs != null && refs.length > 0) {
            if (refs.length > 1)
                throw new HyperIoTRuntimeException("Commands conflict! two or more command are associated with " + commandStr + " please check it!");
            HyperIoTWebSocketChannelCommand command = HyperIoTUtil.getBundleContext(HyperIoTWebSocketChannelCommandFactory.class).getService(refs[0]);
            return command;
        }
        throw new HyperIoTRuntimeException("No hyperiot web socket channel command found for string: " + commandStr);
    }

    public static HyperIoTWebSocketChannelCommand createCommand(String commandStr) {
        return createCommand(commandStr, HyperIoTWebSocketChannelCommand.class);
    }

    public static HyperIoTWebSocketChannelRemoteCommand createRemoteCommand(String commandStr) {
        return (HyperIoTWebSocketChannelRemoteCommand)createCommand(commandStr, HyperIoTWebSocketChannelRemoteCommand.class);
    }
}
