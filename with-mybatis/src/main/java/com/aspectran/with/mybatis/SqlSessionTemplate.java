/*
 * Copyright (c) 2008-2018 The Aspectran Project
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
package com.aspectran.with.mybatis;

import com.aspectran.core.component.bean.annotation.AvoidAdvice;
import com.aspectran.core.component.bean.aware.ActivityContextAware;
import com.aspectran.core.context.ActivityContext;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

public class SqlSessionTemplate implements SqlSession, ActivityContextAware {

    private final String relevantAspectId;

    private ActivityContext context;

    public SqlSessionTemplate(String relevantAspectId) {
        if (relevantAspectId == null) {
            throw new IllegalArgumentException("relevantAspectId can not be null");
        }
        this.relevantAspectId = relevantAspectId;
    }

    @Override
    public <T> T selectOne(String statement) {
        return getSqlSession().selectOne(statement);
    }

    @Override
    public <T> T selectOne(String statement, Object parameter) {
        return getSqlSession().selectOne(statement, parameter);
    }

    @Override
    public <E> List<E> selectList(String statement) {
        return getSqlSession().selectList(statement);
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
        return getSqlSession().selectMap(statement, mapKey);
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
        return getSqlSession().selectCursor(statement);
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
        getSqlSession().select(statement, handler);
    }

    @Override
    public void select(String statement, Object parameter, RowBounds rowBounds, ResultHandler handler) {
        getSqlSession().select(statement, parameter, rowBounds, handler);
    }

    @Override
    public int insert(String statement) {
        return getSqlSession().insert(statement);
    }

    @Override
    public int insert(String statement, Object parameter) {
        return getSqlSession().insert(statement, parameter);
    }

    @Override
    public int update(String statement) {
        return getSqlSession().update(statement);
    }

    @Override
    public int update(String statement, Object parameter) {
        return getSqlSession().update(statement, parameter);
    }

    @Override
    public int delete(String statement) {
        return getSqlSession().delete(statement);
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
        getSqlSession().close();
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
        SqlSession sqlSession = getSqlSessionTxAdvice().getSqlSession();
        if (sqlSession == null) {
            throw new IllegalArgumentException("SqlSession is not opened");
        }
        return sqlSession;
    }

    @AvoidAdvice
    private SqlSessionTxAdvice getSqlSessionTxAdvice() {
        if (context == null) {
            throw new IllegalArgumentException("ActivityContext is not injected");
        }
        SqlSessionTxAdvice advice = context.getCurrentActivity().getAspectAdviceBean(relevantAspectId);
        if (advice == null) {
            if (context.getAspectRuleRegistry().getAspectRule(relevantAspectId) == null) {
                throw new IllegalArgumentException("Aspect '" + relevantAspectId +
                        "' handling SqlSessionTxAdvice is undefined");
            }
            throw new IllegalArgumentException("SqlSessionTxAdvice is not defined");
        }
        return advice;
    }

    @Override
    @AvoidAdvice
    public void setActivityContext(ActivityContext context) {
        this.context = context;
    }

}
