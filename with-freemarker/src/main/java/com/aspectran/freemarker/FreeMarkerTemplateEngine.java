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
package com.aspectran.freemarker;

import com.aspectran.core.component.template.engine.TemplateEngine;
import com.aspectran.core.component.template.engine.TemplateEngineProcessException;
import com.aspectran.utils.annotation.jsr305.NonNull;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.Locale;
import java.util.Map;

/**
 * The Class FreeMarkerTemplateEngine.
 *
 * <p>Created: 2016. 1. 9.</p>
 */
public class FreeMarkerTemplateEngine implements TemplateEngine {

    private final Configuration configuration;

    public FreeMarkerTemplateEngine(Configuration configuration) {
        this.configuration = configuration;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public void process(String templateName, Map<String, Object> model, Writer writer, Locale locale)
            throws TemplateEngineProcessException {
        try {
            Template template = configuration.getTemplate(templateName, locale);
            template.process(model, writer);
            writer.flush();
        } catch (Exception e) {
            throw new TemplateEngineProcessException(e);
        }
    }

    @Override
    public void process(String templateName, String templateSource, String contentType, Map<String, Object> model, Writer writer, Locale locale)
            throws TemplateEngineProcessException {
        try {
            Reader reader = new StringReader(templateSource);
            Template template = new Template(templateName, reader, configuration);
            template.process(model, writer);
            writer.flush();
        } catch (Exception e) {
            throw new TemplateEngineProcessException(e);
        }
    }

    public static void process(@NonNull Configuration configuration, String templateName, Map<String, Object> model, Writer writer, Locale locale)
            throws IOException, TemplateException {
        Template template = configuration.getTemplate(templateName);
        template.process(model, writer);
    }

}
