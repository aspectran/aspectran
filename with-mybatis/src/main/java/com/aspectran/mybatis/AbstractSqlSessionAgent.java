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
import com.aspectran.core.activity.HintParameters;
import com.aspectran.core.activity.InstantActivitySupport;
import com.aspectran.core.component.bean.annotation.Advisable;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.jspecify.annotations.Nullable;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Base class for {@link SqlSession} agents that delegate calls to a context-bound
 * SqlSession while applying Aspectran AOP semantics.
 *
 * <p>This class implements the {@link SqlSession} interface, acting as a facade
 * that forwards calls to the underlying session obtained via {@code getSqlSession()}.
 * By applying the {@link Advisable} annotation to its methods, it allows
 * Aspectran's AOP framework to intercept and manage transactional boundaries.</p>
 *
 * <p>Additionally, it supports {@code autoParameters}, which automatically
 * injects {@link ActivityData} as a parameter when no other parameter is provided
 * to statement methods.</p>
 */
public abstract class AbstractSqlSessionAgent extends InstantActivitySupport implements SqlSession, SqlSessionProvider {

    private boolean autoParameters;

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

    @Advisable
    @Override
    public <T> T selectOne(String statement) {
        return invoke(() -> getSqlSession().selectOne(statement),
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
        return invoke(() -> getSqlSession().selectList(statement),
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
        return invoke(() -> getSqlSession().selectMap(statement, mapKey),
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
        return invoke(() -> getSqlSession().selectCursor(statement),
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
        invoke(() -> {
            getSqlSession().select(statement, handler);
            return null;
        }, p -> {
            getSqlSession().select(statement, p, handler);
            return null;
        });
    }

    @Advisable
    @Override
    public void select(String statement, Object parameter, RowBounds rowBounds, ResultHandler handler) {
        getSqlSession().select(statement, parameter, rowBounds, handler);
    }

    @Advisable
    @Override
    public int insert(String statement) {
        assertNotReadOnly();
        return invoke(() -> getSqlSession().insert(statement),
                p -> getSqlSession().insert(statement, p));
    }

    @Advisable
    @Override
    public int insert(String statement, Object parameter) {
        assertNotReadOnly();
        return getSqlSession().insert(statement, parameter);
    }

    @Advisable
    @Override
    public int update(String statement) {
        assertNotReadOnly();
        return invoke(() -> getSqlSession().update(statement),
                p -> getSqlSession().update(statement, p));
    }

    @Advisable
    @Override
    public int update(String statement, Object parameter) {
        assertNotReadOnly();
        return getSqlSession().update(statement, parameter);
    }

    @Advisable
    @Override
    public int delete(String statement) {
        assertNotReadOnly();
        return invoke(() -> getSqlSession().delete(statement),
                p -> getSqlSession().delete(statement, p));
    }

    @Advisable
    @Override
    public int delete(String statement, Object parameter) {
        assertNotReadOnly();
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
        return getSqlSessionFactory().getConfiguration();
    }

    @Advisable
    @Override
    public <T> T getMapper(Class<T> type) {
        return getConfiguration().getMapper(type, this);
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

    /**
     * Invokes one of the two provided functions based on the 'autoParameters' flag.
     * @param action function to invoke when auto-parameters are disabled
     * @param autoAction function to invoke when auto-parameters are enabled
     * @param <R> the return type
     * @return the result of the invoked function
     */
    private <R> R invoke(Supplier<R> action, Function<Object, R> autoAction) {
        if (autoParameters) {
            return autoAction.apply(getActivityData());
        } else {
            return action.get();
        }
    }


    /**
     * Asserts that the current transactional context is not read-only.
     * If a {@code @Hint(type = "transactional", value = "readOnly: true")} is present,
     * throws an {@link IllegalStateException} to prevent data modification operations
     * from executing within a read-only context.
     * @throws IllegalStateException if the context is read-only
     */
    private void assertNotReadOnly() {
        HintParameters hint = getAvailableActivity().peekHint("transactional");
        if (hint != null && hint.getBoolean("readOnly", false)) {
            throw new IllegalStateException("Data modification operations (insert, update, delete) " +
                    "are not allowed within a read-only transactional hint.");
        }
    }

}
