package com.aspectran.demo.chat;

import com.aspectran.core.component.bean.annotation.Component;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.web.socket.AbstractEndpoint;

import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.ServerEndpointConfig;

/**
 * <p>Created: 29/09/2019</p>
 */
@Component
@ServerEndpoint(value = "/chat")
public class ChatEndpoint extends AbstractEndpoint {

    private static final Log log = LogFactory.getLog(ChatEndpoint.class);

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        ServerEndpointConfig endpointConfig;

        log.debug(
                session.getId()
                        + ": Connected"
        );
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        log.debug(
                session.getId()
                        + ": "
                        + message
        );
        for (Session s : session.getOpenSessions()) {
            s.getAsyncRemote().sendText(message);
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        log.debug(
                session.getId()
                        + ": Disconnected: "
                        + Integer.toString(reason.getCloseCode().getCode())
        );
    }

}
