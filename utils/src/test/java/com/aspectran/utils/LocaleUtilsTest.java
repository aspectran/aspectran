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
package com.aspectran.utils;

import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test case for {@link LocaleUtils}.
 *
 * <p>Created: 2026. 10. 1.</p>
 */
class LocaleUtilsTest {

    @Test
    void testParseLocale() {
        assertEquals(Locale.KOREAN, LocaleUtils.parseLocale("ko"));
        assertEquals(Locale.KOREA, LocaleUtils.parseLocale("ko_KR"));
        assertEquals(Locale.KOREA, LocaleUtils.parseLocale("ko-KR"));
        assertEquals(Locale.US, LocaleUtils.parseLocale("en_US"));
        assertEquals(Locale.US, LocaleUtils.parseLocale("en US"));
        assertEquals(Locale.of("en", "US", "POSIX"), LocaleUtils.parseLocale("en_US_POSIX"));
        assertNull(LocaleUtils.parseLocale(""));
        assertNull(LocaleUtils.parseLocale(null));
    }

    @Test
    void testParseLocaleString() {
        assertEquals(Locale.ENGLISH, LocaleUtils.parseLocaleString("en"));
        assertEquals(Locale.US, LocaleUtils.parseLocaleString("en_US"));
        assertEquals(Locale.of("de", "DE", "EURO"), LocaleUtils.parseLocaleString("de_DE_EURO"));
        assertNull(LocaleUtils.parseLocaleString(""));
        assertNull(LocaleUtils.parseLocaleString(null));
    }

    @Test
    void testParseTimeZoneString() {
        TimeZone tz = LocaleUtils.parseTimeZoneString("Asia/Seoul");
        assertNotNull(tz);
        assertEquals("Asia/Seoul", tz.getID());

        TimeZone gmt = LocaleUtils.parseTimeZoneString("GMT+9");
        assertNotNull(gmt);

        assertThrows(IllegalArgumentException.class, () ->
                LocaleUtils.parseTimeZoneString("Invalid_TimeZone"));
        assertThrows(IllegalArgumentException.class, () ->
                LocaleUtils.parseTimeZoneString(""));
        assertThrows(IllegalArgumentException.class, () ->
                LocaleUtils.parseTimeZoneString(null));
    }

}
