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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * The default implementation of the {@link EventPublisher} interface.
 * It dispatches events to all matching listeners synchronously.
 *
 * @since 8.6.0
 */
public class DefaultEventPublisher implements EventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(DefaultEventPublisher.class);

    private final EventListenerRegistry listenerRegistry;

    /**
     * Instantiates a new Default event publisher.
     * @param listenerRegistry the listener registry
     */
    public DefaultEventPublisher(EventListenerRegistry listenerRegistry) {
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void publish(Object event) {
        if (event == null) {
            logger.warn("Cannot publish a null event");
            return;
        }

        List<ListenerMethod> listeners = listenerRegistry.getListeners(event.getClass());
        if (listeners != null && !listeners.isEmpty()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Publishing event '{}' to {} listener(s)", event, listeners.size());
            }
            for (ListenerMethod listener : listeners) {
                try {
                    listener.invoke(event);
                } catch (Exception e) {
                    logger.error("Failed to invoke event listener method for event '{}'. Listener: {}", event, listener, e);
                }
            }
        } else {
            if (logger.isTraceEnabled()) {
                logger.trace("No listeners found for event type [{}]", event.getClass().getName());
            }
        }
    }

}
