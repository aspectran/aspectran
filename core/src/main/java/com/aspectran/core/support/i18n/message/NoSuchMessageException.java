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

import com.aspectran.core.context.AspectranRuntimeException;

import java.util.Locale;

/**
 * Exception thrown when a message can't be resolved.
 *
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
        super("No message found under code '" + code + "' for locale '" + locale + "'");
    }

    /**
     * Create a new exception.
     * @param code code that could not be resolved for given locale
     */
    public NoSuchMessageException(String code) {
        super("No message found under code '" + code + "' for locale '" + Locale.getDefault() + "'");
    }

}
