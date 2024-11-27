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
import com.aspectran.core.activity.InstantActivity;
import com.aspectran.core.activity.InstantActivityException;
import com.aspectran.core.component.AbstractComponent;
import com.aspectran.core.component.template.engine.TemplateEngine;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.asel.token.Token;
import com.aspectran.core.context.rule.TemplateRule;
import com.aspectran.utils.Assert;

import java.io.Writer;

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
        Assert.notNull(context, "context must not be null");
        Assert.notNull(templateRuleRegistry, "templateRuleRegistry must not be null");
        this.context = context;
        this.templateRuleRegistry = templateRuleRegistry;
    }

    @Override
    public String render(String templateId) {
        try {
            InstantActivity activity = new InstantActivity(context);
            return activity.perform(() -> {
                render(templateId, activity);
                return activity.getResponseAdapter().getWriter().toString();
            });
        } catch (Exception e) {
            throw new InstantActivityException(e);
        }
    }

    @Override
    public void render(String templateId, Activity activity) {
        Assert.notNull(templateId, "templateId must not be null");

        TemplateRule templateRule = templateRuleRegistry.getTemplateRule(templateId);
        if (templateRule == null) {
            throw new TemplateNotFoundException(templateId);
        }

        render(templateRule, activity);
    }

    @Override
    public void render(TemplateRule templateRule, Activity activity) {
        try {
            Assert.notNull(templateRule, "templateRule must not be null");
            Assert.notNull(activity, "activity must not be null");

            Writer writer = null;
            if (activity.getResponseAdapter() != null) {
                writer = activity.getResponseAdapter().getWriter();
            }
            if (writer == null) {
                throw new IllegalStateException("No such writer to transfer the output string");
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

                if (templateRule.isOutsourcing()) {
                    String templateName = templateRule.getName();
                    engine.process(templateName, activity);
                } else {
                    String templateSource = templateRule.getTemplateSource(context);
                    if (templateSource != null) {
                        String contentType = templateRule.getContentType();
                        if (contentType == null && activity.getResponseAdapter() != null) {
                            contentType = activity.getResponseAdapter().getContentType();
                        }
                        engine.process(templateSource, contentType, activity);
                    }
                }
            } else {
                Token[] templateTokens = templateRule.getTemplateTokens(context);
                if (templateTokens != null) {
                    activity.getTokenEvaluator().evaluate(templateTokens, writer);
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
