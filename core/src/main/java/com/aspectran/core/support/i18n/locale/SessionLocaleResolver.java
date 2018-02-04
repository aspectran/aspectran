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
import com.aspectran.core.adapter.SessionAdapter;

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
     * to retrieve the current locale in controllers or views.
     */
    public static final String LOCALE_SESSION_ATTRIBUTE_NAME = SessionLocaleResolver.class.getName() + ".LOCALE";

    /**
     * Name of the session attribute that holds the TimeZone.
     * Only used internally by this implementation.
     */
    public static final String TIME_ZONE_SESSION_ATTRIBUTE_NAME = SessionLocaleResolver.class.getName() + ".TIME_ZONE";

    @Override
    public Locale resolveLocale(Translet translet) {
        SessionAdapter sessionAdapter = translet.getSessionAdapter();
        if (sessionAdapter != null) {
            Locale locale = sessionAdapter.getAttribute(LOCALE_SESSION_ATTRIBUTE_NAME);
            if (locale != null) {
                super.setLocale(translet, locale);
                return locale;
            }
        }
        return resolveDefaultLocale(translet);
    }

    @Override
    public TimeZone resolveTimeZone(Translet translet) {
        SessionAdapter sessionAdapter = translet.getSessionAdapter();
        if (sessionAdapter != null) {
            TimeZone timeZone = sessionAdapter.getAttribute(TIME_ZONE_SESSION_ATTRIBUTE_NAME);
            if (timeZone != null) {
                super.setTimeZone(translet, timeZone);
                return timeZone;
            }
        }
        return resolveDefaultTimeZone(translet);
    }

    @Override
    public void setLocale(Translet translet, Locale locale) {
        super.setLocale(translet, locale);
        SessionAdapter sessionAdapter = translet.getSessionAdapter();
        if (sessionAdapter != null) {
            sessionAdapter.setAttribute(LOCALE_SESSION_ATTRIBUTE_NAME, locale);
        }
    }

    @Override
    public void setTimeZone(Translet translet, TimeZone timeZone) {
        super.setTimeZone(translet, timeZone);
        SessionAdapter sessionAdapter = translet.getSessionAdapter();
        if (sessionAdapter != null) {
            sessionAdapter.setAttribute(TIME_ZONE_SESSION_ATTRIBUTE_NAME, timeZone);
        }
    }

}
