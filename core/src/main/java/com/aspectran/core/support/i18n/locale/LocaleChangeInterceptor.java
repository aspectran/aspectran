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
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

import java.util.Locale;
import java.util.TimeZone;

/**
 * Interceptor that allows for changing the current locale on every request,
 * via a configurable request parameter (default parameter name: "locale").
 *
 * <p>Created: 2016. 3. 13.</p>
 */
public class LocaleChangeInterceptor {

    /**
     * Default name of the locale specification parameter: "locale".
     */
    public static final String DEFAULT_LOCALE_PARAM_NAME = "locale";

    /**
     * Default name of the timezone specification parameter: "timezone".
     */
    public static final String DEFAULT_TIMEZONE_PARAM_NAME = "timezone";

    protected final Log log = LogFactory.getLog(getClass());

    private String localeParamName = DEFAULT_LOCALE_PARAM_NAME;

    private String timeZoneParamName = DEFAULT_TIMEZONE_PARAM_NAME;

    private String[] allowedMethods;

    private boolean ignoreInvalidLocale = false;

    /**
     * Set the name of the parameter that contains a locale specification
     * in a locale change request. Default is "locale".
     *
     * @param localeParamName the locale parameter name
     */
    public void setLocaleParamName(String localeParamName) {
        this.localeParamName = localeParamName;
    }

    /**
     * Set the name of the parameter that contains a timezone specification
     * in a locale change request. Default is "timezone".
     *
     * @param timezoneParamName the timezone parameter name
     */
    public void setTimeZoneParamName(String timezoneParamName) {
        this.timeZoneParamName = timezoneParamName;
    }

    /**
     * Return the name of the parameter that contains a locale specification
     * in a locale change request.
     *
     * @return the locale parameter name
     */
    public String getLocaleParamName() {
        return this.localeParamName;
    }

    /**
     * Return the name of the parameter that contains a timezone specification
     * in a timezone change request.
     *
     * @return the time zone parameter name
     */
    public String getTimeZoneParamName() {
        return this.timeZoneParamName;
    }

    /**
     * Configure the request method(s) over which the locale can be changed.
     *
     * @param requestMethods the methods
     */
    public void setRequestMethods(String... requestMethods) {
        this.allowedMethods = requestMethods;
        if (this.allowedMethods != null) {
            for (int i = 0; i < this.allowedMethods.length; i++) {
                this.allowedMethods[i] = this.allowedMethods[i].toUpperCase();
            }
        }
    }

    /**
     * Return the configured HTTP methods.
     *
     * @return the string [ ]
     */
    public String[] getAllowedMethods() {
        return this.allowedMethods;
    }

    /**
     * Set whether to ignore an invalid value for the locale parameter.
     *
     * @param ignoreInvalidLocale whether ignoring invalid locale
     */
    public void setIgnoreInvalidLocale(boolean ignoreInvalidLocale) {
        this.ignoreInvalidLocale = ignoreInvalidLocale;
    }

    /**
     * Return whether to ignore an invalid value for the locale parameter.
     *
     * @return whether ignoring invalid locale
     */
    public boolean isIgnoreInvalidLocale() {
        return this.ignoreInvalidLocale;
    }

    public void handle(Translet translet, LocaleResolver localeResolver) {
        RequestAdapter requestAdapter = translet.getRequestAdapter();
        if (!checkRequestMethod(requestAdapter.getRequestMethod())) {
            return;
        }

        String newLocale = requestAdapter.getParameter(getLocaleParamName());
        if (newLocale != null) {
            Locale locale = null;
            try {
                locale = StringUtils.parseLocaleString(newLocale);
            } catch (IllegalArgumentException ex) {
                if (isIgnoreInvalidLocale()) {
                    log.debug("Ignoring invalid locale value [" + newLocale + "]: " + ex.getMessage());
                } else {
                    throw ex;
                }
            }
            if (locale != null) {
                if (localeResolver != null) {
                    localeResolver.setLocale(translet, locale);
                } else {
                    requestAdapter.setLocale(locale);
                }
            }
        }

        String newTimeZone = requestAdapter.getParameter(getTimeZoneParamName());
        if (newTimeZone != null) {
            TimeZone timeZone = null;
            try {
                timeZone = StringUtils.parseTimeZoneString(newTimeZone);
            } catch (IllegalArgumentException ex) {
                if (isIgnoreInvalidLocale()) {
                    log.debug("Ignoring invalid timezone value [" + newTimeZone + "]: " + ex.getMessage());
                } else {
                    throw ex;
                }
            }
            if (timeZone != null) {
                if (localeResolver != null) {
                    localeResolver.setTimeZone(translet, timeZone);
                } else {
                    requestAdapter.setTimeZone(timeZone);
                }
            }
        }
    }

    private boolean checkRequestMethod(MethodType requestMethod) {
        String[] configuredMethods = getAllowedMethods();
        if (configuredMethods == null || configuredMethods.length == 0) {
            return true;
        }
        if (requestMethod == null) {
            return false;
        }
        for (String configuredMethod : configuredMethods) {
            if (requestMethod.matches(configuredMethod)) {
                return true;
            }
        }
        return false;
    }

}
