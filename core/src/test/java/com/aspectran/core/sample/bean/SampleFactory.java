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
package com.aspectran.core.sample.bean;

public class SampleFactory {

    private static int staticCallCount = 0;

    private int callCount = 0;

    public void init() {
        staticCallCount = 0;
        callCount = 0;
    }

    public ProductBean createProduct() {
        callCount++;
        ProductBean product = new ProductBean();
        product.setName("non-static-product");
        product.setPrice(callCount);
        return product;
    }

    public static ProductBean createStaticProduct() {
        staticCallCount++;
        ProductBean product = new ProductBean();
        product.setName("static-product");
        product.setPrice(staticCallCount);
        return product;
    }

    public ProductBean createProductWithInitMethod() {
        ProductBean product = new ProductBean();
        product.setName("product-with-init-method");
        return product;
    }

    public static void invalidStaticInitMethod() {
        // This method should not be called
    }

}
