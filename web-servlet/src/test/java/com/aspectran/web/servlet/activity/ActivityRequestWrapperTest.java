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
package com.aspectran.web.servlet.activity;

import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.test.web.servlet.mock.MockHttpServletRequest;
import com.aspectran.web.servlet.adapter.HttpServletRequestAdapter;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test cases for {@link ActivityRequestWrapper}.
 */
class ActivityRequestWrapperTest {

    @Test
    void testWrapperDelegation() {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setHeader("X-Custom-Header", "custom-value");
        mockRequest.setParameter("param1", "value1");

        HttpServletRequestAdapter adapter = new HttpServletRequestAdapter(MethodType.GET, mockRequest);
        adapter.preparse();

        ActivityRequestWrapper wrapper = new ActivityRequestWrapper(adapter);

        // Header tests
        assertEquals("custom-value", wrapper.getHeader("X-Custom-Header"));
        assertNull(wrapper.getHeader("Non-Existent"));
        Enumeration<String> headers = wrapper.getHeaders("X-Custom-Header");
        assertTrue(headers.hasMoreElements());
        assertEquals("custom-value", headers.nextElement());
        assertFalse(headers.hasMoreElements());

        Enumeration<String> headerNames = wrapper.getHeaderNames();
        assertTrue(Collections.list(headerNames).contains("X-Custom-Header"));

        // Parameter tests
        assertEquals("value1", wrapper.getParameter("param1"));
        assertNull(wrapper.getParameter("nonExistent"));
        assertEquals("value1", wrapper.getParameterValues("param1")[0]);
        assertTrue(wrapper.getParameterMap().containsKey("param1"));

        // Attribute tests
        wrapper.setAttribute("testAttr", "testVal");
        assertEquals("testVal", wrapper.getAttribute("testAttr"));
        assertTrue(Collections.list(wrapper.getAttributeNames()).contains("testAttr"));
        wrapper.removeAttribute("testAttr");
        assertNull(wrapper.getAttribute("testAttr"));

        // Locale tests
        assertNotNull(wrapper.getLocale());
        assertNotNull(wrapper.getLocales());
        adapter.setLocale(Locale.KOREAN);
        assertEquals(Locale.KOREAN, wrapper.getLocale());
        Enumeration<Locale> locales = wrapper.getLocales();
        assertTrue(locales.hasMoreElements());
        assertEquals(Locale.KOREAN, locales.nextElement());
    }

}
