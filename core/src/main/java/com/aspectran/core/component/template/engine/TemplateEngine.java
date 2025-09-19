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
package com.aspectran.core.component.template.engine;

import com.aspectran.core.activity.Activity;

/**
 * A generic interface for template engines.
 * This interface defines the contract for processing templates, either by name or from a string source,
 * and writing the output to the response of the current activity.
 *
 * <p>Created: 2016. 1. 9.</p>
 */
public interface TemplateEngine {

    /**
     * Processes a template by its name, using the data model from the activity,
     * and writes the generated output to the activity's response.
     * @param templateName the name of the template to process
     * @param activity the current activity which contains the data model and response writer
     * @throws TemplateEngineProcessException if an error occurs during template processing
     */
    void process(String templateName, Activity activity)
            throws TemplateEngineProcessException;

    /**
     * Processes a template from a string source, using the data model from the activity,
     * and writes the generated output to the activity's response.
     * @param templateSource the template content as a string
     * @param contentType the content type of the template
     * @param activity the current activity which contains the data model and response writer
     * @throws TemplateEngineProcessException if an error occurs during template processing
     */
    void process(String templateSource, String contentType, Activity activity)
            throws TemplateEngineProcessException;

}
