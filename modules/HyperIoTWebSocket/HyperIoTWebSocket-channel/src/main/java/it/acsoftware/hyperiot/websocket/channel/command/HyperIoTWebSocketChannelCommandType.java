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

import it.acsoftware.hyperiot.websocket.api.HyperIoTWebSocketBasicCommandType;
import it.acsoftware.hyperiot.websocket.api.HyperIoTWebSocketCommand;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public enum HyperIoTWebSocketChannelCommandType implements HyperIoTWebSocketCommand {

    //command used to synchronize peer on events happening on each peer of the cluster
    FOLLOW(new HyperIoTWebSocketCommand() {
        @Override
        public String getCommandName() {
            return FOLLOW_COMMAND;
        }
    }),

    CREATE_CHANNEl(new HyperIoTWebSocketCommand() {
        @Override
        public String getCommandName() {
            return CREATE_CHANNEL_COMMAND;
        }
    }),

    DELETE_CHANNEl(new HyperIoTWebSocketCommand() {
        @Override
        public String getCommandName() {
            return DELETE_CHANNEL_COMMAND;
        }
    }),

    JOIN_CHANNEL(new HyperIoTWebSocketCommand() {
        @Override
        public String getCommandName() {
            return JOIN_CHANNEL_COMMAND;
        }
    }),

    LEAVE_CHANNEL(new HyperIoTWebSocketCommand() {
        @Override
        public String getCommandName() {
            return LEAVE_CHANNEL_COMMAND;
        }
    }),

    BAN_USER(new HyperIoTWebSocketCommand() {
        @Override
        public String getCommandName() {
            return BAN_USER_COMMAND;
        }
    }),

    UNBAN_USER(new HyperIoTWebSocketCommand() {
        @Override
        public String getCommandName() {
            return UNBAN_USER_COMMAND;
        }
    }),

    KICK_USER(new HyperIoTWebSocketCommand() {
        @Override
        public String getCommandName() {
            return KICK_USER_COMMAND;
        }
    }),

    SEND_PRIVATE_MESSAGE(new HyperIoTWebSocketCommand() {
        @Override
        public String getCommandName() {
            return SEND_PRIVATE_MESSAGE_COMMAND;
        }
    }),

    SEND_MESSAGE_TO_SERVER(new HyperIoTWebSocketCommand() {
        @Override
        public String getCommandName() {
            return PROCESS_ON_SERVER_COMMAND;
        }
    });

    public static Set<HyperIoTWebSocketCommand> allCmds;

    public static final String FOLLOW_COMMAND = "FOLLOW";
    public static final String BAN_USER_COMMAND = "BAN_USER";
    public static final String UNBAN_USER_COMMAND = "UNBAN_USER";
    public static final String CREATE_CHANNEL_COMMAND = "CREATE_CHANNEL";
    public static final String DELETE_CHANNEL_COMMAND = "DELETE_CHANNEL";
    public static final String JOIN_CHANNEL_COMMAND = "JOIN_CHANNEL";
    public static final String LEAVE_CHANNEL_COMMAND = "LEAVE_CHANNEL";
    public static final String KICK_USER_COMMAND = "KICK_USER";
    public static final String SEND_PRIVATE_MESSAGE_COMMAND = "SEND_PRIVATE_MESSAGE";
    public static final String PROCESS_ON_SERVER_COMMAND = "PROCESS_ON_SERVER";

    static {
        Set<HyperIoTWebSocketCommand> commands = new HashSet<>();
        commands.add(HyperIoTWebSocketChannelCommandType.FOLLOW);
        commands.add(HyperIoTWebSocketChannelCommandType.CREATE_CHANNEl);
        commands.add(HyperIoTWebSocketChannelCommandType.DELETE_CHANNEl);
        commands.add(HyperIoTWebSocketChannelCommandType.JOIN_CHANNEL);
        commands.add(HyperIoTWebSocketChannelCommandType.LEAVE_CHANNEL);
        commands.add(HyperIoTWebSocketChannelCommandType.SEND_PRIVATE_MESSAGE);
        commands.add(HyperIoTWebSocketChannelCommandType.BAN_USER);
        commands.add(HyperIoTWebSocketChannelCommandType.KICK_USER);
        commands.add(HyperIoTWebSocketChannelCommandType.SEND_MESSAGE_TO_SERVER);
        commands.add(HyperIoTWebSocketBasicCommandType.SEND_MESSAGE);
        commands.add(HyperIoTWebSocketBasicCommandType.READ_MESSAGE);
        allCmds = Collections.unmodifiableSet(commands);
    }

    private HyperIoTWebSocketCommand cmd;

    HyperIoTWebSocketChannelCommandType(HyperIoTWebSocketCommand cmd) {
        this.cmd = cmd;
    }

    @Override
    public String getCommandName() {
        return this.cmd.getCommandName();
    }
}
