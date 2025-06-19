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
package com.aspectran.freemarker;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.component.template.engine.TemplateEngine;
import com.aspectran.core.component.template.engine.TemplateEngineProcessException;
import com.aspectran.utils.Assert;
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
        Assert.notNull(configuration, "configuration must not be null");
        this.configuration = configuration;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public void process(String templateName, Activity activity) throws TemplateEngineProcessException {
        checkHasConfiguration();
        try {
            process(configuration, templateName, activity);
            activity.getResponseAdapter().getWriter().flush();
        } catch (Exception e) {
            throw new TemplateEngineProcessException(e);
        }
    }

    @Override
    public void process(String templateSource, String contentType, Activity activity)
            throws TemplateEngineProcessException {
        checkHasConfiguration();
        try {
            Map<String, Object> variables = activity.getActivityData();
            Writer writer = activity.getResponseAdapter().getWriter();

            String templateName = createTemplateName(templateSource);
            Reader reader = new StringReader(templateSource);
            Template template = new Template(templateName, reader, configuration);
            template.process(variables, writer);

            writer.flush();
        } catch (Exception e) {
            throw new TemplateEngineProcessException(e);
        }
    }

    public static void process(Configuration configuration, String templateName, Activity activity)
            throws IOException, TemplateException {
        Assert.notNull(configuration, "configuration must not be null");
        Assert.notNull(templateName, "templateName must not be null");
        Assert.notNull(activity, "activity must not be null");

        Locale locale = activity.getRequestAdapter().getLocale();
        Map<String, Object> variables = activity.getActivityData();
        Writer writer = activity.getResponseAdapter().getWriter();

        Template template = configuration.getTemplate(templateName, locale);
        template.process(variables, writer);
    }

    private void checkHasConfiguration() {
        if (configuration == null) {
            throw new IllegalStateException("Configuration not specified");
        }
    }

    @NonNull
    private String createTemplateName(@NonNull String templateSource) {
        int hashCode = templateSource.hashCode();
        if (hashCode >= 0) {
            return Long.toString(hashCode, 32);
        } else {
            return Long.toString(hashCode & 0x7fffffff, 32);
        }
    }

}
