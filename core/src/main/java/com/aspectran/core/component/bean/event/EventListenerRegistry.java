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
 * Manages the registration of {@link EventListener} methods.
 * This class scans all singleton beans upon initialization, finds methods annotated
 * with {@code @EventListener}, and stores them in a map for quick retrieval by event type.
 *
 * @since 8.6.0
 */
public class EventListenerRegistry {

    private static final Logger logger = LoggerFactory.getLogger(EventListenerRegistry.class);

    private final Map<Class<?>, List<ListenerMethod>> listenerMap = new ConcurrentHashMap<>();

    /**
     * Registers all {@code @EventListener} annotated methods on the given bean.
     * @param bean the bean instance to scan for listener methods
     */
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

    /**
     * Gets the list of listeners for a given event type.
     * @param eventType the event type
     * @return a list of listener methods, or {@code null} if no listeners are found
     */
    public List<ListenerMethod> getListeners(Class<?> eventType) {
        return listenerMap.get(eventType);
    }

    /**
     * Clears all registered listeners.
     */
    public void clear() {
        listenerMap.clear();
    }

}
