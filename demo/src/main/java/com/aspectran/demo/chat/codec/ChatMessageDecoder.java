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
package com.aspectran.demo.chat.codec;

import com.aspectran.core.util.apon.JsonToApon;
import com.aspectran.demo.chat.model.ChatMessage;
import jakarta.websocket.DecodeException;
import jakarta.websocket.Decoder;
import jakarta.websocket.EndpointConfig;

import java.io.IOException;

/**
 * Decoder for {@link ChatMessage}.
 *
 * <p>Created: 2019/10/09</p>
 */
public class ChatMessageDecoder implements Decoder.Text<ChatMessage> {

    @Override
    public ChatMessage decode(String s) throws DecodeException {
        try {
            return JsonToApon.from(s, ChatMessage.class);
        } catch (IOException e) {
            throw new DecodeException(s, "Badly formatted message", e);
        }
    }

    @Override
    public boolean willDecode(String s) {
        return true;
    }

    @Override
    public void init(EndpointConfig config) {
    }

    @Override
    public void destroy() {
    }

}
