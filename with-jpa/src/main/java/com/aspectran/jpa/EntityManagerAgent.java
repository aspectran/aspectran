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

import com.aspectran.core.component.bean.annotation.Advisable;
import jakarta.persistence.CacheRetrieveMode;
import jakarta.persistence.CacheStoreMode;
import jakarta.persistence.ConnectionConsumer;
import jakarta.persistence.ConnectionFunction;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.FindOption;
import jakarta.persistence.FlushModeType;
import jakarta.persistence.LockModeType;
import jakarta.persistence.LockOption;
import jakarta.persistence.Query;
import jakarta.persistence.RefreshOption;
import jakarta.persistence.StoredProcedureQuery;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.TypedQueryReference;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CriteriaSelect;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.metamodel.Metamodel;

import java.util.List;
import java.util.Map;

/**
 * A proxy for {@link EntityManager} that automatically manages transactions for data modification operations.
 * This agent extends {@link EntityManagerProvider} to access a context-bound {@code EntityManager}
 * and uses {@link EntityManagerAdvice} to control transactional boundaries.
 * <p>
 * When methods that mutate the persistence context (e.g., {@link #persist(Object)}, {@link #merge(Object)},
 * {@link #remove(Object)}) are called, this agent ensures that a transaction is active by invoking
 * {@link EntityManagerAdvice#transactional()}. For read-only operations, it delegates directly to the
 * underlying {@code EntityManager} without initiating a new transaction.
 * </p>
 * <p>All methods are marked as {@link Advisable} to integrate with Aspectran's AOP context.</p>
 *
 * <p>Created: 2025-04-24</p>
 */
public class EntityManagerAgent extends EntityManagerProvider implements EntityManager {

    /**
     * Instantiates a new {@code EntityManagerAgent}.
     * @param relevantAspectId the ID of the aspect that manages the {@link EntityManagerAdvice}
     */
    public EntityManagerAgent(String relevantAspectId) {
        super(relevantAspectId);
    }

    /**
     * {@inheritDoc}
     * <p>This operation is transactional and will begin a new transaction if one is not already active.</p>
     */
    @Advisable
    @Override
    public void persist(Object entity) {
        getEntityManagerAdvice().transactional();
        getEntityManager().persist(entity);
    }

    /**
     * {@inheritDoc}
     * <p>This operation is transactional and will begin a new transaction if one is not already active.</p>
     */
    @Advisable
    @Override
    public <T> T merge(T entity) {
        getEntityManagerAdvice().transactional();
        return getEntityManager().merge(entity);
    }

    /**
     * {@inheritDoc}
     * <p>This operation is transactional and will begin a new transaction if one is not already active.</p>
     */
    @Advisable
    @Override
    public void remove(Object entity) {
        getEntityManagerAdvice().transactional();
        getEntityManager().remove(entity);
    }

    /**
     * {@inheritDoc}
     */
    @Advisable
    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey) {
        return getEntityManager().find(entityClass, primaryKey);
    }

    /**
     * {@inheritDoc}
     */
    @Advisable
    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties) {
        return getEntityManager().find(entityClass, primaryKey, properties);
    }

    /**
     * {@inheritDoc}
     */
    @Advisable
    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode) {
        return getEntityManager().find(entityClass, primaryKey, lockMode);
    }

    /**
     * {@inheritDoc}
     */
    @Advisable
    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode, Map<String, Object> properties) {
        return getEntityManager().find(entityClass, primaryKey, lockMode, properties);
    }

    /**
     * {@inheritDoc}
     */
    @Advisable
    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, FindOption... options) {
        return getEntityManager().find(entityClass, primaryKey, options);
    }

    /**
     * {@inheritDoc}
     */
    @Advisable
    @Override
    public <T> T find(EntityGraph<T> entityGraph, Object primaryKey, FindOption... options) {
        return getEntityManager().find(entityGraph, primaryKey, options);
    }

    /**
     * {@inheritDoc}
     */
    @Advisable
    @Override
    public <T> T getReference(Class<T> entityClass, Object primaryKey) {
        return getEntityManager().getReference(entityClass, primaryKey);
    }

    /**
     * {@inheritDoc}
     */
    @Advisable
    @Override
    public <T> T getReference(T entity) {
        return getEntityManager().getReference(entity);
    }

    /**
     * {@inheritDoc}
     */
    @Advisable
    @Override
    public void flush() {
        getEntityManager().flush();
    }

    /**
     * {@inheritDoc}
     */
    @Advisable
    @Override
    public void setFlushMode(FlushModeType flushMode) {
        getEntityManager().setFlushMode(flushMode);
    }

    /**
     * {@inheritDoc}
     */
    @Advisable
    @Override
    public FlushModeType getFlushMode() {
        return getEntityManager().getFlushMode();
    }

    /**
     * {@inheritDoc}
     * <p>This operation is transactional and will begin a new transaction if one is not already active.</p>
     */
    @Advisable
    @Override
    public void lock(Object entity, LockModeType lockMode) {
        getEntityManagerAdvice().transactional();
        getEntityManager().lock(entity, lockMode);
    }

    /**
     * {@inheritDoc}
     * <p>This operation is transactional and will begin a new transaction if one is not already active.</p>
     */
    @Advisable
    @Override
    public void lock(Object entity, LockModeType lockMode, Map<String, Object> properties) {
        getEntityManagerAdvice().transactional();
        getEntityManager().lock(entity, lockMode, properties);
    }

    /**
     * {@inheritDoc}
     * <p>This operation is transactional and will begin a new transaction if one is not already active.</p>
     */
    @Advisable
    @Override
    public void lock(Object entity, LockModeType lockMode, LockOption... options) {
        getEntityManagerAdvice().transactional();
        getEntityManager().lock(entity, lockMode, options);
    }

    /**
     * {@inheritDoc}
     * <p>This operation is transactional and will begin a new transaction if one is not already active.</p>
     */
    @Advisable
    @Override
    public void refresh(Object entity) {
        getEntityManagerAdvice().transactional();
        getEntityManager().refresh(entity);
    }

    /**
     * {@inheritDoc}
     * <p>This operation is transactional and will begin a new transaction if one is not already active.</p>
     */
    @Advisable
    @Override
    public void refresh(Object entity, Map<String, Object> properties) {
        getEntityManagerAdvice().transactional();
        getEntityManager().refresh(entity, properties);
    }

    /**
     * {@inheritDoc}
     * <p>This operation is transactional and will begin a new transaction if one is not already active.</p>
     */
    @Advisable
    @Override
    public void refresh(Object entity, LockModeType lockMode) {
        getEntityManagerAdvice().transactional();
        getEntityManager().refresh(entity, lockMode);
    }

    /**
     * {@inheritDoc}
     * <p>This operation is transactional and will begin a new transaction if one is not already active.</p>
     */
    @Advisable
    @Override
    public void refresh(Object entity, LockModeType lockMode, Map<String, Object> properties) {
        getEntityManagerAdvice().transactional();
        getEntityManager().refresh(entity, lockMode, properties);
    }

    /**
     * {@inheritDoc}
     * <p>This operation is transactional and will begin a new transaction if one is not already active.</p>
     */
    @Advisable
    @Override
    public void refresh(Object entity, RefreshOption... options) {
        getEntityManagerAdvice().transactional();
        getEntityManager().refresh(entity, options);
    }

    /**
     * {@inheritDoc}
     */
    @Advisable
    @Override
    public void clear() {
        getEntityManager().clear();
    }

    /**
     * {@inheritDoc}
     */
    @Advisable
    @Override
    public void detach(Object entity) {
        getEntityManager().detach(entity);
    }

    /**
     * {@inheritDoc}
     */
    @Advisable
    @Override
    public boolean contains(Object entity) {
        return getEntityManager().contains(entity);
    }

    /**
     * {@inheritDoc}
     * <p>This operation is transactional and will begin a new transaction if one is not already active.</p>
     */
    @Advisable
    @Override
    public LockModeType getLockMode(Object entity) {
        getEntityManagerAdvice().transactional();
        return getEntityManager().getLockMode(entity);
    }

    /**
     * {@inheritDoc}
     */
    @Advisable
    @Override
    public void setCacheRetrieveMode(CacheRetrieveMode cacheRetrieveMode) {
        getEntityManager().setCacheRetrieveMode(cacheRetrieveMode);
    }

    /**
     * {@inheritDoc}
     */
    @Advisable
    @Override
    public void setCacheStoreMode(CacheStoreMode cacheStoreMode) {
        getEntityManager().setCacheStoreMode(cacheStoreMode);
    }

    /**
     * {@inheritDoc}
     */
    @Advisable
    @Override
    public CacheRetrieveMode getCacheRetrieveMode() {
        return getEntityManager().getCacheRetrieveMode();
    }

    /**
     * {@inheritDoc}
     */
    @Advisable
    @Override
    public CacheStoreMode getCacheStoreMode() {
        return getEntityManager().getCacheStoreMode();
    }

    /**
     * {@inheritDoc}
     */
    @Advisable
    @Override
    public void setProperty(String propertyName, Object value) {
        getEntityManager().setProperty(propertyName, value);
    }

    /**
     * {@inheritDoc}
     */
    @Advisable
    @Override
    public Map<String, Object> getProperties() {
        return getEntityManager().getProperties();
    }

    /**
     * {@inheritDoc}
     */
    @Advisable
    @Override
    public Query createQuery(String qlString) {
        return getEntityManager().createQuery(qlString);
    }

    /**
     * {@inheritDoc}
     */
    @Advisable
    @Override
    public <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery) {
        return getEntityManager().createQuery(criteriaQuery);
    }

    /**
     * {@inheritDoc}
     */
    @Advisable
    @Override
    public <T> TypedQuery<T> createQuery(CriteriaSelect<T> selectQuery) {
        return getEntityManager().createQuery(selectQuery);
    }

    /**
     * {@inheritDoc}
     * <p>This operation is transactional and will begin a new transaction if one is not already active.</p>
     */
    @Advisable
    @Override
    public Query createQuery(CriteriaUpdate updateQuery) {
        getEntityManagerAdvice().transactional();
        return getEntityManager().createQuery(updateQuery);
    }

    /**
     * {@inheritDoc}
     * <p>This operation is transactional and will begin a new transaction if one is not already active.</p>
     */
    @Advisable
    @Override
    public Query createQuery(CriteriaDelete deleteQuery) {
        getEntityManagerAdvice().transactional();
        return getEntityManager().createQuery(deleteQuery);
    }

    /**
     * {@inheritDoc}
     */
    @Advisable
    @Override
    public <T> TypedQuery<T> createQuery(String qlString, Class<T> resultClass) {
        return getEntityManager().createQuery(qlString, resultClass);
    }

    /**
     * {@inheritDoc}
     */
    @Advisable
    @Override
    public Query createNamedQuery(String name) {
        return getEntityManager().createNamedQuery(name);
    }

    /**
     * {@inheritDoc}
     */
    @Advisable
    @Override
    public <T> TypedQuery<T> createNamedQuery(String name, Class<T> resultClass) {
        return getEntityManager().createNamedQuery(name, resultClass);
    }

    /**
     * {@inheritDoc}
     */
    @Advisable
    @Override
    public <T> TypedQuery<T> createQuery(TypedQueryReference<T> reference) {
        return getEntityManager().createQuery(reference);
    }

    /**
     * {@inheritDoc}
     */
    @Advisable
    @Override
    public Query createNativeQuery(String sqlString) {
        return getEntityManager().createNativeQuery(sqlString);
    }

    /**
     * {@inheritDoc}
     */
    @Advisable
    @Override
    public Query createNativeQuery(String sqlString, Class resultClass) {
        return getEntityManager().createNativeQuery(sqlString, resultClass);
    }

    /**
     * {@inheritDoc}
     */
    @Advisable
    @Override
    public Query createNativeQuery(String sqlString, String resultSetMapping) {
        return getEntityManager().createNativeQuery(sqlString, resultSetMapping);
    }

    /**
     * {@inheritDoc}
     */
    @Advisable
    @Override
    public StoredProcedureQuery createNamedStoredProcedureQuery(String name) {
        return getEntityManager().createNamedStoredProcedureQuery(name);
    }

    /**
     * {@inheritDoc}
     */
    @Advisable
    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String procedureName) {
        return getEntityManager().createStoredProcedureQuery(procedureName);
    }

    /**
     * {@inheritDoc}
     */
    @Advisable
    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String procedureName, Class... resultClasses) {
        return getEntityManager().createStoredProcedureQuery(procedureName, resultClasses);
    }

    /**
     * {@inheritDoc}
     */
    @Advisable
    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String procedureName, String... resultSetMappings) {
        return getEntityManager().createStoredProcedureQuery(procedureName, resultSetMappings);
    }

    /**
     * {@inheritDoc}
     * <p>This operation is transactional and will begin a new transaction if one is not already active.</p>
     */
    @Advisable
    @Override
    public void joinTransaction() {
        getEntityManagerAdvice().transactional();
        getEntityManager().joinTransaction();
    }

    /**
     * {@inheritDoc}
     */
    @Advisable
    @Override
    public boolean isJoinedToTransaction() {
        return getEntityManager().isJoinedToTransaction();
    }

    /**
     * {@inheritDoc}
     */
    @Advisable
    @Override
    public <T> T unwrap(Class<T> cls) {
        return getEntityManager().unwrap(cls);
    }

    /**
     * {@inheritDoc}
     */
    @Advisable
    @Override
    public Object getDelegate() {
        return getEntityManager().getDelegate();
    }

    /**
     * {@inheritDoc}
     */
    @Advisable
    @Override
    public void close() {
        getEntityManager().close();
    }

    /**
     * {@inheritDoc}
     */
    @Advisable
    @Override
    public boolean isOpen() {
        return getEntityManager().isOpen();
    }

    /**
     * {@inheritDoc}
     */
    @Advisable
    @Override
    public EntityTransaction getTransaction() {
        return getEntityManager().getTransaction();
    }

    /**
     * {@inheritDoc}
     */
    @Advisable
    @Override
    public EntityManagerFactory getEntityManagerFactory() {
        return getEntityManager().getEntityManagerFactory();
    }

    /**
     * {@inheritDoc}
     */
    @Advisable
    @Override
    public CriteriaBuilder getCriteriaBuilder() {
        return getEntityManager().getCriteriaBuilder();
    }

    /**
     * {@inheritDoc}
     */
    @Advisable
    @Override
    public Metamodel getMetamodel() {
        return getEntityManager().getMetamodel();
    }

    /**
     * {@inheritDoc}
     */
    @Advisable
    @Override
    public <T> EntityGraph<T> createEntityGraph(Class<T> rootType) {
        return getEntityManager().createEntityGraph(rootType);
    }

    /**
     * {@inheritDoc}
     */
    @Advisable
    @Override
    public EntityGraph<?> createEntityGraph(String graphName) {
        return getEntityManager().createEntityGraph(graphName);
    }

    /**
     * {@inheritDoc}
     */
    @Advisable
    @Override
    public EntityGraph<?> getEntityGraph(String graphName) {
        return getEntityManager().getEntityGraph(graphName);
    }

    /**
     * {@inheritDoc}
     */
    @Advisable
    @Override
    public <T> List<EntityGraph<? super T>> getEntityGraphs(Class<T> entityClass) {
        return getEntityManager().getEntityGraphs(entityClass);
    }

    /**
     * {@inheritDoc}
     */
    @Advisable
    @Override
    public <C> void runWithConnection(ConnectionConsumer<C> action) {
        getEntityManager().runWithConnection(action);
    }

    /**
     * {@inheritDoc}
     */
    @Advisable
    @Override
    public <C, T> T callWithConnection(ConnectionFunction<C, T> function) {
        return getEntityManager().callWithConnection(function);
    }

}
