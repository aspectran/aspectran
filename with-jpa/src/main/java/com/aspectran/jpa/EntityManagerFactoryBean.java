package com.aspectran.jpa;

import com.aspectran.core.component.bean.ablility.DisposableBean;
import com.aspectran.core.component.bean.ablility.InitializableFactoryBean;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>Created: 2025-04-24</p>
 */
public class EntityManagerFactoryBean  implements InitializableFactoryBean<EntityManagerFactory>, DisposableBean {

    private final String persistenceUnitName;

    private Map<String, Object> properties;

    private EntityManagerFactory entityManagerFactory;

    public EntityManagerFactoryBean(String persistenceUnitName) {
        this.persistenceUnitName = persistenceUnitName;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    protected void configure(Map<String, Object> properties) {
    }

    @Override
    public void initialize() throws Exception {
        if (entityManagerFactory == null) {
            Map<String, Object> propertiesToUse = new HashMap<>();
            if (properties != null) {
                propertiesToUse.putAll(properties);
                properties = null;
            }
            configure(propertiesToUse);
            entityManagerFactory = Persistence.createEntityManagerFactory(persistenceUnitName, propertiesToUse);
        }
    }

    @Override
    public EntityManagerFactory getObject() throws Exception {
        return entityManagerFactory;
    }

    @Override
    public void destroy() throws Exception {
        if (entityManagerFactory != null) {
            entityManagerFactory.close();
            entityManagerFactory = null;
        }
    }

}
