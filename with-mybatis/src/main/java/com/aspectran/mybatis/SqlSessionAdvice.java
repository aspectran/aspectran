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
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

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

    private TransactionIsolationLevel isolationLevel;

    private boolean autoCommit;

    private boolean readOnly;

    private boolean readOnlyRollbackOnClose;

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
        ensureNotOpen();
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
        ensureNotOpen();
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
     * Returns the transaction isolation level for the session.
     * @return the transaction isolation level
     */
    public TransactionIsolationLevel getIsolationLevel() {
        return isolationLevel;
    }

    /**
     * Sets the transaction isolation level for the session.
     * @param isolationLevel the transaction isolation level
     */
    public void setIsolationLevel(TransactionIsolationLevel isolationLevel) {
        ensureNotOpen();
        this.isolationLevel = isolationLevel;
    }

    /**
     * Sets the transaction isolation level for the session.
     * @param isolationLevel the transaction isolation level name (e.g., "READ_COMMITTED")
     */
    public void setIsolationLevel(String isolationLevel) {
        setIsolationLevel(TransactionIsolationLevel.valueOf(isolationLevel));
    }

    /**
     * Returns whether the session is in read-only mode.
     * @return true if read-only mode is enabled, false otherwise
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * Sets whether the session is in read-only mode.
     * @param readOnly true to enable read-only mode, false otherwise
     */
    public void setReadOnly(boolean readOnly) {
        ensureNotOpen();
        this.readOnly = readOnly;
    }

    /**
     * Sets whether to force a rollback when closing a read-only session.
     * @param readOnlyRollbackOnClose true to force rollback on close, false otherwise
     */
    public void setReadOnlyRollbackOnClose(boolean readOnlyRollbackOnClose) {
        ensureNotOpen();
        this.readOnlyRollbackOnClose = readOnlyRollbackOnClose;
    }

    /**
     * Returns the managed {@link SqlSession} instance.
     * This session is created by the {@code open()} method and its lifecycle
     * (commit, rollback, close) is controlled by this advice.
     * @return the active SqlSession
     */
    public SqlSession getSqlSession() {
        if (sqlSession == null) {
            if (arbitrarilyClosed) {
                throw new IllegalStateException("SqlSession has been arbitrarily closed and cannot be reopened lazily");
            }
            doOpen();
        }
        return sqlSession;
    }

    /**
     * Returns whether the managed {@link SqlSession} is currently open.
     * @return true if the session is open, false otherwise
     */
    public boolean isOpen() {
        return (sqlSession != null);
    }

    /**
     * Opens a new SqlSession and store its instance inside. Therefore, whenever
     * there is a request for a SqlSessionAdvice bean, a new bean instance of
     * the object must be created.
     */
    public void open() {
        doOpen();
    }

    /**
     * Internal method to open a new SqlSession if not already open.
     */
    private void doOpen() {
        if (sqlSession == null) {
            boolean autoCommitToUse = (!readOnly && autoCommit);
            if (isolationLevel != null) {
                sqlSession = sqlSessionFactory.openSession(executorType, isolationLevel);
            } else {
                sqlSession = sqlSessionFactory.openSession(executorType, autoCommitToUse);
            }

            if (readOnly) {
                try {
                    sqlSession.getConnection().setAutoCommit(false);
                    sqlSession.getConnection().setReadOnly(true);
                } catch (SQLException e) {
                    logger.warn("Failed to set database connection to read-only", e);
                }
            }

            if (logger.isDebugEnabled()) {
                ToStringBuilder tsb = new ToStringBuilder((arbitrarilyClosed ? "Reopen " : "Open ") +
                        ObjectUtils.simpleIdentityToString(sqlSession));
                tsb.append("executorType",
                        (executorType != null ? executorType : sqlSession.getConfiguration().getDefaultExecutorType()));
                if (isolationLevel != null) {
                    tsb.append("isolationLevel", isolationLevel);
                } else {
                    tsb.appendForce("autoCommit", autoCommitToUse);
                }
                tsb.appendForce("readOnly", readOnly);
                if (readOnly) {
                    tsb.appendForce("rollbackOnClose", readOnlyRollbackOnClose);
                }
                logger.debug(tsb.toString());
            }

            arbitrarilyClosed = false;
        }
    }

    /**
     * Flushes batch statements and commits database connection.
     * Note that database connection will not be committed if no updates/deletes/inserts were called.
     * To force the commit, call {@link #commit(boolean)}.
     */
    public void commit() {
        if (readOnly || isSessionUnavailable()) {
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
        if (readOnly || isSessionUnavailable()) {
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
        if (isSessionUnavailable()) {
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
        if (isSessionUnavailable()) {
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
        if (isSessionUnavailable()) {
            return;
        }

        if (readOnly) {
            if (readOnlyRollbackOnClose) {
                rollback(true);
            }
            try {
                // Reset read-only state before returning connection to the pool
                sqlSession.getConnection().setReadOnly(false);
            } catch (SQLException e) {
                // Ignore
            }
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
     * Checks if the SqlSession is unavailable for operations.
     * @return true if the session is not open or has been arbitrarily closed, false otherwise
     */
    private boolean isSessionUnavailable() {
        return (sqlSession == null || arbitrarilyClosed);
    }

    /**
     * Ensures that the SqlSession is not already open.
     * @throws IllegalStateException if the session is already open
     */
    private void ensureNotOpen() {
        Assert.state(sqlSession == null, "Sql Session is already open");
    }

}
