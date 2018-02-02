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
package com.aspectran.core.support.i18n.locale;

import com.aspectran.core.activity.Translet;

import java.util.Locale;
import java.util.TimeZone;

/**
 * Interface for locale and timezone resolution strategies
 *
 * <p>Created: 2016. 3. 13.</p>
 */
public interface LocaleResolver {

    /**
     * Resolve the current locale via the given translet.
     * Can return a default locale as fallback in any case.
     *
     * @param translet the translet to resolve the locale for
     * @return the current locale (never {@code null})
     */
    Locale resolveLocale(Translet translet);

    /**
     * Resolve the current timezone via the given translet.
     * Can return a default timezone as fallback in any case.
     *
     * @param translet the translet to resolve the timezone for
     * @return the current timezone (never {@code null})
     */
    TimeZone resolveTimeZone(Translet translet);

    /**
     * Set the current locale to the given one.
     *
     * @param translet the translet to resolve the locale for
     * @param locale the new locale, or {@code null} to clear the locale
     * @throws UnsupportedOperationException if the LocaleResolver implementation does not
     * support dynamic changing of the locale
     */
    void setLocale(Translet translet, Locale locale);

    /**
     * Set the current timezone to the given one.
     *
     * @param translet the translet to resolve the locale for
     * @param timeZone the new timezone, or {@code null} to clear the timezone
     * @throws UnsupportedOperationException if the LocaleResolver implementation does not
     * support dynamic changing of the timezone
     */
    void setTimeZone(Translet translet, TimeZone timeZone);

}
