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

/**
 * Abstract base class for {@link RequestHandlerFactory} implementations.
 * <p>This class provides the common infrastructure for building a handler chain by applying
 * a list of {@link HandlerWrapper} instances to a root handler. This allows for a modular
 * approach to request processing, where concerns like logging, encoding, and session
 * management can be added as decorators.</p>
 */
public abstract class AbstractRequestHandlerFactory implements ActivityContextAware {

    private ActivityContext context;

    private List<HandlerWrapper> handlerChainWrappers;

    /**
     * Returns the current {@link ActivityContext}.
     * @return the activity context
     */
    @NonNull
    protected ActivityContext getActivityContext() {
        Assert.state(context != null, "No ActivityContext injected");
        return context;
    }

    @Override
    public void setActivityContext(@NonNull ActivityContext context) {
        this.context = context;
    }

    /**
     * Sets the chain of {@link HandlerWrapper}s to be applied to the root handler.
     * These wrappers are used to add cross-cutting concerns like logging, encoding, etc.
     * @param handlerWrappers the array of handler wrappers
     */
    public void setHandlerChainWrappers(HandlerWrapper[] handlerWrappers) {
        if (handlerWrappers == null || handlerWrappers.length == 0) {
            throw new IllegalArgumentException("handlerWrappers must not be null or empty");
        }
        this.handlerChainWrappers = Arrays.asList(handlerWrappers);
    }

    /**
     * Checks if a {@link LoggingGroupHandlerWrapper} is present in the handler chain.
     * @return true if the logging group handler wrapper exists, false otherwise
     */
    protected boolean hasLoggingGroupHandlerWrapper() {
        if (handlerChainWrappers != null) {
            for (HandlerWrapper handlerWrapper : handlerChainWrappers) {
                if (handlerWrapper instanceof LoggingGroupHandlerWrapper) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Wraps the given {@link HttpHandler} with the configured chain of wrappers.
     * The wrappers are applied in reverse order, so the first wrapper in the configured
     * list will be the first to handle an incoming request.
     * @param handler the root handler to be wrapped
     * @return the wrapped handler that represents the start of the chain
     */
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
