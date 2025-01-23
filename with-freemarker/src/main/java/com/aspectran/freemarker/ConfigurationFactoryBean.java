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
package com.aspectran.freemarker;

import com.aspectran.core.component.bean.ablility.InitializableFactoryBean;
import freemarker.template.Configuration;

/**
 * JavaBean to configure FreeMarker.
 *
 * <p>Note: Aspectran's FreeMarker support requires FreeMarker 2.3 or higher.</p>
 *
 * <p>Created: 2016. 1. 9.</p>
 */
public class ConfigurationFactoryBean extends ConfigurationFactory
        implements InitializableFactoryBean<Configuration> {

    private Configuration configuration;

    /**
     * Initialize FreeMarkerConfigurationFactory's Configuration
     * if not overridden by a preconfigured FreeMarker Configuration.
     * @throws Exception Exceptions occurring when you configure FreeMarker
     */
    @Override
    public void initialize() throws Exception {
        if (configuration == null) {
            configuration = createConfiguration();
        }
    }

    @Override
    public Configuration getObject() {
        return configuration;
    }

}
