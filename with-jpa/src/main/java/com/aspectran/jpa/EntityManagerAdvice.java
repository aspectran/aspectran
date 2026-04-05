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

import com.aspectran.utils.Assert;
import com.aspectran.utils.ObjectUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Advice bean that manages the lifecycle of a JPA {@link EntityManager}.
 *
 * <p>This advice handles the lazy creation, commitment, rollback, and closing
 * of an {@code EntityManager}, typically within the context of an Aspectran AOP
 * aspect. It provides declarative transaction management for JPA operations.</p>
 *
 * <p>Key features:</p>
 * <ul>
 *   <li>Lazy session initialization via {@link #getEntityManager()}</li>
 *   <li>Automatic transaction management via {@link #transactional()}</li>
 *   <li>Automatic commit and rollback based on method execution outcomes</li>
 * </ul>
 *
 * @author Juho Jeong
 * @since 2015. 04. 03.
 */
public class EntityManagerAdvice {

    private static final Logger logger = LoggerFactory.getLogger(EntityManagerAdvice.class);

    private final EntityManagerFactory entityManagerFactory;

    private EntityManager entityManager;

    /**
     * Flag indicating whether the EntityManager was closed by external logic.
     * When set, transactional operations become no-ops.
     */
    private boolean arbitrarilyClosed;

    private boolean transactional;

    /**
     * Creates a new {@code EntityManagerAdvice} with the specified factory.
     * @param entityManagerFactory the factory to be used for creating {@link EntityManager} instances
     */
    public EntityManagerAdvice(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    /**
     * Returns the {@link EntityManager} currently managed by this advice.
     * @return the active {@link EntityManager}
     */
    public EntityManager getEntityManager() {
        if (entityManager == null) {
            if (arbitrarilyClosed) {
                throw new IllegalStateException("EntityManager has been arbitrarily closed and cannot be reopened lazily");
            }
            doOpen();
        }
        return entityManager;
    }

    /**
     * Returns whether the managed {@link EntityManager} is currently open.
     * @return true if the entity manager is open, false otherwise
     */
    public boolean isOpen() {
        return (entityManager != null);
    }

    /**
     * Lazily creates and holds an {@link EntityManager} if one is not already active.
     * This method prepares the advice for subsequent data access operations.
     */
    public void open() {
        doOpen();
    }

    private void doOpen() {
        if (entityManager == null) {
            entityManager = entityManagerFactory.createEntityManager();

            if (logger.isDebugEnabled()) {
                logger.debug((arbitrarilyClosed ? "Reopen " : "Open ") +
                        ObjectUtils.simpleIdentityToString(entityManager));
            }

            arbitrarilyClosed = false;
        }
    }

    /**
     * Closes the managed {@link EntityManager} and releases associated resources.
     * If an active transaction is present, it will be rolled back. This method is safe to call multiple times.
     */
    public void close() {
        close(false);
    }

    /**
     * Closes the managed {@link EntityManager} and releases associated resources.
     * @param arbitrarily true if user code arbitrarily closes the entity manager, false otherwise
     */
    public void close(boolean arbitrarily) {
        if (entityManager != null) {
            rollbackTransaction();

            arbitrarilyClosed = arbitrarily;
            entityManager.close();

            if (logger.isDebugEnabled()) {
                logger.debug("Close {}", ObjectUtils.simpleIdentityToString(entityManager));
            }

            entityManager = null;
        }
    }

    /**
     * Returns whether the {@link EntityManager} was marked as closed by an external process.
     * If {@code true}, this advice will no longer perform transactional operations to avoid conflicts.
     * @return {@code true} if the entity manager is considered externally closed; {@code false} otherwise
     */
    public boolean isArbitrarilyClosed() {
        return arbitrarilyClosed;
    }

    /**
     * Begins a new local transaction if one is not already active.
     * This operation is a no-op if the {@code EntityManager} has been marked as arbitrarily closed.
     */
    public void transactional() {
        if (arbitrarilyClosed) {
            return;
        }
        beginTransaction();
    }

    /**
     * Commits the active local transaction.
     * This operation is a no-op if the {@code EntityManager} is not open or has been marked as arbitrarily closed.
     */
    public void commit() {
        if (isEntityManagerUnavailable()) {
            return;
        }
        commitTransaction();
    }

    /**
     * Rolls back the active local transaction.
     * This operation is a no-op if the {@code EntityManager} is not open or has been marked as arbitrarily closed.
     */
    public void rollback() {
        if (isEntityManagerUnavailable()) {
            return;
        }
        rollbackTransaction();
    }

    /**
     * Starts a new transaction. If a transaction is already active, it is rolled back before the new one begins.
     */
    private void beginTransaction() {
        if (!transactional) {
            EntityTransaction transaction = getEntityManager().getTransaction();
            if (transaction.isActive()) {
                transaction.rollback();
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Begin transaction for {}", ObjectUtils.simpleIdentityToString(entityManager));
            }
            transaction.begin();
            transactional = true;
        }
    }

    /**
     * Commits the current transaction if it is active.
     */
    private void commitTransaction() {
        if (transactional) {
            EntityTransaction transaction = getEntityManager().getTransaction();
            if (transaction.isActive()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Commit transaction for {}", ObjectUtils.simpleIdentityToString(entityManager));
                }
                transaction.commit();
            }
            transactional = false;
        }
    }

    /**
     * Rolls back the current transaction if it is active.
     */
    private void rollbackTransaction() {
        if (transactional) {
            EntityTransaction transaction = getEntityManager().getTransaction();
            if (transaction.isActive()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Rollback transaction for {}", ObjectUtils.simpleIdentityToString(entityManager));
                }
                transaction.rollback();
            }
            transactional = false;
        }
    }

    /**
     * Checks if the EntityManager is unavailable for operations.
     * @return true if the entity manager is not open or has been arbitrarily closed, false otherwise
     */
    private boolean isEntityManagerUnavailable() {
        return (entityManager == null || arbitrarilyClosed);
    }

    /**
     * Ensures that the SqlSession is not already open.
     * @throws IllegalStateException if the session is already open
     */
    private void ensureNotOpen() {
        Assert.state(entityManager == null, "EntityManager is already open");
    }

}
