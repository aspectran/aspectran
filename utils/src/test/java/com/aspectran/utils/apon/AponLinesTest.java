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
package com.aspectran.utils.apon;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test cases for AponLines.
 *
 * <p>Created: 2019. 12. 7.</p>
 */
class AponLinesTest {

    /**
     * Tests that two different ways of building APON lines produce the same raw string output.
     */
    @Test
    void testEquivalenceOfBuilderStylesForToString() {
        AponLines aponLines1 = new AponLines()
                .line("name", "value")
                .line("number", 9999)
                .line("text", "(")
                .line("|text-line-1")
                .line("|text-line-2")
                .line(")")
                .line("block", "{")
                .line("nested", "block")
                .line("}")
                .line("count: [")
                .line("1")
                .line("2")
                .line("3")
                .line("]")
                .line("array: [")
                .line("{")
                .line("block1-in-array", 1)
                .line("}")
                .line("{")
                .line("block2-in-array", 2)
                .line("}")
                .line("]")
                ;
        String apon1 = aponLines1.toString();

        AponLines aponLines2 = new AponLines()
                .line("name", "value")
                .line("number", 9999)
                .text("text")
                .line("text-line-1")
                .line("text-line-2")
                .end()
                .block("block")
                .line("nested", "block")
                .end()
                .array("count")
                .line("1")
                .line("2")
                .line("3")
                .end()
                .array("array")
                .block()
                .line("block1-in-array", 1)
                .end()
                .block()
                .line("block2-in-array", 2)
                .end()
                .end()
                ;
        String apon2 = aponLines2.toString();

        assertEquals(apon1, apon2);
    }

    /**
     * Tests that two different ways of building APON lines produce the same formatted string output.
     */
    @Test
    void testEquivalenceOfBuilderStylesForFormat() throws IOException {
        AponLines aponLines1 = new AponLines()
                .line("name", "value")
                .line("number", 9999)
                .line("text", "(")
                .line("|text-line-1")
                .line("|text-line-2")
                .line(")")
                .line("block", "{")
                .line("nested", "block")
                .line("}")
                .line("count: [")
                .line("1")
                .line("2")
                .line("3")
                .line("]")
                .line("array: [")
                .line("{")
                .line("block1-in-array", 1)
                .line("}")
                .line("{")
                .line("block2-in-array", 2)
                .line("}")
                .line("]")
                ;
        String apon1 = aponLines1.format();

        AponLines aponLines2 = new AponLines()
                .line("name", "value")
                .line("number", 9999)
                .text("text")
                .line("text-line-1")
                .line("text-line-2")
                .end()
                .block("block")
                .line("nested", "block")
                .end()
                .array("count")
                .line("1")
                .line("2")
                .line("3")
                .end()
                .array("array")
                .block()
                .line("block1-in-array", 1)
                .end()
                .block()
                .line("block2-in-array", 2)
                .end()
                .end()
                ;
        String apon2 = aponLines2.format();

        assertEquals(apon1, apon2);
    }

    /**
     * Tests the behavior of an empty AponLines object.
     */
    @Test
    void testEmptyApon() throws IOException {
        AponLines aponLines = new AponLines();
        assertEquals("", aponLines.toString());
        assertEquals("", aponLines.format());
    }

    /**
     * Tests building APON with simple key-value pairs.
     */
    @Test
    void testSimpleKeyValuePairs() throws IOException {
        AponLines aponLines = new AponLines()
                .line("name", "John Doe")
                .line("age", 30)
                .line("isStudent", false);

        String expectedString = "name: John Doe\n" +
                "age: 30\n" +
                "isStudent: false\n";
        String expectedFormat = "name: John Doe\n" +
                "age: 30\n" +
                "isStudent: false\n";

        assertEquals(expectedString, aponLines.toString());
        assertEquals(expectedFormat, aponLines.format());
    }

    /**
     * Tests nested blocks and arrays.
     */
    @Test
    void testNestedBlocksAndArrays() throws IOException {
        AponLines aponLines = new AponLines()
                .block("user")
                .line("username", "tester")
                .block("profile")
                .line("email", "tester@example.com")
                .end()
                .array("roles")
                .line("admin")
                .line("editor")
                .end()
                .end();

        String expected = "user: {\n" +
                "  username: tester\n" +
                "  profile: {\n" +
                "    email: tester@example.com\n" +
                "  }\n" +
                "  roles: [\n" +
                "    admin\n" +
                "    editor\n" +
                "  ]\n" +
                "}\n";

        assertEquals(expected, aponLines.format());
    }

    /**
     * Tests values with special characters that might require escaping or special handling.
     */
    @Test
    void testSpecialCharacters() throws IOException {
        AponLines aponLines = new AponLines()
                .line("specialChars", "value with { } [ ] : ( ) ' \"")
                .line("multiline", "line1\nline2");

        String expectedFormat = "specialChars: \"value with { } [ ] : ( ) ' \\\"\"\n" +
                "multiline: (\n" +
                "  |line1\n" +
                "  |line2\n" +
                ")\n";

        assertEquals(expectedFormat, aponLines.format());
    }

}
