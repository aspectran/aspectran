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
package com.aspectran.thymeleaf.template;

import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.templateresolver.AbstractConfigurableTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;
import org.thymeleaf.templateresource.ClassLoaderTemplateResource;
import org.thymeleaf.templateresource.ITemplateResource;

import java.util.Map;

/**
 * Implementation of {@link ITemplateResolver} that extends {@link AbstractConfigurableTemplateResolver}
 * and creates {@link ClassLoaderTemplateResource} instances for template resources.
 */
public class ClassLoaderTemplateResolver extends AbstractConfigurableTemplateResolver {

    private final ClassLoader classLoader;

    public ClassLoaderTemplateResolver() {
        this(null);
    }

    public ClassLoaderTemplateResolver(ClassLoader classLoader) {
        super();
        // Class Loader might be null if we want to apply the default one
        this.classLoader = classLoader;
    }

    @Override
    protected ITemplateResource computeTemplateResource(
            IEngineConfiguration configuration,
            String ownerTemplate,
            String template,
            String resourceName,
            String characterEncoding,
            Map<String, Object> templateResolutionAttributes) {
        return new ClassLoaderTemplateResource(classLoader, resourceName, characterEncoding);
    }

}
