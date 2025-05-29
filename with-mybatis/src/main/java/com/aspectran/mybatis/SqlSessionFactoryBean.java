/*
 * Copyright (c) 2008-2025 The Aspectran Project
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
package com.aspectran.mybatis;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.component.bean.ablility.InitializableFactoryBean;
import com.aspectran.core.component.bean.aware.ApplicationAdapterAware;
import com.aspectran.utils.Assert;
import com.aspectran.utils.ResourceUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import static com.aspectran.utils.ResourceUtils.CLASSPATH_URL_PREFIX;

/**
 * {@code FactoryBean} that creates an MyBatis {@code SqlSessionFactory}
 * using default MyBatis Configuration.
 */
public class SqlSessionFactoryBean implements ApplicationAdapterAware, InitializableFactoryBean<SqlSessionFactory> {

    private static final Logger logger = LoggerFactory.getLogger(SqlSessionFactoryBean.class);

    private ApplicationAdapter applicationAdapter;

    private Properties variables;

    private String environmentId;

    private Environment environment;

    private DatabaseIdProvider databaseIdProvider;

    private String configLocation;

    private Configuration configuration;

    private SqlSessionFactory sqlSessionFactory;

    @Override
    public void setApplicationAdapter(@NonNull ApplicationAdapter applicationAdapter) {
        this.applicationAdapter = applicationAdapter;
    }

    /**
     * Set optional properties to be passed into the SqlSession configuration.
     * @param variables the optional properties
     */
    public void setVariables(Properties variables) {
        this.variables = variables;
    }

    public void setEnvironmentId(String environmentId) {
        this.environmentId = environmentId;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public void setDatabaseIdProvider(DatabaseIdProvider databaseIdProvider) {
        this.databaseIdProvider = databaseIdProvider;
    }

    /**
     * Set the location of the MyBatis {@code SqlSessionFactory} config file.
     * @param configLocation the location of the MyBatis {@code SqlSessionFactory} config file
     */
    public void setConfigLocation(String configLocation) {
        this.configLocation = configLocation;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    private Configuration createConfiguration() throws Exception {
        Properties variablesToUse = new Properties();
        if (variables != null) {
            variablesToUse.putAll(variables);
            variables = null;
        }
        if (configLocation != null) {
            Assert.state(applicationAdapter != null, "No ApplicationAdapter injected");
            InputStream inputStream;
            if (configLocation.startsWith(CLASSPATH_URL_PREFIX)) {
                inputStream = ResourceUtils.getResourceAsStream(configLocation.substring(CLASSPATH_URL_PREFIX.length()));
            } else {
                inputStream = new FileInputStream(applicationAdapter.getRealPath(configLocation).toFile());
            }
            try (inputStream) {
                XMLConfigBuilder builder = new XMLConfigBuilder(inputStream, environmentId, variablesToUse);
                return builder.parse();
            } catch (Exception e) {
                throw new Exception("Error building configuration with resource " + configLocation, e);
            } finally {
                ErrorContext.instance().reset();
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Property 'configuration' or 'configLocation' not specified, using default MyBatis Configuration");
            }
            Configuration configuration = new Configuration();
            configuration.setVariables(variablesToUse);
            return configuration;
        }
    }

    protected void configure(Configuration configuration) {
    }

    @Override
    public void initialize() throws Exception {
        if (sqlSessionFactory == null) {
            if (configuration == null) {
                configuration = createConfiguration();
            }
            if (environment != null) {
                configuration.setEnvironment(environment);
                if (databaseIdProvider != null && environment.getDataSource() != null) {
                    configuration.setDatabaseId(databaseIdProvider.getDatabaseId(environment.getDataSource()));
                }
            }
            configure(configuration);
            sqlSessionFactory = new DefaultSqlSessionFactory(configuration);
        }
    }

    @Override
    public SqlSessionFactory getObject() {
        return sqlSessionFactory;
    }

}
