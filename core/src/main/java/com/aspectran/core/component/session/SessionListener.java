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
 * Interface for receiving notification events about BasicSession
 * lifecycle changes.
 *
 * <p>Created: 2017. 6. 25.</p>
 */
public interface SessionListener extends EventListener {

    /**
     * Receives notification that a session has been created.
     *
     * @param session the basic session
     */
    void sessionCreated(Session session);

    /**
     * Receives notification that a session is about to be invalidated.
     *
     * @param session the basic session
     */
    void sessionDestroyed(Session session);

}
