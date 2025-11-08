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

import com.aspectran.utils.StringifyContext;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test cases for converting Java Objects to APON Parameters.
 *
 * <p>Created: 2019-07-07</p>
 */
class ObjectToParametersTest {

    /**
     * Tests the conversion of various collection types like Array, List, and Enumeration.
     */
    @Test
    void testConvertVariousCollectionTypes() {
        List<String> list = Arrays.asList("1", "2", null, "3");
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("array", list.toArray(new String[0]));
        map.put("list", list);
        map.put("enum", Collections.enumeration(list));

        StringifyContext stringifyContext = new StringifyContext();
        stringifyContext.setNullWritable(true);

        Parameters parameters = new ObjectToParameters()
                .apply(stringifyContext)
                .read(map);

        assertEquals(Arrays.asList("1", "2", null, "3"), parameters.getStringList("array"));
        assertEquals(Arrays.asList("1", "2", null, "3"), parameters.getStringList("list"));
        assertEquals(Arrays.asList("1", "2", null, "3"), parameters.getStringList("enum"));
    }

    /**
     * Tests the conversion of a List of Parameters and how the 'nullWritable' option affects the output.
     */
    @Test
    void testConvertListWithNullWritableOption() {
        // Create a list of Parameters objects, including a null
        List<Parameters> customerList = new ArrayList<>();
        Parameters p1 = new VariableParameters();
        p1.putValue("id", "guest-1");
        customerList.add(p1);
        customerList.add(null); // Add a null element

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("customers", customerList);

        // Case 1: nullWritable = false (nulls in list should be skipped)
        StringifyContext contextFalse = new StringifyContext();
        contextFalse.setNullWritable(false);
        Parameters paramsFalse = new ObjectToParameters().apply(contextFalse).read(map);
        List<Parameters> resultListFalse = paramsFalse.getParametersList("customers");
        assertEquals(1, resultListFalse.size());
        assertEquals("guest-1", resultListFalse.get(0).getString("id"));

        // Case 2: nullWritable = true (nulls in list should be preserved)
        StringifyContext contextTrue = new StringifyContext();
        contextTrue.setNullWritable(true);
        Parameters paramsTrue = new ObjectToParameters().apply(contextTrue).read(map);
        List<Parameters> resultListTrue = paramsTrue.getParametersList("customers");
        assertEquals(2, resultListTrue.size());
        assertEquals("guest-1", resultListTrue.get(0).getString("id"));
        assertNull(resultListTrue.get(1));
    }

    /**
     * Tests the conversion of a Map with a mix of various data types.
     */
    @Test
    void testConvertMapWithMixedDataTypes() throws ParseException {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("intro", "Start Testing Now!");
        map.put("one", 1);
        map.put("aNull", null);
        map.put("date", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse("31/12/1998 11:12:13"));
        map.put("localDate", LocalDate.parse("2016-08-16"));
        map.put("localDateTime", LocalDateTime.parse("2016-03-04T10:15:30"));
        map.put("char", 'A');

        StringifyContext stringifyContext = new StringifyContext();
        stringifyContext.setDateFormat("yyyy-MM-dd");
        stringifyContext.setDateTimeFormat("yyyy-MM-dd HH:mm:ss");
        stringifyContext.setNullWritable(true);

        Parameters params = new ObjectToParameters()
                .apply(stringifyContext)
                .read(map);

        assertEquals("Start Testing Now!", params.getString("intro"));
        assertEquals(1, params.getInt("one"));
        assertTrue(params.hasParameter("aNull"));
        assertNull(params.getString("aNull"));
        assertEquals("1998-12-31 11:12:13", params.getString("date"));
        assertEquals("2016-08-16", params.getString("localDate"));
        assertEquals("2016-03-04 10:15:30", params.getString("localDateTime"));
        assertEquals("A", params.getString("char"));
    }

    /**
     * Tests the conversion of a nested Map structure.
     */
    @Test
    void testNestedMapConversion() {
        Map<String, Object> root = new LinkedHashMap<>();
        Map<String, Object> nested = new LinkedHashMap<>();
        nested.put("key", "value");
        nested.put("number", 123);
        root.put("nestedMap", nested);

        Parameters params = new ObjectToParameters().read(root);

        Parameters nestedParams = params.getParameters("nestedMap");
        assertNotNull(nestedParams);
        assertEquals("value", nestedParams.getString("key"));
        assertEquals(123, nestedParams.getInt("number"));
    }

}
