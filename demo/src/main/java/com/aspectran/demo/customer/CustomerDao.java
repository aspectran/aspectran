/*
 * Copyright (c) 2008-2017 The Aspectran Project
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
 * 고객 정보 DAO
 */
public class CustomerDao {
    
    private final Log log = LogFactory.getLog(CustomerDao.class);

    private final Map<Integer, Customer> customerMap;

    private static final AtomicInteger counter = new AtomicInteger();
    
    public CustomerDao() {
        // 10명의 고객을 미리 생성합니다.
        customerMap = new ConcurrentSkipListMap<Integer, Customer>();
        
        for(int i = 1; i <= 10; i++) {
            Customer customer = new Customer();
            customer.putValue(Customer.id, i);
            customer.putValue(Customer.name, "Guest - " + i);
            customer.putValue(Customer.age, i + 20);
            customer.putValue(Customer.approved, true);

            customerMap.put(i, customer);
        }

        counter.set(customerMap.size() + 1);
    }
    
    public Customer getCustomer(int id) {
        log.info(id + "번 고객의 상세정보를 조회합니다");

        Customer customer = customerMap.get(id);
        
        return customer;

    }

    public boolean isCustomer(int id) {
        if(customerMap.containsKey(id)) {
            log.info(id + "번 고객은 등록되어 있습니다");
            return true;
        } else {
            log.info(id + "번 고객은 등록되어 있지 않습니다");
            return false;
        }
    }
    
    public List<Customer> getCustomerList() {
        log.info("전체 고객 목록을 조회합니다");

        List<Customer> customerList = new ArrayList<Customer>(customerMap.values());
        
        log.info(customerList.size() + "명의 고객이 조회되었습니다");
        
        return customerList;
    }
    
    public int insertCustomer(Customer customer) {
        int id = counter.incrementAndGet();
        customer.putValue(Customer.id, id);
        
        customerMap.put(id, customer);

        log.info(id + "번 고객이 등록되었습니다");
        
        return id;
    }

    public synchronized boolean updateCustomer(Customer customer) {
        int id = customer.getInt(Customer.id);
        
        if(customerMap.containsKey(id)) {
            log.info(id + "번 고객의 정보를 수정합니다");
            customerMap.put(id, customer);
            return true;
        }

        return false;
    }

    public synchronized boolean deleteCustomer(int id) {
        if(customerMap.containsKey(id)) {
            log.info(id + "번 고객의 정보를 삭제합니다");
            customerMap.remove(id);
            return true;
        }
        
        return false;
    }
    
    public boolean approve(int id, boolean approved) {
        Customer customer = customerMap.get(id);
        
        if(customer != null) {
            log.info(id + "번 고객에 대해 승인처리를 합니다. (승인여부: " + approved + ")");
            customer.putValue(Customer.approved, approved);
            return true;
        }
        
        return false;
    }

    public boolean isApproved(int id) {
        Customer customer = customerMap.get(id);
        
        if(customer != null) {
            log.info(id + "번 고객에 대해 승인여부를 조회합니다");
            return customer.getBoolean(Customer.approved);
        }
        
        return false;
    }
    
}
