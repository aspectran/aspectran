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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
        arrayParameters.addValue(p1);
        arrayParameters.addValue(p2);

        String expected = """
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
        arrayParameters.addValue(p1);

        assertEquals(1, arrayParameters.getParametersList().size());
        assertEquals(1, arrayParameters.getParametersList().get(0).getInt("id"));

        Parameters p2 = new VariableParameters();
        p2.putValue("id", 2);
        arrayParameters.addValue(p2);

        assertEquals(2, arrayParameters.getParametersList().size());
        assertEquals(2, arrayParameters.getParametersList().get(1).getInt("id"));
    }

    /**
     * Tests the behavior of an empty ArrayParameters object.
     */
    @Test
    void testEmptyArray() throws AponParseException {
        ArrayParameters fromEmptyString = new ArrayParameters("");
        assertNotNull(fromEmptyString.iterator());
        assertFalse(fromEmptyString.iterator().hasNext());

        ArrayParameters fromWhitespace = new ArrayParameters("  \n\t  ");
        assertNotNull(fromWhitespace.iterator());
        assertFalse(fromWhitespace.iterator().hasNext());

        ArrayParameters programmatically = new ArrayParameters();
        assertNotNull(programmatically.iterator());
        assertEquals("[\n]", programmatically.toString().trim().replace("\r\n", "\n"));
    }

    @Test
    void testGettingTypedArrays() {
        // Test with Strings
        ArrayParameters stringParams = new ArrayParameters();
        stringParams.addValue("apple");
        stringParams.addValue("banana");
        stringParams.addValue("cherry");

        String[] stringArray = {"apple", "banana", "cherry"};
        List<String> stringList = List.of("apple", "banana", "cherry");
        assertArrayEquals(stringArray, stringParams.getStringArray());
        assertEquals(stringList, stringParams.getStringList());

        // Test with Integers
        ArrayParameters intParams = new ArrayParameters();
        intParams.addValue(10);
        intParams.addValue(20);
        intParams.addValue(30);

        Integer[] intArray = {10, 20, 30};
        List<Integer> intList = List.of(10, 20, 30);
        assertArrayEquals(intArray, intParams.getIntArray());
        assertEquals(intList, intParams.getIntList());

        // Test with Longs
        ArrayParameters longParams = new ArrayParameters();
        longParams.addValue(100L);
        longParams.addValue(200L);
        longParams.addValue(300L);

        Long[] longArray = {100L, 200L, 300L};
        List<Long> longList = List.of(100L, 200L, 300L);
        assertArrayEquals(longArray, longParams.getLongArray());
        assertEquals(longList, longParams.getLongList());

        // Test with Doubles
        ArrayParameters doubleParams = new ArrayParameters();
        doubleParams.addValue(10.1);
        doubleParams.addValue(20.2);
        doubleParams.addValue(30.3);

        Double[] doubleArray = {10.1, 20.2, 30.3};
        List<Double> doubleList = List.of(10.1, 20.2, 30.3);
        assertArrayEquals(doubleArray, doubleParams.getDoubleArray());
        assertEquals(doubleList, doubleParams.getDoubleList());

        // Test with Booleans
        ArrayParameters boolParams = new ArrayParameters();
        boolParams.addValue(true);
        boolParams.addValue(false);
        boolParams.addValue(true);

        Boolean[] boolArray = {true, false, true};
        List<Boolean> boolList = List.of(true, false, true);
        assertArrayEquals(boolArray, boolParams.getBooleanArray());
        assertEquals(boolList, boolParams.getBooleanList());
    }

    @Test
    void testMixedTypeArray() {
        ArrayParameters mixedParams = new ArrayParameters();
        mixedParams.addValue("text");
        mixedParams.addValue(123);
        mixedParams.addValue(true);
        Parameters p = new VariableParameters();
        p.putValue("p1", "v1");
        mixedParams.addValue(p);

        List<?> valueList = mixedParams.getValueList();
        assertEquals(4, valueList.size());
        assertEquals("text", valueList.get(0));
        assertEquals(123, valueList.get(1));
        assertEquals(true, valueList.get(2));
        assertEquals(p, valueList.get(3));

        // Test conversion to string array
        String[] stringArray = {"text", "123", "true", p.toString()};
        assertArrayEquals(stringArray, mixedParams.getStringArray());
    }

    @Test
    void testNestedArrayParametersInVariableParameters() throws IOException {
        VariableParameters mainParams = new VariableParameters();

        // 1. Add an ArrayParameters of Strings
        ArrayParameters stringArrayParams = new ArrayParameters();
        stringArrayParams.addValue("value1");
        stringArrayParams.addValue("value2");
        mainParams.putValue("stringList", stringArrayParams);

        // 2. Add an ArrayParameters of Integers
        ArrayParameters intArrayParams = new ArrayParameters();
        intArrayParams.addValue(100);
        intArrayParams.addValue(200);
        mainParams.putValue("intList", intArrayParams);

        // 3. Add an ArrayParameters of nested Parameters
        ArrayParameters nestedParamsArray = new ArrayParameters();
        VariableParameters nested1 = new VariableParameters();
        nested1.putValue("id", 1);
        nested1.putValue("name", "Item A");
        nestedParamsArray.addValue(nested1);

        VariableParameters nested2 = new VariableParameters();
        nested2.putValue("id", 2);
        nested2.putValue("name", "Item B");
        nestedParamsArray.addValue(nested2);
        mainParams.putValue("objectList", nestedParamsArray);

        // Verify retrieval of stringList
        ArrayParameters retrievedStringArray = mainParams.getParameters("stringList");
        assertNotNull(retrievedStringArray);
        assertEquals(2, retrievedStringArray.getStringList().size());
        assertEquals("value1", retrievedStringArray.getStringList().get(0));
        assertEquals("value2", retrievedStringArray.getStringList().get(1));

        // Verify retrieval of intList
        ArrayParameters retrievedIntArray = mainParams.getParameters("intList");
        assertNotNull(retrievedIntArray);
        assertEquals(2, retrievedIntArray.getIntList().size());
        assertEquals(100, retrievedIntArray.getIntList().get(0));
        assertEquals(200, retrievedIntArray.getIntList().get(1));

        // Verify retrieval of objectList
        ArrayParameters retrievedObjectArray = mainParams.getParameters("objectList");
        assertNotNull(retrievedObjectArray);
        assertEquals(2, retrievedObjectArray.getParametersList().size());
        assertEquals(1, retrievedObjectArray.getParametersList().get(0).getInt("id"));
        assertEquals("Item A", retrievedObjectArray.getParametersList().get(0).getString("name"));
        assertEquals(2, retrievedObjectArray.getParametersList().get(1).getInt("id"));
        assertEquals("Item B", retrievedObjectArray.getParametersList().get(1).getString("name"));

        // 4. Verify APON string output
        String apon = new AponWriter().write(mainParams).toString();
        assertEquals(apon, mainParams.toString());

        String expectedApon = """
                stringList: [
                  value1
                  value2
                ]
                intList: [
                  100
                  200
                ]
                objectList: [
                  {
                    id: 1
                    name: Item A
                  }
                  {
                    id: 2
                    name: Item B
                  }
                ]""";
        // Normalize line endings for comparison
        String actual = mainParams.toString().trim().replace("\r\n", "\n");
        String normalizedExpected = expectedApon.trim().replace("\r\n", "\n");
        assertEquals(normalizedExpected, actual);
    }

}
