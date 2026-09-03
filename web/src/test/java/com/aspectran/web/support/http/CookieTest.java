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
package com.aspectran.web.support.http;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test cases for {@link Cookie}.
 *
 * <p>Created: 2026-09-02</p>
 */
class CookieTest {

    @Test
    void testCookieBasic() {
        Cookie cookie = new Cookie("sessionId", "abc123xyz");
        assertEquals("sessionId", cookie.getName());
        assertEquals("abc123xyz", cookie.getValue());

        cookie.setValue("newVal");
        assertEquals("newVal", cookie.getValue());

        cookie.setPath("/app");
        assertEquals("/app", cookie.getPath());

        cookie.setDomain("example.com");
        assertEquals("example.com", cookie.getDomain());

        cookie.setMaxAge(3600);
        assertEquals(3600, cookie.getMaxAge());

        cookie.setSecure(true);
        assertTrue(cookie.isSecure());

        cookie.setHttpOnly(true);
        assertTrue(cookie.isHttpOnly());

        cookie.setSameSite("Lax");
        assertEquals("Lax", cookie.getSameSite());

        assertEquals("sessionId=newVal", cookie.toString());
    }

    @Test
    void testCookieEqualsAndHashCode() {
        Cookie c1 = new Cookie("user", "alice");
        c1.setPath("/test");
        c1.setDomain("example.com");

        Cookie c2 = new Cookie("user", "bob");
        c2.setPath("/test");
        c2.setDomain("example.com");

        // Equals compares name, path, domain
        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());

        Cookie c3 = new Cookie("other", "alice");
        assertNotEquals(c1, c3);
    }

    @Test
    void testToHeaderValue() {
        Cookie cookie = new Cookie("authToken", "secret123");
        cookie.setPath("/admin");
        cookie.setDomain("example.com");
        cookie.setMaxAge(1800);
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        cookie.setSameSite("Strict");

        assertEquals("authToken=secret123; Domain=example.com; Path=/admin; Max-Age=1800; Secure; HttpOnly; SameSite=Strict",
                cookie.toHeaderValue());
    }

    @Test
    void testBuilder() {
        Cookie cookie = Cookie.builder("session", "xyz")
                .path("/api")
                .domain("example.org")
                .maxAge(3600)
                .secure(true)
                .httpOnly(true)
                .sameSite("Lax")
                .build();

        assertEquals("session", cookie.getName());
        assertEquals("xyz", cookie.getValue());
        assertEquals("/api", cookie.getPath());
        assertEquals("example.org", cookie.getDomain());
        assertEquals(3600, cookie.getMaxAge());
        assertTrue(cookie.isSecure());
        assertTrue(cookie.isHttpOnly());
        assertEquals("Lax", cookie.getSameSite());
        assertEquals("session=xyz; Domain=example.org; Path=/api; Max-Age=3600; Secure; HttpOnly; SameSite=Lax",
                cookie.toHeaderValue());
    }

}
