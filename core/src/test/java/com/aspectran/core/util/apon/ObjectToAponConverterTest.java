package com.aspectran.core.util.apon;

import com.aspectran.core.util.apon.test.Customer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>Created: 2019-07-07</p>
 */
class ObjectToAponConverterTest {

    public static void main(String[] args) {
        try {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("intro", "Start Testing Now!");
            map.put("one", 1);
            map.put("two", 2);
            map.put("three", 3);
            map.put("null", null);
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
                customer.putValue(Customer.episode, "His individual skills are outstanding.\nI don't know as how he is handsome");
                customer.putValue(Customer.approved, true);

                customerList.add(customer);
            }

            map.put("customers", customerList);

            Parameters parameters = new ObjectToAponConverter()
                    .dateFormat("yyyy-MM-dd")
                    .dateTimeFormat("yyyy-MM-dd HH:mm:ss")
                    .toParameters(map);

            String result = new AponWriter()
                    .nullWritable(false)
                    .write(parameters)
                    .toString();

            System.out.println(result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}