package com.aspectran.thymeleaf.context.web;

import com.aspectran.core.activity.Activity;
import com.aspectran.thymeleaf.context.CurrentActivityHolder;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IContext;
import org.thymeleaf.context.IEngineContext;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.context.WebEngineContext;
import org.thymeleaf.engine.TemplateData;
import org.thymeleaf.web.IWebExchange;

import java.util.Locale;
import java.util.Map;

/**
 * <p>Created: 2024-11-27</p>
 */
public class WebActivityEngineContext extends WebEngineContext implements CurrentActivityHolder {

    private final Activity activity;

    /**
     * Creates a new instance of this {@link IEngineContext} implementation binding engine execution to
     * the Servlet API.
     * <p>
     * Note that implementations of {@link IEngineContext} are not meant to be used in order to call
     * the template engine (use implementations of {@link IContext} such as {@link Context} or {@link WebContext}
     * instead). This is therefore mostly an <b>internal</b> implementation, and users should have no reason
     * to ever call this constructor except in very specific integration/extension scenarios.
     * </p>
     * @param activity the aspectran activity
     * @param configuration the configuration instance being used
     * @param templateData the template data for the template to be processed
     * @param templateResolutionAttributes the template resolution attributes
     * @param webExchange the web exchange object
     * @param locale the locale
     * @param variables the context variables, probably coming from another {@link IContext} implementation
     */
    public WebActivityEngineContext(
            Activity activity, IEngineConfiguration configuration, TemplateData templateData,
            Map<String, Object> templateResolutionAttributes, IWebExchange webExchange,
            Locale locale, Map<String, Object> variables) {
        super(configuration, templateData, templateResolutionAttributes, webExchange, locale, variables);
        this.activity = activity;
    }

    @Override
    public Activity getActivity() {
        return activity;
    }

}
