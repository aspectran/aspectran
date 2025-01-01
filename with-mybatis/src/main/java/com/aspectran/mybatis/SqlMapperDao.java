/*
 * Copyright (c) 2008-2025 The Aspectran Project
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

public abstract class SqlMapperDao<T> {

    private final SqlMapperAgent mapperAgent;

    private final Class<T> mapperInterface;

    public SqlMapperDao(SqlMapperAgent mapperAgent, Class<T> mapperInterface) {
        this.mapperAgent = mapperAgent;
        this.mapperInterface = mapperInterface;
    }

    public T simple() {
        return mapperAgent.simple(mapperInterface);
    }

    public T batch() {
        return mapperAgent.batch(mapperInterface);
    }

    public T reuse() {
        return mapperAgent.reuse(mapperInterface);
    }

}
