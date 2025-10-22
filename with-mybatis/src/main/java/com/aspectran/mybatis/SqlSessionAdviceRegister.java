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

import com.aspectran.core.component.bean.NoSuchBeanException;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.rule.AdviceRule;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.IllegalRuleException;
import com.aspectran.core.context.rule.JoinpointRule;
import com.aspectran.core.context.rule.PointcutPatternRule;
import com.aspectran.core.context.rule.PointcutRule;
import com.aspectran.core.context.rule.type.JoinpointTargetType;
import com.aspectran.core.context.rule.type.PointcutType;
import com.aspectran.utils.Assert;
import com.aspectran.utils.ToStringBuilder;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;

/**
 * A helper class that dynamically registers a {@link SqlSessionAdvice} aspect.
 * <p>This class is used internally by {@link SqlSessionProvider} to encapsulate
 * the logic for creating and registering the AOP aspect that manages the
 * lifecycle of a MyBatis {@link org.apache.ibatis.session.SqlSession}.</p>
 *
 * <p>Created: 2025/10/22</p>
 */
class SqlSessionAdviceRegister {

    private final ActivityContext activityContext;

    private String relevantAspectId;

    private String sqlSessionFactoryBeanId;

    private ExecutorType executorType;

    private boolean autoCommit;

    private Class<?> targetBeanClass;

    /**
     * Instantiates a new SqlSessionAdviceRegistrar.
     * @param activityContext the activity context
     */
    SqlSessionAdviceRegister(ActivityContext activityContext) {
        this.activityContext = activityContext;
    }

    /**
     * Sets the ID for the aspect rule to be registered. This ID is used by
     * {@link SqlSessionProvider} to look up the advice bean.
     * @param relevantAspectId the aspect ID
     */
    void setRelevantAspectId(String relevantAspectId) {
        this.relevantAspectId = relevantAspectId;
    }

    /**
     * Sets the bean ID of the {@link SqlSessionFactory} to be used.
     * @param sqlSessionFactoryBeanId the bean ID of the SqlSessionFactory
     */
    void setSqlSessionFactoryBeanId(String sqlSessionFactoryBeanId) {
        this.sqlSessionFactoryBeanId = sqlSessionFactoryBeanId;
    }

    /**
     * Sets the default {@link ExecutorType} for the sessions.
     * @param executorType the executor type
     */
    void setExecutorType(ExecutorType executorType) {
        this.executorType = executorType;
    }

    /**
     * Sets whether to enable auto-commit for the sessions.
     * @param autoCommit true to enable auto-commit, false otherwise
     */
    void setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    /**
     * Sets the target bean class to which the SqlSession advice will be applied.
     * Typically, this is a subclass of {@link SqlSessionProvider}, like {@link SqlSessionAgent}.
     * @param targetBeanClass the target bean class
     */
    void setTargetBeanClass(Class<?> targetBeanClass) {
        this.targetBeanClass = targetBeanClass;
    }

    void register() {
        Assert.notNull(relevantAspectId, "relevantAspectId must not be null");
        Assert.notNull(targetBeanClass, "targetBeanClass must not be null");

        if (activityContext.getAspectRuleRegistry().contains(relevantAspectId)) {
            throw new IllegalStateException("SqlSessionAdvice is already registered with aspect id '" +
                    relevantAspectId + "'");
        }

        SqlSessionFactory sqlSessionFactory;
        try {
            sqlSessionFactory = activityContext.getBeanRegistry().getBean(SqlSessionFactory.class, sqlSessionFactoryBeanId);
        } catch (NoSuchBeanException e) {
            if (sqlSessionFactoryBeanId != null) {
                throw new IllegalStateException("Cannot resolve SqlSessionFactory with bean id '"
                        + sqlSessionFactoryBeanId + "'", e);
            } else {
                throw new IllegalStateException("SqlSessionFactory is not defined", e);
            }
        }

        AspectRule aspectRule = new AspectRule();
        aspectRule.setId(relevantAspectId);
        aspectRule.setOrder(0);

        String pattern = "**@class:" + targetBeanClass.getName();
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
            activityContext.getAspectRuleRegistry().addAspectRule(aspectRule);
        } catch (IllegalRuleException e) {
            ToStringBuilder tsb = new ToStringBuilder("Failed to register SqlSessionAdvice with");
            tsb.append("relevantAspectId", relevantAspectId);
            tsb.append("sqlSessionFactoryBeanId", sqlSessionFactoryBeanId);
            throw new RuntimeException(tsb.toString(), e);
        }
    }

}
