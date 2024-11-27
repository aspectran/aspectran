package com.aspectran.thymeleaf.context;

import com.aspectran.core.activity.Activity;
import com.aspectran.utils.Assert;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.context.AbstractExpressionContext;

import java.util.Locale;
import java.util.Map;

public class ActivityExpressionContext extends AbstractExpressionContext implements CurrentActivityHolder {

    private final Activity activity;

    public ActivityExpressionContext(Activity activity, IEngineConfiguration configuration) {
        super(configuration);
        Assert.notNull(activity, "activity must not be null");
        this.activity = activity;
    }

    public ActivityExpressionContext(Activity activity, IEngineConfiguration configuration, Locale locale) {
        super(configuration, locale);
        Assert.notNull(activity, "activity must not be null");
        this.activity = activity;
    }

    public ActivityExpressionContext(
            Activity activity, IEngineConfiguration configuration, Locale locale, Map<String, Object> variables) {
        super(configuration, locale, variables);
        Assert.notNull(activity, "activity must not be null");
        this.activity = activity;
    }

    @Override
    public Activity getActivity() {
        return activity;
    }

}
