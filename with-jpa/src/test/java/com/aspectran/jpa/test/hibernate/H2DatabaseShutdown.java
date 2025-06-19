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
import com.aspectran.core.component.bean.annotation.Destroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Shutdown H2 database programmatically.
 * <p>Created: 2025. 2. 15.</p>
 */
@Component
@Bean(lazyDestroy = true)
public final class H2DatabaseShutdown {

    private static final Logger logger = LoggerFactory.getLogger(H2DatabaseShutdown.class);

    private final DataSource dataSource;

    @Autowired(required = false)
    public H2DatabaseShutdown(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Destroy(profile = "h2")
    public void shutdown() {
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
