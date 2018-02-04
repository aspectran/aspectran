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
 * Strategy interface for resolving messages, with support for the parameterization
 * and internationalization of such messages.
 *
 * <p>Created: 2016. 2. 8.</p>
 */
public interface MessageSource {

    /**
     * Try to resolve the message. Return default message if no message was found.
     *
     * @param code the code to lookup up, such as 'calculator.noRateSet'. Users of
     *         this class are encouraged to base message names on the relevant fully
     *         qualified class name, thus avoiding conflict and ensuring maximum clarity.
     * @param args array of arguments that will be filled in for params within
     *         the message (params look like "{0}", "{1,date}", "{2,time}" within a message),
     *         or {@code null} if none.
     * @param defaultMessage String to return if the lookup fails
     * @param locale the Locale in which to do the lookup
     * @return the resolved message if the lookup was successful;
     *         otherwise the default message passed as a parameter
     * @see java.text.MessageFormat
     */
    String getMessage(String code, Object[] args, String defaultMessage, Locale locale);

    /**
     * Try to resolve the message. Treat as an error if the message can't be found.
     *
     * @param code the code to lookup up, such as 'calculator.noRateSet'
     * @param args Array of arguments that will be filled in for params within
     *         the message (params look like "{0}", "{1,date}", "{2,time}" within a message),
     *         or {@code null} if none.
     * @param locale the Locale in which to do the lookup
     * @return the resolved message
     * @throws NoSuchMessageException if the message wasn't found
     * @see java.text.MessageFormat
     */
    String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException;

}
