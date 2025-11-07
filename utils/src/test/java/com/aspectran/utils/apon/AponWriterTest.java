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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test cases for AponWriter.
 *
 * <p>Created: 2020/05/29</p>
 */
class AponWriterTest {

    /**
     * Tests the write-read cycle for various strings that may require special handling.
     * @param inputValue the string to be written and read back
     */
    @ParameterizedTest
    @ValueSource(strings = {
            "'", "\"", " s ", "\u2019", "\\u2019", "a:b", "{c}", "[d]"
    })
    void testWriteAndReadBackSpecialStrings(String inputValue) throws IOException {
        Parameters parameters = new VariableParameters();
        parameters.putValue("param1", inputValue);

        StringWriter stringWriter = new StringWriter();
        new AponWriter(stringWriter).write(parameters);

        Parameters output = new AponReader(stringWriter.toString()).read();

        assertEquals(inputValue, output.getString("param1"));
    }

    /**
     * Tests that a string with newlines is written as a text block and can be read back correctly.
     */
    @Test
    void testWriteMultiLineStringAsTextBlock() throws IOException {
        String input = "1\n2\n3";
        input = input.replace("\n", AponFormat.SYSTEM_NEW_LINE);

        Parameters parameters = new VariableParameters();
        parameters.putValue("textParam", input);

        StringWriter stringWriter = new StringWriter();
        new AponWriter(stringWriter).write(parameters);
        String apon = stringWriter.toString();
        assertTrue(apon.contains("(\n".replace("\n", AponFormat.SYSTEM_NEW_LINE)));
        assertTrue(apon.contains("|1\n".replace("\n", AponFormat.SYSTEM_NEW_LINE)));

        Parameters output = new AponReader(apon).read();
        assertEquals(input, output.getString("textParam"));
    }

    /**
     * Tests writing various primitive data types.
     */
    @Test
    void testWriteValueTypes() throws IOException {
        Parameters params = new VariableParameters();
        params.putValue("boolean", true);
        params.putValue("integer", 123);
        params.putValue("long", 456L);
        params.putValue("double", 78.9);
        params.putValue("nullValue", null);

        String apon = new AponWriter().enableValueTypeHints(true).write(params).toString();
        Parameters readParams = AponReader.read(apon);

        assertEquals(true, readParams.getBoolean("boolean"));
        assertEquals(123, readParams.getInt("integer"));
        assertEquals(456L, readParams.getLong("long"));
        assertEquals(78.9, readParams.getDouble("double"));
        assertTrue(readParams.hasParameter("nullValue"));
        assertNull(null, readParams.getString("nullValue"));
    }

    /**
     * Tests writing nested structures like blocks and arrays.
     */
    @Test
    void testWriteNestedStructures() throws IOException {
        Parameters params = new VariableParameters();
        Parameters nestedBlock = new VariableParameters();
        nestedBlock.putValue("key", "value");
        params.putValue("block", nestedBlock);
        List<String> list = Arrays.asList("a", "b", "c");
        params.putValue("array", list);

        String apon = new AponWriter().write(params).toString();
        Parameters readParams = AponReader.read(apon);

        assertEquals("value", readParams.getParameters("block").getString("key"));
        assertEquals(list, readParams.getStringList("array"));
    }

    /**
     * Tests the 'nullWritable' option.
     */
    @Test
    void testNullWritableOption() throws IOException {
        Parameters params = new VariableParameters();
        params.putValue("key", "value");
        params.putValue("nullKey", null);

        // When nullWritable is false (default), null values are omitted
        AponWriter writer1 = new AponWriter().nullWritable(false);
        String apon1 = writer1.write(params).toString();
        assertFalse(apon1.contains("nullKey"));

        // When nullWritable is true, null values are included
        AponWriter writer2 = new AponWriter().nullWritable(true);
        String apon2 = writer2.write(params).toString();
        assertTrue(apon2.contains("nullKey: null"));
    }

    /**
     * Tests the indentation option for pretty formatting.
     */
    @Test
    void testIndentationOption() throws IOException {
        Parameters params = new VariableParameters();
        Parameters nested = new VariableParameters();
        nested.putValue("key", "value");
        params.putValue("nested", nested);

        AponWriter writer = new AponWriter().indentString("  "); // 2 spaces
        String apon = writer.write(params).toString();

        String expected = """
                nested: {
                  key: value
                }
                """;
        assertEquals(expected.replace("\r\n", "\n"), apon.replace("\r\n", "\n"));
    }

}
