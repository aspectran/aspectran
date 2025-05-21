package com.aspectran.jpa;

import com.aspectran.core.activity.InstantActivitySupport;
import com.aspectran.core.component.bean.ablility.DisposableBean;
import com.aspectran.core.component.bean.ablility.InitializableFactoryBean;
import com.aspectran.core.component.bean.scan.BeanClassScanner;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * <p>Created: 2025-04-24</p>
 */
public class EntityManagerFactoryBean extends InstantActivitySupport
        implements InitializableFactoryBean<EntityManagerFactory>, DisposableBean {

    private final String persistenceUnitName;

    private Map<String, Object> properties;

    private EntityManagerFactory entityManagerFactory;

    public EntityManagerFactoryBean(String persistenceUnitName) {
        this.persistenceUnitName = persistenceUnitName;
    }

    public String getPersistenceUnitName() {
        return persistenceUnitName;
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

    protected void preConfigure(PersistenceConfiguration configuration) {
    }

    protected void postConfigure(EntityManagerFactory entityManagerFactory) {
    }

    @Override
    public void initialize() throws Exception {
        if (entityManagerFactory == null) {
            PersistenceConfiguration configuration = configuration();
            if (configuration == null) {
                configuration = new PersistenceConfiguration(persistenceUnitName);
            }

            Map<String, Object> propertiesToUse = new HashMap<>();
            if (properties != null) {
                propertiesToUse.putAll(properties);
                properties = null;
            }
            preConfigure(propertiesToUse);
            configuration.properties(propertiesToUse);

            // Find all managed classes
            scanManagedClasses(configuration);

            preConfigure(configuration);

            entityManagerFactory = configuration.createEntityManagerFactory();
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

    private void scanManagedClasses(PersistenceConfiguration configuration) {
        Set<String> basePackages = getBeanRegistry().getBasePackages();
        if (!basePackages.isEmpty()) {
            for (String basePackage : basePackages) {
                BeanClassScanner scanner = new BeanClassScanner(getActivityContext().getClassLoader());
                scanner.scan(basePackage + ".**", (resourceName, targetClass) -> {
                    if (targetClass.isAnnotationPresent(Entity.class)) {
                        configuration.managedClass(targetClass);
                    }
                });
            }
        }
    }

}
