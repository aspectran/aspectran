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
import com.aspectran.utils.ClassUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

/**
 * Base support class for JPA {@link EntityManager} agents.
 *
 * <p>Created: 2025-04-24</p>
 */
public abstract class AbstractEntityManagerProvider extends AbstractEntityManagerAgent {

    private String entityManagerFactoryBeanId;

    private String targetBeanId;

    public String getEntityManagerFactoryBeanId() {
        return entityManagerFactoryBeanId;
    }

    public void setEntityManagerFactoryBeanId(String entityManagerFactoryBeanId) {
        this.entityManagerFactoryBeanId = entityManagerFactoryBeanId;
    }

    public String getTargetBeanId() {
        return targetBeanId;
    }

    public void setTargetBeanId(String targetBeanId) {
        this.targetBeanId = targetBeanId;
    }

    public Class<?> getTargetBeanClass() {
        return ClassUtils.getUserClass(getClass());
    }

    @Override
    public EntityManager getEntityManager() {
        return getEntityManagerAdvice().getEntityManager();
    }

    @Override
    public EntityManagerFactory getEntityManagerFactory() {
        try {
            return getBeanRegistry().getBean(EntityManagerFactory.class, entityManagerFactoryBeanId);
        } catch (Exception e) {
            StringBuilder msg = new StringBuilder();
            msg.append("Failed to resolve EntityManagerFactory");
            if (targetBeanId != null) {
                msg.append(" on bean '").append(targetBeanId).append("'");
            }
            if (entityManagerFactoryBeanId != null) {
                msg.append(" with bean id '").append(entityManagerFactoryBeanId).append("'");
            } else {
                msg.append("; No EntityManagerFactory bean found");
            }
            throw new IllegalStateException(msg.toString(), e);
        }
    }

    /**
     * Ensures that the provider is operating within a transactional context.
     * @throws IllegalStateException if called during a non-transactional proxy-mode activity
     */
    protected void checkTransactional(Activity.Mode activityMode) {
        if (activityMode == Activity.Mode.PROXY) {
            throw new IllegalStateException("Cannot be executed on a non-transactional activity;" +
                    " needs to be wrapped in an instant activity.");
        }
    }

}
