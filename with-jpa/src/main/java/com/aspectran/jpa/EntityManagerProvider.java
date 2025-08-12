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
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

/**
 * Base support class that locates and manages access to an {@link EntityManager}
 * and the corresponding {@link EntityManagerAdvice} registered via Aspectran AOP.
 * <p>
 * Subclasses can call {@link #getEntityManager()} to obtain a context-bound
 * EntityManager and rely on {@link #getEntityManagerAdvice()} for transactional
 * control as configured by the registered aspect.
 * </p>
 *
 * <p>Created: 2025-04-24</p>
 */
public abstract class EntityManagerProvider extends InstantActivitySupport implements InitializableBean {

    private final String relevantAspectId;

    private String entityManagerFactoryBeanId;

    public EntityManagerProvider(String relevantAspectId) {
        if (relevantAspectId == null) {
            throw new IllegalArgumentException("relevantAspectId must not be null");
        }
        this.relevantAspectId = relevantAspectId;
    }

    public void setEntityManagerFactoryBeanId(String entityManagerFactoryBeanId) {
        this.entityManagerFactoryBeanId = entityManagerFactoryBeanId;
    }

    protected EntityManager getEntityManager() {
        EntityManagerAdvice entityManagerAdvice = getEntityManagerAdvice();
        EntityManager entityManager = entityManagerAdvice.getEntityManager();
        if (entityManager == null) {
            if (entityManagerAdvice.isArbitrarilyClosed()) {
                entityManagerAdvice.open();
                entityManager = entityManagerAdvice.getEntityManager();
            } else {
                throw new IllegalStateException("EntityManager is not opened");
            }
        }
        return entityManager;
    }

    @NonNull
    protected EntityManagerAdvice getEntityManagerAdvice() {
        checkTransactional();
        EntityManagerAdvice entityManagerAdvice = getAvailableActivity().getAdviceBean(relevantAspectId);
        if (entityManagerAdvice == null) {
            entityManagerAdvice = getAvailableActivity().getBeforeAdviceResult(relevantAspectId);
        }
        if (entityManagerAdvice == null) {
            if (getActivityContext().getAspectRuleRegistry().getAspectRule(relevantAspectId) == null) {
                throw new IllegalArgumentException("Aspect '" + relevantAspectId +
                        "' handling EntityManagerAdvice is not registered");
            }
            throw new IllegalStateException("EntityManagerAdvice not found handled by aspect '" + relevantAspectId + "'");
        }
        return entityManagerAdvice;
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
            throw new IllegalStateException("EntityManagerAdvice is already registered");
        }

        EntityManagerFactory entityManagerFactory;
        try {
            entityManagerFactory = getBeanRegistry().getBean(EntityManagerFactory.class, entityManagerFactoryBeanId);
        } catch (NoSuchBeanException e) {
            if (entityManagerFactoryBeanId != null) {
                throw new IllegalStateException("Cannot resolve EntityManagerFactory with id=" + entityManagerFactoryBeanId, e);
            } else {
                throw new IllegalStateException("EntityManagerFactory is not defined", e);
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
            EntityManagerAdvice entityManagerAdvice = new EntityManagerAdvice(entityManagerFactory);
            entityManagerAdvice.open();
            return entityManagerAdvice;
        });

        AdviceRule afterAdviceRule = aspectRule.newAfterAdviceRule();
        afterAdviceRule.setAdviceAction(activity -> {
            EntityManagerAdvice entityManagerAdvice = activity.getBeforeAdviceResult(relevantAspectId);
            entityManagerAdvice.commit();
            return null;
        });

        AdviceRule finallyAdviceRule = aspectRule.newFinallyAdviceRule();
        finallyAdviceRule.setAdviceAction(activity -> {
            EntityManagerAdvice entityManagerAdvice = activity.getBeforeAdviceResult(relevantAspectId);
            entityManagerAdvice.close();
            return null;
        });

        try {
            getActivityContext().getAspectRuleRegistry().addAspectRule(aspectRule);
        } catch (IllegalRuleException e) {
            ToStringBuilder tsb = new ToStringBuilder("Failed to register EntityManagerAdvice with");
            tsb.append("relevantAspectId", relevantAspectId);
            tsb.append("sqlSessionFactoryBeanId", entityManagerFactoryBeanId);
            throw new RuntimeException(tsb.toString(), e);
        }
    }

}
