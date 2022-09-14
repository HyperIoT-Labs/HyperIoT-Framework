package it.acsoftware.hyperiot.websocket.channel;

public enum HyperIoTWebSocketChannelType {
    PLAIN("PLAIN"),
    ENCRYPTED_RSA_WITH_AES("ENCRYPTED_RSA_WITH_AES");

    String typeStr;

    HyperIoTWebSocketChannelType(String typeStr) {
        this.typeStr = typeStr;
    }

    public String getTypeStr() {
        return typeStr;
    }
}
