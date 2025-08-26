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
package com.aspectran.core.component.session;

/**
 * Interface for receiving notification events about session lifecycle changes.
 * This includes events for session creation, destruction, and attribute modification.
 *
 * <p>Created: 2017. 6. 25.</p>
 */
public interface SessionListener {

    /**
     * Receives notification that a session has been created.
     * @param session the session that was created
     */
    default void sessionCreated(Session session) {
    }

    /**
     * Receives notification that a session is about to be invalidated.
     * @param session the session that is being destroyed
     */
    default void sessionDestroyed(Session session) {
    }

    /**
     * Receives notification that a session is about to be evicted from the cache.
     * @param session the session that is being evicted
     */
    default void sessionEvicted(Session session) {
    }

    /**
     * Receives notification that a stored session is about to be restored in the cache.
     * @param session the session that is being restored
     */
    default void sessionResided(Session session) {
    }

    /**
     * Receives notification that an attribute has been added to a session.
     * @param session the session to which the attribute was added
     * @param name the name of the attribute
     * @param value the value of the attribute
     */
    default void attributeAdded(Session session, String name, Object value) {
    }

    /**
     * Receives notification that an attribute has been replaced in a session.
     *
     * @param session the session where the attribute was updated
     * @param name the name of the attribute
     * @param newValue the new value of the attribute
     * @param oldValue the old value of the attribute
     */
    default void attributeUpdated(Session session, String name, Object newValue, Object oldValue) {
    }

    /**
     * Receives notification that an attribute has been removed from a session.
     *
     * @param session the session from which the attribute was removed
     * @param name the name of the attribute
     * @param oldValue the old value of the attribute
     */
    default void attributeRemoved(Session session, String name, Object oldValue) {
    }

    /**
     * Receives notification that a session ID has been changed.
     *
     * @param session the session whose ID was changed
     * @param oldSessionId the old session ID
     */
    default void sessionIdChanged(Session session, String oldSessionId) {
    }

}
