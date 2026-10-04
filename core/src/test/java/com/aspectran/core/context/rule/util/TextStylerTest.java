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
package com.aspectran.core.context.rule.util;

import com.aspectran.core.context.rule.type.TextStyleType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test case for {@link TextStyler}.
 */
class TextStylerTest {

    @Test
    void testNullAndEmpty() {
        assertNull(TextStyler.styling(null, (String) null));
        assertNull(TextStyler.styling(null, (TextStyleType) null));
        assertNull(TextStyler.styling(null, "compact"));
        assertNull(TextStyler.styling(null, TextStyleType.COMPACT));
        assertEquals("", TextStyler.styling("", "compact"));
        assertEquals("", TextStyler.styling("", TextStyleType.COMPACT));
        assertEquals("hello", TextStyler.styling("hello", (TextStyleType) null));
    }

    @Test
    void testUnknownStyle() {
        assertThrows(IllegalArgumentException.class, () -> TextStyler.styling("hello", "unknown"));
    }

    @Test
    void testStripAponStyle() {
        String aponText = "  | line 1\n  | line 2\n  | line 3";
        String expected = " line 1" + System.lineSeparator() + " line 2" + System.lineSeparator() + " line 3";
        assertEquals(expected, TextStyler.stripAponStyle(aponText));
        assertEquals(expected, TextStyler.styling(aponText, "apon"));
        assertEquals(expected, TextStyler.styling(aponText, TextStyleType.APON));

        // APON text with CRLF
        String aponCrlf = "  | line 1\r\n  | line 2";
        String expectedCrlf = " line 1" + System.lineSeparator() + " line 2";
        assertEquals(expectedCrlf, TextStyler.stripAponStyle(aponCrlf));

        // Plain text fallback (no '|' at line start)
        String plain = "   hello world   ";
        assertEquals("hello world", TextStyler.stripAponStyle(plain));

        // Plain text with pipe in the middle
        String plainWithPipe = "  hello | world  ";
        assertEquals("hello | world", TextStyler.stripAponStyle(plainWithPipe));
    }

    @Test
    void testCompact() {
        String input = "\n\n   line 1   \n\n   \n   line 2   \n\n";
        String expected = "line 1" + System.lineSeparator() + "line 2";
        assertEquals(expected, TextStyler.compact(input));
        assertEquals(expected, TextStyler.styling(input, "compact"));
        assertEquals(expected, TextStyler.styling(input, TextStyleType.COMPACT));

        // CRLF input
        String inputCrlf = "\r\n   line 1   \r\n   \r\n   line 2   \r\n";
        assertEquals(expected, TextStyler.compact(inputCrlf));
    }

    @Test
    void testCompress() {
        String input = "\n\n   line 1   \n\n   \n   line 2   \n\n";
        String expected = "line 1line 2";
        assertEquals(expected, TextStyler.compress(input));
        assertEquals(expected, TextStyler.styling(input, "compressed"));
        assertEquals(expected, TextStyler.styling(input, TextStyleType.COMPRESSED));

        String inputCrlf = "\r\n   line 1   \r\n   line 2   \r\n";
        assertEquals(expected, TextStyler.compress(inputCrlf));
    }

}
