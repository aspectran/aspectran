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
package com.aspectran.thymeleaf.context.web;

import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.web.adapter.AbstractWebRequestAdapter;
import com.aspectran.web.adapter.WebRequestAdapter;
import com.aspectran.web.support.http.HttpHeaders;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test cases for {@link WebActivityRequest}.
 *
 * <p>Created: 2026-09-02</p>
 */
class WebActivityRequestTest {

    @Test
    void testWebActivityRequest() {
        WebRequestAdapter adapter = new AbstractWebRequestAdapter(MethodType.GET, null) {
            @Override
            public String getScheme() {
                return "https";
            }

            @Override
            public String getServerName() {
                return "example.com";
            }

            @Override
            public int getServerPort() {
                return 8443;
            }

            @Override
            public String getRequestURI() {
                return "/app/user/profile";
            }

            @Override
            public String getQueryString() {
                return "tab=settings";
            }

            @Override
            public String getContextPath() {
                return "/app";
            }

            @Override
            public void preparse() {
            }

            @Override
            public void preparse(WebRequestAdapter requestAdapter) {
            }
        };
        adapter.getHeaderMap().add(HttpHeaders.COOKIE, "theme=dark; sid=abc123");

        WebActivityRequest request = new WebActivityRequest(adapter);

        assertEquals("GET", request.getMethod());
        assertEquals("https", request.getScheme());
        assertEquals("example.com", request.getServerName());
        assertEquals(8443, request.getServerPort());
        assertEquals("/app", request.getApplicationPath());
        assertEquals("/user/profile", request.getPathWithinApplication());
        assertEquals("tab=settings", request.getQueryString());

        assertTrue(request.containsCookie("theme"));
        assertTrue(request.containsCookie("sid"));
        assertFalse(request.containsCookie("unknown"));
        assertEquals(2, request.getCookieCount());

        Set<String> cookieNames = request.getAllCookieNames();
        assertEquals(2, cookieNames.size());
        assertTrue(cookieNames.contains("theme"));
        assertTrue(cookieNames.contains("sid"));

        assertNotNull(request.getCookieValues("theme"));
        assertEquals("dark", request.getCookieValues("theme")[0]);
    }

}
