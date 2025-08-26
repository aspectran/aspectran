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
import com.aspectran.utils.Assert;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.context.AbstractExpressionContext;

import java.util.Locale;
import java.util.Map;

/**
 * A Thymeleaf {@link org.thymeleaf.context.IExpressionContext} implementation that
 * wraps an Aspectran {@link Activity}.
 *
 * <p>This context makes the Aspectran Activity available to Thymeleaf expressions.</p>
 *
 * <p>Created: 2024-11-27</p>
 */
public class ActivityExpressionContext extends AbstractExpressionContext implements CurrentActivityHolder {

    private final Activity activity;

    /**
     * Instantiates a new ActivityExpressionContext.
     * @param activity the Aspectran activity
     * @param configuration the engine configuration
     */
    public ActivityExpressionContext(Activity activity, IEngineConfiguration configuration) {
        this(activity, configuration, null, null);
    }

    /**
     * Instantiates a new ActivityExpressionContext.
     * @param activity the Aspectran activity
     * @param configuration the engine configuration
     * @param locale the locale
     */
    public ActivityExpressionContext(Activity activity, IEngineConfiguration configuration, Locale locale) {
        this(activity, configuration, locale, null);
    }

    /**
     * Instantiates a new ActivityExpressionContext.
     * @param activity the Aspectran activity
     * @param configuration the engine configuration
     * @param locale the locale
     * @param variables the context variables
     */
    public ActivityExpressionContext(
            Activity activity, IEngineConfiguration configuration, Locale locale, Map<String, Object> variables) {
        super(configuration, locale, variables);
        Assert.notNull(activity, "activity must not be null");
        this.activity = activity;
    }

    @Override
    public Activity getActivity() {
        return activity;
    }

}
