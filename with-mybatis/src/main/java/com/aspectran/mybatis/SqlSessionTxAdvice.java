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

import com.aspectran.utils.Assert;
import com.aspectran.utils.ObjectUtils;
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

/**
 * Advice for SqlSession Transactions.
 *
 * @author Juho Jeong
 * @since 2015. 04. 03.
 */
public class SqlSessionTxAdvice {

    private static final Logger logger = LoggerFactory.getLogger(SqlSessionTxAdvice.class);

    private final SqlSessionFactory sqlSessionFactory;

    private ExecutorType executorType;

    private boolean autoCommit;

    private SqlSession sqlSession;

    private boolean arbitrarilyClosed;

    public SqlSessionTxAdvice(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

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
     * Returns an open SqlSession.
     * If no SqlSession is open then return null.
     * @return a SqlSession instance
     */
    public SqlSession getSqlSession() {
        return sqlSession;
    }

    /**
     * Opens a new SqlSession and store its instance inside. Therefore, whenever
     * there is a request for a SqlSessionTxAdvice bean, a new bean instance of
     * the object must be created.
     */
    public void open() {
        if (sqlSession == null) {
            if (executorType == null) {
                executorType = ExecutorType.SIMPLE;
            }

            sqlSession = sqlSessionFactory.openSession(executorType, autoCommit);

            if (logger.isDebugEnabled()) {
                ToStringBuilder tsb = new ToStringBuilder((arbitrarilyClosed ? "Reopen " : "Open ") +
                        ObjectUtils.simpleIdentityToString(sqlSession));
                tsb.append("executorType", executorType);
                tsb.appendForce("autoCommit", autoCommit);
                logger.debug(tsb.toString());
            }

            arbitrarilyClosed = false;
        }
    }

    public void open(boolean autoCommit) {
        setAutoCommit(autoCommit);
        open();
    }

    public void open(ExecutorType executorType) {
        setExecutorType(executorType);
        open();
    }

    public void open(ExecutorType executorType, boolean autoCommit) {
        setExecutorType(executorType);
        setAutoCommit(autoCommit);
        open();
    }

    public void open(String executorType) {
        setExecutorType(executorType);
        open();
    }

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
        if (checkSession()) {
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Commit transaction for " + ObjectUtils.simpleIdentityToString(sqlSession));
        }

        sqlSession.commit();
    }

    /**
     * Flushes batch statements and commits database connection.
     * @param force forces connection commit
     */
    public void commit(boolean force) {
        if (checkSession()) {
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
        if (checkSession()) {
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Rollback transaction for " + ObjectUtils.simpleIdentityToString(sqlSession));
        }

        sqlSession.rollback();
    }

    /**
     * Discards pending batch statements and rolls database connection back.
     * Note that database connection will not be rolled back if no updates/deletes/inserts were called.
     * @param force forces connection rollback
     */
    public void rollback(boolean force) {
        if (checkSession()) {
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
     * @param arbitrarily true if the session is arbitrarily closed by user code, false otherwise
     */
    public void close(boolean arbitrarily) {
        if (checkSession()) {
            return;
        }

        arbitrarilyClosed = arbitrarily;
        sqlSession.close();

        if (logger.isDebugEnabled()) {
            logger.debug("Close " + ObjectUtils.simpleIdentityToString(sqlSession));
        }

        sqlSession = null;
    }

    /**
     * Returns whether the session was logically closed in user code, not by the framework.
     * @return true if the session was closed by user code, false otherwise
     */
    public boolean isArbitrarilyClosed() {
        return arbitrarilyClosed;
    }

    /**
     * Checks if the SqlSession is open.
     * If the session has already been closed by user code, it always returns true to ignore further processing.
     * @return true if the session has already been closed by user code, otherwise false
     */
    private boolean checkSession() {
        if (arbitrarilyClosed) {
            return true;
        }
        if (sqlSession == null) {
            throw new IllegalStateException("SqlSession is not open");
        }
        return false;
    }

}
