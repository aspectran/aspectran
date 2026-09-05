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
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.web.adapter.AbstractWebRequestAdapter;
import com.aspectran.web.support.http.HttpHeaders;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Test cases for {@link WebUtils}.
 */
class WebUtilsTest {

    @Test
    void testParseReverseContextPath() {
        assertNull(WebUtils.parseReverseContextPath(null));
        assertEquals("", WebUtils.parseReverseContextPath("/"));
        assertEquals("/myapp", WebUtils.parseReverseContextPath("/myapp/"));
        assertEquals("/myapp", WebUtils.parseReverseContextPath("/myapp"));
        assertEquals("/myapp/sub", WebUtils.parseReverseContextPath("/myapp/sub/"));
    }

    @Test
    void testGetReverseContextPath() {
        assertEquals("/default", WebUtils.parseReverseContextPath(null, "/default"));
        assertEquals("/default", WebUtils.getReverseContextPath((String) null, "/default"));
        assertEquals("", WebUtils.getReverseContextPath("/", "/default"));
        assertEquals("/myapp", WebUtils.getReverseContextPath("/myapp/", "/default"));
        assertEquals("/myapp", WebUtils.getReverseContextPath("/myapp", "/default"));
    }

    @Test
    void testParseRemoteAddr() {
        assertNull(WebUtils.parseRemoteAddr(null));
        assertNull(WebUtils.parseRemoteAddr(""));
        assertEquals("203.0.113.195", WebUtils.parseRemoteAddr("203.0.113.195"));
        assertEquals("203.0.113.195", WebUtils.parseRemoteAddr("203.0.113.195, 70.41.3.18, 150.172.238.178"));
        assertEquals("203.0.113.195", WebUtils.parseRemoteAddr("  203.0.113.195  , 70.41.3.18"));
    }

    @Test
    void testGetRemoteAddr() {
        assertEquals("203.0.113.195", WebUtils.getRemoteAddr("203.0.113.195, 70.41.3.18", "127.0.0.1"));
        assertEquals("127.0.0.1", WebUtils.getRemoteAddr(null, "127.0.0.1"));
        assertEquals("127.0.0.1", WebUtils.getRemoteAddr("", "127.0.0.1"));
    }

    @Test
    void testGetRemoteAddrWithRequestAdapter() {
        AbstractWebRequestAdapter adapter = new AbstractWebRequestAdapter(MethodType.GET, null) {
            @Override
            public String getRemoteAddr() {
                return "192.168.1.100";
            }

            @Override
            public String getContextPath() {
                return null;
            }

            @Override
            public String getRequestURI() {
                return null;
            }

            @Override
            public String getQueryString() {
                return null;
            }

            @Override
            public void preparse() {
            }
        };

        // Without X-Forwarded-For header, fallback to getRemoteAddr()
        assertEquals("192.168.1.100", WebUtils.getRemoteAddr(adapter));

        // With empty X-Forwarded-For header, fallback to getRemoteAddr()
        adapter.getHeaderMap().set(HttpHeaders.X_FORWARDED_FOR, "");
        assertEquals("192.168.1.100", WebUtils.getRemoteAddr(adapter));

        // With X-Forwarded-For header, prefers X-Forwarded-For
        adapter.getHeaderMap().set(HttpHeaders.X_FORWARDED_FOR, "203.0.113.195, 70.41.3.18");
        assertEquals("203.0.113.195", WebUtils.getRemoteAddr(adapter));

        // Non-WebRequestAdapter returns null
        AbstractRequestAdapter nonWebAdapter = new AbstractRequestAdapter(MethodType.GET, null) {};
        assertNull(WebUtils.getRemoteAddr(nonWebAdapter));
    }

}
