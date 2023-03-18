/*
 * Copyright (c) 2008-2023 The Aspectran Project
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
package com.aspectran.demo.chat;

import com.aspectran.core.activity.InstantActivitySupport;
import com.aspectran.core.component.bean.annotation.Component;
import com.aspectran.core.util.logging.Logger;
import com.aspectran.core.util.logging.LoggerFactory;
import com.aspectran.demo.chat.codec.ChatMessageDecoder;
import com.aspectran.demo.chat.codec.ChatMessageEncoder;
import com.aspectran.demo.chat.model.ChatMessage;
import com.aspectran.demo.chat.model.payload.BroadcastAvailableUsersPayload;
import com.aspectran.demo.chat.model.payload.BroadcastConnectedUserPayload;
import com.aspectran.demo.chat.model.payload.BroadcastDisconnectedUserPayload;
import com.aspectran.demo.chat.model.payload.BroadcastTextMessagePayload;
import com.aspectran.demo.chat.model.payload.DuplicatedUserPayload;
import com.aspectran.demo.chat.model.payload.SendTextMessagePayload;
import com.aspectran.demo.chat.model.payload.WelcomeUserPayload;
import com.aspectran.websocket.jsr356.AspectranConfigurator;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket endpoint for the chat server.
 *
 * <p>Created: 29/09/2019</p>
 */
@Component
@ServerEndpoint(
        value = "/chat",
        encoders = ChatMessageEncoder.class,
        decoders = ChatMessageDecoder.class,
        configurator = AspectranConfigurator.class
)
public class ChatServerEndpoint extends InstantActivitySupport {

    private static final Logger logger = LoggerFactory.getLogger(ChatServerEndpoint.class);

    private static final Map<String, Session> sessions = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session) {
        if (logger.isDebugEnabled()) {
            logger.debug("WebSocket connection established with session: " + session.getId());
        }
    }

    @OnMessage
    public void onMessage(Session session, ChatMessage chatMessage) {
        SendTextMessagePayload payload = chatMessage.getSendTextMessagePayload();
        if (payload != null) {
            String username = getUsername(session);
            switch (payload.getType()) {
                case CHAT:
                    if (username != null) {
                        broadcastTextMessage(session, username, payload.getContent());
                    }
                    break;
                case JOIN:
                    if (username == null) {
                        username = payload.getUsername();
                        if (sessions.containsKey(username)) {
                            duplicatedUser(session, username);
                            return;
                        }
                        setUsername(session, username);
                        sessions.put(username, session);
                        welcomeUser(session, username);
                        broadcastUserConnected(session, username);
                        broadcastAvailableUsers();
                    }
                    break;
                case LEAVE:
                    if (username != null) {
                        leaveUser(username);
                    }
                    break;
            }
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        if (logger.isDebugEnabled()) {
            logger.debug("Websocket session " + session.getId() + " has been closed. Reason: " + reason);
        }
        String username = getUsername(session);
        if (username != null) {
            leaveUser(username);
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        logger.error("Error in websocket session: " + session.getId(), error);
        try {
            session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, null));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String getUsername(Session session) {
        if (session.getUserProperties().get("username") != null) {
            return session.getUserProperties().get("username").toString();
        } else {
            return null;
        }
    }

    private void setUsername(Session session, String username) {
        session.getUserProperties().put("username", username);
    }

    private void welcomeUser(Session session, String username) {
        WelcomeUserPayload payload = new WelcomeUserPayload();
        payload.setUsername(username);
        session.getAsyncRemote().sendObject(new ChatMessage(payload));
    }

    private void duplicatedUser(Session session, String username) {
        DuplicatedUserPayload payload = new DuplicatedUserPayload();
        payload.setUsername(username);
        session.getAsyncRemote().sendObject(new ChatMessage(payload));
    }

    private void leaveUser(String username) {
        sessions.remove(username);
        broadcastUserDisconnected(username);
        broadcastAvailableUsers();
    }

    private void broadcastUserConnected(Session session, String username) {
        BroadcastConnectedUserPayload payload = new BroadcastConnectedUserPayload();
        payload.setUsername(username);
        broadcast(new ChatMessage(payload), session);
    }

    private void broadcastUserDisconnected(String username) {
        BroadcastDisconnectedUserPayload payload = new BroadcastDisconnectedUserPayload();
        payload.setUsername(username);
        broadcast(new ChatMessage(payload));
    }

    private void broadcastTextMessage(Session session, String username, String text) {
        BroadcastTextMessagePayload payload = new BroadcastTextMessagePayload();
        payload.setContent(text);
        payload.setUsername(username);
        broadcast(new ChatMessage(payload), session);
    }

    private void broadcastAvailableUsers() {
        BroadcastAvailableUsersPayload payload = new BroadcastAvailableUsersPayload();
        payload.setUsernames(sessions.keySet());
        broadcast(new ChatMessage(payload));
    }

    private void broadcast(ChatMessage message) {
        synchronized (sessions) {
            for (Session session : sessions.values()) {
                if (session.isOpen()) {
                    session.getAsyncRemote().sendObject(message);
                }
            }
        }
    }

    private void broadcast(ChatMessage message, Session ignoredSession) {
        synchronized (sessions) {
            for (Session session : sessions.values()) {
                if (session.isOpen() && !session.getId().equals(ignoredSession.getId())) {
                    session.getAsyncRemote().sendObject(message);
                }
            }
        }
    }

}
