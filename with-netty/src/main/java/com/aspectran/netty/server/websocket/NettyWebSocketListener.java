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

/**
 * Event-driven listener interface for handling WebSocket connection lifecycle
 * and incoming text or binary messages in a Netty environment.
 *
 * <p>Created: 2026-09-02</p>
 */
public interface NettyWebSocketListener {

    /**
     * Invoked when a new WebSocket connection is established and the handshake is complete.
     * @param session the connected WebSocket session
     * @throws Exception if an error occurs during connection opening
     */
    default void onOpen(NettyWebSocketSession session) throws Exception {
    }

    /**
     * Invoked when a text message frame is received from the client.
     * @param session the WebSocket session
     * @param text the received text payload
     * @throws Exception if an error occurs during message processing
     */
    default void onMessage(NettyWebSocketSession session, String text) throws Exception {
    }

    /**
     * Invoked when a binary message frame is received from the client.
     * @param session the WebSocket session
     * @param data the received binary payload
     * @throws Exception if an error occurs during message processing
     */
    default void onMessage(NettyWebSocketSession session, byte[] data) throws Exception {
    }

    /**
     * Invoked when the WebSocket connection is closed.
     * @param session the closing WebSocket session
     * @param statusCode the close status code (e.g. 1000 for normal closure)
     * @param reason the close reason phrase
     * @throws Exception if an error occurs during close cleanup
     */
    default void onClose(NettyWebSocketSession session, int statusCode, String reason) throws Exception {
    }

    /**
     * Invoked when an unexpected error or transport exception occurs.
     * @param session the affected WebSocket session
     * @param cause the throwable cause
     */
    default void onError(NettyWebSocketSession session, Throwable cause) {
    }

}
