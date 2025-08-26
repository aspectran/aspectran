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
 * Provides the core components for session management.
 * This package includes interfaces and classes for creating, managing, and expiring sessions.
 * It defines the session lifecycle and provides mechanisms for session persistence.
 *
 * <p>The main components are:
 * <ul>
 *   <li>{@link com.aspectran.core.component.session.SessionManager} for managing sessions</li>
 *   <li>{@link com.aspectran.core.component.session.Session} for representing a user session</li>
 *   <li>{@link com.aspectran.core.component.session.SessionCache} for caching sessions</li>
 *   <li>{@link com.aspectran.core.component.session.SessionStore} for persisting sessions</li>
 * </ul>
 */
package com.aspectran.core.component.session;
