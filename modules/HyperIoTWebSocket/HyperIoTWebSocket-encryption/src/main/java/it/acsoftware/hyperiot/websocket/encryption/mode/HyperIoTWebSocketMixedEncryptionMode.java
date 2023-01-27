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

import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * @Author Aristide Cittadino
 * MixedEncryptionMode rappresents symmetric and asymmetric encryption methods combined.
 * In the first phase, client will exchange their symmetric password using an asymmetric encryption.
 * Then the communication will continuo with symmetric cryptography.
 */
public abstract class HyperIoTWebSocketMixedEncryptionMode extends HyperIoTWebSocketEncryptionMode {
    private PublicKey publicKey;
    private PrivateKey privateKey;
    private byte[] symmetricPassword;
    private byte[] symmetricIv;

    /**
     * @return
     */
    public Key getPublicKey() {
        return publicKey;
    }

    /**
     * @param publicKey
     */
    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    /**
     * @return
     */
    public Key getPrivateKey() {
        return privateKey;
    }

    /**
     * @param privateKey
     */
    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    /**
     * @return
     */
    public byte[] getSymmetricPassword() {
        return symmetricPassword;
    }

    /**
     * @param symmetricPassword
     */
    public void setSymmetricPassword(byte[] symmetricPassword) {
        this.symmetricPassword = symmetricPassword;
    }

    /**
     * @return
     */
    public byte[] getSymmetricIv() {
        return symmetricIv;
    }

    /**
     * @param symmetricIv
     */
    public void setSymmetricIv(byte[] symmetricIv) {
        this.symmetricIv = symmetricIv;
    }

    /**
     * @param plainText
     * @param encodeBase64
     * @return
     * @throws Exception
     */
    @Override
    public byte[] encrypt(byte[] plainText, boolean encodeBase64) throws Exception {
        if (this.symmetricPassword == null) {
            return encryptAsymmetric(this.publicKey, plainText, encodeBase64);
        } else {
            return encryptSymmetric(symmetricPassword, symmetricIv, new String(plainText));
        }
    }

    /**
     * @param symmetricPassword
     * @param symmetricIv
     * @param s
     * @return
     */
    protected abstract byte[] encryptSymmetric(byte[] symmetricPassword, byte[] symmetricIv, String s) throws Exception;

    /**
     * @param publicKey
     * @param plainText
     * @param encodeBase64
     * @return
     */
    protected abstract byte[] encryptAsymmetric(PublicKey publicKey, byte[] plainText, boolean encodeBase64) throws Exception;

    /**
     * @param plainText
     * @return
     * @throws Exception
     */
    @Override
    public byte[] decrypt(byte[] plainText) throws Exception {
        if (this.symmetricPassword == null) {
            return decryptAsymmetric(this.privateKey, plainText);
        } else {
            return decryptSymmetric(symmetricPassword, symmetricIv, new String(plainText));
        }
    }

    /**
     * @param symmetricPassword
     * @param symmetricIv
     * @param s
     * @return
     */
    protected abstract byte[] decryptSymmetric(byte[] symmetricPassword, byte[] symmetricIv, String s) throws Exception;

    /**
     * @param privateKey
     * @param plainText
     * @return
     */
    protected abstract byte[] decryptAsymmetric(PrivateKey privateKey, byte[] plainText) throws Exception;
}
