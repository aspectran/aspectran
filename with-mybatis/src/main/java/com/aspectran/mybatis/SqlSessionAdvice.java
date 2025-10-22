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

import com.aspectran.utils.Assert;
import com.aspectran.utils.ObjectUtils;
import com.aspectran.utils.ToStringBuilder;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the lifecycle of a MyBatis {@link SqlSession}, including creation,
 * commit, rollback, and closing. It also handles transactional boundaries.
 *
 * <p>This class is intended to be used as an advice bean within Aspectran's
 * AOP framework to provide declarative transaction management for MyBatis
 * operations.</p>
 *
 * <p>
 * Typical usage pattern:
 * </p>
 * <ul>
 *   <li>open(): lazily create and hold a SqlSession</li>
 *   <li>commit(): commit pending updates if auto-commit is disabled</li>
 *   <li>rollback(): rollback on error if auto-commit is disabled</li>
 *   <li>close(): close the session; optionally mark as arbitrarily closed</li>
 * </ul>
 *
 * @author Juho Jeong
 * @since 2015. 04. 03.
 */
public class SqlSessionAdvice {

    private static final Logger logger = LoggerFactory.getLogger(SqlSessionAdvice.class);

    private final SqlSessionFactory sqlSessionFactory;

    private ExecutorType executorType;

    private boolean autoCommit;

    private SqlSession sqlSession;

    private boolean arbitrarilyClosed;

    /**
     * Instantiates a new SqlSessionAdvice.
     * @param sqlSessionFactory the SqlSessionFactory to create sessions from
     */
    public SqlSessionAdvice(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    /**
     * Returns whether auto-commit is enabled for the session.
     * @return true if auto-commit is enabled, false otherwise
     */
    public boolean isAutoCommit() {
        return autoCommit;
    }

    /**
     * Specifies whether to auto-commit.
     * @param autoCommit true to automatically commit each time updates/deletes/inserts
     *                   is called, false to commit manually
     */
    public void setAutoCommit(boolean autoCommit) {
        Assert.state(sqlSession == null, "Sql Session is already open");
        this.autoCommit = autoCommit;
    }

    /**
     * Returns the executor type for the session.
     * @return the executor type
     */
    public ExecutorType getExecutorType() {
        return executorType;
    }

    /**
     * Sets the mode for using PreparedStatements effectively.
     * @param executorType executor types include SIMPLE, REUSE, and BATCH
     */
    public void setExecutorType(ExecutorType executorType) {
        Assert.state(sqlSession == null, "Sql Session is already open");
        this.executorType = executorType;
    }

    /**
     * Sets the mode for using PreparedStatements effectively.
     * @param executorType executor types include SIMPLE, REUSE, and BATCH, and can be
     *                     specified as a string value
     */
    public void setExecutorType(String executorType) {
        setExecutorType(ExecutorType.valueOf(executorType));
    }

    /**
     * Returns the managed {@link SqlSession} instance.
     * This session is created by the {@code open()} method and its lifecycle
     * (commit, rollback, close) is controlled by this advice.
     * @return the active SqlSession, or {@code null} if the session has not been opened
     */
    public SqlSession getSqlSession() {
        return sqlSession;
    }

    /**
     * Opens a new SqlSession and store its instance inside. Therefore, whenever
     * there is a request for a SqlSessionAdvice bean, a new bean instance of
     * the object must be created.
     */
    public void open() {
        if (sqlSession == null) {
            sqlSession = sqlSessionFactory.openSession(executorType, autoCommit);

            if (logger.isDebugEnabled()) {
                ToStringBuilder tsb = new ToStringBuilder((arbitrarilyClosed ? "Reopen " : "Open ") +
                        ObjectUtils.simpleIdentityToString(sqlSession));
                tsb.append("executorType",
                        (executorType != null ? executorType : sqlSession.getConfiguration().getDefaultExecutorType()));
                tsb.appendForce("autoCommit", autoCommit);
                logger.debug(tsb.toString());
            }

            arbitrarilyClosed = false;
        }
    }

    /**
     * Opens a new SqlSession with the specified auto-commit setting.
     * @param autoCommit true to enable auto-commit, false otherwise
     */
    public void open(boolean autoCommit) {
        setAutoCommit(autoCommit);
        open();
    }

    /**
     * Opens a new SqlSession with the specified executor type.
     * @param executorType the executor type
     */
    public void open(ExecutorType executorType) {
        setExecutorType(executorType);
        open();
    }

    /**
     * Opens a new SqlSession with the specified executor type and auto-commit setting.
     * @param executorType the executor type
     * @param autoCommit true to enable auto-commit, false otherwise
     */
    public void open(ExecutorType executorType, boolean autoCommit) {
        setExecutorType(executorType);
        setAutoCommit(autoCommit);
        open();
    }

    /**
     * Opens a new SqlSession with the specified executor type.
     * @param executorType the executor type name (e.g., "SIMPLE", "REUSE", "BATCH")
     */
    public void open(String executorType) {
        setExecutorType(executorType);
        open();
    }

    /**
     * Opens a new SqlSession with the specified executor type and auto-commit setting.
     * @param executorType the executor type name (e.g., "SIMPLE", "REUSE", "BATCH")
     * @param autoCommit true to enable auto-commit, false otherwise
     */
    public void open(String executorType, boolean autoCommit) {
        setExecutorType(executorType);
        setAutoCommit(autoCommit);
        open();
    }

    /**
     * Flushes batch statements and commits database connection.
     * Note that database connection will not be committed if no updates/deletes/inserts were called.
     * To force the commit, call {@link #commit(boolean)}.
     */
    public void commit() {
        if (checkOpen()) {
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Commit transaction for {}", ObjectUtils.simpleIdentityToString(sqlSession));
        }

        sqlSession.commit();
    }

    /**
     * Flushes batch statements and commits database connection.
     * @param force forces connection commit
     */
    public void commit(boolean force) {
        if (checkOpen()) {
            return;
        }

        if (logger.isDebugEnabled()) {
            ToStringBuilder tsb = new ToStringBuilder("Commit transaction for " +
                    ObjectUtils.simpleIdentityToString(sqlSession));
            tsb.append("force", force);
            logger.debug(tsb.toString());
        }

        sqlSession.commit(force);
    }

    /**
     * Discards pending batch statements and rolls database connection back.
     * Note that database connection will not be rolled back if no updates/deletes/inserts were called.
     * To force the rollback, call {@link #rollback(boolean)}.
     */
    public void rollback() {
        if (checkOpen()) {
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Rollback transaction for {}", ObjectUtils.simpleIdentityToString(sqlSession));
        }

        sqlSession.rollback();
    }

    /**
     * Discards pending batch statements and rolls database connection back.
     * Note that database connection will not be rolled back if no updates/deletes/inserts were called.
     * @param force forces connection rollback
     */
    public void rollback(boolean force) {
        if (checkOpen()) {
            return;
        }

        if (logger.isDebugEnabled()) {
            ToStringBuilder tsb = new ToStringBuilder("Rollback transaction for " +
                    ObjectUtils.simpleIdentityToString(sqlSession));
            tsb.append("force", force);
            logger.debug(tsb.toString());
        }

        sqlSession.rollback(force);
    }

    /**
     * Closes the session.
     */
    public void close() {
        close(false);
    }

    /**
     * Closes the session.
     * @param arbitrarily true if user code arbitrarily closes the session, false otherwise
     */
    public void close(boolean arbitrarily) {
        if (checkOpen()) {
            return;
        }

        arbitrarilyClosed = arbitrarily;
        sqlSession.close();

        if (logger.isDebugEnabled()) {
            logger.debug("Close {}", ObjectUtils.simpleIdentityToString(sqlSession));
        }

        sqlSession = null;
    }

    /**
     * Returns whether the session was logically closed in user code, not by the framework.
     * @return true if user code closed the session, false otherwise
     */
    public boolean isArbitrarilyClosed() {
        return arbitrarilyClosed;
    }

    /**
     * Checks if the SqlSession is open.
     * If user code has already closed the session, it always returns true to ignore further processing.
     * @return true if user code has already closed the session, otherwise false
     */
    private boolean checkOpen() {
        if (arbitrarilyClosed) {
            return true;
        }
        if (sqlSession == null) {
            throw new IllegalStateException("SqlSession is not open");
        }
        return false;
    }

}
