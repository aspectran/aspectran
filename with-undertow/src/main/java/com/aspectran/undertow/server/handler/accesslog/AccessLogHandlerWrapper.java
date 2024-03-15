/*
 * Copyright (c) 2008-2024 The Aspectran Project
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

import com.aspectran.core.component.bean.annotation.AvoidAdvice;
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
 * <p>Created: 2019-08-18</p>
 */
public class AccessLogHandlerWrapper implements ActivityContextAware, HandlerWrapper {

    private ClassLoader classLoader;

    private String formatString;

    private String category;

    @Override
    @AvoidAdvice
    public void setActivityContext(@NonNull ActivityContext context) {
        this.classLoader = context.getAvailableActivity().getClassLoader();
    }

    public ClassLoader getClassLoader() {
        if (classLoader == null) {
            return ClassUtils.getDefaultClassLoader();
        } else {
            return classLoader;
        }
    }

    public void setFormatString(String formatString) {
        this.formatString = formatString;
    }

    public void setCategory(String category) {
        this.category = category;
    }

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
