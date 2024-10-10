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
package com.aspectran.undertow.server.handler;

import io.undertow.server.HandlerWrapper;
import io.undertow.server.HttpHandler;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

public abstract class AbstractRequestHandlerFactory {

    private List<HandlerWrapper> handlerChainWrappers;

    public void setHandlerChainWrappers(HandlerWrapper[] handlerWrappers) {
        if (handlerWrappers == null || handlerWrappers.length == 0) {
            throw new IllegalArgumentException("handlerWrappers must not be null or empty");
        }
        this.handlerChainWrappers = Arrays.asList(handlerWrappers);
    }

    protected HttpHandler wrapHandler(HttpHandler wrapee) {
        if (handlerChainWrappers != null) {
            HttpHandler current = wrapee;
            ListIterator<HandlerWrapper> iterator = handlerChainWrappers.listIterator(handlerChainWrappers.size());
            while (iterator.hasPrevious()) {
                HandlerWrapper wrapper = iterator.previous();
                current = wrapper.wrap(current);
            }
            return current;
        } else {
            return wrapee;
        }
    }

}
