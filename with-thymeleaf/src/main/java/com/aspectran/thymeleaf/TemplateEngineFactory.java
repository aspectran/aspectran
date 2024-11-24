/*
 * Copyright (c) 2008-2024 The Aspectran Project
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

import com.aspectran.core.component.bean.annotation.AvoidAdvice;
import com.aspectran.core.component.bean.aware.ActivityContextAware;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.utils.annotation.jsr305.NonNull;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.messageresolver.IMessageResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

import java.util.Set;

/**
 * Factory that configures a Thymeleaf Template Engine.
 *
 * <p>Created: 2024. 11. 18.</p>
 */
public class TemplateEngineFactory implements ActivityContextAware {

    private ActivityContext context;

    private Set<ITemplateResolver> templateResolvers;

    @Override
    @AvoidAdvice
    public void setActivityContext(@NonNull ActivityContext context) {
        this.context = context;
    }

    public void setTemplateResolvers(Set<ITemplateResolver> templateResolvers) {
        this.templateResolvers = templateResolvers;
    }

    public ITemplateEngine createTemplateEngine() {
        IMessageResolver messageResolver = new AspectranMessageResolver(context.getMessageSource());

        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setMessageResolver(messageResolver);
        if (templateResolvers != null) {
            templateEngine.setTemplateResolvers(templateResolvers);
        }
        return templateEngine;
    }

}
