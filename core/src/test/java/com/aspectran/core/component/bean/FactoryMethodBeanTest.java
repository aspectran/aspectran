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

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

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

    @Test
    void testSingletonCreationConcurrency() throws InterruptedException {
        int numThreads = 100;
        BeanRegistry beanRegistry = context.getBeanRegistry();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(numThreads);
        Set<ProductBean> instances = ConcurrentHashMap.newKeySet();

        // Reset counter before test
        ConcurrentTestFactory.reset();

        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await(); // Wait for the signal to start
                    ProductBean bean = beanRegistry.getBean("concurrentTestProduct");
                    instances.add(bean);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // Signal all threads to start at once
        endLatch.await(); // Wait for all threads to finish
        executor.shutdown();

        // Verification
        assertEquals(1, ConcurrentTestFactory.getInvocationCount(),
                "Factory method should be called exactly once for a singleton bean under concurrency.");
        assertEquals(1, instances.size(),
                "All threads should receive the same singleton instance.");
    }

    /**
     * A factory class for concurrency testing.
     */
    public static class ConcurrentTestFactory {

        private static final AtomicInteger invocationCount = new AtomicInteger(0);

        public static ProductBean createSingletonProduct() {
            invocationCount.incrementAndGet();
            // Simulate some work to increase the chance of race conditions
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                // ignore
            }
            ProductBean product = new ProductBean();
            product.setName("concurrent-singleton");
            return product;
        }

        public static int getInvocationCount() {
            return invocationCount.get();
        }

        public static void reset() {
            invocationCount.set(0);
        }

    }

}
