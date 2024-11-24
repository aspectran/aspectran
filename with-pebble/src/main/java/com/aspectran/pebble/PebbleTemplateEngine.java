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
package com.aspectran.pebble;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.component.template.engine.TemplateEngine;
import com.aspectran.core.component.template.engine.TemplateEngineProcessException;
import com.aspectran.utils.Assert;
import com.aspectran.utils.annotation.jsr305.NonNull;
import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.template.PebbleTemplate;

import java.io.IOException;
import java.io.Writer;
import java.util.Locale;
import java.util.Map;

/**
 * The Class PebbleTemplateEngine.
 *
 * <p>Created: 2016. 1. 9.</p>
 */
public class PebbleTemplateEngine implements TemplateEngine {

    private final PebbleEngine pebbleEngine;

    public PebbleTemplateEngine(PebbleEngine pebbleEngine) {
        this.pebbleEngine = pebbleEngine;
    }

    public PebbleEngine getPebbleEngine() {
        return pebbleEngine;
    }

    @Override
    public void process(String templateName, Activity activity) throws TemplateEngineProcessException {
        checkHasEngine();
        try {
            process(pebbleEngine, templateName, activity);
            activity.getResponseAdapter().getWriter().flush();
        } catch (Exception e) {
            throw new TemplateEngineProcessException(e);
        }
    }

    @Override
    public void process(String templateSource, String contentType, Activity activity)
            throws TemplateEngineProcessException {
        checkHasEngine();
        try {
            processLiteral(pebbleEngine, templateSource, activity);
            activity.getResponseAdapter().getWriter().flush();
        } catch (Exception e) {
            throw new TemplateEngineProcessException(e);
        }
    }

    public static void process(PebbleEngine pebbleEngine, String templateName, Activity activity) throws IOException {
        Assert.notNull(pebbleEngine, "pebbleEngine must not be null");
        Assert.notNull(templateName, "templateName must not be null");
        Assert.notNull(activity, "activity must not be null");

        Locale locale = activity.getRequestAdapter().getLocale();
        Map<String, Object> variables = activity.getActivityData();
        Writer writer = activity.getResponseAdapter().getWriter();

        PebbleTemplate compiledTemplate = pebbleEngine.getTemplate(templateName);
        compiledTemplate.evaluate(writer, variables, locale);
    }

    private static void processLiteral(PebbleEngine pebbleEngine, String templateSource, Activity activity)
            throws IOException {
        Assert.notNull(pebbleEngine, "pebbleEngine must not be null");
        Assert.notNull(templateSource, "templateName must not be null");
        Assert.notNull(activity, "activity must not be null");

        Locale locale = activity.getRequestAdapter().getLocale();
        Map<String, Object> variables = activity.getActivityData();
        Writer writer = activity.getResponseAdapter().getWriter();

        PebbleTemplate compiledTemplate = pebbleEngine.getLiteralTemplate(templateSource);
        compiledTemplate.evaluate(writer, variables, locale);
    }

    private void checkHasEngine() {
        if (pebbleEngine == null) {
            throw new IllegalStateException("PebbleEngine not specified");
        }
    }

}
