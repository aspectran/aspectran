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
package com.aspectran.web.support.i18n.locale;

import com.aspectran.core.activity.Translet;
import com.aspectran.core.support.i18n.locale.AbstractLocaleResolver;
import com.aspectran.core.support.i18n.locale.LocaleResolver;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;
import com.aspectran.web.support.http.HttpHeaders;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * <p>This class is a clone of org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver</p>
 * <p>
 * {@link LocaleResolver} implementation that looks for a match between locales
 * in the {@code Accept-Language} header and a list of configured supported
 * locales.
 *
 * <p>See {@link #setSupportedLocales(List)} for further details on how
 * supported and requested locales are matched.
 *
 * <p>Note: This implementation does not support {@link #setLocale} since the
 * {@code Accept-Language} header can only be changed by changing the client's
 * locale settings.
 */
public class AcceptHeaderLocaleResolver extends AbstractLocaleResolver {

    @Override
    public Locale resolveLocale(Translet translet) {
        Locale defaultLocale = getDefaultLocale();
        if (defaultLocale != null && translet.getRequestAdapter().getHeader(HttpHeaders.ACCEPT_LANGUAGE) == null) {
            return defaultLocale;
        }
        Locale requestLocale = translet.getRequestAdapter().getLocale();
        List<Locale> supportedLocales = getSupportedLocales();
        if (supportedLocales == null || supportedLocales.isEmpty() || supportedLocales.contains(requestLocale)) {
            return requestLocale;
        }
        Locale supportedLocale = findSupportedLocale(translet.getResponseAdapter().getAdaptee(), supportedLocales);
        if (supportedLocale != null) {
            return supportedLocale;
        }
        return (defaultLocale != null ? defaultLocale : requestLocale);
    }

    @Override
    public TimeZone resolveTimeZone(Translet translet) {
        return determineDefaultTimeZone(translet);
    }

    @Nullable
    private Locale findSupportedLocale(@NonNull HttpServletRequest request, List<Locale> supportedLocales) {
        Enumeration<Locale> requestLocales = request.getLocales();
        Locale languageMatch = null;
        while (requestLocales.hasMoreElements()) {
            Locale locale = requestLocales.nextElement();
            if (supportedLocales.contains(locale)) {
                if (languageMatch == null || languageMatch.getLanguage().equals(locale.getLanguage())) {
                    // Full match: language + country, possibly narrowed from earlier language-only match
                    return locale;
                }
            } else if (languageMatch == null) {
                // Let's try to find a language-only match as a fallback
                for (Locale supportedLocale : supportedLocales) {
                    if (!StringUtils.hasLength(supportedLocale.getCountry()) &&
                            supportedLocale.getLanguage().equals(locale.getLanguage())) {
                        languageMatch = supportedLocale;
                        break;
                    }
                }
            }
        }
        return languageMatch;
    }

    @Override
    public void setLocale(Translet translet, Locale locale) {
        throw new UnsupportedOperationException(
                "Cannot change HTTP Accept-Language header - use a different locale resolution strategy");
    }

    @Override
    public void setTimeZone(@NonNull Translet translet, TimeZone timeZone) {
        translet.getRequestAdapter().setTimeZone(timeZone);
    }

}
