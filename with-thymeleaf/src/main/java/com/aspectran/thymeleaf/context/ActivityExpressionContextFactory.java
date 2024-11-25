package com.aspectran.thymeleaf.context;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.process.result.ActionResult;
import com.aspectran.core.activity.process.result.ContentResult;
import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.thymeleaf.context.web.WebActivityExchange;
import com.aspectran.thymeleaf.context.web.WebActivityExpressionContext;
import com.aspectran.utils.Assert;
import com.aspectran.utils.annotation.jsr305.NonNull;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.web.IWebExchange;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public abstract class ActivityExpressionContextFactory {

    @NonNull
    public static ActivityExpressionContext create(
            Activity activity, IEngineConfiguration configuration, Locale locale) {
        Assert.notNull(activity, "activity cannot be null");
        if (activity.getMode() == Activity.Mode.WEB) {
            IWebExchange webExchange = WebActivityExchange.buildExchange(activity);
            Map<String, Object> variables = toVariables(activity.getProcessResult());
            return new WebActivityExpressionContext(configuration, webExchange, locale, variables);
        } else {
            Map<String, Object> variables = activity.getActivityData();
            return new ActivityExpressionContext(configuration, locale, variables);
        }
    }

    private static Map<String, Object> toVariables(ProcessResult processResult) {
        if (processResult != null) {
            Map<String, Object> variables = new HashMap<>();
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
