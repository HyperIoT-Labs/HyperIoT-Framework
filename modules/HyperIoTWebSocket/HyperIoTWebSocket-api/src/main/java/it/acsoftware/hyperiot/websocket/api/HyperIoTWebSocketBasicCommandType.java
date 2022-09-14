package it.acsoftware.hyperiot.websocket.api;

import it.acsoftware.hyperiot.websocket.api.HyperIoTWebSocketCommand;

public enum HyperIoTWebSocketBasicCommandType implements HyperIoTWebSocketCommand {

    READ_MESSAGE(new HyperIoTWebSocketCommand() {
        @Override
        public String getCommandName() {
            return READ_MESSAGE_COMMAND;
        }
    }),

    SEND_MESSAGE(new HyperIoTWebSocketCommand() {
        @Override
        public String getCommandName() {
            return SEND_MESSAGE_COMMAND;
        }
    });

    private HyperIoTWebSocketCommand cmd;

    public static final String READ_MESSAGE_COMMAND = "READ_MESSAGE";
    public static final String SEND_MESSAGE_COMMAND = "SEND_MESSAGE";

    HyperIoTWebSocketBasicCommandType(HyperIoTWebSocketCommand cmd) {
        this.cmd = cmd;
    }

    @Override
    public String getCommandName() {
        return this.cmd.getCommandName();
    }
}
