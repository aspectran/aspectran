/*
 * Copyright (c) 2008-present The Aspectran Project
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
package com.aspectran.netty.server.websocket;

import com.aspectran.utils.Assert;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.Map;

/**
 * Represents a matched WebSocket endpoint along with extracted URI path parameters.
 *
 * <p>Created: 2026-09-03</p>
 */
public class WebSocketEndpointMatch {

    private final NettyWebSocketListener listener;

    private final Map<String, String> pathParameters;

    public WebSocketEndpointMatch(@NonNull NettyWebSocketListener listener) {
        this(listener, null);
    }

    public WebSocketEndpointMatch(@NonNull NettyWebSocketListener listener, @Nullable Map<String, String> pathParameters) {
        Assert.notNull(listener, "listener must not be null");
        this.listener = listener;
        this.pathParameters = (pathParameters != null && !pathParameters.isEmpty()
                ? Collections.unmodifiableMap(pathParameters)
                : Collections.emptyMap());
    }

    @NonNull
    public NettyWebSocketListener getListener() {
        return listener;
    }

    @NonNull
    public Map<String, String> getPathParameters() {
        return pathParameters;
    }

}
