package com.aspectran.thymeleaf.context.web;

import com.aspectran.thymeleaf.context.ActivityExpressionContext;
import com.aspectran.utils.Assert;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.context.IWebContext;
import org.thymeleaf.web.IWebExchange;

import java.util.Locale;
import java.util.Map;

public class WebActivityExpressionContext extends ActivityExpressionContext implements IWebContext {

    private final IWebExchange webExchange;

    public WebActivityExpressionContext(IEngineConfiguration configuration,
                                        IWebExchange webExchange) {
        super(configuration);
        Assert.notNull(webExchange, "Web exchange cannot be null in web context");
        this.webExchange = webExchange;
    }

    public WebActivityExpressionContext(IEngineConfiguration configuration,
                                        IWebExchange webExchange, Locale locale) {
        super(configuration, locale);
        Assert.notNull(webExchange, "Web exchange cannot be null in web context");
        this.webExchange = webExchange;
    }

    public WebActivityExpressionContext(IEngineConfiguration configuration,
                                        IWebExchange webExchange, Locale locale,
                                        Map<String, Object> variables) {
        super(configuration, locale, variables);
        Assert.notNull(webExchange, "Web exchange cannot be null in web context");
        this.webExchange = webExchange;
    }

    @Override
    public IWebExchange getExchange() {
        return this.webExchange;
    }

}
