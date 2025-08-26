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
package com.aspectran.undertow.server.handler.accesslog;

import com.aspectran.core.component.bean.aware.ActivityContextAware;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.utils.ClassUtils;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import io.undertow.server.HandlerWrapper;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.accesslog.AccessLogHandler;
import io.undertow.server.handlers.accesslog.AccessLogReceiver;

/**
 * A {@link HandlerWrapper} that creates and configures an {@link AccessLogHandler}.
 * <p>This wrapper allows for easy, bean-style configuration of Undertow's access logging
 * within the Aspectran configuration files. It sets up a {@link TowAccessLogReceiver}
 * to route log messages to SLF4J.</p>
 *
 * <p>Created: 2019-08-18</p>
 */
public class AccessLogHandlerWrapper implements ActivityContextAware, HandlerWrapper {

    private ClassLoader classLoader;

    private String formatString;

    private String category;

    @Override
    public void setActivityContext(@NonNull ActivityContext context) {
        this.classLoader = context.getClassLoader();
    }

    /**
     * Returns the class loader to be used by the access log handler.
     * @return the class loader
     */
    public ClassLoader getClassLoader() {
        if (classLoader == null) {
            return ClassUtils.getDefaultClassLoader();
        } else {
            return classLoader;
        }
    }

    /**
     * Sets the access log format string (e.g., "common", "combined", or a custom pattern).
     * @param formatString the log format string
     */
    public void setFormatString(String formatString) {
        this.formatString = formatString;
    }

    /**
     * Sets the SLF4J logger category to which access log messages will be sent.
     * @param category the logger category name
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * Wraps the given handler with a new {@link AccessLogHandler}
     * configured with the specified format and logger category.
     * @param handler the next handler in the chain
     * @return the new {@code AccessLogHandler}
     */
    @Override
    public HttpHandler wrap(HttpHandler handler) {
        if (handler == null) {
            throw new IllegalArgumentException("handler must not be null");
        }
        AccessLogReceiver accessLogReceiver = new TowAccessLogReceiver(category);
        String formatString = (StringUtils.hasText(this.formatString) ? this.formatString : "combined");
        return new AccessLogHandler(handler, accessLogReceiver, formatString, getClassLoader());
    }

}
