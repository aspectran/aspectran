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
package com.aspectran.core.context.asel.bean;

import com.aspectran.core.activity.Activity;

/**
 * Defines a strategy for providing a value from a bean-related token.
 * This allows for different evaluation logic (e.g., for fields, methods, classes)
 * to be encapsulated in separate strategy objects.
 */
public interface ValueProvider {

    /**
     * Evaluates and returns the value.
     * @param activity the current activity
     * @return the resolved value
     * @throws Exception if an error occurs during evaluation
     */
    Object evaluate(Activity activity) throws Exception;

    /**
     * Returns the type of the bean that this provider might depend on.
     * For instance or static method/field providers, this would be the declaring class.
     * @return the dependent bean type, or null if not applicable
     */
    Class<?> getDependentBeanType();

    /**
     * Returns whether the provider requires an instance of a bean to be evaluated.
     * @return true if it depends on a bean instance, false for static access
     */
    boolean isRequiresBeanInstance();

}
