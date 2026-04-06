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
package com.aspectran.jpa.routing;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.component.bean.ablility.InitializableBean;
import com.aspectran.jpa.AbstractEntityManagerProvider;
import com.aspectran.jpa.EntityManagerAdvice;
import com.aspectran.jpa.EntityManagerAdviceRegister;
import com.aspectran.utils.Assert;
import org.jspecify.annotations.NonNull;

/**
 * Advanced {@link jakarta.persistence.EntityManager} agent that routes operations
 * between primary and replica aspects based on the method name patterns.
 *
 * <p>This implementation allows for optimized database access by directing
 * read-only operations to a separate transactional context, which can be
 * configured differently (e.g., directed to a read-replica database).</p>
 *
 * <p>Created: 2026. 4. 5.</p>
 */
public class RoutingEntityManagerAgent extends AbstractEntityManagerProvider implements InitializableBean {

    /** Method name patterns that are treated as read-only by default. */
    private static final String[] DEFAULT_READONLY_METHOD_PATTERNS = {
            "find*",
            "getReference*"
    };

    /** Method name patterns for management that do not require transactional advice. */
    public static final String[] MANAGEMENT_METHOD_PATTERNS = {
            "getCriteria*",
            "getCache*Mode",
            "getEntityGraph*",
            "getMetamodel",
            "getTransaction",
            "getDelegate",
            "getProperties",
            "getFlushMode",
            "set*",
            "is*",
            "close",
            "flush",
            "clear",
            "detach",
            "contains",
            "unwrap",
            "createEntityGraph",
            "*WithConnection"
    };

    private final String primaryAspectId;

    private final String replicaAspectId;

    /**
     * Instantiates a new RoutingEntityManagerAgent.
     * @param primaryAspectId the ID for the primary aspect rule
     * @param replicaAspectId the ID for the replica aspect rule
     */
    public RoutingEntityManagerAgent(String primaryAspectId, String replicaAspectId) {
        Assert.notNull(primaryAspectId, "primaryAspectId must not be null");
        Assert.notNull(replicaAspectId, "replicaAspectId must not be null");
        this.primaryAspectId = primaryAspectId;
        this.replicaAspectId = replicaAspectId;
    }

    @Override
    @NonNull
    public EntityManagerAdvice getEntityManagerAdvice() {
        Activity currentActivity = getAvailableActivity();
        checkTransactional(currentActivity);

        EntityManagerAdvice primaryAdvice = currentActivity.getAvailableAdvice(primaryAspectId);
        if (primaryAdvice != null && primaryAdvice.isOpen()) {
            return primaryAdvice;
        }

        EntityManagerAdvice replicaAdvice = currentActivity.getAvailableAdvice(replicaAspectId);
        if (replicaAdvice != null && replicaAdvice.isOpen()) {
            return replicaAdvice;
        }

        EntityManagerAdvice entityManagerAdvice = (primaryAdvice != null ? primaryAdvice : replicaAdvice);
        if (entityManagerAdvice == null) {
            throw new IllegalStateException("No transactional context found for the current activity; " +
                    "ensure the activity is advised by aspect '" + primaryAspectId + "' or '" + replicaAspectId + "'");
        }
        return entityManagerAdvice;
    }

    @Override
    public void initialize() {
        if (!getAspectRuleRegistry().contains(primaryAspectId)) {
            EntityManagerAdviceRegister register = new EntityManagerAdviceRegister(getActivityContext());
            register.setTxAspectId(primaryAspectId);
            register.setEntityManagerFactoryBeanId(getEntityManagerFactoryBeanId());
            register.setTargetBeanId(getTargetBeanId());
            register.setTargetBeanClass(getTargetBeanClass());

            String[] excludeMethodNamePatterns = new String[DEFAULT_READONLY_METHOD_PATTERNS.length + MANAGEMENT_METHOD_PATTERNS.length];
            System.arraycopy(DEFAULT_READONLY_METHOD_PATTERNS, 0, excludeMethodNamePatterns, 0, DEFAULT_READONLY_METHOD_PATTERNS.length);
            System.arraycopy(MANAGEMENT_METHOD_PATTERNS, 0, excludeMethodNamePatterns, DEFAULT_READONLY_METHOD_PATTERNS.length, MANAGEMENT_METHOD_PATTERNS.length);
            register.setExcludeMethodNamePatterns(excludeMethodNamePatterns);

            register.register();
        }
        if (!getAspectRuleRegistry().contains(replicaAspectId)) {
            EntityManagerAdviceRegister register = new EntityManagerAdviceRegister(getActivityContext());
            register.setTxAspectId(replicaAspectId);
            register.setEntityManagerFactoryBeanId(getEntityManagerFactoryBeanId());
            register.setTargetBeanId(getTargetBeanId());
            register.setTargetBeanClass(getTargetBeanClass());

            register.setIncludeMethodNamePatterns(DEFAULT_READONLY_METHOD_PATTERNS);

            register.register();
        }
    }

}
