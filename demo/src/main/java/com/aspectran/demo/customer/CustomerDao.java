/*
 * Copyright (c) 2008-2018 The Aspectran Project
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
package com.aspectran.demo.customer;

import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The DAO to retrieve or manipulate customer data.
 */
public class CustomerDao {
    
    private final Log log = LogFactory.getLog(CustomerDao.class);

    private final Map<Integer, Customer> customerMap;

    private static final AtomicInteger counter = new AtomicInteger();
    
    public CustomerDao() {
        // Pre-create 10 customers whose names begin with "Guest"
        customerMap = new ConcurrentSkipListMap<>();
        
        for(int i = 1; i <= 10; i++) {
            Customer customer = new Customer();
            customer.putValue(Customer.id, i);
            customer.putValue(Customer.name, "Guest - " + i);
            customer.putValue(Customer.age, i + 20);
            customer.putValue(Customer.approved, true);

            customerMap.put(i, customer);
        }

        counter.set(customerMap.size());
    }
    
    public Customer getCustomer(int id) {
        log.debug("Gets the details of customer: " + id);

        Customer customer = customerMap.get(id);
        
        return customer;

    }

    public boolean isCustomer(int id) {
        if(customerMap.containsKey(id)) {
            log.debug("Customer " + id + " exists");
            return true;
        } else {
            log.debug("Customer " + id + " does not exists");
            return false;
        }
    }
    
    public List<Customer> getCustomerList() {
        log.debug("Get a list of all customers");

        List<Customer> customerList = new ArrayList<>(customerMap.values());
        
        log.debug("Retrieved " + customerList.size() + " customers");
        
        return customerList;
    }
    
    public int insertCustomer(Customer customer) {
        int id = counter.incrementAndGet();
        customer.putValue(Customer.id, id);
        
        customerMap.put(id, customer);

        log.debug("Customer " + id + " is registered");
        
        return id;
    }

    public synchronized boolean updateCustomer(Customer customer) {
        int id = customer.getInt(Customer.id);
        
        if(customerMap.containsKey(id)) {
            log.debug("Update customer: " + id);
            customerMap.put(id, customer);
            return true;
        }

        return false;
    }

    public synchronized boolean deleteCustomer(int id) {
        if(customerMap.containsKey(id)) {
            log.debug("Delete customer: " + id);
            customerMap.remove(id);
            return true;
        }
        
        return false;
    }
    
    public boolean approve(int id, boolean approved) {
        Customer customer = customerMap.get(id);
        
        if(customer != null) {
            log.debug(id + "Approval for customer " + id + " (approved: " + approved + ")");
            customer.putValue(Customer.approved, approved);
            return true;
        }
        
        return false;
    }

    public boolean isApproved(int id) {
        Customer customer = customerMap.get(id);
        
        if(customer != null) {
            log.debug("Returns whether customer " + id + " is approved");
            return customer.getBoolean(Customer.approved);
        }
        
        return false;
    }
    
}
