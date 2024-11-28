package com.aspectran.thymeleaf.context;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.process.result.ActionResult;
import com.aspectran.core.activity.process.result.ContentResult;
import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.thymeleaf.context.web.WebActivityEngineContext;
import com.aspectran.utils.Assert;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.context.Contexts;
import org.thymeleaf.context.IContext;
import org.thymeleaf.context.IEngineContext;
import org.thymeleaf.context.IEngineContextFactory;
import org.thymeleaf.context.IWebContext;
import org.thymeleaf.engine.TemplateData;

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

        Activity activity = (context instanceof CurrentActivityHolder holder ? holder.getActivity() : null);
        Assert.notNull(activity, "Activity object cannot be null");

        Map<String, Object> variables;
        if (Contexts.isWebContext(context)) {
            variables = toVariables(activity.getProcessResult());
        } else {
            variables = activity.getActivityData();
        }

        Set<String> variableNames = context.getVariableNames();
        if (variableNames != null && !variableNames.isEmpty()) {
            if (variables == null) {
                variables = new LinkedHashMap<>(variableNames.size() + 1, 1.0f);
            }
            for (String variableName : variableNames) {
                variables.put(variableName, context.getVariable(variableName));
            }
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

    private static Map<String, Object> toVariables(ProcessResult processResult) {
        if (processResult != null) {
            Map<String, Object> variables = new LinkedHashMap<>();
            for (ContentResult cr : processResult) {
                for (ActionResult ar : cr) {
                    if (ar.getActionId() != null) {
                        variables.put(ar.getActionId(), ar.getResultValue());
                    }
                }
            }
            return variables;
        } else {
            return null;
        }
    }

}
