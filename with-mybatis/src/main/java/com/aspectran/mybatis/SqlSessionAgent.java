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
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * An {@link SqlSession} facade that delegates to a context-bound SqlSession
 * while applying {@link Advisable} semantics. It forwards calls to the
 * underlying session obtained via {@link SqlSessionProvider#getSqlSession()} and
 * optionally injects {@link ActivityData} as parameters when autoParameters is enabled.
 */
public class SqlSessionAgent extends SqlSessionProvider implements SqlSession {

    private boolean autoParameters;

    /**
     * Instantiates a new SqlSessionAgent.
     * @param relevantAspectId the ID of the aspect that provides the SqlSessionAdvice
     */
    public SqlSessionAgent(String relevantAspectId) {
        super(relevantAspectId);
    }

    /**
     * Sets whether to automatically pass the current {@link ActivityData} as a parameter
     * to statement methods when no other parameter is provided.
     * <p>For example, if enabled, a call to {@code selectOne("some.statement")} will
     * effectively be executed as {@code selectOne("some.statement", activity.getActivityData())}.
     * The default is {@code false}.</p>
     * @param autoParameters true to enable auto-parameter injection, false otherwise
     */
    public void setAutoParameters(boolean autoParameters) {
        this.autoParameters = autoParameters;
    }

    /**
     * Executes one of the two provided functions based on the 'autoParameters' flag.
     * @param withoutAutoParams function to execute when auto-parameters are disabled
     * @param withAutoParams function to execute when auto-parameters are enabled
     * @param <R> the return type
     * @return the result of the executed function
     */
    private <R> R execute(Supplier<R> withoutAutoParams, Function<Object, R> withAutoParams) {
        if (autoParameters) {
            return withAutoParams.apply(getActivityData());
        } else {
            return withoutAutoParams.get();
        }
    }

    @Advisable
    @Override
    public <T> T selectOne(String statement) {
        return execute(() -> getSqlSession().selectOne(statement),
                p -> getSqlSession().selectOne(statement, p));
    }

    @Advisable
    @Override
    public <T> T selectOne(String statement, Object parameter) {
        return getSqlSession().selectOne(statement, parameter);
    }

    @Advisable
    @Override
    public <E> List<E> selectList(String statement) {
        return execute(() -> getSqlSession().selectList(statement),
                p -> getSqlSession().selectList(statement, p));
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
        return execute(() -> getSqlSession().selectMap(statement, mapKey),
                p -> getSqlSession().selectMap(statement, p, mapKey));
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
        return execute(() -> getSqlSession().selectCursor(statement),
                p -> getSqlSession().selectCursor(statement, p));
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
        return execute(() -> getSqlSession().insert(statement),
                p -> getSqlSession().insert(statement, p));
    }

    @Advisable
    @Override
    public int insert(String statement, Object parameter) {
        return getSqlSession().insert(statement, parameter);
    }

    @Advisable
    @Override
    public int update(String statement) {
        return execute(() -> getSqlSession().update(statement),
                p -> getSqlSession().update(statement, p));
    }

    @Advisable
    @Override
    public int update(String statement, Object parameter) {
        return getSqlSession().update(statement, parameter);
    }

    @Advisable
    @Override
    public int delete(String statement) {
        return execute(() -> getSqlSession().delete(statement),
                p -> getSqlSession().delete(statement, p));
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
