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
package com.aspectran.thymeleaf;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.component.template.engine.TemplateEngine;
import com.aspectran.core.component.template.engine.TemplateEngineProcessException;
import com.aspectran.thymeleaf.expression.ActivityExpressionContext;
import com.aspectran.thymeleaf.expression.ActivityExpressionContextFactory;
import com.aspectran.utils.Assert;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.TemplateSpec;
import org.thymeleaf.context.ExpressionContext;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.standard.expression.FragmentExpression;
import org.thymeleaf.standard.expression.IStandardExpressionParser;
import org.thymeleaf.standard.expression.StandardExpressions;

import java.io.IOException;
import java.io.Writer;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * The Class ThymeleafTemplateEngine.
 *
 * <p>Created: 2024. 11. 18.</p>
 *
 * @since 8.2.0
 */
public class ThymeleafTemplateEngine implements TemplateEngine {

    private final ITemplateEngine templateEngine;

    public ThymeleafTemplateEngine(ITemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public ITemplateEngine getTemplateEngine() {
        return templateEngine;
    }

    @Override
    public void process(String templateName, Activity activity) throws TemplateEngineProcessException {
        checkHasEngine();
        try {
            process(templateEngine, templateName, activity);
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
            Locale locale = activity.getRequestAdapter().getLocale();
            Map<String, Object> variables = activity.getActivityData();
            Writer writer = activity.getResponseAdapter().getWriter();

            IEngineConfiguration configuration = templateEngine.getConfiguration();
            ExpressionContext context = new ExpressionContext(configuration, locale, variables);
            TemplateSpec templateSpec = new TemplateSpec(templateSource, contentType);
            templateEngine.process(templateSpec, context, writer);

            writer.flush();
        } catch (Exception e) {
            throw new TemplateEngineProcessException(e);
        }
    }

    public static void process(ITemplateEngine templateEngine, String templateName, Activity activity)
            throws IOException {
        Assert.notNull(templateEngine, "templateEngine must not be null");
        Assert.notNull(templateName, "templateName must not be null");
        Assert.notNull(activity, "activity must not be null");

        Locale locale = activity.getRequestAdapter().getLocale();
        Map<String, Object> variables = activity.getActivityData();
        Writer writer = activity.getResponseAdapter().getWriter();

        IEngineConfiguration configuration = templateEngine.getConfiguration();
        ActivityExpressionContext context = ActivityExpressionContextFactory.create(activity, configuration, locale, variables);

        String templateNameToUse;
        Set<String> markupSelectors;
        if (!templateName.contains("::")) {
            // No fragment specified at the template name
            templateNameToUse = templateName;
            markupSelectors = null;
        } else {
            // Template name contains a fragment name, so we should parse it as such

            IStandardExpressionParser parser = StandardExpressions.getExpressionParser(configuration);
            FragmentExpression fragmentExpression;
            try {
                // By parsing it as a standard expression, we might profit from the expression cache
                fragmentExpression = (FragmentExpression)parser.parseExpression(context, "~{" + templateName + "}");
            } catch (TemplateProcessingException e) {
                throw new IllegalArgumentException("Invalid template name specification: '" + templateName + "'");
            }

            FragmentExpression.ExecutedFragmentExpression fragment =
                FragmentExpression.createExecutedFragmentExpression(context, fragmentExpression);

            templateNameToUse = FragmentExpression.resolveTemplateName(fragment);
            markupSelectors = FragmentExpression.resolveFragments(fragment);

            Map<String,Object> nameFragmentParameters = fragment.getFragmentParameters();
            if (nameFragmentParameters != null) {
                if (fragment.hasSyntheticParameters()) {
                    // We cannot allow synthetic parameters because there is no way to specify them at the template
                    // engine execution!
                    throw new IllegalArgumentException(
                        "Parameters in a view specification must be named (non-synthetic): '" + templateName + "'");
                }
                context.setVariables(nameFragmentParameters);
            }
        }

        templateEngine.process(templateNameToUse, markupSelectors, context, writer);
    }

    private void checkHasEngine() {
        if (templateEngine == null) {
            throw new IllegalStateException("TemplateEngine not specified");
        }
    }

}
