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
package com.aspectran.netty.service;

import com.aspectran.core.component.session.SessionManager;
import com.aspectran.web.service.WebService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import org.jspecify.annotations.NonNull;

import java.io.IOException;

/**
 * The main interface for the Aspectran Netty service.
 * <p>This service extends {@link WebService} to handle HTTP requests in a high-performance,
 * non-blocking, servlet-less Netty environment.</p>
 *
 * <p>Created: 2026-09-02</p>
 */
public interface NettyService extends WebService {

    /**
     * Returns whether session adaptation is enabled for this Netty service.
     * @return true if session adaptation is enabled; false otherwise
     */
    boolean isSessionAdaptable();

    /**
     * Returns the session manager associated with this service.
     * @return the session manager
     */
    SessionManager getSessionManager();

    /**
     * Sets the session manager for this service.
     * @param sessionManager the session manager
     */
    void setSessionManager(SessionManager sessionManager);

    /**
     * Returns the context path of this service.
     * @return the context path, or an empty string if this is the root context
     */
    String getContextPath();

    /**
     * Sets the context path for this service.
     * @param contextPath the context path
     */
    void setContextPath(String contextPath);

    /**
     * Processes an incoming HTTP request using Netty's {@link ChannelHandlerContext} and {@link FullHttpRequest}.
     * @param ctx the Netty channel handler context
     * @param request the full HTTP request
     * @return true if the request was handled; false otherwise
     * @throws IOException if an I/O error occurs during processing
     */
    boolean service(@NonNull ChannelHandlerContext ctx, @NonNull FullHttpRequest request) throws IOException;

    /**
     * Returns whether proxy address forwarding headers (X-Forwarded-*) are trusted.
     * @return true if proxy address forwarding is enabled; false otherwise
     */
    boolean isProxyAddressForwarding();

    /**
     * Sets whether proxy address forwarding headers (X-Forwarded-*) are trusted.
     * @param proxyAddressForwarding true to enable proxy address forwarding; false otherwise
     */
    void setProxyAddressForwarding(boolean proxyAddressForwarding);

}
