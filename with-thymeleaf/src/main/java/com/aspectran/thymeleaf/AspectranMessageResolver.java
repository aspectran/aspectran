package com.aspectran.thymeleaf;

import com.aspectran.core.support.i18n.message.MessageSource;
import com.aspectran.core.support.i18n.message.NoSuchMessageException;
import com.aspectran.utils.Assert;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.messageresolver.AbstractMessageResolver;
import org.thymeleaf.messageresolver.StandardMessageResolver;

public class AspectranMessageResolver extends AbstractMessageResolver {

    private final MessageSource messageSource;

    private final StandardMessageResolver standardMessageResolver;

    public AspectranMessageResolver(MessageSource messageSource) {
        super();
        Assert.notNull(messageSource, "messageSource must not be null");
        this.messageSource = messageSource;
        this.standardMessageResolver = new StandardMessageResolver();
    }

    @Override
    public String resolveMessage(ITemplateContext context, Class<?> origin, String key, Object[] messageParameters) {
        Assert.notNull(context, "context must not be null");
        Assert.notNull(key, "key must not be null");

        /*
         * FIRST STEP: Look for the message using template-based resolution
         */
        try {
            return messageSource.getMessage(key, messageParameters, context.getLocale());
        } catch (NoSuchMessageException e) {
            // Try other methods
        }

        /*
         * SECOND STEP: Look for the message using origin-based resolution, delegated to the StandardMessageResolver
         */
        if (origin != null) {
            // We will be disabling template-based resolution when delegating in order to use only origin-based
            return standardMessageResolver.resolveMessage(context, origin, key, messageParameters,
                false, true, true);
        }

        /*
         * NOT FOUND, return null
         */
        return null;
    }

    @Override
    public String createAbsentMessageRepresentation(
            ITemplateContext context, Class<?> origin, String key, Object[] messageParameters) {
        return standardMessageResolver.createAbsentMessageRepresentation(context, origin, key, messageParameters);
    }

}
