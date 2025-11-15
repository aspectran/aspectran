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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Test cases for behavior of ParameterValue.
 *
 * <p>Created: 2025-11-13</p>
 */
class ParameterValueBehaviorTest {

    /**
     * A Parameters class with a 1D array of VARIABLE type.
     */
    public static class VariableArrayParameters extends DefaultParameters {

        private static final ParameterKey array = new ParameterKey("array", ValueType.VARIABLE, true);

        private static final ParameterKey[] parameterKeys = {array};

        public VariableArrayParameters() {
            super(parameterKeys);
        }

        public List<?> getArray() {
            return getValueList(array);
        }

    }

    /**
     * A Parameters class with a 1D array of a fixed type (STRING).
     */
    public static class FixedStringArrayParameters extends DefaultParameters {

        private static final ParameterKey array = new ParameterKey("array", ValueType.STRING, true);

        private static final ParameterKey[] parameterKeys = {array};

        public FixedStringArrayParameters() {
            super(parameterKeys);
        }

        public List<String> getArray() {
            return getStringList(array);
        }

    }

    /**
     * This test verifies that a 1D array parameter with ValueType.VARIABLE
     * can be automatically promoted to a higher dimension if a list is added to it.
     */
    @Test
    void testAutomaticDimensionPromotionForVariableType() {
        VariableArrayParameters params = new VariableArrayParameters();
        ParameterValue pv = params.getParameterValue("array");

        // 1. Add a simple string value directly to the ParameterValue
        pv.putValue("value1");

        // 2. Add a list directly to the ParameterValue to test dimension promotion
        List<String> nestedList = Arrays.asList("nested_a", "nested_b");
        pv.putValue(nestedList);

        // 3. Retrieve the final list and verify its contents
        List<?> resultList = params.getArray();

        // Assert that the list now contains both the string and the nested list
        assertEquals(2, resultList.size());
        assertEquals("value1", resultList.get(0));
        assertInstanceOf(List.class, resultList.get(1), "The second element should be a List");
        assertEquals(nestedList, resultList.get(1));

        // This confirms that the array's dimension was effectively promoted,
        // creating a mixed-type, multi-dimensional structure.
    }

    /**
     * This test verifies that a 1D array parameter with a fixed ValueType (e.g., STRING)
     * is NOT promoted to a higher dimension. Instead, the added list is converted
     * to its string representation.
     */
    @Test
    void testNoDimensionPromotionForFixedType() {
        FixedStringArrayParameters params = new FixedStringArrayParameters();

        // 1. Add a simple string value to the array
        params.putValue("array", "value1");
        params.removeValue("array");

        // 2. Add a list to the same array
        List<String> nestedList = Arrays.asList("nested_a", "nested_b");
        params.putValue("array", nestedList);

        // 3. Retrieve the final list and verify its contents
        List<String> resultList = params.getArray();

        // Assert that the list contains the original string and a string representation of the nested list
        assertEquals(2, resultList.size());
        assertEquals("nested_a", resultList.get(0));
        assertEquals(nestedList.toString(), resultList.toString());
    }

}
