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

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

/**
 * An advisory helper that manages the lifecycle of an {@link EntityManager} and its
 * transaction boundaries. This class is designed to be used as a bean within an
 * Aspectran aspect to provide programmatic transaction control.
 * <p>
 * It encapsulates a simple, explicit usage pattern for JPA operations:
 * </p>
 * <ul>
 *   <li>{@link #open()}: Lazily creates and holds an {@code EntityManager}.</li>
 *   <li>{@link #transactional()}: Begins a new transaction if one is not already active.</li>
 *   <li>{@link #commit()}: Commits the active transaction.</li>
 *   <li>{@link #close()}: Rolls back any pending transaction and closes the {@code EntityManager}.</li>
 * </ul>
 * <p>
 * This implementation is minimal and does not integrate with container-managed transactions,
 * offering a lightweight alternative for managing persistence contexts.
 * </p>
 *
 * <p>Created: 2025-04-24</p>
 */
public class EntityManagerAdvice {

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
     * @return the active {@link EntityManager}, or {@code null} if it has not been opened or has been closed
     */
    public EntityManager getEntityManager() {
        return entityManager;
    }

    /**
     * Lazily creates and holds an {@link EntityManager} if one is not already active.
     * This method prepares the advice for subsequent data access operations.
     */
    public void open() {
        if (entityManager == null) {
            entityManager = entityManagerFactory.createEntityManager();
            arbitrarilyClosed = false;
        }
    }

    /**
     * Closes the managed {@link EntityManager} and releases associated resources.
     * If an active transaction is present, it will be rolled back. This method is safe to call multiple times.
     */
    public void close() {
        if (entityManager != null) {
            rollbackTransaction();
            entityManager.close();
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
     * This operation is a no-op if the {@code EntityManager} is not open or has been marked as arbitrarily closed.
     */
    public void transactional() {
        if (checkOpen()) {
            return;
        }
        beginTransaction();
    }

    /**
     * Commits the active local transaction.
     * This operation is a no-op if the {@code EntityManager} is not open or has been marked as arbitrarily closed.
     */
    public void commit() {
        if (checkOpen()) {
            return;
        }
        commitTransaction();
    }

    /**
     * Starts a new transaction. If a transaction is already active, it is rolled back before the new one begins.
     */
    private void beginTransaction() {
        if (!transactional) {
            EntityTransaction transaction = entityManager.getTransaction();
            if (transaction.isActive()) {
                transaction.rollback();
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
            EntityTransaction transaction = entityManager.getTransaction();
            if (transaction.isActive()) {
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
            EntityTransaction transaction = entityManager.getTransaction();
            if (transaction.isActive()) {
                transaction.rollback();
            }
            transactional = false;
        }
    }

    /**
     * Verifies that the {@link EntityManager} is open and ready for use.
     * @return {@code true} if operations should be skipped because the entity manager was arbitrarily closed
     * @throws IllegalStateException if the {@link EntityManager} is not open
     */
    private boolean checkOpen() {
        if (arbitrarilyClosed) {
            return true;
        }
        if (entityManager == null) {
            throw new IllegalStateException("EntityManager is not open");
        }
        return false;
    }

}
