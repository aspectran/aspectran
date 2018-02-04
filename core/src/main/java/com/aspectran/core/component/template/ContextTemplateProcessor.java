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
package com.aspectran.core.component.template;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.ActivityDataMap;
import com.aspectran.core.component.AbstractComponent;
import com.aspectran.core.component.template.engine.TemplateEngine;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.expr.TokenEvaluator;
import com.aspectran.core.context.expr.TokenExpressionParser;
import com.aspectran.core.context.expr.token.Token;
import com.aspectran.core.context.rule.TemplateRule;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Locale;
import java.util.Map;

/**
 * The Class ContextTemplateProcessor.
 *
 * <p>Created: 2016. 1. 14.</p>
 */
public class ContextTemplateProcessor extends AbstractComponent implements TemplateProcessor {

    private final ActivityContext context;

    private final TemplateRuleRegistry templateRuleRegistry;

    /**
     * Instantiates a new context template processor.
     *
     * @param context the activity context
     * @param templateRuleRegistry the template rule registry
     */
    public ContextTemplateProcessor(ActivityContext context, TemplateRuleRegistry templateRuleRegistry) {
        this.context = context;
        this.templateRuleRegistry = templateRuleRegistry;
    }

    @Override
    public String process(String templateId) {
        StringWriter writer = new StringWriter();
        process(templateId, null, null, writer);
        return writer.toString();
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
    public void process(String templateId, Activity activity, Writer writer) {
        process(templateId, activity, null, writer);
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
            if (activity == null) {
                activity = context.getCurrentActivity();
            }

            if (writer == null) {
                if (activity.getResponseAdapter() != null) {
                    writer = activity.getResponseAdapter().getWriter();
                }
                if (writer == null) {
                    throw new IllegalStateException("No such writer to transfer the output string");
                }
            }

            if (templateRule.isExternalEngine()) {
                TemplateEngine engine = null;
                if (templateRule.getEngineBeanClass() != null) {
                    engine = (TemplateEngine)activity.getBean(templateRule.getEngineBeanClass());
                } else if (templateRule.getEngineBeanId() != null) {
                    engine = activity.getBean(templateRule.getEngineBeanId());
                }
                if (engine == null) {
                    throw new IllegalArgumentException("No template engine bean type for '" + templateRule.getEngine() + "'");
                }

                if (model == null) {
                    if (activity.getTranslet() != null) {
                        model = activity.getTranslet().getActivityDataMap();
                    } else {
                        model = new ActivityDataMap(activity);
                    }
                }

                if (templateRule.isOutsourcing()) {
                    String templateName = templateRule.getName();
                    Locale locale = (activity.getRequestAdapter() != null ? activity.getRequestAdapter().getLocale() : null);
                    engine.process(templateName, model, writer, locale);
                } else {
                    String templateSource = templateRule.getTemplateSource(context.getEnvironment());
                    if (templateSource != null) {
                        String templateName = templateRule.getId();
                        if (templateName == null) {
                            templateName = templateRule.getEngine() + "/" + templateRule.hashCode();
                        }
                        engine.process(templateName, model, templateSource, writer);
                    }
                }
            } else {
                Token[] templateTokens = templateRule.getTemplateTokens(context.getEnvironment());
                if (templateTokens != null) {
                    TokenEvaluator evaluator = new TokenExpressionParser(activity);
                    evaluator.evaluate(templateTokens, writer);
                } else {
                    writer.write(templateRule.getTemplateSource(context.getEnvironment()));
                }
            }
        } catch (Exception e) {
            throw new TemplateProcessorException("An error occurred while processing a template", templateRule, e);
        }
    }

    @Override
    protected void doInitialize() throws Exception {
        templateRuleRegistry.initialize();
    }

    @Override
    protected void doDestroy() {
        templateRuleRegistry.destroy();
    }

}
