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
package com.aspectran.core.component.template.engine;

import java.io.Writer;
import java.util.Locale;
import java.util.Map;

/**
 * The Interface TemplateEngine.
 *
 * <p>Created: 2016. 1. 9.</p>
 */
public interface TemplateEngine {

    /**
     * Executes template, using the data-model provided, writing the generated output to the supplied {@link Writer}.
     *
     * @param templateName the template name
     * @param model the holder of the variables visible from the template (name-value pairs)
     * @param templateSource the template source
     * @param writer the {@link Writer} where the output of the template will go. {@link Writer#close()} is not called.
     * @throws TemplateEngineProcessException if an exception occurs during template processing
     */
    void process(String templateName, Map<String, Object> model, String templateSource, Writer writer)
            throws TemplateEngineProcessException;

    /**
     * Executes template, using the data-model provided, writing the generated output to the supplied {@link Writer}.
     *
     * @param templateName the template name
     * @param model the holder of the variables visible from the template (name-value pairs)
     * @param writer the {@link Writer} where the output of the template will go. {@link Writer#close()} is not called.
     * @throws TemplateEngineProcessException if an exception occurs during template processing
     */
    void process(String templateName, Map<String, Object> model, Writer writer) throws TemplateEngineProcessException;

    /**
     * Executes template, using the data-model provided, writing the generated output to the supplied {@link Writer}.
     *
     * @param templateName the template name
     * @param model the holder of the variables visible from the template (name-value pairs)
     * @param writer the {@link Writer} where the output of the template will go. {@link Writer#close()} is not called.
     * @param locale the locale
     * @throws TemplateEngineProcessException if an exception occurs during template processing
     */
    void process(String templateName, Map<String, Object> model, Writer writer, Locale locale)
            throws TemplateEngineProcessException;

}
