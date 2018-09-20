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
import com.aspectran.core.util.StringUtils;

import java.util.Locale;
import java.util.TimeZone;

/**
 * Abstract base class for {@link LocaleResolver} implementations.
 * Provides support for a default locale and timezone.
 *
 * <p>Created: 2016. 3. 13.</p>
 */
public abstract class AbstractLocaleResolver implements LocaleResolver {

    private Locale defaultLocale;

    private TimeZone defaultTimeZone;

    /**
     * Return the default Locale that this resolver is supposed to fall back to, if any.
     *
     * @return the default locale
     */
    public Locale getDefaultLocale() {
        return this.defaultLocale;
    }

    /**
     * Set a default Locale that this resolver will return if no other locale found.
     *
     * @param defaultLocale the default locale
     */
    public void setDefaultLocale(Locale defaultLocale) {
        this.defaultLocale = defaultLocale;
    }

    /**
     * Set a default Locale that this resolver will return if no other locale found.
     *
     * @param defaultLocale the default locale
     */
    public void setDefaultLocale(String defaultLocale) {
        setDefaultLocale(StringUtils.parseLocaleString(defaultLocale));
    }

    /**
     * Return the default TimeZone that this resolver is supposed to fall back to, if any.
     *
     * @return the default time zone
     */
    public TimeZone getDefaultTimeZone() {
        return this.defaultTimeZone;
    }

    /**
     * Set a default TimeZone that this resolver will return if no other time zone found.
     *
     * @param defaultTimeZone the default time zone
     */
    public void setDefaultTimeZone(TimeZone defaultTimeZone) {
        this.defaultTimeZone = defaultTimeZone;
    }

    /**
     * Set a default TimeZone that this resolver will return if no other time zone found.
     *
     * @param defaultTimeZone the default time zone
     */
    public void setDefaultTimeZone(String defaultTimeZone) {
        setDefaultTimeZone(StringUtils.parseTimeZoneString(defaultTimeZone));
    }

    /**
     * Resolve the default locale for the given translet,
     * Called if can not find specified Locale.
     *
     * @param translet the translet to resolve the locale for
     * @return the default locale (never {@code null})
     * @see #setDefaultLocale
     */
    protected Locale resolveDefaultLocale(Translet translet) {
        Locale defaultLocale = getDefaultLocale();
        if (defaultLocale != null) {
            translet.getRequestAdapter().setLocale(defaultLocale);
            return defaultLocale;
        } else {
            return translet.getRequestAdapter().getLocale();
        }
    }

    /**
     * Resolve the default time zone for the given translet,
     * Called if can not find specified TimeZone.
     *
     * @param translet the translet to resolve the time zone for
     * @return the default time zone (or {@code null} if none defined)
     * @see #setDefaultTimeZone
     */
    protected TimeZone resolveDefaultTimeZone(Translet translet) {
        TimeZone defaultTimeZone = getDefaultTimeZone();
        if (defaultTimeZone != null) {
            translet.getRequestAdapter().setTimeZone(defaultTimeZone);
            return defaultTimeZone;
        } else {
            return translet.getRequestAdapter().getTimeZone();
        }
    }

    @Override
    public void setLocale(Translet translet, Locale locale) {
        translet.getRequestAdapter().setLocale(locale);
    }

    @Override
    public void setTimeZone(Translet translet, TimeZone timeZone) {
        translet.getRequestAdapter().setTimeZone(timeZone);
    }

}