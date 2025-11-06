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
import com.aspectran.utils.apon.test.Customer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * <p>Created: 2019-07-07</p>
 */
class ObjectToParametersTest {

    @Test
    void testConvert1() throws IOException {
        String expected = """
            array1: [
              1
            ]
            array2: [
              1
              2
              null
              3
            ]
            list: [
              1
              2
              null
              3
            ]
            enum: [
              1
              2
              null
              3
            ]
            """;

        List<String> list1 = new ArrayList<>();
        list1.add("1");

        List<String> list2 = new ArrayList<>();
        list2.add("1");
        list2.add("2");
        list2.add(null);
        list2.add("3");

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("array1", list1.toArray(new String[0]));
        map.put("array2", list2.toArray(new String[0]));
        map.put("list", list2);
        map.put("enum", Collections.enumeration(list2));

        StringifyContext stringifyContext = new StringifyContext();
        stringifyContext.setDateFormat("yyyy-MM-dd");
        stringifyContext.setDateTimeFormat("yyyy-MM-dd HH:mm:ss");
        stringifyContext.setNullWritable(true);

        Parameters parameters = new ObjectToParameters()
            .apply(stringifyContext)
            .read(map);

        expected = expected.replace("\n", AponFormat.SYSTEM_NEW_LINE);
        String converted = new AponWriter()
            .nullWritable(true)
            .write(parameters)
            .toString();
        assertEquals(expected, converted);
    }

    @Test
    void testConvert2() throws IOException {
        List<Customer> customerList = new ArrayList<>();
        for (int i = 1; i <= 2; i++) {
            Customer customer = new Customer();
            customer.putValue(Customer.id, "guest-" + i);
            customerList.add(customer);
        }
        customerList.add(null);

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("customers", customerList);

        StringifyContext stringifyContext = new StringifyContext();
        stringifyContext.setDateFormat("yyyy-MM-dd");
        stringifyContext.setDateTimeFormat("yyyy-MM-dd HH:mm:ss");
        stringifyContext.setNullWritable(false);

        Parameters parameters = new ObjectToParameters()
            .apply(stringifyContext)
            .read(map);

        String expected = """
            customers: [
              {
                id: guest-1
              }
              {
                id: guest-2
              }
            ]
            """;

        expected = expected.replace("\n", AponFormat.SYSTEM_NEW_LINE);
        String converted = new AponWriter()
            .nullWritable(false)
            .write(parameters)
            .toString();
        assertEquals(expected, converted);
        System.out.println(parameters);
    }

    @Test
    void testConvert3() throws IOException, ParseException {
        String expected = """
            intro: Start Testing Now!
            one: 1
            two: 2
            three: 3
            null: null
            nullArray: [
              null
              null
              null
            ]
            date: 1998-12-31 11:12:13
            localDate: 2016-08-16
            localDateTime: 2016-03-04 10:15:30
            char: A
            customers: [
              {
                id: guest-1
                name: Guest1
                age: 21
                episode: (
                  |His individual skills are outstanding.
                  |I don't know as how he is handsome
                )
                approved: true
              }
              {
                id: guest-2
                name: Guest2
                age: 22
                episode: (
                  |His individual skills are outstanding.
                  |I don't know as how he is handsome
                )
                approved: true
              }
            ]
            """;
        expected = expected.replace("\n", AponFormat.SYSTEM_NEW_LINE);
        String converted = convert();
        assertEquals(expected, converted);
    }

    static String convert() throws IOException, ParseException {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("intro", "Start Testing Now!");
        map.put("one", 1);
        map.put("two", 2);
        map.put("three", 3);
        map.put("null", null);
        map.put("nullArray", new String[] {null, null, null});
        map.put("date", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse("31/12/1998 11:12:13"));
        map.put("localDate", LocalDate.parse("2016-08-16"));
        map.put("localDateTime", LocalDateTime.parse("2016-03-04T10:15:30"));
        map.put("char", 'A');

        List<Customer> customerList = new ArrayList<>();
        for (int i = 1; i <= 2; i++) {
            Customer customer = new Customer();
            customer.putValue(Customer.id, "guest-" + i);
            customer.putValue(Customer.name, "Guest" + i);
            customer.putValue(Customer.age, 20 + i);
            customer.putValue(Customer.episode, "His individual skills are outstanding.\nI don't know as how he is handsome");
            customer.putValue(Customer.approved, true);
            customerList.add(customer);
        }

        map.put("customers", customerList);

        StringifyContext stringifyContext = new StringifyContext();
        stringifyContext.setDateFormat("yyyy-MM-dd");
        stringifyContext.setDateTimeFormat("yyyy-MM-dd HH:mm:ss");
        stringifyContext.setNullWritable(true);

        Parameters parameters = new ObjectToParameters()
                .apply(stringifyContext)
                .read(map);

        return new AponWriter()
                .nullWritable(true)
                .write(parameters)
                .toString();
    }

}
