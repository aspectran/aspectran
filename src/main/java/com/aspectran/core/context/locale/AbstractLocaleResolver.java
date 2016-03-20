/**
 * Copyright 2008-2016 Juho Jeong
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
package com.aspectran.core.context.locale;

import java.util.Locale;
import java.util.TimeZone;

import com.aspectran.core.activity.Translet;

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
     * Set a default Locale that this resolver will return if no other locale found.
     */
    public void setDefaultLocale(Locale defaultLocale) {
        this.defaultLocale = defaultLocale;
    }

    /**
     * Return the default Locale that this resolver is supposed to fall back to, if any.
     */
    protected Locale getDefaultLocale() {
        return this.defaultLocale;
    }

    /**
     * Set a default TimeZone that this resolver will return if no other time zone found.
     */
    public void setDefaultTimeZone(TimeZone defaultTimeZone) {
        this.defaultTimeZone = defaultTimeZone;
    }

    /**
     * Return the default TimeZone that this resolver is supposed to fall back to, if any.
     */
    public TimeZone getDefaultTimeZone() {
        return this.defaultTimeZone;
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