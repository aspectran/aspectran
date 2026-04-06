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
 * Add-on package for using Querydsl with JPA.
 *
 * <p>This package provides a seamless integration of Querydsl's type-safe queries
 * with Aspectran's AOP-based JPA transaction management.</p>
 *
 * <h3>Key Classes:</h3>
 * <ul>
 *   <li>{@link com.aspectran.jpa.querydsl.AbstractEntityQuery}: Base class for integrated
 *       {@code EntityManager} and {@code JPQLQueryFactory}.</li>
 *   <li>{@link com.aspectran.jpa.querydsl.DefaultEntityQuery}: An integrated agent
 *       that uses a single transaction aspect for all operations.</li>
 *   <li>{@link com.aspectran.jpa.querydsl.routing.RoutingEntityQuery}: An advanced
 *       integrated agent that routes operations between primary and replica
 *       transaction aspects.</li>
 * </ul>
 */
package com.aspectran.jpa.querydsl;
