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

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.rule.AdviceRule;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.IllegalRuleException;
import com.aspectran.core.context.rule.JoinpointRule;
import com.aspectran.core.context.rule.PointcutPatternRule;
import com.aspectran.core.context.rule.PointcutRule;
import com.aspectran.core.context.rule.type.JoinpointTargetType;
import com.aspectran.core.context.rule.type.PointcutType;
import com.aspectran.core.component.bean.NoSuchBeanException;
import com.aspectran.core.component.bean.NoUniqueBeanException;
import com.aspectran.utils.Assert;
import com.aspectran.utils.ToStringBuilder;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * A helper class that dynamically registers a {@link SqlSessionAdvice} aspect.
 * <p>This class is used internally by {@link SqlSessionProvider} to encapsulate
 * the logic for creating and registering the AOP aspect that manages the
 * lifecycle of a MyBatis {@link org.apache.ibatis.session.SqlSession}.</p>
 *
 * <p>Created: 2025/10/22</p>
 */
class SqlSessionAdviceRegister {

    private static final Logger logger = LoggerFactory.getLogger(SqlSessionAdviceRegister.class);

    private final ActivityContext activityContext;

    private String txAspectId;

    private String sqlSessionFactoryBeanId;

    private String targetBeanId;

    private Class<?> targetBeanClass;

    private String[] includeMethodNamePatterns;

    private String[] excludeMethodNamePatterns;

    private ExecutorType executorType;

    private TransactionIsolationLevel isolationLevel;

    private boolean autoCommit;

    private boolean readOnly;

    private boolean readOnlyRollbackOnClose;

    /**
     * Instantiates a new SqlSessionAdviceRegister.
     * @param activityContext the activity context
     */
    SqlSessionAdviceRegister(ActivityContext activityContext) {
        this.activityContext = activityContext;
    }

    /**
     * Sets the ID for the aspect rule to be registered. This ID is used by
     * {@link SqlSessionProvider} to look up the advice bean.
     * @param txAspectId the aspect ID
     */
    void setTxAspectId(String txAspectId) {
        this.txAspectId = txAspectId;
    }

    /**
     * Sets the bean ID of the {@link SqlSessionFactory} to be used.
     * @param sqlSessionFactoryBeanId the bean ID of the SqlSessionFactory
     */
    void setSqlSessionFactoryBeanId(String sqlSessionFactoryBeanId) {
        this.sqlSessionFactoryBeanId = sqlSessionFactoryBeanId;
    }

    /**
     * Sets the ID of the target bean to which the SqlSession advice will be applied.
     * @param targetBeanId the target bean ID
     */
    void setTargetBeanId(String targetBeanId) {
        this.targetBeanId = targetBeanId;
    }

    /**
     * Sets the target bean class to which the SqlSession advice will be applied.
     * Typically, this is a subclass of {@link SqlSessionProvider}, like {@link SqlSessionAgent}.
     * @param targetBeanClass the target bean class
     */
    void setTargetBeanClass(Class<?> targetBeanClass) {
        this.targetBeanClass = targetBeanClass;
    }

    /**
     * Sets the method name patterns to include.
     * @param includeMethodNamePatterns the include method name patterns
     */
    void setIncludeMethodNamePatterns(String[] includeMethodNamePatterns) {
        this.includeMethodNamePatterns = includeMethodNamePatterns;
    }

    /**
     * Sets the method name patterns to exclude.
     * @param excludeMethodNamePatterns the exclude method name patterns
     */
    void setExcludeMethodNamePatterns(String[] excludeMethodNamePatterns) {
        this.excludeMethodNamePatterns = excludeMethodNamePatterns;
    }

    /**
     * Sets the default {@link ExecutorType} for the sessions.
     * @param executorType the executor type
     */
    void setExecutorType(ExecutorType executorType) {
        this.executorType = executorType;
    }

    /**
     * Sets the transaction isolation level for the sessions.
     * @param isolationLevel the transaction isolation level
     */
    void setIsolationLevel(TransactionIsolationLevel isolationLevel) {
        this.isolationLevel = isolationLevel;
    }

    /**
     * Sets whether to enable auto-commit for the sessions.
     * @param autoCommit true to enable auto-commit, false otherwise
     */
    void setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    /**
     * Sets whether to enable read-only mode for the sessions.
     * @param readOnly true to enable read-only mode, false otherwise
     */
    void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    /**
     * Sets whether to force a rollback when closing a read-only session.
     * @param readOnlyRollbackOnClose true to force rollback on close, false otherwise
     */
    void setReadOnlyRollbackOnClose(boolean readOnlyRollbackOnClose) {
        this.readOnlyRollbackOnClose = readOnlyRollbackOnClose;
    }

    void register() {
        Assert.notNull(txAspectId, "txAspectId must not be null");
        Assert.notNull(targetBeanClass, "targetBeanClass must not be null");

        if (activityContext.getAspectRuleRegistry().contains(txAspectId)) {
            throw new IllegalStateException("SqlSessionAdvice is already registered with aspect id '" +
                    txAspectId + "'");
        }

        SqlSessionFactory sqlSessionFactory;
        try {
            sqlSessionFactory = activityContext.getBeanRegistry().getBean(SqlSessionFactory.class, sqlSessionFactoryBeanId);
        } catch (NoSuchBeanException e) {
            if (sqlSessionFactoryBeanId != null) {
                throw new IllegalStateException("Cannot resolve SqlSessionFactory with bean id '"
                        + sqlSessionFactoryBeanId + "'", e);
            } else {
                throw new IllegalStateException("No SqlSessionFactory bean found", e);
            }
        } catch (NoUniqueBeanException e) {
            throw new IllegalStateException("No unique SqlSessionFactory bean found; " +
                    "If multiple SqlSessionFactory beans are defined, please specify a sqlSessionFactoryBeanId", e);
        }

        AspectRule aspectRule = new AspectRule();
        aspectRule.setId(txAspectId);
        aspectRule.setOrder(0);

        String beanPattern;
        if (targetBeanId != null) {
            beanPattern = targetBeanId;
        } else {
            beanPattern = BeanRule.CLASS_DIRECTIVE_PREFIX + targetBeanClass.getName();
        }
        List<PointcutPatternRule> excludePatternRuleList = null;
        if (excludeMethodNamePatterns != null && excludeMethodNamePatterns.length > 0) {
            excludePatternRuleList = new ArrayList<>(excludeMethodNamePatterns.length);
            for (String methodNamePattern : excludeMethodNamePatterns) {
                excludePatternRuleList.add(PointcutPatternRule.newInstance(null, beanPattern, methodNamePattern));
            }
        }

        PointcutRule pointcutRule = new PointcutRule(PointcutType.WILDCARD);
        if (includeMethodNamePatterns != null && includeMethodNamePatterns.length > 0) {
            for (String methodNamePattern : includeMethodNamePatterns) {
                PointcutPatternRule ppr = PointcutPatternRule.newInstance(null, beanPattern, methodNamePattern);
                ppr.setExcludePatternRuleList(excludePatternRuleList);
                pointcutRule.addPointcutPatternRule(ppr);
            }
        } else {
            PointcutPatternRule ppr = PointcutPatternRule.newInstance(null, beanPattern, null);
            ppr.setExcludePatternRuleList(excludePatternRuleList);
            pointcutRule.addPointcutPatternRule(ppr);
        }

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
            if (isolationLevel != null) {
                sqlSessionAdvice.setIsolationLevel(isolationLevel);
            }
            sqlSessionAdvice.setAutoCommit(autoCommit);
            sqlSessionAdvice.setReadOnly(readOnly);
            sqlSessionAdvice.setReadOnlyRollbackOnClose(readOnlyRollbackOnClose);
            return sqlSessionAdvice;
        });

        AdviceRule afterAdviceRule = aspectRule.newAfterAdviceRule();
        afterAdviceRule.setAdviceAction(activity -> {
            SqlSessionAdvice sqlSessionAdvice = activity.getBeforeAdviceResult(txAspectId);
            if (sqlSessionAdvice != null) {
                sqlSessionAdvice.commit();
            }
            return null;
        });

        AdviceRule thrownAdviceRule = aspectRule.newThrownAdviceRule();
        thrownAdviceRule.setAdviceAction(activity -> {
            SqlSessionAdvice sqlSessionAdvice = activity.getBeforeAdviceResult(txAspectId);
            if (sqlSessionAdvice != null) {
                sqlSessionAdvice.rollback();
            }
            return null;
        });

        AdviceRule finallyAdviceRule = aspectRule.newFinallyAdviceRule();
        finallyAdviceRule.setAdviceAction(activity -> {
            SqlSessionAdvice sqlSessionAdvice = activity.getBeforeAdviceResult(txAspectId);
            if (sqlSessionAdvice != null) {
                sqlSessionAdvice.close();
            }
            return null;
        });

        try {
            activityContext.getAspectRuleRegistry().addAspectRule(aspectRule);
            if (logger.isDebugEnabled()) {
                logger.debug("Registered AspectRule {}", aspectRule);
            }
        } catch (IllegalRuleException e) {
            ToStringBuilder tsb = new ToStringBuilder("Failed to register SqlSessionAdvice with");
            tsb.append("txAspectId", txAspectId);
            tsb.append("sqlSessionFactoryBeanId", sqlSessionFactoryBeanId);
            throw new RuntimeException(tsb.toString(), e);
        }
    }

}
