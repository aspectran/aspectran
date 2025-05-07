/*
 * Copyright (c) 2008-2025 The Aspectran Project
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
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.util.Locale;
import java.util.TimeZone;

/**
 * {@link LocaleResolver} implementation that
 * uses a locale attribute in the user's session in case of a custom setting,
 * with a fallback to the specified default locale or the request's
 * accept-header locale.
 *
 * <p>Created: 2016. 3. 13.</p>
 */
public class SessionLocaleResolver extends AbstractLocaleResolver {

    /**
     * Name of the session attribute that holds the Locale.
     * Only used internally by this implementation.
     */
    public static final String LOCALE_SESSION_ATTR_NAME = SessionLocaleResolver.class.getName() + ".LOCALE";

    /**
     * Name of the session attribute that holds the TimeZone.
     * Only used internally by this implementation.
     */
    public static final String TIME_ZONE_SESSION_ATTR_NAME = SessionLocaleResolver.class.getName() + ".TIME_ZONE";

    @Override
    public Locale resolveLocale(@NonNull Translet translet) {
        SessionAdapter sessionAdapter = translet.getSessionAdapter();
        if (sessionAdapter != null) {
            Locale locale = sessionAdapter.getAttribute(LOCALE_SESSION_ATTR_NAME);
            if (locale != null) {
                translet.getRequestAdapter().setLocale(locale);
                return locale;
            }
        }
        return determineDefaultLocale(translet);
    }

    @Override
    public TimeZone resolveTimeZone(@NonNull Translet translet) {
        SessionAdapter sessionAdapter = translet.getSessionAdapter();
        if (sessionAdapter != null) {
            TimeZone timeZone = sessionAdapter.getAttribute(TIME_ZONE_SESSION_ATTR_NAME);
            if (timeZone != null) {
                translet.getRequestAdapter().setTimeZone(timeZone);
                return timeZone;
            }
        }
        return determineDefaultTimeZone(translet);
    }

    @Override
    public void setLocale(@NonNull Translet translet, Locale locale) {
        translet.getRequestAdapter().setLocale(locale);
        SessionAdapter sessionAdapter = translet.getSessionAdapter();
        if (sessionAdapter != null) {
            sessionAdapter.setAttribute(LOCALE_SESSION_ATTR_NAME, locale);
        }
    }

    @Override
    public void setTimeZone(@NonNull Translet translet, TimeZone timeZone) {
        translet.getRequestAdapter().setTimeZone(timeZone);
        SessionAdapter sessionAdapter = translet.getSessionAdapter();
        if (sessionAdapter != null) {
            sessionAdapter.setAttribute(TIME_ZONE_SESSION_ATTR_NAME, timeZone);
        }
    }

}
