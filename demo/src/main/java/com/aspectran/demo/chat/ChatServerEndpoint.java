package com.aspectran.demo.chat;

import com.aspectran.core.component.bean.annotation.Component;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.web.socket.jsr356.ActivityContextAwareEndpoint;

import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Set;

/**
 * <p>Created: 29/09/2019</p>
 */
@Component
@ServerEndpoint(value = "/chat")
public class ChatServerEndpoint extends ActivityContextAwareEndpoint {

    private static final Log log = LogFactory.getLog(ChatServerEndpoint.class);

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) throws IOException {
        log.debug(session.getId() + ": Connected");

        session.getBasicRemote().sendText(session.getId() + ": Connected");
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        log.debug(session.getId() + ": " + message);

        for (Session s : session.getOpenSessions()) {
            s.getAsyncRemote().sendText(message);
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        log.debug(session.getId() + ": Disconnected: " + reason.getCloseCode().getCode());
    }

}
