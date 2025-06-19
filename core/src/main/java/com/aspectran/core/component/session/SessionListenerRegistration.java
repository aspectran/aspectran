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
 * Support for registering or removing session listeners in the session manager.
 *
 * <p>Created: 2020/05/09</p>
 *
 * @since 6.7.0
 */
public interface SessionListenerRegistration {

    /**
     * Register a session listener to the session manager.
     * @param listener the session listener to register
     */
    void register(SessionListener listener);

    /**
     * Register a session listener to the session manager.
     * @param listener the session listener to register
     * @param deploymentName the deployment name
     */
    void register(SessionListener listener, String deploymentName);

    /**
     * Remove a session listener from the session manager.
     * @param listener the session listener to remove
     */
    void remove(SessionListener listener);

    /**
     * Remove a session listener from the session manager.
     * @param listener the session listener to remove
     * @param deploymentName the deployment name
     */
    void remove(SessionListener listener, String deploymentName);

}
