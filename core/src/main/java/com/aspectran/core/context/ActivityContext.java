/*
 * Copyright (c) 2008-2018 The Aspectran Project
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
import com.aspectran.core.component.aspect.AspectRuleRegistry;
import com.aspectran.core.component.bean.BeanRegistry;
import com.aspectran.core.component.schedule.ScheduleRuleRegistry;
import com.aspectran.core.component.template.TemplateProcessor;
import com.aspectran.core.component.translet.TransletRuleRegistry;
import com.aspectran.core.context.env.Environment;
import com.aspectran.core.service.CoreService;
import com.aspectran.core.support.i18n.message.MessageSource;

/**
 * Central interface to provide configuration for performing various activities.
 *
 * <p>Created: 2008. 06. 09 PM 2:12:40</p>
 */
public interface ActivityContext {

    String ID_SEPARATOR = ".";

    char ID_SEPARATOR_CHAR = '.';

    char TRANSLET_NAME_SEPARATOR_CHAR = '/';

    String LINE_SEPARATOR = "\n";

    String DEFAULT_ENCODING = "UTF-8";

    String MESSAGE_SOURCE_BEAN_ID = "messageSource";

    String BASE_DIR_PROPERTY_NAME = "aspectran.baseDir";

    /**
     * Gets the description of this ActivityContext.
     *
     * @return the description of this ActivityContext
     */
    String getDescription();

    /**
     * Gets the context environment.
     *
     * @return the context environment
     */
    Environment getEnvironment();

    /**
     * Returns the Aspectran Service that created the current ActivityContext.
     *
     * @return the root service
     */
    CoreService getRootService();

    /**
     * Sets the Aspectran Service that created the current ActivityContext.
     * It is set only once, just after the ActivityContext is created.
     *
     * @param rootService the root service
     */
    void setRootService(CoreService rootService);

    /**
     * Gets the aspect rule registry.
     *
     * @return the aspect rule registry
     */
    AspectRuleRegistry getAspectRuleRegistry();

    /**
     * Gets the bean registry.
     *
     * @return the bean registry
     */
    BeanRegistry getBeanRegistry();

    /**
     * Gets the schedule rule registry.
     *
     * @return the schedule rule registry
     */
    ScheduleRuleRegistry getScheduleRuleRegistry();

    /**
     * Gets the template processor.
     *
     * @return the template processor
     */
    TemplateProcessor getTemplateProcessor();

    /**
     * Gets the translet rule registry.
     *
     * @return the translet rule registry
     */
    TransletRuleRegistry getTransletRuleRegistry();

    /**
     * Gets the message source.
     *
     * @return the message source
     */
    MessageSource getMessageSource();

    /**
     * Gets the default activity.
     *
     * @return the default activity
     */
    Activity getDefaultActivity();

    /**
     * Gets the current activity.
     *
     * @return the current activity
     */
    Activity getCurrentActivity();

    /**
     * Sets the current activity.
     *
     * @param activity the new current activity
     */
    void setCurrentActivity(Activity activity);

    /**
     * Removes the current activity.
     */
    void removeCurrentActivity();

}
