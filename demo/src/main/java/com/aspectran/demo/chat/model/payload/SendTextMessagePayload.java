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
package com.aspectran.demo.chat.model.payload;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterKey;
import com.aspectran.core.util.apon.ValueType;

/**
 * Payload with details of a message sent by the client.
 *
 * <p>Created: 2019/10/09</p>
 */
public class SendTextMessagePayload extends AbstractParameters {

    private static final ParameterKey type;
    private static final ParameterKey username;
    private static final ParameterKey content;

    private static final ParameterKey[] parameterKeys;

    static {
        type = new ParameterKey("type", ValueType.STRING);
        username = new ParameterKey("username", ValueType.STRING);
        content = new ParameterKey("content", ValueType.TEXT);

        parameterKeys = new ParameterKey[] {
                type,
                username,
                content
        };
    }

    public enum MessageType {
        CHAT,
        JOIN,
        LEAVE
    }

    public SendTextMessagePayload() {
        super(parameterKeys);
    }

    public MessageType getType() {
        return MessageType.valueOf(getString(type));
    }

    public String getUsername() {
        return getString(username);
    }

    public String getContent() {
        return getString(content);
    }

}
