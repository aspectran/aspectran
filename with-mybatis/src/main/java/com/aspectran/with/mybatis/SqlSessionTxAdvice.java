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
package com.aspectran.with.mybatis;

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
        sqlSession.commit();
    }

    /**
     * Flushes batch statements and commits database connection.
     *
     * @param force forces connection commit
     */
    public void commit(boolean force) {
        checkSession();
        sqlSession.commit(force);
    }

    /**
     * Discards pending batch statements and rolls database connection back.
     * Note that database connection will not be rolled back if no updates/deletes/inserts were called.
     * To force the rollback call {@link #rollback(boolean)}
     */
    public void rollback() {
        checkSession();
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
        sqlSession.rollback(force);
    }

    /**
     * Closes the session.
     */
    public void close() {
        checkSession();
        sqlSession.close();
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
