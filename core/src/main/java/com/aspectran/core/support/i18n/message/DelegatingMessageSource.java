/*
 * Copyright (c) 2008-2018 The Aspectran Project
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
        if (this.parentMessageSource != null) {
            return this.parentMessageSource.getMessage(code, args, defaultMessage, locale);
        } else {
            return renderDefaultMessage(defaultMessage, args, locale);
        }
    }

    @Override
    public String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException {
        if (this.parentMessageSource != null) {
            return this.parentMessageSource.getMessage(code, args, locale);
        } else {
            throw new NoSuchMessageException(code, locale);
        }
    }

}
