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
package com.aspectran.core.activity;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.component.bean.BeanRegistry;
import com.aspectran.core.component.bean.aware.ActivityContextAware;
import com.aspectran.core.component.template.TemplateRenderer;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.env.Environment;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.support.i18n.message.MessageSource;
import com.aspectran.utils.Assert;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;

/**
 * Inheriting this class provides access to the activity context and
 * facilitates execution of the activity.
 *
 * <p>Created: 29/09/2019</p>
 */
public abstract class InstantActivitySupport implements ActivityContextAware {

    private ActivityContext context;

    @NonNull
    protected ActivityContext getActivityContext() {
        Assert.state(context != null, "No ActivityContext injected");
        return context;
    }

    @Override
    public void setActivityContext(@NonNull ActivityContext context) {
        Assert.state(this.context == null, "ActivityContext already injected");
        this.context = context;
    }

    protected Activity getAvailableActivity() {
        return getActivityContext().getAvailableActivity();
    }

    protected Activity getCurrentActivity() {
        return getActivityContext().getCurrentActivity();
    }

    protected boolean hasCurrentActivity() {
        return getActivityContext().hasCurrentActivity();
    }

    protected ApplicationAdapter getApplicationAdapter() {
        return getActivityContext().getApplicationAdapter();
    }

    protected Environment getEnvironment() {
        return getActivityContext().getEnvironment();
    }

    protected BeanRegistry getBeanRegistry() {
        return getActivityContext().getBeanRegistry();
    }

    protected TemplateRenderer getTemplateRenderer() {
        return getActivityContext().getTemplateRenderer();
    }

    protected MessageSource getMessageSource() {
        return getActivityContext().getMessageSource();
    }

    protected <V> V instantActivity(InstantAction<V> instantAction) {
        if (instantAction == null) {
            throw new IllegalArgumentException("instantAction must not be null");
        }
        try {
            InstantActivity activity = new InstantActivity(getActivityContext());
            return activity.perform(instantAction);
        } catch (Exception e) {
            throw new InstantActivityException(e);
        }
    }

    protected void instantActivity(String requestName) {
        instantActivity(requestName, MethodType.GET);
    }

    protected void instantActivity(String requestName, MethodType requestMethod) {
        if (StringUtils.isEmpty(requestName)) {
            throw new IllegalArgumentException("requestName must not be null or empty");
        }
        Activity currentActivity = getCurrentActivity();
        Translet translet = currentActivity.getTranslet();
        if (translet == null) {
            throw new IllegalStateException("No Translet in " + currentActivity);
        }
        try {
            InstantActivity activity = new InstantActivity(currentActivity);
            activity.prepare(requestName, requestMethod);
            activity.perform();
        } catch (Exception e) {
            throw new InstantActivityException(e);
        }
    }

}
