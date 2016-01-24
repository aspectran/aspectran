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
package com.aspectran.core.context.template.engine.st;

import com.aspectran.core.context.template.engine.TemplateEngine;
import com.aspectran.core.context.template.engine.TemplateEngineProcessException;

import java.io.Reader;
import java.io.Writer;
import java.util.Locale;
import java.util.Map;

/**
 * <p>Created: 2016. 1. 9.</p>
 */
public class StringTemplateEngine implements TemplateEngine {

    @Override
    public void process(String templateName, Map<String, Object> dataModel, Reader reader, Writer writer) throws TemplateEngineProcessException {
    }

    @Override
    public void process(String templateName, Map<String, Object> dataModel, Writer writer) throws TemplateEngineProcessException {
    }

    @Override
    public void process(String templateName, Map<String, Object> dataModel, Writer writer, Locale locale) throws TemplateEngineProcessException {
    }

}
