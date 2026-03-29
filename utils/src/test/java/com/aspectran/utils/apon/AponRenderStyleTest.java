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

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test cases for verifying the three APON rendering styles:
 * PRETTY, SINGLE_LINE, and COMPACT.
 */
class AponRenderStyleTest {

    @Test
    void testPrettyStyle() throws IOException {
        Parameters params = createSampleParameters();
        params.setRenderStyle(AponRenderStyle.PRETTY);
        params.setBraceless(false);

        String expected = """
                {
                  name: John Doe
                  age: 30
                  nested: {
                    key: value
                  }
                }
                """;

        String result = new AponWriter().write(params).toString();
        assertEquals(expected.replace("\r\n", "\n"), result.replace("\r\n", "\n"));
    }

    @Test
    void testSingleLineStyle() throws IOException {
        Parameters params = createSampleParameters();
        params.setRenderStyle(AponRenderStyle.SINGLE_LINE);
        params.setBraceless(false);

        // Single line, spaces after comma and colon
        String expected = "{name: John Doe, age: 30, nested: {key: value}}";

        String result = new AponWriter().write(params).toString();
        assertEquals(expected, result);
    }

    @Test
    void testCompactStyle() throws IOException {
        Parameters params = createSampleParameters();
        params.setRenderStyle(AponRenderStyle.COMPACT);
        params.setBraceless(false);

        // Dense one-line, no extra spaces
        String expected = "{name:John Doe,age:30,nested:{key:value}}";

        String result = new AponWriter().write(params).toString();
        assertEquals(expected, result);
    }

    @Test
    void testBracelessAtRoot() throws IOException {
        Parameters params = createSampleParameters();
        params.setBraceless(true);

        // PRETTY + Braceless
        params.setRenderStyle(AponRenderStyle.PRETTY);
        String expectedPretty = """
                name: John Doe
                age: 30
                nested: {
                  key: value
                }
                """;
        assertEquals(expectedPretty.replace("\r\n", "\n"), params.toString().replace("\r\n", "\n"));

        // SINGLE_LINE + Braceless
        params.setRenderStyle(AponRenderStyle.SINGLE_LINE);
        assertEquals("name: John Doe, age: 30, nested: {key: value}", params.toString());

        // COMPACT + Braceless
        params.setRenderStyle(AponRenderStyle.COMPACT);
        assertEquals("name:John Doe,age:30,nested:{key:value}", params.toString());
    }

    @Test
    void testNestedStyleMix() throws IOException {
        Parameters root = new VariableParameters();
        root.putValue("id", "root");

        Parameters child = root.touchParameters("child");
        child.putValue("email", "test@example.com");
        child.setRenderStyle(AponRenderStyle.COMPACT); // Only child is compact

        root.setRenderStyle(AponRenderStyle.PRETTY);
        root.setBraceless(true);

        // Child is compact, so its endBlock() -> newLine() won't write \n
        String expected = """
                id: root
                child: {email:test@example.com}""";

        String result = new AponWriter().write(root).toString();
        assertEquals(expected.replace("\r\n", "\n"), result.replace("\r\n", "\n"));
    }

    private @NonNull Parameters createSampleParameters() {
        Parameters params = new VariableParameters();
        params.putValue("name", "John Doe");
        params.putValue("age", 30);
        Parameters nested = params.touchParameters("nested");
        nested.putValue("key", "value");
        return params;
    }

}
