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
package com.aspectran.web.adapter;

import com.aspectran.core.component.bean.scope.SessionScope;
import jakarta.servlet.http.HttpSessionBindingEvent;
import jakarta.servlet.http.HttpSessionBindingListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serial;
import java.io.Serializable;

/**
 * A specialized {@link com.aspectran.core.component.bean.scope.SessionScope} for the
 * Servlet environment.
 * <p>This class implements {@link jakarta.servlet.http.HttpSessionBindingListener}
 * to receive notifications when it is bound to or unbound from an
 * {@link jakarta.servlet.http.HttpSession}. When unbound from the session, it
 * automatically calls the {@link #destroy()} method to clean up all session-scoped beans.</p>
 *
 * @since 2.0.0
 */
public class HttpSessionScope extends SessionScope implements HttpSessionBindingListener, Serializable {

    @Serial
    private static final long serialVersionUID = 209145824535745248L;

    private static final Logger logger = LoggerFactory.getLogger(HttpSessionScope.class);

    /**
     * Creates a new {@code HttpSessionScope}.
     */
    public HttpSessionScope() {
        super();
    }

    /**
     * {@inheritDoc}
     * <p>This method is called by the servlet container when this object is bound to a session.</p>
     */
    @Override
    public void valueBound(HttpSessionBindingEvent event) {
        if (logger.isDebugEnabled()) {
            logger.debug("New HttpSessionScope bound in session {}", event.getSession());
        }
    }

    /**
     * {@inheritDoc}
     * <p>This method is called by the servlet container when this object is unbound
     * from a session. It triggers the destruction of all beans in this scope.</p>
     */
    @Override
    public void valueUnbound(HttpSessionBindingEvent event) {
        if (logger.isDebugEnabled()) {
            logger.debug("HttpSessionScope removed from session {}", event.getSession());
        }
        destroy();
    }

}
