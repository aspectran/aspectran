/*
 * Copyright (c) 2008-2025 The Aspectran Project
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
package com.aspectran.web.websocket.jsr356;

import com.aspectran.core.activity.InstantActivitySupport;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.service.CoreServiceHolder;
import com.aspectran.utils.ExceptionUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;
import com.aspectran.utils.logging.LoggingGroupHelper;
import jakarta.websocket.CloseReason;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.channels.ClosedChannelException;
import java.util.concurrent.TimeoutException;

/**
 * <p>Created: 2025-03-24</p>
 */
public abstract class AbstractEndpoint extends InstantActivitySupport {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final String groupName;

    public AbstractEndpoint() {
        this.groupName = resolveLoggingGroup();
    }

    public AbstractEndpoint(String groupName) {
        this.groupName = groupName;
    }

    @OnOpen
    public void doOnOpen(@NonNull Session session) throws IOException {
        setLoggingGroup();
        if (checkAuthorized(session)) {
            if (logger.isDebugEnabled()) {
                logger.debug("WebSocket connection established for session {}", session.getId());
            }
            registerMessageHandlers(session);
        } else {
            String reason = "Unauthorized";
            session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, reason));
            throw new IOException("WebSocket is closed before the connection is established. Cause: " + reason);
        }
    }

    protected boolean checkAuthorized(Session session) {
        return true;
    }

    protected abstract void registerMessageHandlers(Session session);

    @OnClose
    public void doOnClose(Session session, CloseReason reason) {
        setLoggingGroup();
        if (logger.isDebugEnabled()) {
            logger.debug("Websocket session {} has been closed. Reason: {}", session.getId(), reason);
        }
        removeSession(session);
    }

    @OnError
    public void doOnError(@NonNull Session session, Throwable error) {
        setLoggingGroup();
        if (!ExceptionUtils.hasCause(error, ClosedChannelException.class, TimeoutException.class)) {
            logger.warn("Error in websocket session: {}", session.getId(), error);
        }
        try {
            removeSession(session);
            session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, null));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected abstract void removeSession(Session session);

    protected void setLoggingGroup() {
        if (groupName != null) {
            LoggingGroupHelper.set(groupName);
        }
    }

    protected void setLoggingGroup(String groupName) {
        if (groupName != null) {
            LoggingGroupHelper.set(groupName);
        } else {
            LoggingGroupHelper.clear();
        }
    }

    @Nullable
    private String resolveLoggingGroup() {
        ActivityContext context = CoreServiceHolder.findActivityContext(getClass());
        if (context != null && context.getName() != null) {
            return context.getName();
        }
        return null;
    }

}
