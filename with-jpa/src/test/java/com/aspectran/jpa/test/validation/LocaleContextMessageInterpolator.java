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
package com.aspectran.jpa.test.validation;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.Translet;
import com.aspectran.core.component.bean.aware.ActivityContextAware;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.utils.Assert;
import jakarta.validation.MessageInterpolator;

import java.util.Locale;

/**
 * <p>Created: 2025-05-14</p>
 */
public class LocaleContextMessageInterpolator implements MessageInterpolator, ActivityContextAware {

    private final MessageInterpolator targetInterpolator;

    private ActivityContext context;

    /**
     * Create a new LocaleContextMessageInterpolator, wrapping the given target interpolator.
     * @param targetInterpolator the target MessageInterpolator to wrap
     */
    public LocaleContextMessageInterpolator(MessageInterpolator targetInterpolator) {
        Assert.notNull(targetInterpolator, "Target MessageInterpolator must not be null");
        this.targetInterpolator = targetInterpolator;
    }

    @Override
    public void setActivityContext(ActivityContext context) {
        this.context = context;
    }

    @Override
    public String interpolate(String message, Context context) {
        return this.targetInterpolator.interpolate(message, context, getLocale());
    }

    @Override
    public String interpolate(String message, Context context, Locale locale) {
        return this.targetInterpolator.interpolate(message, context, locale);
    }

    private Locale getLocale() {
        if (context != null && context.hasCurrentActivity()) {
            Activity activity = context.getCurrentActivity();
            if (activity.hasTranslet()) {
                Translet translet = activity.getTranslet();
                if (translet.getRequestAdapter() != null) {
                    return translet.getRequestAdapter().getLocale();
                }
            }
        }
        return Locale.getDefault();
    }

}
