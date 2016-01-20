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
package com.aspectran.core.context.template;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.rule.TemplateRule;
import com.aspectran.core.context.template.engine.TemplateEngine;
import com.aspectran.core.context.template.engine.TemplateEngineException;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

/**
 * Created by gulendol on 2016. 1. 14..
 */
public class ContextTemplateProcessor implements TemplateProcessor {

    private final Log log = LogFactory.getLog(ContextTemplateProcessor.class);

    protected ActivityContext context;

    protected final TemplateRuleRegistry templateRuleRegistry;

    private boolean initialized;

    public ContextTemplateProcessor(TemplateRuleRegistry templateRuleRegistry) {
        this.templateRuleRegistry = templateRuleRegistry;
    }

    public TemplateRuleRegistry getTemplateRuleRegistry() {
        return templateRuleRegistry;
    }

    public String process(String templateId, Reader reader) {
        StringWriter writer = new StringWriter();
        process(templateId, reader, writer);

        return writer.toString();
    }

    public void process(String templateId, Reader reader, Writer writer) {
    	Activity activity = context.getCurrentActivity();

        TemplateDataMap templateDataMap = new TemplateDataMap(activity);

        process(templateId, templateDataMap, reader, writer);
    }
    
    public void process(String templateId, Map<String, String> dataModel, Reader reader, Writer writer) {
    	TemplateRule templateRule = templateRuleRegistry.getTemplateRule(templateId);

        if(templateRule == null) {
            throw new TemplateNotFoundException(templateId);
        }

        process(templateRule, dataModel, reader, writer);
    }
    
    private void process(TemplateRule templateRule, Map<String, String> dataModel, Reader reader, Writer writer) {
    	String engineBeanId = templateRule.getEngine();

        TemplateEngine engine = context.getBeanRegistry().getBean(engineBeanId);

        if(engine == null) {
            throw new TemplateEngineException(templateRule, "Template Engine Bean is not registered.");
        }

        engine.process(templateRule, dataModel, reader, writer);
    }
    
    public synchronized void initialize(ActivityContext context) {
        if(initialized) {
            throw new UnsupportedOperationException("TemplateProcessor has already been initialized.");
        }

        this.context = context;

        initialized = true;

        log.info("TemplateProcessor has been initialized successfully.");
    }

    public synchronized void destroy() {
        if(!initialized) {
            throw new UnsupportedOperationException("TemplateProcessor has not yet initialized.");
        }

        initialized = false;

        log.info("TemplateProcessor has been destroyed successfully.");
    }

}
