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
import com.aspectran.utils.LocaleUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Abstract base class for {@link LocaleResolver} implementations.
 * Provides support for a default locale and timezone.
 *
 * <p>Created: 2016. 3. 13.</p>
 */
public abstract class AbstractLocaleResolver implements LocaleResolver {

    private List<Locale> supportedLocales;

    private Locale defaultLocale;

    private TimeZone defaultTimeZone;

    /**
     * Return the configured list of supported locales.
     */
    public List<Locale> getSupportedLocales() {
        return this.supportedLocales;
    }

    /**
     * Configure the list of supported locales to compare and match against requested locales.
     * <p>In order for a supported locale to be considered a match, it must match
     * on both country and language. If you want to support a language-only match
     * as a fallback, you must configure the language explicitly as a supported
     * locale.
     * <p>For example, if the supported locales are {@code ["de-DE","en-US"]},
     * then a request for {@code "en-GB"} will not match, and neither will a
     * request for {@code "en"}. If you want to support additional locales for a
     * given language such as {@code "en"}, then you must add it to the list of
     * supported locales.
     * <p>If there is no match, then the {@link #setDefaultLocale(Locale)
     * defaultLocale} is used, if configured, or otherwise falling back on
     * {@link com.aspectran.core.adapter.RequestAdapter#getLocale()}.
     * @param locales the supported locales
     */
    public void setSupportedLocales(List<Locale> locales) {
        this.supportedLocales = locales;
    }

    /**
     * Configure supported locales.
     * @param locales the supported locales
     */
    public void setSupportedLocales(String[] locales) {
        if (locales == null || locales.length == 0) {
            this.supportedLocales = null;
            return;
        }
        List<Locale> supportedLocales = new ArrayList<>(locales.length);
        for (String locale : locales) {
            supportedLocales.add(LocaleUtils.parseLocale(locale));
        }
        this.supportedLocales = supportedLocales;
    }

    /**
     * Return the default Locale that this resolver is supposed to fall back to, if any.
     * @return the default locale
     */
    public Locale getDefaultLocale() {
        return this.defaultLocale;
    }

    /**
     * Set a default Locale that this resolver will return if no other locale found.
     * @param defaultLocale the default locale
     */
    public void setDefaultLocale(Locale defaultLocale) {
        this.defaultLocale = defaultLocale;
    }

    /**
     * Set a default Locale that this resolver will return if no other locale found.
     * @param defaultLocale the default locale
     */
    public void setDefaultLocale(String defaultLocale) {
        setDefaultLocale(LocaleUtils.parseLocale(defaultLocale));
    }

    /**
     * Return the default TimeZone that this resolver is supposed to fall back to, if any.
     * @return the default time zone
     */
    @Nullable
    public TimeZone getDefaultTimeZone() {
        return this.defaultTimeZone;
    }

    /**
     * Set a default TimeZone that this resolver will return if no other time zone found.
     * @param defaultTimeZone the default time zone
     */
    public void setDefaultTimeZone(TimeZone defaultTimeZone) {
        this.defaultTimeZone = defaultTimeZone;
    }

    /**
     * Set a default TimeZone that this resolver will return if no other time zone found.
     * @param defaultTimeZone the default time zone
     */
    public void setDefaultTimeZone(String defaultTimeZone) {
        setDefaultTimeZone(LocaleUtils.parseTimeZoneString(defaultTimeZone));
    }

    /**
     * Determines the default locale for the given translet.
     * @param translet the translet to resolve the locale for
     * @return the default locale (never {@code null})
     * @see #setDefaultLocale
     */
    protected Locale determineDefaultLocale(@NonNull Translet translet) {
        Locale locale = translet.getRequestAdapter().getLocale();
        if (locale != null && !isSupportedLocale(locale)) {
            locale = null;
        }
        if (locale == null) {
            locale = getDefaultLocale();
            if (locale != null) {
                translet.getRequestAdapter().setLocale(locale);
            }
        }
        return locale;
    }

    /**
     * Determines the default time zone for the given translet.
     * @param translet the translet to resolve the time zone for
     * @return the default time zone (or {@code null} if none defined)
     * @see #setDefaultTimeZone
     */
    protected TimeZone determineDefaultTimeZone(@NonNull Translet translet) {
        TimeZone timeZone = translet.getRequestAdapter().getTimeZone();
        if (timeZone == null) {
            timeZone = getDefaultTimeZone();
            if (timeZone != null) {
                translet.getRequestAdapter().setTimeZone(timeZone);
            }
        }
        return timeZone;
    }

    private boolean isSupportedLocale(Locale locale) {
        if (supportedLocales == null) {
            return true;
        }
        for (Locale loc : supportedLocales) {
            if (!loc.getCountry().isEmpty() || !loc.getVariant().isEmpty() || loc.hasExtensions()) {
                if (loc.equals(locale)) {
                    return true;
                }
            } else if (loc.getLanguage().equals(locale.getLanguage())) {
                return true;
            }
        }
        return false;
    }

}
