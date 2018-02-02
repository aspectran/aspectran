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

import com.aspectran.core.activity.Translet;
import com.aspectran.core.component.bean.annotation.Autowired;
import com.aspectran.web.support.http.HttpStatusSetter;

import java.util.List;

public class CustomerAction {
    
    @Autowired
    private CustomerDao dao;
    
    public List<Customer> getCustomerList(Translet translet) {
        return dao.getCustomerList();
    }

    public Customer getCustomer(Translet translet) {
        int id = Integer.parseInt(translet.getParameter("id"));

        Customer customer = dao.getCustomer(id);
        
        if(customer == null) {
            HttpStatusSetter.notFound(translet);
            return null;
        }

        return customer;
    }
    
    public Customer insertCustomer(Translet translet) {
        String name = translet.getParameter("name");
        int age = Integer.valueOf(translet.getParameter("age"));
        boolean approved = "Y".equals(translet.getParameter("approved"));
        
        Customer customer = new Customer();
        customer.putValue(Customer.name, name);
        customer.putValue(Customer.age, age);
        customer.putValue(Customer.approved, approved);
        
        int id = dao.insertCustomer(customer);
        
        String resourceUri = translet.getName() + "/" + id;
        HttpStatusSetter.created(translet, resourceUri);

        return customer;
    }
    
    public Customer updateCustomer(Translet translet) {
        int id = Integer.parseInt(translet.getParameter("id"));
        String name = translet.getParameter("name");
        int age = Integer.valueOf(translet.getParameter("age"));
        boolean approved = "Y".equals(translet.getParameter("approved"));

        Customer customer = new Customer();
        customer.putValue(Customer.id, id);
        customer.putValue(Customer.name, name);
        customer.putValue(Customer.age, age);
        customer.putValue(Customer.approved, approved);

        boolean updated = dao.updateCustomer(customer);

        if(!updated) {
            HttpStatusSetter.notFound(translet);
            return null;
        }

        return customer;
    }

    public boolean deleteCustomer(Translet translet) {
        int id = Integer.parseInt(translet.getParameter("id"));

        boolean deleted = dao.deleteCustomer(id);

        if(!deleted) {
            HttpStatusSetter.notFound(translet);
            return false;
        }

        return true;
    }

    public boolean approve(Translet translet) {
        int id = Integer.parseInt(translet.getParameter("id"));
        boolean approved = Boolean.parseBoolean(translet.getParameter("approved"));

        boolean updated = dao.approve(id, approved);

        if(!updated) {
            HttpStatusSetter.notFound(translet);
            return false;
        }

        return true;
    }
    
    public boolean isApproved(Translet translet) {
        int id = Integer.parseInt(translet.getParameter("id"));
        return dao.isApproved(id);
    }

}
