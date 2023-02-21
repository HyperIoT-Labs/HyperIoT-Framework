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

package it.acsoftware.hyperiot.websocket.channel.session.encryption;

import it.acsoftware.hyperiot.base.security.util.HyperIoTSecurityUtil;
import it.acsoftware.hyperiot.websocket.api.channel.HyperIoTWebSocketChannelManager;
import it.acsoftware.hyperiot.websocket.channel.util.HyperIoTWebSocketChannelConstants;
import it.acsoftware.hyperiot.websocket.compression.HyperIoTWebSocketCompression;
import it.acsoftware.hyperiot.websocket.encryption.HyperIoTWebSocketEncryption;
import it.acsoftware.hyperiot.websocket.encryption.mode.HyperIoTRSAWithAESEncryptionMode;
import it.acsoftware.hyperiot.websocket.model.HyperIoTWebSocketUserInfo;
import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class HyperIoTWebSocketChannelRSAWithAESEncryptedSession extends HyperIoTWebSocketChannelEncryptedSession {

    private static Logger log = LoggerFactory.getLogger(HyperIoTWebSocketChannelRSAWithAESEncryptedSession.class);
    private Map<String, Object> params;

    public HyperIoTWebSocketChannelRSAWithAESEncryptedSession(Session session, boolean authenticationRequired, HyperIoTWebSocketChannelManager channelManager) {
        super(session, authenticationRequired, channelManager);
    }

    public HyperIoTWebSocketChannelRSAWithAESEncryptedSession(Session session, boolean authenticated, HyperIoTWebSocketEncryption encryptionPolicy, HyperIoTWebSocketChannelManager channelManager) {
        super(session, authenticated, encryptionPolicy, channelManager);
    }

    public HyperIoTWebSocketChannelRSAWithAESEncryptedSession(Session session, boolean authenticated, HyperIoTWebSocketCompression compressionPolicy, HyperIoTWebSocketChannelManager channelManager) {
        super(session, authenticated, compressionPolicy, channelManager);
    }

    public HyperIoTWebSocketChannelRSAWithAESEncryptedSession(Session session, boolean authenticated, HyperIoTWebSocketEncryption encryptionPolicy, HyperIoTWebSocketCompression compressionPolicy, HyperIoTWebSocketChannelManager channelManager) {
        super(session, authenticated, encryptionPolicy, compressionPolicy, channelManager);
    }

    @Override
    protected String defineEncryptionMessage() {
        try {
            byte[] aesPwd = HyperIoTSecurityUtil.generateRandomAESPassword();
            byte[] aesIv = HyperIoTSecurityUtil.generateRandomAESInitVector();
            //sending <password:iv>
            String aesPwdStr = new String(Base64.getEncoder().encode(aesPwd));
            String aesIvStr = new String(Base64.getEncoder().encode(aesIv));
            String aesInfoPayload = aesPwdStr + HyperIoTWebSocketChannelConstants.WS_MESSAGE_CHANNEL_AES_DATA_SEPARATOR + aesIvStr;
            params = new HashMap<>();
            params.put(HyperIoTRSAWithAESEncryptionMode.MODE_PARAM_AES_PASSWORD, aesPwd);
            params.put(HyperIoTRSAWithAESEncryptionMode.MODE_PARAM_AES_IV, aesIv);
            return aesInfoPayload;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    @Override
    protected Map<String, Object> defineEncryptionPolicyParams() {
        return params;
    }
}
