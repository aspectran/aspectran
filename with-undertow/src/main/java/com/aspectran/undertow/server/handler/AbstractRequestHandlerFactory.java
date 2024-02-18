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

import com.aspectran.core.component.bean.annotation.AvoidAdvice;
import com.aspectran.core.component.bean.aware.ActivityContextAware;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.utils.annotation.jsr305.NonNull;
import io.undertow.server.HandlerWrapper;
import io.undertow.server.HttpHandler;

import java.util.Arrays;
import java.util.List;

public abstract class AbstractRequestHandlerFactory implements ActivityContextAware {

    private ActivityContext context;

    private List<HandlerWrapper> outerHandlerChainWrappers;

    @NonNull
    public ActivityContext getActivityContext() {
        return context;
    }

    @Override
    @AvoidAdvice
    public void setActivityContext(ActivityContext context) {
        this.context = context;
    }

    public void setOuterHandlerChainWrappers(HandlerWrapper[] handlerWrappers) {
        if (handlerWrappers == null || handlerWrappers.length == 0) {
            throw new IllegalArgumentException("handlerWrappers must not be null or empty");
        }
        this.outerHandlerChainWrappers = Arrays.asList(handlerWrappers);
    }

    protected HttpHandler wrapHandler(HttpHandler wrapee) {
        if (outerHandlerChainWrappers != null) {
            HttpHandler current = wrapee;
            for (HandlerWrapper wrapper : outerHandlerChainWrappers) {
                current = wrapper.wrap(current);
            }
            return current;
        } else {
            return wrapee;
        }
    }

}
