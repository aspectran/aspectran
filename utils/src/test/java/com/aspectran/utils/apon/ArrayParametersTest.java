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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Test cases for ArrayParameters.
 *
 * <p>Created: 2019-06-28</p>
 */
class ArrayParametersTest {

    /**
     * Tests that parsing an APON string representing an array of objects
     * via the constructor and via AponReader yields the same result.
     */
    @Test
    void testParsingAponArrayFromText() throws AponParseException {
        String apon = """
                {
                  param1: 111
                  param2: 222
                }
                {
                  param3: 333
                  param4: 444
                }
                """;

        ArrayParameters fromConstructor = new ArrayParameters(apon);
        ArrayParameters fromReader = new AponReader(apon).read(new ArrayParameters());

        assertEquals(fromConstructor.toString(), fromReader.toString());
        assertEquals(2, fromConstructor.getParametersList().size());
        assertEquals(111, fromConstructor.getParametersList().get(0).getInt("param1"));
        assertEquals(444, fromConstructor.getParametersList().get(1).getInt("param4"));
    }

    /**
     * Tests the programmatic creation of an ArrayParameters object and verifies its string output.
     */
    @Test
    void testProgrammaticCreation() {
        Parameters p1 = new VariableParameters();
        p1.putValue("param1", 111);
        p1.putValue("param2", 222);

        Parameters p2 = new VariableParameters();
        p2.putValue("param3", 333);
        p2.putValue("param4", 444);

        ArrayParameters arrayParameters = new ArrayParameters();
        arrayParameters.addParameters(p1);
        arrayParameters.addParameters(p2);

        String expected = """
                {
                  param1: 111
                  param2: 222
                }
                {
                  param3: 333
                  param4: 444
                }
                """;
        // Normalize line endings for comparison
        String actual = arrayParameters.toString().trim().replace("\r\n", "\n");
        String normalizedExpected = expected.trim().replace("\r\n", "\n");

        assertEquals(normalizedExpected, actual);
    }

    /**
     * Tests adding, accessing, and checking the size of elements in ArrayParameters.
     */
    @Test
    void testAddingAndAccessingElements() {
        ArrayParameters arrayParameters = new ArrayParameters();
        assertFalse(arrayParameters.isEmpty());

        Parameters p1 = new VariableParameters();
        p1.putValue("id", 1);
        arrayParameters.addParameters(p1);

        assertEquals(1, arrayParameters.getParametersList().size());
        assertEquals(1, arrayParameters.getParametersList().get(0).getInt("id"));

        Parameters p2 = new VariableParameters();
        p2.putValue("id", 2);
        arrayParameters.addParameters(p2);

        assertEquals(2, arrayParameters.getParametersList().size());
        assertEquals(2, arrayParameters.getParametersList().get(1).getInt("id"));
    }

    /**
     * Tests the behavior of an empty ArrayParameters object.
     */
    @Test
    void testEmptyArray() throws AponParseException {
        ArrayParameters fromEmptyString = new ArrayParameters("");
        assertNull(fromEmptyString.getParametersList());

        ArrayParameters fromWhitespace = new ArrayParameters("  \n\t  ");
        assertNull(fromWhitespace.getParametersList());

        ArrayParameters programmatically = new ArrayParameters();
        assertNull(programmatically.getParametersList());
        assertEquals("[\n]", programmatically.toString().trim());
    }

}
