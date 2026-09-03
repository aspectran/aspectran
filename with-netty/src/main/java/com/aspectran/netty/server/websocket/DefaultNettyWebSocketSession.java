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
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.SocketAddress;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of {@link NettyWebSocketSession}.
 *
 * <p>Created: 2026-09-02</p>
 */
public class DefaultNettyWebSocketSession implements NettyWebSocketSession {

    private final String id;

    private final Channel channel;

    private final String uri;

    private final String path;

    private final HttpHeaders handshakeHeaders;

    private final WebSocketServerHandshaker handshaker;

    private final Map<String, String> pathParameters;

    private final Map<String, Object> attributes = new ConcurrentHashMap<>();

    private final NettyWebSocketConfig webSocketConfig;

    private volatile int maxTextMessageBufferSize;

    private volatile int maxBinaryMessageBufferSize;

    private volatile long maxIdleTimeout;

    public DefaultNettyWebSocketSession(
            @NonNull Channel channel,
            @NonNull String uri,
            @NonNull String path,
            @NonNull HttpHeaders handshakeHeaders,
            @Nullable WebSocketServerHandshaker handshaker) {
        this(channel, uri, path, handshakeHeaders, handshaker, null, null);
    }

    public DefaultNettyWebSocketSession(
            @NonNull Channel channel,
            @NonNull String uri,
            @NonNull String path,
            @NonNull HttpHeaders handshakeHeaders,
            @Nullable WebSocketServerHandshaker handshaker,
            @Nullable Map<String, String> pathParameters) {
        this(channel, uri, path, handshakeHeaders, handshaker, pathParameters, null);
    }

    public DefaultNettyWebSocketSession(
            @NonNull Channel channel,
            @NonNull String uri,
            @NonNull String path,
            @NonNull HttpHeaders handshakeHeaders,
            @Nullable WebSocketServerHandshaker handshaker,
            @Nullable Map<String, String> pathParameters,
            @Nullable NettyWebSocketConfig webSocketConfig) {
        Assert.notNull(channel, "channel must not be null");
        Assert.notNull(uri, "uri must not be null");
        Assert.notNull(path, "path must not be null");
        Assert.notNull(handshakeHeaders, "handshakeHeaders must not be null");
        this.id = UUID.randomUUID().toString();
        this.channel = channel;
        this.uri = uri;
        this.path = path;
        this.handshakeHeaders = handshakeHeaders;
        this.handshaker = handshaker;
        this.pathParameters = (pathParameters != null && !pathParameters.isEmpty()
                ? Collections.unmodifiableMap(pathParameters)
                : Collections.emptyMap());
        this.webSocketConfig = webSocketConfig;
        if (webSocketConfig != null) {
            this.maxTextMessageBufferSize = webSocketConfig.getMaxTextMessageBufferSize();
            this.maxBinaryMessageBufferSize = webSocketConfig.getMaxBinaryMessageBufferSize();
            this.maxIdleTimeout = webSocketConfig.getMaxIdleTimeout();
        } else {
            this.maxTextMessageBufferSize = 65536;
            this.maxBinaryMessageBufferSize = 65536;
            this.maxIdleTimeout = 0;
        }
    }

    @Override
    @NonNull
    public String getId() {
        return id;
    }

    @Override
    @NonNull
    public Channel getChannel() {
        return channel;
    }

    @Override
    @Nullable
    public SocketAddress getLocalAddress() {
        return channel.localAddress();
    }

    @Override
    @Nullable
    public SocketAddress getRemoteAddress() {
        return channel.remoteAddress();
    }

    @Override
    @NonNull
    public String getUri() {
        return uri;
    }

    @Override
    @NonNull
    public String getPath() {
        return path;
    }

    @Override
    @NonNull
    public Map<String, String> getPathParameters() {
        return pathParameters;
    }

    @Override
    @NonNull
    public HttpHeaders getHandshakeHeaders() {
        return handshakeHeaders;
    }

    @Override
    @NonNull
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String name) {
        return (T) attributes.get(name);
    }

    @Override
    public void setAttribute(String name, Object value) {
        if (value != null) {
            attributes.put(name, value);
        } else {
            attributes.remove(name);
        }
    }

    @Override
    @Nullable
    public Object removeAttribute(String name) {
        return attributes.remove(name);
    }

    @Override
    public boolean isOpen() {
        return channel.isActive();
    }

    @Override
    public ChannelFuture sendText(String text) {
        return channel.writeAndFlush(new TextWebSocketFrame(text != null ? text : ""));
    }

    @Override
    public ChannelFuture sendBinary(byte[] data) {
        ByteBuf buf = (data != null ? Unpooled.wrappedBuffer(data) : Unpooled.EMPTY_BUFFER);
        return channel.writeAndFlush(new BinaryWebSocketFrame(buf));
    }

    @Override
    public ChannelFuture sendBinary(ByteBuf data) {
        return channel.writeAndFlush(new BinaryWebSocketFrame(data != null ? data : Unpooled.EMPTY_BUFFER));
    }

    @Override
    public ChannelFuture sendPing() {
        return channel.writeAndFlush(new PingWebSocketFrame());
    }

    @Override
    public ChannelFuture sendPong() {
        return channel.writeAndFlush(new PongWebSocketFrame());
    }

    @Override
    public ChannelFuture close() {
        return close(1000, null);
    }

    @Override
    public ChannelFuture close(int statusCode, @Nullable String reasonText) {
        if (channel.isActive()) {
            CloseWebSocketFrame closeFrame = (statusCode > 0 ?
                    new CloseWebSocketFrame(statusCode, reasonText) : new CloseWebSocketFrame());
            if (handshaker != null) {
                return handshaker.close(channel, closeFrame).addListener(ChannelFutureListener.CLOSE);
            } else {
                return channel.writeAndFlush(closeFrame).addListener(ChannelFutureListener.CLOSE);
            }
        }
        return channel.newSucceededFuture();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DefaultNettyWebSocketSession that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "NettyWebSocketSession[" + id + ", uri=" + uri + "]";
    }

    @Override
    @Nullable
    public NettyWebSocketConfig getWebSocketConfig() {
        return webSocketConfig;
    }

    @Override
    public long getMaxIdleTimeout() {
        return maxIdleTimeout;
    }

    @Override
    public void setMaxIdleTimeout(long milliseconds) {
        this.maxIdleTimeout = milliseconds;
    }

    @Override
    public int getMaxTextMessageBufferSize() {
        return maxTextMessageBufferSize;
    }

    @Override
    public void setMaxTextMessageBufferSize(int length) {
        this.maxTextMessageBufferSize = length;
    }

    @Override
    public int getMaxBinaryMessageBufferSize() {
        return maxBinaryMessageBufferSize;
    }

    @Override
    public void setMaxBinaryMessageBufferSize(int length) {
        this.maxBinaryMessageBufferSize = length;
    }

}
