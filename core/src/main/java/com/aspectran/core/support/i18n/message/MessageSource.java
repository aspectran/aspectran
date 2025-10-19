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

import com.aspectran.utils.annotation.jsr305.Nullable;

import java.util.Locale;

/**
 * The central strategy interface for resolving messages, with support for
 * parameterization and internationalization.
 *
 * <p>Implementations of this interface are responsible for resolving a message
 * for a given code and locale. If a message is not found, they can optionally
 * delegate to a parent message source.
 *
 * <p>It is recommended to base message code names on the relevant fully qualified
 * class name of the component that uses them, avoiding conflicts and ensuring
 * maximum clarity.
 *
 * <p>Created: 2016. 2. 8.</p>
 *
 * @see HierarchicalMessageSource
 * @see ResourceBundleMessageSource
 */
public interface MessageSource {

    /**
     * Resolves the message for the given code and locale.
     * @param code the message code to look up (e.g., "calculator.noRateSet")
     * @param locale the locale in which to do the lookup
     * @return the resolved message
     * @throws NoSuchMessageException if the message could not be found
     */
    String getMessage(String code, Locale locale) throws NoSuchMessageException;

    /**
     * Resolves the message for the given code and locale, returning a default
     * message if the lookup fails.
     * @param code the message code to look up (e.g., "calculator.noRateSet")
     * @param defaultMessage the string to return if the lookup fails
     * @param locale the locale in which to do the lookup
     * @return the resolved message, or the default message if the lookup was unsuccessful
     */
    String getMessage(String code, @Nullable String defaultMessage, Locale locale);

    /**
     * Resolves the message for the given code and locale, filling in any
     * arguments found in the message.
     * @param code the message code to look up (e.g., "calculator.noRateSet")
     * @param args an array of arguments that will be filled in for params within
     *      the message (e.g., "{0}", "{1,date}"), or {@code null} if none
     * @param locale the locale in which to do the lookup
     * @return the resolved message
     * @throws NoSuchMessageException if the message could not be found
     */
    String getMessage(String code, @Nullable Object[] args, Locale locale) throws NoSuchMessageException;

    /**
     * Resolves the message for the given code and locale, filling in any
     * arguments and returning a default message if the lookup fails.
     * @param code the message code to look up (e.g., "calculator.noRateSet")
     * @param args an array of arguments that will be filled in for params within
     *      the message (e.g., "{0}", "{1,date}"), or {@code null} if none
     * @param defaultMessage the string to return if the lookup fails
     * @param locale the locale in which to do the lookup
     * @return the resolved message, or the default message if the lookup was unsuccessful
     */
    String getMessage(String code, @Nullable Object[] args, @Nullable String defaultMessage, Locale locale);

}
