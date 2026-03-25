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
import com.aspectran.core.activity.InstantActivitySupport;
import com.aspectran.core.component.bean.ablility.InitializableBean;
import com.aspectran.utils.ClassUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.jspecify.annotations.NonNull;

/**
 * Base support class that locates and manages access to a MyBatis {@link SqlSession}
 * and the corresponding {@link SqlSessionAdvice} registered via Aspectran AOP.
 * <p>
 * Subclasses can call {@link #getSqlSession()} to obtain a context-bound
 * SqlSession and rely on {@link #getSqlSessionAdvice()} for transactional
 * control (begin/commit/close) as configured by the registered aspect.
 * </p>
 *
 * <p>Created: 2025-04-23</p>
 */
public abstract class SqlSessionProvider extends InstantActivitySupport implements InitializableBean {

    private static final String[] DEFAULT_READONLY_METHOD_PATTERNS = { "select*" };

    private final String txAspectId;

    private String readOnlyAspectId;

    private String sqlSessionFactoryBeanId;

    private String targetBeanId;

    private ExecutorType executorType;

    private TransactionIsolationLevel isolationLevel;

    private boolean autoCommit;

    private boolean readOnly;

    /**
     * Instantiates a new SqlSessionProvider.
     * @param txAspectId the ID of the aspect that provides the SqlSessionAdvice
     */
    public SqlSessionProvider(String txAspectId) {
        if (txAspectId == null) {
            throw new IllegalArgumentException("txAspectId must not be null");
        }
        this.txAspectId = txAspectId;
    }

    /**
     * Sets the ID for the read-only aspect rule.
     * @param readOnlyAspectId the read-only aspect ID
     */
    public void setReadOnlyAspectId(String readOnlyAspectId) {
        this.readOnlyAspectId = readOnlyAspectId;
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
    protected String getTargetBeanId() {
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

    /**
     * Sets whether to enable auto-commit for the advice.
     * @param autoCommit true to enable auto-commit, false otherwise
     */
    public void setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    /**
     * Sets whether to enable read-only mode for the advice.
     * @param readOnly true to enable read-only mode, false otherwise
     */
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    /**
     * Initializes the provider. If the aspect specified by {@code txAspectId}
     * is not already registered in the aspect rule registry, this method
     * automatically creates and registers a new {@link SqlSessionAdvice} aspect
     * using the current configuration.
     */
    @Override
    public void initialize() {
        if (!getActivityContext().getAspectRuleRegistry().contains(txAspectId)) {
            SqlSessionAdviceRegister register = new SqlSessionAdviceRegister(getActivityContext());
            register.setTxAspectId(txAspectId);
            register.setSqlSessionFactoryBeanId(sqlSessionFactoryBeanId);
            register.setTargetBeanId(targetBeanId);
            register.setTargetBeanClass(ClassUtils.getUserClass(getClass()));
            if (readOnlyAspectId != null) {
                register.setExcludeMethodNamePatterns(DEFAULT_READONLY_METHOD_PATTERNS);
            }
            register.setExecutorType(executorType);
            register.setIsolationLevel(isolationLevel);
            register.setAutoCommit(autoCommit);
            register.setReadOnly(readOnly);
            register.register();
        }
        if (readOnlyAspectId != null && !getActivityContext().getAspectRuleRegistry().contains(readOnlyAspectId)) {
            SqlSessionAdviceRegister register = new SqlSessionAdviceRegister(getActivityContext());
            register.setTxAspectId(readOnlyAspectId);
            register.setSqlSessionFactoryBeanId(sqlSessionFactoryBeanId);
            register.setTargetBeanId(targetBeanId);
            register.setTargetBeanClass(ClassUtils.getUserClass(getClass()));
            register.setIncludeMethodNamePatterns(DEFAULT_READONLY_METHOD_PATTERNS);
            register.setExecutorType(executorType);
            register.setIsolationLevel(isolationLevel);
            register.setAutoCommit(autoCommit);
            register.setReadOnly(true);
            register.register();
        }
    }

    /**
     * Returns the current SqlSession bound to this advisor/context, opening one
     * if necessary when previously marked as arbitrarily closed.
     * @return the active SqlSession
     * @throws IllegalStateException if a SqlSession has not been opened
     */
    protected SqlSession getSqlSession() {
        SqlSessionAdvice sqlSessionAdvice = getSqlSessionAdvice();
        SqlSession sqlSession = sqlSessionAdvice.getSqlSession();
        if (sqlSession == null) {
            if (sqlSessionAdvice.isArbitrarilyClosed()) {
                sqlSessionAdvice.open();
                sqlSession = sqlSessionAdvice.getSqlSession();
            } else {
                throw new IllegalStateException("SqlSession is not opened");
            }
        }
        return sqlSession;
    }

    @NonNull
    protected SqlSessionAdvice getSqlSessionAdvice() {
        checkTransactional();
        Activity currentActivity = getAvailableActivity();
        SqlSessionAdvice sqlSessionAdvice = null;
        if (readOnlyAspectId != null) {
            sqlSessionAdvice = currentActivity.getAdviceBean(readOnlyAspectId);
            if (sqlSessionAdvice == null) {
                sqlSessionAdvice = currentActivity.getBeforeAdviceResult(readOnlyAspectId);
            }
        }
        if (sqlSessionAdvice == null) {
            sqlSessionAdvice = currentActivity.getAdviceBean(txAspectId);
            if (sqlSessionAdvice == null) {
                sqlSessionAdvice = currentActivity.getBeforeAdviceResult(txAspectId);
            }
        }
        if (sqlSessionAdvice == null) {
            if (getActivityContext().getAspectRuleRegistry().getAspectRule(txAspectId) == null) {
                throw new IllegalArgumentException("Aspect '" + txAspectId +
                        "' handling SqlSessionAdvice is not registered");
            }
            throw new IllegalStateException("SqlSessionAdvice not found handled by aspect '" + txAspectId + "'");
        }
        return sqlSessionAdvice;
    }

    private void checkTransactional() {
        if (getAvailableActivity().getMode() == Activity.Mode.PROXY) {
            throw new IllegalStateException("Cannot be executed on a non-transactional activity;" +
                    " needs to be wrapped in an instant activity.");
        }
    }

}
