/*
 * Copyright (c) 2008-2023 The Aspectran Project
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
package com.aspectran.core.util.apon;

import com.aspectran.core.util.apon.test.Customer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * <p>Created: 2019-07-07</p>
 */
class ObjectToAponConverterTest {

    @Test
    void test() throws IOException, ParseException {
        String converted = convert();
        String expected = "intro: Start Testing Now!\n" + "one: 1\n" + "two: 2\n" + "three: 3\n" + "null: null\n" + "nullArray: null\n" + "date: 1998-12-31 11:12:13\n" + "localDate: 2016-08-16\n" + "localDateTime: 2016-03-04 10:15:30\n" + "customers: [\n" + "  {\n" + "    id: guest-1\n" + "    name: Guest1\n" + "    age: 21\n" + "    episode: (\n" + "      |His individual skills are outstanding.\n" + "      |I don't know as how he is handsome\n" + "    )\n" + "    approved: true\n" + "  }\n" + "  {\n" + "    id: guest-2\n" + "    name: Guest2\n" + "    age: 22\n" + "    episode: (\n" + "      |His individual skills are outstanding.\n" + "      |I don't know as how he is handsome\n" + "    )\n" + "    approved: true\n" + "  }\n" + "]\n";
        assertEquals(converted, expected.replace("\n", AponFormat.SYSTEM_NEW_LINE));
    }

    public static String convert() throws IOException, ParseException {
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

        Parameters parameters = new ObjectToAponConverter()
                .dateFormat("yyyy-MM-dd")
                .dateTimeFormat("yyyy-MM-dd HH:mm:ss")
                .toParameters(map);

        return new AponWriter()
                .nullWritable(true)
                .write(parameters)
                .toString();
    }

    public static void main(String[] args) {
        try {
            String result = convert();
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
