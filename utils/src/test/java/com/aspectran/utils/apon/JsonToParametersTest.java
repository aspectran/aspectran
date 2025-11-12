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
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test cases for converting JSON to APON Parameters.
 *
 * <p>Created: 2019-06-29</p>
 */
class JsonToParametersTest {

    /**
     * Tests converting a JSON array to an ArrayParameters object.
     */
    @Test
    void testConvertJsonArrayToArrayParameters() throws IOException {
        String json = """
                [
                  { "param1": 111, "param2": 222 },
                  { "param3": 333, "param4": 444 },
                  null
                ]
                """;
        String apon = """
                [
                  {
                    param1: 111
                    param2: 222
                  }
                  {
                    param3: 333
                    param4: 444
                  }
                ]
                """;

        ArrayParameters params = JsonToParameters.from(json, ArrayParameters.class);
        assertEquals(3, params.getParametersList().size());
        assertEquals(111, params.getParametersList().get(0).getInt("param1"));
        assertEquals(222, params.getParametersList().get(0).getInt("param2"));
        assertEquals(333, params.getParametersList().get(1).getInt("param3"));
        assertEquals(444, params.getParametersList().get(1).getInt("param4"));
        assertEquals(apon.replace("\r\n", "\n"), params.toString().replace("\r\n", "\n"));
    }

    /**
     * Tests converting a complex, nested JSON object.
     */
    @Test
    void testConvertComplexJsonObject() throws IOException {
        String json = """
                {
                    "glossary": {
                        "title": "example glossary",
                        "GlossDiv": {
                            "title": "S",
                            "GlossList": {
                                "GlossEntry": {
                                    "ID": "SGML",
                                    "GlossSeeAlso": ["GML", "XML"]
                                }
                            }
                        }
                    }
                }
                """;

        Parameters params = JsonToParameters.from(json);
        Parameters glossary = params.getParameters("glossary");
        assertNotNull(glossary);
        assertEquals("example glossary", glossary.getString("title"));

        Parameters glossEntry = glossary.getParameters("GlossDiv").getParameters("GlossList").getParameters("GlossEntry");
        assertNotNull(glossEntry);
        assertEquals("SGML", glossEntry.getString("ID"));
        assertEquals(Arrays.asList("GML", "XML"), glossEntry.getStringList("GlossSeeAlso"));
    }

    /**
     * Tests converting JSON to a specific, typed Parameters subclass.
     */
    @Test
    void testConvertJsonToTypedParameters() throws IOException {
        String json = "{\"message\": \"line1\\nline2\"}";

        MessagePayload messagePayload = JsonToParameters.from(json, MessagePayload.class);
        assertEquals("line1\nline2", messagePayload.getContent());

        // Verify that the converted object can be read back by AponReader
        String apon = messagePayload.toString();
        MessagePayload rereadPayload = new AponReader(apon).read(new MessagePayload());
        assertEquals(messagePayload.getContent(), rereadPayload.getContent());
    }

    public static class MessagePayload extends DefaultParameters {
        private static final ParameterKey message = new ParameterKey("message", ValueType.STRING);
        private static final ParameterKey[] parameterKeys = { message };

        public MessagePayload() {
            super(parameterKeys);
        }

        public String getContent() {
            return getString(message);
        }
    }

    /**
     * Tests converting a JSON object that contains an array of objects.
     */
    @Test
    void testConvertJsonObjectWithArray() throws IOException {
        String json = """
                {
                  "arrayObject1": [
                    { "key1": "value1" }
                  ],
                  "arrayObject2": [
                    { "key2-1": "value2-1" },
                    { "key2-2": "value2-2" }
                  ],
                  "arrayString1": [
                    "str1"
                  ],
                  "arrayString2": [
                    "str1", "str2"
                  ],
                  "arrayStringWithNull": [
                    "str1", null
                  ],
                  "arrayNullWithString": [
                    null, "str2"
                  ],
                  "arrayInt1": [
                    1
                  ],
                  "arrayInt2": [
                    1, 2
                  ]
                }
                """;

        Parameters parameters = JsonToParameters.from(json);
        List<Parameters> arrayObject1 = parameters.getParametersList("arrayObject1");
        assertEquals(1, arrayObject1.size());
        assertEquals("value1", arrayObject1.getFirst().getString("key1"));

        List<Parameters> arrayObject2 = parameters.getParametersList("arrayObject2");
        assertNotNull(arrayObject2);
        assertEquals(2, arrayObject2.size());
        assertEquals("value2-1", arrayObject2.get(0).getString("key2-1"));
        assertEquals("value2-2", arrayObject2.get(1).getString("key2-2"));

        List<String> arrayString1 = parameters.getStringList("arrayString1");
        assertEquals(1, arrayString1.size());
        assertEquals("str1", arrayString1.getFirst());

        List<String> arrayString2 = parameters.getStringList("arrayString2");
        assertEquals(2, arrayString2.size());
        assertEquals("str1", arrayString2.get(0));
        assertEquals("str2", arrayString2.get(1));

        List<String> arrayStringWithNull = parameters.getStringList("arrayStringWithNull");
        assertEquals(2, arrayStringWithNull.size());
        assertEquals("str1", arrayStringWithNull.get(0));
        assertNull(arrayStringWithNull.get(1));

        List<String> arrayNullWithString = parameters.getStringList("arrayNullWithString");
        assertEquals(2, arrayNullWithString.size());
        assertNull(arrayNullWithString.getFirst());
        assertEquals("str2", arrayNullWithString.get(1));

        List<Integer> arrayInt1 = parameters.getIntList("arrayInt1");
        assertEquals(1, arrayInt1.size());
        assertEquals(1, arrayInt1.getFirst());

        List<Integer> arrayInt2 = parameters.getIntList("arrayInt2");
        assertEquals(2, arrayInt2.size());
        assertEquals(1, arrayInt2.get(0));
        assertEquals(2, arrayInt2.get(1));
    }

    /**
     * Tests conversion of all JSON primitive types.
     */
    @Test
    void testJsonPrimitiveTypes() throws IOException {
        String json = """
                {
                  "string": "hello",
                  "integer": 123,
                  "long": 1234567890123,
                  "float": 45.67,
                  "double": 98.76,
                  "boolean": true,
                  "nullValue": null
                }
                """;
        Parameters params = JsonToParameters.from(json);
        assertEquals("hello", params.getString("string"));
        assertEquals(123, params.getInt("integer"));
        assertEquals(1234567890123L, params.getLong("long"));
        assertEquals(45.67, params.getDouble("float"), 0.0001); // Assert as double, with a delta for float comparison
        assertEquals(98.76, params.getDouble("double"), 0.0001);
        assertTrue(params.getBoolean("boolean"));
        assertNull(params.getString("nullValue"));
    }

    /**
     * Tests conversion of empty JSON structures.
     */
    @Test
    void testEmptyJsonStructures() throws IOException {
        // Empty object
        Parameters p1 = JsonToParameters.from("{}");
        assertTrue(p1.isEmpty());

        // Empty array
        Parameters p2 = JsonToParameters.from("{\"emptyArray\":[]}");
        assertTrue(p2.getValueList("emptyArray").isEmpty());
    }

    /**
     * Tests that invalid JSON input throws an exception.
     */
    @Test
    void testLenientParsing() throws IOException {
        // Lenient mode should parse non-standard JSON
        Parameters p1 = JsonToParameters.from("{ key: 'value' }", true);
        assertEquals("value", p1.getString("key"));

        Parameters p2 = JsonToParameters.from("{ \"key\": \"value\", }", true);
        assertEquals("value", p2.getString("key"));

        // Strict mode (default) should fail
        assertThrows(IOException.class, () -> JsonToParameters.from("{ key: 'value' }"));
        assertThrows(IOException.class, () -> JsonToParameters.from("{ \"key\": \"value\", }"));
        assertThrows(IOException.class, () -> JsonToParameters.from("not json"));
    }

    @Test
    void testFloatConversionWithTypedParameters() throws IOException {
        String json = "{\"floatValue\": 45.67}";

        TypedPayload payload = JsonToParameters.from(json, TypedPayload.class);
        assertEquals(45.67f, payload.getFloatValue(), 0.001f);
    }

    public static class TypedPayload extends DefaultParameters {
        private static final ParameterKey floatValue = new ParameterKey("floatValue", ValueType.FLOAT);
        private static final ParameterKey[] parameterKeys = { floatValue };

        public TypedPayload() {
            super(parameterKeys);
        }

        public float getFloatValue() {
            return getFloat(floatValue);
        }
    }

    @Test
    void testStringWithQuote() throws IOException {
        String json = "{\"name\":\"she's\"}";
        Parameters parameters = JsonToParameters.from(json);
        String expected = "name: \"she's\"";
        String actual = parameters.toString().trim();
        assertEquals(expected, actual);
    }

    @Test
    void testMixedJsonObject() throws IOException {
        String json = """
            {
              "intro": "Start Testing Now!",
              "one": 1,
              "two": 2,
              "three": 3,
              "nullArray": [
                null,
                null
              ],
              "customers": [
                {
                  "id": "guest-1",
                  "name": "Guest1",
                  "age": 21,
                  "approved": true
                },
                {
                  "id": "guest-2",
                  "name": "Guest2",
                  "age": 22,
                  "approved": true
                }
              ],
              "emptyMap": {
              }
            }
            """;

        String expected = """
            intro: Start Testing Now!
            one: 1
            two: 2
            three: 3
            nullArray: [
              null
              null
            ]
            customers: [
              {
                id: guest-1
                name: Guest1
                age: 21
                approved: true
              }
              {
                id: guest-2
                name: Guest2
                age: 22
                approved: true
              }
            ]
            emptyMap: {
            }
            """.replace("\n", AponFormat.SYSTEM_NEW_LINE);

        Parameters parameters = JsonToParameters.from(json);

        String actual = new AponWriter()
                .nullWritable(true)
                .write(parameters)
                .toString();

        assertEquals(expected, actual);
    }

    /**
     * Tests converting a JSON double array (array of arrays) to Parameters.
     */
    @Test
    void testConvertJsonDoubleArray() throws IOException {
        String json = """
                {
                  "matrix": [
                    ["a", "b"],
                    ["c", "d", "e"]
                  ],
                  "numbers": [
                    [1, 2],
                    [3, 4, 5]
                  ]
                }
                """;

        String apon = """
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
                numbers: [
                  [
                    1
                    2
                  ]
                  [
                    3
                    4
                    5
                  ]
                ]
                """.replace("\n", AponFormat.SYSTEM_NEW_LINE);

        Parameters parameters = JsonToParameters.from(json);
        assertNotNull(parameters);

        // Test matrix (array of string arrays)
        List<Parameters> matrix = parameters.getParametersList("matrix");
        assertNotNull(matrix);
        assertEquals(2, matrix.size());

        List<String> row1 = matrix.get(0).getStringList(ArrayParameters.NONAME);
        assertNotNull(row1);
        assertEquals(Arrays.asList("a", "b"), row1);

        List<String> row2 = matrix.get(1).getStringList(ArrayParameters.NONAME);
        assertNotNull(row2);
        assertEquals(Arrays.asList("c", "d", "e"), row2);

        // Test numbers (array of integer arrays)
        List<Parameters> numbers = parameters.getParametersList("numbers");
        assertNotNull(numbers);
        assertEquals(2, numbers.size());

        List<Integer> numRow1 = numbers.get(0).getIntList(ArrayParameters.NONAME);
        assertNotNull(numRow1);
        assertEquals(Arrays.asList(1, 2), numRow1);

        List<Integer> numRow2 = numbers.get(1).getIntList(ArrayParameters.NONAME);
        assertNotNull(numRow2);
        assertEquals(Arrays.asList(3, 4, 5), numRow2);

        String actualApon = new AponWriter()
                .nullWritable(true)
                .write(parameters)
                .toString();

        assertEquals(apon, actualApon);
    }

}
