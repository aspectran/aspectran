/*
 * Copyright (c) 2008-2023 The Aspectran Project
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
import com.aspectran.core.component.bean.ablility.FactoryBean;
import com.aspectran.core.component.bean.ablility.InitializableBean;
import com.aspectran.core.component.bean.annotation.AvoidAdvice;
import com.aspectran.core.component.bean.aware.ApplicationAdapterAware;
import com.aspectran.core.util.Assert;
import com.aspectran.core.util.ClassUtils;
import com.aspectran.core.util.ResourceUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import static com.aspectran.core.util.ResourceUtils.CLASSPATH_URL_PREFIX;

/**
 * {@code FactoryBean} that creates an MyBatis {@code SqlSessionFactory}
 * using default MyBatis Configuration.
 */
@AvoidAdvice
public class SqlSessionFactoryBean implements ApplicationAdapterAware, InitializableBean,
        FactoryBean<SqlSessionFactory> {

    private ApplicationAdapter applicationAdapter;

    private String configLocation;

    private String environment;

    private Properties properties;

    private SqlSessionFactory sqlSessionFactory;

    /**
     * Set the location of the MyBatis {@code SqlSessionFactory} config file.
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
     * @param properties the optional properties
     */
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    protected SqlSessionFactory buildSqlSessionFactory(InputStream inputStream) {
        ClassLoader originalClassLoader = null;
        try {
            originalClassLoader = ClassUtils.overrideThreadContextClassLoader(applicationAdapter.getClassLoader());
            SqlSessionFactoryBuilder sqlSessionFactoryBuilder = new SqlSessionFactoryBuilder();
            return sqlSessionFactoryBuilder.build(inputStream, environment, properties);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to parse mybatis config resource: " +
                    configLocation, ex);
        } finally {
            ClassUtils.restoreThreadContextClassLoader(originalClassLoader);
        }
    }

    @Override
    public void setApplicationAdapter(ApplicationAdapter applicationAdapter) {
        this.applicationAdapter = applicationAdapter;
    }

    @Override
    public void initialize() throws Exception {
        Assert.state(applicationAdapter != null, "No ApplicationAdapter injected");
        if (sqlSessionFactory == null) {
            if (configLocation == null) {
                throw new IllegalArgumentException("Property 'configLocation' is required");
            }
            InputStream is;
            if (configLocation.startsWith(CLASSPATH_URL_PREFIX)) {
                is = ResourceUtils.getResourceAsStream(configLocation.substring(CLASSPATH_URL_PREFIX.length()),
                        applicationAdapter.getClassLoader());
            } else {
                is = new FileInputStream(applicationAdapter.toRealPathAsFile(configLocation));
            }
            sqlSessionFactory = buildSqlSessionFactory(is);
        }
    }

    @Override
    public SqlSessionFactory getObject() {
        return sqlSessionFactory;
    }

}
