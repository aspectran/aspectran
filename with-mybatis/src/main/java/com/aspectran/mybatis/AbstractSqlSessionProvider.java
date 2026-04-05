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

import com.aspectran.core.activity.Activity;
import com.aspectran.core.component.bean.NoSuchBeanException;
import com.aspectran.core.component.bean.NoUniqueBeanException;
import com.aspectran.utils.ClassUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.TransactionIsolationLevel;

/**
 * Base support class for MyBatis {@link SqlSession} agents.
 *
 * <p>This class manages the configuration for and access to a MyBatis
 * {@link SqlSession} and its corresponding {@link SqlSessionAdvice}
 * registered via Aspectran's AOP framework.</p>
 *
 * <p>Subclasses provide concrete logic for resolving the appropriate
 * transactional context, allowing for single-aspect transactions or
 * multi-aspect routing (e.g., read-write and read-only aspects).</p>
 *
 * <p>Created: 2025-04-23</p>
 */
public abstract class AbstractSqlSessionProvider extends AbstractSqlSessionAgent {

    private String sqlSessionFactoryBeanId;

    private String targetBeanId;

    private ExecutorType executorType;

    private TransactionIsolationLevel isolationLevel;

    private boolean autoCommit;

    private boolean readOnly;

    private boolean readOnlyRollbackOnClose;

    public String getSqlSessionFactoryBeanId() {
        return sqlSessionFactoryBeanId;
    }

    /**
     * Sets the bean ID of the {@link SqlSessionFactory} to use for the advice.
     * @param sqlSessionFactoryBeanId the bean ID of the SqlSessionFactory
     */
    public void setSqlSessionFactoryBeanId(String sqlSessionFactoryBeanId) {
        this.sqlSessionFactoryBeanId = sqlSessionFactoryBeanId;
    }

    /**
     * Returns the ID of the target bean to which the SqlSession advice will be applied.
     * @return the target bean ID
     */
    public String getTargetBeanId() {
        return targetBeanId;
    }

    /**
     * Sets the ID of the target bean to which the SqlSession advice will be applied.
     * @param targetBeanId the target bean ID
     */
    public void setTargetBeanId(String targetBeanId) {
        this.targetBeanId = targetBeanId;
    }

    /**
     * Returns the target bean class to which the SqlSession advice will be applied.
     * @return the target bean class
     */
    public Class<?> getTargetBeanClass() {
        return ClassUtils.getUserClass(getClass());
    }

    public ExecutorType getExecutorType() {
        return executorType;
    }

    /**
     * Sets the default {@link ExecutorType} for the advice.
     * @param executorType the executor type
     */
    public void setExecutorType(ExecutorType executorType) {
        this.executorType = executorType;
    }

    /**
     * Sets the default {@link ExecutorType} for the advice.
     * <p>Supported executor types are:</p>
     * <ul>
     *   <li>{@code SIMPLE} - This executor type does nothing special. It creates a new
     *       PreparedStatement for each execution.</li>
     *   <li>{@code REUSE} - This executor type reuses PreparedStatements.</li>
     *   <li>{@code BATCH} - This executor type batches all update statements and
     *       reuses PreparedStatements. It is suitable for executing a large number
     *       of update statements in a single batch.</li>
     * </ul>
     * @param executorType the executor type name
     */
    public void setExecutorTypeAsString(String executorType) {
        if (executorType != null) {
            setExecutorType(ExecutorType.valueOf(executorType.toUpperCase()));
        }
    }

    public TransactionIsolationLevel getIsolationLevel() {
        return isolationLevel;
    }

    /**
     * Sets the transaction isolation level for the advice.
     * @param isolationLevel the transaction isolation level
     */
    public void setIsolationLevel(TransactionIsolationLevel isolationLevel) {
        this.isolationLevel = isolationLevel;
    }

    /**
     * Sets the transaction isolation level for the advice.
     * <p>Supported isolation levels are:</p>
     * <ul>
     *   <li>{@code NONE} - Transactions are not supported.</li>
     *   <li>{@code READ_UNCOMMITTED} - Dirty reads, non-repeatable reads and phantom reads can occur.
     *       This level allows a transaction to read data that is being modified by another transaction
     *       but has not yet been committed.</li>
     *   <li>{@code READ_COMMITTED} - Dirty reads are prevented; non-repeatable reads and phantom reads can occur.
     *       This level only allows reading data that has been committed.</li>
     *   <li>{@code REPEATABLE_READ} - Dirty reads and non-repeatable reads are prevented; phantom reads can occur.
     *       This level ensures that if a row is read twice within the same transaction, the results are the same.</li>
     *   <li>{@code SERIALIZABLE} - Dirty reads, non-repeatable reads and phantom reads are prevented.
     *       This is the highest isolation level, which ensures that transactions are executed in a way
     *       that is equivalent to serial execution.</li>
     * </ul>
     * @param isolationLevel the isolation level name
     */
    public void setIsolationLevelAsString(String isolationLevel) {
        if (isolationLevel != null) {
            setIsolationLevel(TransactionIsolationLevel.valueOf(isolationLevel.toUpperCase()));
        }
    }

    public boolean isAutoCommit() {
        return autoCommit;
    }

    /**
     * Sets whether to enable auto-commit for the advice.
     * @param autoCommit true to enable auto-commit, false otherwise
     */
    public void setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * Sets whether to enable read-only mode for the advice.
     * @param readOnly true to enable read-only mode, false otherwise
     */
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public boolean isReadOnlyRollbackOnClose() {
        return readOnlyRollbackOnClose;
    }

    /**
     * Sets whether to force a rollback when closing a read-only session.
     * @param readOnlyRollbackOnClose true to force rollback on close, false otherwise
     */
    public void setReadOnlyRollbackOnClose(boolean readOnlyRollbackOnClose) {
        this.readOnlyRollbackOnClose = readOnlyRollbackOnClose;
    }

    /**
     * Returns the {@link SqlSessionFactory} associated with this provider.
     * @return the SqlSessionFactory
     */
    @Override
    public SqlSessionFactory getSqlSessionFactory() {
        try {
            return getBeanRegistry().getBean(SqlSessionFactory.class, sqlSessionFactoryBeanId);
        } catch (NoSuchBeanException e) {
            StringBuilder msg = new StringBuilder();
            msg.append("Failed to resolve SqlSessionFactory ");
            if (sqlSessionFactoryBeanId != null) {
                msg.append(" with bean id '").append(sqlSessionFactoryBeanId).append("'");
            } else {
                msg.append("; No SqlSessionFactory bean found");
            }
            throw new IllegalStateException(msg.toString(), e);
        } catch (NoUniqueBeanException e) {
            String msg = "Failed to resolve SqlSessionFactory" +
                    "; No unique SqlSessionFactory bean found. If multiple SqlSessionFactory beans are defined, " +
                    "please specify a sqlSessionFactoryBeanId";
            throw new IllegalStateException(msg, e);
        }
    }

    /**
     * Returns the current SqlSession bound to this advisor/context.
     * @return the active SqlSession
     */
    @Override
    public SqlSession getSqlSession() {
        return getSqlSessionAdvice().getSqlSession();
    }

    /**
     * Ensures that the provider is operating within a transactional context.
     * @throws IllegalStateException if called during a non-transactional proxy-mode activity
     */
    protected void checkTransactional(Activity.Mode activityMode) {
        if (activityMode == Activity.Mode.PROXY) {
            throw new IllegalStateException("Cannot be executed on a non-transactional activity;" +
                    " needs to be wrapped in an instant activity.");
        }
    }

}
