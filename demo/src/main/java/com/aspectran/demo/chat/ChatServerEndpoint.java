/*
 * Copyright (c) 2008-2025 The Aspectran Project
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

import com.aspectran.core.component.bean.annotation.Component;
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
import com.aspectran.utils.Assert;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;
import com.aspectran.web.websocket.jsr356.AbstractEndpoint;
import com.aspectran.web.websocket.jsr356.AspectranConfigurator;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

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
public class ChatServerEndpoint extends AbstractEndpoint {

    private static final String USERNAME_KEY = "username";

    private static final Set<String> usernames = new CopyOnWriteArraySet<>();

    @Override
    protected void registerMessageHandlers(@NonNull Session session) {
        session.addMessageHandler(ChatMessage.class, message
                -> handleMessage(session, message));
    }

    private void handleMessage(Session session, @NonNull ChatMessage chatMessage) {
        SendTextMessagePayload payload = chatMessage.getSendTextMessagePayload();
        if (payload != null) {
            String username = getUsername(session);
            switch (payload.getType()) {
                case CHAT:
                    if (username != null) {
                        broadcast(session, username, payload.getContent());
                    }
                    break;
                case JOIN:
                    if (username == null) {
                        username = payload.getUsername();
                        join(session, username);
                    }
                    break;
                case LEAVE:
                    if (username != null) {
                        userLeft(session, username);
                    }
                    break;
            }
        }
    }

    @Override
    protected void removeSession(Session session) {
        String username = getUsername(session);
        if (username != null) {
            userLeft(session, username);
        }
    }

    @Nullable
    private String getUsername(@NonNull Session session) {
        Object username = session.getUserProperties().get(USERNAME_KEY);
        return (username != null ? username.toString() : null);
    }

    private void setUsername(@NonNull Session session, String username) {
        session.getUserProperties().put(USERNAME_KEY, username);
    }

    private void join(@NonNull Session session, @NonNull String username) {
        Assert.hasText(username, "username must not be empty");
        synchronized (usernames) {
            if (usernames.contains(username)) {
                userDuplicated(session, username);
                return;
            }
            setUsername(session, username);
            usernames.add(username);
        }
        welcome(session, username);
        userConnected(session, username);
        notifyJoinedUsers(session);
    }

    private void welcome(@NonNull Session session, String username) {
        WelcomeUserPayload payload = new WelcomeUserPayload();
        payload.setUsername(username);
        sendMessage(session, new ChatMessage(payload));
    }

    private void userDuplicated(@NonNull Session session, String username) {
        DuplicatedUserPayload payload = new DuplicatedUserPayload();
        payload.setUsername(username);
        sendMessage(session, new ChatMessage(payload));
    }

    private void userLeft(@NonNull Session session, String username) {
        if (usernames.remove(username)) {
            userDisconnected(session, username);
            notifyJoinedUsers(session);
        }
    }

    private void userConnected(Session session, String username) {
        BroadcastConnectedUserPayload payload = new BroadcastConnectedUserPayload();
        payload.setUsername(username);
        broadcast(session, new ChatMessage(payload), false);
    }

    private void userDisconnected(Session session, String username) {
        BroadcastDisconnectedUserPayload payload = new BroadcastDisconnectedUserPayload();
        payload.setUsername(username);
        broadcast(session, new ChatMessage(payload), true);
    }

    private void notifyJoinedUsers(Session session) {
        BroadcastAvailableUsersPayload payload = new BroadcastAvailableUsersPayload();
        payload.setUsernames(usernames);
        broadcast(session, new ChatMessage(payload), true);
    }

    private void broadcast(Session session, String username, String text) {
        BroadcastTextMessagePayload payload = new BroadcastTextMessagePayload();
        payload.setContent(text);
        payload.setUsername(username);
        broadcast(session, new ChatMessage(payload), false);
    }

    private void broadcast(@NonNull Session session, ChatMessage message, boolean includingMe) {
        for (Session other : session.getOpenSessions()) {
            if (includingMe || !other.getId().equals(session.getId())) {
                sendMessage(other, message);
            }
        }
    }

    private void sendMessage(@NonNull Session session, @NonNull ChatMessage message) {
        if (session.isOpen()) {
            session.getAsyncRemote().sendObject(message);
        }
    }

}
