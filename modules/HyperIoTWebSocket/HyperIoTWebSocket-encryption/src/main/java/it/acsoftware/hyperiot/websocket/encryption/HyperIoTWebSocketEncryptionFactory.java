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
