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
package com.aspectran.core.component.bean.event;

import com.aspectran.core.component.bean.annotation.EventListener;
import com.aspectran.utils.Assert;
import com.aspectran.utils.annotation.jsr305.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A registry for methods that are annotated with @EventListener.
 *
 * @since 8.6.0
 */
public class EventListenerRegistry {

    private static final Logger logger = LoggerFactory.getLogger(EventListenerRegistry.class);

    private final Map<Class<?>, List<ListenerMethod>> listenerMap = new ConcurrentHashMap<>();

    public void registerListener(Object bean) {
        Assert.notNull(bean, "bean must not be null");
        for (Method method : bean.getClass().getMethods()) {
            if (method.isAnnotationPresent(EventListener.class)) {
                registerListener(bean, method);
            }
        }
    }

    private void registerListener(Object bean, @NonNull Method method) {
        if (method.getParameterCount() != 1) {
            logger.warn("Method '{}' annotated with @EventListener must have exactly one parameter", method);
            return;
        }
        Class<?> eventType = method.getParameterTypes()[0];
        listenerMap.computeIfAbsent(eventType, k -> new ArrayList<>()).add(new ListenerMethod(bean, method));
        if (logger.isDebugEnabled()) {
            logger.debug("Registered @EventListener method '{}' for event type [{}]", method, eventType.getName());
        }
    }

    public List<ListenerMethod> getListeners(Class<?> eventType) {
        return listenerMap.get(eventType);
    }

    public void clear() {
        listenerMap.clear();
    }

}
