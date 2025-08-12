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
 * Advisory helper around an {@link EntityManager} lifecycle and transaction boundaries.
 * <p>
 * This class encapsulates a simple usage pattern:
 * </p>
 * <ul>
 *   <li>open(): lazily create and hold an EntityManager</li>
 *   <li>transactional(): begin a transaction if none is active for this advisor</li>
 *   <li>commit(): commit the transaction if one had been started via {@link #transactional()}</li>
 *   <li>close(): rollback any active transaction and close the EntityManager</li>
 * </ul>
 * <p>
 * It is intentionally minimal and does not integrate with container-managed transactions.
 * </p>
 *
 * <p>Created: 2025-04-24</p>
 */
public class EntityManagerAdvice {

    /** Factory used to create EntityManager instances. */
    private final EntityManagerFactory entityManagerFactory;

    /** The lazily created EntityManager held by this advisor. */
    private EntityManager entityManager;

    /**
     * Flag indicating whether the EntityManager was closed by external logic.
     * When set, transactional operations become no-ops.
     */
    private boolean arbitrarilyClosed;

    /** Whether a transaction has been started via {@link #transactional()}. */
    private boolean transactional;

    /**
     * Create a new advisor using the given factory.
     * @param entityManagerFactory the factory to create EntityManagers
     */
    public EntityManagerAdvice(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    /**
     * Returns the currently held EntityManager, or {@code null} if {@link #open()} was not called
     * or after {@link #close()}.
     * @return the current EntityManager, or {@code null}
     */
    public EntityManager getEntityManager() {
        return entityManager;
    }

    /**
     * Lazily creates an EntityManager if one is not already open.
     */
    public void open() {
        if (entityManager == null) {
            entityManager = entityManagerFactory.createEntityManager();
            arbitrarilyClosed = false;
        }
    }

    /**
     * Closes the held EntityManager, rolling back any active transaction first.
     * Safe to call multiple times.
     */
    public void close() {
        if (entityManager != null) {
            rollbackTransaction();
            entityManager.close();
            entityManager = null;
        }
    }

    /**
     * Returns whether this advisor is marked as arbitrarily closed.
     * @return {@code true} if arbitrarily closed
     */
    public boolean isArbitrarilyClosed() {
        return arbitrarilyClosed;
    }

    /**
     * Begins a transaction if none is currently active for this advisor.
     * If the advisor is marked arbitrarily closed, this is a no-op.
     */
    public void transactional() {
        if (checkOpen()) {
            return;
        }
        beginTransaction();
    }

    /**
     * Commits an active transaction started via {@link #transactional()}.
     * If the advisor is marked arbitrarily closed, this is a no-op.
     */
    public void commit() {
        if (checkOpen()) {
            return;
        }
        commitTransaction();
    }

    /** Start a new transaction, rolling back any active one first. */
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

    /** Commit the current transaction, if one was started. */
    private void commitTransaction() {
        if (transactional) {
            EntityTransaction transaction = entityManager.getTransaction();
            if (transaction.isActive()) {
                transaction.commit();
            }
            transactional = false;
        }
    }

    /** Roll back the current transaction, if one was started. */
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
     * Checks if operations should proceed: returns true if arbitrarilyClosed is set, throws if
     * no EntityManager is open, otherwise allows further work.
     * @return {@code true} if arbitrarily closed and the caller should stop further work
     * @throws IllegalStateException if no EntityManager has been opened
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
