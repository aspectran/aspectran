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

import java.util.EventListener;

/**
 * Notifies an object when it is bound to or unbound from a session.
 * This is similar to the {@code jakarta.servlet.http.HttpSessionBindingListener}.
 *
 * <p>Created: 2017. 6. 25.</p>
 *
 * @see Session#setAttribute(String, Object)
 * @see Session#removeAttribute(String)
 */
public interface SessionBindingListener extends EventListener {

    /**
     * Notifies the attribute that it is being bound to a session.
     * @param session the session to which the attribute is being bound
     * @param name the name with which the attribute is being bound
     * @param value the value of the attribute being bound
     */
    default void valueBound(Session session, String name, Object value) {
    }

    /**
     * Notifies the attribute that it is being unbound from a session.
     * @param session the session from which the attribute is being unbound
     * @param name the name with which the attribute is being unbound
     * @param value the value of the attribute being unbound
     */
    default void valueUnbound(Session session, String name, Object value) {
    }

}
