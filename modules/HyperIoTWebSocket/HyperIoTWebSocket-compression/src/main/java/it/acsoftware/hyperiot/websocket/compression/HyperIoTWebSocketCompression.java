package it.acsoftware.hyperiot.websocket.compression;

import org.eclipse.jetty.websocket.api.Session;

//TO DO: must implement compression policy
public abstract class HyperIoTWebSocketCompression {

    public abstract void init(Session s);

    public abstract void dispose(Session s);

    public abstract byte[] compress(byte[] message);

    public abstract byte[] decompress(byte[] message);
}
