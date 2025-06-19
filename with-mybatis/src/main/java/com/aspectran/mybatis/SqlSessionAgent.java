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

import com.aspectran.core.activity.ActivityData;
import com.aspectran.core.component.bean.annotation.Advisable;
import com.aspectran.utils.annotation.jsr305.Nullable;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

/**
 * The SqlSession Agent.
 */
public class SqlSessionAgent extends SqlSessionProvider implements SqlSession {

    private boolean autoParameters;

    public SqlSessionAgent(String relevantAspectId) {
        super(relevantAspectId);
    }

    public void setAutoParameters(boolean autoParameters) {
        this.autoParameters = autoParameters;
    }

    @Advisable
    @Override
    public <T> T selectOne(String statement) {
        if (autoParameters) {
            return getSqlSession().selectOne(statement, getActivityData());
        } else {
            return getSqlSession().selectOne(statement);
        }
    }

    @Advisable
    @Override
    public <T> T selectOne(String statement, Object parameter) {
        return getSqlSession().selectOne(statement, parameter);
    }

    @Advisable
    @Override
    public <E> List<E> selectList(String statement) {
        if (autoParameters) {
            return getSqlSession().selectList(statement, getActivityData());
        } else {
            return getSqlSession().selectList(statement);
        }
    }

    @Advisable
    @Override
    public <E> List<E> selectList(String statement, Object parameter) {
        return getSqlSession().selectList(statement, parameter);
    }

    @Advisable
    @Override
    public <E> List<E> selectList(String statement, Object parameter, RowBounds rowBounds) {
        return getSqlSession().selectList(statement, parameter, rowBounds);
    }

    @Advisable
    @Override
    public <K, V> Map<K, V> selectMap(String statement, String mapKey) {
        if (autoParameters) {
            return getSqlSession().selectMap(statement, getActivityData(), mapKey);
        } else {
            return getSqlSession().selectMap(statement, mapKey);
        }
    }

    @Advisable
    @Override
    public <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey) {
        return getSqlSession().selectMap(statement, parameter, mapKey);
    }

    @Advisable
    @Override
    public <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey, RowBounds rowBounds) {
        return getSqlSession().selectMap(statement, parameter, mapKey, rowBounds);
    }

    @Advisable
    @Override
    public <T> Cursor<T> selectCursor(String statement) {
        if (autoParameters) {
            return getSqlSession().selectCursor(statement, getActivityData());
        } else {
            return getSqlSession().selectCursor(statement);
        }
    }

    @Advisable
    @Override
    public <T> Cursor<T> selectCursor(String statement, Object parameter) {
        return getSqlSession().selectCursor(statement, parameter);
    }

    @Advisable
    @Override
    public <T> Cursor<T> selectCursor(String statement, Object parameter, RowBounds rowBounds) {
        return getSqlSession().selectCursor(statement, parameter, rowBounds);
    }

    @Advisable
    @Override
    public void select(String statement, Object parameter, ResultHandler handler) {
        getSqlSession().select(statement, parameter, handler);
    }

    @Advisable
    @Override
    public void select(String statement, ResultHandler handler) {
        if (autoParameters) {
            getSqlSession().select(statement, getActivityData(), handler);
        } else {
            getSqlSession().select(statement, handler);
        }
    }

    @Advisable
    @Override
    public void select(String statement, Object parameter, RowBounds rowBounds, ResultHandler handler) {
        getSqlSession().select(statement, parameter, rowBounds, handler);
    }

    @Advisable
    @Override
    public int insert(String statement) {
        if (autoParameters) {
            return getSqlSession().insert(statement, getActivityData());
        } else {
            return getSqlSession().insert(statement);
        }
    }

    @Advisable
    @Override
    public int insert(String statement, Object parameter) {
        return getSqlSession().insert(statement, parameter);
    }

    @Advisable
    @Override
    public int update(String statement) {
        if (autoParameters) {
            return getSqlSession().update(statement, getActivityData());
        } else {
            return getSqlSession().update(statement);
        }
    }

    @Advisable
    @Override
    public int update(String statement, Object parameter) {
        return getSqlSession().update(statement, parameter);
    }

    @Advisable
    @Override
    public int delete(String statement) {
        if (autoParameters) {
            return getSqlSession().delete(statement, getActivityData());
        } else {
            return getSqlSession().delete(statement);
        }
    }

    @Advisable
    @Override
    public int delete(String statement, Object parameter) {
        return getSqlSession().delete(statement, parameter);
    }

    @Advisable
    @Override
    public void commit() {
        getSqlSession().commit();
    }

    @Advisable
    @Override
    public void commit(boolean force) {
        getSqlSession().commit(force);
    }

    @Advisable
    @Override
    public void rollback() {
        getSqlSession().rollback();
    }

    @Advisable
    @Override
    public void rollback(boolean force) {
        getSqlSession().rollback(force);
    }

    @Advisable
    @Override
    public List<BatchResult> flushStatements() {
        return getSqlSession().flushStatements();
    }

    @Advisable
    @Override
    public void close() {
        getSqlSessionAdvice().close(true);
    }

    @Advisable
    @Override
    public void clearCache() {
        getSqlSession().clearCache();
    }

    @Advisable
    @Override
    public Configuration getConfiguration() {
        return getSqlSession().getConfiguration();
    }

    @Advisable
    @Override
    public <T> T getMapper(Class<T> type) {
        return getSqlSession().getMapper(type);
    }

    @Advisable
    @Override
    public Connection getConnection() {
        return getSqlSession().getConnection();
    }

    @Nullable
    private ActivityData getActivityData() {
        return getAvailableActivity().getActivityData();
    }

}
