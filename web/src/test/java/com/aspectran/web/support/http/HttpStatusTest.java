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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test cases for {@link HttpStatus}.
 */
class HttpStatusTest {

    @Test
    void testResolve() {
        assertSame(HttpStatus.OK, HttpStatus.resolve(200));
        assertSame(HttpStatus.CREATED, HttpStatus.resolve(201));
        assertSame(HttpStatus.NO_CONTENT, HttpStatus.resolve(204));
        assertSame(HttpStatus.MOVED_PERMANENTLY, HttpStatus.resolve(301));
        assertSame(HttpStatus.FOUND, HttpStatus.resolve(302));
        assertSame(HttpStatus.NOT_MODIFIED, HttpStatus.resolve(304));
        assertSame(HttpStatus.BAD_REQUEST, HttpStatus.resolve(400));
        assertSame(HttpStatus.UNAUTHORIZED, HttpStatus.resolve(401));
        assertSame(HttpStatus.FORBIDDEN, HttpStatus.resolve(403));
        assertSame(HttpStatus.NOT_FOUND, HttpStatus.resolve(404));
        assertSame(HttpStatus.UNPROCESSABLE_ENTITY, HttpStatus.resolve(422));
        assertSame(HttpStatus.TOO_MANY_REQUESTS, HttpStatus.resolve(429));
        assertSame(HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.resolve(500));
        assertSame(HttpStatus.BAD_GATEWAY, HttpStatus.resolve(502));
        assertSame(HttpStatus.SERVICE_UNAVAILABLE, HttpStatus.resolve(503));
        assertSame(HttpStatus.GATEWAY_TIMEOUT, HttpStatus.resolve(504));

        assertNull(HttpStatus.resolve(999));
        assertNull(HttpStatus.resolve(-1));
    }

    @Test
    void testValueOf() {
        assertSame(HttpStatus.OK, HttpStatus.valueOf(200));
        assertSame(HttpStatus.NOT_FOUND, HttpStatus.valueOf(404));

        assertThrows(IllegalArgumentException.class, () -> HttpStatus.valueOf(999));
    }

    @Test
    void testSeries() {
        assertEquals(HttpStatus.Series.INFORMATIONAL, HttpStatus.CONTINUE.getSeries());
        assertEquals(HttpStatus.Series.SUCCESSFUL, HttpStatus.OK.getSeries());
        assertEquals(HttpStatus.Series.REDIRECTION, HttpStatus.MOVED_PERMANENTLY.getSeries());
        assertEquals(HttpStatus.Series.CLIENT_ERROR, HttpStatus.NOT_FOUND.getSeries());
        assertEquals(HttpStatus.Series.SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR.getSeries());

        assertSame(HttpStatus.Series.INFORMATIONAL, HttpStatus.Series.resolve(100));
        assertSame(HttpStatus.Series.SUCCESSFUL, HttpStatus.Series.resolve(204));
        assertSame(HttpStatus.Series.REDIRECTION, HttpStatus.Series.resolve(302));
        assertSame(HttpStatus.Series.CLIENT_ERROR, HttpStatus.Series.resolve(404));
        assertSame(HttpStatus.Series.SERVER_ERROR, HttpStatus.Series.resolve(500));
        assertNull(HttpStatus.Series.resolve(999));
    }

    @Test
    void testSeriesForStatus() {
        assertSame(HttpStatus.Series.SUCCESSFUL, HttpStatus.Series.forStatus(200));
        assertThrows(IllegalArgumentException.class, () -> HttpStatus.Series.forStatus(999));
    }

    @Test
    void testStatusPredicates() {
        assertTrue(HttpStatus.CONTINUE.is1xxInformational());
        assertFalse(HttpStatus.CONTINUE.is2xxSuccessful());
        assertFalse(HttpStatus.CONTINUE.isError());

        assertTrue(HttpStatus.OK.is2xxSuccessful());
        assertFalse(HttpStatus.OK.is3xxRedirection());
        assertFalse(HttpStatus.OK.isError());

        assertTrue(HttpStatus.MOVED_PERMANENTLY.is3xxRedirection());
        assertFalse(HttpStatus.MOVED_PERMANENTLY.is4xxClientError());
        assertFalse(HttpStatus.MOVED_PERMANENTLY.isError());

        assertTrue(HttpStatus.BAD_REQUEST.is4xxClientError());
        assertTrue(HttpStatus.BAD_REQUEST.isError());
        assertFalse(HttpStatus.BAD_REQUEST.is5xxServerError());

        assertTrue(HttpStatus.INTERNAL_SERVER_ERROR.is5xxServerError());
        assertTrue(HttpStatus.INTERNAL_SERVER_ERROR.isError());
        assertFalse(HttpStatus.INTERNAL_SERVER_ERROR.is4xxClientError());
    }

    @Test
    void testToString() {
        assertEquals("200 OK", HttpStatus.OK.toString());
        assertEquals("404 NOT_FOUND", HttpStatus.NOT_FOUND.toString());
    }

}
