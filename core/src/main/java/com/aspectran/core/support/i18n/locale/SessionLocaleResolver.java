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
import com.aspectran.core.adapter.SessionAdapter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Locale;
import java.util.TimeZone;

/**
 * A {@link LocaleResolver} implementation that uses locale and time zone attributes
 * stored in the user's session.
 *
 * <p>This resolver checks for a custom locale/time zone in the session. If one is not
 * found or is not supported, it falls back to the default locale/timezone determined by
 * {@link #determineDefaultLocale(Translet)} and {@link #determineDefaultTimeZone(Translet)}.</p>
 *
 * <p>Created: 2016. 3. 13.</p>
 *
 * @see #LOCALE_SESSION_ATTR_NAME
 * @see #TIME_ZONE_SESSION_ATTR_NAME
 */
public class SessionLocaleResolver extends AbstractLocaleResolver {

    /**
     * The name of the session attribute that holds the {@link Locale}.
     */
    public static final String LOCALE_SESSION_ATTR_NAME = SessionLocaleResolver.class.getName() + ".LOCALE";

    /**
     * The name of the session attribute that holds the {@link TimeZone}.
     */
    public static final String TIME_ZONE_SESSION_ATTR_NAME = SessionLocaleResolver.class.getName() + ".TIME_ZONE";

    /**
     * Resolves the locale from the session attribute, falling back to the default
     * locale if not found.
     */
    @Override
    @Nullable
    public Locale resolveLocale(@NonNull Translet translet) {
        if (translet.hasSessionAdapter()) {
            SessionAdapter sessionAdapter = translet.getSessionAdapter();
            Locale locale = sessionAdapter.getAttribute(LOCALE_SESSION_ATTR_NAME);
            if (locale != null && isSupportedLocale(locale)) {
                translet.getRequestAdapter().setLocale(locale);
                return locale;
            }
        }
        return determineDefaultLocale(translet);
    }

    /**
     * Resolves the time zone from the session attribute, falling back to the default
     * time zone if not found.
     */
    @Override
    @Nullable
    public TimeZone resolveTimeZone(@NonNull Translet translet) {
        if (translet.hasSessionAdapter()) {
            SessionAdapter sessionAdapter = translet.getSessionAdapter();
            TimeZone timeZone = sessionAdapter.getAttribute(TIME_ZONE_SESSION_ATTR_NAME);
            if (timeZone != null) {
                translet.getRequestAdapter().setTimeZone(timeZone);
                return timeZone;
            }
        }
        return determineDefaultTimeZone(translet);
    }

    /**
     * Sets the locale in the request and stores it in the session for future requests.
     */
    @Override
    public void setLocale(@NonNull Translet translet, @Nullable Locale locale) {
        translet.getRequestAdapter().setLocale(locale);
        if (translet.hasSessionAdapter()) {
            translet.getSessionAdapter().setAttribute(LOCALE_SESSION_ATTR_NAME, locale);
        }
    }

    /**
     * Sets the time zone in the request and stores it in the session for future requests.
     */
    @Override
    public void setTimeZone(@NonNull Translet translet, @Nullable TimeZone timeZone) {
        translet.getRequestAdapter().setTimeZone(timeZone);
        if (translet.hasSessionAdapter()) {
            translet.getSessionAdapter().setAttribute(TIME_ZONE_SESSION_ATTR_NAME, timeZone);
        }
    }

}
