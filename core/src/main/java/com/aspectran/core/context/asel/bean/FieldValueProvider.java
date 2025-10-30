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
import com.aspectran.utils.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * A {@link ValueProvider} implementation that resolves a value from a bean's field.
 */
public class FieldValueProvider implements ValueProvider {

    private final Field field;

    public FieldValueProvider(Field field) {
        this.field = field;
    }

    @Override
    public Object evaluate(Activity activity) {
        if (Modifier.isStatic(field.getModifiers())) {
            return ReflectionUtils.getField(field, null);
        } else {
            Object target = activity.getBean(field.getDeclaringClass());
            return ReflectionUtils.getField(field, target);
        }
    }

    @Override
    public Class<?> getDependentBeanType() {
        return field.getDeclaringClass();
    }

    @Override
    public boolean isRequiresBeanInstance() {
        return !Modifier.isStatic(field.getModifiers());
    }

    public Field getField() {
        return field;
    }

}
