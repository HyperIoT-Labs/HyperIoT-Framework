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


import it.acsoftware.hyperiot.websocket.encryption.mode.HyperIoTRSAWithAESEncryptionMode;

/**
 * @Author Aristide Cittadino
 * Factory for creating alla available Encryption Policies for websockets
 */
public class HyperIoTWebSocketEncryptionFactory {

    /**
     * @return
     */
    public static HyperIoTWebSocketEncryption createRSAAndAESEncryptionPolicy() {
        HyperIoTRSAWithAESEncryptionMode rsaAndAesMode = new HyperIoTRSAWithAESEncryptionMode();
        HyperIoTWebSocketEncryption rsaAndAes = new HyperIoTWebSocketEncryption(rsaAndAesMode);
        return rsaAndAes;
    }
}
