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
package com.aspectran.core.component.bean;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.builder.ActivityContextBuilderException;
import com.aspectran.core.context.builder.HybridActivityContextBuilder;
import com.aspectran.core.sample.bean.ProductBean;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FactoryMethodBeanTest {

    private HybridActivityContextBuilder builder;

    private ActivityContext context;

    @BeforeAll
    void ready() throws ActivityContextBuilderException {
        builder = new HybridActivityContextBuilder();
        builder.setDebugMode(true);
        builder.setBasePackages("com.aspectran.core.sample.bean");
        context = builder.build("classpath:config/bean/factory-method-bean-test-config.xml");
    }

    @AfterAll
    void finish() {
        if (builder != null) {
            builder.destroy();
        }
    }

    @Test
    void testNonStaticFactoryMethod() {
        BeanRegistry beanRegistry = context.getBeanRegistry();
        ProductBean product1 = beanRegistry.getBean("product1");
        assertNotNull(product1);
        assertEquals("non-static-product", product1.getName());
        assertEquals(1, product1.getPrice());

        // The factory bean is a singleton, so the call count should increase.
        ProductBean product2 = beanRegistry.getBean("product1");
        assertEquals(2, product2.getPrice());
    }

    @Test
    void testStaticFactoryMethod() {
        BeanRegistry beanRegistry = context.getBeanRegistry();
        ProductBean product1 = beanRegistry.getBean("product2");
        assertNotNull(product1);
        assertEquals("static-product", product1.getName());
        assertEquals(1, product1.getPrice());

        ProductBean product2 = beanRegistry.getBean("product2");
        assertEquals(2, product2.getPrice());
    }

    @Test
    void testAnnotatedFactoryMethod() {
        BeanRegistry beanRegistry = context.getBeanRegistry();
        ProductBean annotatedProduct = beanRegistry.getBean("annotatedProduct");
        assertNotNull(annotatedProduct);
        assertEquals("product-from-annotated-factory", annotatedProduct.getName());
        assertEquals(999, annotatedProduct.getPrice());
    }

    @Test
    void testInitMethod() {
        BeanRegistry beanRegistry = context.getBeanRegistry();
        ProductBean annotatedProduct = beanRegistry.getBean("product4");
        assertEquals("product1-init", annotatedProduct.getName());
    }

}
