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
