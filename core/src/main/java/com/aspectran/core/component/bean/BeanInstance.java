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

import com.aspectran.utils.annotation.jsr305.NonNull;

import java.io.Serial;
import java.io.Serializable;

/**
 * A serializable wrapper for a bean instance stored in a scope.
 * <p>This class encapsulates the final bean instance (the "product") and, if applicable,
 * the factory bean that created it. Storing the factory instance is crucial for managing
 * its lifecycle, particularly for invoking its {@code destroy-method} when the scope is
 * destroyed. This is essential for beans created with a {@code factory-method} on a
 * regular bean class, where the factory itself has a separate lifecycle to manage.</p>
 */
public class BeanInstance implements Serializable {

    @Serial
    private static final long serialVersionUID = -286637932832213733L;

    /** The final bean instance (the product). */
    private final Object bean;

    /** The factory bean instance that created the final bean, if applicable. */
    private final Object factory;

    /**
     * Creates a new BeanInstance.
     * This constructor is private to enforce the use of static factory methods.
     * @param bean the final bean instance (the product)
     * @param factory the factory bean instance that created the bean
     */
    private BeanInstance(Object bean, Object factory) {
        this.bean = bean;
        this.factory = factory;
    }

    /**
     * Returns the final bean instance (the product).
     * @return the bean instance
     */
    public Object getBean() {
        return bean;
    }

    /**
     * Returns the factory bean instance that created the final bean.
     * @return the factory bean instance, or {@code null} if not applicable
     */
    public Object getFactory() {
        return factory;
    }

    /**
     * Creates a BeanInstance representing a final bean product.
     * The factory is null in this case.
     * @param product the final bean instance (the product)
     * @return a new BeanInstance
     */
    @NonNull
    public static BeanInstance forProduct(Object product) {
        return new BeanInstance(product, null);
    }

    /**
     * Creates a BeanInstance representing a factory bean.
     * The product is null in this case. This is typically used for early exposure
     * of a factory bean for circular dependency resolution.
     * @param factory the factory bean instance
     * @return a new BeanInstance
     */
    @NonNull
    public static BeanInstance forFactory(Object factory) {
        return new BeanInstance(null, factory);
    }

    /**
     * Creates a BeanInstance representing a final bean product and its producing factory.
     * @param product the final bean instance (the product)
     * @param factory the factory bean instance that created the product
     * @return a new BeanInstance
     */
    @NonNull
    public static BeanInstance of(Object product, Object factory) {
        return new BeanInstance(product, factory);
    }

}
