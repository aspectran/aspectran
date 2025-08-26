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
import com.aspectran.thymeleaf.context.CurrentActivityHolder;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IContext;
import org.thymeleaf.context.IEngineContext;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.context.WebEngineContext;
import org.thymeleaf.engine.TemplateData;
import org.thymeleaf.web.IWebExchange;

import java.util.Locale;
import java.util.Map;

/**
 * A web-specific Thymeleaf {@link org.thymeleaf.context.IEngineContext} that
 * makes the current Aspectran {@link Activity} available during template processing.
 *
 * <p>This class extends {@link org.thymeleaf.context.WebEngineContext} and implements
 * {@link com.aspectran.thymeleaf.context.CurrentActivityHolder}, acting as a bridge
 * between Aspectran's web environment and Thymeleaf's processing engine. It holds
 * not only the standard web context variables but also a reference to the underlying
 * {@code Activity}, which can be accessed by other custom components in the
 * Thymeleaf integration.</p>
 *
 * <p>Created: 2024-11-27</p>
 */
public class WebActivityEngineContext extends WebEngineContext implements CurrentActivityHolder {

    private final Activity activity;

    /**
     * Creates a new instance of this {@link IEngineContext} implementation binding engine execution to
     * the Servlet API.
     * <p>
     * Note that implementations of {@link IEngineContext} are not meant to be used in order to call
     * the template engine (use implementations of {@link IContext} such as {@link Context} or {@link WebContext}
     * instead). This is therefore mostly an <b>internal</b> implementation, and users should have no reason
     * to ever call this constructor except in very specific integration/extension scenarios.
     * </p>
     * @param activity the aspectran activity
     * @param configuration the configuration instance being used
     * @param templateData the template data for the template to be processed
     * @param templateResolutionAttributes the template resolution attributes
     * @param webExchange the web exchange object
     * @param locale the locale
     * @param variables the context variables, probably coming from another {@link IContext} implementation
     */
    public WebActivityEngineContext(
            Activity activity, IEngineConfiguration configuration, TemplateData templateData,
            Map<String, Object> templateResolutionAttributes, IWebExchange webExchange,
            Locale locale, Map<String, Object> variables) {
        super(configuration, templateData, templateResolutionAttributes, webExchange, locale, variables);
        this.activity = activity;
    }

    @Override
    public Activity getActivity() {
        return activity;
    }

}
