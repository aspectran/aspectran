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
    void testParseForwardedFor() {
        assertNull(WebUtils.parseForwardedFor(null));
        assertNull(WebUtils.parseForwardedFor(""));
        assertEquals("203.0.113.195", WebUtils.parseForwardedFor("203.0.113.195"));
        assertEquals("203.0.113.195", WebUtils.parseForwardedFor("203.0.113.195, 70.41.3.18, 150.172.238.178"));
        assertEquals("203.0.113.195", WebUtils.parseForwardedFor("  203.0.113.195  , 70.41.3.18"));
    }

    @Test
    void testResolveRemoteAddr() {
        assertEquals("203.0.113.195", WebUtils.resolveRemoteAddr("203.0.113.195, 70.41.3.18", "127.0.0.1"));
        assertEquals("127.0.0.1", WebUtils.resolveRemoteAddr(null, "127.0.0.1"));
        assertEquals("127.0.0.1", WebUtils.resolveRemoteAddr("", "127.0.0.1"));
    }

    @Test
    void testGetRemoteAddrWithRequestAdapter() {
        AbstractWebRequestAdapter adapter = new AbstractWebRequestAdapter(MethodType.GET, null) {
            @Override
            protected String getActualRemoteAddr() {
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

        // With X-Forwarded-For header but proxyAddressForwarding disabled, fallback to getRemoteAddr()
        adapter.getHeaderMap().set(HttpHeaders.X_FORWARDED_FOR, "203.0.113.195, 70.41.3.18");
        assertEquals("192.168.1.100", WebUtils.getRemoteAddr(adapter));

        // With X-Forwarded-For header and proxyAddressForwarding enabled, prefers X-Forwarded-For
        adapter.setProxyAddressForwarding(true);
        assertEquals("203.0.113.195", WebUtils.getRemoteAddr(adapter));

        // Non-WebRequestAdapter returns null
        AbstractRequestAdapter nonWebAdapter = new AbstractRequestAdapter(MethodType.GET, null) {};
        assertNull(WebUtils.getRemoteAddr(nonWebAdapter));
    }

    @Test
    void testDetermineEffectiveContextPathWithRequestAdapter() {
        AbstractWebRequestAdapter adapter = new AbstractWebRequestAdapter(MethodType.GET, null) {
            @Override
            protected String getActualRemoteAddr() {
                return null;
            }

            @Override
            public String getContextPath() {
                return "/console";
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

        // Without X-Forwarded-Path header, fallback to getContextPath()
        assertEquals("/console", WebUtils.determineEffectiveContextPath(adapter));

        // With X-Forwarded-Path header but proxyAddressForwarding disabled, fallback to getContextPath()
        adapter.getHeaderMap().set(HttpHeaders.X_FORWARDED_PATH, "/ops/console");
        assertEquals("/console", WebUtils.determineEffectiveContextPath(adapter));

        // With X-Forwarded-Path header and proxyAddressForwarding enabled, prefers X-Forwarded-Path
        adapter.setProxyAddressForwarding(true);
        assertEquals("/ops/console", WebUtils.determineEffectiveContextPath(adapter));

        // Normalizes trailing slash
        adapter.getHeaderMap().set(HttpHeaders.X_FORWARDED_PATH, "/ops/console/");
        assertEquals("/ops/console", WebUtils.determineEffectiveContextPath(adapter));

        // Normalizes root "/" to empty string
        adapter.getHeaderMap().set(HttpHeaders.X_FORWARDED_PATH, "/");
        assertEquals("", WebUtils.determineEffectiveContextPath(adapter));

        // Non-WebRequestAdapter returns empty string
        AbstractRequestAdapter nonWebAdapter = new AbstractRequestAdapter(MethodType.GET, null) {};
        assertEquals("", WebUtils.determineEffectiveContextPath(nonWebAdapter));
    }

}
