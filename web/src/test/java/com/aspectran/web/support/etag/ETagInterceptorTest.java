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
package com.aspectran.web.support.etag;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.InstantActivity;
import com.aspectran.core.activity.InstantTranslet;
import com.aspectran.core.activity.Translet;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.adapter.DefaultRequestAdapter;
import com.aspectran.core.adapter.DefaultResponseAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.test.ActivityTester;
import com.aspectran.test.AspectranTest;
import com.aspectran.utils.DigestUtils;
import com.aspectran.web.support.http.HttpHeaders;
import com.aspectran.web.support.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AspectranTest
class ETagInterceptorTest {

    private ActivityTester tester;

    private ETagInterceptor interceptor;

    private static final byte[] TOKEN_DATA = "resource-content".getBytes(StandardCharsets.UTF_8);

    @BeforeEach
    void setUp(ActivityContext context) {
        tester = new ActivityTester(context);
        ETagTokenFactory tokenFactory = translet -> TOKEN_DATA;
        interceptor = new ETagInterceptor(tokenFactory);
    }

    private Translet createTranslet(Activity activity) {
        return new InstantTranslet(activity) {
            private Response response;
            private boolean responseReserved;

            @Override
            public void response(Response response) {
                this.response = response;
                this.responseReserved = true;
            }

            @Override
            public void response() {
                this.responseReserved = true;
            }

            @Override
            public Response getDeclaredResponse() {
                return response;
            }

            @Override
            public boolean isResponseReserved() {
                return responseReserved;
            }
        };
    }

    @Test
    void testGenerateETagToken() throws Exception {
        tester.perform(activity -> {
            Translet translet = createTranslet(activity);

            String strongEtag = interceptor.generateETagToken(translet, false);
            assertNotNull(strongEtag);
            assertTrue(strongEtag.startsWith("\"0"));
            assertTrue(strongEtag.endsWith("\""));
            assertEquals("\"0" + DigestUtils.md5DigestAsHex(TOKEN_DATA) + "\"", strongEtag);

            String weakEtag = interceptor.generateETagToken(translet, true);
            assertNotNull(weakEtag);
            assertTrue(weakEtag.startsWith("W/\"0"));
            assertTrue(weakEtag.endsWith("\""));
            assertEquals("W/\"0" + DigestUtils.md5DigestAsHex(TOKEN_DATA) + "\"", weakEtag);

            return null;
        });
    }

    @Test
    void testInterceptSetsETagHeader() throws Exception {
        tester.perform(activity -> {
            InstantActivity instantActivity = (InstantActivity) activity;
            Translet translet = createTranslet(activity);
            DefaultRequestAdapter request = new DefaultRequestAdapter(MethodType.GET);
            DefaultResponseAdapter response = new DefaultResponseAdapter(null);
            response.setStatus(200);

            instantActivity.setRequestAdapter(request);
            instantActivity.setResponseAdapter(response);

            interceptor.intercept(translet);

            String etag = response.getHeader(HttpHeaders.ETAG);
            assertNotNull(etag);
            assertEquals("\"0" + DigestUtils.md5DigestAsHex(TOKEN_DATA) + "\"", etag);
            assertEquals(200, response.getStatus());

            return null;
        });
    }

    @Test
    void testInterceptIfNoneMatchExact() throws Exception {
        tester.perform(activity -> {
            InstantActivity instantActivity = (InstantActivity) activity;
            Translet translet = createTranslet(activity);
            String expectedETag = "\"0" + DigestUtils.md5DigestAsHex(TOKEN_DATA) + "\"";

            DefaultRequestAdapter request = new DefaultRequestAdapter(MethodType.GET);
            request.setHeader(HttpHeaders.IF_NONE_MATCH, expectedETag);
            DefaultResponseAdapter response = new DefaultResponseAdapter(null);
            response.setStatus(200);

            instantActivity.setRequestAdapter(request);
            instantActivity.setResponseAdapter(response);

            interceptor.intercept(translet);

            assertEquals(HttpStatus.NOT_MODIFIED.value(), response.getStatus());
            assertEquals(expectedETag, response.getHeader(HttpHeaders.ETAG));
            assertTrue(translet.isResponseReserved());

            return null;
        });
    }

    @Test
    void testInterceptIfNoneMatchWildcard() throws Exception {
        tester.perform(activity -> {
            InstantActivity instantActivity = (InstantActivity) activity;
            Translet translet = createTranslet(activity);

            DefaultRequestAdapter request = new DefaultRequestAdapter(MethodType.GET);
            request.setHeader(HttpHeaders.IF_NONE_MATCH, "*");
            DefaultResponseAdapter response = new DefaultResponseAdapter(null);
            response.setStatus(200);

            instantActivity.setRequestAdapter(request);
            instantActivity.setResponseAdapter(response);

            interceptor.intercept(translet);

            assertEquals(HttpStatus.NOT_MODIFIED.value(), response.getStatus());
            assertTrue(translet.isResponseReserved());

            return null;
        });
    }

    @Test
    void testInterceptIfNoneMatchMismatch() throws Exception {
        tester.perform(activity -> {
            InstantActivity instantActivity = (InstantActivity) activity;
            Translet translet = createTranslet(activity);

            DefaultRequestAdapter request = new DefaultRequestAdapter(MethodType.GET);
            request.setHeader(HttpHeaders.IF_NONE_MATCH, "\"other-etag\"");
            DefaultResponseAdapter response = new DefaultResponseAdapter(null);
            response.setStatus(200);

            instantActivity.setRequestAdapter(request);
            instantActivity.setResponseAdapter(response);

            interceptor.intercept(translet);

            assertEquals(200, response.getStatus());
            assertNotNull(response.getHeader(HttpHeaders.ETAG));

            return null;
        });
    }

    @Test
    void testInterceptCacheControlNoStore() throws Exception {
        tester.perform(activity -> {
            InstantActivity instantActivity = (InstantActivity) activity;
            Translet translet = createTranslet(activity);

            DefaultRequestAdapter request = new DefaultRequestAdapter(MethodType.GET);
            DefaultResponseAdapter response = new DefaultResponseAdapter(null);
            response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store, no-cache");
            response.setStatus(200);

            instantActivity.setRequestAdapter(request);
            instantActivity.setResponseAdapter(response);

            interceptor.intercept(translet);

            assertNull(response.getHeader(HttpHeaders.ETAG));
            assertEquals(200, response.getStatus());

            return null;
        });
    }

    @Test
    void testInterceptIneligibleMethod() throws Exception {
        tester.perform(activity -> {
            InstantActivity instantActivity = (InstantActivity) activity;
            Translet translet = createTranslet(activity);

            DefaultRequestAdapter request = new DefaultRequestAdapter(MethodType.POST);
            DefaultResponseAdapter response = new DefaultResponseAdapter(null);
            response.setStatus(200);

            instantActivity.setRequestAdapter(request);
            instantActivity.setResponseAdapter(response);

            interceptor.intercept(translet);

            assertNull(response.getHeader(HttpHeaders.ETAG));

            return null;
        });
    }

    @Test
    void testInterceptIneligibleResponseStatus() throws Exception {
        tester.perform(activity -> {
            InstantActivity instantActivity = (InstantActivity) activity;
            Translet translet = createTranslet(activity);

            DefaultRequestAdapter request = new DefaultRequestAdapter(MethodType.GET);
            DefaultResponseAdapter response = new DefaultResponseAdapter(null);
            response.setStatus(404);

            instantActivity.setRequestAdapter(request);
            instantActivity.setResponseAdapter(response);

            interceptor.intercept(translet);

            assertNull(response.getHeader(HttpHeaders.ETAG));

            return null;
        });
    }

    @Test
    void testInterceptCacheControlCaseInsensitiveNoStore() throws Exception {
        tester.perform(activity -> {
            InstantActivity instantActivity = (InstantActivity) activity;
            Translet translet = createTranslet(activity);

            DefaultRequestAdapter request = new DefaultRequestAdapter(MethodType.GET);
            DefaultResponseAdapter response = new DefaultResponseAdapter(null);
            response.setHeader(HttpHeaders.CACHE_CONTROL, "No-Store, private");
            response.setStatus(200);

            instantActivity.setRequestAdapter(request);
            instantActivity.setResponseAdapter(response);

            interceptor.intercept(translet);

            assertNull(response.getHeader(HttpHeaders.ETAG));
            assertEquals(200, response.getStatus());

            return null;
        });
    }

    @Test
    void testInterceptIfNoneMatchMultipleTags() throws Exception {
        tester.perform(activity -> {
            InstantActivity instantActivity = (InstantActivity) activity;
            Translet translet = createTranslet(activity);
            String expectedETag = "\"0" + DigestUtils.md5DigestAsHex(TOKEN_DATA) + "\"";

            DefaultRequestAdapter request = new DefaultRequestAdapter(MethodType.GET);
            request.setHeader(HttpHeaders.IF_NONE_MATCH, "\"other-etag\", " + expectedETag + ", \"third-etag\"");
            DefaultResponseAdapter response = new DefaultResponseAdapter(null);
            response.setStatus(200);

            instantActivity.setRequestAdapter(request);
            instantActivity.setResponseAdapter(response);

            interceptor.intercept(translet);

            assertEquals(HttpStatus.NOT_MODIFIED.value(), response.getStatus());
            assertEquals(expectedETag, response.getHeader(HttpHeaders.ETAG));
            assertTrue(translet.isResponseReserved());

            return null;
        });
    }

    @Test
    void testInterceptIfNoneMatchWeakComparison() throws Exception {
        tester.perform(activity -> {
            InstantActivity instantActivity = (InstantActivity) activity;
            Translet translet = createTranslet(activity);
            String rawETag = "\"0" + DigestUtils.md5DigestAsHex(TOKEN_DATA) + "\"";

            // Client sends weak ETag, server generates strong ETag
            DefaultRequestAdapter request = new DefaultRequestAdapter(MethodType.GET);
            request.setHeader(HttpHeaders.IF_NONE_MATCH, "W/" + rawETag);
            DefaultResponseAdapter response = new DefaultResponseAdapter(null);
            response.setStatus(200);

            instantActivity.setRequestAdapter(request);
            instantActivity.setResponseAdapter(response);

            interceptor.intercept(translet);

            assertEquals(HttpStatus.NOT_MODIFIED.value(), response.getStatus());
            assertTrue(translet.isResponseReserved());

            return null;
        });
    }

}
