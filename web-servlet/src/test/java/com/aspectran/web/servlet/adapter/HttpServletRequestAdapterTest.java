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
package com.aspectran.web.servlet.adapter;

import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.test.web.servlet.mock.MockHttpServletRequest;
import com.aspectran.utils.MultiValueMap;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link HttpServletRequestAdapter}.
 *
 * <p>Created: 2026/09/30</p>
 */
class HttpServletRequestAdapterTest {

    @Test
    void testFastPathHeaderDelegation() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setHeader("Host", "localhost:8080");
        request.setHeader("User-Agent", "Aspectran-Test");

        HttpServletRequestAdapter adapter = new HttpServletRequestAdapter(MethodType.GET, request);

        // Fast-path calls
        assertTrue(adapter.containsHeader("Host"));
        assertTrue(adapter.containsHeader("User-Agent"));
        assertFalse(adapter.containsHeader("Authorization"));

        assertEquals("localhost:8080", adapter.getHeader("Host"));
        assertEquals("Aspectran-Test", adapter.getHeader("User-Agent"));
        assertNull(adapter.getHeader("Authorization"));

        List<String> hostValues = adapter.getHeaderValues("Host");
        assertNotNull(hostValues);
        assertEquals(1, hostValues.size());
        assertEquals("localhost:8080", hostValues.getFirst());
    }

    @Test
    void testLazyMaterializationAndModification() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setHeader("Host", "localhost:8080");

        HttpServletRequestAdapter adapter = new HttpServletRequestAdapter(MethodType.GET, request);

        // Fast-path read
        assertEquals("localhost:8080", adapter.getHeader("Host"));

        // Mutation triggers materialization
        adapter.setHeader("X-Custom", "custom-value");
        assertEquals("custom-value", adapter.getHeader("X-Custom"));
        assertEquals("localhost:8080", adapter.getHeader("Host"));

        MultiValueMap<String, String> headerMap = adapter.getHeaderMap();
        assertEquals(2, headerMap.size());
        assertEquals("localhost:8080", headerMap.getFirst("host"));
        assertEquals("custom-value", headerMap.getFirst("x-custom"));
    }

}
