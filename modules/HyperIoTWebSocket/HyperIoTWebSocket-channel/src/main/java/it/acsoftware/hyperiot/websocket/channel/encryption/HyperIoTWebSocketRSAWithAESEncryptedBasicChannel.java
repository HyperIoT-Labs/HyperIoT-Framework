package it.acsoftware.hyperiot.websocket.channel.encryption;

import it.acsoftware.hyperiot.base.exception.HyperIoTRuntimeException;
import it.acsoftware.hyperiot.base.security.util.HyperIoTSecurityUtil;
import it.acsoftware.hyperiot.websocket.api.channel.HyperIoTWebSocketChannelClusterMessageBroker;
import it.acsoftware.hyperiot.websocket.api.channel.HyperIoTWebSocketChannelSession;
import it.acsoftware.hyperiot.websocket.channel.util.HyperIoTWebSocketChannelConstants;
import it.acsoftware.hyperiot.websocket.model.message.HyperIoTWebSocketMessage;
import it.acsoftware.hyperiot.websocket.model.message.HyperIoTWebSocketMessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.Map;

public class HyperIoTWebSocketRSAWithAESEncryptedBasicChannel extends HyperIoTWebSocketEncryptedBasicChannel {
    private static Logger log = LoggerFactory.getLogger(HyperIoTWebSocketRSAWithAESEncryptedBasicChannel.class);

    private byte[] aesPwd;
    private byte[] aesIv;
    private String aesPwdStr;
    private String aesIvStr;
    private String aesInfoPayload;
    private HyperIoTWebSocketMessage aesInfoMessage;

    public HyperIoTWebSocketRSAWithAESEncryptedBasicChannel(String channelName, String channelId, int maxPartecipants, Map<String, Object> channelParams, HyperIoTWebSocketChannelClusterMessageBroker clusterMessageBroker) {
        super(channelName, channelId, maxPartecipants, channelParams, clusterMessageBroker);
    }

    //used for deserialization
    private HyperIoTWebSocketRSAWithAESEncryptedBasicChannel(){
        super();
    }

    /**
     * This is invoked at channel creation.
     * We create specific key for each channel
     */
    @Override
    protected void initChannelEncryption() {
        {
            try {
                aesPwd = HyperIoTSecurityUtil.generateRandomAESPassword();
                aesIv = HyperIoTSecurityUtil.generateRandomAESInitVector();
                aesPwdStr = new String(Base64.getEncoder().encode(aesPwd));
                aesIvStr = new String(Base64.getEncoder().encode(aesIv));
                aesInfoPayload = aesPwdStr + HyperIoTWebSocketChannelConstants.WS_MESSAGE_CHANNEL_AES_DATA_SEPARATOR + aesIvStr;
                aesInfoMessage = HyperIoTWebSocketMessage.createMessage(null, aesInfoPayload.getBytes("UTF8"), HyperIoTWebSocketMessageType.SET_CHANNEL_ENCRYPTION_KEY);
            } catch (Throwable t) {
                log.error(t.getMessage(), t);
                throw new HyperIoTRuntimeException("Impossible to create channel:" + t.getMessage());
            }
        }
    }

    /**
     * This is invoked every time a partecipant joins into a specific channel
     *
     * @param session
     */
    protected void setupPartecipantEncryptedSession(HyperIoTWebSocketChannelSession session) {
        try {
            session.sendRemote(aesInfoMessage);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

}
