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

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.InstantActivity;
import com.aspectran.core.activity.InstantTranslet;
import com.aspectran.core.activity.Translet;
import com.aspectran.core.adapter.DefaultRequestAdapter;
import com.aspectran.core.adapter.DefaultResponseAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.test.ActivityTester;
import com.aspectran.test.AspectranTest;
import com.aspectran.web.support.http.HttpHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test cases for {@link CookieLocaleResolver}.
 *
 * <p>Created: 2026-10-03</p>
 */
@AspectranTest
class CookieLocaleResolverTest {

    private ActivityTester tester;

    private CookieLocaleResolver resolver;

    @BeforeEach
    void setUp(ActivityContext context) {
        tester = new ActivityTester(context);
        resolver = new CookieLocaleResolver();
    }

    private Translet createTranslet(Activity activity) {
        return new InstantTranslet(activity);
    }

    @Test
    void testResolveLocaleFromCookieBcp47() throws Exception {
        tester.perform(activity -> {
            InstantActivity instantActivity = (InstantActivity)activity;
            Translet translet = createTranslet(activity);
            DefaultRequestAdapter request = new DefaultRequestAdapter(MethodType.GET);
            request.setHeader(HttpHeaders.COOKIE, CookieLocaleResolver.LOCALE_COOKIE_NAME + "=ko-KR");
            instantActivity.setRequestAdapter(request);

            Locale resolved = resolver.resolveLocale(translet);
            assertEquals(Locale.KOREA, resolved);
            assertEquals(Locale.KOREA, translet.getRequestAdapter().getLocale());
            return null;
        });
    }

    @Test
    void testResolveLocaleFromCookieLegacyFormat() throws Exception {
        tester.perform(activity -> {
            InstantActivity instantActivity = (InstantActivity)activity;
            Translet translet = createTranslet(activity);
            DefaultRequestAdapter request = new DefaultRequestAdapter(MethodType.GET);
            request.setHeader(HttpHeaders.COOKIE, CookieLocaleResolver.LOCALE_COOKIE_NAME + "=ko_KR");
            instantActivity.setRequestAdapter(request);

            resolver.setLanguageTagCompliant(false);
            Locale resolved = resolver.resolveLocale(translet);
            assertEquals(Locale.KOREA, resolved);
            assertEquals(Locale.KOREA, translet.getRequestAdapter().getLocale());
            return null;
        });
    }

    @Test
    void testResolveLocaleWithoutCookie() throws Exception {
        tester.perform(activity -> {
            InstantActivity instantActivity = (InstantActivity)activity;
            Translet translet = createTranslet(activity);
            DefaultRequestAdapter request = new DefaultRequestAdapter(MethodType.GET);
            instantActivity.setRequestAdapter(request);

            resolver.setDefaultLocale(Locale.ENGLISH);
            Locale resolved = resolver.resolveLocale(translet);
            assertEquals(Locale.ENGLISH, resolved);
            return null;
        });
    }

    @Test
    void testResolveLocaleWithEmptyCookieValue() throws Exception {
        tester.perform(activity -> {
            InstantActivity instantActivity = (InstantActivity)activity;
            Translet translet = createTranslet(activity);
            DefaultRequestAdapter request = new DefaultRequestAdapter(MethodType.GET);
            request.setHeader(HttpHeaders.COOKIE, CookieLocaleResolver.LOCALE_COOKIE_NAME + "=\"\"");
            instantActivity.setRequestAdapter(request);

            resolver.setDefaultLocale(Locale.ENGLISH);
            Locale resolved = resolver.resolveLocale(translet);
            assertEquals(Locale.ENGLISH, resolved);
            return null;
        });
    }

    @Test
    void testResolveLocaleWithInvalidCookie() throws Exception {
        tester.perform(activity -> {
            InstantActivity instantActivity = (InstantActivity)activity;
            Translet translet = createTranslet(activity);
            DefaultRequestAdapter request = new DefaultRequestAdapter(MethodType.GET);
            request.setHeader(HttpHeaders.COOKIE, CookieLocaleResolver.LOCALE_COOKIE_NAME + "=invalid@locale");
            instantActivity.setRequestAdapter(request);

            resolver.setLanguageTagCompliant(false);
            resolver.setRejectInvalidCookies(true);

            assertThrows(IllegalStateException.class, () -> resolver.resolveLocale(translet));
            return null;
        });
    }

    @Test
    void testSetLocale() throws Exception {
        tester.perform(activity -> {
            InstantActivity instantActivity = (InstantActivity)activity;
            Translet translet = createTranslet(activity);
            DefaultRequestAdapter request = new DefaultRequestAdapter(MethodType.GET);
            DefaultResponseAdapter response = new DefaultResponseAdapter(null);
            instantActivity.setRequestAdapter(request);
            instantActivity.setResponseAdapter(response);

            resolver.setCookieSameSite("Lax");
            resolver.setCookieHttpOnly(true);
            resolver.setLocale(translet, Locale.KOREA);

            assertEquals(Locale.KOREA, translet.getRequestAdapter().getLocale());

            String setCookie = response.getHeader(HttpHeaders.SET_COOKIE);
            assertNotNull(setCookie);
            assertTrue(setCookie.contains(CookieLocaleResolver.LOCALE_COOKIE_NAME + "=ko-KR"));
            assertTrue(setCookie.contains("SameSite=Lax"));
            assertTrue(setCookie.contains("HttpOnly"));
            return null;
        });
    }

    @Test
    void testSetLocaleNullRemovesCookie() throws Exception {
        tester.perform(activity -> {
            InstantActivity instantActivity = (InstantActivity)activity;
            Translet translet = createTranslet(activity);
            DefaultRequestAdapter request = new DefaultRequestAdapter(MethodType.GET);
            DefaultResponseAdapter response = new DefaultResponseAdapter(null);
            instantActivity.setRequestAdapter(request);
            instantActivity.setResponseAdapter(response);

            resolver.setLocale(translet, null);

            assertNull(translet.getRequestAdapter().getLocale());

            String setCookie = response.getHeader(HttpHeaders.SET_COOKIE);
            assertNotNull(setCookie);
            assertTrue(setCookie.contains(CookieLocaleResolver.LOCALE_COOKIE_NAME + "="));
            assertTrue(setCookie.contains("Max-Age=0"));
            return null;
        });
    }

    @Test
    void testResolveTimeZoneFromCookie() throws Exception {
        tester.perform(activity -> {
            InstantActivity instantActivity = (InstantActivity)activity;
            Translet translet = createTranslet(activity);
            DefaultRequestAdapter request = new DefaultRequestAdapter(MethodType.GET);
            request.setHeader(HttpHeaders.COOKIE, CookieLocaleResolver.TIME_ZONE_COOKIE_NAME + "=Asia/Seoul");
            instantActivity.setRequestAdapter(request);

            TimeZone resolved = resolver.resolveTimeZone(translet);
            assertNotNull(resolved);
            assertEquals(TimeZone.getTimeZone("Asia/Seoul"), resolved);
            assertEquals(TimeZone.getTimeZone("Asia/Seoul"), translet.getRequestAdapter().getTimeZone());
            return null;
        });
    }

    @Test
    void testResolveTimeZoneWithEmptyCookieValue() throws Exception {
        tester.perform(activity -> {
            InstantActivity instantActivity = (InstantActivity)activity;
            Translet translet = createTranslet(activity);
            DefaultRequestAdapter request = new DefaultRequestAdapter(MethodType.GET);
            request.setHeader(HttpHeaders.COOKIE, CookieLocaleResolver.TIME_ZONE_COOKIE_NAME + "=\"\"");
            instantActivity.setRequestAdapter(request);

            resolver.setDefaultTimeZone(TimeZone.getTimeZone("GMT"));
            TimeZone resolved = resolver.resolveTimeZone(translet);
            assertEquals(TimeZone.getTimeZone("GMT"), resolved);
            return null;
        });
    }

    @Test
    void testResolveTimeZoneWithInvalidCookie() throws Exception {
        tester.perform(activity -> {
            InstantActivity instantActivity = (InstantActivity)activity;
            Translet translet = createTranslet(activity);
            DefaultRequestAdapter request = new DefaultRequestAdapter(MethodType.GET);
            request.setHeader(HttpHeaders.COOKIE, CookieLocaleResolver.TIME_ZONE_COOKIE_NAME + "=Invalid/TimeZone_Zone");
            instantActivity.setRequestAdapter(request);

            resolver.setRejectInvalidCookies(true);

            assertThrows(IllegalStateException.class, () -> resolver.resolveTimeZone(translet));
            return null;
        });
    }

    @Test
    void testSetTimeZone() throws Exception {
        tester.perform(activity -> {
            InstantActivity instantActivity = (InstantActivity)activity;
            Translet translet = createTranslet(activity);
            DefaultRequestAdapter request = new DefaultRequestAdapter(MethodType.GET);
            DefaultResponseAdapter response = new DefaultResponseAdapter(null);
            instantActivity.setRequestAdapter(request);
            instantActivity.setResponseAdapter(response);

            resolver.setCookieSameSite("Strict");
            resolver.setTimeZone(translet, TimeZone.getTimeZone("Asia/Seoul"));

            assertEquals(TimeZone.getTimeZone("Asia/Seoul"), translet.getRequestAdapter().getTimeZone());

            String setCookie = response.getHeader(HttpHeaders.SET_COOKIE);
            assertNotNull(setCookie);
            assertTrue(setCookie.contains(CookieLocaleResolver.TIME_ZONE_COOKIE_NAME + "=Asia/Seoul"));
            assertTrue(setCookie.contains("SameSite=Strict"));
            return null;
        });
    }

    @Test
    void testSetTimeZoneNullRemovesCookie() throws Exception {
        tester.perform(activity -> {
            InstantActivity instantActivity = (InstantActivity)activity;
            Translet translet = createTranslet(activity);
            DefaultRequestAdapter request = new DefaultRequestAdapter(MethodType.GET);
            DefaultResponseAdapter response = new DefaultResponseAdapter(null);
            instantActivity.setRequestAdapter(request);
            instantActivity.setResponseAdapter(response);

            resolver.setTimeZone(translet, null);

            assertNull(translet.getRequestAdapter().getTimeZone());

            String setCookie = response.getHeader(HttpHeaders.SET_COOKIE);
            assertNotNull(setCookie);
            assertTrue(setCookie.contains(CookieLocaleResolver.TIME_ZONE_COOKIE_NAME + "="));
            assertTrue(setCookie.contains("Max-Age=0"));
            return null;
        });
    }

}
