/*
 * Copyright (c) 2008-2019 The Aspectran Project
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
package com.aspectran.with.mybatis;

import com.aspectran.core.component.bean.ablility.FactoryBean;
import com.aspectran.core.component.bean.ablility.InitializableBean;
import com.aspectran.core.component.bean.annotation.AvoidAdvice;
import com.aspectran.core.component.bean.aware.ActivityContextAware;
import com.aspectran.core.context.ActivityContext;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.Properties;

/**
 * {@code FactoryBean} that creates an MyBatis {@code SqlSessionFactory}
 * using default MyBatis Configuration.
 */
@AvoidAdvice
public class SqlSessionFactoryBean implements ActivityContextAware, InitializableBean,
        FactoryBean<SqlSessionFactory> {

    private ActivityContext context;

    private String configLocation;

    private String environment;

    private Properties properties;

    private SqlSessionFactory sqlSessionFactory;

    /**
     * Set the location of the MyBatis {@code SqlSessionFactory} config file.
     *
     * @param configLocation the location of the MyBatis {@code SqlSessionFactory} config file
     */
    public void setConfigLocation(String configLocation) {
        this.configLocation = configLocation;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    /**
     * Set optional properties to be passed into the SqlSession configuration.
     *
     * @param properties the optional properties
     */
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    protected SqlSessionFactory buildSqlSessionFactory(File configFile) {
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        try (Reader reader = new FileReader(configFile)) {
            Thread.currentThread().setContextClassLoader(context.getEnvironment().getClassLoader());
            SqlSessionFactoryBuilder sqlSessionFactoryBuilder = new SqlSessionFactoryBuilder();
            return sqlSessionFactoryBuilder.build(reader, environment, properties);
        } catch(Exception ex) {
            throw new IllegalArgumentException("Failed to parse mybatis config resource: " +
                    configLocation, ex);
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }

    @Override
    public void setActivityContext(ActivityContext context) {
        this.context = context;
    }

    @Override
    public void initialize() throws Exception {
        if(sqlSessionFactory == null) {
            if(configLocation == null) {
                throw new IllegalArgumentException("Property 'configLocation' is required");
            }

            File configFile = context.getEnvironment().toRealPathAsFile(configLocation);
            sqlSessionFactory = buildSqlSessionFactory(configFile);
        }
    }

    @Override
    public SqlSessionFactory getObject() {
        return sqlSessionFactory;
    }

}