/*
 * Copyright (c) 2008-2019 The Aspectran Project
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

import com.aspectran.core.util.ToStringBuilder;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
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

    private static final Log log = LogFactory.getLog(SqlSessionTxAdvice.class);

    private final SqlSessionFactory sqlSessionFactory;

    private ExecutorType executorType;

    private boolean autoCommit;

    private SqlSession sqlSession;

    public SqlSessionTxAdvice(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    public void setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    public void setExecutorType(String executorType) {
        this.executorType = ExecutorType.valueOf(executorType);
    }

    /**
     * Returns an open SqlSession.
     * If no SqlSession is open then return null.
     *
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
        if(sqlSession == null) {
            if (executorType == null) {
                executorType = ExecutorType.SIMPLE;
            }
            sqlSession = sqlSessionFactory.openSession(executorType, autoCommit);

            if (log.isDebugEnabled()) {
                ToStringBuilder tsb = new ToStringBuilder(String.format("Created %s@%x",
                        sqlSession.getClass().getSimpleName(), sqlSession.hashCode()));
                tsb.append("executorType", executorType);
                tsb.append("autoCommit", autoCommit);
                log.debug(tsb.toString());
            }
        }
    }

    public void open(boolean autoCommit) {
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
     * To force the commit call {@link #commit(boolean)}
     */
    public void commit() {
        checkSession();

        if (log.isDebugEnabled()) {
            log.debug(String.format("Committing transactional %s@%x",
                    sqlSession.getClass().getSimpleName(),
                    sqlSession.hashCode()));
        }

        sqlSession.commit();
    }

    /**
     * Flushes batch statements and commits database connection.
     *
     * @param force forces connection commit
     */
    public void commit(boolean force) {
        checkSession();

        if (log.isDebugEnabled()) {
            ToStringBuilder tsb = new ToStringBuilder(String.format("Committing transactional %s@%x",
                    sqlSession.getClass().getSimpleName(),sqlSession.hashCode()));
            tsb.append("force", force);
            log.debug(tsb.toString());
        }

        sqlSession.commit(force);
    }

    /**
     * Discards pending batch statements and rolls database connection back.
     * Note that database connection will not be rolled back if no updates/deletes/inserts were called.
     * To force the rollback call {@link #rollback(boolean)}
     */
    public void rollback() {
        checkSession();

        if (log.isDebugEnabled()) {
            log.debug(String.format("Rolling back transactional %s@%x",
                    sqlSession.getClass().getSimpleName(),
                    sqlSession.hashCode()));
        }

        sqlSession.rollback();
    }

    /**
     * Discards pending batch statements and rolls database connection back.
     * Note that database connection will not be rolled back if no updates/deletes/inserts were called.
     *
     * @param force forces connection rollback
     */
    public void rollback(boolean force) {
        checkSession();

        if (log.isDebugEnabled()) {
            ToStringBuilder tsb = new ToStringBuilder(String.format("Rolling back transactional %s@%x",
                    sqlSession.getClass().getSimpleName(),sqlSession.hashCode()));
            tsb.append("force", force);
            log.debug(tsb.toString());
        }

        sqlSession.rollback(force);
    }

    /**
     * Closes the session.
     */
    public void close() {
        checkSession();
        sqlSession.close();

        if (log.isDebugEnabled()) {
            log.debug(String.format("Closed %s@%x",
                    sqlSession.getClass().getSimpleName(),
                    sqlSession.hashCode()));
        }

        sqlSession = null;
    }

    /**
     * Checks if the SqlSession is open.
     */
    private void checkSession() {
        if(sqlSession == null) {
            throw new IllegalStateException("SqlSession is not open");
        }
    }

}