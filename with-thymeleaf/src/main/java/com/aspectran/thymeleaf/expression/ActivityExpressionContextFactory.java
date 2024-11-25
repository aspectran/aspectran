package com.aspectran.thymeleaf.expression;

import com.aspectran.core.activity.Activity;
import com.aspectran.thymeleaf.expression.web.WebActivityExchange;
import com.aspectran.utils.Assert;
import com.aspectran.utils.annotation.jsr305.NonNull;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.web.IWebExchange;

import java.util.Locale;
import java.util.Map;

public abstract class ActivityExpressionContextFactory {

    @NonNull
    public static ActivityExpressionContext create(Activity activity, IEngineConfiguration configuration, Locale locale) {
        return create(activity, configuration, locale, null);
    }

    @NonNull
    public static ActivityExpressionContext create(
            Activity activity, IEngineConfiguration configuration, Locale locale, Map<String, Object> variables) {
        Assert.notNull(activity, "activity cannot be null");
        if (activity.getMode() == Activity.Mode.WEB) {
            IWebExchange webExchange = WebActivityExchange.buildExchange(activity);
            return new WebActivityExpressionContext(configuration, webExchange, locale, variables);
        } else {
            return new ActivityExpressionContext(configuration, locale, variables);
        }
    }

}
