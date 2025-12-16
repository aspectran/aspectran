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

    private final String relevantAspectId;

    private String sqlSessionFactoryBeanId;

    private ExecutorType executorType;

    private boolean autoCommit;

    /**
     * Instantiates a new SqlSessionProvider.
     * @param relevantAspectId the ID of the aspect that provides the SqlSessionAdvice
     */
    public SqlSessionProvider(String relevantAspectId) {
        if (relevantAspectId == null) {
            throw new IllegalArgumentException("relevantAspectId must not be null");
        }
        this.relevantAspectId = relevantAspectId;
    }

    /**
     * Sets the bean ID of the {@link SqlSessionFactory} to use for the advice.
     * @param sqlSessionFactoryBeanId the bean ID of the SqlSessionFactory
     */
    public void setSqlSessionFactoryBeanId(String sqlSessionFactoryBeanId) {
        this.sqlSessionFactoryBeanId = sqlSessionFactoryBeanId;
    }

    /**
     * Sets the default {@link ExecutorType} for the advice.
     * @param executorType the executor type
     */
    public void setExecutorType(ExecutorType executorType) {
        this.executorType = executorType;
    }

    /**
     * Sets whether to enable auto-commit for the advice.
     * @param autoCommit true to enable auto-commit, false otherwise
     */
    public void setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    @Override
    public void initialize() {
        if (!getActivityContext().getAspectRuleRegistry().contains(relevantAspectId)) {
            SqlSessionAdviceRegister register = new SqlSessionAdviceRegister(getActivityContext());
            register.setRelevantAspectId(relevantAspectId);
            register.setSqlSessionFactoryBeanId(sqlSessionFactoryBeanId);
            register.setExecutorType(executorType);
            register.setAutoCommit(autoCommit);
            register.setTargetBeanClass(ClassUtils.getUserClass(getClass()));
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
        SqlSessionAdvice sqlSessionAdvice = getAvailableActivity().getAdviceBean(relevantAspectId);
        if (sqlSessionAdvice == null) {
            sqlSessionAdvice = getAvailableActivity().getBeforeAdviceResult(relevantAspectId);
        }
        if (sqlSessionAdvice == null) {
            if (getActivityContext().getAspectRuleRegistry().getAspectRule(relevantAspectId) == null) {
                throw new IllegalArgumentException("Aspect '" + relevantAspectId +
                        "' handling SqlSessionAdvice is not registered");
            }
            throw new IllegalStateException("SqlSessionAdvice not found handled by aspect '" + relevantAspectId + "'");
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
