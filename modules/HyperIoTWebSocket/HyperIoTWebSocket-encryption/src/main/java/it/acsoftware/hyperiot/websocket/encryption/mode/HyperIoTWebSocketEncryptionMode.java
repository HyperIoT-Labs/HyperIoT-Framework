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

package it.acsoftware.hyperiot.websocket.encryption.mode;

import org.eclipse.jetty.websocket.api.Session;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * @Author Aristide Cittadino
 * Abstract Class identifying an Encryption Mode.
 */
public abstract class HyperIoTWebSocketEncryptionMode {
    /**
     * Init method for intializing mode
     *
     * @param s
     */
    public abstract void init(Session s);

    /**
     * Called on close
     *
     * @param s
     */
    public abstract void dispose(Session s);

    /**
     * Method used to update mode (new keys received or change algorithm)
     *
     * @param params
     */
    public abstract void update(Map<String, Object> params);

    /**
     * @return Current params of the encryption mode
     */
    public abstract Map<String, Object> getParams();


    /**
     * Encrypts content
     *
     * @param plainText
     * @param encodeBase64
     * @return
     * @throws Exception
     */
    public abstract byte[] encrypt(byte[] plainText, boolean encodeBase64) throws Exception;

    /**
     * Decrypts content
     *
     * @param cipherText
     * @return
     * @throws Exception
     */
    public abstract byte[] decrypt(byte[] cipherText) throws Exception;
}
