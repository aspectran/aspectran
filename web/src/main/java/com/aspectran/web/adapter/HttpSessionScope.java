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
 * The Class HttpSessionScope.
 */
public class HttpSessionScope extends SessionScope implements HttpSessionBindingListener, Serializable {

    @Serial
    private static final long serialVersionUID = 209145824535745248L;

    private static final Logger logger = LoggerFactory.getLogger(HttpSessionScope.class);

    /**
     * Instantiates a new HttpSessionScope.
     */
    public HttpSessionScope() {
        super();
    }

    @Override /* Explicit overrides for backward compatibility with Servlet 3.x */
    public  void valueBound(HttpSessionBindingEvent event) {
        if (logger.isDebugEnabled()) {
            logger.debug("New HttpSessionScope bound in session {}", event.getSession());
        }
    }

    @Override
    public void valueUnbound(HttpSessionBindingEvent event) {
        if (logger.isDebugEnabled()) {
            logger.debug("HttpSessionScope removed from session {}", event.getSession());
        }

        destroy();
    }

}
