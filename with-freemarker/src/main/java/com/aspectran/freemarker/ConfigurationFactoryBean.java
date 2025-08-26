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
package com.aspectran.freemarker;

import com.aspectran.core.component.bean.ablility.InitializableFactoryBean;
import freemarker.template.Configuration;

/**
 * A {@link com.aspectran.core.component.bean.ablility.FactoryBean} that creates
 * and configures a FreeMarker {@link Configuration} object.
 * <p>This is the recommended way to manage a shared, singleton FreeMarker Configuration
 * instance within an Aspectran application. It allows for easy setup and injection of the
 * configuration into other components, such as the {@link FreeMarkerTemplateEngine}.</p>
 *
 * <p>Note: Aspectran's FreeMarker support requires FreeMarker 2.3 or higher.</p>
 *
 * @since 2016. 1. 9.
 * @see ConfigurationFactory
 */
public class ConfigurationFactoryBean extends ConfigurationFactory
        implements InitializableFactoryBean<Configuration> {

    private Configuration configuration;

    /**
     * This method is called by the Aspectran bean container after all configuration
     * properties have been set. It invokes the {@link #createConfiguration()} method
     * to build the singleton {@code Configuration} instance.
     * @throws Exception if an error occurs during FreeMarker configuration
     */
    @Override
    public void initialize() throws Exception {
        if (configuration == null) {
            configuration = createConfiguration();
        }
    }

    /**
     * Returns the singleton, configured FreeMarker {@link Configuration} object.
     * This is the object that will be returned when this FactoryBean is referenced as a bean.
     * @return the configured Configuration instance, or {@code null} if not yet initialized
     */
    @Override
    public Configuration getObject() {
        return configuration;
    }

}
