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
package com.aspectran.mybatis;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

/**
 * Interface that provides access to a context-bound MyBatis {@link SqlSession}
 * and its associated {@link SqlSessionAdvice}.
 *
 * <p>Created: 2026. 4. 5.</p>
 */
public interface SqlSessionProvider {

    /**
     * Returns the current SqlSession bound to the current activity context.
     * @return the active SqlSession
     */
    SqlSession getSqlSession();

    /**
     * Returns the {@link SqlSessionAdvice} that manages the lifecycle of the
     * current SqlSession.
     * @return the SqlSessionAdvice
     */
    SqlSessionAdvice getSqlSessionAdvice();

    /**
     * Returns the {@link SqlSessionFactory} used by this provider.
     * @return the SqlSessionFactory
     */
    SqlSessionFactory getSqlSessionFactory();

}
