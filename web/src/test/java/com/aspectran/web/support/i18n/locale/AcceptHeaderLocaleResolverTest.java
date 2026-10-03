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
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.test.ActivityTester;
import com.aspectran.test.AspectranTest;
import com.aspectran.web.support.http.HttpHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test cases for {@link AcceptHeaderLocaleResolver}.
 *
 * <p>Created: 2026-10-03</p>
 */
@AspectranTest
class AcceptHeaderLocaleResolverTest {

    private ActivityTester tester;

    private AcceptHeaderLocaleResolver resolver;

    @BeforeEach
    void setUp(ActivityContext context) {
        tester = new ActivityTester(context);
        resolver = new AcceptHeaderLocaleResolver();
    }

    private Translet createTranslet(Activity activity) {
        return new InstantTranslet(activity);
    }

    @Test
    void testResolveLocaleWithoutHeader() throws Exception {
        tester.perform(activity -> {
            InstantActivity instantActivity = (InstantActivity) activity;
            Translet translet = createTranslet(activity);
            DefaultRequestAdapter request = new DefaultRequestAdapter(MethodType.GET);
            instantActivity.setRequestAdapter(request);

            resolver.setDefaultLocale(Locale.ENGLISH);
            Locale resolved = resolver.resolveLocale(translet);

            assertEquals(Locale.ENGLISH, resolved);
            assertEquals(Locale.ENGLISH, translet.getRequestAdapter().getLocale());
            return null;
        });
    }

    @Test
    void testResolveLocaleWithAcceptLanguageHeader() throws Exception {
        tester.perform(activity -> {
            InstantActivity instantActivity = (InstantActivity) activity;
            Translet translet = createTranslet(activity);
            DefaultRequestAdapter request = new DefaultRequestAdapter(MethodType.GET);
            request.setHeader(HttpHeaders.ACCEPT_LANGUAGE, "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7");
            instantActivity.setRequestAdapter(request);

            Locale resolved = resolver.resolveLocale(translet);

            assertEquals(Locale.KOREA, resolved);
            assertEquals(Locale.KOREA, translet.getRequestAdapter().getLocale());
            return null;
        });
    }

    @Test
    void testResolveLocaleWithSupportedLocales() throws Exception {
        tester.perform(activity -> {
            InstantActivity instantActivity = (InstantActivity) activity;
            Translet translet = createTranslet(activity);
            DefaultRequestAdapter request = new DefaultRequestAdapter(MethodType.GET);
            request.setHeader(HttpHeaders.ACCEPT_LANGUAGE, "ja-JP,ja;q=0.9,en-US;q=0.8,en;q=0.7");
            instantActivity.setRequestAdapter(request);

            resolver.setSupportedLocales(List.of(Locale.KOREA, Locale.US));
            Locale resolved = resolver.resolveLocale(translet);

            assertEquals(Locale.US, resolved);
            assertEquals(Locale.US, translet.getRequestAdapter().getLocale());
            return null;
        });
    }

    @Test
    void testResolveLocaleWithSupportedLocalesFallback() throws Exception {
        tester.perform(activity -> {
            InstantActivity instantActivity = (InstantActivity) activity;
            Translet translet = createTranslet(activity);
            DefaultRequestAdapter request = new DefaultRequestAdapter(MethodType.GET);
            request.setHeader(HttpHeaders.ACCEPT_LANGUAGE, "fr-FR,fr;q=0.9");
            instantActivity.setRequestAdapter(request);

            resolver.setSupportedLocales(List.of(Locale.KOREA, Locale.JAPAN));
            resolver.setDefaultLocale(Locale.KOREA);
            Locale resolved = resolver.resolveLocale(translet);

            assertEquals(Locale.KOREA, resolved);
            assertEquals(Locale.KOREA, translet.getRequestAdapter().getLocale());
            return null;
        });
    }

    @Test
    void testResolveTimeZone() throws Exception {
        tester.perform(activity -> {
            InstantActivity instantActivity = (InstantActivity) activity;
            Translet translet = createTranslet(activity);
            DefaultRequestAdapter request = new DefaultRequestAdapter(MethodType.GET);
            instantActivity.setRequestAdapter(request);

            assertNull(resolver.resolveTimeZone(translet));

            resolver.setDefaultTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
            assertEquals(TimeZone.getTimeZone("Asia/Seoul"), resolver.resolveTimeZone(translet));
            return null;
        });
    }

    @Test
    void testSetLocaleUnsupported() throws Exception {
        tester.perform(activity -> {
            Translet translet = createTranslet(activity);
            assertThrows(UnsupportedOperationException.class, () ->
                    resolver.setLocale(translet, Locale.KOREA));
            return null;
        });
    }

    @Test
    void testSetTimeZone() throws Exception {
        tester.perform(activity -> {
            InstantActivity instantActivity = (InstantActivity) activity;
            Translet translet = createTranslet(activity);
            DefaultRequestAdapter request = new DefaultRequestAdapter(MethodType.GET);
            instantActivity.setRequestAdapter(request);

            resolver.setTimeZone(translet, TimeZone.getTimeZone("Asia/Seoul"));
            assertEquals(TimeZone.getTimeZone("Asia/Seoul"), translet.getRequestAdapter().getTimeZone());
            return null;
        });
    }

}
