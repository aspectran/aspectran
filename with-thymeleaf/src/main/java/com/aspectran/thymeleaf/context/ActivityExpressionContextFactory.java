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
import com.aspectran.thymeleaf.context.web.WebActivityExchange;
import com.aspectran.thymeleaf.context.web.WebActivityExpressionContext;
import com.aspectran.utils.Assert;
import com.aspectran.utils.annotation.jsr305.NonNull;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.web.IWebExchange;

import java.util.Locale;

/**
 * A factory for creating {@link ActivityExpressionContext} instances.
 *
 * <p>This factory checks if the current activity is a web-based activity
 * and creates the appropriate context instance (e.g., {@link WebActivityExpressionContext}).</p>
 *
 * <p>Created: 2024-11-27</p>
 */
public abstract class ActivityExpressionContextFactory {

    /**
     * Creates a new {@link ActivityExpressionContext} for the given activity.
     * <p>If the activity is a web activity, a {@link WebActivityExpressionContext}
     * is created.</p>
     * @param activity the current Aspectran activity
     * @param configuration the Thymeleaf engine configuration
     * @param locale the locale
     * @return a new context instance
     */
    @NonNull
    public static ActivityExpressionContext create(
            Activity activity, IEngineConfiguration configuration, Locale locale) {
        Assert.notNull(activity, "activity cannot be null");
        if (activity.getMode() == Activity.Mode.WEB) {
            IWebExchange webExchange = WebActivityExchange.buildExchange(activity);
            return new WebActivityExpressionContext(activity, configuration, webExchange, locale);
        } else {
            return new ActivityExpressionContext(activity, configuration, locale);
        }
    }

}
