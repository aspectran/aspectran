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
import com.aspectran.core.component.bean.NoSuchBeanException;
import com.aspectran.core.component.bean.NoUniqueBeanException;
import com.aspectran.utils.BeanTypeUtils;
import com.aspectran.utils.BeanUtils;

import java.lang.reflect.InvocationTargetException;

/**
 * A {@link ValueProvider} implementation that resolves a bean instance or a static
 * property from a class.
 */
public class ClassValueProvider implements ValueProvider {

    private final Class<?> beanClass;

    private final String getterName;

    public ClassValueProvider(Class<?> beanClass, String getterName) {
        this.beanClass = beanClass;
        this.getterName = getterName;
    }

    @Override
    public Object evaluate(Activity activity) throws Exception {
        Object value;
        if (getterName != null && beanClass.isEnum()) {
            for (Object en : beanClass.getEnumConstants()) {
                if (getterName.equals(en.toString())) {
                    return en;
                }
            }
        }
        try {
            value = activity.getBean(beanClass);
        } catch (NoSuchBeanException | NoUniqueBeanException e) {
            if (getterName != null) {
                try {
                    // Fallback to static property access
                    return BeanTypeUtils.getProperty(beanClass, getterName);
                } catch (InvocationTargetException e2) {
                    // ignore and fall through to throw the original bean exception
                }
            }
            throw e;
        }
        // If bean is found, access instance property if getterName is provided
        if (value != null && getterName != null) {
            return BeanUtils.getProperty(value, getterName);
        }
        return value;
    }

    @Override
    public Class<?> getDependentBeanType() {
        return beanClass;
    }

    @Override
    public boolean isRequiresBeanInstance() {
        return true;
    }

    public Class<?> getBeanClass() {
        return beanClass;
    }

}
