/*
 * Copyright (c) 2008-2018 The Aspectran Project
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
 * Interface for receiving notification events about Session attribute changes.
 *
 * <p>Created: 2017. 6. 25.</p>
 */
public interface SessionAttributeListener {

    /**
     * Receives notification that an attribute has been added to a session.
     *
     * @param session the session to which the object is bound or unbound
     * @param name the name with which the object is bound or unbound
     * @param value the new value of the attribute that has been added
     */
    void attributeAdded(Session session, String name, Object value);

    /**
     * Receives notification that an attribute has been removed from a session.
     *
     * @param session the session to which the object is bound or unbound
     * @param name the name with which the object is bound or unbound
     * @param value the old value of the attribute that has been removed
     */
    void attributeRemoved(Session session, String name, Object value);

    /**
     * Receives notification that an attribute has been replaced in a session.
     *
     * @param session the session to which the object is bound or unbound
     * @param name the name with which the object is bound or unbound
     * @param value the old value of the attribute that has been removed
     */
    void attributeReplaced(Session session, String name, Object value);

}
