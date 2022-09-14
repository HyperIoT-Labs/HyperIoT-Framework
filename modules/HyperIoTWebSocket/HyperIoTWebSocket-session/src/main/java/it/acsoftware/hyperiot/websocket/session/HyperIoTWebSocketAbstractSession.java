package it.acsoftware.hyperiot.websocket.session;

import it.acsoftware.hyperiot.base.api.HyperIoTJwtContext;
import it.acsoftware.hyperiot.base.security.rest.HyperIoTAuthenticationFilter;
import it.acsoftware.hyperiot.base.util.HyperIoTConstants;
import it.acsoftware.hyperiot.base.util.HyperIoTUtil;
import it.acsoftware.hyperiot.websocket.api.HyperIoTWebSocketSession;
import it.acsoftware.hyperiot.websocket.compression.HyperIoTWebSocketCompression;
import it.acsoftware.hyperiot.websocket.encryption.HyperIoTWebSocketEncryption;
import it.acsoftware.hyperiot.websocket.model.HyperIoTWebSocketUserInfo;
import it.acsoftware.hyperiot.websocket.model.message.HyperIoTWebSocketMessage;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WriteCallback;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import java.net.HttpCookie;

/**
 * Author Generoso Martello
 * This class implements the concept of a Web Socket Session
 */
public abstract class HyperIoTWebSocketAbstractSession implements HyperIoTWebSocketSession {
    private static Logger log = LoggerFactory.getLogger(HyperIoTWebSocketAbstractSession.class.getName());

    private Session session;
    private HyperIoTJwtContext context;
    private BundleContext bundleContext;
    private boolean authenticationRequired;
    private HyperIoTWebSocketMessageBroker messageBroker;
    private HyperIoTWebSocketUserInfo userInfo;

    private HyperIoTAuthenticationFilter hyperIoTAuthenticationFilter;

    /**
     * @param session
     */
    public HyperIoTWebSocketAbstractSession(Session session, boolean authenticationRequired) {
        log.debug("Creating websocket session....");
        this.session = session;
        bundleContext = HyperIoTUtil.getBundleContext(this);
        this.authenticationRequired = authenticationRequired;
        this.initMessageBroker(session, null, null);
    }

    /**
     * @param session
     * @param authenticated
     * @param encryptionPolicy
     */
    public HyperIoTWebSocketAbstractSession(Session session, boolean authenticated, HyperIoTWebSocketEncryption encryptionPolicy) {
        this(session, authenticated);
        initMessageBroker(session, encryptionPolicy, null);
    }

    /**
     * @param session
     * @param authenticated
     * @param compressionPolicy
     */
    public HyperIoTWebSocketAbstractSession(Session session, boolean authenticated, HyperIoTWebSocketCompression compressionPolicy) {
        this(session, authenticated);
        initMessageBroker(session, null, compressionPolicy);
    }

    /**
     * @param session
     * @param authenticated
     * @param encryptionPolicy
     * @param compressionPolicy
     */
    public HyperIoTWebSocketAbstractSession(Session session, boolean authenticated, HyperIoTWebSocketEncryption encryptionPolicy, HyperIoTWebSocketCompression compressionPolicy) {
        this(session, authenticated);
        initMessageBroker(session, encryptionPolicy, compressionPolicy);
    }

    /**
     * @param s
     * @param encryptionPolicy
     * @param compressionPolicy
     */
    private void initMessageBroker(Session s, HyperIoTWebSocketEncryption encryptionPolicy, HyperIoTWebSocketCompression compressionPolicy) {
        this.messageBroker = new HyperIoTWebSocketMessageBroker(s);
        this.messageBroker.setEncryptionPolicy(encryptionPolicy);
        this.messageBroker.setCompressionPolicy(compressionPolicy);

        if (encryptionPolicy != null)
            this.messageBroker.setEncryptionPolicy(encryptionPolicy);

        if (compressionPolicy != null)
            this.messageBroker.setCompressionPolicy(compressionPolicy);

        this.messageBroker.onOpenSession(s);
    }

    /**
     * @return
     */
    public Session getSession() {
        return session;
    }

    /**
     * @return
     */
    protected HyperIoTWebSocketMessageBroker getMessageBroker() {
        return messageBroker;
    }

    /**
     * @return
     */
    protected BundleContext getBundleContext() {
        return bundleContext;
    }

    /**
     * @return
     */
    protected HyperIoTJwtContext getContext() {
        return context;
    }

    /**
     *
     */
    public void dispose() {
        this.getMessageBroker().onCloseSession(this.getSession());
        try {
            session.close();
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
        }
    }

    /**
     * @return
     */
    public boolean isAuthenticated() {
        return this.authenticationRequired && this.context != null && context.getLoggedUsername() != null && !context.getLoggedUsername().isEmpty();
    }

    /**
     * @return
     */
    public boolean isAuthenticationRequired() {
        return authenticationRequired;
    }

    /**
     * @param m
     */
    public void sendRemote(HyperIoTWebSocketMessage m) {
        this.sendRemote(m, true, null);
    }

    /**
     * @param m
     * @param callback
     */
    public void sendRemote(HyperIoTWebSocketMessage m, WriteCallback callback) {
        this.sendRemote(m, true, callback);
    }

    /**
     * @param message
     * @param callback
     */
    public void sendRemote(String message, WriteCallback callback) {
        this.sendRemote(message, true, callback);
    }

    /**
     * @param message
     */
    public void sendRemote(String message) {
        this.sendRemote(message, true);
    }

    /**
     * @param m
     */
    public void sendRemote(HyperIoTWebSocketMessage m, boolean encodeBase64) {
        this.messageBroker.sendAsync(m, encodeBase64, null);
    }

    /**
     * @param m
     * @param callback
     */
    public void sendRemote(HyperIoTWebSocketMessage m, boolean encodeBase64, WriteCallback callback) {
        this.messageBroker.sendAsync(m, encodeBase64, callback);
    }

    /**
     * @param message
     * @param callback
     */
    public void sendRemote(String message, boolean encodeBase64, WriteCallback callback) {
        this.messageBroker.sendAsync(message.getBytes(), encodeBase64, callback);
    }

    /**
     * @param message
     */
    public void sendRemote(String message, boolean encodeBase64) {
        this.messageBroker.sendAsync(message.getBytes(), encodeBase64, null);
    }

    public final void authenticate() {
        preAuthenticate(session);
        String username = doAuthenticate();
        this.userInfo = HyperIoTWebSocketUserInfo.fromSession(username,session);
        postAuthenticate(session);
    }

    @Override
    public void authenticateAnonymous() {
        this.userInfo = HyperIoTWebSocketUserInfo.anonymous(session);
    }

    /**
     *
     */
    protected String doAuthenticate() {
        try {
            if (this.bundleContext == null)
                bundleContext = HyperIoTUtil.getBundleContext(this);
            ServiceReference serviceReference = bundleContext
                    .getAllServiceReferences(ContainerRequestFilter.class.getName(), "(org.apache.cxf.dosgi.IntentName=jwtAuthFilter)")[0];
            hyperIoTAuthenticationFilter = (HyperIoTAuthenticationFilter) bundleContext
                    .getService(serviceReference);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        String jwtToken = null;

        log.debug("Checking Auth token in cookies or headers");
        if (session.getUpgradeRequest()
                .getCookies() != null && session.getUpgradeRequest()
                .getCookies().size() > 0) {
            log.debug("Token found in cookies");
            HttpCookie cookie = session.getUpgradeRequest()
                    .getCookies()
                    .stream()
                    .filter((c) -> c.getName().equals(HyperIoTConstants.HYPERIOT_AUTHORIZATION_COOKIE_NAME))
                    .findAny().orElse(null);

            if (cookie != null) {
                log.debug("Cookie found, checking authentication...");
                jwtToken = cookie.getValue();

            }
        } else if (session.getUpgradeRequest().getHeader(HttpHeaders.AUTHORIZATION) != null) {
            log.debug("token found in header");
            jwtToken = session.getUpgradeRequest().getHeader(HttpHeaders.AUTHORIZATION).replace("JWT ", "");
        }

        if (jwtToken != null) {
            HyperIoTJwtContext hyperIoTContext = hyperIoTAuthenticationFilter.doApplicationFilter(jwtToken);
            if (hyperIoTContext != null) {
                context = hyperIoTContext;
            }
        }

        if (this.context == null) {
            log.debug("User not authorized to connect to websocket");
            //Closes the connection if the client is not authenticated and authentication is required
            if (this.isAuthenticationRequired()) {
                try {
                    session.getRemote().sendString("Client not authenticated!", new WriteCallback() {
                        @Override
                        public void writeFailed(Throwable x) {
                            log.warn("Error while sending message: {}", new Object[]{x});
                        }

                        @Override
                        public void writeSuccess() {
                            log.debug("Send message success!");
                        }
                    });
                    session.close(1008, "Client not authenticated!");
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
            return null;
        }
        return context.getLoggedUsername();
    }

    @Override
    public HyperIoTWebSocketUserInfo getUserInfo() {
        return this.userInfo;
    }

    protected void preAuthenticate(Session s) {
        //Do nothing
    }

    protected void postAuthenticate(Session s) {
        //Do nothing
    }


    @Override
    public abstract void initialize();

    /**
     * @param message
     */
    @Override
    public abstract void onMessage(String message);
}
