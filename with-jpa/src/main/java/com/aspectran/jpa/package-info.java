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
 * Provides integration with the Jakarta Persistence API (JPA).
 * <p>This package includes key components to seamlessly use JPA within an
 * Aspectran application, featuring AOP-based transaction management and
 * simplified entity manager access.</p>
 *
 * <h3>Key Classes:</h3>
 * <ul>
 *   <li>{@link com.aspectran.jpa.EntityManagerFactoryBean}: A factory bean that
 *       creates and configures the Jakarta Persistence {@code EntityManagerFactory}.</li>
 *   <li>{@link com.aspectran.jpa.EntityManagerAdvice}: An advice bean that manages
 *       the lifecycle of a Jakarta Persistence {@code EntityManager} and its
 *       transaction boundaries.</li>
 *   <li>{@link com.aspectran.jpa.EntityManagerProvider}: A base class for components
 *       that need access to a context-bound {@code EntityManager} managed via AOP.
 *       It supports stack-based aspect tracking and intelligent routing between
 *       writable and read-only sessions.</li>
 *   <li>{@link com.aspectran.jpa.EntityManagerAgent}: A proxy for {@code EntityManager}
 *       that simplifies data access and automatically participates in transactions
 *       managed by {@code EntityManagerAdvice}. It supports dual aspect IDs for
 *       switching between writable and read-only modes.</li>
 * </ul>
 */
package com.aspectran.jpa;
