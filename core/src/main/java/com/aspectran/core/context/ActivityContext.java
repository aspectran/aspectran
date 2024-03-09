/*
 * Copyright (c) 2008-2024 The Aspectran Project
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
package com.aspectran.core.context;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.component.aspect.AspectRuleRegistry;
import com.aspectran.core.component.bean.BeanRegistry;
import com.aspectran.core.component.schedule.ScheduleRuleRegistry;
import com.aspectran.core.component.template.TemplateRenderer;
import com.aspectran.core.component.translet.TransletRuleRegistry;
import com.aspectran.core.context.env.Environment;
import com.aspectran.core.service.CoreService;
import com.aspectran.core.support.i18n.message.MessageSource;

import java.nio.charset.StandardCharsets;

/**
 * Central interface to provide configuration for performing various activities.
 *
 * <p>Created: 2008. 06. 09 PM 2:12:40</p>
 */
public interface ActivityContext {

    String ID_SEPARATOR = ".";

    char ID_SEPARATOR_CHAR = '.';

    String NAME_SEPARATOR = "/";

    char NAME_SEPARATOR_CHAR = '/';

    String DEFAULT_ENCODING = StandardCharsets.UTF_8.name();

    String MESSAGE_SOURCE_BEAN_ID = "messageSource";

    String getName();

    /**
     * Gets the description of this ActivityContext.
     * @return the description of this ActivityContext
     */
    String getDescription();

    /**
     * Returns the first created CoreService that holds the ActivityContext.
     * @return the root service
     */
    CoreService getRootService();

    /**
     * Sets the Aspectran Service that created the current ActivityContext.
     * It is set only once, just after the ActivityContext is created.
     * @param rootService the root service
     */
    void setRootService(CoreService rootService);

    /**
     * Returns the class loader used by the current application.
     * @return the class loader
     */
    ApplicationAdapter getApplicationAdapter();

    /**
     * Returns the ClassLoader for this service.
     * @return the ClassLoader for this service
     */
    ClassLoader getClassLoader();

    /**
     * Gets the environment.
     * @return the environment
     */
    Environment getEnvironment();

    /**
     * Gets the aspect rule registry.
     * @return the aspect rule registry
     */
    AspectRuleRegistry getAspectRuleRegistry();

    /**
     * Gets the bean registry.
     * @return the bean registry
     */
    BeanRegistry getBeanRegistry();

    /**
     * Gets the schedule rule registry.
     * @return the schedule rule registry
     */
    ScheduleRuleRegistry getScheduleRuleRegistry();

    /**
     * Gets the template renderer.
     * @return the template renderer
     */
    TemplateRenderer getTemplateRenderer();

    /**
     * Gets the translet rule registry.
     * @return the translet rule registry
     */
    TransletRuleRegistry getTransletRuleRegistry();

    /**
     * Gets the message source.
     * @return the message source
     */
    MessageSource getMessageSource();

    /**
     * Gets the default activity.
     * @return the default activity
     */
    Activity getDefaultActivity();

    /**
     * Gets the available activity.
     * If there is no current activity, the application default activity is returned.
     * @return the available activity
     */
    Activity getAvailableActivity();

    /**
     * Gets the current activity.
     * @return the current activity
     * @throws InactivityStateException if there is no current activity
     */
    Activity getCurrentActivity();

    /**
     * Sets the current activity.
     * @param activity the new current activity
     */
    void setCurrentActivity(Activity activity);

    /**
     * Removes the current activity.
     */
    void removeCurrentActivity();

    /**
     * Returns whether there is current activity.
     * @return {@code true} if there is current activity, {@code false} otherwise
     */
    boolean hasCurrentActivity();

}
