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

import java.util.Locale;

/**
 * A {@link MessageSource} implementation that delegates all message resolution calls
 * to its parent {@code MessageSource}.
 *
 * <p>This class does not resolve any messages on its own. If no parent is configured,
 * it will either throw a {@link NoSuchMessageException} or return a default message,
 * depending on the invoked method. It is useful as a placeholder in a message
 * source hierarchy that will be populated later.</p>
 *
 * <p>Created: 2016. 3. 13.</p>
 *
 * @see #setParentMessageSource
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
    public String getMessage(String code, Locale locale) throws NoSuchMessageException {
        return getMessage(code, (Object[])null, locale);
    }

    @Override
    public String getMessage(String code, String defaultMessage, Locale locale) {
        return getMessage(code, null, defaultMessage, locale);
    }

    @Override
    public String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException {
        if (this.parentMessageSource != null) {
            return this.parentMessageSource.getMessage(code, args, locale);
        } else {
            throw new NoSuchMessageException(code, locale);
        }
    }

    @Override
    public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
        if (this.parentMessageSource != null) {
            return this.parentMessageSource.getMessage(code, args, defaultMessage, locale);
        } else {
            return renderDefaultMessage(defaultMessage, args, locale);
        }
    }

}
