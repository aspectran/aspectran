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
 * {@link LocaleResolver} implementation
 * that always returns a fixed default locale and optionally time zone.
 * Default is the current JVM's default locale.
 * 
 * <p>Note: Does not support {@code setLocale(Context)}, as the fixed
 * locale and time zone cannot be changed.
 *  
 * <p>Created: 2016. 9. 5.</p>
 */
public class FixedLocaleResolver extends AbstractLocaleResolver {

    /**
     * Create a default FixedLocaleResolver, exposing a configured default
     * locale (or the JVM's default locale as fallback).
     *
     * @see #setDefaultLocale
     * @see #setDefaultTimeZone
     */
    public FixedLocaleResolver() {
        setDefaultLocale(Locale.getDefault());
    }

    /**
     * Create a FixedLocaleResolver that exposes the given locale.
     *
     * @param locale the locale to expose
     */
    public FixedLocaleResolver(Locale locale) {
        setDefaultLocale(locale);
    }

    /**
     * Create a FixedLocaleResolver that exposes the given locale and time zone.
     *
     * @param locale the locale to expose
     * @param timeZone the time zone to expose
     */
    public FixedLocaleResolver(Locale locale, TimeZone timeZone) {
        setDefaultLocale(locale);
        setDefaultTimeZone(timeZone);
    }

    @Override
    public Locale resolveLocale(Translet translet) {
        return resolveDefaultLocale(translet);
    }

    @Override
    public TimeZone resolveTimeZone(Translet translet) {
        return resolveDefaultTimeZone(translet);
    }

    @Override
    public void setLocale(Translet translet, Locale locale) {
        throw new UnsupportedOperationException("Cannot change fixed locale - use a different locale resolution strategy");
    }

    @Override
    public void setTimeZone(Translet translet, TimeZone timeZone) {
        throw new UnsupportedOperationException("Cannot change fixed locale - use a different locale resolution strategy");
    }

}
