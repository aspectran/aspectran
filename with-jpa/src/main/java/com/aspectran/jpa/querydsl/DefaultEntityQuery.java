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
package com.aspectran.jpa.querydsl;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.component.bean.ablility.InitializableBean;
import com.aspectran.jpa.EntityManagerAdvice;
import com.aspectran.jpa.EntityManagerAdviceRegister;
import com.querydsl.jpa.JPQLQueryFactory;
import jakarta.persistence.EntityManager;
import org.jspecify.annotations.NonNull;

/**
 * Integrated {@link EntityManager} and {@link JPQLQueryFactory} (Querydsl) agent
 * that uses a single transaction aspect for all intercepted operations.
 *
 * <p>Created: 2025-04-24</p>
 */
public class DefaultEntityQuery extends EntityQuery implements InitializableBean {

    private final String txAspectId;

    /**
     * Instantiates a new DefaultEntityQuery.
     * @param txAspectId the ID of the aspect that provides the EntityManagerAdvice
     */
    public DefaultEntityQuery(String txAspectId) {
        if (txAspectId == null) {
            throw new IllegalArgumentException("txAspectId must not be null");
        }
        this.txAspectId = txAspectId;
    }

    @Override
    @NonNull
    public EntityManagerAdvice getEntityManagerAdvice() {
        Activity currentActivity = getAvailableActivity();
        checkTransactional(currentActivity.getMode());
        EntityManagerAdvice entityManagerAdvice = currentActivity.getAvailableAdvice(txAspectId);
        if (entityManagerAdvice == null) {
            if (getAspectRuleRegistry().getAspectRule(txAspectId) == null) {
                throw new IllegalArgumentException("Aspect '" + txAspectId +
                        "' handling EntityManagerAdvice is not registered");
            }
            throw new IllegalStateException("No transactional context found for the current activity; " +
                    "ensure the activity is advised by aspect '" + txAspectId + "'");
        }
        return entityManagerAdvice;
    }

    @Override
    public void initialize() {
        if (!getAspectRuleRegistry().contains(txAspectId)) {
            EntityManagerAdviceRegister register = new EntityManagerAdviceRegister(getActivityContext());
            register.setTxAspectId(txAspectId);
            register.setEntityManagerFactoryBeanId(getEntityManagerFactoryBeanId());
            register.setTargetBeanId(getTargetBeanId());
            register.setTargetBeanClass(getTargetBeanClass());
            register.register();
        }
    }

}
