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
package com.aspectran.demo.chat.model;

import com.aspectran.demo.chat.model.payload.BroadcastAvailableUsersPayload;
import com.aspectran.demo.chat.model.payload.BroadcastConnectedUserPayload;
import com.aspectran.demo.chat.model.payload.BroadcastDisconnectedUserPayload;
import com.aspectran.demo.chat.model.payload.BroadcastTextMessagePayload;
import com.aspectran.demo.chat.model.payload.DuplicatedUserPayload;
import com.aspectran.demo.chat.model.payload.SendTextMessagePayload;
import com.aspectran.demo.chat.model.payload.WelcomeUserPayload;
import com.aspectran.utils.apon.AbstractParameters;
import com.aspectran.utils.apon.ParameterKey;

/**
 * The Chat Message.
 *
 * <p>Created: 2019/10/09</p>
 */
public class ChatMessage extends AbstractParameters {

    private static final ParameterKey welcomeUser;
    private static final ParameterKey duplicatedUser;
    private static final ParameterKey broadcastAvailableUsers;
    private static final ParameterKey broadcastConnectedUser;
    private static final ParameterKey broadcastDisconnectedUser;
    private static final ParameterKey broadcastTextMessage;
    private static final ParameterKey sendTextMessage;

    private static final ParameterKey[] parameterKeys;

    static {
        welcomeUser = new ParameterKey("welcomeUser", WelcomeUserPayload.class);
        duplicatedUser = new ParameterKey("duplicatedUser", DuplicatedUserPayload.class);
        broadcastAvailableUsers = new ParameterKey("broadcastAvailableUsers", BroadcastAvailableUsersPayload.class);
        broadcastConnectedUser = new ParameterKey("broadcastConnectedUser", BroadcastConnectedUserPayload.class);
        broadcastDisconnectedUser = new ParameterKey("broadcastDisconnectedUser", BroadcastDisconnectedUserPayload.class);
        broadcastTextMessage = new ParameterKey("broadcastTextMessage", BroadcastTextMessagePayload.class);
        sendTextMessage = new ParameterKey("sendTextMessage", SendTextMessagePayload.class);

        parameterKeys = new ParameterKey[] {
                welcomeUser,
                duplicatedUser,
                broadcastAvailableUsers,
                broadcastConnectedUser,
                broadcastDisconnectedUser,
                broadcastTextMessage,
                sendTextMessage
        };
    }

    public ChatMessage() {
        super(parameterKeys);
    }

    public ChatMessage(WelcomeUserPayload welcomeUserPayload) {
        this();
        putValue(welcomeUser, welcomeUserPayload);
    }

    public ChatMessage(DuplicatedUserPayload duplicatedUserPayload) {
        this();
        putValue(duplicatedUser, duplicatedUserPayload);
    }

    public ChatMessage(BroadcastAvailableUsersPayload broadcastAvailableUsersPayload) {
        this();
        putValue(broadcastAvailableUsers, broadcastAvailableUsersPayload);
    }

    public ChatMessage(BroadcastConnectedUserPayload broadcastConnectedUserPayload) {
        this();
        putValue(broadcastConnectedUser, broadcastConnectedUserPayload);
    }

    public ChatMessage(BroadcastDisconnectedUserPayload broadcastDisconnectedUserPayload) {
        this();
        putValue(broadcastDisconnectedUser, broadcastDisconnectedUserPayload);
    }

    public ChatMessage(BroadcastTextMessagePayload broadcastTextMessagePayload) {
        this();
        putValue(broadcastTextMessage, broadcastTextMessagePayload);
    }

    public SendTextMessagePayload getSendTextMessagePayload() {
        return getParameters(sendTextMessage);
    }

}
