package com.aspectran.demo.apm.log;

import com.aspectran.core.component.bean.NoSuchBeanException;
import com.aspectran.core.component.bean.annotation.AvoidAdvice;
import com.aspectran.core.component.bean.annotation.Component;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.web.socket.jsr356.ActivityContextAwareEndpoint;
import com.aspectran.web.socket.jsr356.AspectranConfigurator;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint(
        value = "/apm/logtail",
        configurator = AspectranConfigurator.class
)
@AvoidAdvice
public class LogtailEndpoint extends ActivityContextAwareEndpoint {

    private static final Log log = LogFactory.getLog(LogtailEndpoint.class);

    private static final String TAILERS_PROPERTY = "tailers";

    private static final String COMMAND_JOIN = "JOIN:";

    private static final String COMMAND_LEAVE = "LEAVE";

    private static final String HEARTBEAT_PING_MSG = "--heartbeat-ping--";

    private static final String HEARTBEAT_PONG_MSG = "--heartbeat-pong--";

    private static final Set<Session> sessions =
            Collections.synchronizedSet(new HashSet<>());

    private static final Map<String, LogTailer> tailers = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session) {
        if (log.isDebugEnabled()) {
            log.debug("WebSocket connection established with session: " + session.getId());
        }
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        if (HEARTBEAT_PING_MSG.equals(message)) {
            session.getAsyncRemote().sendText(HEARTBEAT_PONG_MSG);
            return;
        }
        if (message != null && message.startsWith(COMMAND_JOIN)) {
            addSession(session, message);
        } else if (COMMAND_LEAVE.equals(message)) {
            removeSession(session);
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        removeSession(session);
    }

    protected void broadcast(String message) {
        synchronized (sessions) {
            for (Session session : sessions) {
                if (session.isOpen()) {
                    session.getAsyncRemote().sendText(message);
                }
            }
        }
    }

    private void addSession(Session session, String message) {
        if (sessions.add(session)) {
            String[] names = StringUtils.splitCommaDelimitedString(message.substring(COMMAND_JOIN.length()));
            session.getUserProperties().put(TAILERS_PROPERTY, names);
            buildLogTailers(names);
        }
    }

    private void removeSession(Session session) {
        if (sessions.remove(session)) {
            String[] names = (String[])session.getUserProperties().get(TAILERS_PROPERTY);
            if (names != null) {
                clearLogTailers(names);
            }
        }
    }

    private void buildLogTailers(String[] names) {
        synchronized (tailers) {
            for (String name : names) {
                if (!tailers.containsKey(name)) {
                    LogTailer tailer = getLogTailer(name);
                    if (tailer != null) {
                        try {
                            tailer.setTailerListener(name, this);
                            tailer.start();
                        } catch (Exception e) {
                            // ignore
                        }
                        tailers.put(name, tailer);
                    }
                }
            }
        }
    }

    private void clearLogTailers(String[] names) {
        synchronized (tailers) {
            for (String name : names) {
                if (tailers.containsKey(name) && !isBeingUsedTailer(name)) {
                    LogTailer tailer = tailers.remove(name);
                    if (tailer != null) {
                        try {
                            tailer.stop();
                        } catch (Exception e) {
                            // ignore
                        }
                    }
                    tailers.remove(name);
                }
            }
        }
    }

    private boolean isBeingUsedTailer(String name) {
        for (Session session : sessions) {
            String[] names = (String[])session.getUserProperties().get(TAILERS_PROPERTY);
            if (names != null) {
                for (String name2 : names) {
                    if (name.equals(name2)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private LogTailer getLogTailer(String name) {
        try {
            return getBeanRegistry().getBean(LogTailer.class, name);
        } catch (NoSuchBeanException e) {
            return null;
        }
    }

}
