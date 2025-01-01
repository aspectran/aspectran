/*
 * Copyright (c) 2008-2024 The Aspectran Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.demo.monitoring.stats;

import com.aspectran.core.activity.InstantActivitySupport;
import com.aspectran.core.component.bean.annotation.AvoidAdvice;
import com.aspectran.core.component.bean.annotation.Component;
import com.aspectran.core.component.session.ManagedSession;
import com.aspectran.core.component.session.SessionHandler;
import com.aspectran.core.component.session.SessionStatistics;
import com.aspectran.undertow.server.TowServer;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;
import com.aspectran.web.websocket.jsr356.AspectranConfigurator;
import jakarta.websocket.CloseReason;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

@Component
@ServerEndpoint(
        value = "/monitoring/stats/undertow",
        configurator = AspectranConfigurator.class
)
@AvoidAdvice
public class UndertowSessionStatsEndpoint extends InstantActivitySupport {

    private static final Logger logger = LoggerFactory.getLogger(UndertowSessionStatsEndpoint.class);

    private static final String COMMAND_JOIN = "JOIN:";

    private static final String COMMAND_LEAVE = "LEAVE";

    private static final String HEARTBEAT_PING_MSG = "--ping--";

    private static final String HEARTBEAT_PONG_MSG = "--pong--";

    private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());

    private int refreshIntervalInSeconds = 5;

    private volatile Timer timer;

    private volatile boolean first;

    @OnOpen
    public void onOpen(Session session) {
        if (logger.isDebugEnabled()) {
            logger.debug("WebSocket connection established with session: " + session.getId());
        }

    }

    @OnMessage
    public void onMessage(Session session, String message) {
        if (HEARTBEAT_PING_MSG.equals(message)) {
            session.getAsyncRemote().sendText(HEARTBEAT_PONG_MSG);
            return;
        }
        if (message != null && message.startsWith(COMMAND_JOIN)) {
            try {
                refreshIntervalInSeconds = Integer.parseInt(message.substring(COMMAND_JOIN.length()));
            } catch (NumberFormatException e) {
                // ignore
            }
            addSession(session);
        } else if (COMMAND_LEAVE.equals(message)) {
            removeSession(session);
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        if (logger.isDebugEnabled()) {
            logger.debug("Websocket session " + session.getId() + " has been closed. Reason: " + reason);
        }
        removeSession(session);
    }

    @OnError
    public void onError(@NonNull Session session, Throwable error) {
        logger.error("Error in websocket session: " + session.getId(), error);
        try {
            removeSession(session);
            session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, null));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void broadcast(String message) {
        synchronized (sessions) {
            for (Session session : sessions) {
                if (session.isOpen()) {
                    session.getAsyncRemote().sendText(message);
                }
            }
        }
    }

    private void addSession(Session session) {
        if (sessions.add(session)) {
            first = true;
            if (timer == null) {
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    private SessionStatsPayload oldStats;

                    @Override
                    public void run() {
                        SessionStatsPayload newStats = getUndertowSessionStatsPayload();
                        if (first || !newStats.equals(oldStats)) {
                            broadcast(newStats.toJson());
                            oldStats = newStats;
                            if (first) {
                                first = false;
                            }
                        }
                    }
                }, 0, refreshIntervalInSeconds * 1000L);
            }
        }
    }

    private void removeSession(Session session) {
        if (sessions.remove(session) && sessions.isEmpty() && timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @NonNull
    private SessionStatsPayload getUndertowSessionStatsPayload() {
        TowServer towServer = getBeanRegistry().getBean("tow.server");
        SessionHandler sessionHandler = towServer.getSessionHandler("root.war");
        SessionStatistics statistics = sessionHandler.getStatistics();

        SessionStatsPayload stats = new SessionStatsPayload();
        stats.setCreatedSessionCount(statistics.getNumberOfCreated());
        stats.setExpiredSessionCount(statistics.getNumberOfExpired());
        stats.setActiveSessionCount(statistics.getNumberOfActives());
        stats.setHighestActiveSessionCount(statistics.getHighestNumberOfActives());
        stats.setEvictedSessionCount(statistics.getNumberOfUnmanaged());
        stats.setRejectedSessionCount(statistics.getNumberOfRejected());
        stats.setElapsedTime(formatDuration(statistics.getStartTime()));

        // Current Users
        List<String> currentSessions = new ArrayList<>();
        Set<String> sessionIds = sessionHandler.getActiveSessions();
        for (String sessionId : sessionIds) {
            ManagedSession session = sessionHandler.getSession(sessionId);
            if (session != null) {
                currentSessions.add("1:Session " + session.getId() + " created at " +
                        formatTime(session.getCreationTime()));
            }
        }
        stats.setCurrentSessions(currentSessions.toArray(new String[0]));
        return stats;
    }

    @NonNull
    private String formatTime(long time) {
        LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault());
        return date.toString();
    }

    @NonNull
    private static String formatDuration(long startTime) {
        Instant start = Instant.ofEpochMilli(startTime);
        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);
        long seconds = duration.getSeconds();
        return String.format(
            "%02d:%02d:%02d",
            seconds / 3600,
            (seconds % 3600) / 60,
            seconds % 60);
    }

}
