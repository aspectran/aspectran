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
package com.aspectran.core.component.bean.scan;

/**
 * A filter for determining the eligibility of bean classes during component scanning.
 *
 * <p>This class allows for defining include and exclude patterns to control which classes
 * are considered for automatic bean registration. It provides methods to check if a class
 * should be excluded, included, or is generally eligible based on its type and name.</p>
 *
 * <p>Created: 2016. 3. 13.</p>
 */
public interface BeanClassFilter {

    /**
     * Determine whether the given class is eligible for bean registration.
     * @param beanId the bean id
     * @param resourceName the resource name
     * @param targetClass the target class to check
     * @return the bean id if the class is eligible, or {@code null} if it is not
     */
    String filter(String beanId, String resourceName, Class<?> targetClass);

}
