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
 * Provides integration with the JPA (Jakarta Persistence API).
 *
 * <p>This package includes key components to seamlessly use JPA within
 * an Aspectran application, featuring AOP-based transaction management and
 * simplified entity manager access.</p>
 *
 * <h3>Key Classes:</h3>
 * <ul>
 *   <li>{@link com.aspectran.jpa.EntityManagerFactoryBean}: A factory bean that
 *       creates and configures the JPA {@code EntityManagerFactory}.</li>
 *   <li>{@link com.aspectran.jpa.EntityManagerAdvice}: An advice bean that provides
 *       declarative transaction management for {@code EntityManager} operations,
 *       handling commit, rollback, and closing automatically via AOP.</li>
 *   <li>{@link com.aspectran.jpa.EntityManagerProvider}: Interface that provides access
 *       to a MyBatis {@code SqlSession} and the corresponding {@code SqlSessionAdvice}.</li>
 *   <li>{@link com.aspectran.jpa.AbstractEntityManagerProvider}: Base support class
 *       that manages access to a JPA {@code EntityManager} and the
 *       corresponding {@code EntityManagerAdvice} registered via AOP.</li>
 *   <li>{@link com.aspectran.jpa.DefaultEntityManagerAgent}: A proxy for
 *       {@code EntityManager} that uses a single transaction aspect for all operations.</li>
 *   <li>{@link com.aspectran.jpa.routing.RoutingEntityManagerAgent}: An advanced
 *       {@code EntityManager} proxy that routes operations between primary and
 *       replica transaction aspects based on method name patterns.</li>
 * </ul>
 */
package com.aspectran.jpa;
