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

package it.acsoftware.hyperiot.websocket.encryption;

import it.acsoftware.hyperiot.websocket.encryption.mode.HyperIoTWebSocketEncryptionMode;
import org.eclipse.jetty.websocket.api.Session;

import java.util.Map;

/**
 * Author Aristide Cittadino
 * This class maps the concept of encryption policy, It owns an encryption mode which is responsable
 * of how messages are encrypted or decrypted.
 */
public class HyperIoTWebSocketEncryption {

    private HyperIoTWebSocketEncryptionMode mode;

    public HyperIoTWebSocketEncryption(HyperIoTWebSocketEncryptionMode mode) {
        this.mode = mode;
    }

    public byte[] encrypt(byte[] message, boolean encodeBase64) throws Exception {
        return mode.encrypt(message, encodeBase64);
    }

    public byte[] decrypt(byte[] message, boolean decodeBase64) throws Exception {
        return mode.decrypt(message);
    }

    public void updateMode(Map<String, Object> params) {
        mode.update(params);
    }

    public Map<String, Object> getModeParams() {
        return mode.getParams();
    }

    public void init(Session s) {
        mode.init(s);
    }

    public void dispose(Session s) {
        mode.dispose(s);
    }

}
