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

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.InstantActivitySupport;
import com.aspectran.core.component.bean.NoSuchBeanException;
import com.aspectran.core.component.bean.ablility.InitializableBean;
import com.aspectran.core.context.rule.AdviceRule;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.IllegalRuleException;
import com.aspectran.core.context.rule.JoinpointRule;
import com.aspectran.core.context.rule.PointcutPatternRule;
import com.aspectran.core.context.rule.PointcutRule;
import com.aspectran.core.context.rule.type.JoinpointTargetType;
import com.aspectran.core.context.rule.type.PointcutType;
import com.aspectran.utils.ClassUtils;
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.annotation.jsr305.NonNull;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

/**
 * <p>Created: 2025-04-23</p>
 */
public abstract class SqlSessionProvider extends InstantActivitySupport implements InitializableBean {

    private final String relevantAspectId;

    private String sqlSessionFactoryBeanId;

    private ExecutorType executorType;

    private boolean autoCommit;

    public SqlSessionProvider(String relevantAspectId) {
        if (relevantAspectId == null) {
            throw new IllegalArgumentException("relevantAspectId must not be null");
        }
        this.relevantAspectId = relevantAspectId;
    }

    public void setSqlSessionFactoryBeanId(String sqlSessionFactoryBeanId) {
        this.sqlSessionFactoryBeanId = sqlSessionFactoryBeanId;
    }

    public void setExecutorType(ExecutorType executorType) {
        this.executorType = executorType;
    }

    public void setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

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

    @Override
    public void initialize() {
        if (!getActivityContext().getAspectRuleRegistry().contains(relevantAspectId)) {
            registerSqlSessionAdvice();
        }
    }

    protected void registerSqlSessionAdvice() {
        if (getActivityContext().getAspectRuleRegistry().contains(relevantAspectId)) {
            throw new IllegalStateException("SqlSessionAdvice is already registered");
        }

        SqlSessionFactory sqlSessionFactory;
        try {
            sqlSessionFactory = getBeanRegistry().getBean(SqlSessionFactory.class, sqlSessionFactoryBeanId);
        } catch (NoSuchBeanException e) {
            if (sqlSessionFactoryBeanId != null) {
                throw new IllegalStateException("Cannot resolve SqlSessionFactory with id=" + sqlSessionFactoryBeanId, e);
            } else {
                throw new IllegalStateException("SqlSessionFactory is not defined", e);
            }
        }

        AspectRule aspectRule = new AspectRule();
        aspectRule.setId(relevantAspectId);
        aspectRule.setOrder(0);

        String pattern = "**@class:" + ClassUtils.getUserClass(getClass()).getName();
        PointcutPatternRule pointcutPatternRule = PointcutPatternRule.newInstance(pattern);

        PointcutRule pointcutRule = new PointcutRule(PointcutType.WILDCARD);
        pointcutRule.addPointcutPatternRule(pointcutPatternRule);

        JoinpointRule joinpointRule = new JoinpointRule();
        joinpointRule.setJoinpointTargetType(JoinpointTargetType.ACTIVITY);
        joinpointRule.setPointcutRule(pointcutRule);

        aspectRule.setJoinpointRule(joinpointRule);

        AdviceRule beforeAdviceRule = aspectRule.newBeforeAdviceRule();
        beforeAdviceRule.setAdviceAction(activity -> {
            SqlSessionAdvice sqlSessionAdvice = new SqlSessionAdvice(sqlSessionFactory);
            if (executorType != null) {
                sqlSessionAdvice.setExecutorType(executorType);
            }
            sqlSessionAdvice.setAutoCommit(autoCommit);
            sqlSessionAdvice.open();
            return sqlSessionAdvice;
        });

        AdviceRule afterAdviceRule = aspectRule.newAfterAdviceRule();
        afterAdviceRule.setAdviceAction(activity -> {
            SqlSessionAdvice sqlSessionAdvice = activity.getBeforeAdviceResult(relevantAspectId);
            sqlSessionAdvice.commit();
            return null;
        });

        AdviceRule finallyAdviceRule = aspectRule.newFinallyAdviceRule();
        finallyAdviceRule.setAdviceAction(activity -> {
            SqlSessionAdvice sqlSessionAdvice = activity.getBeforeAdviceResult(relevantAspectId);
            sqlSessionAdvice.close();
            return null;
        });

        try {
            getActivityContext().getAspectRuleRegistry().addAspectRule(aspectRule);
        } catch (IllegalRuleException e) {
            ToStringBuilder tsb = new ToStringBuilder("Failed to register SqlSessionAdvice with");
            tsb.append("relevantAspectId", relevantAspectId);
            tsb.append("sqlSessionFactoryBeanId", sqlSessionFactoryBeanId);
            throw new RuntimeException(tsb.toString(), e);
        }
    }

}
