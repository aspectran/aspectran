/*
 * Copyright (c) 2008-2023 The Aspectran Project
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
 * Interface for receiving notification events about DefaultSession
 * lifecycle changes.
 *
 * <p>Created: 2017. 6. 25.</p>
 */
public interface SessionListener {

    /**
     * Receives notification that a session has been created.
     *
     * @param session the new session
     */
    default void sessionCreated(Session session) {
    }

    /**
     * Receives notification that a session is about to be invalidated.
     *
     * @param session the session
     */
    default void sessionDestroyed(Session session) {
    }

    /**
     * Receives notification that an attribute has been added to a session.
     *
     * @param session the session to which the object is bound or unbound
     * @param name the name with which the object is bound or unbound
     * @param value the new value of the attribute that has been added
     */
    default void attributeAdded(Session session, String name, Object value) {
    }

    /**
     * Receives notification that an attribute has been replaced in a session.
     *
     * @param session the session to which the object is bound or unbound
     * @param name the name with which the object is bound or unbound
     * @param newValue the new value of the attribute that has been added
     * @param oldValue the old value of the attribute that has been removed
     */
    default void attributeUpdated(Session session, String name, Object newValue, Object oldValue) {
    }

    /**
     * Receives notification that an attribute has been removed from a session.
     *
     * @param session the session to which the object is bound or unbound
     * @param name the name with which the object is bound or unbound
     * @param oldValue the old value of the attribute that has been removed
     */
    default void attributeRemoved(Session session, String name, Object oldValue) {
    }

    default void sessionIdChanged(Session session, String oldSessionId) {
    }

}
