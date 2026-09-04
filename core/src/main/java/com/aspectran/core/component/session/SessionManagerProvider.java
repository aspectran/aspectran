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

import org.jspecify.annotations.Nullable;

/**
 * A provider interface for retrieving {@link SessionManager} instances
 * associated with deployments or context paths.
 *
 * <p>Created: 2026-09-04</p>
 */
public interface SessionManagerProvider {

    /**
     * Retrieves the {@link SessionManager} for the default or root context.
     * @return the session manager, or {@code null} if not found
     */
    @Nullable
    SessionManager getSessionManager();

    /**
     * Retrieves the {@link SessionManager} for a specific deployment name or context path.
     * @param name the deployment name or context path
     * @return the session manager, or {@code null} if not found
     */
    @Nullable
    SessionManager getSessionManager(String name);

    /**
     * Retrieves the {@link SessionManager} for a web application by its context path.
     * @param path the context path of the web application
     * @return the session manager, or {@code null} if not found
     */
    @Nullable
    SessionManager getSessionManagerByPath(String path);

}
