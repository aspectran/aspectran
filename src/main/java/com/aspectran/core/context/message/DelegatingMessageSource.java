package com.aspectran.core.context.message;

import java.util.Locale;

/**
 * Empty {@link MessageSource} that delegates all calls to the parent MessageSource.
 * If no parent is available, it simply won't resolve any message.
 *
 * <p>Created: 2016. 3. 13.</p>
 */
public class DelegatingMessageSource extends MessageSourceSupport implements HierarchicalMessageSource {

    private MessageSource parentMessageSource;

    @Override
    public void setParentMessageSource(MessageSource parent) {
        this.parentMessageSource = parent;
    }

    @Override
    public MessageSource getParentMessageSource() {
        return this.parentMessageSource;
    }

    @Override
    public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
        if(this.parentMessageSource != null) {
            return this.parentMessageSource.getMessage(code, args, defaultMessage, locale);
        } else {
            return renderDefaultMessage(defaultMessage, args, locale);
        }
    }

    @Override
    public String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException {
        if(this.parentMessageSource != null) {
            return this.parentMessageSource.getMessage(code, args, locale);
        } else {
            throw new NoSuchMessageException(code, locale);
        }
    }

}
