/*
 * Copyright (c) 2008-2024 The Aspectran Project
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
import com.aspectran.core.activity.InstantActivitySupport;
import com.aspectran.core.component.bean.annotation.AvoidAdvice;
import com.aspectran.utils.annotation.jsr305.NonNull;
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
public class SqlSessionAgent extends InstantActivitySupport implements SqlSession {

    private final String relevantAspectId;

    private boolean autoParameters;

    public SqlSessionAgent(String relevantAspectId) {
        if (relevantAspectId == null) {
            throw new IllegalArgumentException("relevantAspectId must not be null");
        }
        this.relevantAspectId = relevantAspectId;
    }

    public void setAutoParameters(boolean autoParameters) {
        this.autoParameters = autoParameters;
    }

    @Override
    public <T> T selectOne(String statement) {
        if (autoParameters) {
            return getSqlSession().selectOne(statement, getActivityData());
        } else {
            return getSqlSession().selectOne(statement);
        }
    }

    @Override
    public <T> T selectOne(String statement, Object parameter) {
        return getSqlSession().selectOne(statement, parameter);
    }

    @Override
    public <E> List<E> selectList(String statement) {
        if (autoParameters) {
            return getSqlSession().selectList(statement, getActivityData());
        } else {
            return getSqlSession().selectList(statement);
        }
    }

    @Override
    public <E> List<E> selectList(String statement, Object parameter) {
        return getSqlSession().selectList(statement, parameter);
    }

    @Override
    public <E> List<E> selectList(String statement, Object parameter, RowBounds rowBounds) {
        return getSqlSession().selectList(statement, parameter, rowBounds);
    }

    @Override
    public <K, V> Map<K, V> selectMap(String statement, String mapKey) {
        if (autoParameters) {
            return getSqlSession().selectMap(statement, getActivityData(), mapKey);
        } else {
            return getSqlSession().selectMap(statement, mapKey);
        }
    }

    @Override
    public <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey) {
        return getSqlSession().selectMap(statement, parameter, mapKey);
    }

    @Override
    public <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey, RowBounds rowBounds) {
        return getSqlSession().selectMap(statement, parameter, mapKey, rowBounds);
    }

    @Override
    public <T> Cursor<T> selectCursor(String statement) {
        if (autoParameters) {
            return getSqlSession().selectCursor(statement, getActivityData());
        } else {
            return getSqlSession().selectCursor(statement);
        }
    }

    @Override
    public <T> Cursor<T> selectCursor(String statement, Object parameter) {
        return getSqlSession().selectCursor(statement, parameter);
    }

    @Override
    public <T> Cursor<T> selectCursor(String statement, Object parameter, RowBounds rowBounds) {
        return getSqlSession().selectCursor(statement, parameter, rowBounds);
    }

    @Override
    public void select(String statement, Object parameter, ResultHandler handler) {
        getSqlSession().select(statement, parameter, handler);
    }

    @Override
    public void select(String statement, ResultHandler handler) {
        if (autoParameters) {
            getSqlSession().select(statement, getActivityData(), handler);
        } else {
            getSqlSession().select(statement, handler);
        }
    }

    @Override
    public void select(String statement, Object parameter, RowBounds rowBounds, ResultHandler handler) {
        getSqlSession().select(statement, parameter, rowBounds, handler);
    }

    @Override
    public int insert(String statement) {
        if (autoParameters) {
            return getSqlSession().insert(statement, getActivityData());
        } else {
            return getSqlSession().insert(statement);
        }
    }

    @Override
    public int insert(String statement, Object parameter) {
        return getSqlSession().insert(statement, parameter);
    }

    @Override
    public int update(String statement) {
        if (autoParameters) {
            return getSqlSession().update(statement, getActivityData());
        } else {
            return getSqlSession().update(statement);
        }
    }

    @Override
    public int update(String statement, Object parameter) {
        return getSqlSession().update(statement, parameter);
    }

    @Override
    public int delete(String statement) {
        if (autoParameters) {
            return getSqlSession().delete(statement, getActivityData());
        } else {
            return getSqlSession().delete(statement);
        }
    }

    @Override
    public int delete(String statement, Object parameter) {
        return getSqlSession().delete(statement, parameter);
    }

    @Override
    public void commit() {
        getSqlSession().commit();
    }

    @Override
    public void commit(boolean force) {
        getSqlSession().commit(force);
    }

    @Override
    public void rollback() {
        getSqlSession().rollback();
    }

    @Override
    public void rollback(boolean force) {
        getSqlSession().rollback(force);
    }

    @Override
    public List<BatchResult> flushStatements() {
        return getSqlSession().flushStatements();
    }

    @Override
    public void close() {
        getSqlSessionTxAdvice().close(true);
    }

    @Override
    public void clearCache() {
        getSqlSession().clearCache();
    }

    @Override
    public Configuration getConfiguration() {
        return getSqlSession().getConfiguration();
    }

    @Override
    public <T> T getMapper(Class<T> type) {
        return getSqlSession().getMapper(type);
    }

    @Override
    public Connection getConnection() {
        return getSqlSession().getConnection();
    }

    @AvoidAdvice
    private SqlSession getSqlSession() {
        SqlSessionTxAdvice sqlSessionTxAdvice = getSqlSessionTxAdvice();
        SqlSession sqlSession = sqlSessionTxAdvice.getSqlSession();
        if (sqlSession == null) {
            if (sqlSessionTxAdvice.isArbitrarilyClosed()) {
                sqlSessionTxAdvice.open();
                sqlSession = sqlSessionTxAdvice.getSqlSession();
            } else {
                throw new IllegalStateException("SqlSession is not opened");
            }
        }
        return sqlSession;
    }

    @AvoidAdvice
    @NonNull
    private SqlSessionTxAdvice getSqlSessionTxAdvice() {
        SqlSessionTxAdvice txAdvice = getAvailableActivity().getAspectAdviceBean(relevantAspectId);
        if (txAdvice == null) {
            if (getActivityContext().getAspectRuleRegistry().getAspectRule(relevantAspectId) == null) {
                throw new IllegalArgumentException("Aspect '" + relevantAspectId +
                        "' handling SqlSessionTxAdvice is undefined");
            }
            throw new IllegalStateException("SqlSessionTxAdvice is not defined");
        }
        return txAdvice;
    }

    @Nullable
    private ActivityData getActivityData() {
        return getAvailableActivity().getActivityData();
    }

}
