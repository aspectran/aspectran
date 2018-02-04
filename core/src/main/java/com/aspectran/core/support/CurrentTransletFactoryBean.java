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
package com.aspectran.core.support;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.Translet;
import com.aspectran.core.component.bean.ablility.FactoryBean;
import com.aspectran.core.component.bean.aware.CurrentActivityAware;

/**
 * {@code CurrentTransletFactoryBean} that returns the {@code Translet} for the current request.
 * It should be declared as a {@code request} or {@code prototype} bean because it is intended
 * to use the value that the current {@code Translet} has.
 *
 * <p>Created: 2017. 10. 22.</p>
 */
public class CurrentTransletFactoryBean implements CurrentActivityAware, FactoryBean<Translet> {

    private String attributeName;

    private Translet translet;

    /**
     * Returns whether the current {@code Translet} is registered as an attribute
     * in the request scope.
     *
     * @return true if the current {@code Translet} is registered as an attribute
     *      in the request scope; false otherwise
     */
    public boolean isAttributable() {
        return (attributeName != null);
    }

    /**
     * Returns the attribute name of the current {@code Translet} specified to register
     * in the request scope.
     *
     * @return the attribute name
     */
    public String getAttributeName() {
        return attributeName;
    }

    /**
     * Specifies the attribute name for registering the current {@code Translet} as an attribute
     * in the request scope.
     *
     * @param attributeName the attribute name of the current {@code Translet} to be registered
     *                      in the request scope.
     */
    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    @Override
    public void setCurrentActivity(Activity activity) {
        translet = activity.getTranslet();

        if (attributeName != null) {
            translet.setAttribute(attributeName, translet);
        }
    }

    @Override
    public Translet getObject() throws Exception {
        return translet;
    }

}
