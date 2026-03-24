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

import java.lang.reflect.ParameterizedType;

/**
 * Convenience base class that provides easy access to MyBatis mapper proxies
 * with different {@link org.apache.ibatis.session.ExecutorType} strategies.
 * <p>
 * Subclasses can expose typed accessors that call {@link #mapper()},
 * {@link #batchMapper()}, or {@link #reuseMapper()} to obtain the mapper
 * with the corresponding executor behavior from a {@link SqlMapperProvider}.
 * </p>
 *
 * @param <T> the type of the mapper interface
 */
public abstract class SqlMapperAccess<T> {

    private final SqlMapperProvider mapperProvider;

    private final Class<T> mapperType;

    /**
     * Instantiates a new SqlMapperAccess.
     * <p>This constructor automatically resolves the mapper interface type from the generic
     * type parameter. If the generic type information is missing or the inheritance
     * structure is too complex for automatic resolution, please use the constructor
     * {@link #SqlMapperAccess(SqlMapperProvider, Class)} to specify the mapper type explicitly.</p>
     * @param mapperProvider the provider for MyBatis mappers
     */
    @SuppressWarnings("unchecked")
    public SqlMapperAccess(SqlMapperProvider mapperProvider) {
        this.mapperType = (Class<T>)((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        this.mapperProvider = mapperProvider;
    }

    /**
     * Instantiates a new SqlMapperAccess.
     * @param mapperProvider the provider for MyBatis mappers
     * @param mapperType the specific mapper interface to be accessed
     */
    public SqlMapperAccess(SqlMapperProvider mapperProvider, Class<T> mapperType) {
        this.mapperProvider = mapperProvider;
        this.mapperType = mapperType;
    }

    /**
     * Returns the underlying mapper provider.
     * @return the mapper provider
     */
    public SqlMapperProvider getMapperProvider() {
        return mapperProvider;
    }

    /**
     * Returns the {@link SqlSession} instance that uses the SIMPLE executor type.
     * @return the SqlSession with SIMPLE executor behavior
     */
    public SqlSession session() {
        return mapperProvider.session();
    }

    /**
     * Returns the {@link SqlSession} instance that uses the BATCH executor type.
     * @return the SqlSession with BATCH executor behavior
     */
    public SqlSession batchSession() {
        return mapperProvider.batchSession();
    }

    /**
     * Returns the {@link SqlSession} instance that uses the REUSE executor type.
     * @return the SqlSession with REUSE executor behavior
     */
    public SqlSession reuseSession() {
        return mapperProvider.reuseSession();
    }

    /**
     * Returns a mapper instance that is bound to the SIMPLE {@link SqlSession}.
     * @return a mapper instance
     */
    public T mapper() {
        return mapperProvider.mapper(mapperType);
    }

    /**
     * Returns a mapper instance that is bound to the BATCH {@link SqlSession}.
     * @return a mapper instance
     */
    public T batchMapper() {
        return mapperProvider.batchMapper(mapperType);
    }

    /**
     * Returns a mapper instance that is bound to the REUSE {@link SqlSession}.
     * @return a mapper instance
     */
    public T reuseMapper() {
        return mapperProvider.reuseMapper(mapperType);
    }

    /**
     * Returns a mapper instance that uses the SIMPLE executor type.
     * @return a mapper instance with SIMPLE executor behavior
     * @deprecated use {@link #mapper()} instead
     */
    @Deprecated
    public T simple() {
        return mapper();
    }

    /**
     * Returns a mapper instance that uses the BATCH executor type.
     * @return a mapper instance with BATCH executor behavior
     * @deprecated use {@link #batchMapper()} instead
     */
    @Deprecated
    public T batch() {
        return batchMapper();
    }

    /**
     * Returns a mapper instance that uses the REUSE executor type.
     * @return a mapper instance with REUSE executor behavior
     * @deprecated use {@link #reuseMapper()} instead
     */
    @Deprecated
    public T reuse() {
        return reuseMapper();
    }

}
