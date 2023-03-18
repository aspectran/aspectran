/*
 * Copyright (c) 2008-2023 The Aspectran Project
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
import com.aspectran.core.lang.Nullable;
import com.aspectran.core.support.i18n.locale.AbstractLocaleResolver;
import com.aspectran.core.support.i18n.locale.LocaleResolver;
import com.aspectran.core.util.LocaleUtils;
import com.aspectran.core.util.logging.Logger;
import com.aspectran.core.util.logging.LoggerFactory;
import com.aspectran.web.support.util.CookieGenerator;
import com.aspectran.web.support.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;
import java.util.TimeZone;

/**
 * <p>This class is a clone of org.springframework.web.servlet.i18n.CookieLocaleResolver</p>
 *
 * {@link LocaleResolver} implementation that uses a cookie sent back to the user
 * in case of a custom setting, with a fallback to the specified default locale
 * or the request's accept-header locale.
 *
 * <p>This is particularly useful for stateless applications without user sessions.
 * The cookie may optionally contain an associated time zone value as well;
 * alternatively, you may specify a default time zone.</p>
 */
public class CookieLocaleResolver extends AbstractLocaleResolver {

    private static final Logger logger = LoggerFactory.getLogger(CookieLocaleResolver.class);

    private static final String LOCALE_COOKIE_NAME = CookieLocaleResolver.class.getName() + ".LOCALE";

    private static final String TIME_ZONE_COOKIE_NAME = CookieLocaleResolver.class.getName() + ".TIME_ZONE";

    private volatile CookieGenerator localeCookieGenerator;

    private volatile CookieGenerator timeZoneCookieGenerator;

    private boolean languageTagCompliant = true;

    private boolean rejectInvalidCookies = true;

    /**
     * Create a new instance of the {@link CookieLocaleResolver} class.
     */
    public CookieLocaleResolver() {
    }

    public void setLocaleCookieGenerator(CookieGenerator localeCookieGenerator) {
        this.localeCookieGenerator = localeCookieGenerator;
    }

    public CookieGenerator getLocaleCookieGenerator() {
        if (localeCookieGenerator == null) {
            localeCookieGenerator = new CookieGenerator();
            localeCookieGenerator.setCookieName(LOCALE_COOKIE_NAME);
        }
        return localeCookieGenerator;
    }

    public void setTimeZoneCookieGenerator(CookieGenerator timeZoneCookieGenerator) {
        this.timeZoneCookieGenerator = timeZoneCookieGenerator;
    }

    public CookieGenerator getTimeZoneCookieGenerator() {
        if (timeZoneCookieGenerator == null) {
            timeZoneCookieGenerator = new CookieGenerator();
            timeZoneCookieGenerator.setCookieName(TIME_ZONE_COOKIE_NAME);
        }
        return timeZoneCookieGenerator;
    }

    /**
     * Use the given domain for cookies.
     * The cookie is only visible to servers in this domain.
     *
     * @see javax.servlet.http.Cookie#setDomain
     */
    public void setCookieDomain(String cookieDomain) {
        getLocaleCookieGenerator().setCookieDomain(cookieDomain);
        getTimeZoneCookieGenerator().setCookieDomain(cookieDomain);
    }

    /**
     * Use the given path for cookies.
     * The cookie is only visible to URLs in this path and below.
     *
     * @see javax.servlet.http.Cookie#setPath
     */
    public void setCookiePath(String cookiePath) {
        getLocaleCookieGenerator().setCookiePath(cookiePath);
        getTimeZoneCookieGenerator().setCookiePath(cookiePath);
    }

    /**
     * Use the given maximum age (in seconds) for cookies.
     * Useful special value: -1 ... not persistent, deleted when client shuts down.
     * <p>Default is no specific maximum age at all, using the Servlet container's
     * default.</p>
     *
     * @see javax.servlet.http.Cookie#setMaxAge
     */
    public void setCookieMaxAge(Integer cookieMaxAge) {
        getLocaleCookieGenerator().setCookieMaxAge(cookieMaxAge);
        getTimeZoneCookieGenerator().setCookieMaxAge(cookieMaxAge);
    }

    /**
     * Set whether the cookie should only be sent using a secure protocol,
     * such as HTTPS (SSL). This is an indication to the receiving browser,
     * not processed by the HTTP server itself.
     * <p>Default is "false".</p>
     *
     * @see javax.servlet.http.Cookie#setSecure
     */
    public void setCookieSecure(boolean cookieSecure) {
        getLocaleCookieGenerator().setCookieSecure(cookieSecure);
        getTimeZoneCookieGenerator().setCookieSecure(cookieSecure);
    }

    /**
     * Set whether the cookie is supposed to be marked with the "HttpOnly" attribute.
     * <p>Default is "false".</p>
     *
     * @see javax.servlet.http.Cookie#setHttpOnly
     */
    public void setCookieHttpOnly(boolean cookieHttpOnly) {
        getLocaleCookieGenerator().setCookieHttpOnly(cookieHttpOnly);
        getTimeZoneCookieGenerator().setCookieHttpOnly(cookieHttpOnly);
    }

    /**
     * Specify whether this resolver's cookies should be compliant with BCP 47
     * language tags instead of Java's legacy locale specification format.
     * <p>The default is {@code true}, as of 5.1. Switch this to {@code false}
     * for rendering Java's legacy locale specification format. For parsing,
     * this resolver leniently accepts the legacy {@link Locale#toString}
     * format as well as BCP 47 language tags in any case.</p>
     *
     * @see #parseLocaleValue(String)
     * @see #toLocaleValue(Locale)
     * @see Locale#forLanguageTag(String)
     * @see Locale#toLanguageTag()
     */
    public void setLanguageTagCompliant(boolean languageTagCompliant) {
        this.languageTagCompliant = languageTagCompliant;
    }

    /**
     * Return whether this resolver's cookies should be compliant with BCP 47
     * language tags instead of Java's legacy locale specification format.
     */
    public boolean isLanguageTagCompliant() {
        return this.languageTagCompliant;
    }

    /**
     * Specify whether to reject cookies with invalid content (e.g. invalid format).
     * <p>The default is {@code true}. Turn this off for lenient handling of parse
     * failures, falling back to the default locale and time zone in such a case.</p>
     *
     * @see #setDefaultLocale
     * @see #setDefaultTimeZone
     * @see #determineDefaultLocale
     * @see #determineDefaultTimeZone
     */
    public void setRejectInvalidCookies(boolean rejectInvalidCookies) {
        this.rejectInvalidCookies = rejectInvalidCookies;
    }

    /**
     * Return whether to reject cookies with invalid content (e.g. invalid format).
     */
    public boolean isRejectInvalidCookies() {
        return this.rejectInvalidCookies;
    }

    @Override
    public Locale resolveLocale(Translet translet) {
        Locale locale = parseLocaleCookie(translet);
        if (locale != null) {
            translet.getRequestAdapter().setLocale(locale);
            return locale;
        }
        return determineDefaultLocale(translet);
    }

    @Override
    public TimeZone resolveTimeZone(Translet translet) {
        TimeZone timeZone = parseTimeZoneCookie(translet);
        if (timeZone != null) {
            translet.getRequestAdapter().setTimeZone(timeZone);
            return timeZone;
        }
        return determineDefaultTimeZone(translet);
    }

    @Override
    public void setLocale(Translet translet, Locale locale) {
        translet.getRequestAdapter().setLocale(locale);
        HttpServletResponse response = translet.getResponseAdapter().getAdaptee();
        getLocaleCookieGenerator().addCookie(response, (locale != null ? toLocaleValue(locale) : ""));
    }

    @Override
    public void setTimeZone(Translet translet, TimeZone timeZone) {
        translet.getRequestAdapter().setTimeZone(timeZone);
        HttpServletResponse response = translet.getResponseAdapter().getAdaptee();
        getTimeZoneCookieGenerator().addCookie(response, (timeZone != null ? timeZone.getID() : ""));
    }

    private Locale parseLocaleCookie(Translet translet) {
        Locale locale = null;
        String cookieName = getLocaleCookieGenerator().getCookieName();
        HttpServletRequest request = translet.getRequestAdapter().getAdaptee();
        Cookie cookie = WebUtils.getCookie(request, cookieName);
        if (cookie != null) {
            String value = cookie.getValue();
            try {
                locale = parseLocaleValue(value);
            } catch (IllegalArgumentException ex) {
                if (isRejectInvalidCookies() &&
                        request.getAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE) == null) {
                    throw new IllegalStateException("Encountered invalid locale cookie '" +
                            cookieName + "': [" + value + "] due to: " + ex.getMessage());
                } else {
                    // Lenient handling (e.g. error dispatch): ignore locale/timezone parse exceptions
                    if (logger.isDebugEnabled()) {
                        logger.debug("Ignoring invalid locale cookie '" + cookieName +
                                "': [" + value + "] due to: " + ex.getMessage());
                    }
                }
            }
            if (locale != null && logger.isTraceEnabled()) {
                logger.trace("Parsed cookie value [" + cookie.getValue() + "] into locale '" + locale + "'");
            }
        }
        return locale;
    }

    private TimeZone parseTimeZoneCookie(Translet translet) {
        TimeZone timeZone = null;
        String cookieName = getTimeZoneCookieGenerator().getCookieName();
        HttpServletRequest request = translet.getRequestAdapter().getAdaptee();
        Cookie cookie = WebUtils.getCookie(request, cookieName);
        if (cookie != null) {
            String value = cookie.getValue();
            try {
                timeZone = LocaleUtils.parseTimeZoneString(value);
            } catch (IllegalArgumentException ex) {
                if (isRejectInvalidCookies() &&
                        request.getAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE) == null) {
                    throw new IllegalStateException("Encountered invalid time zone cookie '" +
                            cookieName + "': [" + value + "] due to: " + ex.getMessage());
                } else {
                    // Lenient handling (e.g. error dispatch): ignore locale/timezone parse exceptions
                    if (logger.isDebugEnabled()) {
                        logger.debug("Ignoring invalid time zone cookie '" + cookieName +
                                "': [" + value + "] due to: " + ex.getMessage());
                    }
                }
            }
            if (timeZone != null && logger.isTraceEnabled()) {
                logger.trace("Parsed cookie value [" + cookie.getValue() + "] into time zone '" + timeZone.getID() + "'");
            }
        }
        return timeZone;
    }

    /**
     * Parse the given locale value coming from an incoming cookie.
     * <p>The default implementation calls {@link LocaleUtils#parseLocale(String)},
     * accepting the {@link Locale#toString} format as well as BCP 47 language tags.</p>
     *
     * @param localeValue the locale value to parse
     * @return the corresponding {@code Locale} instance
     * @see LocaleUtils#parseLocale(String)
     */
    @Nullable
    protected Locale parseLocaleValue(String localeValue) {
        return LocaleUtils.parseLocale(localeValue);
    }

    /**
     * Render the given locale as a text value for inclusion in a cookie.
     * <p>The default implementation calls {@link Locale#toString()}
     * or JDK 7's {@link Locale#toLanguageTag()}, depending on the
     * {@link #setLanguageTagCompliant "languageTagCompliant"} configuration property.
     * @param locale the locale to stringify
     * @return a String representation for the given locale
     * @see #isLanguageTagCompliant()
     */
    protected String toLocaleValue(Locale locale) {
        return (isLanguageTagCompliant() ? locale.toLanguageTag() : locale.toString());
    }

}
