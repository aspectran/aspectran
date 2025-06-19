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
