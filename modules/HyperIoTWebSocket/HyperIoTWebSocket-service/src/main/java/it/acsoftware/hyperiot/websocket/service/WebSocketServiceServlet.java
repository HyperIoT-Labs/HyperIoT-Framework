package it.acsoftware.hyperiot.websocket.service;

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

/**
 * Author Generoso Martello.
 *
 */
public class WebSocketServiceServlet extends WebSocketServlet {
    @Override
    public void configure(WebSocketServletFactory factory) {
        factory.register(WebSocketService.class);
    }
}
