package it.acsoftware.hyperiot.websocket.session;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.acsoftware.hyperiot.websocket.compression.HyperIoTWebSocketCompression;
import it.acsoftware.hyperiot.websocket.encryption.HyperIoTWebSocketEncryption;
import it.acsoftware.hyperiot.websocket.model.message.HyperIoTWebSocketMessage;
import it.acsoftware.hyperiot.websocket.model.message.HyperIoTWebSocketMessageType;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WriteCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

/**
 * Component used to send messages over websocket.
 * This component allows to specify custom behaviour for communication as, for example, encryption or compression.
 */
public class HyperIoTWebSocketMessageBroker {

    private Logger log = LoggerFactory.getLogger("it.ascsoftware.hyperiot");
    private Session session;
    private static ObjectMapper mapper = new ObjectMapper();

    private HyperIoTWebSocketEncryption encryptionPolicy;
    private HyperIoTWebSocketCompression compressionPolicy;

    public HyperIoTWebSocketMessageBroker(Session session) {
        this.session = session;
    }

    /**
     * @param encryptionPolicy
     */
    public void setEncryptionPolicy(HyperIoTWebSocketEncryption encryptionPolicy) {
        this.encryptionPolicy = encryptionPolicy;
    }

    /**
     * @param compressionPolicy
     */
    public void setCompressionPolicy(HyperIoTWebSocketCompression compressionPolicy) {
        this.compressionPolicy = compressionPolicy;
    }

    /**
     * Method invoked by WebSocket at session opening
     *
     * @param s
     */
    public void onOpenSession(Session s) {
        if (encryptionPolicy != null)
            encryptionPolicy.init(s);

        if (compressionPolicy != null)
            compressionPolicy.init(s);
    }

    /**
     * Method invoked by websocket at session close
     *
     * @param s
     */
    public void onCloseSession(Session s) {
        if (encryptionPolicy != null)
            encryptionPolicy.dispose(s);

        if (compressionPolicy != null)
            compressionPolicy.dispose(s);
    }

    /**
     * @param message a String rappresenting a HyperIoTWebSocketMessage
     * @return HyperIoTWebSocketMessage parse from string
     */
    public HyperIoTWebSocketMessage read(String message) {
        try {
            byte[] tmpMessage = message.getBytes("UTF8");
            tmpMessage = processMessageBeforeRead(tmpMessage, true);
            String finalMessage = new String(tmpMessage);
            HyperIoTWebSocketMessage m = HyperIoTWebSocketMessage.fromString(finalMessage);
            return m;
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
        return null;
    }

    /**
     * @param message                   raw bytes of message
     * @param decodeBase64BeforeDecrypt if the message is base64 encoded
     * @return bytes rappresenting the original message in plain text (so it is eventually decrypted and decompressed)
     */
    public byte[] readRaw(byte[] message, boolean decodeBase64BeforeDecrypt) {
        try {
            return processMessageBeforeRead(message, decodeBase64BeforeDecrypt);
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
        return null;
    }

    /**
     * This method assume the incoming message is base64 encoded
     *
     * @param message Raw String message to eventually decrypt and decompress
     * @return bytes rappresenting the original message in plain text (so it is eventually decrypted and decompressed)
     */
    public byte[] readRaw(String message) {
        try {
            return this.readRaw(message, true);
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
        return null;
    }

    /**
     * This method assume the incoming message is base64 encoded
     *
     * @param message                   Raw String message to eventually decrypt and decompress
     * @param decodeBase64BeforeDecrypt if the message is base64 encoded and it must be decoded before processing it
     * @return bytes rappresenting the original message in plain text (so it is eventually decrypted and decompressed)
     */
    public byte[] readRaw(String message, boolean decodeBase64BeforeDecrypt) {
        try {
            return this.readRaw(message.getBytes("UTF8"), decodeBase64BeforeDecrypt);
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
        return null;
    }


    /**
     * Sends message over websocket in async mode
     *
     * @param message HyperIoTWebSocket message to send
     */
    public void sendAsync(HyperIoTWebSocketMessage message) {
        this.sendAsync(message, null);
    }

    /**
     * Sends message over websocket in async mode
     *
     * @param message       HyperIoTWebSocket message to send
     * @param writeCallback callback for async mode
     */
    public void sendAsync(HyperIoTWebSocketMessage message, WriteCallback writeCallback) {
        this.sendAsync(message, true, writeCallback);
    }

    /**
     * Sends message over websocket in async mode
     *
     * @param message                HyperIoTWebSocket message to send
     * @param encodeBase64BeforeSend if message must be encoded in base64 before sending it
     * @param writeCallback          callback for async mode
     */
    public void sendAsync(HyperIoTWebSocketMessage message, boolean encodeBase64BeforeSend, WriteCallback writeCallback) {
        try {
            this.sendAsync(mapper.writeValueAsString(message).getBytes("UTF8"), encodeBase64BeforeSend, writeCallback);
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            try {
                HyperIoTWebSocketMessage m = new HyperIoTWebSocketMessage();
                m.setType(HyperIoTWebSocketMessageType.ERROR);
                if (e.getMessage() != null)
                    m.setPayload(e.getMessage().getBytes());
                m.setCmd("");
                m.setTimestamp(new Date());
                this.sendAsync((mapper.writeValueAsString(m)));
            } catch (Throwable e1) {
                log.error(e1.getMessage(), e1);
            }
        }
    }

    /**
     * Sends message over websocket in async mode
     *
     * @param message      raw bytes message to send
     * @param encodeBase64 if encryption is enabled this boolean forces encoding after encryptiom.
     *                     If it is not enabled the message is encode in base64 before sending it through the websocket
     * @param callback     callback for async mode
     */
    public void sendAsync(byte[] message, boolean encodeBase64, WriteCallback callback) {
        try {
            message = processMessageBeforeSend(message, encodeBase64);
            String finalMessage = new String(message);
            if (callback != null) {
                session.getRemote().sendString(finalMessage, callback);
            } else {
                session.getRemote().sendString(finalMessage, null);
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }

    /**
     * @param message message to send
     */
    public void sendAsync(String message) {
        //Avoiding blocking I/O network traffic for multiple threads
        this.sendAsync(message, null);
    }

    /**
     * Overidable method for customizing message encryption
     *
     * @param message  message to send
     * @param callback callback for async send
     */
    public void sendAsync(String message, WriteCallback callback) {
        try {
            //Avoiding blocking I/O network traffic for multiple threads
            this.sendAsync(message.getBytes("UTF8"), true, callback);
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }

    /**
     * @param message data to send as a reason of websocket closing
     */
    public void closeSessionWithMessage(HyperIoTWebSocketMessage message) {
        String messageStr = null;
        try {
            messageStr = new String(processMessageBeforeSend(mapper.writeValueAsString(message).getBytes("UTF8"), false));
            this.session.close(500, messageStr);
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            this.session.close();
        }
    }

    /**
     * @param params
     */
    public void updateEncryptionPolicyParams(Map<String, Object> params) {
        if (this.encryptionPolicy != null)
            this.encryptionPolicy.updateMode(params);
    }

    /**
     * @return
     */
    public Map<String, Object> getEncryptionPolicyParams() {
        if (this.encryptionPolicy != null)
            return this.encryptionPolicy.getModeParams();
        return null;
    }

    /**
     * @param message
     * @param encodeBase64
     * @return
     * @throws NoSuchPaddingException
     * @throws InvalidAlgorithmParameterException
     * @throws UnsupportedEncodingException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    private byte[] processMessageBeforeSend(byte[] message, boolean encodeBase64) throws Exception {
        if (encryptionPolicy != null) {
            message = encryptionPolicy.encrypt(message, encodeBase64);
        } else if (encodeBase64) {
            message = Base64.getEncoder().encode(message);
        }

        if (compressionPolicy != null) {
            message = compressionPolicy.compress(message);
        }
        return message;
    }

    /**
     * @param message
     * @param decodeBase64BeforeDecrypt
     * @return
     * @throws NoSuchPaddingException
     * @throws InvalidAlgorithmParameterException
     * @throws UnsupportedEncodingException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    private byte[] processMessageBeforeRead(byte[] message, boolean decodeBase64BeforeDecrypt) throws Exception {
        if (compressionPolicy != null) {
            message = this.compressionPolicy.decompress(message);
        }
        if (encryptionPolicy != null)
            message = this.encryptionPolicy.decrypt(message, decodeBase64BeforeDecrypt);

        return message;
    }

    public boolean hasEncryption() {
        return encryptionPolicy != null;
    }

    public boolean hasCompression() {
        return compressionPolicy != null;
    }
}
