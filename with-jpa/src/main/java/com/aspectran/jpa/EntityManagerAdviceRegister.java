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
package com.aspectran.jpa;

import com.aspectran.core.component.bean.NoSuchBeanException;
import com.aspectran.core.component.bean.NoUniqueBeanException;
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
import com.aspectran.utils.Assert;
import com.aspectran.utils.ToStringBuilder;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class that dynamically registers an Aspectran {@link com.aspectran.core.context.rule.AspectRule} for
 * {@link EntityManagerAdvice}.
 *
 * <p>This class encapsulates the complex logic of configuring pointcuts,
 * joinpoints, and advice actions to manage the lifecycle of a JPA
 * {@link jakarta.persistence.EntityManager} automatically via AOP.</p>
 *
 * <p>It is used by {@link DefaultEntityManagerAgent} and {@link com.aspectran.jpa.routing.RoutingEntityManagerAgent}
 * to ensure that the required transaction aspects are present in the
 * activity context.</p>
 */
public class EntityManagerAdviceRegister {

    private static final Logger logger = LoggerFactory.getLogger(EntityManagerAdviceRegister.class);

    private final ActivityContext activityContext;

    private String txAspectId;

    private String entityManagerFactoryBeanId;

    private String targetBeanId;

    private Class<?> targetBeanClass;

    private String[] includeMethodNamePatterns;

    private String[] excludeMethodNamePatterns;

    /**
     * Instantiates a new EntityManagerAdviceRegister.
     * @param activityContext the activity context
     */
    public EntityManagerAdviceRegister(ActivityContext activityContext) {
        this.activityContext = activityContext;
    }

    /**
     * Sets the ID for the aspect rule to be registered.
     * @param txAspectId the aspect ID
     */
    public void setTxAspectId(String txAspectId) {
        this.txAspectId = txAspectId;
    }

    /**
     * Sets the bean ID of the {@link EntityManagerFactory} to be used.
     * @param entityManagerFactoryBeanId the bean ID of the EntityManagerFactory
     */
    public void setEntityManagerFactoryBeanId(String entityManagerFactoryBeanId) {
        this.entityManagerFactoryBeanId = entityManagerFactoryBeanId;
    }

    /**
     * Sets the ID of the target bean to which the advice will be applied.
     * @param targetBeanId the target bean ID
     */
    public void setTargetBeanId(String targetBeanId) {
        this.targetBeanId = targetBeanId;
    }

    /**
     * Sets the target bean class to which the advice will be applied.
     * @param targetBeanClass the target bean class
     */
    public void setTargetBeanClass(Class<?> targetBeanClass) {
        this.targetBeanClass = targetBeanClass;
    }

    /**
     * Sets the method name patterns to include.
     * @param includeMethodNamePatterns the include method name patterns
     */
    public void setIncludeMethodNamePatterns(String[] includeMethodNamePatterns) {
        this.includeMethodNamePatterns = includeMethodNamePatterns;
    }

    /**
     * Sets the method name patterns to exclude.
     * @param excludeMethodNamePatterns the exclude method name patterns
     */
    public void setExcludeMethodNamePatterns(String[] excludeMethodNamePatterns) {
        this.excludeMethodNamePatterns = excludeMethodNamePatterns;
    }

    public void register() {
        Assert.notNull(txAspectId, "txAspectId must not be null");
        Assert.notNull(targetBeanClass, "targetBeanClass must not be null");

        if (activityContext.getAspectRuleRegistry().contains(txAspectId)) {
            throw new IllegalStateException("EntityManagerAdvice is already registered with aspect id '" +
                    txAspectId + "'");
        }

        EntityManagerFactory entityManagerFactory;
        try {
            entityManagerFactory = activityContext.getBeanRegistry().getBean(EntityManagerFactory.class, entityManagerFactoryBeanId);
        } catch (NoSuchBeanException e) {
            if (entityManagerFactoryBeanId != null) {
                throw new IllegalStateException("Cannot resolve EntityManagerFactory with bean id '"
                        + entityManagerFactoryBeanId + "'", e);
            } else {
                throw new IllegalStateException("No EntityManagerFactory bean found", e);
            }
        } catch (NoUniqueBeanException e) {
            throw new IllegalStateException("No unique EntityManagerFactory bean found; " +
                    "If multiple EntityManagerFactory beans are defined, please specify an entityManagerFactoryBeanId", e);
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
        beforeAdviceRule.setAdviceAction(activity -> new EntityManagerAdvice(entityManagerFactory));

        AdviceRule afterAdviceRule = aspectRule.newAfterAdviceRule();
        afterAdviceRule.setAdviceAction(activity -> {
            EntityManagerAdvice entityManagerAdvice = activity.getBeforeAdviceResult(txAspectId);
            if (entityManagerAdvice != null) {
                entityManagerAdvice.commit();
            }
            return null;
        });

        AdviceRule thrownAdviceRule = aspectRule.newThrownAdviceRule();
        thrownAdviceRule.setAdviceAction(activity -> {
            EntityManagerAdvice entityManagerAdvice = activity.getBeforeAdviceResult(txAspectId);
            if (entityManagerAdvice != null) {
                entityManagerAdvice.rollback();
            }
            return null;
        });

        AdviceRule finallyAdviceRule = aspectRule.newFinallyAdviceRule();
        finallyAdviceRule.setAdviceAction(activity -> {
            EntityManagerAdvice entityManagerAdvice = activity.getBeforeAdviceResult(txAspectId);
            if (entityManagerAdvice != null) {
                entityManagerAdvice.close();
            }
            return null;
        });

        try {
            activityContext.getAspectRuleRegistry().addAspectRule(aspectRule);
            if (logger.isDebugEnabled()) {
                logger.debug("Registered AspectRule {}", aspectRule);
            }
        } catch (IllegalRuleException e) {
            ToStringBuilder tsb = new ToStringBuilder("Failed to register EntityManagerAdvice with");
            tsb.append("txAspectId", txAspectId);
            tsb.append("entityManagerFactoryBeanId", entityManagerFactoryBeanId);
            throw new RuntimeException(tsb.toString(), e);
        }
    }

}
