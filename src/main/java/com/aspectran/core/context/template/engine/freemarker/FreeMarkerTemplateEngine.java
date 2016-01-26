/**
 *    Copyright 2009-2015 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.aspectran.core.context.template.engine.freemarker;

import com.aspectran.core.context.template.engine.TemplateEngine;
import com.aspectran.core.context.template.engine.TemplateEngineProcessException;
import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.Locale;
import java.util.Map;

/**
 * The Class FreemakerTemplateEngine.
 *
 * <p>Created: 2016. 1. 9.</p>
 */
public class FreeMarkerTemplateEngine implements TemplateEngine {

    private Configuration configuration;

    public FreeMarkerTemplateEngine(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void process(String templateName, Map<String, Object> model, String templateSource, Writer writer) throws TemplateEngineProcessException {
        try {
        	Reader reader = new StringReader(templateSource);
        	Template template = new Template(templateName, reader, configuration);
            template.process(model, writer);
        } catch(Exception e) {
            throw new TemplateEngineProcessException(e);
        }
    }

    @Override
    public void process(String templateName, Map<String, Object> model, Writer writer) throws TemplateEngineProcessException {
        process(templateName, model, writer, null);
    }

    @Override
    public void process(String templateName, Map<String, Object> model, Writer writer, Locale locale) throws TemplateEngineProcessException {
        try {
            Template template = configuration.getTemplate(templateName, locale);
            template.process(model, writer);
        } catch(Exception e) {
            throw new TemplateEngineProcessException(e);
        }
    }

}
