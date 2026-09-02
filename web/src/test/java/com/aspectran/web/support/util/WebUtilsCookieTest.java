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
package com.aspectran.web.support.util;

import com.aspectran.core.adapter.AbstractRequestAdapter;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.web.support.http.Cookie;
import com.aspectran.web.support.http.HttpHeaders;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Test cases for cookie extraction and parsing in {@link WebUtils}.
 *
 * <p>Created: 2026-09-02</p>
 */
class WebUtilsCookieTest {

    @Test
    void testGetCookieFromHeader() {
        RequestAdapter requestAdapter = new AbstractRequestAdapter(MethodType.GET, null) {};
        requestAdapter.getHeaderMap().add(HttpHeaders.COOKIE, "theme=dark; lang=ko_KR; token=\"xyz=123\"");

        Cookie themeCookie = WebUtils.getCookie(requestAdapter, "theme");
        assertNotNull(themeCookie);
        assertEquals("theme", themeCookie.getName());
        assertEquals("dark", themeCookie.getValue());

        Cookie langCookie = WebUtils.getCookie(requestAdapter, "lang");
        assertNotNull(langCookie);
        assertEquals("lang", langCookie.getName());
        assertEquals("ko_KR", langCookie.getValue());

        // Quoted value stripped
        Cookie tokenCookie = WebUtils.getCookie(requestAdapter, "token");
        assertNotNull(tokenCookie);
        assertEquals("token", tokenCookie.getName());
        assertEquals("xyz=123", tokenCookie.getValue());

        // Non-existent cookie returns null
        Cookie missingCookie = WebUtils.getCookie(requestAdapter, "nonExistent");
        assertNull(missingCookie);
    }

    @Test
    void testGetCookiesEmpty() {
        RequestAdapter requestAdapter = new AbstractRequestAdapter(MethodType.GET, null) {};
        Cookie[] cookies = WebUtils.getCookies(requestAdapter);
        assertNull(cookies);
    }

}
