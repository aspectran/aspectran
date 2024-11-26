package com.aspectran.thymeleaf.context;

import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.context.AbstractExpressionContext;

import java.util.Locale;
import java.util.Map;

public class ActivityExpressionContext extends AbstractExpressionContext {

    public ActivityExpressionContext(IEngineConfiguration configuration) {
        super(configuration);
    }

    public ActivityExpressionContext(IEngineConfiguration configuration, Locale locale) {
        super(configuration, locale);
    }

    public ActivityExpressionContext(
            IEngineConfiguration configuration, Locale locale, Map<String, Object> variables) {
        super(configuration, locale, variables);
    }

}
