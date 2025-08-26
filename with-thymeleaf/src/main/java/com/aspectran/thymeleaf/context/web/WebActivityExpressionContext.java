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
package com.aspectran.thymeleaf.context.web;

import com.aspectran.core.activity.Activity;
import com.aspectran.thymeleaf.context.ActivityExpressionContext;
import com.aspectran.utils.Assert;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.context.IWebContext;
import org.thymeleaf.web.IWebExchange;

import java.util.Locale;
import java.util.Map;

/**
 * A Thymeleaf {@link IWebContext} implementation that is backed by an
 * Aspectran {@link Activity}.
 *
 * <p>This class extends {@link ActivityExpressionContext}
 * to provide web-specific capabilities.</p>
 *
 * <p>Created: 2024-11-27</p>
 */
public class WebActivityExpressionContext extends ActivityExpressionContext implements IWebContext {

    private final IWebExchange webExchange;

    /**
     * Instantiates a new WebActivityExpressionContext.
     * @param activity the Aspectran activity
     * @param configuration the engine configuration
     * @param webExchange the web exchange
     */
    public WebActivityExpressionContext(
        Activity activity, IEngineConfiguration configuration, IWebExchange webExchange) {
        this(activity, configuration, webExchange, null, null);
    }

    /**
     * Instantiates a new WebActivityExpressionContext.
     * @param activity the Aspectran activity
     * @param configuration the engine configuration
     * @param webExchange the web exchange
     * @param locale the locale
     */
    public WebActivityExpressionContext(
            Activity activity, IEngineConfiguration configuration, IWebExchange webExchange, Locale locale) {
        this(activity, configuration, webExchange, locale, null);
    }

    /**
     * Instantiates a new WebActivityExpressionContext.
     * @param activity the Aspectran activity
     * @param configuration the engine configuration
     * @param webExchange the web exchange
     * @param locale the locale
     * @param variables the context variables
     */
    public WebActivityExpressionContext(
            Activity activity, IEngineConfiguration configuration, IWebExchange webExchange, Locale locale,
            Map<String, Object> variables) {
        super(activity, configuration, locale, variables);
        Assert.notNull(webExchange, "Web exchange cannot be null in web context");
        this.webExchange = webExchange;
    }

    @Override
    public IWebExchange getExchange() {
        return this.webExchange;
    }

}
