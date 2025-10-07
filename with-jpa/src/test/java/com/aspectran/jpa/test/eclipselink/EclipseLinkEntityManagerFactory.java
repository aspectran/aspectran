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
package com.aspectran.jpa.test.eclipselink;

import com.aspectran.core.component.bean.annotation.Autowired;
import com.aspectran.core.component.bean.annotation.Bean;
import com.aspectran.core.component.bean.annotation.Component;
import com.aspectran.core.component.bean.annotation.Initialize;
import com.aspectran.core.component.bean.annotation.Profile;
import com.aspectran.jpa.EntityManagerFactoryBean;
import com.aspectran.jpa.eclipselink.logging.Slf4jSessionLogger;
import jakarta.persistence.PersistenceConfiguration;
import jakarta.persistence.PersistenceUnitTransactionType;
import org.eclipse.persistence.config.PersistenceUnitProperties;

import javax.sql.DataSource;

/**
 * <p>Created: 2025-10-07</p>
 */
@Component
@Profile("eclipselink")
@Bean(lazyDestroy = true)
public class EclipseLinkEntityManagerFactory extends EntityManagerFactoryBean {

    private final DataSource dataSource;

    @Autowired
    public EclipseLinkEntityManagerFactory(DataSource dataSource) {
        super("petclinic-test");
        this.dataSource = dataSource;
    }

    @Override
    protected void preConfigure(PersistenceConfiguration configuration) {
        super.preConfigure(configuration);
        configuration.provider("org.eclipse.persistence.jpa.PersistenceProvider");
        configuration.transactionType(PersistenceUnitTransactionType.RESOURCE_LOCAL);
        configuration.property(PersistenceUnitProperties.NON_JTA_DATASOURCE, dataSource);
        configuration.property(PersistenceUnitProperties.LOGGING_LOGGER, Slf4jSessionLogger.class.getName());
        configuration.property(PersistenceUnitProperties.LOGGING_LEVEL, "FINE");
        configuration.property(PersistenceUnitProperties.LOGGING_PARAMETERS, "true");
    }

    @Initialize(profile = "test")
    public void initInTestMode() {
        setProperty(PersistenceUnitProperties.DDL_GENERATION, PersistenceUnitProperties.NONE);
    }

    @Initialize(profile = "!test")
    public void initInProdMode() {
        setProperty(PersistenceUnitProperties.DDL_GENERATION, PersistenceUnitProperties.NONE);
    }

}
