package com.aspectran.thymeleaf.context;

import com.aspectran.core.activity.Activity;
import com.aspectran.thymeleaf.context.web.WebActivityEngineContext;
import com.aspectran.utils.Assert;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.context.Contexts;
import org.thymeleaf.context.IContext;
import org.thymeleaf.context.IEngineContext;
import org.thymeleaf.context.IEngineContextFactory;
import org.thymeleaf.context.IWebContext;
import org.thymeleaf.engine.TemplateData;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * <p>Created: 2024-11-27</p>
 */
public class ActivityEngineContextFactory implements IEngineContextFactory {

    @Override
    public IEngineContext createEngineContext(
            IEngineConfiguration configuration, TemplateData templateData,
            Map<String, Object> templateResolutionAttributes, IContext context) {
        Assert.notNull(context, "Context object cannot be null");

        // NOTE calling getVariableNames() on an IWebContext would be very expensive, as it would mean
        // calling HttpServletRequest#getAttributeNames(), which is very slow in some common implementations
        // (e.g. Apache Tomcat). So it's a good thing we might have tried to reuse the IEngineContext
        // before calling this factory.
        Set<String> variableNames = context.getVariableNames();
        Activity activity = (context instanceof CurrentActivityHolder holder ? holder.getActivity() : null);

        if (variableNames == null || variableNames.isEmpty()) {
            if (Contexts.isWebContext(context)) {
                IWebContext webContext = Contexts.asWebContext(context);
                return new WebActivityEngineContext(
                        activity, configuration, templateData, templateResolutionAttributes,
                        webContext.getExchange(), webContext.getLocale(), Collections.emptyMap());
            } else {
                return new ActivityEngineContext(
                        activity, configuration, templateData, templateResolutionAttributes,
                        context.getLocale(), Collections.emptyMap());
            }
        } else {
            Map<String, Object> variables = new LinkedHashMap<>(variableNames.size() + 1, 1.0f);
            for (String variableName : variableNames) {
                variables.put(variableName, context.getVariable(variableName));
            }
            if (Contexts.isWebContext(context)) {
                IWebContext webContext = Contexts.asWebContext(context);
                return new WebActivityEngineContext(
                        activity, configuration, templateData, templateResolutionAttributes,
                        webContext.getExchange(), webContext.getLocale(), variables);
            } else {
                return new ActivityEngineContext(
                        activity, configuration, templateData, templateResolutionAttributes,
                        context.getLocale(), variables);
            }
        }
    }

}
