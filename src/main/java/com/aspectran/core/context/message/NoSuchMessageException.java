package com.aspectran.core.context.message;

import java.util.Locale;

import com.aspectran.core.context.AspectranRuntimeException;

/**
 * <p>Created: 2016. 2. 8.</p>
 */
public class NoSuchMessageException extends AspectranRuntimeException {

    private static final long serialVersionUID = -2677086951169637323L;

    /**
     * Create a new exception.
     * @param code code that could not be resolved for given locale
     * @param locale locale that was used to search for the code within
     */
    public NoSuchMessageException(String code, Locale locale) {
        super("No message found under code '" + code + "' for locale '" + locale + "'.");
    }

    /**
     * Create a new exception.
     * @param code code that could not be resolved for given locale
     */
    public NoSuchMessageException(String code) {
        super("No message found under code '" + code + "' for locale '" + Locale.getDefault() + "'.");
    }

}
