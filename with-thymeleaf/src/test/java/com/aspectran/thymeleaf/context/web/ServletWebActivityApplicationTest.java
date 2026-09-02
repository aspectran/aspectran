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

import jakarta.servlet.ServletContext;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test cases for {@link ServletWebActivityApplication}.
 *
 * <p>Created: 2026-09-02</p>
 */
class ServletWebActivityApplicationTest {

    @Test
    void testServletContextAttributes() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("appTitle", "Aspectran Web Application");

        ServletContext servletContext = (ServletContext) Proxy.newProxyInstance(
                ServletContext.class.getClassLoader(),
                new Class<?>[] { ServletContext.class },
                (proxy, method, args) -> switch (method.getName()) {
                    case "getAttribute" -> attributes.get(args[0]);
                    case "setAttribute" -> {
                        attributes.put((String) args[0], args[1]);
                        yield null;
                    }
                    case "removeAttribute" -> {
                        attributes.remove(args[0]);
                        yield null;
                    }
                    case "getAttributeNames" -> Collections.enumeration(attributes.keySet());
                    default -> null;
                }
        );

        ServletWebActivityApplication app = new ServletWebActivityApplication(servletContext);
        assertEquals(1, app.getAttributeCount());
        assertTrue(app.containsAttribute("appTitle"));
        assertEquals("Aspectran Web Application", app.getAttributeValue("appTitle"));
        assertSame(servletContext, app.getNativeServletContextObject());

        app.setAttributeValue("version", "9.7.0");
        assertEquals(2, app.getAttributeCount());
        assertEquals("9.7.0", app.getAttributeValue("version"));

        app.removeAttribute("appTitle");
        assertEquals(1, app.getAttributeCount());
    }

}
