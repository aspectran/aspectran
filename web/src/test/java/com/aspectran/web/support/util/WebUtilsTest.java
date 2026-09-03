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

}
