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
package com.aspectran.netty.server.websocket.jsr356;

import com.aspectran.netty.server.websocket.NettyWebSocketSession;
import com.aspectran.utils.Assert;
import jakarta.websocket.CloseReason;
import jakarta.websocket.Decoder;
import jakarta.websocket.EncodeException;
import jakarta.websocket.Encoder;
import jakarta.websocket.Extension;
import jakarta.websocket.MessageHandler;
import jakarta.websocket.RemoteEndpoint;
import jakarta.websocket.SendHandler;
import jakarta.websocket.SendResult;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URI;
import java.nio.ByteBuffer;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;

/**
 * Netty adapter implementing the standard JSR-356 {@link jakarta.websocket.Session}.
 *
 * <p>Created: 2026-09-02</p>
 */
public class JsrWebSocketSession implements Session {

    private static final Logger logger = LoggerFactory.getLogger(JsrWebSocketSession.class);

    private final NettyWebSocketSession nettySession;

    private final Set<Session> openSessions;

    private final List<Encoder> encoders;

    private final List<Decoder> decoders;

    private final List<MessageHandler> messageHandlers = new CopyOnWriteArrayList<>();

    private final RemoteEndpoint.Basic basicRemote;

    private final RemoteEndpoint.Async asyncRemote;

    private final URI requestURI;

    private final Map<String, List<String>> requestParameterMap;

    public JsrWebSocketSession(
            @NonNull NettyWebSocketSession nettySession,
            @NonNull Set<Session> openSessions,
            @Nullable List<Encoder> encoders,
            @Nullable List<Decoder> decoders) {
        Assert.notNull(nettySession, "nettySession must not be null");
        Assert.notNull(openSessions, "openSessions must not be null");
        this.nettySession = nettySession;
        this.openSessions = openSessions;
        this.encoders = (encoders != null ? encoders : Collections.emptyList());
        this.decoders = (decoders != null ? decoders : Collections.emptyList());
        this.basicRemote = new NettyBasicRemote();
        this.asyncRemote = new NettyAsyncRemote();
        this.requestURI = URI.create(nettySession.getUri());
        this.requestParameterMap = parseRequestParameters(this.requestURI);
    }

    public NettyWebSocketSession getNettySession() {
        return nettySession;
    }

    public boolean hasMessageHandlers() {
        return !messageHandlers.isEmpty();
    }

    public void handleTextMessage(String text) throws Exception {
        for (MessageHandler handler : messageHandlers) {
            if (handler instanceof MessageHandler.Whole whole) {
                boolean decoded = false;
                for (Decoder decoder : decoders) {
                    if (decoder instanceof Decoder.Text textDecoder) {
                        try {
                            if (textDecoder.willDecode(text)) {
                                Object object = textDecoder.decode(text);
                                try {
                                    whole.onMessage(object);
                                    decoded = true;
                                    break;
                                } catch (ClassCastException ignore) {
                                }
                            }
                        } catch (Exception e) {
                            logger.error("Error decoding text message with decoder {}", decoder.getClass().getName(), e);
                        }
                    }
                }
                if (!decoded) {
                    try {
                        whole.onMessage(text);
                    } catch (ClassCastException ignore) {
                    }
                }
            }
        }
    }

    public void handleBinaryMessage(byte[] data) throws Exception {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        for (MessageHandler handler : messageHandlers) {
            if (handler instanceof MessageHandler.Whole whole) {
                try {
                    whole.onMessage(buffer.duplicate());
                } catch (ClassCastException e) {
                    try {
                        whole.onMessage(data);
                    } catch (ClassCastException ignore) {
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected String encodeTextObject(Object object) throws EncodeException {
        if (object == null) {
            return null;
        }
        if (object instanceof String str) {
            return str;
        }
        for (Encoder encoder : encoders) {
            if (encoder instanceof Encoder.Text textEncoder) {
                try {
                    return textEncoder.encode(object);
                } catch (ClassCastException ignore) {
                }
            }
        }
        throw new EncodeException(object, "No Encoder.Text available for: " + object.getClass().getName());
    }

    @Override
    public WebSocketContainer getContainer() {
        return null;
    }

    @Override
    public void addMessageHandler(MessageHandler handler) throws IllegalStateException {
        if (handler != null && !messageHandlers.contains(handler)) {
            messageHandlers.add(handler);
        }
    }

    @Override
    public <T> void addMessageHandler(Class<T> clazz, MessageHandler.Whole<T> handler) {
        addMessageHandler(handler);
    }

    @Override
    public <T> void addMessageHandler(Class<T> clazz, MessageHandler.Partial<T> handler) {
        addMessageHandler(handler);
    }

    @Override
    public Set<MessageHandler> getMessageHandlers() {
        return Set.copyOf(messageHandlers);
    }

    @Override
    public void removeMessageHandler(MessageHandler handler) {
        messageHandlers.remove(handler);
    }

    @Override
    public String getProtocolVersion() {
        return "13";
    }

    @Override
    public String getNegotiatedSubprotocol() {
        return "";
    }

    @Override
    public List<Extension> getNegotiatedExtensions() {
        return Collections.emptyList();
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public boolean isOpen() {
        return nettySession.isOpen();
    }

    @Override
    public long getMaxIdleTimeout() {
        return nettySession.getMaxIdleTimeout();
    }

    @Override
    public void setMaxIdleTimeout(long milliseconds) {
        nettySession.setMaxIdleTimeout(milliseconds);
    }

    @Override
    public void setMaxBinaryMessageBufferSize(int length) {
        nettySession.setMaxBinaryMessageBufferSize(length);
    }

    @Override
    public int getMaxBinaryMessageBufferSize() {
        return nettySession.getMaxBinaryMessageBufferSize();
    }

    @Override
    public void setMaxTextMessageBufferSize(int length) {
        nettySession.setMaxTextMessageBufferSize(length);
    }

    @Override
    public int getMaxTextMessageBufferSize() {
        return nettySession.getMaxTextMessageBufferSize();
    }

    @Override
    public RemoteEndpoint.Async getAsyncRemote() {
        return asyncRemote;
    }

    @Override
    public RemoteEndpoint.Basic getBasicRemote() {
        return basicRemote;
    }

    @Override
    public String getId() {
        return nettySession.getId();
    }

    @Override
    public void close() throws IOException {
        close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, ""));
    }

    @Override
    public void close(CloseReason closeReason) throws IOException {
        int code = (closeReason != null ? closeReason.getCloseCode().getCode() : 1000);
        String reason = (closeReason != null ? closeReason.getReasonPhrase() : null);
        nettySession.close(code, reason);
    }

    @Override
    public URI getRequestURI() {
        return requestURI;
    }

    @Override
    public Map<String, List<String>> getRequestParameterMap() {
        return requestParameterMap;
    }

    @Override
    public String getQueryString() {
        return requestURI.getQuery();
    }

    @Override
    public Map<String, String> getPathParameters() {
        return nettySession.getPathParameters();
    }

    @Override
    public Map<String, Object> getUserProperties() {
        return nettySession.getAttributes();
    }

    @Override
    public Principal getUserPrincipal() {
        return null;
    }

    @Override
    public Set<Session> getOpenSessions() {
        return Collections.unmodifiableSet(openSessions);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Session that)) return false;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        return "JsrWebSocketSession[" + getId() + ", uri=" + requestURI + "]";
    }

    private static Map<String, List<String>> parseRequestParameters(@NonNull URI uri) {
        String query = uri.getQuery();
        if (query == null || query.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, List<String>> params = new HashMap<>();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf('=');
            String key = (idx > 0 ? pair.substring(0, idx) : pair);
            String val = (idx > 0 && pair.length() > idx + 1 ? pair.substring(idx + 1) : "");
            params.computeIfAbsent(key, k -> new ArrayList<>()).add(val);
        }
        return Collections.unmodifiableMap(params);
    }

    // RemoteEndpoint.Basic implementation
    private class NettyBasicRemote implements RemoteEndpoint.Basic {
        @Override
        public void sendText(String text) throws IOException {
            try {
                nettySession.sendText(text).syncUninterruptibly();
            } catch (Exception e) {
                throw new IOException(e);
            }
        }

        @Override
        public void sendBinary(ByteBuffer data) throws IOException {
            try {
                byte[] bytes = new byte[data.remaining()];
                data.get(bytes);
                nettySession.sendBinary(bytes).syncUninterruptibly();
            } catch (Exception e) {
                throw new IOException(e);
            }
        }

        @Override
        public void sendText(String partialMessage, boolean isLast) throws IOException {
            if (isLast) {
                sendText(partialMessage);
            }
        }

        @Override
        public void sendBinary(ByteBuffer partialByte, boolean isLast) throws IOException {
            if (isLast) {
                sendBinary(partialByte);
            }
        }

        @Override
        @NonNull
        public OutputStream getSendStream() throws IOException {
            return new ByteArrayOutputStream() {
                @Override
                public void flush() throws IOException {
                    super.flush();
                    sendBinary(ByteBuffer.wrap(toByteArray()));
                    reset();
                }
            };
        }

        @Override
        @NonNull
        public Writer getSendWriter() throws IOException {
            return new Writer() {
                private final StringBuilder sb = new StringBuilder();

                @Override
                public void write(char @NonNull [] cbuf, int off, int len) {
                    sb.append(cbuf, off, len);
                }

                @Override
                public void flush() throws IOException {
                    if (!sb.isEmpty()) {
                        sendText(sb.toString());
                        sb.setLength(0);
                    }
                }

                @Override
                public void close() throws IOException {
                    flush();
                }
            };
        }

        @Override
        public void sendObject(Object data) throws IOException, EncodeException {
            sendText(encodeTextObject(data));
        }

        @Override
        public void sendPing(ByteBuffer applicationData) throws IOException {
            try {
                nettySession.sendPing().syncUninterruptibly();
            } catch (Exception e) {
                throw new IOException(e);
            }
        }

        @Override
        public void sendPong(ByteBuffer applicationData) throws IOException {
            try {
                nettySession.sendPong().syncUninterruptibly();
            } catch (Exception e) {
                throw new IOException(e);
            }
        }

        @Override
        public void setBatchingAllowed(boolean allowed) throws IOException {
        }

        @Override
        public boolean getBatchingAllowed() {
            return false;
        }

        @Override
        public void flushBatch() throws IOException {
        }
    }

    // RemoteEndpoint.Async implementation
    private class NettyAsyncRemote implements RemoteEndpoint.Async {
        private long sendTimeout;

        @Override
        public long getSendTimeout() {
            return sendTimeout;
        }

        @Override
        public void setSendTimeout(long timeoutmillis) {
            this.sendTimeout = timeoutmillis;
        }

        @Override
        public void sendText(String text, SendHandler handler) {
            nettySession.sendText(text).addListener(future -> {
                if (handler != null) {
                    handler.onResult(future.isSuccess() ?
                            new SendResult(JsrWebSocketSession.this) :
                            new SendResult(JsrWebSocketSession.this, future.cause()));
                }
            });
        }

        @Override
        @NonNull
        public Future<Void> sendText(String text) {
            CompletableFuture<Void> completable = new CompletableFuture<>();
            nettySession.sendText(text).addListener(future -> {
                if (future.isSuccess()) {
                    completable.complete(null);
                } else {
                    completable.completeExceptionally(future.cause());
                }
            });
            return completable;
        }

        @Override
        @NonNull
        public Future<Void> sendBinary(@NonNull ByteBuffer data) {
            CompletableFuture<Void> completable = new CompletableFuture<>();
            byte[] bytes = new byte[data.remaining()];
            data.get(bytes);
            nettySession.sendBinary(bytes).addListener(future -> {
                if (future.isSuccess()) {
                    completable.complete(null);
                } else {
                    completable.completeExceptionally(future.cause());
                }
            });
            return completable;
        }

        @Override
        public void sendBinary(@NonNull ByteBuffer data, SendHandler handler) {
            byte[] bytes = new byte[data.remaining()];
            data.get(bytes);
            nettySession.sendBinary(bytes).addListener(future -> {
                if (handler != null) {
                    handler.onResult(future.isSuccess() ?
                            new SendResult(JsrWebSocketSession.this) :
                            new SendResult(JsrWebSocketSession.this, future.cause()));
                }
            });
        }

        @Override
        @NonNull
        public Future<Void> sendObject(Object data) {
            try {
                return sendText(encodeTextObject(data));
            } catch (EncodeException e) {
                CompletableFuture<Void> future = new CompletableFuture<>();
                future.completeExceptionally(e);
                return future;
            }
        }

        @Override
        public void sendObject(Object data, SendHandler handler) {
            try {
                sendText(encodeTextObject(data), handler);
            } catch (EncodeException e) {
                if (handler != null) {
                    handler.onResult(new SendResult(JsrWebSocketSession.this, e));
                }
            }
        }

        @Override
        public void setBatchingAllowed(boolean allowed) throws IOException {
        }

        @Override
        public boolean getBatchingAllowed() {
            return false;
        }

        @Override
        public void sendPing(ByteBuffer applicationData) throws IOException {
            try {
                nettySession.sendPing().syncUninterruptibly();
            } catch (Exception e) {
                throw new IOException(e);
            }
        }

        @Override
        public void sendPong(ByteBuffer applicationData) throws IOException {
            try {
                nettySession.sendPong().syncUninterruptibly();
            } catch (Exception e) {
                throw new IOException(e);
            }
        }

        @Override
        public void flushBatch() throws IOException {
        }
    }

}
