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
import com.aspectran.utils.ToStringBuilder;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Locale;
import java.util.TimeZone;

/**
 * {@link LocaleResolver} implementation that always resolves to a fixed locale
 * and optionally a fixed time zone.
 *
 * <p>Unlike resolvers that dynamically determine the locale from request headers,
 * cookies, or sessions, this resolver exposes a pre-configured fixed locale
 * and applies it to the {@link com.aspectran.core.adapter.RequestAdapter}.</p>
 *
 * <p>Note: Does not support dynamic modifications via {@link #setLocale} or
 * {@link #setTimeZone}; calling them will throw an {@link UnsupportedOperationException}.</p>
 *
 * <p>Created: 2016. 9. 5.</p>
 */
public class FixedLocaleResolver extends AbstractLocaleResolver {

    /**
     * Creates a {@code FixedLocaleResolver} that exposes the JVM's default locale.
     * @see #setDefaultLocale
     * @see #setDefaultTimeZone
     */
    public FixedLocaleResolver() {
        setDefaultLocale(Locale.getDefault());
    }

    /**
     * Creates a {@code FixedLocaleResolver} that exposes the specified fixed locale.
     * @param locale the fixed locale to expose
     */
    public FixedLocaleResolver(Locale locale) {
        setDefaultLocale(locale);
    }

    /**
     * Creates a {@code FixedLocaleResolver} that exposes the specified fixed locale
     * and time zone.
     * @param locale the fixed locale to expose
     * @param timeZone the fixed time zone to expose
     */
    public FixedLocaleResolver(Locale locale, TimeZone timeZone) {
        setDefaultLocale(locale);
        setDefaultTimeZone(timeZone);
    }

    @Override
    @Nullable
    public Locale resolveLocale(@NonNull Translet translet) {
        Locale locale = getDefaultLocale();
        if (locale != null) {
            translet.getRequestAdapter().setLocale(locale);
        }
        return locale;
    }

    @Override
    @Nullable
    public TimeZone resolveTimeZone(@NonNull Translet translet) {
        TimeZone timeZone = getDefaultTimeZone();
        if (timeZone != null) {
            translet.getRequestAdapter().setTimeZone(timeZone);
            return timeZone;
        }
        return null;
    }

    @Override
    public void setLocale(@NonNull Translet translet, @Nullable Locale locale) {
        throw new UnsupportedOperationException("Cannot change fixed locale - use a different locale resolution strategy");
    }

    @Override
    public void setTimeZone(@NonNull Translet translet, @Nullable TimeZone timeZone) {
        throw new UnsupportedOperationException("Cannot change fixed locale - use a different locale resolution strategy");
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("defaultLocale", getDefaultLocale());
        TimeZone timeZone = getDefaultTimeZone();
        tsb.append("defaultTimeZone", timeZone != null ? timeZone.getID() : null);
        return tsb.toString();
    }

}
