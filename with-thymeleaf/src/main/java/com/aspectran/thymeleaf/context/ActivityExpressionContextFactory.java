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
package com.aspectran.thymeleaf.context;

import com.aspectran.core.activity.Activity;
import com.aspectran.thymeleaf.context.tow.TowActivityExchange;
import com.aspectran.thymeleaf.context.web.WebActivityExchange;
import com.aspectran.thymeleaf.context.web.WebActivityExpressionContext;
import com.aspectran.undertow.activity.TowActivity;
import com.aspectran.utils.Assert;
import com.aspectran.web.activity.WebActivity;
import org.jspecify.annotations.NonNull;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.web.IWebExchange;

import java.util.Locale;

/**
 * A factory for creating Thymeleaf {@link org.thymeleaf.context.IExpressionContext} instances
 * tailored to the specific type of Aspectran {@link Activity}.
 *
 * <p>This factory is the central point for context creation in the Aspectran-Thymeleaf
 * integration. It inspects the provided {@code Activity} and creates the appropriate
 * context to bridge Aspectran's environment with Thymeleaf's expression processing.
 * It distinguishes between:
 * <ul>
 *   <li>Servlet-based web activities ({@link WebActivity}), for which it creates a
 *       {@link WebActivityExpressionContext} with a servlet-aware exchange.</li>
 *   <li>Non-servlet web activities ({@link TowActivity}), for which it also creates a
 *       {@link WebActivityExpressionContext}, but with a non-servlet-aware exchange.</li>
 *   <li>Non-web activities, for which it creates a basic {@link ActivityExpressionContext}.</li>
 * </ul>
 *
 * <p>Created: 2024-11-27</p>
 */
public abstract class ActivityExpressionContextFactory {

    /**
     * Creates a new {@link ActivityExpressionContext} appropriate for the given activity type.
     * <p>This method inspects the activity and creates a web-enabled context
     * ({@link WebActivityExpressionContext}) for web activities (both servlet and non-servlet)
     * or a basic context for non-web activities. The created context provides the necessary
     * variables and expression objects for Thymeleaf template processing.</p>
     * @param activity      the current Aspectran {@link Activity}
     * @param configuration the active {@link IEngineConfiguration} for the Thymeleaf engine
     * @param locale        the {@link Locale} to be used for processing
     * @return a new {@link ActivityExpressionContext} instance, either a web or non-web variant
     */
    @NonNull
    public static ActivityExpressionContext create(
            Activity activity, IEngineConfiguration configuration, Locale locale) {
        Assert.notNull(activity, "activity cannot be null");
        if (activity instanceof WebActivity) {
            IWebExchange webExchange = WebActivityExchange.buildExchange(activity);
            return new WebActivityExpressionContext(activity, configuration, webExchange, locale);
        } else if (activity instanceof TowActivity towActivity) {
            IWebExchange webExchange = TowActivityExchange.buildExchange(towActivity);
            return new WebActivityExpressionContext(towActivity, configuration, webExchange, locale);
        } else {
            return new ActivityExpressionContext(activity, configuration, locale);
        }
    }

}
