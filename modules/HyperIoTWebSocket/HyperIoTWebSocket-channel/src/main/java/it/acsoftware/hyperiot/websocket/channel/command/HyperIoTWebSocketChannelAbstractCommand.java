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

package it.acsoftware.hyperiot.websocket.channel.command;

import it.acsoftware.hyperiot.base.exception.HyperIoTRuntimeException;
import it.acsoftware.hyperiot.websocket.api.channel.HyperIoTWebSocketChannel;
import it.acsoftware.hyperiot.websocket.api.channel.HyperIoTWebSocketChannelCommand;
import it.acsoftware.hyperiot.websocket.api.channel.HyperIoTWebSocketChannelManager;
import it.acsoftware.hyperiot.websocket.model.message.HyperIoTWebSocketMessage;

import java.util.Arrays;

public abstract class HyperIoTWebSocketChannelAbstractCommand implements HyperIoTWebSocketChannelCommand {

    void checkRequiredParameters(HyperIoTWebSocketMessage message, String... paramsNames) {
        Arrays.asList(paramsNames).stream().forEach(paramName -> {
            if (!message.getParams().containsKey(paramName) || message.getParams().get(paramName) == null)
                throw new HyperIoTRuntimeException("Param name: " + paramName + " is required!");
        });
    }

    HyperIoTWebSocketChannel findChannelOrDie(String channelId, HyperIoTWebSocketChannelManager channelManager) {
        HyperIoTWebSocketChannel channel = channelManager.findChannel(channelId);
        if (channel == null)
            throw new HyperIoTRuntimeException("Channel " + channelId + " not found!");
        return channel;
    }
}
