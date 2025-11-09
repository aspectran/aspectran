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
                .raw("|text-line-1")
                .raw("|text-line-2")
                .raw(")")
                .line("block", "{")
                .line("nested", "block")
                .raw("}")
                .raw("count: [")
                .raw("1")
                .raw("2")
                .raw("3")
                .raw("]")
                .raw("array: [")
                .raw("{")
                .line("block1-in-array", 1)
                .raw("}")
                .raw("{")
                .line("block2-in-array", 2)
                .raw("}")
                .raw("]")
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
                .raw("|text-line-1")
                .raw("|text-line-2")
                .raw(")")
                .line("block", "{")
                .line("nested", "block")
                .raw("}")
                .raw("count: [")
                .raw("1")
                .raw("2")
                .raw("3")
                .raw("]")
                .raw("array: [")
                .raw("{")
                .raw("block1-in-array: 1")
                .raw("}")
                .raw("{")
                .raw("block2-in-array: 2")
                .raw("}")
                .raw("]")
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

        String expectedString = """
                name: John Doe
                age: 30
                isStudent: false
                """;
        String expectedFormat = """
                name: John Doe
                age: 30
                isStudent: false
                """;

        assertEquals(expectedString, aponLines.toString().replace("\r\n", "\n"));
        assertEquals(expectedFormat, aponLines.format().replace("\r\n", "\n"));
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
                .array("groups")
                .block()
                .line("user1", "tester1")
                .line("user2", "tester2")
                .end()
                .block()
                .line("user3", "tester3")
                .line("user4",  "tester4")
                .end()
                .end()
                .end();

        String expected = """
                user: {
                  username: tester
                  profile: {
                    email: tester@example.com
                  }
                  roles: [
                    admin
                    editor
                  ]
                  groups: [
                    {
                      user1: tester1
                      user2: tester2
                    }
                    {
                      user3: tester3
                      user4: tester4
                    }
                  ]
                }
                """;

        assertEquals(expected.replace("\r\n", "\n"), aponLines.format().replace("\r\n", "\n"));
    }

    /**
     * Tests values with special characters that might require escaping or special handling.
     */
    @Test
    void testSpecialCharacters() throws IOException {
        AponLines aponLines = new AponLines()
                .line("specialChars", "value with { } [ ] : ( ) ' \"")
                .line("multiline", "line1\nline2");

        String expectedFormat = """
                specialChars: "value with { } [ ] : ( ) ' \\""
                multiline: (
                  |line1
                  |line2
                )
                """;

        assertEquals(expectedFormat.replace("\r\n", "\n"), aponLines.format().replace("\r\n", "\n"));
    }

}
