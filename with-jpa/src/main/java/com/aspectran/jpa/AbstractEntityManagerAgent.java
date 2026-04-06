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

import com.aspectran.core.activity.HintParameters;
import com.aspectran.core.activity.InstantActivitySupport;
import com.aspectran.core.component.bean.annotation.Advisable;
import jakarta.persistence.CacheRetrieveMode;
import jakarta.persistence.CacheStoreMode;
import jakarta.persistence.ConnectionConsumer;
import jakarta.persistence.ConnectionFunction;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
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
 * Base class for {@link EntityManager} agents that delegate calls to a context-bound
 * EntityManager while applying Aspectran AOP semantics.
 */
public abstract class AbstractEntityManagerAgent extends InstantActivitySupport
        implements EntityManager, EntityManagerProvider {

    @Advisable
    @Override
    public void persist(Object entity) {
        assertNotReadOnly();
        getEntityManagerAdvice().transactional();
        getEntityManager().persist(entity);
    }

    @Advisable
    @Override
    public <T> T merge(T entity) {
        assertNotReadOnly();
        getEntityManagerAdvice().transactional();
        return getEntityManager().merge(entity);
    }

    @Advisable
    @Override
    public void remove(Object entity) {
        assertNotReadOnly();
        getEntityManagerAdvice().transactional();
        getEntityManager().remove(entity);
    }

    @Advisable
    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey) {
        return getEntityManager().find(entityClass, primaryKey);
    }

    @Advisable
    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties) {
        return getEntityManager().find(entityClass, primaryKey, properties);
    }

    @Advisable
    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode) {
        return getEntityManager().find(entityClass, primaryKey, lockMode);
    }

    @Advisable
    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode, Map<String, Object> properties) {
        return getEntityManager().find(entityClass, primaryKey, lockMode, properties);
    }

    @Advisable
    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, FindOption... options) {
        return getEntityManager().find(entityClass, primaryKey, options);
    }

    @Advisable
    @Override
    public <T> T find(EntityGraph<T> entityGraph, Object primaryKey, FindOption... options) {
        return getEntityManager().find(entityGraph, primaryKey, options);
    }

    @Advisable
    @Override
    public <T> T getReference(Class<T> entityClass, Object primaryKey) {
        return getEntityManager().getReference(entityClass, primaryKey);
    }

    @Advisable
    @Override
    public <T> T getReference(T entity) {
        return getEntityManager().getReference(entity);
    }

    @Advisable
    @Override
    public void flush() {
        getEntityManager().flush();
    }

    @Advisable
    @Override
    public void setFlushMode(FlushModeType flushMode) {
        getEntityManager().setFlushMode(flushMode);
    }

    @Advisable
    @Override
    public FlushModeType getFlushMode() {
        return getEntityManager().getFlushMode();
    }

    @Advisable
    @Override
    public void lock(Object entity, LockModeType lockMode) {
        assertNotReadOnly();
        getEntityManagerAdvice().transactional();
        getEntityManager().lock(entity, lockMode);
    }

    @Advisable
    @Override
    public void lock(Object entity, LockModeType lockMode, Map<String, Object> properties) {
        assertNotReadOnly();
        getEntityManagerAdvice().transactional();
        getEntityManager().lock(entity, lockMode, properties);
    }

    @Advisable
    @Override
    public void lock(Object entity, LockModeType lockMode, LockOption... options) {
        assertNotReadOnly();
        getEntityManagerAdvice().transactional();
        getEntityManager().lock(entity, lockMode, options);
    }

    @Advisable
    @Override
    public void refresh(Object entity) {
        assertNotReadOnly();
        getEntityManagerAdvice().transactional();
        getEntityManager().refresh(entity);
    }

    @Advisable
    @Override
    public void refresh(Object entity, Map<String, Object> properties) {
        assertNotReadOnly();
        getEntityManagerAdvice().transactional();
        getEntityManager().refresh(entity, properties);
    }

    @Advisable
    @Override
    public void refresh(Object entity, LockModeType lockMode) {
        assertNotReadOnly();
        getEntityManagerAdvice().transactional();
        getEntityManager().refresh(entity, lockMode);
    }

    @Advisable
    @Override
    public void refresh(Object entity, LockModeType lockMode, Map<String, Object> properties) {
        assertNotReadOnly();
        getEntityManagerAdvice().transactional();
        getEntityManager().refresh(entity, lockMode, properties);
    }

    @Advisable
    @Override
    public void refresh(Object entity, RefreshOption... options) {
        assertNotReadOnly();
        getEntityManagerAdvice().transactional();
        getEntityManager().refresh(entity, options);
    }

    @Advisable
    @Override
    public void clear() {
        getEntityManager().clear();
    }

    @Advisable
    @Override
    public void detach(Object entity) {
        getEntityManager().detach(entity);
    }

    @Advisable
    @Override
    public boolean contains(Object entity) {
        return getEntityManager().contains(entity);
    }

    @Advisable
    @Override
    public LockModeType getLockMode(Object entity) {
        getEntityManagerAdvice().transactional();
        return getEntityManager().getLockMode(entity);
    }

    @Advisable
    @Override
    public void setCacheRetrieveMode(CacheRetrieveMode cacheRetrieveMode) {
        getEntityManager().setCacheRetrieveMode(cacheRetrieveMode);
    }

    @Advisable
    @Override
    public void setCacheStoreMode(CacheStoreMode cacheStoreMode) {
        getEntityManager().setCacheStoreMode(cacheStoreMode);
    }

    @Advisable
    @Override
    public CacheRetrieveMode getCacheRetrieveMode() {
        return getEntityManager().getCacheRetrieveMode();
    }

    @Advisable
    @Override
    public CacheStoreMode getCacheStoreMode() {
        return getEntityManager().getCacheStoreMode();
    }

    @Advisable
    @Override
    public void setProperty(String propertyName, Object value) {
        getEntityManager().setProperty(propertyName, value);
    }

    @Advisable
    @Override
    public Map<String, Object> getProperties() {
        return getEntityManager().getProperties();
    }

    @Advisable
    @Override
    public Query createQuery(String qlString) {
        getEntityManagerAdvice().transactional();
        return getEntityManager().createQuery(qlString);
    }

    @Advisable
    @Override
    public <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery) {
        getEntityManagerAdvice().transactional();
        return getEntityManager().createQuery(criteriaQuery);
    }

    @Advisable
    @Override
    public <T> TypedQuery<T> createQuery(CriteriaSelect<T> selectQuery) {
        getEntityManagerAdvice().transactional();
        return getEntityManager().createQuery(selectQuery);
    }

    @Advisable
    @Override
    public Query createQuery(CriteriaUpdate updateQuery) {
        assertNotReadOnly();
        getEntityManagerAdvice().transactional();
        return getEntityManager().createQuery(updateQuery);
    }

    @Advisable
    @Override
    public Query createQuery(CriteriaDelete deleteQuery) {
        assertNotReadOnly();
        getEntityManagerAdvice().transactional();
        return getEntityManager().createQuery(deleteQuery);
    }

    @Advisable
    @Override
    public <T> TypedQuery<T> createQuery(String qlString, Class<T> resultClass) {
        getEntityManagerAdvice().transactional();
        return getEntityManager().createQuery(qlString, resultClass);
    }

    @Advisable
    @Override
    public Query createNamedQuery(String name) {
        getEntityManagerAdvice().transactional();
        return getEntityManager().createNamedQuery(name);
    }

    @Advisable
    @Override
    public <T> TypedQuery<T> createNamedQuery(String name, Class<T> resultClass) {
        getEntityManagerAdvice().transactional();
        return getEntityManager().createNamedQuery(name, resultClass);
    }

    @Advisable
    @Override
    public <T> TypedQuery<T> createQuery(TypedQueryReference<T> reference) {
        getEntityManagerAdvice().transactional();
        return getEntityManager().createQuery(reference);
    }

    @Advisable
    @Override
    public Query createNativeQuery(String sqlString) {
        getEntityManagerAdvice().transactional();
        return getEntityManager().createNativeQuery(sqlString);
    }

    @Advisable
    @Override
    public Query createNativeQuery(String sqlString, Class resultClass) {
        getEntityManagerAdvice().transactional();
        return getEntityManager().createNativeQuery(sqlString, resultClass);
    }

    @Advisable
    @Override
    public Query createNativeQuery(String sqlString, String resultSetMapping) {
        getEntityManagerAdvice().transactional();
        return getEntityManager().createNativeQuery(sqlString, resultSetMapping);
    }

    @Advisable
    @Override
    public StoredProcedureQuery createNamedStoredProcedureQuery(String name) {
        getEntityManagerAdvice().transactional();
        return getEntityManager().createNamedStoredProcedureQuery(name);
    }

    @Advisable
    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String procedureName) {
        getEntityManagerAdvice().transactional();
        return getEntityManager().createStoredProcedureQuery(procedureName);
    }

    @Advisable
    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String procedureName, Class... resultClasses) {
        getEntityManagerAdvice().transactional();
        return getEntityManager().createStoredProcedureQuery(procedureName, resultClasses);
    }

    @Advisable
    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String procedureName, String... resultSetMappings) {
        getEntityManagerAdvice().transactional();
        return getEntityManager().createStoredProcedureQuery(procedureName, resultSetMappings);
    }

    @Advisable
    @Override
    public void joinTransaction() {
        assertNotReadOnly();
        getEntityManagerAdvice().transactional();
        getEntityManager().joinTransaction();
    }

    @Advisable
    @Override
    public boolean isJoinedToTransaction() {
        return getEntityManager().isJoinedToTransaction();
    }

    @Advisable
    @Override
    public <T> T unwrap(Class<T> cls) {
        return getEntityManager().unwrap(cls);
    }

    @Advisable
    @Override
    public Object getDelegate() {
        return getEntityManager().getDelegate();
    }

    @Advisable
    @Override
    public void close() {
        getEntityManager().close();
    }

    @Advisable
    @Override
    public boolean isOpen() {
        return getEntityManager().isOpen();
    }

    @Advisable
    @Override
    public EntityTransaction getTransaction() {
        return getEntityManager().getTransaction();
    }

    @Advisable
    @Override
    public CriteriaBuilder getCriteriaBuilder() {
        return getEntityManager().getCriteriaBuilder();
    }

    @Advisable
    @Override
    public Metamodel getMetamodel() {
        return getEntityManager().getMetamodel();
    }

    @Advisable
    @Override
    public <T> EntityGraph<T> createEntityGraph(Class<T> rootType) {
        return getEntityManager().createEntityGraph(rootType);
    }

    @Advisable
    @Override
    public EntityGraph<?> createEntityGraph(String graphName) {
        return getEntityManager().createEntityGraph(graphName);
    }

    @Advisable
    @Override
    public EntityGraph<?> getEntityGraph(String graphName) {
        return getEntityManager().getEntityGraph(graphName);
    }

    @Advisable
    @Override
    public <T> List<EntityGraph<? super T>> getEntityGraphs(Class<T> entityClass) {
        return getEntityManager().getEntityGraphs(entityClass);
    }

    @Advisable
    @Override
    public <C> void runWithConnection(ConnectionConsumer<C> action) {
        getEntityManager().runWithConnection(action);
    }

    @Advisable
    @Override
    public <C, T> T callWithConnection(ConnectionFunction<C, T> function) {
        return getEntityManager().callWithConnection(function);
    }

    /**
     * Asserts that the current transactional context is not read-only.
     * <p>If a {@code @Hint(type = "transactional", value = "readOnly: true")} is present
     * in the current activity, throws an {@link IllegalStateException} to prevent
     * data modification operations from executing.</p>
     * @throws IllegalStateException if the context is read-only
     */
    protected void assertNotReadOnly() {
        HintParameters hint = getAvailableActivity().peekHint("transactional");
        if (hint != null && hint.getBoolean("readOnly", false)) {
            throw new IllegalStateException("Data modification operations (persist, merge, remove, etc.) " +
                    "are not allowed within a read-only transactional hint.");
        }
    }

}
