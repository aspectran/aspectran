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
package com.aspectran.utils.json;

import com.aspectran.utils.apon.JsonToParameters;
import com.aspectran.utils.apon.Parameters;
import com.aspectran.utils.apon.VariableParameters;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test cases for {@link JsonString}.
 *
 * <p>Created: 2026. 01. 19.</p>
 */
class JsonStringTest {

    @Test
    void testJsonString() {
        String rawJson = "{\"key\":\"value\"}";
        JsonString jsonString = new JsonString(rawJson);
        assertEquals(rawJson, jsonString.toString());
    }

    @Test
    void testJsonStringWithNull() {
        JsonString jsonString = new JsonString(null);
        assertNull(jsonString.toString());
    }

    @Test
    void testWithJsonWriter() throws IOException {
        String rawJson = "{\"key\":\"value\"}";
        JsonString jsonString = new JsonString(rawJson);

        StringWriter out = new StringWriter();
        JsonWriter writer = new JsonWriter(out);
        writer.setPrettyPrint(false);
        writer.beginObject()
                .name("data").value(jsonString)
                .name("desc").value(rawJson)
                .endObject();

        String expected = "{\"data\":{\"key\":\"value\"},\"desc\":\"{\\\"key\\\":\\\"value\\\"}\"}";
        assertEquals(expected, out.toString());
    }

    @Test
    void testWithJsonWriterNullContent() throws IOException {
        JsonString jsonString = new JsonString(null);

        StringWriter out = new StringWriter();
        JsonWriter writer = new JsonWriter(out);
        writer.setPrettyPrint(false);
        writer.beginObject()
                .name("data").value(jsonString)
                .endObject();

        String expected = "{\"data\":null}";
        assertEquals(expected, out.toString());
    }

    @Test
    void testSingleElementArrayConversion() throws IOException {
        String json = """
                {
                    "stringList": ["item1"],
                    "objList": [{"id": 1}]
                }
                """;

        Parameters params = new VariableParameters();
        params.putValue("param1", "value1");
        params.putValue("param2", JsonToParameters.from(json));

        Parameters param2 = params.getParameters("param2");

        // Check string list
        assertTrue(param2.hasParameter("stringList"));
        // This is the critical check: verify it's a List, not a single String
        Object stringVal = param2.getValue("stringList");
        assertInstanceOf(List.class, stringVal);
        List<?> stringList = (List<?>)stringVal;
        assertEquals(1, stringList.size());
        assertEquals("item1", stringList.get(0));

        // Check object list
        assertTrue(param2.hasParameter("objList"));
        // This is the critical check: verify it's a List, not a single Parameters object
        Object objVal = param2.getValue("objList");
        assertInstanceOf(List.class, objVal);
        List<?> objList = (List<?>)objVal;
        assertEquals(1, objList.size());
        assertInstanceOf(Parameters.class, objList.get(0));
        assertEquals(1, ((Parameters)objList.get(0)).getInt("id"));
    }

}
