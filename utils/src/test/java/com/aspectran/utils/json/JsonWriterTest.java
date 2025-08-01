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

import com.aspectran.utils.StringifyContext;
import com.aspectran.utils.apon.AponFormat;
import com.aspectran.utils.apon.Parameters;
import com.aspectran.utils.apon.VariableParameters;
import com.aspectran.utils.apon.test.Customer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonWriterTest {

    @Test
    void test1() throws IOException {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("intro", "Start Testing Now!");
        map.put("null0", null);
        map.put("one", 1);
        map.put("two", 2);
        map.put("three", 3);
        map.put("nullArray", new String[] {null, null});

        List<Customer> customerList = new ArrayList<>();
        for (int i = 1; i <= 2; i++) {
            Customer customer = new Customer();
            customer.putValue(Customer.id, "guest-" + i);
            customer.putValue(Customer.name, "Guest" + i);
            customer.putValue(Customer.age, 20 + i);
            customer.putValue(Customer.approved, true);
            customer.putValue(Customer.episode, null);
            customerList.add(customer);
        }
        map.put("customers", customerList);

        Map<String, Object> emptyMap = new LinkedHashMap<>();
        map.put("emptyMap", emptyMap);

        map.put("null", null);
        map.put("null2", null);

        StringifyContext stringifyContext = new StringifyContext();
        stringifyContext.setDateFormat("yyyy-MM-dd");
        stringifyContext.setDateTimeFormat("yyyy-MM-dd HH:mm:ss");

        String result = new JsonWriter(new StringWriter())
                .apply(stringifyContext)
                .nullWritable(false)
                .value(map)
                .toString();

        // System.out.println(result);

        String expected = """
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

        expected = expected.trim();

        assertEquals(expected, result);
    }

    @Test
    void test2() throws IOException {
        Parameters parameters = new VariableParameters();
        parameters.putValue("item1", 1);
        parameters.putValue("item2", 2);
        Parameters parameters2 = new VariableParameters();
        parameters2.putValue("item11", 11);
        parameters2.putValue("item22", 22);
        parameters.putValue("item3", parameters2);
        parameters.putValue("null", null);

        StringifyContext stringifyContext = new StringifyContext();
        stringifyContext.setDateFormat("yyyy-MM-dd");
        stringifyContext.setDateTimeFormat("yyyy-MM-dd HH:mm:ss");

        String result = new JsonWriter(new StringWriter())
                .apply(stringifyContext)
                .nullWritable(false)
                .value(parameters)
                .toString();

        String expected = """
            {
              "item1": 1,
              "item2": 2,
              "item3": {
                "item11": 11,
                "item22": 22
              }
            }
            """.trim();

        assertEquals(expected, result);
    }

    @Test
    void test3() throws IOException {
        LocalDate date = LocalDate.parse("2019-11-19", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalDate dateTime = LocalDate.parse("2019-11-19 11:15:30", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("localDate", date);
        map.put("localDateTime", dateTime);

        StringifyContext stringifyContext = new StringifyContext();
        stringifyContext.setDateFormat("yyyy-MM-dd");
        stringifyContext.setDateTimeFormat("yyyy-MM-dd HH:mm:ss");

        String result = new JsonWriter(new StringWriter())
                .value(map)
                .toString();

        // System.out.println(result);

        String expected = "{\n" + "  \"localDate\": \"2019-11-19\",\n" + "  \"localDateTime\": \"2019-11-19\"\n" + "}"
                .replace("\n", AponFormat.SYSTEM_NEW_LINE);

        assertEquals(expected, result.trim());
    }

    @Test
    void test4() throws IOException {
        JsonWriter writer2 = new JsonWriter(new StringWriter());
        writer2.beginObject();
        writer2.writeName("key1");
        writer2.writeValue("value");
        writer2.writeName("key2");
        writer2.writeJson("\"1234\"");
        writer2.endObject();

        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginObject();
        writer.writeName("key1");
        writer.writeValue("value");
        writer.writeName("key2");
        writer.writeJson("\"1234\"");
        writer.writeName("json");
        writer.writeJson(writer2.toString());
        writer.writeName("array");
        writer.writeJson("[1, 2, 3]");
        writer.endObject();

        // System.out.println(writer.toString());

        String expected = "{\n" + "  \"key1\": \"value\",\n" + "  \"key2\": \"1234\",\n" + "  \"json\": {\n" +
                "    \"key1\": \"value\",\n" + "    \"key2\": \"1234\"\n" + "  },\n" + "  \"array\": [1, 2, 3]\n" + "}"
                .replace("\n", AponFormat.SYSTEM_NEW_LINE);

        assertEquals(expected, writer.toString().trim());
    }

    @Test
    void test5() {
        Map<String, Object> map1 = new HashMap<>();
        Map<String, Object> map2 = new HashMap<>();

        map1.put("map0", "map0");
        map1.put("map1-2", map2);
        map2.put("map2-1", map1);

        try {
            JsonWriter writer = new JsonWriter(new StringWriter());
            writer.writeValue(map1);
        } catch (IOException e) {
            assertEquals("JSON Serialization Failure: Circular reference was detected while converting member 'map2-1'", e.getMessage());
        }
    }

    @Test
    void test6() {
        Map<String, Object> map1 = new HashMap<>();
        List<Object> list1 = new ArrayList<>();

        map1.put("list1", list1);
        list1.add(map1);

        try {
            JsonWriter writer = new JsonWriter(new StringWriter());
            writer.writeValue(map1);
        } catch (IOException e) {
            assertEquals("JSON Serialization Failure: Circular reference was detected while converting a member", e.getMessage());
        }
    }

}
