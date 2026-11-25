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
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
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

    @Test
    void testLazyHttpSession() {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        HttpServletRequestAdapter adapter = new HttpServletRequestAdapter(MethodType.GET, mockRequest);
        adapter.preparse();

        ActivityRequestWrapper wrapper = new ActivityRequestWrapper(adapter);

        // 1. getSession(false) should return null without creating a session
        assertNull(wrapper.getSession(false));
        assertNull(mockRequest.getSession(false));

        // 2. getSession() or getSession(true) should return LazyHttpSession without creating an underlying session
        HttpSession session = wrapper.getSession();
        assertNotNull(session);
        assertInstanceOf(LazyHttpSession.class, session);
        assertNull(mockRequest.getSession(false)); // Underlying session is NOT created yet!

        // 3. Read operations on LazyHttpSession do NOT create underlying session
        assertNull(session.getAttribute("user"));
        assertFalse(session.getAttributeNames().hasMoreElements());
        assertNull(session.getId());
        assertEquals(0, session.getMaxInactiveInterval());
        assertNull(mockRequest.getSession(false)); // Still null!

        // 4. State-changing operation (setAttribute) triggers real session creation
        session.setAttribute("user", "aspectran-user");
        assertNotNull(mockRequest.getSession(false)); // Real session is now created!
        assertEquals("aspectran-user", session.getAttribute("user"));
        assertNotNull(session.getId());

        // 5. Subsequent wrapper.getSession() returns the real session
        HttpSession realSession = wrapper.getSession();
        assertNotNull(realSession);
        assertEquals("aspectran-user", realSession.getAttribute("user"));
    }

    @Test
    void testExistingSessionDelegation() {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        HttpSession initialSession = mockRequest.getSession(true);
        initialSession.setAttribute("existingKey", "existingVal");

        HttpServletRequestAdapter adapter = new HttpServletRequestAdapter(MethodType.GET, mockRequest);
        adapter.preparse();

        ActivityRequestWrapper wrapper = new ActivityRequestWrapper(adapter);

        // When a session already exists, getSession returns that session directly
        HttpSession session = wrapper.getSession();
        assertEquals(initialSession, session);
        assertEquals("existingVal", session.getAttribute("existingKey"));
    }

}
