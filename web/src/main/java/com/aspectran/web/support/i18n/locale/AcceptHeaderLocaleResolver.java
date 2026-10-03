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
package com.aspectran.web.support.i18n.locale;

import com.aspectran.core.activity.Translet;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.support.i18n.locale.AbstractLocaleResolver;
import com.aspectran.core.support.i18n.locale.LocaleResolver;
import com.aspectran.utils.StringUtils;
import com.aspectran.web.support.http.HttpHeaders;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * A {@link LocaleResolver} implementation that looks for a match between locales
 * in the {@code Accept-Language} header and a list of configured supported
 * locales.
 *
 * <p>See {@link #setSupportedLocales(List)} for further details on how
 * supported and requested locales are matched.</p>
 *
 * <p>Note: This implementation does not support {@link #setLocale} since the
 * {@code Accept-Language} header can only be changed by changing the client's
 * locale settings.</p>
 */
public class AcceptHeaderLocaleResolver extends AbstractLocaleResolver {

    @Override
    public Locale resolveLocale(@NonNull Translet translet) {
        Locale defaultLocale = getDefaultLocale();
        RequestAdapter requestAdapter = translet.getRequestAdapter();
        String header = requestAdapter.getHeader(HttpHeaders.ACCEPT_LANGUAGE);
        if (defaultLocale != null && !StringUtils.hasText(header)) {
            requestAdapter.setLocale(defaultLocale);
            return defaultLocale;
        }
        Locale requestLocale = requestAdapter.getLocale();
        if (requestLocale == null && StringUtils.hasText(header)) {
            requestLocale = findFirstLocale(header);
        }
        List<Locale> supportedLocales = getSupportedLocales();
        if (supportedLocales == null || supportedLocales.isEmpty() ||
                (requestLocale != null && supportedLocales.contains(requestLocale))) {
            Locale resolved = (requestLocale != null ? requestLocale : defaultLocale);
            if (resolved != null) {
                requestAdapter.setLocale(resolved);
            }
            return resolved;
        }
        Locale supportedLocale = findSupportedLocale(header, supportedLocales);
        if (supportedLocale != null) {
            requestAdapter.setLocale(supportedLocale);
            return supportedLocale;
        }
        Locale resolvedLocale = (defaultLocale != null ? defaultLocale : requestLocale);
        if (resolvedLocale != null) {
            requestAdapter.setLocale(resolvedLocale);
        }
        return resolvedLocale;
    }

    @Override
    @Nullable
    public TimeZone resolveTimeZone(@NonNull Translet translet) {
        return determineDefaultTimeZone(translet);
    }

    @Nullable
    private Locale findSupportedLocale(String header, List<Locale> supportedLocales) {
        if (StringUtils.hasText(header)) {
            try {
                List<Locale.LanguageRange> languageRanges = Locale.LanguageRange.parse(header);
                return Locale.lookup(languageRanges, supportedLocales);
            } catch (IllegalArgumentException e) {
                // ignore parse exception
            }
        }
        return null;
    }

    @Nullable
    private Locale findFirstLocale(String header) {
        try {
            List<Locale.LanguageRange> languageRanges = Locale.LanguageRange.parse(header);
            if (!languageRanges.isEmpty()) {
                return Locale.forLanguageTag(languageRanges.getFirst().getRange());
            }
        } catch (IllegalArgumentException e) {
            // ignore parse exception
        }
        return null;
    }

    @Override
    public void setLocale(@NonNull Translet translet, @Nullable Locale locale) {
        throw new UnsupportedOperationException(
                "Cannot change HTTP Accept-Language header - use a different locale resolution strategy");
    }

    @Override
    public void setTimeZone(@NonNull Translet translet, @Nullable TimeZone timeZone) {
        translet.getRequestAdapter().setTimeZone(timeZone);
    }

}
