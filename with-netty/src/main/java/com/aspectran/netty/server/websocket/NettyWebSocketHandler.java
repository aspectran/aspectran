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

import com.aspectran.netty.server.handler.logging.ChannelLoggingGroupHelper;
import com.aspectran.utils.Assert;
import com.aspectran.utils.logging.LoggingGroupHelper;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Netty channel handler that processes incoming {@link WebSocketFrame}s and delegates
 * them to a registered {@link NettyWebSocketListener}.
 *
 * <p>Created: 2026-09-02</p>
 */
public class NettyWebSocketHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private static final Logger logger = LoggerFactory.getLogger(NettyWebSocketHandler.class);

    private final NettyWebSocketSession session;

    private final NettyWebSocketListener listener;

    private final ExecutorService requestExecutor;

    private final WebSocketServerHandshaker handshaker;

    private final AtomicBoolean closed = new AtomicBoolean();

    /**
     * Creates a new handler for processing WebSocket frames for a specific session.
     * @param session the WebSocket session
     * @param listener the event listener to receive messages and lifecycle callbacks
     * @param requestExecutor optional executor service to dispatch listener callbacks asynchronously
     * @param handshaker the server handshaker used during the upgrade
     */
    public NettyWebSocketHandler(
            @NonNull NettyWebSocketSession session,
            @NonNull NettyWebSocketListener listener,
            @Nullable ExecutorService requestExecutor,
            @Nullable WebSocketServerHandshaker handshaker) {
        Assert.notNull(session, "session must not be null");
        Assert.notNull(listener, "listener must not be null");
        this.session = session;
        this.listener = listener;
        this.requestExecutor = requestExecutor;
        this.handshaker = handshaker;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        if (frame instanceof CloseWebSocketFrame closeFrame) {
            if (closed.compareAndSet(false, true)) {
                int code = closeFrame.statusCode();
                if (code == -1) {
                    code = 1000;
                }
                String reason = closeFrame.reasonText();
                int finalCode = code;
                dispatchTask(() -> {
                    try {
                        listener.onClose(session, finalCode, reason);
                    } catch (Exception e) {
                        logger.error("Error in WebSocket onClose", e);
                    }
                });
                if (handshaker != null) {
                    handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
                } else {
                    ctx.channel().close();
                }
            }
            return;
        }

        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
            return;
        }

        if (frame instanceof PongWebSocketFrame) {
            // Keep-alive pong acknowledgment
            return;
        }

        if (frame instanceof TextWebSocketFrame textFrame) {
            String text = textFrame.text();
            dispatchTask(() -> {
                try {
                    listener.onMessage(session, text);
                } catch (Exception e) {
                    logger.error("Error in WebSocket onMessage(text)", e);
                    listener.onError(session, e);
                }
            });
            return;
        }

        if (frame instanceof BinaryWebSocketFrame binaryFrame) {
            byte[] bytes = ByteBufUtil.getBytes(binaryFrame.content());
            dispatchTask(() -> {
                try {
                    listener.onMessage(session, bytes);
                } catch (Exception e) {
                    logger.error("Error in WebSocket onMessage(binary)", e);
                    listener.onError(session, e);
                }
            });
            return;
        }

        logger.warn("Unsupported WebSocket frame type: {}", frame.getClass().getName());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent idleStateEvent) {
            if (idleStateEvent.state() == IdleState.ALL_IDLE) {
                if (closed.compareAndSet(false, true)) {
                    session.close(1000, "Idle timeout");
                }
            }
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (closed.compareAndSet(false, true)) {
            dispatchTask(() -> {
                try {
                    listener.onClose(session, 1006, "Connection closed abnormally");
                } catch (Exception e) {
                    logger.error("Error in WebSocket onClose", e);
                }
            });
        }
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(@NonNull ChannelHandlerContext ctx, Throwable cause) {
        dispatchTask(() -> {
            try {
                listener.onError(session, cause);
            } catch (Exception e) {
                logger.error("Error in WebSocket onError", e);
            }
        });
        ctx.close();
    }

    private void dispatchTask(Runnable task) {
        String groupName = ChannelLoggingGroupHelper.get(session.getChannel());
        Runnable wrappedTask = (groupName != null ? () -> {
            LoggingGroupHelper.set(groupName);
            try {
                task.run();
            } finally {
                LoggingGroupHelper.clear();
            }
        } : task);
        if (requestExecutor != null && !requestExecutor.isShutdown()) {
            requestExecutor.submit(wrappedTask);
        } else {
            wrappedTask.run();
        }
    }

}
