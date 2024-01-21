/*
 * Copyright (c) 2008-2024 The Aspectran Project
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
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.web.activity.response.DefaultRestResponse;
import com.aspectran.web.activity.response.RestResponse;

import java.util.List;

@Component("/examples/gs-rest-service/customers")
@Bean
public class CustomerActivity {

    private final CustomerRepository repository;

    @Autowired
    public CustomerActivity(CustomerRepository repository) {
        this.repository = repository;
    }

    @RequestToGet
    @Description("Returns list of all customers in JSON format.")
    public RestResponse getCustomerList() {
        List<Customer> list = repository.getCustomerList();
        return new DefaultRestResponse("customers", list).ok();
    }

    @RequestToGet("/${id:guest}")
    @Description("Retrieves a customer by ID.")
    public RestResponse getCustomer(@Required Integer id) {
        Customer customer = repository.getCustomer(id);
        RestResponse response = new DefaultRestResponse();
        if (customer != null) {
            response.setData("customer", customer);
        } else {
            response.notFound();
        }
        return response;
    }

    @RequestToPost
    @Description("Add a new customer to the repository.")
    public RestResponse addCustomer(@NonNull Translet translet, @Required Customer customer) {
        int id = repository.insertCustomer(customer);
        String resourceUri = translet.getRequestName() + "/" + id;
        return new DefaultRestResponse(customer)
                .created(resourceUri);
    }

    @RequestToPut("/${id}")
    @Description("Updates an existing customer in the repository with form data.")
    public RestResponse updateCustomer(@Required Customer customer) {
        boolean updated = repository.updateCustomer(customer);
        RestResponse response = new DefaultRestResponse();
        if (updated) {
            response.setData("customer", customer);
        } else {
            response.notFound();
        }
        return response;
    }

    @RequestToDelete("/${id}")
    @Description("Deletes a customer by ID.")
    public RestResponse deleteCustomer(@Required Integer id) {
        boolean deleted = repository.deleteCustomer(id);
        RestResponse response = new DefaultRestResponse();
        if (deleted) {
            response.setData("result", Boolean.TRUE);
        } else {
            response.notFound();
        }
        return response;
    }

    @RequestToPut("/${id}/attributes")
    @Description("Updates an existing customer's attributes.")
    public RestResponse updateAttributes(@Required Integer id, @Required Boolean approved) {
        boolean updated = repository.approve(id, approved);
        RestResponse response = new DefaultRestResponse();
        if (updated) {
            response.setData("result", Boolean.TRUE);
        } else {
            response.notFound();
        }
        return response;
    }

}
