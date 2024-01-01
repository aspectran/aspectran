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
package com.aspectran.core.component.template;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.context.rule.TemplateRule;

import java.io.Writer;
import java.util.Map;

/**
 * The Interface TemplateRenderer.
 *
 * <p>Created: 2016. 1. 14.</p>
 */
public interface TemplateRenderer {

    /**
     * Renders the template with specified TemplateRule by its ID.
     * @param templateId the template id
     * @return the output string of the template
     */
    String render(String templateId);

    /**
     * Renders the template with specified TemplateRule by its ID.
     * @param templateId the template id
     * @param model the holder of the variables visible from the template (name-value pairs)
     * @return the output string of the template
     */
    String render(String templateId, Map<String, Object> model);

    /**
     * Renders the template with specified TemplateRule.
     * @param templateRule the template rule
     * @param model the holder of the variables visible from the template (name-value pairs)
     * @return the output string of the template
     */
    String render(TemplateRule templateRule, Map<String, Object> model);

    /**
     * Renders the template with specified TemplateRule by its ID.
     * @param templateId the template id
     * @param activity the activity
     */
    void render(String templateId, Activity activity);

    /**
     * Renders the template with specified TemplateRule.
     * @param templateRule the template rule
     * @param activity the activity
     */
    void render(TemplateRule templateRule, Activity activity);

    /**
     * Renders the template with specified TemplateRule by its ID.
     * @param templateId the template id
     * @param activity the activity
     * @param model the holder of the variables visible from the template (name-value pairs)
     */
    void render(String templateId, Activity activity, Map<String, Object> model);

    /**
     * Renders the template with specified TemplateRule by its ID.
     * @param templateId the template id
     * @param activity the activity
     * @param writer the {@link Writer} where the output of the template will go.
     *         {@link Writer#close()} is not called.
     */
    void render(String templateId, Activity activity, Writer writer);

    /**
     * Renders the template with specified TemplateRule.
     * @param templateRule the template rule
     * @param activity the activity
     * @param model the holder of the variables visible from the template (name-value pairs)
     */
    void render(TemplateRule templateRule, Activity activity, Map<String, Object> model);

    /**
     * TRenders the template with specified TemplateRule by its ID.
     * Writing the generated output to the supplied {@link Writer}.
     * @param templateId the template id
     * @param activity the activity
     * @param model the holder of the variables visible from the template (name-value pairs)
     * @param writer the {@link Writer} where the output of the template will go.
     *         {@link Writer#close()} is not called.
     */
    void render(String templateId, Activity activity, Map<String, Object> model, Writer writer);

    /**
     * Renders the template with specified TemplateRule.
     * Writing the generated output to the supplied {@link Writer}.
     * @param templateRule the template rule
     * @param activity the activity
     * @param model the holder of the variables visible from the template (name-value pairs)
     * @param writer the {@link Writer} where the output of the template will go.
     *         {@link Writer#close()} is not called.
     */
    void render(TemplateRule templateRule, Activity activity, Map<String, Object> model, Writer writer);

}
