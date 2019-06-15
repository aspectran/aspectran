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
import com.aspectran.core.component.bean.annotation.Action;
import com.aspectran.core.component.bean.annotation.Autowired;
import com.aspectran.core.component.bean.annotation.Bean;
import com.aspectran.core.component.bean.annotation.Component;
import com.aspectran.core.component.bean.annotation.Description;
import com.aspectran.core.component.bean.annotation.RequestToDelete;
import com.aspectran.core.component.bean.annotation.RequestToGet;
import com.aspectran.core.component.bean.annotation.RequestToPost;
import com.aspectran.core.component.bean.annotation.RequestToPut;
import com.aspectran.core.component.bean.annotation.Required;
import com.aspectran.core.component.bean.annotation.Transform;
import com.aspectran.core.context.rule.type.TransformType;
import com.aspectran.web.support.http.HttpStatusSetter;

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
    @Action("customers")
    @Transform(TransformType.JSON)
    public List<Customer> getCustomerList() {
        return repository.getCustomerList();
    }

    @RequestToGet("/customers/${id:guest}")
    @Description("Retrieve customer info by a given id parameter.")
    @Action("customer")
    @Transform(TransformType.JSON)
    public Customer getCustomer(Translet translet, @Required Integer id) {
        Customer customer = repository.getCustomer(id);

        if(customer == null) {
            HttpStatusSetter.notFound(translet);
            return null;
        }

        return customer;
    }

    @RequestToPost("/customers")
    @Description("Register a new customer.")
    @Transform(TransformType.JSON)
    public Customer insertCustomer(Translet translet, Customer customer) {
        int id = repository.insertCustomer(customer);

        String resourceUri = translet.getRequestName() + "/" + id;
        HttpStatusSetter.created(translet, resourceUri);

        return customer;
    }

    @RequestToPut("/customers/${id}")
    @Description("Update customer info with a given ID.")
    @Transform(TransformType.JSON)
    public Customer updateCustomer(Translet translet, Customer customer) {
        boolean updated = repository.updateCustomer(customer);

        if(!updated) {
            HttpStatusSetter.notFound(translet);
            return null;
        }

        return customer;
    }

    @RequestToDelete("/customers/${id}")
    @Description("Delete customer info by a given id parameter.")
    @Action("result")
    @Transform(TransformType.JSON)
    public boolean deleteCustomer(Translet translet, @Required Integer id) {
        boolean deleted = repository.deleteCustomer(id);

        if(!deleted) {
            HttpStatusSetter.notFound(translet);
            return false;
        }

        return true;
    }

    @RequestToPut("/customers/${id}/attributes")
    @Description("Update customer's attributes by a given id parameter.")
    @Transform(TransformType.JSON)
    public boolean updateAttributes(Translet translet, @Required Integer id, @Required Boolean approved) {
        boolean updated = repository.approve(id, approved);

        if(!updated) {
            HttpStatusSetter.notFound(translet);
            return false;
        }

        return true;
    }

}
