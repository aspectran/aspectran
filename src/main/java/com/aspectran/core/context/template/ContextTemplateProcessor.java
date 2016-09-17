/**
 * Copyright 2008-2016 Juho Jeong
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
package com.aspectran.core.context.template;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Locale;
import java.util.Map;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.ActivityDataMap;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.expr.TokenEvaluator;
import com.aspectran.core.context.expr.TokenExpressionParser;
import com.aspectran.core.context.expr.token.Token;
import com.aspectran.core.context.rule.TemplateRule;
import com.aspectran.core.context.template.engine.TemplateEngine;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * The Class ContextTemplateProcessor.
 *
 * <p>Created: 2016. 1. 14.</p>
 */
public class ContextTemplateProcessor implements TemplateProcessor {

    private final Log log = LogFactory.getLog(ContextTemplateProcessor.class);

    private final TemplateRuleRegistry templateRuleRegistry;

    private ActivityContext context;

    private boolean active;

    private boolean closed;

    /**
     * Instantiates a new context template processor.
     *
     * @param templateRuleRegistry the template rule registry
     */
    public ContextTemplateProcessor(TemplateRuleRegistry templateRuleRegistry) {
        this.templateRuleRegistry = templateRuleRegistry;
    }

    @Override
    public String process(String templateId, Map<String, Object> model) {
        StringWriter writer = new StringWriter();
        process(templateId, null, model, writer);
        return writer.toString();
    }

    @Override
    public String process(TemplateRule templateRule, Map<String, Object> model) {
        StringWriter writer = new StringWriter();
        process(templateRule, null, model, writer);
        return writer.toString();
    }

    @Override
    public void process(String templateId, Activity activity) {
        process(templateId, activity, null, null);
    }

    @Override
    public void process(TemplateRule templateRule, Activity activity) {
        process(templateRule, activity, null, null);
    }

    @Override
    public void process(String templateId, Activity activity, Map<String, Object> model) {
        process(templateId, activity, model, null);
    }

    @Override
    public void process(TemplateRule templateRule, Activity activity, Map<String, Object> model) {
        process(templateRule, activity, model, null);
    }

    @Override
    public void process(String templateId, Activity activity, Map<String, Object> model, Writer writer) {
    	TemplateRule templateRule = templateRuleRegistry.getTemplateRule(templateId);
        if (templateRule == null) {
            throw new TemplateNotFoundException(templateId);
        }
        process(templateRule, activity, model, writer);
    }

    @Override
    public void process(TemplateRule templateRule, Activity activity, Map<String, Object> model, Writer writer) {
        try {
            if(activity == null) {
                activity = context.getCurrentActivity();
            }

            if(writer == null) {
                writer = (activity.getResponseAdapter() != null ? activity.getResponseAdapter().getWriter() : null);
                if(writer == null) {
                    throw new IllegalStateException("No such writer to transfer the output string.");
                }
            }

            String engineBeanId = templateRule.getEngine();

            if (engineBeanId != null) {
                TemplateEngine engine = context.getBeanRegistry().getBean(engineBeanId);
                if (engine == null) {
                    throw new IllegalArgumentException("No template engine bean registered for '" + engineBeanId + "'.");
                }

                if(model == null) {
                    if(activity.getTranslet() != null) {
                        model = activity.getTranslet().getActivityDataMap();
                    } else {
                        model = new ActivityDataMap(activity);
                    }
                }

                if (templateRule.isUseExternalSource()) {
                    String templateName = templateRule.getName();
                    Locale locale = (activity.getRequestAdapter() != null ? activity.getRequestAdapter().getLocale() : null);
                    engine.process(templateName, model, writer, locale);
                } else {
                    String templateSource = templateRule.getTemplateSource(activity.getApplicationAdapter());
                    if (templateSource != null) {
                        String templateName = templateRule.getId();
                        if (templateName == null) {
                            templateName = templateRule.getEngine() + "/" + templateRule.hashCode();
                        }
                        engine.process(templateName, model, templateSource, writer);
                    }
                }
            } else {
                Token[] contentTokens = templateRule.getContentTokens(context.getApplicationAdapter());
                if (contentTokens != null) {
                    TokenEvaluator evaluator = new TokenExpressionParser(activity);
                    evaluator.evaluate(contentTokens, writer);
                }
            }
        } catch (Exception e) {
            throw new TemplateProcessorException("Failed to process the template", templateRule, e);
        }
    }

    /**
     * Initialize TemplateProcessor.
     */
    public synchronized void initialize(ActivityContext context) {
        if (this.active) {
            log.warn("Template Processor has already been initialized.");
            return;
        }

        this.context = context;

        this.closed = false;
        this.active = true;

        log.info("Template Processor has been initialized.");
    }

    /**
     * Destroy TemplateProcessor.
     */
    public synchronized void destroy() {
        if (this.active && !this.closed) {
            templateRuleRegistry.clear();
            this.closed = true;
            this.active = false;

            log.info("Template Processor has been destroyed.");
        }
    }

}
