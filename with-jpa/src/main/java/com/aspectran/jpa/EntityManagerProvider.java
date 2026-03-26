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
import com.aspectran.core.component.bean.NoUniqueBeanException;
import com.aspectran.core.component.bean.ablility.InitializableBean;
import com.aspectran.utils.ClassUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.jspecify.annotations.NonNull;

/**
 * A base class for components that need access to a transactional {@link EntityManager}.
 * This provider simplifies JPA data access by managing the lifecycle of an {@code EntityManager}
 * through an associated {@link EntityManagerAdvice}, which is handled by Aspectran's AOP mechanism.
 * <p>
 * Subclasses can obtain the current {@code EntityManager} by calling
 * {@link #getEntityManager()}. Transactional behavior is controlled by the AOP advice
 * linked via the {@code txAspectId}. If the required aspect is not pre-configured
 * in the Aspectran context, this provider can dynamically register it during initialization.
 * This ensures that methods are executed within a proper transactional boundary.
 * </p>
 *
 * <p>Created: 2025-04-24</p>
 */
public abstract class EntityManagerProvider extends InstantActivitySupport implements InitializableBean {

    private final String txAspectId;

    private String readOnlyAspectId;

    private String entityManagerFactoryBeanId;

    private String targetBeanId;

    private boolean reuseWritable = true;

    /**
     * Instantiates a new {@code EntityManagerProvider}.
     * @param txAspectId the ID of the aspect that manages the {@link EntityManagerAdvice}
     */
    public EntityManagerProvider(String txAspectId) {
        if (txAspectId == null) {
            throw new IllegalArgumentException("txAspectId must not be null");
        }
        this.txAspectId = txAspectId;
    }

    /**
     * Sets the ID for the read-only aspect rule.
     * @param readOnlyAspectId the read-only aspect ID
     */
    public void setReadOnlyAspectId(String readOnlyAspectId) {
        this.readOnlyAspectId = readOnlyAspectId;
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
     * Returns the ID of the target bean to which the entity manager advice will be applied.
     * @return the target bean ID
     */
    protected String getTargetBeanId() {
        return targetBeanId;
    }

    /**
     * Sets the ID of the target bean to which the entity manager advice will be applied.
     * @param targetBeanId the target bean ID
     */
    public void setTargetBeanId(String targetBeanId) {
        this.targetBeanId = targetBeanId;
    }

    /**
     * Returns the target bean class to which the entity manager advice will be applied.
     * @return the target bean class
     */
    protected Class<?> getTargetBeanClass() {
        return ClassUtils.getUserClass(getClass());
    }

    /**
     * Sets whether to reuse a writable session even for read-only operations.
     * @param reuseWritable true to reuse a writable session if one is already open,
     *                      false to always use a read-only session for read-only operations.
     *                      The default is {@code true}.
     */
    public void setReuseWritable(boolean reuseWritable) {
        this.reuseWritable = reuseWritable;
    }

    /**
     * Returns the {@link EntityManagerFactory} associated with this provider.
     * @return the EntityManagerFactory
     */
    protected EntityManagerFactory getEntityManagerFactory() {
        try {
            return getActivityContext().getBeanRegistry().getBean(EntityManagerFactory.class, entityManagerFactoryBeanId);
        } catch (NoSuchBeanException e) {
            StringBuilder msg = new StringBuilder();
            msg.append("Failed to resolve EntityManagerFactory for aspect '").append(txAspectId).append("'");
            if (targetBeanId != null) {
                msg.append(" on bean '").append(targetBeanId).append("'");
            }
            if (entityManagerFactoryBeanId != null) {
                msg.append(" with bean id '").append(entityManagerFactoryBeanId).append("'");
            } else {
                msg.append("; No EntityManagerFactory bean found");
            }
            throw new IllegalStateException(msg.toString(), e);
        } catch (NoUniqueBeanException e) {
            StringBuilder msg = new StringBuilder();
            msg.append("Failed to resolve EntityManagerFactory for aspect '").append(txAspectId).append("'");
            if (targetBeanId != null) {
                msg.append(" on bean '").append(targetBeanId).append("'");
            }
            msg.append("; No unique EntityManagerFactory bean found. If multiple EntityManagerFactory beans are defined, " +
                    "please specify an entityManagerFactoryBeanId");
            throw new IllegalStateException(msg.toString(), e);
        }
    }

    /**
     * Initializes the provider by ensuring the necessary {@link EntityManagerAdvice} aspect is registered.
     * If the aspect is not found, it proceeds to register it dynamically.
     */
    @Override
    public void initialize() {
        if (!getActivityContext().getAspectRuleRegistry().contains(txAspectId)) {
            EntityManagerAdviceRegister register = new EntityManagerAdviceRegister(getActivityContext());
            register.setTxAspectId(txAspectId);
            register.setEntityManagerFactoryBeanId(entityManagerFactoryBeanId);
            register.setTargetBeanId(targetBeanId);
            register.setTargetBeanClass(getTargetBeanClass());
            register.register();
        }
        if (readOnlyAspectId != null && !getActivityContext().getAspectRuleRegistry().contains(readOnlyAspectId)) {
            EntityManagerAdviceRegister register = new EntityManagerAdviceRegister(getActivityContext());
            register.setTxAspectId(readOnlyAspectId);
            register.setEntityManagerFactoryBeanId(entityManagerFactoryBeanId);
            register.setTargetBeanId(targetBeanId);
            register.setTargetBeanClass(getTargetBeanClass());
            register.setReadOnly(true);
            register.register();
        }
    }

    /**
     * Returns the current transactional {@link EntityManager}.
     * This method retrieves the {@code EntityManager} from the associated {@link EntityManagerAdvice}.
     * If the {@code EntityManager} was arbitrarily closed, it attempts to reopen it.
     * @return the active {@link EntityManager} instance
     * @throws IllegalStateException if the {@code EntityManager} is not open and cannot be reopened
     */
    protected EntityManager getEntityManager() {
        return getEntityManagerAdvice().getEntityManager();
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
        Activity currentActivity = getAvailableActivity();

        EntityManagerAdvice writableAdvice = currentActivity.getAvailableAdvice(txAspectId);
        EntityManagerAdvice readOnlyAdvice = null;
        if (readOnlyAspectId != null) {
            readOnlyAdvice = currentActivity.getAvailableAdvice(readOnlyAspectId);
        }

        if (reuseWritable && writableAdvice != null && writableAdvice.isOpen()) {
            return writableAdvice;
        }

        String currentAspectId = EntityManagerAdviceRegister.peekCurrentAspectId(currentActivity, getTargetBeanClass());
        if (currentAspectId != null) {
            EntityManagerAdvice entityManagerAdvice = currentActivity.getAvailableAdvice(currentAspectId);
            if (entityManagerAdvice != null) {
                return entityManagerAdvice;
            }
        }

        if (writableAdvice != null && writableAdvice.isOpen()) {
            return writableAdvice;
        }
        if (readOnlyAdvice != null && readOnlyAdvice.isOpen()) {
            return readOnlyAdvice;
        }

        EntityManagerAdvice entityManagerAdvice = (readOnlyAdvice != null ? readOnlyAdvice : writableAdvice);
        if (entityManagerAdvice == null) {
            if (getActivityContext().getAspectRuleRegistry().getAspectRule(txAspectId) == null) {
                throw new IllegalArgumentException("Aspect '" + txAspectId +
                        "' handling EntityManagerAdvice is not registered");
            }
            throw new IllegalStateException("EntityManagerAdvice not found handled by aspect '" + txAspectId + "'");
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

}
