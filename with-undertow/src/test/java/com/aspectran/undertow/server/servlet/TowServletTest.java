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
package com.aspectran.undertow.server.servlet;

import com.aspectran.web.servlet.WebActivityServlet;
import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Test case for {@link TowServlet}.
 */
class TowServletTest {

    @Test
    void testAutoDetectMultipartConfigOnWebActivityServlet() throws Exception {
        TowServlet servlet = new TowServlet("webActivityServlet", WebActivityServlet.class.getName());
        MultipartConfigElement multipartConfig = servlet.getMultipartConfig();
        assertNotNull(multipartConfig, "MultipartConfigElement should be auto-configured for WebActivityServlet");
        assertEquals("", multipartConfig.getLocation());
        assertEquals(-1L, multipartConfig.getMaxFileSize());
        assertEquals(-1L, multipartConfig.getMaxRequestSize());
        assertEquals(0, multipartConfig.getFileSizeThreshold());
    }

    @Test
    void testNoMultipartConfigOnPlainServlet() {
        TowServlet servlet = new TowServlet("plainServlet", PlainServlet.class);
        assertNull(servlet.getMultipartConfig(), "MultipartConfigElement should be null for plain servlet");
    }

    @Test
    void testCustomMultipartConfig() {
        TowServlet servlet = new TowServlet("customMultipartServlet", CustomMultipartServlet.class);
        MultipartConfigElement multipartConfig = servlet.getMultipartConfig();
        assertNotNull(multipartConfig);
        assertEquals("/tmp", multipartConfig.getLocation());
        assertEquals(1024L, multipartConfig.getMaxFileSize());
        assertEquals(2048L, multipartConfig.getMaxRequestSize());
        assertEquals(512, multipartConfig.getFileSizeThreshold());
    }

    private static class PlainServlet extends HttpServlet {
    }

    @MultipartConfig(location = "/tmp", maxFileSize = 1024L, maxRequestSize = 2048L, fileSizeThreshold = 512)
    private static class CustomMultipartServlet extends HttpServlet {
    }

}
