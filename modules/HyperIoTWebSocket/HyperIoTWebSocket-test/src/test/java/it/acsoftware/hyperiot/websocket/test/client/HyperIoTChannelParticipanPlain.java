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
