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

import com.aspectran.core.activity.InstantActivitySupport;
import com.aspectran.core.component.bean.ablility.DisposableBean;
import com.aspectran.core.component.bean.ablility.InitializableFactoryBean;
import com.aspectran.core.component.bean.scan.BeanClassScanner;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * An Aspectran {@link com.aspectran.core.component.bean.ablility.FactoryBean} that creates
 * a Jakarta Persistence {@link EntityManagerFactory}. This class provides a convenient way to
 * configure and instantiate an {@code EntityManagerFactory} as an Aspectran bean.
 * <p>
 * The factory is configured using properties set on this bean, and it automatically scans
 * for classes annotated with {@link jakarta.persistence.Entity} within the application's
 * base packages, adding them to the persistence unit.
 * </p>
 * <p>
 * For advanced customization, subclasses can override the following methods:
 * </p>
 * <ul>
 *   <li>{@link #configuration()}: Provide a custom {@link PersistenceConfiguration} instance.</li>
 *   <li>{@link #preConfigure(Map)}: Modify persistence properties before they are applied.</li>
 *   <li>{@link #preConfigure(PersistenceConfiguration)}: Further customize the configuration object.</li>
 *   <li>{@link #postConfigure(EntityManagerFactory)}: Perform actions after the factory is created.</li>
 * </ul>
 *
 * <p>Created: 2025-04-24</p>
 * @see Persistence#createEntityManagerFactory(String, Map)
 * @see PersistenceConfiguration
 */
public class EntityManagerFactoryBean extends InstantActivitySupport
        implements InitializableFactoryBean<EntityManagerFactory>, DisposableBean {

    private final String persistenceUnitName;

    private Map<String, Object> properties;

    private EntityManagerFactory entityManagerFactory;

    /**
     * Instantiates a new {@code EntityManagerFactoryBean}.
     * @param persistenceUnitName the name of the persistence unit
     */
    public EntityManagerFactoryBean(String persistenceUnitName) {
        this.persistenceUnitName = persistenceUnitName;
    }

    /**
     * Returns the name of the persistence unit.
     * @return the persistence unit name
     */
    public String getPersistenceUnitName() {
        return persistenceUnitName;
    }

    /**
     * Sets the properties to be passed to the persistence provider.
     * @param properties a map of persistence properties
     */
    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    /**
     * Sets a single property to be passed to the persistence provider.
     * @param name the name of the property
     * @param value the value of the property
     */
    public void setProperty(String name, Object value) {
        if (properties == null) {
            properties = new HashMap<>();
        }
        properties.put(name, value);
    }

    /**
     * Provides a custom {@link PersistenceConfiguration} instance.
     * Subclasses can override this to take full control over the configuration process.
     * @return a {@link PersistenceConfiguration} instance, or {@code null} to use the default
     */
    protected PersistenceConfiguration configuration() {
        return null;
    }

    /**
     * A hook for modifying the persistence properties before the {@link EntityManagerFactory} is created.
     * Subclasses can override this to add, remove, or change properties.
     * @param properties the mutable map of properties to be passed to the persistence provider
     */
    protected void preConfigure(Map<String, Object> properties) {
    }

    /**
     * A hook for customizing the {@link PersistenceConfiguration} before the {@link EntityManagerFactory} is created.
     * @param configuration the {@code PersistenceConfiguration} to be customized
     */
    protected void preConfigure(PersistenceConfiguration configuration) {
    }

    /**
     * A hook for performing actions after the {@link EntityManagerFactory} has been created.
     * Subclasses can override this to implement custom initialization logic.
     * @param entityManagerFactory the newly created {@link EntityManagerFactory}
     */
    protected void postConfigure(EntityManagerFactory entityManagerFactory) {
    }

    /**
     * {@inheritDoc}
     * <p>This method orchestrates the creation of the {@link EntityManagerFactory}, including
     * applying properties, scanning for entity classes, and invoking customization hooks.</p>
     */
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

    /**
     * {@inheritDoc}
     * @return the created {@link EntityManagerFactory} instance.
     */
    @Override
    public EntityManagerFactory getObject() throws Exception {
        return entityManagerFactory;
    }

    /**
     * {@inheritDoc}
     * <p>Closes the {@link EntityManagerFactory} to release its resources.</p>
     */
    @Override
    public void destroy() throws Exception {
        if (entityManagerFactory != null) {
            entityManagerFactory.close();
            entityManagerFactory = null;
        }
    }

    /**
     * Scans the configured base packages for classes annotated with {@link Entity}
     * and adds them to the persistence configuration as managed classes.
     * @param configuration the persistence configuration to update
     */
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
