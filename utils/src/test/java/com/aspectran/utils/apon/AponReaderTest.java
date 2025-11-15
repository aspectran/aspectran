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

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test cases for AponReader.
 *
 * <p>Created: 2020/05/30</p>
 */
class AponReaderTest {

    /**
     * Tests reading a value containing an escaped unicode character.
     */
    @Test
    void testReadWithEscapedUnicodeCharacter() throws AponParseException {
        String input = "name: \"she\\u2019s \"";
        AponReader reader = new AponReader(input);
        Parameters parameters = reader.read();
        assertEquals("she’s ", parameters.getString("name"));
    }

    /**
     * Tests reading a value containing a raw (unescaped) unicode character.
     */
    @Test
    void testReadWithUnescapedUnicodeCharacter() throws AponParseException {
        String input = "name: she\u2019s";
        AponReader reader = new AponReader(input);
        Parameters parameters = reader.read();
        assertEquals("she\u2019s", parameters.getString("name"));
    }

    /**
     * Tests parsing of various primitive data types and null.
     */
    @Test
    void testParseValueTypes() throws AponParseException {
        String input = """
            string: Hello World
            integer: 123
            long: 2147483648
            float(float): 78.9
            double: 0.1000000000000000055511151231257827021181583404541015625
            boolean: true
            nullValue: null
            """;
        Parameters params = AponReader.read(input);
        assertEquals("Hello World", params.getString("string"));
        assertEquals(123, params.getInt("integer"));
        assertEquals(2147483648L, params.getLong("long"));
        assertEquals(78.9f, params.getFloat("float"));
        assertEquals(0.1000000000000000055511151231257827021181583404541015625d, params.getDouble("double"));
        assertTrue(params.getBoolean("boolean"));
        assertNull(params.getString("nullValue"));
    }

    /**
     * Tests parsing of a multi-line text block.
     */
    @Test
    void testParseTextBlock() throws AponParseException {
        String input = """
            message: (
              |Line 1
              |Line 2
              |  Indented Line 3
            )
            """;
        String expected = """
                Line 1
                Line 2
                  Indented Line 3""";
        Parameters params = AponReader.read(input);
        assertEquals(expected.replace("\r\n", "\n"), params.getString("message").replace("\r\n", "\n"));
    }

    /**
     * Tests parsing of a nested structure with blocks and arrays.
     */
    @Test
    void testParseNestedStructure() throws AponParseException {
        String input = """
            config: {
              name: App1
              settings: {
                enabled: true
                retries: 3
              }
              users: [
                {
                    name: Alice
                    role: admin
                }
                {
                    name: Bob
                    role: user
                }
              ]
            }
            """;
        Parameters config = AponReader.read(input).getParameters("config");
        assertEquals("App1", config.getString("name"));
        assertEquals(true, config.getParameters("settings").getBoolean("enabled"));
        assertEquals(2, config.getParametersList("users").size());
        assertEquals("Alice", config.getParametersList("users").get(0).getString("name"));
    }

    /**
     * Tests that comments are properly ignored by the parser.
     */
    @Test
    void testParseWithComments() throws AponParseException {
        String input = """
            # This is a full-line comment
            key: value
            # Another comment
            anotherKey: anotherValue
            """;
        Parameters params = AponReader.read(input);
        assertEquals("value", params.getString("key"));
        assertEquals("anotherValue", params.getString("anotherKey"));
        assertEquals(2, params.size());
    }

    /**
     * Tests that invalid syntax correctly throws AponParseException.
     */
    @Test
    void testInvalidSyntaxThrowsException() {
        assertThrows(AponParseException.class, () -> AponReader.read("key value"));    // missing colon
    }

    /**
     * Tests parsing of empty and whitespace-only input.
     */
    @Test
    void testEmptyAndWhitespaceInput() throws AponParseException {
        Parameters p1 = AponReader.read("");
        assertTrue(p1.isEmpty());

        Parameters p2 = AponReader.read("   \n\t\r\n   ");
        assertTrue(p2.isEmpty());
    }

    @Test
    void testParseSingleLineEmptyStructures() throws AponParseException {
        String input = """
            emptyBlock: {}
            arrayWithEmpty: [
                []
                []
            ]
            """;
        Parameters params = AponReader.read(input);

        Parameters emptyBlock = params.getParameters("emptyBlock");
        assertNotNull(emptyBlock);
        assertTrue(emptyBlock.isEmpty());

        List<Parameters> list = params.getParametersList("arrayWithEmpty");
        assertNotNull(list);
        assertEquals(2, list.size());
        assertTrue(list.get(0).isEmpty());
        assertTrue(list.get(1).isEmpty());
    }

    @Test
    void testParseMultiDimensionalStringArray() throws AponParseException {
        String input = """
            matrix: [
              [
                a
                b
              ]
              [
                 c
                 d
                 e
              ]
            ]
            """;

        Parameters params = AponReader.read(input);
        assertNotNull(params);

        // AponReader does not parse into List<List<String>>.
        // Instead, it parses into a List of Parameters objects.
        @SuppressWarnings("unchecked")
        List<List<String>> matrix = (List<List<String>>)params.getValueList("matrix");
        assertNotNull(matrix);
        assertEquals(2, matrix.size());

        // Check the first inner array, which is parsed as a Parameters object
        List<String> row1 = matrix.get(0);
        assertNotNull(row1);
        assertEquals(Arrays.asList("a", "b"), row1);

        // Check the second inner array
        List<String> row2 = matrix.get(1);
        assertNotNull(row2);
        assertEquals(Arrays.asList("c", "d", "e"), row2);
    }

    @Test
    void testParseBracedRoot() throws AponParseException {
        String apon = """
            {
              name: John Doe
              age: 30
            }
            """;
        Parameters params = AponReader.read(apon);

        assertFalse(params.isCompactStyle());
        assertEquals("John Doe", params.getString("name"));
        assertEquals(30, params.getInt("age"));
    }

    @Test
    void testParseNonBracedRootAndCompactStyle() throws AponParseException {
        String apon = """
            name: John Doe
            age: 30
            """;
        Parameters params = AponReader.read(apon);

        assertTrue(params.isCompactStyle());
        assertEquals("John Doe", params.getString("name"));
        assertEquals(30, params.getInt("age"));
    }

    @Test
    void testErrorHandlingUnclosedBracedRoot() {
        String apon = """
            {
              name: John Doe
            """; // Missing closing brace
        AponParseException e = assertThrows(AponParseException.class, () -> AponReader.read(apon));
        assertTrue(e.getMessage().contains("no closing curly bracket"));
    }

    @Test
    void testErrorHandlingTrailingContentAfterBracedRoot() {
        String apon = """
            {
              name: John Doe
            }
            extra: content
            """;
        AponParseException e = assertThrows(AponParseException.class, () -> AponReader.read(apon));
        assertTrue(e.getMessage().contains("Unexpected content after closing brace"));
    }

}
