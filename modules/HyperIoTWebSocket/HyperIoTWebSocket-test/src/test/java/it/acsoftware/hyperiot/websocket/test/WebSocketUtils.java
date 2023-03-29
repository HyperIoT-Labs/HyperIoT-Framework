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

package it.acsoftware.hyperiot.websocket.test;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WriteCallback;
import org.json.JSONObject;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WebSocketUtils {
    private static Logger log = Logger.getLogger(WebSocketUtils.class.getName());

    public static boolean sendMessage(String messageSend, String cmd, String type, HashMap<String, String> params, boolean verbose, Session s, PublicKey serverPubKey) {
        JSONObject jsonMessage = createMessageFormString(messageSend, cmd, type, params, verbose);
        return sendEncryptedMessage(s, jsonMessage, serverPubKey, verbose);
    }

    public static boolean sendMessage(String messageSend, String cmd, String type, HashMap<String, String> params, boolean verbose, Session s, byte[] aesPassword, byte[] aesIv) {
        JSONObject jsonMessage = createMessageFormString(messageSend, cmd, type, params, verbose);
        return sendEncryptedMessage(s, jsonMessage, aesPassword, aesIv, verbose);
    }

    public static boolean sendMessage(String messageSend, String cmd, String type, HashMap<String, String> params, boolean verbose, Session s) {
        JSONObject jsonMessage = createMessageFormString(messageSend, cmd, type, params, verbose);
        return sendMessage(s,jsonMessage.toString());
    }

    private static JSONObject createMessageFormString(String messageSend, String cmd, String type, HashMap<String, String> params, boolean verbose) {
        JSONObject jsonMessage = new JSONObject();
        jsonMessage.put("cmd", cmd);
        jsonMessage.put("payload", messageSend.getBytes());
        jsonMessage.put("type", type);

        if (params != null) {
            JSONObject paramsJSON = new JSONObject();
            Iterator<String> it = params.keySet().iterator();
            while (it.hasNext()) {
                String name = it.next();
                String val = params.get(name);
                paramsJSON.put(name, val);
            }
            jsonMessage.put("params", paramsJSON);
        }
        return jsonMessage;
    }

    private static boolean sendEncryptedMessage(Session s, JSONObject jsonMessage, byte[] aesPassword, byte[] aesIv, boolean verbose) {
        if (s != null) {
            try {
                String messageSend = encryptWithAES(aesPassword, aesIv, jsonMessage.toString());
                return sendMessage(s, messageSend);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private static boolean sendEncryptedMessage(Session s, JSONObject jsonMessage, PublicKey serverPubKey, boolean verbose) {
        if (s != null) {
            try {
                byte[] message = encryptForServer(jsonMessage.toString().getBytes(StandardCharsets.UTF_8), serverPubKey);
                return sendMessage(s, new String(message));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean sendMessage(Session s, String message) {
        s.getRemote().sendString(new String(message), new WriteCallback() {
            @Override
            public void writeFailed(Throwable x) {
                x.printStackTrace();
            }

            @Override
            public void writeSuccess() {
                System.out.println("Message sent!");
            }
        });
        return true;
    }

    /**
     * @param aesPassword secret aes key
     * @param content     Content to encrypt
     * @return Encrypted text as a String
     * @throws InvalidKeyException          Invalid key exception
     * @throws UnsupportedEncodingException Unsupported encoding
     * @throws NoSuchPaddingException       No Such padding
     * @throws NoSuchAlgorithmException     No Such Algotithm
     */
    public static String encryptWithAES(byte[] aesPassword, byte[] initVector, String content) throws InvalidKeyException, UnsupportedEncodingException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector);
            SecretKeySpec skeySpec = new SecretKeySpec(aesPassword, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
            byte[] encrypted = cipher.doFinal(content.getBytes());
            return new String(Base64.getEncoder().encode(encrypted));
        } catch (Exception ex) {
            log.log(Level.SEVERE, ex.getMessage(), ex);
        }
        return null;
    }

    /**
     * @param aesPassword secret aes key
     * @param content     Content to decrypt
     * @return Decrypted text as a String
     * @throws InvalidKeyException      Invalid key exception
     * @throws NoSuchPaddingException   No Such padding
     * @throws NoSuchAlgorithmException No Such Algotithm
     */
    public static String decryptWithAES(byte[] aesPassword, byte[] initVector, String content) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException, InvalidAlgorithmParameterException {
        SecretKeySpec skeySpec = new SecretKeySpec(aesPassword, "AES");
        IvParameterSpec iv = new IvParameterSpec(initVector);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
        return new String(cipher.doFinal(Base64.getDecoder().decode(content)));
    }

    private static byte[] decrypt(byte[] cipherText, PublicKey pubKey) {
        try {
            Security.addProvider(new BouncyCastleProvider());
            Cipher asymmetricCipher = Cipher.getInstance("RSA/NONE/OAEPPadding", "BC");
            asymmetricCipher.init(Cipher.DECRYPT_MODE, pubKey);
            // asuming, cipherText is a byte array containing your encrypted message
            byte[] plainText = asymmetricCipher.doFinal(cipherText);
            return plainText;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static byte[] decrypt(byte[] cipherText, PrivateKey key) {
        try {
            Security.addProvider(new BouncyCastleProvider());
            Cipher asymmetricCipher = Cipher.getInstance("RSA/NONE/OAEPPadding", "BC");
            asymmetricCipher.init(Cipher.DECRYPT_MODE, key);
            byte[] plainText = asymmetricCipher.doFinal(cipherText);
            return plainText;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String decryptFromServer(String cipherText, KeyPair clientKeyPair) throws UnsupportedEncodingException {
        byte[] decode = Base64.getDecoder().decode(cipherText.getBytes("UTF8"));
        byte[] pClientMessage = decrypt(decode, clientKeyPair.getPrivate());
        return new String(pClientMessage);
    }

    public static byte[] encryptText(byte[] text, PublicKey key, boolean encodeBase64) {
        try {
            Security.addProvider(new BouncyCastleProvider());
            Cipher asymmetricCipher = Cipher.getInstance("RSA/NONE/OAEPPadding", "BC");
            asymmetricCipher.init(Cipher.ENCRYPT_MODE, key);
            if (encodeBase64)
                return Base64.getEncoder().encode(asymmetricCipher.doFinal(text));
            return asymmetricCipher.doFinal(text);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static byte[] encryptForServer(byte[] message, PublicKey serverPubKey) throws UnsupportedEncodingException {
        byte[] pServerMessage = encryptText(message, serverPubKey, true);
        return pServerMessage;
    }
}
