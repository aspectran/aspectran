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
package com.aspectran.core.support;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.component.bean.ablility.InitializableFactoryBean;
import com.aspectran.core.component.bean.annotation.AvoidAdvice;
import com.aspectran.core.component.bean.aware.ApplicationAdapterAware;
import com.aspectran.utils.Assert;
import com.aspectran.utils.PropertiesLoaderSupport;
import com.aspectran.utils.ResourceUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static com.aspectran.utils.ResourceUtils.CLASSPATH_URL_PREFIX;

/**
 * Allows for making a properties file from a classpath location available
 * as Properties instance in a bean factory. Can be used to populate
 * any bean property of type Properties via a bean reference.
 * Supports loading from a properties file and/or setting local properties
 * on this FactoryBean. The created Properties instance will be merged from
 * loaded and local values.
 *
 * <p>Created: 2025. 2. 18.</p>
 */
@AvoidAdvice
public class PropertiesFactoryBean extends PropertiesLoaderSupport
        implements ApplicationAdapterAware, InitializableFactoryBean<Properties> {

    private Properties properties;

    private ApplicationAdapter applicationAdapter;

    @Override
    public void setApplicationAdapter(ApplicationAdapter applicationAdapter) {
        this.applicationAdapter = applicationAdapter;
    }

    @Override
    protected InputStream getResourceAsStream(String location) throws IOException {
        Assert.notNull(location, "location must not be null");
        InputStream is;
        if (location.startsWith(CLASSPATH_URL_PREFIX)) {
            is = ResourceUtils.getResourceAsStream(location.substring(CLASSPATH_URL_PREFIX.length()));
        } else {
            Assert.state(applicationAdapter != null, "No ApplicationAdapter injected");
            is = new FileInputStream(applicationAdapter.getRealPath(location).toFile());
        }
        return is;
    }

    @Override
    public void initialize() throws Exception {
        if (properties == null) {
            properties = mergeProperties();
        }
    }

    @Override
    public Properties getObject() throws Exception {
        return properties;
    }

}
