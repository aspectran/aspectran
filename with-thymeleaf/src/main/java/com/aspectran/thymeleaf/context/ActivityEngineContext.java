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
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.context.EngineContext;
import org.thymeleaf.context.IContext;
import org.thymeleaf.context.IEngineContext;
import org.thymeleaf.engine.TemplateData;

import java.util.Locale;
import java.util.Map;

/**
 * A Thymeleaf {@link IEngineContext} implementation that makes the current
 * Aspectran {@link Activity} available during template processing.
 *
 * <p>Created: 2024-11-27</p>
 */
public class ActivityEngineContext extends EngineContext implements CurrentActivityHolder {

    private final Activity activity;

    /**
     * <p>
     *   Creates a new instance of this {@link IEngineContext} implementation.
     * </p>
     * <p>
     *   Note that implementations of {@link IEngineContext} are not meant to be used in order to call
     *   the template engine (use implementations of {@link IContext} instead).
     *   This is therefore mostly an <b>internal</b> implementation, and users should have no reason
     *   to ever call this constructor except in very specific integration/extension scenarios.
     * </p>
     *
     * @param activity the Aspectran activity
     * @param configuration the configuration instance being used
     * @param templateData the template data for the template to be processed
     * @param templateResolutionAttributes the template resolution attributes
     * @param locale the locale
     * @param variables the context variables, probably coming from another {@link IContext} implementation
     */
    public ActivityEngineContext(
            Activity activity, IEngineConfiguration configuration, TemplateData templateData,
            Map<String, Object> templateResolutionAttributes, Locale locale, Map<String, Object> variables) {
        super(configuration, templateData, templateResolutionAttributes, locale, variables);
        this.activity = activity;
    }

    @Override
    public Activity getActivity() {
        return activity;
    }

}
