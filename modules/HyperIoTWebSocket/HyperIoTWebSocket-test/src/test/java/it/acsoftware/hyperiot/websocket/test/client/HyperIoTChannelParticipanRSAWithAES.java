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

package it.acsoftware.hyperiot.websocket.test.client;

import it.acsoftware.hyperiot.websocket.test.WebSocketUtils;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Base64;

public class HyperIoTChannelParticipanRSAWithAES extends HyperIoTChannelParticipant {
    private static Logger log = LoggerFactory.getLogger(HyperIoTChannelParticipanRSAWithAES.class);

    private KeyPair clientKeyPair;
    private PublicKey serverPublicKey;

    public HyperIoTChannelParticipanRSAWithAES(String alias,String websocketBaseUrl, HyperIoTChannelWebSocketClient client, int numThreads, boolean verbose, KeyPair clientKeyPair, PublicKey serverPublicKey) {
        super(alias, websocketBaseUrl, client, numThreads, verbose);
        this.clientKeyPair = clientKeyPair;
        this.serverPublicKey = serverPublicKey;
    }

    @Override
    protected void setupRequestHeaders(ClientUpgradeRequest request) {
        try {
            String clientPubKey = new String(Base64.getEncoder().encode(clientKeyPair.getPublic().getEncoded()));
            String clientPubKeyEncrypted = new String(WebSocketUtils.encryptForServer(clientPubKey.getBytes("UTF8"), serverPublicKey));
            request.setHeader("X-HYPERIOT-CLIENT-PUB-KEY", clientPubKeyEncrypted);
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage(), e);
        }
    }
}
