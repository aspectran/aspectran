/*
 * Copyright (c) 2008-2019 The Aspectran Project
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
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import java.io.Serializable;

/**
 * The Class HttpSessionScope.
 */
public class HttpSessionScope extends SessionScope implements HttpSessionBindingListener, Serializable {

    private static final long serialVersionUID = 209145824535745248L;

    private static final Log log = LogFactory.getLog(HttpSessionScope.class);

    /**
     * Instantiates a new HttpSessionScope.
     */
    public HttpSessionScope() {
        super();
    }

    @Override /* Explicit overrides for backward compatibility with Servlet 3.x */
    public  void valueBound(HttpSessionBindingEvent event) {
        if (log.isDebugEnabled()) {
            log.debug("New HttpSessionScope bound in session " + event.getSession());
        }
    }

    @Override
    public void valueUnbound(HttpSessionBindingEvent event) {
        if (log.isDebugEnabled()) {
            log.debug("HttpSessionScope removed from session " + event.getSession());
        }

        destroy();
    }

}
