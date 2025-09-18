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

/**
 * Strategy interface that supplies {@link SqlSession} instances configured for
 * different executor behaviors (SIMPLE, BATCH, REUSE) and provides convenient
 * helper methods to obtain mapper proxies for each mode.
 */
public interface SqlMapperProvider {

    /**
     * Returns a {@link SqlSession} with SIMPLE executor behavior.
     * @return a {@code SqlSession}
     */
    SqlSession getSimpleSqlSession();

    /**
     * Returns a {@link SqlSession} with BATCH executor behavior.
     * @return a {@code SqlSession}
     */
    SqlSession getBatchSqlSession();

    /**
     * Returns a {@link SqlSession} with REUSE executor behavior.
     * @return a {@code SqlSession}
     */
    SqlSession getReuseSqlSession();

    /**
     * Returns a mapper instance that is bound to the SIMPLE {@link SqlSession}.
     * @param mapperType the type of the mapper
     * @param <T> the type of the mapper
     * @return a mapper instance
     */
    default <T> T simple(Class<T> mapperType) {
        return getSimpleSqlSession().getMapper(mapperType);
    }

    /**
     * Returns a mapper instance that is bound to the BATCH {@link SqlSession}.
     * @param mapperType the type of the mapper
     * @param <T> the type of the mapper
     * @return a mapper instance
     */
    default <T> T batch(Class<T> mapperType) {
        return getBatchSqlSession().getMapper(mapperType);
    }

    /**
     * Returns a mapper instance that is bound to the REUSE {@link SqlSession}.
     * @param mapperType the type of the mapper
     * @param <T> the type of the mapper
     * @return a mapper instance
     */
    default <T> T reuse(Class<T> mapperType) {
        return getReuseSqlSession().getMapper(mapperType);
    }

}
