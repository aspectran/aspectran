/*
 * Copyright (c) 2008-2019 The Aspectran Project
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
package com.aspectran.core.util.json;

import com.aspectran.core.util.apon.Parameters;
import com.aspectran.core.util.apon.VariableParameters;
import com.aspectran.core.util.apon.test.Customer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JsonWriterTest {

    public static void main(String[] args) {
        try {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("intro", "Start Testing Now!");
            map.put("null0", null);
            map.put("one", 1);
            map.put("two", 2);
            map.put("three", 3);
            map.put("nullArray", new String[] {null, null, null});
            map.put("date", new Date());
            map.put("localDate", LocalDate.now());
            map.put("localDateTime", LocalDateTime.now());

            List<Customer> customerList = new ArrayList<>();

            for (int i = 1; i <= 10; i++) {
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

            String result = new JsonWriter()
                    .nullWritable(false)
                    .dateFormat("yyyy-MM-dd")
                    .dateTimeFormat("yyyy-MM-dd HH:mm:ss")
                    .write(map)
                    .toString();

            System.out.println(result);


            Parameters parameters = new VariableParameters();
            parameters.putValue("item1", 1);
            parameters.putValue("item2", 2);
            Parameters parameters2 = new VariableParameters();
            parameters2.putValue("item11", 11);
            parameters2.putValue("item22", 22);
            parameters.putValue("item3", parameters2);

            String result2 = new JsonWriter()
                    .nullWritable(false)
                    .dateFormat("yyyy-MM-dd")
                    .dateTimeFormat("yyyy-MM-dd HH:mm:ss")
                    .write(parameters)
                    .toString();

            System.out.println(result2);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
