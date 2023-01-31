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

package it.acsoftware.hyperiot.websocket.channel.factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.acsoftware.hyperiot.base.exception.HyperIoTRuntimeException;
import it.acsoftware.hyperiot.websocket.api.channel.HyperIoTWebSocketChannel;
import it.acsoftware.hyperiot.websocket.api.channel.HyperIoTWebSocketChannelClusterMessageBroker;
import it.acsoftware.hyperiot.websocket.channel.HyperIoTWebSocketBasicChannel;
import it.acsoftware.hyperiot.websocket.channel.HyperIoTWebSocketChannelType;
import it.acsoftware.hyperiot.websocket.channel.encryption.HyperIoTWebSocketRSAWithAESEncryptedBasicChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public class HyperIoTWebSocketChannelFactory {
    private static Logger log = LoggerFactory.getLogger(HyperIoTWebSocketChannelFactory.class);

    private static ObjectMapper mapper = new ObjectMapper();

    public static HyperIoTWebSocketChannel createChannelFromChannelType(String channelType, String channelId, String channelName, int maxPartecipants, Map<String, Object> params, HyperIoTWebSocketChannelClusterMessageBroker clusterMessageBroker) {
        HyperIoTWebSocketChannelType type = HyperIoTWebSocketChannelType.valueOf(channelType);
        switch (type) {
            case PLAIN:
                return new HyperIoTWebSocketBasicChannel(channelName, channelId, maxPartecipants, params, clusterMessageBroker);
            case ENCRYPTED_RSA_WITH_AES:
                return new HyperIoTWebSocketRSAWithAESEncryptedBasicChannel(channelName, channelId, maxPartecipants, params, clusterMessageBroker);
            default:
                throw new HyperIoTRuntimeException("Invalid channel type");
        }
    }

    public static <T extends HyperIoTWebSocketChannel> HyperIoTWebSocketChannel createChannelFromClass(Class<T> classType, String channelId, String channelName, int maxPartecipants, Map<String, Object> params, HyperIoTWebSocketChannelClusterMessageBroker clusterMessageBroker) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        return (HyperIoTWebSocketChannel) classType.getDeclaredConstructors()[0].newInstance(channelId, channelName, maxPartecipants, params, clusterMessageBroker);
    }

    public static <T extends HyperIoTWebSocketChannel> HyperIoTWebSocketChannel createFromString(String channelJson, String classNameStr, HyperIoTWebSocketChannelClusterMessageBroker clusterMessageBroker) {
        try {
            Class<? extends HyperIoTWebSocketChannel> channelClassType = (Class<? extends HyperIoTWebSocketChannel>) Class.forName(classNameStr);
            HyperIoTWebSocketChannel channel = mapper.readValue(channelJson, channelClassType);
            channel.defineClusterMessageBroker(clusterMessageBroker);
            return channel;
        } catch (Throwable t) {
            log.debug("Error while parsing websocket channel: {}", new Object[]{t.getMessage()});
        }
        return null;
    }
}
