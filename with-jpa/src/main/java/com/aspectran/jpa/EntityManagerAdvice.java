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
import com.aspectran.utils.ToStringBuilder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.PersistenceException;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * An advisory helper that manages the lifecycle of an {@link EntityManager} and its
 * transaction boundaries. This class is designed to be used as a bean within an
 * Aspectran aspect to provide programmatic transaction control.
 * <p>
 * It encapsulates a simple, explicit usage pattern for JPA operations:
 * </p>
 * <ul>
 *   <li>open(): lazily create and hold an {@link EntityManager}</li>
 *   <li>transactional(): Begins a new transaction if one is not already active.</li>
 *   <li>commit(): Commits the active transaction.</li>
 *   <li>close(): Rolls back any pending transaction and closes the {@code EntityManager}.</li>
 * </ul>
 * <p>
 * This implementation is minimal and does not integrate with container-managed transactions,
 * offering a lightweight alternative for managing persistence contexts.
 * </p>
 *
 * <p>Created: 2025-04-24</p>
 */
public class EntityManagerAdvice {

    private static final Logger logger = LoggerFactory.getLogger(EntityManagerAdvice.class);

    private final EntityManagerFactory entityManagerFactory;

    private EntityManager entityManager;

    private boolean readOnly;

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
     * Returns whether the entity manager is in read-only mode.
     * @return true if read-only mode is enabled, false otherwise
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * Sets whether the entity manager is in read-only mode.
     * @param readOnly true to enable read-only mode, false otherwise
     */
    public void setReadOnly(boolean readOnly) {
        ensureNotOpen();
        this.readOnly = readOnly;
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

            if (readOnly) {
                applyReadOnly(entityManager, true);
            }

            if (logger.isDebugEnabled()) {
                ToStringBuilder tsb = new ToStringBuilder((arbitrarilyClosed ? "Reopen " : "Open ") +
                        ObjectUtils.simpleIdentityToString(entityManager));
                tsb.appendForce("readOnly", readOnly);
                logger.debug(tsb.toString());
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

            if (readOnly) {
                applyReadOnly(entityManager, false);
            }

            arbitrarilyClosed = arbitrarily;
            entityManager.close();

            if (logger.isDebugEnabled()) {
                logger.debug("Close {}", ObjectUtils.simpleIdentityToString(entityManager));
            }

            entityManager = null;
        }
    }

    /**
     * Applies the read-only setting to the entity manager.
     * <p>This method attempts to set the read-only state on the underlying JDBC connection
     * if possible. It also tries to apply provider-specific optimizations for Hibernate
     * and EclipseLink using reflection to avoid hard dependencies.</p>
     * @param entityManager the entity manager to configure
     * @param readOnly true to enable read-only mode, false to disable it
     */
    private void applyReadOnly(@NonNull EntityManager entityManager, boolean readOnly) {
        // Try to unwrap to JDBC Connection (standard JPA approach)
        try {
            Connection conn = entityManager.unwrap(Connection.class);
            if (conn != null) {
                conn.setReadOnly(readOnly);
            }
        } catch (PersistenceException | SQLException e) {
            if (logger.isTraceEnabled()) {
                logger.trace("Failed to set JDBC connection to read-only: {}", e.getMessage());
            }
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
        if (readOnly || arbitrarilyClosed) {
            return;
        }
        beginTransaction();
    }

    /**
     * Commits the active local transaction.
     * This operation is a no-op if the {@code EntityManager} is not open or has been marked as arbitrarily closed.
     */
    public void commit() {
        if (readOnly || isEntityManagerUnavailable()) {
            return;
        }
        commitTransaction();
    }

    /**
     * Rolls back the active local transaction.
     * This operation is a no-op if the {@code EntityManager} is not open or has been marked as arbitrarily closed.
     */
    public void rollback() {
        if (readOnly || isEntityManagerUnavailable()) {
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
