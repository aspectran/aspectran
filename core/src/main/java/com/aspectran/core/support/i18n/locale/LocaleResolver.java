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
package com.aspectran.core.support.i18n.locale;

import com.aspectran.core.activity.Translet;
import org.jspecify.annotations.Nullable;

import java.util.Locale;
import java.util.TimeZone;

/**
 * A strategy interface for resolving the current locale and time zone of a request.
 *
 * <p>This is a central component for internationalization (i18n), allowing the
 * application to determine the user's preferred locale and time zone from the
 * current {@link Translet}. Implementations can provide different strategies,
 * such as resolving from the session, cookies, or the request header.
 *
 * <p>Created: 2016. 3. 13.</p>
 *
 * @see SessionLocaleResolver
 */
public interface LocaleResolver {

    /**
     * The bean name for the default {@code LocaleResolver} instance.
     */
    String LOCALE_RESOLVER_BEAN_ID = "localeResolver";

    /**
     * Resolves the current locale from the given translet.
     * @param translet the translet to resolve the locale for
     * @return the current locale (never {@code null})
     */
    Locale resolveLocale(Translet translet);

    /**
     * Resolves the current time zone from the given translet.
     * @param translet the translet to resolve the time zone for
     * @return the current time zone (never {@code null})
     */
    TimeZone resolveTimeZone(Translet translet);

    /**
     * Sets the current locale to the given one.
     * <p>Passing {@code null} for the locale value is intended to clear
     * the locale setting, reverting to the default behavior of the resolver.
     * @param translet the translet to set the locale for
     * @param locale the new locale, or {@code null} to clear the current locale
     * @throws UnsupportedOperationException if the {@code LocaleResolver} implementation
     *      does not support dynamically changing the locale
     */
    void setLocale(Translet translet, @Nullable Locale locale);

    /**
     * Sets the current time zone to the given one.
     * <p>Passing {@code null} for the time zone value is intended to clear
     * the time zone setting, reverting to the default behavior of the resolver.
     * @param translet the translet to set the time zone for
     * @param timeZone the new time zone, or {@code null} to clear the current time zone
     * @throws UnsupportedOperationException if the {@code LocaleResolver} implementation
     *      does not support dynamically changing the time zone
     */
    void setTimeZone(Translet translet, @Nullable TimeZone timeZone);

}
