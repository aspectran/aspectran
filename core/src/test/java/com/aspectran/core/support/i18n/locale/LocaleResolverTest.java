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

import com.aspectran.core.activity.CoreTranslet;
import com.aspectran.core.activity.InstantActivity;
import com.aspectran.core.activity.Translet;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.builder.HybridActivityContextBuilder;
import com.aspectran.core.context.rule.TransletRule;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test cases for {@link LocaleResolver} implementations and {@link LocaleChangeInterceptor}.
 */
class LocaleResolverTest {

    @Test
    void testFixedLocaleResolver() {
        FixedLocaleResolver resolver = new FixedLocaleResolver(Locale.KOREAN, TimeZone.getTimeZone("Asia/Seoul"));
        assertEquals(Locale.KOREAN, resolver.getDefaultLocale());
        assertEquals(TimeZone.getTimeZone("Asia/Seoul"), resolver.getDefaultTimeZone());

        String str = resolver.toString();
        assertNotNull(str);
        assertTrue(str.contains("ko"));
        assertTrue(str.contains("Asia/Seoul"));
    }

    @Test
    void testAbstractLocaleResolverConfig() {
        FixedLocaleResolver resolver = new FixedLocaleResolver();
        resolver.setDefaultLocale("en_US");
        resolver.setDefaultTimeZone("GMT");
        resolver.setSupportedLocales("en_US", "ko_KR", "ja_JP");

        assertEquals(Locale.US, resolver.getDefaultLocale());
        assertEquals(TimeZone.getTimeZone("GMT"), resolver.getDefaultTimeZone());

        List<Locale> supported = resolver.getSupportedLocales();
        assertNotNull(supported);
        assertEquals(3, supported.size());
        assertTrue(supported.contains(Locale.US));
        assertTrue(supported.contains(Locale.KOREA));
        assertTrue(supported.contains(Locale.JAPAN));

        resolver.setSupportedLocales((String[]) null);
        assertNull(resolver.getSupportedLocales());
    }

    @Test
    void testLocaleChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setLocaleParamName("lang");
        interceptor.setTimeZoneParamName("tz");
        interceptor.setRequestMethods("GET", "POST");
        interceptor.setIgnoreInvalidLocale(true);

        assertEquals("lang", interceptor.getLocaleParamName());
        assertEquals("tz", interceptor.getTimeZoneParamName());
        assertTrue(interceptor.isIgnoreInvalidLocale());
        assertNotNull(interceptor.getRequestMethods());
        assertEquals(2, interceptor.getRequestMethods().length);

        String str = interceptor.toString();
        assertNotNull(str);
        assertTrue(str.contains("lang"));
        assertTrue(str.contains("tz"));
    }

    @Test
    void testFixedLocaleResolverResolution() throws Exception {
        HybridActivityContextBuilder builder = new HybridActivityContextBuilder();
        ActivityContext context = builder.build();
        InstantActivity activity = new InstantActivity(context);

        activity.perform(() -> {
            TransletRule transletRule = new TransletRule();
            Translet translet = new CoreTranslet(transletRule, activity);

            FixedLocaleResolver resolver = new FixedLocaleResolver(Locale.KOREAN);
            Locale resolved = resolver.resolveLocale(translet);
            assertEquals(Locale.KOREAN, resolved);
            assertEquals(Locale.KOREAN, translet.getRequestAdapter().getLocale());

            // Default time zone should be null when not configured
            TimeZone timeZone = resolver.resolveTimeZone(translet);
            assertNull(timeZone);
            return null;
        });
    }

    @Test
    void testFixedLocaleResolverFixedValue() throws Exception {
        HybridActivityContextBuilder builder = new HybridActivityContextBuilder();
        ActivityContext context = builder.build();
        InstantActivity activity = new InstantActivity(context);

        activity.perform(() -> {
            TransletRule transletRule = new TransletRule();
            Translet translet = new CoreTranslet(transletRule, activity);

            FixedLocaleResolver resolver = new FixedLocaleResolver(Locale.GERMAN, TimeZone.getTimeZone("Europe/Berlin"));
            Locale resolved = resolver.resolveLocale(translet);
            assertEquals(Locale.GERMAN, resolved);
            assertEquals(Locale.GERMAN, translet.getRequestAdapter().getLocale());

            TimeZone timeZone = resolver.resolveTimeZone(translet);
            assertEquals(TimeZone.getTimeZone("Europe/Berlin"), timeZone);
            assertEquals(TimeZone.getTimeZone("Europe/Berlin"), translet.getRequestAdapter().getTimeZone());
            return null;
        });
    }

    @Test
    void testSessionLocaleResolverWithSupportedLocales() throws Exception {
        HybridActivityContextBuilder builder = new HybridActivityContextBuilder();
        ActivityContext context = builder.build();
        InstantActivity activity = new InstantActivity(context);

        activity.perform(() -> {
            TransletRule transletRule = new TransletRule();
            Translet translet = new CoreTranslet(transletRule, activity);

            SessionLocaleResolver resolver = new SessionLocaleResolver();
            resolver.setDefaultLocale(Locale.ENGLISH);
            resolver.setSupportedLocales("en", "ko");

            // Without session, falls back to defaultLocale
            Locale resolved = resolver.resolveLocale(translet);
            assertEquals(Locale.ENGLISH, resolved);
            return null;
        });
    }

    @Test
    void testSessionLocaleResolverToString() {
        SessionLocaleResolver resolver = new SessionLocaleResolver();
        resolver.setDefaultLocale(Locale.ENGLISH);
        resolver.setDefaultTimeZone(TimeZone.getTimeZone("UTC"));

        String str = resolver.toString();
        assertNotNull(str);
        assertTrue(str.contains("en"));
        assertTrue(str.contains("UTC"));
    }

}
