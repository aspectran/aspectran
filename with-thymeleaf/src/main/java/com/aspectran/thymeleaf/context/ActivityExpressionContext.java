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
package com.aspectran.thymeleaf.context;

import com.aspectran.core.activity.Activity;
import com.aspectran.utils.Assert;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.context.AbstractExpressionContext;

import java.util.Locale;
import java.util.Map;

public class ActivityExpressionContext extends AbstractExpressionContext implements CurrentActivityHolder {

    private final Activity activity;

    public ActivityExpressionContext(Activity activity, IEngineConfiguration configuration) {
        this(activity, configuration, null, null);
    }

    public ActivityExpressionContext(Activity activity, IEngineConfiguration configuration, Locale locale) {
        this(activity, configuration, locale, null);
    }

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
