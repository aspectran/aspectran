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

import com.aspectran.core.component.bean.aware.ActivityContextAware;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.utils.annotation.jsr305.NonNull;
import org.thymeleaf.dialect.IDialect;
import org.thymeleaf.templateresolver.ITemplateResolver;

import java.util.Set;

/**
 * A factory for creating and configuring an {@link AspectranTemplateEngine} instance.
 *
 * <p>This factory allows for the programmatic setup of a Thymeleaf template engine,
 * including the configuration of template resolvers and integration with Aspectran's
 * internationalization features.</p>
 *
 * <p>Created: 2024. 11. 18.</p>
 */
public class TemplateEngineFactory implements ActivityContextAware {

    private ActivityContext context;

    private Set<ITemplateResolver> templateResolvers;

    private Set<IDialect> dialects;

    @Override
    public void setActivityContext(@NonNull ActivityContext context) {
        this.context = context;
    }

    /**
     * Sets the template resolvers to be used by the template engine.
     * @param templateResolvers a set of template resolvers
     */
    public void setTemplateResolvers(Set<ITemplateResolver> templateResolvers) {
        this.templateResolvers = templateResolvers;
    }

    public void setDialects(Set<IDialect> dialects) {
        this.dialects = dialects;
    }

    /**
     * Creates a new {@link AspectranTemplateEngine} instance, configured with
     * the specified template resolvers and Aspectran's message source.
     * @return a new {@code AspectranTemplateEngine} instance
     */
    public AspectranTemplateEngine createTemplateEngine() {
        AspectranTemplateEngine templateEngine = new AspectranTemplateEngine();
        templateEngine.setMessageSource(context.getMessageSource());
        if (templateResolvers != null) {
            templateEngine.setTemplateResolvers(templateResolvers);
        }
        if (dialects != null) {
            for (IDialect dialect : dialects) {
                templateEngine.addDialect(dialect);
            }
        }
        return templateEngine;
    }

}
