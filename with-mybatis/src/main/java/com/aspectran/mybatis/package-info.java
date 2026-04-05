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
 * Provides integration with the MyBatis persistence framework.
 * <p>This package includes key components to seamlessly use MyBatis within an
 * Aspectran application, featuring AOP-based transaction management and
 * simplified session access.</p>
 *
 * <h3>Key Classes:</h3>
 * <ul>
 *   <li>{@link com.aspectran.mybatis.SqlSessionFactoryBean}: A factory bean that
 *       creates and configures the MyBatis {@code SqlSessionFactory}.</li>
 *   <li>{@link com.aspectran.mybatis.SqlSessionAdvice}: An advice bean that provides
 *       declarative transaction management for {@code SqlSession} operations,
 *       handling commit, rollback, and closing automatically via AOP.</li>
 *   <li>{@link com.aspectran.mybatis.DefaultSqlSessionProvider}: Base support class that
 *       locates and manages access to a MyBatis {@code SqlSession} and the
 *       corresponding {@code SqlSessionAdvice} registered via AOP.</li>
 *   <li>{@link com.aspectran.mybatis.DefaultSqlSessionAgent}: A proxy for
 *       {@code SqlSession} that simplifies data access and automatically
 *       participates in transactions managed by {@code SqlSessionAdvice}.</li>
 *   <li>{@link com.aspectran.mybatis.SqlMapperProvider}: Strategy interface that
 *       supplies {@code SqlSession} instances for different executor behaviors
 *       (SIMPLE, BATCH, REUSE) and provides helper methods to obtain mappers.</li>
 *   <li>{@link com.aspectran.mybatis.SqlMapperAccess}: A convenience base class for
 *       accessing typed mapper interfaces with different executor types by
 *       delegating to a {@link com.aspectran.mybatis.SqlMapperProvider}.</li>
 * </ul>
 */
package com.aspectran.mybatis;
