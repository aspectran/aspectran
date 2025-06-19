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
package com.aspectran.undertow.server.handler;

import com.aspectran.core.component.bean.aware.ActivityContextAware;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.undertow.server.handler.logging.LoggingGroupHandlerWrapper;
import com.aspectran.utils.Assert;
import com.aspectran.utils.annotation.jsr305.NonNull;
import io.undertow.server.HandlerWrapper;
import io.undertow.server.HttpHandler;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

public abstract class AbstractRequestHandlerFactory implements ActivityContextAware {

    private ActivityContext context;

    private List<HandlerWrapper> handlerChainWrappers;

    @NonNull
    protected ActivityContext getActivityContext() {
        Assert.state(context != null, "No ActivityContext injected");
        return context;
    }

    @Override
    public void setActivityContext(@NonNull ActivityContext context) {
        this.context = context;
    }

    public void setHandlerChainWrappers(HandlerWrapper[] handlerWrappers) {
        if (handlerWrappers == null || handlerWrappers.length == 0) {
            throw new IllegalArgumentException("handlerWrappers must not be null or empty");
        }
        this.handlerChainWrappers = Arrays.asList(handlerWrappers);
    }

    protected boolean hasLoggingGroupHandlerWrapper() {
        for (HandlerWrapper handlerWrapper : handlerChainWrappers) {
            if (handlerWrapper instanceof LoggingGroupHandlerWrapper) {
                return true;
            }
        }
        return false;
    }

    protected HttpHandler wrapHandler(HttpHandler handler) {
        if (handlerChainWrappers != null) {
            HttpHandler current = handler;
            ListIterator<HandlerWrapper> iterator = handlerChainWrappers.listIterator(handlerChainWrappers.size());
            while (iterator.hasPrevious()) {
                HandlerWrapper wrapper = iterator.previous();
                current = wrapper.wrap(current);
            }
            return current;
        } else {
            return handler;
        }
    }

}
