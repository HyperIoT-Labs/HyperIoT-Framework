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

package it.acsoftware.hyperiot.websocket.test.client;

import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;

import java.security.KeyPair;
import java.security.PublicKey;

public class HyperIoTChannelParticipanPlain extends HyperIoTChannelParticipant {

    private KeyPair clientKeyPair;
    private PublicKey serverPublicKey;

    public HyperIoTChannelParticipanPlain(String alias,String websocketBaseUrl, HyperIoTChannelWebSocketClient client, int numThreads, boolean verbose) {
        super(alias,websocketBaseUrl, client, numThreads, verbose);
    }

    @Override
    protected void setupRequestHeaders(ClientUpgradeRequest request) {
        //do nothing
    }
}
