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
package com.aspectran.jpa.common;

import com.aspectran.core.component.bean.annotation.Autowired;
import com.aspectran.core.component.bean.annotation.Bean;
import com.aspectran.core.component.bean.annotation.Component;
import com.aspectran.core.component.bean.annotation.Destroy;
import com.aspectran.core.component.bean.annotation.Initialize;
import com.aspectran.core.component.bean.annotation.Profile;
import com.aspectran.core.component.bean.annotation.Qualifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manage H2 database lifecycle.
 * <p>Created: 2025. 2. 15.</p>
 */
@Component
@Profile("h2")
@Bean(lazyDestroy = true)
public final class H2DatabaseManager {

    private static final Logger logger = LoggerFactory.getLogger(H2DatabaseManager.class);

    private final DataSource dataSource;

    private final DataSource readOnlyDataSource;

    @Autowired(required = false)
    public H2DatabaseManager(
            @Qualifier("dataSource") DataSource dataSource,
            @Qualifier("readOnlyDataSource") DataSource readOnlyDataSource) {
        this.dataSource = dataSource;
        this.readOnlyDataSource = readOnlyDataSource;
    }

    @Initialize
    public void initialize() {
        if (dataSource != null) {
            try (Connection connection = dataSource.getConnection();
                 Statement statement = connection.createStatement()) {
                if (connection.getMetaData().getDatabaseProductName().equals("H2")) {
                    logger.info("Initializing H2 database");
                    statement.execute("RUNSCRIPT FROM 'classpath:com/aspectran/jpa/common/db/h2/petclinic-creation.sql'");
                    logger.info("H2 database initialized successfully");
                }
            } catch (SQLException e) {
                logger.error("Failed to initialize H2 database", e);
                throw new RuntimeException(e);
            }
        }
    }

    @Destroy
    public void shutdown() {
        shutdownH2(dataSource);
        shutdownH2(readOnlyDataSource);
    }

    private void shutdownH2(DataSource dataSource) {
        if (dataSource != null) {
            try (Connection connection = dataSource.getConnection()) {
                if (connection.getMetaData().getDatabaseProductName().equals("H2")) {
                    logger.info("Shutting down H2 database");
                    connection.createStatement().execute("SHUTDOWN");
                } else {
                    logger.info("Not shutting down non-H2 database");
                }
            } catch (SQLException e) {
                if (!e.getMessage().contains("Database is already closed")) {
                    logger.error("Failed to shutdown H2 database", e);
                }
            }
        }
    }

}
