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
package com.aspectran.core.component.template;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.ActivityData;
import com.aspectran.core.component.AbstractComponent;
import com.aspectran.core.component.template.engine.TemplateEngine;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.expr.TokenEvaluation;
import com.aspectran.core.context.expr.TokenEvaluator;
import com.aspectran.core.context.expr.token.Token;
import com.aspectran.core.context.rule.TemplateRule;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Locale;
import java.util.Map;

/**
 * The Class DefaultTemplateRenderer.
 *
 * <p>Created: 2016. 1. 14.</p>
 */
public class DefaultTemplateRenderer extends AbstractComponent implements TemplateRenderer {

    private final ActivityContext context;

    private final TemplateRuleRegistry templateRuleRegistry;

    /**
     * Instantiates a new context template renderer.
     * @param context the activity context
     * @param templateRuleRegistry the template rule registry
     */
    public DefaultTemplateRenderer(ActivityContext context, TemplateRuleRegistry templateRuleRegistry) {
        this.context = context;
        this.templateRuleRegistry = templateRuleRegistry;
    }

    @Override
    public String render(String templateId) {
        StringWriter writer = new StringWriter();
        render(templateId, null, null, writer);
        return writer.toString();
    }

    @Override
    public String render(String templateId, Map<String, Object> model) {
        StringWriter writer = new StringWriter();
        render(templateId, null, model, writer);
        return writer.toString();
    }

    @Override
    public String render(TemplateRule templateRule, Map<String, Object> model) {
        StringWriter writer = new StringWriter();
        render(templateRule, null, model, writer);
        return writer.toString();
    }

    @Override
    public void render(String templateId, Activity activity) {
        render(templateId, activity, null, null);
    }

    @Override
    public void render(TemplateRule templateRule, Activity activity) {
        render(templateRule, activity, null, null);
    }

    @Override
    public void render(String templateId, Activity activity, Map<String, Object> model) {
        render(templateId, activity, model, null);
    }

    @Override
    public void render(String templateId, Activity activity, Writer writer) {
        render(templateId, activity, null, writer);
    }

    @Override
    public void render(TemplateRule templateRule, Activity activity, Map<String, Object> model) {
        render(templateRule, activity, model, null);
    }

    @Override
    public void render(String templateId, Activity activity, Map<String, Object> model, Writer writer) {
        if (templateId == null) {
            throw new IllegalArgumentException("templateId must not be null");
        }

        TemplateRule templateRule = templateRuleRegistry.getTemplateRule(templateId);
        if (templateRule == null) {
            throw new TemplateNotFoundException(templateId);
        }

        render(templateRule, activity, model, writer);
    }

    @Override
    public void render(TemplateRule templateRule, Activity activity, Map<String, Object> model, Writer writer) {
        try {
            if (activity == null) {
                activity = context.getAvailableActivity();
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
                    throw new IllegalArgumentException("No template engine bean named '" +
                            templateRule.getEngine() + "'");
                }

                if (model == null) {
                    if (activity.getTranslet() != null) {
                        model = activity.getTranslet().getActivityData();
                    } else {
                        model = new ActivityData(activity);
                    }
                }

                if (templateRule.isOutsourcing()) {
                    String templateName = templateRule.getName();
                    Locale locale = (activity.getRequestAdapter() != null ? activity.getRequestAdapter().getLocale() : null);
                    engine.process(templateName, model, writer, locale);
                } else {
                    String templateSource = templateRule.getTemplateSource(context);
                    if (templateSource != null) {
                        String templateName = templateRule.getId();
                        if (templateName == null) {
                            templateName = templateRule.getEngine() + "/" + templateRule.hashCode();
                        }
                        String contentType = templateRule.getContentType();
                        if (contentType == null && activity.getResponseAdapter() != null) {
                            contentType = activity.getResponseAdapter().getContentType();
                        }
                        Locale locale = (activity.getRequestAdapter() != null ? activity.getRequestAdapter().getLocale() : null);
                        engine.process(templateName, templateSource, contentType, model, writer, locale);
                    }
                }
            } else {
                Token[] templateTokens = templateRule.getTemplateTokens(context);
                if (templateTokens != null) {
                    TokenEvaluator evaluator = new TokenEvaluation(activity);
                    evaluator.evaluate(templateTokens, writer);
                } else {
                    writer.write(templateRule.getTemplateSource(context));
                }
            }
        } catch (Exception e) {
            throw new TemplateRenderingException("An error occurred during rendering of the template", templateRule, e);
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
