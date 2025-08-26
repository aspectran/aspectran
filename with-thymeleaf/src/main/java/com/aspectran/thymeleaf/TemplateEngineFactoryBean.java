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
package com.aspectran.thymeleaf;

import com.aspectran.core.component.bean.ablility.InitializableFactoryBean;

/**
 * A {@link com.aspectran.core.component.bean.ablility.FactoryBean} that creates
 * a singleton {@link AspectranTemplateEngine} instance.
 *
 * <p>This class extends {@link TemplateEngineFactory} to provide a convenient way
 * to configure and instantiate an {@code AspectranTemplateEngine} as a singleton
 * bean within the Aspectran framework.</p>
 *
 * <p>Created: 2024. 11. 18.</p>
 */
public class TemplateEngineFactoryBean extends TemplateEngineFactory
        implements InitializableFactoryBean<AspectranTemplateEngine> {

    private AspectranTemplateEngine templateEngine;

    /**
     * Initializes the factory by creating the singleton {@link AspectranTemplateEngine} instance.
     */
    @Override
    public void initialize() {
        if (templateEngine == null) {
            templateEngine = createTemplateEngine();
        }
    }

    /**
     * Returns the singleton {@link AspectranTemplateEngine} instance.
     * @return the {@code AspectranTemplateEngine} instance
     */
    @Override
    public AspectranTemplateEngine getObject() {
        return templateEngine;
    }

}
