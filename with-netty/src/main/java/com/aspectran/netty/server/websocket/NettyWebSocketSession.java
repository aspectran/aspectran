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

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.HttpHeaders;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.SocketAddress;
import java.util.Map;

/**
 * Represents an active WebSocket session connection hosted by a Netty server.
 *
 * <p>Created: 2026-09-02</p>
 */
public interface NettyWebSocketSession {

    /**
     * Returns the unique identifier for this WebSocket session.
     * @return the session ID
     */
    @NonNull
    String getId();

    /**
     * Returns the underlying Netty channel associated with this session.
     * @return the channel
     */
    @NonNull
    Channel getChannel();

    /**
     * Returns the local socket address for this connection.
     * @return the local address
     */
    @Nullable
    SocketAddress getLocalAddress();

    /**
     * Returns the remote socket address of the connected client.
     * @return the remote address
     */
    @Nullable
    SocketAddress getRemoteAddress();

    /**
     * Returns the full request URI used during the WebSocket handshake.
     * @return the handshake URI
     */
    @NonNull
    String getUri();

    /**
     * Returns the relative path mapped to the WebSocket endpoint.
     * @return the endpoint path
     */
    @NonNull
    String getPath();

    /**
     * Returns the HTTP headers received during the initial handshake upgrade request.
     * @return the handshake headers
     */
    @NonNull
    HttpHeaders getHandshakeHeaders();

    /**
     * Returns the mutable map of user-defined attributes associated with this session.
     * @return the session attributes
     */
    @NonNull
    Map<String, Object> getAttributes();

    /**
     * Returns the attribute value associated with the given name.
     * @param <T> the expected value type
     * @param name the attribute name
     * @return the attribute value, or {@code null} if not found
     */
    @Nullable
    <T> T getAttribute(String name);

    /**
     * Sets a user-defined attribute on this session.
     * @param name the attribute name
     * @param value the attribute value
     */
    void setAttribute(String name, Object value);

    /**
     * Removes a user-defined attribute from this session.
     * @param name the attribute name
     * @return the removed value, or {@code null} if not present
     */
    @Nullable
    Object removeAttribute(String name);

    /**
     * Returns whether the underlying connection channel is active and open.
     * @return {@code true} if open, {@code false} otherwise
     */
    boolean isOpen();

    /**
     * Sends a text message frame across this WebSocket connection.
     * @param text the text content to send
     * @return a {@link ChannelFuture} representing the I/O completion
     */
    ChannelFuture sendText(String text);

    /**
     * Sends a binary message frame across this WebSocket connection.
     * @param data the byte array payload to send
     * @return a {@link ChannelFuture} representing the I/O completion
     */
    ChannelFuture sendBinary(byte[] data);

    /**
     * Sends a binary message frame from a Netty {@link ByteBuf}.
     * @param data the byte buffer to send
     * @return a {@link ChannelFuture} representing the I/O completion
     */
    ChannelFuture sendBinary(ByteBuf data);

    /**
     * Sends a ping control frame to the client.
     * @return a {@link ChannelFuture} representing the I/O completion
     */
    ChannelFuture sendPing();

    /**
     * Sends a pong control frame to the client.
     * @return a {@link ChannelFuture} representing the I/O completion
     */
    ChannelFuture sendPong();

    /**
     * Gracefully closes the WebSocket connection with status code 1000 (Normal Closure).
     * @return a {@link ChannelFuture} representing the close operation
     */
    ChannelFuture close();

    /**
     * Gracefully closes the WebSocket connection with a specific status code and reason text.
     * @param statusCode the WebSocket close status code (e.g. 1000, 1001)
     * @param reasonText optional human-readable reason
     * @return a {@link ChannelFuture} representing the close operation
     */
    ChannelFuture close(int statusCode, @Nullable String reasonText);

}
