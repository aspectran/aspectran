package com.aspectran.thymeleaf;

import com.aspectran.core.support.i18n.message.MessageSource;
import com.aspectran.thymeleaf.context.ActivityEngineContextFactory;
import com.aspectran.thymeleaf.dialect.AspectranStandardDialect;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.IEngineContextFactory;
import org.thymeleaf.dialect.IDialect;

/**
 * <p>Created: 2024. 11. 25.</p>
 */
public class AspectranTemplateEngine extends TemplateEngine {

    public static final IDialect DIALECT = new AspectranStandardDialect();

    public static final IEngineContextFactory ENGINE_CONTEXT_FACTORY = new ActivityEngineContextFactory();

    public AspectranTemplateEngine() {
        super();
        setDialect(DIALECT);
        setEngineContextFactory(ENGINE_CONTEXT_FACTORY);
    }

    public void setMessageSource(MessageSource messageSource) {
        setMessageResolver(new AspectranMessageResolver(messageSource));
    }

}
