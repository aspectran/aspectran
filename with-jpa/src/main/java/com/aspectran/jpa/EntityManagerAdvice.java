package com.aspectran.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

/**
 * <p>Created: 2025-04-24</p>
 */
public class EntityManagerAdvice {

    private final EntityManagerFactory entityManagerFactory;

    private EntityManager entityManager;

    private boolean arbitrarilyClosed;

    private boolean transactional;

    public EntityManagerAdvice(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void open() {
        if (entityManager == null) {
            entityManager = entityManagerFactory.createEntityManager();
            arbitrarilyClosed = false;
        }
    }

    public void close() {
        if (entityManager != null) {
            rollbackTransaction();
            entityManager.close();
            entityManager = null;
        }
    }

    public boolean isArbitrarilyClosed() {
        return arbitrarilyClosed;
    }

    public void transactional() {
        if (checkOpen()) {
            return;
        }
        beginTransaction();
    }

    public void commit() {
        if (checkOpen()) {
            return;
        }
        commitTransaction();
    }

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

    private void commitTransaction() {
        if (transactional) {
            EntityTransaction transaction = entityManager.getTransaction();
            if (transaction.isActive()) {
                transaction.commit();
            }
            transactional = false;
        }
    }

    private void rollbackTransaction() {
        if (transactional) {
            EntityTransaction transaction = entityManager.getTransaction();
            if (transaction.isActive()) {
                transaction.rollback();
            }
            transactional = false;
        }
    }

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
