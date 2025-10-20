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
/**
 * Provides the core components for session management in Aspectran.
 * <p>
 * This package defines a layered architecture for creating, managing, and persisting
 * user sessions. It handles the complete session lifecycle, including creation,
 * invalidation, and expiration, with support for pluggable persistence mechanisms.
 *
 * <h2>Core Architecture</h2>
 * The session management system is composed of several key components that work together:
 * <ul>
 *   <li><b>{@link com.aspectran.core.component.session.SessionManager}</b>:
 *       The central coordinator and main entry point for all session operations.
 *       It orchestrates the other components and manages the overall session lifecycle.</li>
 *
 *   <li><b>{@link com.aspectran.core.component.session.SessionCache}</b>:
 *       An in-memory cache for active {@link com.aspectran.core.component.session.Session} objects.
 *       It provides fast access to sessions and is responsible for applying caching strategies
 *       and checking for session validity and expiration before returning a session to the caller.</li>
 *
 *   <li><b>{@link com.aspectran.core.component.session.SessionStore}</b>:
 *       The persistence layer responsible for reading and writing raw
 *       {@link com.aspectran.core.component.session.SessionData} to a backing store
 *       (e.g., file system, database, or distributed cache). It acts as the authoritative
 *       source of truth for session data.</li>
 *
 *   <li><b>{@link com.aspectran.core.component.session.HouseKeeper}</b>:
 *       A background task scheduler responsible for periodically scavenging expired
 *       sessions. It works with the {@code SessionManager} to identify and clean up
 *       sessions that are no longer valid, ensuring system resources are properly managed.</li>
 * </ul>
 */
package com.aspectran.core.component.session;
