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

import com.aspectran.core.support.i18n.message.MessageSource;
import com.aspectran.thymeleaf.context.ActivityEngineContextFactory;
import com.aspectran.thymeleaf.dialect.AspectranStandardDialect;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.IEngineContextFactory;
import org.thymeleaf.dialect.IDialect;

/**
 * <p>Created: 2024. 11. 25.</p>
 */
public class AspectranTemplateEngine extends TemplateEngine {

    public static final IDialect DIALECT = new AspectranStandardDialect();

    public static final IEngineContextFactory ENGINE_CONTEXT_FACTORY = new ActivityEngineContextFactory();

    public AspectranTemplateEngine() {
        super();
        setDialect(DIALECT);
        setEngineContextFactory(ENGINE_CONTEXT_FACTORY);
    }

    public void setMessageSource(MessageSource messageSource) {
        setMessageResolver(new AspectranMessageResolver(messageSource));
    }

}
