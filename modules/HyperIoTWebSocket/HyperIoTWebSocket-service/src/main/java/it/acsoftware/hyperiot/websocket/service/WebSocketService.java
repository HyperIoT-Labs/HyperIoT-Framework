package it.acsoftware.hyperiot.websocket.service;

import it.acsoftware.hyperiot.base.util.HyperIoTThreadFactoryBuilder;
import it.acsoftware.hyperiot.base.util.HyperIoTUtil;
import it.acsoftware.hyperiot.websocket.api.HyperIoTWebSocketEndPoint;
import it.acsoftware.hyperiot.websocket.api.HyperIoTWebSocketPolicy;
import it.acsoftware.hyperiot.websocket.api.HyperIoTWebSocketSession;
import it.acsoftware.hyperiot.websocket.model.message.HyperIoTWebSocketMessage;
import it.acsoftware.hyperiot.websocket.model.message.HyperIoTWebSocketMessageType;
import it.acsoftware.hyperiot.websocket.policy.*;
import it.acsoftware.hyperiot.websocket.session.HyperIoTWebSocketAbstractSession;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WriteCallback;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.osgi.framework.*;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Author Generoso Martello
 * OSGi component which open a web socket session and keep tracks of all user's sessions
 */
@Component(
        name = "websocket-service",
        immediate = true
)
@WebSocket()
public class WebSocketService {
    public final String WEB_SOCKET_SERVICE_URL = HyperIoTUtil.getHyperIoTBaseRestContext(HyperIoTUtil.getBundleContext(WebSocketService.class)) + "/ws";
    private Logger log = LoggerFactory.getLogger(WebSocketService.class.getName());

    /**
     * Managing all websocket sessions in terms of:
     * 1) Classical sessions
     * 2) Bridged Sessions
     * 3) Custom Websocket policies for each session
     */
    private static final Map<Session, HyperIoTWebSocketSession> sessions;
    private static final Map<Session, List<HyperIoTWebSocketPolicy>> webSocketSessionPolicies;

    /**
     * Managing Threads with custom executors in order to avoid greedy clients to monopolize connections
     */
    private static final ThreadFactory onOpenThreadsFactory;
    private static final ThreadFactory onCloseThreadsFactory;
    private static final ThreadFactory onOnMessageThreadsFactory;
    private static final ThreadFactory onErrorThreadsFactory;
    private static final Executor onOpenDispatchThreads;
    private static final Executor onCloseDispatchThreads;
    private static final Executor onMessageDispatchThreads;
    private static final Executor onErrorDispatchThreads;


    static {
        sessions = Collections.synchronizedMap(new HashMap<>());
        webSocketSessionPolicies = Collections.synchronizedMap(new HashMap<>());
        //sharing bridged session object inside the JVM
        onOpenThreadsFactory = HyperIoTThreadFactoryBuilder.build("hyperiot-ws-open-thread-%d", false);
        onCloseThreadsFactory = HyperIoTThreadFactoryBuilder.build("hyperiot-ws-close-thread-%d", false);
        onOnMessageThreadsFactory = HyperIoTThreadFactoryBuilder.build("hyperiot-ws-message-thread-%d", false);
        onErrorThreadsFactory = HyperIoTThreadFactoryBuilder.build("hyperiot-ws-error-thread-%d", false);
        //new connections must be throtled 100 is a good chioce
        onOpenDispatchThreads = Executors.newFixedThreadPool(HyperIoTUtil.getWebSocketOnOpenDispatchThreads(200), onOpenThreadsFactory);
        //same as Open
        onCloseDispatchThreads = Executors.newFixedThreadPool(HyperIoTUtil.getWebSocketOnCloseDispatchThreads(200), onCloseThreadsFactory);
        //high used threads, may be this number should be related to jetty max threads
        onMessageDispatchThreads = Executors.newFixedThreadPool(HyperIoTUtil.getWebSocketOnMessageDispatchThreads(500), onOnMessageThreadsFactory);
        //Error threads can be considered as message thread but a very rare
        onErrorDispatchThreads = Executors.newFixedThreadPool(HyperIoTUtil.getWebSocketOnErrorDispatchThreads(20), onErrorThreadsFactory);

    }

    private ServiceRegistration<Servlet> hyperIoTWebSocketServlet ;

    @Activate
    public void activate() throws Exception {
        try {
            Hashtable<String,Object> props = new Hashtable<>();
            props.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_PATTERN, WEB_SOCKET_SERVICE_URL.concat("/*"));
            props.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_NAME, "hyperiotWebSocketServlet");
            hyperIoTWebSocketServlet = (ServiceRegistration<Servlet>) FrameworkUtil.getBundle(this.getClass()).getBundleContext().registerService(Servlet.class.getName(), new WebSocketServiceServlet(), props);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    @Deactivate
    public void deactivate() throws Exception {
        if(sessions.values().isEmpty()){
            for(HyperIoTWebSocketSession hyperIoTWebSocketSession : sessions.values()){
                hyperIoTWebSocketSession.dispose();
            }
        }
        hyperIoTWebSocketServlet.unregister();
    }

    @OnWebSocketConnect
    public void onOpen(Session session) {
        try {
            log.debug("Opening web socket...");
            HashMap<String, HyperIoTWebSocketEndPoint> endPointHashMap = findEndWebSocketEndPoints();
            String requestPath = session.getUpgradeRequest().getRequestURI().getPath().replace(WEB_SOCKET_SERVICE_URL, "");
            if (requestPath.startsWith("/")) requestPath = requestPath.substring(1);
            if (endPointHashMap.containsKey(requestPath)) {
                HyperIoTWebSocketEndPoint hyperIotWebSocketEndPoint = endPointHashMap.get(requestPath);
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            HyperIoTWebSocketSession hyperIoTWebSocketSession = hyperIotWebSocketEndPoint.getHandler(session);
                            if (hyperIoTWebSocketSession.isAuthenticationRequired())
                                hyperIoTWebSocketSession.authenticate();
                            else
                                hyperIoTWebSocketSession.authenticateAnonymous();
                            //2 minutes idle timeout
                            session.setIdleTimeout(120000);
                            sessions.put(hyperIoTWebSocketSession.getSession(), hyperIoTWebSocketSession);
                            List<HyperIoTWebSocketPolicy> policies = hyperIoTWebSocketSession.getWebScoketPolicies();
                            if (policies != null && policies.size() > 0) {
                                webSocketSessionPolicies.put(session, policies);
                                applyCustomPolicies(session, policies);
                            }
                            hyperIoTWebSocketSession.initialize();
                        } catch (Throwable t) {
                            log.error(t.getMessage(), t);
                        }
                    }
                };
                Executor onOpenCustomExecutor = hyperIotWebSocketEndPoint.getExecutorForOpenConnections(session);
                Executor runner = (onOpenCustomExecutor != null) ? onOpenCustomExecutor : onOpenDispatchThreads;
                runner.execute(r);
            } else {
                HyperIoTWebSocketMessage m = HyperIoTWebSocketMessage.createMessage(null, "Unknown service requested.".getBytes(), HyperIoTWebSocketMessageType.ERROR);
                session.close(1010, m.toJson());
            }
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        }
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    final HyperIoTWebSocketSession hyperIoTWebSocketSession = sessions.get(session);
                    sessions.remove(session);
                    log.debug("On Close websocket : {}", reason);
                    if (session.isOpen()) {
                        try {
                            HyperIoTWebSocketMessage m = HyperIoTWebSocketMessage.createMessage(null, ("Closing websocket: " + reason).getBytes(), HyperIoTWebSocketMessageType.DISCONNECTING);
                            WriteCallback wc = new WriteCallback() {
                                @Override
                                public void writeFailed(Throwable x) {
                                    log.warn("Error while sending message: {}", x);
                                }

                                @Override
                                public void writeSuccess() {
                                    log.debug("Close message sent!");
                                }
                            };
                            //if websocket is a WebSocketAbstractSession it will use it's own method for sending messages
                            if (hyperIoTWebSocketSession instanceof HyperIoTWebSocketAbstractSession) {
                                ((HyperIoTWebSocketAbstractSession) hyperIoTWebSocketSession).sendRemote(m, wc);
                            } else {
                                session.getRemote().sendString(m.toJson(), wc);
                            }
                        } catch (Throwable e) {
                            log.warn("Cannot send closing reason on websocket: {}", e.getMessage());
                        }
                    }

                    if (hyperIoTWebSocketSession != null) {
                        try {
                            hyperIoTWebSocketSession.dispose();
                        } catch (Throwable e) {
                            log.warn("Error closing connection: {}", e.getMessage());
                        }
                    }
                } catch (Throwable t) {
                    log.error("Error while closing websocket: {} - {}", new Object[]{t.getMessage(), t.getCause()});
                }
            }
        };
        onCloseDispatchThreads.execute(r);
    }


    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    if (session == null || !session.isOpen())
                        return;
                    log.debug("On Message websocket getting session...");
                    HyperIoTWebSocketSession hyperIoTWebSocketSession = sessions.get(session);
                    //Policy Check
                    if (webSocketSessionPolicies != null && !webSocketSessionPolicies.isEmpty() && webSocketSessionPolicies.containsKey(hyperIoTWebSocketSession.getSession())) {
                        List<HyperIoTWebSocketPolicy> policies = webSocketSessionPolicies.get(hyperIoTWebSocketSession.getSession());
                        for (HyperIoTWebSocketPolicy policy : policies) {
                            if (!policy.isSatisfied(hyperIoTWebSocketSession.getPolicyParams(), message.getBytes())) {
                                if (policy.printWarningOnFail()) {
                                    log.error("Policy {} not satisfied! ", policy.getClass().getSimpleName());
                                }

                                if (policy.sendWarningBackToClientOnFail()) {
                                    String policyWarning = "Policy " + policy.getClass().getSimpleName() + " Not satisfied!";
                                    HyperIoTWebSocketMessage m = HyperIoTWebSocketMessage.createMessage(null, policyWarning.getBytes(), HyperIoTWebSocketMessageType.WEBSOCKET_POLICY_WARNING);
                                    hyperIoTWebSocketSession.getSession().getRemote().sendString(m.toJson());
                                }

                                if (policy.closeWebSocketOnFail()) {
                                    hyperIoTWebSocketSession.dispose();
                                    return;
                                }

                                if (policy.ignoreMessageOnFail()) {
                                    return;
                                }
                            }
                        }
                    }

                    //Forwarding message after policy check
                    long sessionFoundTime = System.nanoTime();
                    if (hyperIoTWebSocketSession != null) {
                        hyperIoTWebSocketSession.onMessage(message);
                        log.debug("Message forwarded to session in {} seconds", ((double) System.nanoTime() - sessionFoundTime) / 1_000_000_000);
                    }
                } catch (Throwable e) {
                    log.error("Error while forwarding message to websocket session: {}", e);
                }
            }
        };
        onMessageDispatchThreads.execute(r);
    }

    @OnWebSocketError
    public void onError(Session session, Throwable cause) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                if (session == null)
                    return;
                log.warn("On Web Socket Error: {} , {}", new Object[]{cause.getMessage(), cause});
                try {
                    log.debug("Tring close websocket on error: {} , {}", new Object[]{cause.getMessage(), cause});
                    WebSocketService.this.onClose(session, 500, cause.getCause().getMessage());
                } catch (Throwable e) {
                    log.error(e.getMessage(), e);
                }
            }
        };
        onErrorDispatchThreads.execute(r);
    }

    private HashMap<String, HyperIoTWebSocketEndPoint> findEndWebSocketEndPoints() {
        // TODO: in a future version consider using OSGi ServiceTracker to keep track of available WebSocketEndPoints
        //       https://mnlipp.github.io/osgi-getting-started/TrackingAService.html
        HashMap<String, HyperIoTWebSocketEndPoint> endPointHashMap = new HashMap<>();
        try {
            BundleContext bundleContext = HyperIoTUtil.getBundleContext(this);
            ServiceReference[] serviceReferences = bundleContext
                    .getAllServiceReferences(HyperIoTWebSocketEndPoint.class.getName(), null);
            if (serviceReferences != null) {
                for (ServiceReference serviceReference : serviceReferences) {
                    HyperIoTWebSocketEndPoint ep = (HyperIoTWebSocketEndPoint) bundleContext
                            .getService(serviceReference);
                    String endPointPath = (ep.getPath().startsWith("/"))?ep.getPath().substring(1):ep.getPath();
                    endPointHashMap.put(endPointPath, ep);
                }
            }
        } catch (InvalidSyntaxException e) {
            log.error(e.getMessage(), e);
        }
        return endPointHashMap;
    }

    /**
     * Apply custom policies to session
     *
     * @param s
     * @param policies
     */
    private void applyCustomPolicies(Session s, List<HyperIoTWebSocketPolicy> policies) {
        for (HyperIoTWebSocketPolicy policy : policies) {
            if (policy instanceof InputBufferSizePolicy) {
                InputBufferSizePolicy ibsPolicy = (InputBufferSizePolicy) policy;
                s.getPolicy().setInputBufferSize(ibsPolicy.getInputBufferSize());
            } else if (policy instanceof MaxBinaryMessageBufferSizePolicy) {
                MaxBinaryMessageBufferSizePolicy mbmbsPolicy = (MaxBinaryMessageBufferSizePolicy) policy;
                s.getPolicy().setMaxBinaryMessageBufferSize(mbmbsPolicy.getMaxBinaryMessageBufferSize());
            } else if (policy instanceof MaxBinaryMessageSizePolicy) {
                MaxBinaryMessageSizePolicy mbmsPolicy = (MaxBinaryMessageSizePolicy) policy;
                s.getPolicy().setMaxBinaryMessageSize(mbmsPolicy.getMaxBinaryMessageSize());
            } else if (policy instanceof MaxTextMessageBufferSizePolicy) {
                MaxTextMessageBufferSizePolicy mtmbsPolicy = (MaxTextMessageBufferSizePolicy) policy;
                s.getPolicy().setMaxTextMessageBufferSize(mtmbsPolicy.getMaxTextMessageBufferSize());
            } else if (policy instanceof MaxTextMessageSizePolicy) {
                MaxTextMessageSizePolicy mtmsPolicy = (MaxTextMessageSizePolicy) policy;
                s.getPolicy().setMaxTextMessageSize(mtmsPolicy.getMaxTextMessageSizePolicy());
            }
        }
    }

    public static Map<Session, HyperIoTWebSocketSession> getSessions() {
        return sessions;
    }
}
