package com.aspectran.thymeleaf;

import com.aspectran.core.support.i18n.message.MessageSource;
import com.aspectran.thymeleaf.dialect.AspectranStandardDialect;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.dialect.IDialect;

/**
 * <p>Created: 2024. 11. 25.</p>
 */
public class AspectranTemplateEngine extends TemplateEngine {

    public static final IDialect DIALECT = new AspectranStandardDialect();

    public AspectranTemplateEngine() {
        super();
        setDialect(DIALECT);
    }

    public void setMessageSource(MessageSource messageSource) {
        setMessageResolver(new AspectranMessageResolver(messageSource));
    }

}
