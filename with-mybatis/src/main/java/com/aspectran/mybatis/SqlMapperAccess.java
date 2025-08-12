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

/**
 * Convenience base class that provides easy access to MyBatis mapper proxies
 * with different {@link org.apache.ibatis.session.ExecutorType} strategies.
 * <p>
 * Subclasses can expose typed accessors that call {@link #simple()},
 * {@link #batch()}, or {@link #reuse()} to obtain the mapper with the
 * corresponding executor behavior from a {@link SqlMapperProvider}.
 * </p>
 */
public abstract class SqlMapperAccess<T> {

    private final SqlMapperProvider mapperProvider;

    private final Class<T> mapperType;

    public SqlMapperAccess(SqlMapperProvider mapperProvider, Class<T> mapperType) {
        this.mapperProvider = mapperProvider;
        this.mapperType = mapperType;
    }

    public SqlMapperProvider getMapperProvider() {
        return mapperProvider;
    }

    public T simple() {
        return mapperProvider.simple(mapperType);
    }

    public T batch() {
        return mapperProvider.batch(mapperType);
    }

    public T reuse() {
        return mapperProvider.reuse(mapperType);
    }

}
