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
package com.aspectran.web.support.cors;

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
import com.aspectran.web.support.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test cases for {@link DefaultCorsProcessor}.
 */
@AspectranTest
class DefaultCorsProcessorTest {

    private ActivityTester tester;

    private DefaultCorsProcessor corsProcessor;

    @BeforeEach
    void setUp(ActivityContext context) {
        tester = new ActivityTester(context);
        corsProcessor = new DefaultCorsProcessor();
    }

    private Translet createTranslet(Activity activity) {
        return new InstantTranslet(activity);
    }

    @Test
    void testIsCorsRequest() {
        DefaultRequestAdapter nonCorsRequest = new DefaultRequestAdapter(MethodType.GET);
        org.junit.jupiter.api.Assertions.assertFalse(corsProcessor.isCorsRequest(nonCorsRequest));

        DefaultRequestAdapter corsRequest = new DefaultRequestAdapter(MethodType.GET);
        corsRequest.setHeader(HttpHeaders.ORIGIN, "https://example.com");
        org.junit.jupiter.api.Assertions.assertTrue(corsProcessor.isCorsRequest(corsRequest));
    }

    @Test
    void testIsPreflightRequest() {
        DefaultRequestAdapter nonCorsRequest = new DefaultRequestAdapter(MethodType.OPTIONS);
        org.junit.jupiter.api.Assertions.assertFalse(corsProcessor.isPreflightRequest(nonCorsRequest));

        DefaultRequestAdapter notOptionsRequest = new DefaultRequestAdapter(MethodType.POST);
        notOptionsRequest.setHeader(HttpHeaders.ORIGIN, "https://example.com");
        notOptionsRequest.setHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST");
        org.junit.jupiter.api.Assertions.assertFalse(corsProcessor.isPreflightRequest(notOptionsRequest));

        DefaultRequestAdapter missingMethodHeader = new DefaultRequestAdapter(MethodType.OPTIONS);
        missingMethodHeader.setHeader(HttpHeaders.ORIGIN, "https://example.com");
        org.junit.jupiter.api.Assertions.assertFalse(corsProcessor.isPreflightRequest(missingMethodHeader));

        DefaultRequestAdapter preflightRequest = new DefaultRequestAdapter(MethodType.OPTIONS);
        preflightRequest.setHeader(HttpHeaders.ORIGIN, "https://example.com");
        preflightRequest.setHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST");
        org.junit.jupiter.api.Assertions.assertTrue(corsProcessor.isPreflightRequest(preflightRequest));
        org.junit.jupiter.api.Assertions.assertTrue(corsProcessor.isPreFlightRequest(preflightRequest));
    }

    @Test
    void testActualRequestNonCors() throws Exception {
        tester.perform(activity -> {
            InstantActivity instantActivity = (InstantActivity) activity;
            Translet translet = createTranslet(activity);
            DefaultRequestAdapter request = new DefaultRequestAdapter(MethodType.GET);
            DefaultResponseAdapter response = new DefaultResponseAdapter(null);

            instantActivity.setRequestAdapter(request);
            instantActivity.setResponseAdapter(response);

            corsProcessor.processActualRequest(translet);

            assertNull(response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
            return null;
        });
    }

    @Test
    void testActualRequestAllowedAnyOrigin() throws Exception {
        tester.perform(activity -> {
            InstantActivity instantActivity = (InstantActivity) activity;
            Translet translet = createTranslet(activity);
            DefaultRequestAdapter request = new DefaultRequestAdapter(MethodType.GET);
            request.setHeader(HttpHeaders.ORIGIN, "https://example.com");
            DefaultResponseAdapter response = new DefaultResponseAdapter(null);

            instantActivity.setRequestAdapter(request);
            instantActivity.setResponseAdapter(response);

            corsProcessor.processActualRequest(translet);

            assertEquals("*", response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
            assertEquals(HttpHeaders.ORIGIN, response.getHeader(HttpHeaders.VARY));
            return null;
        });
    }

    @Test
    void testActualRequestAllowedSpecificOrigin() throws Exception {
        corsProcessor.setAllowedOrigins("https://example.com, https://aspectran.com");

        tester.perform(activity -> {
            InstantActivity instantActivity = (InstantActivity) activity;
            Translet translet = createTranslet(activity);
            DefaultRequestAdapter request = new DefaultRequestAdapter(MethodType.GET);
            request.setHeader(HttpHeaders.ORIGIN, "https://example.com");
            DefaultResponseAdapter response = new DefaultResponseAdapter(null);

            instantActivity.setRequestAdapter(request);
            instantActivity.setResponseAdapter(response);

            corsProcessor.processActualRequest(translet);

            assertEquals("https://example.com", response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
            assertEquals(HttpHeaders.ORIGIN, response.getHeader(HttpHeaders.VARY));
            return null;
        });
    }

    @Test
    void testActualRequestDeniedOrigin() throws Exception {
        corsProcessor.setAllowedOrigins("https://example.com");

        tester.perform(activity -> {
            InstantActivity instantActivity = (InstantActivity) activity;
            Translet translet = createTranslet(activity);
            DefaultRequestAdapter request = new DefaultRequestAdapter(MethodType.GET);
            request.setHeader(HttpHeaders.ORIGIN, "https://evil.com");
            DefaultResponseAdapter response = new DefaultResponseAdapter(null);

            instantActivity.setRequestAdapter(request);
            instantActivity.setResponseAdapter(response);

            CorsException ex = assertThrows(CorsException.class, () -> corsProcessor.processActualRequest(translet));
            assertEquals(CorsException.ORIGIN_DENIED, ex);
            assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
            return null;
        });
    }

    @Test
    void testActualRequestUnsupportedMethod() throws Exception {
        corsProcessor.setAllowedMethods("GET, POST");

        tester.perform(activity -> {
            InstantActivity instantActivity = (InstantActivity) activity;
            Translet translet = createTranslet(activity);
            DefaultRequestAdapter request = new DefaultRequestAdapter(MethodType.DELETE);
            request.setHeader(HttpHeaders.ORIGIN, "https://example.com");
            DefaultResponseAdapter response = new DefaultResponseAdapter(null);

            instantActivity.setRequestAdapter(request);
            instantActivity.setResponseAdapter(response);

            CorsException ex = assertThrows(CorsException.class, () -> corsProcessor.processActualRequest(translet));
            assertEquals(CorsException.UNSUPPORTED_METHOD, ex);
            assertEquals(HttpStatus.METHOD_NOT_ALLOWED.value(), response.getStatus());
            return null;
        });
    }

    @Test
    void testActualRequestAllowCredentials() throws Exception {
        corsProcessor.setAllowCredentials(true);
        corsProcessor.setAllowedOrigins("https://example.com");

        tester.perform(activity -> {
            InstantActivity instantActivity = (InstantActivity) activity;
            Translet translet = createTranslet(activity);
            DefaultRequestAdapter request = new DefaultRequestAdapter(MethodType.GET);
            request.setHeader(HttpHeaders.ORIGIN, "https://example.com");
            DefaultResponseAdapter response = new DefaultResponseAdapter(null);

            instantActivity.setRequestAdapter(request);
            instantActivity.setResponseAdapter(response);

            corsProcessor.processActualRequest(translet);

            assertEquals("https://example.com", response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
            assertEquals("true", response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS));
            assertEquals(HttpHeaders.ORIGIN, response.getHeader(HttpHeaders.VARY));
            return null;
        });
    }

    @Test
    void testActualRequestExposedHeaders() throws Exception {
        corsProcessor.setExposedHeaders("X-Custom-Header, X-Other-Header");

        tester.perform(activity -> {
            InstantActivity instantActivity = (InstantActivity) activity;
            Translet translet = createTranslet(activity);
            DefaultRequestAdapter request = new DefaultRequestAdapter(MethodType.GET);
            request.setHeader(HttpHeaders.ORIGIN, "https://example.com");
            DefaultResponseAdapter response = new DefaultResponseAdapter(null);

            instantActivity.setRequestAdapter(request);
            instantActivity.setResponseAdapter(response);

            corsProcessor.processActualRequest(translet);

            assertEquals("X-Custom-Header, X-Other-Header", response.getHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS));
            return null;
        });
    }

    @Test
    void testPreflightRequestSuccess() throws Exception {
        corsProcessor.setAllowedOrigins("https://example.com");
        corsProcessor.setAllowedMethods("GET, POST, PUT");
        corsProcessor.setAllowedHeaders("Content-Type, Authorization");
        corsProcessor.setMaxAgeSeconds(3600);

        tester.perform(activity -> {
            InstantActivity instantActivity = (InstantActivity) activity;
            Translet translet = createTranslet(activity);
            DefaultRequestAdapter request = new DefaultRequestAdapter(MethodType.OPTIONS);
            request.setHeader(HttpHeaders.ORIGIN, "https://example.com");
            request.setHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST");
            request.setHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Content-Type, Authorization");
            DefaultResponseAdapter response = new DefaultResponseAdapter(null);

            instantActivity.setRequestAdapter(request);
            instantActivity.setResponseAdapter(response);

            corsProcessor.processPreflightRequest(translet);

            assertEquals("https://example.com", response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
            assertEquals("GET, POST, PUT", response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS));
            assertEquals("Content-Type, Authorization", response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS));
            assertEquals("3600", response.getHeader(HttpHeaders.ACCESS_CONTROL_MAX_AGE));
            return null;
        });
    }

    @Test
    void testPreflightRequestCaseInsensitiveHeadersAndMethods() throws Exception {
        corsProcessor.setAllowedOrigins("https://example.com");
        corsProcessor.setAllowedMethods("POST, PUT");
        corsProcessor.setAllowedHeaders("Content-Type, Authorization");

        tester.perform(activity -> {
            InstantActivity instantActivity = (InstantActivity) activity;
            Translet translet = createTranslet(activity);
            DefaultRequestAdapter request = new DefaultRequestAdapter(MethodType.OPTIONS);
            request.setHeader(HttpHeaders.ORIGIN, "https://example.com");
            request.setHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "post"); // Lowercase method
            request.setHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "content-type, authorization"); // Lowercase headers
            DefaultResponseAdapter response = new DefaultResponseAdapter(null);

            instantActivity.setRequestAdapter(request);
            instantActivity.setResponseAdapter(response);

            corsProcessor.processPreflightRequest(translet);

            assertEquals("https://example.com", response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
            assertEquals("POST, PUT", response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS));
            assertEquals("Content-Type, Authorization", response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS));
            return null;
        });
    }

    @Test
    void testPreflightRequestMaxAgeZero() throws Exception {
        corsProcessor.setMaxAgeSeconds(0);

        tester.perform(activity -> {
            InstantActivity instantActivity = (InstantActivity) activity;
            Translet translet = createTranslet(activity);
            DefaultRequestAdapter request = new DefaultRequestAdapter(MethodType.OPTIONS);
            request.setHeader(HttpHeaders.ORIGIN, "https://example.com");
            request.setHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET");
            DefaultResponseAdapter response = new DefaultResponseAdapter(null);

            instantActivity.setRequestAdapter(request);
            instantActivity.setResponseAdapter(response);

            corsProcessor.processPreflightRequest(translet);

            assertEquals("0", response.getHeader(HttpHeaders.ACCESS_CONTROL_MAX_AGE));
            return null;
        });
    }

    @Test
    void testPreflightRequestMissingRequestMethod() throws Exception {
        tester.perform(activity -> {
            InstantActivity instantActivity = (InstantActivity) activity;
            Translet translet = createTranslet(activity);
            DefaultRequestAdapter request = new DefaultRequestAdapter(MethodType.OPTIONS);
            request.setHeader(HttpHeaders.ORIGIN, "https://example.com");
            DefaultResponseAdapter response = new DefaultResponseAdapter(null);

            instantActivity.setRequestAdapter(request);
            instantActivity.setResponseAdapter(response);

            CorsException ex = assertThrows(CorsException.class, () -> corsProcessor.processPreflightRequest(translet));
            assertEquals(CorsException.MISSING_ACCESS_CONTROL_REQUEST_METHOD_HEADER, ex);
            assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
            return null;
        });
    }

    @Test
    void testPreflightRequestUnsupportedHeader() throws Exception {
        corsProcessor.setAllowedHeaders("Content-Type");

        tester.perform(activity -> {
            InstantActivity instantActivity = (InstantActivity) activity;
            Translet translet = createTranslet(activity);
            DefaultRequestAdapter request = new DefaultRequestAdapter(MethodType.OPTIONS);
            request.setHeader(HttpHeaders.ORIGIN, "https://example.com");
            request.setHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET");
            request.setHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "X-Evil-Header");
            DefaultResponseAdapter response = new DefaultResponseAdapter(null);

            instantActivity.setRequestAdapter(request);
            instantActivity.setResponseAdapter(response);

            CorsException ex = assertThrows(CorsException.class, () -> corsProcessor.processPreflightRequest(translet));
            assertEquals(CorsException.UNSUPPORTED_REQUEST_HEADER, ex);
            assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
            return null;
        });
    }

    @Test
    void testPreflightRequestDeniedOrigin() throws Exception {
        corsProcessor.setAllowedOrigins("https://allowed.com");

        tester.perform(activity -> {
            InstantActivity instantActivity = (InstantActivity) activity;
            Translet translet = createTranslet(activity);
            DefaultRequestAdapter request = new DefaultRequestAdapter(MethodType.OPTIONS);
            request.setHeader(HttpHeaders.ORIGIN, "https://denied.com");
            request.setHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET");
            DefaultResponseAdapter response = new DefaultResponseAdapter(null);

            instantActivity.setRequestAdapter(request);
            instantActivity.setResponseAdapter(response);

            CorsException ex = assertThrows(CorsException.class, () -> corsProcessor.processPreflightRequest(translet));
            assertEquals(CorsException.ORIGIN_DENIED, ex);
            assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
            return null;
        });
    }

}
