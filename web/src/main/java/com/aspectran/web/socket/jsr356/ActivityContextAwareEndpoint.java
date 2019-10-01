/*
 * Copyright (c) 2008-2019 The Aspectran Project
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
package com.aspectran.web.socket.jsr356;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.component.bean.BeanRegistry;
import com.aspectran.core.component.bean.aware.ActivityContextAware;
import com.aspectran.core.component.template.TemplateRenderer;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.env.Environment;
import com.aspectran.core.support.i18n.message.MessageSource;
import com.aspectran.core.util.Assert;

/**
 * <p>Created: 29/09/2019</p>
 */
public abstract class ActivityContextAwareEndpoint implements ActivityContextAware {

    private ActivityContext context;

    public ApplicationAdapter getApplicationAdapter() {
        return getActivityContext().getApplicationAdapter();
    }

    public Environment getEnvironment() {
        return getActivityContext().getEnvironment();
    }

    public BeanRegistry getBeanRegistry() {
        return getActivityContext().getBeanRegistry();
    }

    public TemplateRenderer getTemplateRenderer() {
        return getActivityContext().getTemplateRenderer();
    }

    public MessageSource getMessageSource() {
        return getActivityContext().getMessageSource();
    }

    private ActivityContext getActivityContext() {
        Assert.state(context != null, "No ActivityContext configured");
        return context;
    }

    @Override
    public void setActivityContext(ActivityContext context) {
        this.context = context;
    }

}
