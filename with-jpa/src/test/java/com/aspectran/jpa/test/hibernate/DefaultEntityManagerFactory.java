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
package com.aspectran.jpa.test.hibernate;

import com.aspectran.core.component.bean.annotation.Autowired;
import com.aspectran.core.component.bean.annotation.Bean;
import com.aspectran.core.component.bean.annotation.Component;
import com.aspectran.core.component.bean.annotation.Initialize;
import com.aspectran.jpa.EntityManagerFactoryBean;
import jakarta.persistence.PersistenceConfiguration;
import jakarta.persistence.PersistenceUnitTransactionType;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.JdbcSettings;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.hibernate.tool.schema.Action;

import javax.sql.DataSource;
import java.util.Map;

/**
 * <p>Created: 2025-05-02</p>
 */
@Component
@Bean(lazyDestroy = true)
public class DefaultEntityManagerFactory extends EntityManagerFactoryBean {

    private final DataSource dataSource;

    @Autowired
    public DefaultEntityManagerFactory(DataSource dataSource) {
        super("petclinic-test");
        this.dataSource = dataSource;
    }

    @Override
    protected void preConfigure(Map<String, Object> properties) {
        super.preConfigure(properties);
    }

    @Override
    protected void preConfigure(PersistenceConfiguration configuration) {
        super.preConfigure(configuration);
        configuration.provider(HibernatePersistenceProvider.class.getName());
        configuration.transactionType(PersistenceUnitTransactionType.RESOURCE_LOCAL);
        configuration.property(JdbcSettings.JAKARTA_NON_JTA_DATASOURCE, dataSource);
    }

    @Initialize(profile = "test")
    public void initInTestMode() {
        setProperty(AvailableSettings.HBM2DDL_AUTO, Action.ACTION_NONE);
    }

    @Initialize(profile = "!test")
    public void initInProdMode() {
        setProperty(AvailableSettings.HBM2DDL_AUTO, Action.ACTION_NONE);
    }

}
