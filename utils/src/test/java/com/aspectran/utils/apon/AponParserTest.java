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

import static com.aspectran.utils.apon.AponFormat.SYSTEM_NEW_LINE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test cases for AponParser.
 *
 * <p>Created: 2025-11-13</p>
 */
class AponParserTest {

    /**
     * Tests parsing of a basic APON object with scalar values.
     */
    @Test
    void testParseBasicObject() throws AponParseException {
        String apon = """
            name: John Doe
            age: 30
            isActive: true
            balance: 123.45
            id: 1234567890123
            nullValue: null
            """;
        Parameters params = AponParser.parse(apon);

        assertEquals("John Doe", params.getString("name"));
        assertEquals(30, params.getInt("age"));
        assertTrue(params.getBoolean("isActive"));
        assertEquals(123.45, params.getDouble("balance"));
        assertEquals(1234567890123L, params.getLong("id"));
        assertNull(params.getString("nullValue"));
    }

    /**
     * Tests parsing of a one-dimensional array of scalar values.
     */
    @Test
    void testParseOneDimensionalArray() throws AponParseException {
        String apon = """
            items: [
              "apple"
              "banana"
              "cherry"
            ]
            """;
        Parameters params = AponParser.parse(apon);
        List<String> items = params.getStringList("items");

        assertNotNull(items);
        assertEquals(3, items.size());
        assertEquals("apple", items.get(0));
        assertEquals("banana", items.get(1));
        assertEquals("cherry", items.get(2));
    }

    /**
     * Tests parsing of a multi-dimensional array (List of Lists).
     */
    @Test
    void testParseMultiDimensionalArray() throws AponParseException {
        String apon = """
            matrix: [
              [
                "a"
                "b"
              ]
              [
                "c"
                "d"
                "e"
              ]
            ]
            """;
        Parameters params = AponParser.parse(apon);

        @SuppressWarnings("unchecked")
        List<List<String>> matrix = (List<List<String>>)params.getValueList("matrix");

        assertNotNull(matrix);
        assertEquals(2, matrix.size());

        List<String> row1 = matrix.get(0);
        assertEquals(Arrays.asList("a", "b"), row1);

        List<String> row2 = matrix.get(1);
        assertEquals(Arrays.asList("c", "d", "e"), row2);
    }

    /**
     * Tests parsing of nested objects and arrays.
     */
    @Test
    void testParseNestedStructure() throws AponParseException {
        String apon = """
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
        Parameters config = AponParser.parse(apon).getParameters("config");

        assertEquals("App1", config.getString("name"));
        assertEquals(true, config.getParameters("settings").getBoolean("enabled"));
        assertEquals(2, config.getParametersList("users").size());
        assertEquals("Alice", config.getParametersList("users").get(0).getString("name"));
    }

    /**
     * Tests parsing with comments and empty lines.
     */
    @Test
    void testParseWithCommentsAndEmptyLines() throws AponParseException {
        String apon = """
            # This is a comment
            key1: value1

            # Another comment
            key2: value2
            """;
        Parameters params = AponParser.parse(apon);

        assertEquals("value1", params.getString("key1"));
        assertEquals("value2", params.getString("key2"));
        assertEquals(2, params.size());
    }

    /**
     * Tests error handling for unclosed array brackets.
     */
    @Test
    void testErrorHandlingUnclosedArray() {
        String apon = """
            items: [
              "item1"
              "item2"
            # Missing closing bracket
            """;
        assertThrows(AponParseException.class, () -> AponParser.parse(apon));
    }

    /**
     * Tests error handling for unclosed object blocks.
     */
    @Test
    void testErrorHandlingUnclosedObject() {
        String apon = """
            config: {
              key: value
            # Missing closing brace
            """;
        assertThrows(AponParseException.class, () -> AponParser.parse(apon));
    }

    /**
     * Tests error handling for invalid line format.
     */
    @Test
    void testErrorHandlingInvalidLineFormat() {
        String apon = """
            key value # Missing colon
            """;
        assertThrows(AponParseException.class, () -> AponParser.parse(apon));
    }

    /**
     * Tests parsing of a multi-line text block.
     */
    @Test
    void testParseTextBlock() throws AponParseException {
        String apon = """
            message: (
              |Line 1
              |  Line 2
              |Line 3
            )
            """;
        String expected = "Line 1" + SYSTEM_NEW_LINE +
                "  Line 2" + SYSTEM_NEW_LINE +
                "Line 3";

        Parameters params = AponParser.parse(apon);
        assertEquals(expected, params.getString("message"));
    }

    /**
     * Tests parsing of empty structures.
     */
    @Test
    void testParseEmptyStructures() throws AponParseException {
        String apon = """
            emptyObject: {}
            emptyArray: []
            """;
        Parameters params = AponParser.parse(apon);

        Parameters emptyObject = params.getParameters("emptyObject");
        assertNotNull(emptyObject);
        assertTrue(emptyObject.isEmpty());

        List<?> emptyArray = params.getValueList("emptyArray");
        assertNotNull(emptyArray);
        assertTrue(emptyArray.isEmpty());
    }

}
