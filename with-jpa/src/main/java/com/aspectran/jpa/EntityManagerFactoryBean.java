package com.aspectran.jpa;

import com.aspectran.core.component.bean.ablility.DisposableBean;
import com.aspectran.core.component.bean.ablility.InitializableFactoryBean;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceConfiguration;

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

    public void setProperty(String name, Object value) {
        if (properties == null) {
            properties = new HashMap<>();
        }
        properties.put(name, value);
    }

    protected PersistenceConfiguration configuration() {
        return null;
    }

    protected void preConfigure(Map<String, Object> properties) {
    }

    protected void preConfigure(PersistenceConfiguration persistenceConfiguration) {
    }

    protected void postConfigure(EntityManagerFactory entityManagerFactory) {
    }

    @Override
    public void initialize() throws Exception {
        if (entityManagerFactory == null) {
            PersistenceConfiguration persistenceConfiguration = configuration();
            if (persistenceConfiguration == null) {
                persistenceConfiguration = new PersistenceConfiguration(persistenceUnitName);
            }

            Map<String, Object> propertiesToUse = new HashMap<>();
            if (properties != null) {
                propertiesToUse.putAll(properties);
                properties = null;
            }
            preConfigure(propertiesToUse);
            persistenceConfiguration.properties(propertiesToUse);

            preConfigure(persistenceConfiguration);

            entityManagerFactory = persistenceConfiguration.createEntityManagerFactory();
            postConfigure(entityManagerFactory);
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
