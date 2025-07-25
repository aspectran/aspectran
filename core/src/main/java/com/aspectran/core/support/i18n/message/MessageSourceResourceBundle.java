/*
 * Copyright (c) 2008-present The Aspectran Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.core.support.i18n.message;

import com.aspectran.utils.annotation.jsr305.NonNull;

import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * <p>This class is a clone of org.springframework.context.support.MessageSourceResourceBundle</p>
 *
 * Helper class that allows for accessing an Aspectran
 * {@link MessageSource} as a {@link ResourceBundle}.
 *
 * @see java.util.ResourceBundle
 */
public class MessageSourceResourceBundle extends ResourceBundle {

    private final MessageSource messageSource;

    private final Locale locale;

    /**
     * Create a new MessageSourceResourceBundle for the given MessageSource and Locale.
     * @param source the MessageSource to retrieve messages from
     * @param locale the Locale to retrieve messages for
     */
    public MessageSourceResourceBundle(MessageSource source, Locale locale) {
        if (source == null) {
            throw new IllegalArgumentException("MessageSource must not be null");
        }
        this.messageSource = source;
        this.locale = locale;
    }

    /**
     * Create a new MessageSourceResourceBundle for the given MessageSource and Locale.
     * @param source the MessageSource to retrieve messages from
     * @param locale the Locale to retrieve messages for
     * @param parent the parent ResourceBundle to delegate to if no local message found
     */
    public MessageSourceResourceBundle(MessageSource source, Locale locale, ResourceBundle parent) {
        this(source, locale);
        setParent(parent);
    }

    /**
     * This implementation resolves the code in the MessageSource.
     * Returns {@code null} if the message could not be resolved.
     */
    @Override
    protected Object handleGetObject(@NonNull String key) {
        try {
            return this.messageSource.getMessage(key, this.locale);
        } catch (NoSuchMessageException ex) {
            return null;
        }
    }

    /**
     * This implementation checks whether the target MessageSource can resolve
     * a message for the given key, translating {@code NoSuchMessageException}
     * accordingly. In contrast to ResourceBundle's default implementation in
     * JDK 1.6, this does not rely on the capability to enumerate message keys.
     */
    @Override
    public boolean containsKey(@NonNull String key) {
        try {
            this.messageSource.getMessage(key, this.locale);
            return true;
        } catch (NoSuchMessageException ex) {
            return false;
        }
    }

    /**
     * This implementation throws {@code UnsupportedOperationException},
     * as a MessageSource does not allow for enumerating the defined message codes.
     */
    @Override
    @NonNull
    public Enumeration<String> getKeys() {
        throw new UnsupportedOperationException("MessageSourceResourceBundle does not support enumerating its keys");
    }

    /**
     * This implementation exposes the specified Locale for introspection
     * through the standard {@code ResourceBundle.getLocale()} method.
     */
    @Override
    public Locale getLocale() {
        return this.locale;
    }

}
