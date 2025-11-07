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

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test cases for JsonReader.
 *
 * <p>Created: 2020/05/30</p>
 */
class JsonReaderTest {

    @Test
    void testReadSimpleObject() throws IOException {
        String json = "{\"name\":\"John Doe\",\"age\":30,\"isStudent\":false}";
        JsonReader reader = new JsonReader(json);
        assertEquals(JsonToken.BEGIN_OBJECT, reader.peek());
        reader.beginObject();
        assertEquals("name", reader.nextName());
        assertEquals("John Doe", reader.nextString());
        assertEquals("age", reader.nextName());
        assertEquals(30, reader.nextInt());
        assertEquals("isStudent", reader.nextName());
        assertEquals(false, reader.nextBoolean());
        reader.endObject();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    @Test
    void testReadSimpleArray() throws IOException {
        String json = "[1, \"hello\", true, null]";
        JsonReader reader = new JsonReader(json);
        assertEquals(JsonToken.BEGIN_ARRAY, reader.peek());
        reader.beginArray();
        assertEquals(1, reader.nextInt());
        assertEquals("hello", reader.nextString());
        assertEquals(true, reader.nextBoolean());
        reader.nextNull();
        reader.endArray();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    @Test
    void testReadNested() throws IOException {
        String json = "{\"data\":[{\"id\":1}]}";
        JsonReader reader = new JsonReader(json);
        reader.beginObject();
        assertEquals("data", reader.nextName());
        reader.beginArray();
        reader.beginObject();
        assertEquals("id", reader.nextName());
        assertEquals(1, reader.nextInt());
        reader.endObject();
        reader.endArray();
        reader.endObject();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    @Test
    void testUnclosedObject() {
        String json = "{\"name\":\"John Doe\"";
        JsonReader reader = new JsonReader(json);
        assertThrows(IOException.class, () -> {
            reader.beginObject();
            reader.nextName();
            reader.nextString();
            reader.endObject();
        });
    }

    @Test
    void testMalformedJson() {
        String json = "{key: 'value'}"; // Unquoted key
        JsonReader reader = new JsonReader(json);
        assertThrows(MalformedJsonException.class, () -> {
            reader.beginObject();
            reader.nextName(); // This should fail
        });
    }

}
