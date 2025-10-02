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
 * A base class for components that need access to a transactional {@link EntityManager}.
 * This provider simplifies JPA data access by managing the lifecycle of an {@code EntityManager}
 * through an associated {@link EntityManagerAdvice}, which is handled by Aspectran's AOP mechanism.
 * <p>
 * Subclasses can obtain the current, thread-safe {@code EntityManager} by calling
 * {@link #getEntityManager()}. Transactional behavior is controlled by the AOP advice
 * linked via the {@code relevantAspectId}. If the required aspect is not pre-configured
 * in the Aspectran context, this provider can dynamically register it during initialization.
 * This ensures that methods are executed within a proper transactional boundary.
 * </p>
 *
 * <p>Created: 2025-04-24</p>
 */
public abstract class EntityManagerProvider extends InstantActivitySupport implements InitializableBean {

    private final String relevantAspectId;

    private String entityManagerFactoryBeanId;

    /**
     * Instantiates a new {@code EntityManagerProvider}.
     * @param relevantAspectId the ID of the aspect that manages the {@link EntityManagerAdvice}
     */
    public EntityManagerProvider(String relevantAspectId) {
        if (relevantAspectId == null) {
            throw new IllegalArgumentException("relevantAspectId must not be null");
        }
        this.relevantAspectId = relevantAspectId;
    }

    /**
     * Sets the bean ID of the {@link EntityManagerFactory}.
     * This is used to locate the factory when the advice needs to be auto-registered.
     * @param entityManagerFactoryBeanId the bean ID of the {@link EntityManagerFactory}
     */
    public void setEntityManagerFactoryBeanId(String entityManagerFactoryBeanId) {
        this.entityManagerFactoryBeanId = entityManagerFactoryBeanId;
    }

    /**
     * Returns the current transactional {@link EntityManager}.
     * This method retrieves the {@code EntityManager} from the associated {@link EntityManagerAdvice}.
     * If the {@code EntityManager} was arbitrarily closed, it attempts to reopen it.
     * @return the active {@link EntityManager} instance
     * @throws IllegalStateException if the {@code EntityManager} is not open and cannot be reopened
     */
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

    /**
     * Retrieves the {@link EntityManagerAdvice} associated with the current activity.
     * It first checks for a bean instance and then for an advice result from the current activity.
     * @return the {@link EntityManagerAdvice} instance
     * @throws IllegalStateException if the advice is not found or the aspect is not registered
     */
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

    /**
     * Ensures that the provider is operating within a transactional context.
     * @throws IllegalStateException if called during a non-transactional proxy-mode activity
     */
    private void checkTransactional() {
        if (getAvailableActivity().getMode() == Activity.Mode.PROXY) {
            throw new IllegalStateException("Cannot be executed on a non-transactional activity;" +
                    " needs to be wrapped in an instant activity.");
        }
    }

    /**
     * Initializes the provider by ensuring the necessary {@link EntityManagerAdvice} aspect is registered.
     * If the aspect is not found, it proceeds to register it dynamically.
     */
    @Override
    public void initialize() {
        if (!getActivityContext().getAspectRuleRegistry().contains(relevantAspectId)) {
            registerSqlSessionAdvice();
        }
    }

    /**
     * Dynamically registers an {@link AspectRule} to manage the {@link EntityManager} lifecycle.
     * <p>Note: The method name {@code registerSqlSessionAdvice} is a misnomer due to a likely
     * copy-paste error; it actually registers advice for an {@code EntityManager}.</p>
     * This method creates and configures an aspect with before, after, and finally advice
     * to handle opening, committing, and closing the {@code EntityManager}.
     * @throws IllegalStateException if the advice is already registered or the {@link EntityManagerFactory} cannot be resolved
     */
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
