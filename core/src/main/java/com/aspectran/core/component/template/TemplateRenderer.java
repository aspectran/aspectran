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
package com.aspectran.core.component.template;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.request.ParameterMap;
import com.aspectran.core.context.rule.TemplateRule;

import java.util.Map;

/**
 * The TemplateRenderer interface provides a mechanism for rendering templates.
 * It allows for rendering templates identified by an ID or defined by a TemplateRule,
 * and supports passing parameters and attributes to the template engine.
 *
 * <p>Created: 2016. 1. 14.</p>
 */
public interface TemplateRenderer {

    /**
     * Renders the template specified by its ID.
     * @param templateId the ID of the template to render
     * @return the rendered template as a string
     */
    String render(String templateId);

    /**
     * Renders the template specified by its ID with the given parameters.
     * @param templateId the ID of the template to render
     * @param parameterMap the parameters to use for rendering
     * @return the rendered template as a string
     */
    String render(String templateId, ParameterMap parameterMap);

    /**
     * Renders the template specified by its ID with the given attributes.
     * @param templateId the ID of the template to render
     * @param attributeMap the attributes to use for rendering
     * @return the rendered template as a string
     */
    String render(String templateId, Map<String, Object> attributeMap);

    /**
     * Renders the template specified by its ID with the given parameters and attributes.
     * @param templateId the ID of the template to render
     * @param parameterMap the parameters to use for rendering
     * @param attributeMap the attributes to use for rendering
     * @return the rendered template as a string
     */
    String render(String templateId, ParameterMap parameterMap, Map<String, Object> attributeMap);

    /**
     * Renders the template specified by its ID and writes the output to the activity's response.
     * @param templateId the ID of the template to render
     * @param activity the current activity
     */
    void render(String templateId, Activity activity);

    /**
     * Renders the template specified by the TemplateRule and writes the output to the activity's response.
     * @param templateRule the TemplateRule that defines the template to render
     * @param activity the current activity
     */
    void render(TemplateRule templateRule, Activity activity);

}
