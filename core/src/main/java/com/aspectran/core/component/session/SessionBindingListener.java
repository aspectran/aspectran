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

import java.util.EventListener;

/**
 * Causes an object to be notified when it is bound to or unbound from a session.
 *
 * <p>Created: 2017. 6. 25.</p>
 */
public interface SessionBindingListener extends EventListener {

    /**
     *
     * Notifies the object that it is being bound to
     * a session and identifies the session.
     *
     * @param session the session to which the object is bound or unbound
     * @param name the name with which the object is bound or unbound
     * @param value the value of the attribute that has been added, removed or replaced.
     * @see #valueUnbound
     */
    void valueBound(Session session, String name, Object value);

    /**
     *
     * Notifies the object that it is being unbound
     * from a session and identifies the session.
     *
     * @param session the session to which the object is bound or unbound
     * @param name the name with which the object is bound or unbound
     * @param value the value of the attribute that has been added, removed or replaced.
     * @see #valueBound
     */
    void valueUnbound(Session session, String name, Object value);

}
