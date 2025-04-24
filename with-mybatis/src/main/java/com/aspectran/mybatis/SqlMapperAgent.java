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

import org.apache.ibatis.session.SqlSession;

public interface SqlMapperAgent {

    SqlSession getSimpleSqlSession();

    SqlSession getBatchSqlSession();

    SqlSession getReuseSqlSession();

    default <T> T simple(Class<T> type) {
        return getSimpleSqlSession().getMapper(type);
    }

    default <T> T batch(Class<T> type) {
        return getBatchSqlSession().getMapper(type);
    }

    default <T> T reuse(Class<T> type) {
        return getReuseSqlSession().getMapper(type);
    }

}
