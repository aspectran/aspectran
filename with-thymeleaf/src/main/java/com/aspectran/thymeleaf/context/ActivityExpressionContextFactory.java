package com.aspectran.thymeleaf.context;

import com.aspectran.core.activity.Activity;
import com.aspectran.thymeleaf.context.web.WebActivityExchange;
import com.aspectran.thymeleaf.context.web.WebActivityExpressionContext;
import com.aspectran.utils.Assert;
import com.aspectran.utils.annotation.jsr305.NonNull;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.web.IWebExchange;

import java.util.Locale;

public abstract class ActivityExpressionContextFactory {

    @NonNull
    public static ActivityExpressionContext create(
            Activity activity, IEngineConfiguration configuration, Locale locale) {
        Assert.notNull(activity, "activity cannot be null");
        if (activity.getMode() == Activity.Mode.WEB) {
            IWebExchange webExchange = WebActivityExchange.buildExchange(activity);
            return new WebActivityExpressionContext(activity, configuration, webExchange, locale);
        } else {
            return new ActivityExpressionContext(activity, configuration, locale);
        }
    }

}
