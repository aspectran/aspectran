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
package com.aspectran.core.component.bean;

/**
 * The Class RequiredTypeBeanNotFoundException.
 */
public class RequiredTypeBeanNotFoundException extends BeanException {

    /** @serial */
    private static final long serialVersionUID = 4313746137902620189L;

    private Class<?> requiredType;

    /**
     * Instantiates a new BeanNotFoundException.
     *
     * @param requiredType the required type
     */
    public RequiredTypeBeanNotFoundException(Class<?> requiredType) {
        super("No matching bean of type [" + requiredType + "] found");
        this.requiredType = requiredType;
    }

    /**
     * Gets the required type.
     *
     * @return the required type
     */
    public Class<?> getRequiredType() {
        return requiredType;
    }

}
