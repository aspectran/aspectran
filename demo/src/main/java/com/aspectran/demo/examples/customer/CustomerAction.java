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
package com.aspectran.demo.examples.customer;

import com.aspectran.core.activity.Translet;
import com.aspectran.core.component.bean.annotation.Autowired;
import com.aspectran.core.component.bean.annotation.Bean;
import com.aspectran.core.component.bean.annotation.Component;
import com.aspectran.core.component.bean.annotation.Description;
import com.aspectran.core.component.bean.annotation.RequestToDelete;
import com.aspectran.core.component.bean.annotation.RequestToGet;
import com.aspectran.core.component.bean.annotation.RequestToPost;
import com.aspectran.core.component.bean.annotation.RequestToPut;
import com.aspectran.core.component.bean.annotation.Required;
import com.aspectran.web.activity.response.DefaultRestResponse;
import com.aspectran.web.activity.response.RestResponse;

import java.util.List;

@Component(namespace = "/examples/gs-rest-service")
@Bean
public class CustomerAction {

    private final CustomerRepository repository;

    @Autowired
    public CustomerAction(CustomerRepository repository) {
        this.repository = repository;
    }

    @RequestToGet("/customers")
    @Description("Returns a list of all customers in JSON format.")
    public RestResponse getCustomerList() {
        List<Customer> list = repository.getCustomerList();
        return new DefaultRestResponse("customers", list).ok();
    }

    @RequestToGet("/customers/${id:guest}")
    @Description("Retrieve customer info by a given id parameter.")
    public RestResponse getCustomer(@Required Integer id) {
        Customer customer = repository.getCustomer(id);
        RestResponse response = new DefaultRestResponse();
        if(customer != null) {
            response.setData("customer", customer);
        } else {
            response.notFound();
        }
        return response;
    }

    @RequestToPost("/customers")
    @Description("Register a new customer.")
    public RestResponse insertCustomer(Translet translet, Customer customer) {
        int id = repository.insertCustomer(customer);
        String resourceUri = translet.getRequestName() + "/" + id;
        return new DefaultRestResponse(customer)
                .created(resourceUri);
    }

    @RequestToPut("/customers/${id}")
    @Description("Update customer info with a given ID.")
    public RestResponse updateCustomer(Customer customer) {
        boolean updated = repository.updateCustomer(customer);
        RestResponse response = new DefaultRestResponse();
        if(!updated) {
            response.setData("customer", customer);
        } else {
            response.notFound();
        }
        return response;
    }

    @RequestToDelete("/customers/${id}")
    @Description("Delete customer info by a given id parameter.")
    public RestResponse deleteCustomer(@Required Integer id) {
        boolean deleted = repository.deleteCustomer(id);
        RestResponse response = new DefaultRestResponse();
        if(deleted) {
            response.setData("result", Boolean.TRUE);
        } else {
            response.notFound();
        }
        return response;
    }

    @RequestToPut("/customers/${id}/attributes")
    @Description("Update customer's attributes by a given id parameter.")
    public RestResponse updateAttributes(@Required Integer id, @Required Boolean approved) {
        boolean updated = repository.approve(id, approved);
        RestResponse response = new DefaultRestResponse();
        if(updated) {
            response.setData("result", Boolean.TRUE);
        } else {
            response.notFound();
        }
        return response;
    }

}
