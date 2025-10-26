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
package com.aspectran.core.context.converter.impl;

import com.aspectran.core.component.bean.annotation.Format;
import com.aspectran.core.context.converter.TypeConverter;

import java.lang.annotation.Annotation;

/**
 * Abstract base class for date/time converters that handles the @Format annotation.
 *
 * <p>Created: 2025. 10. 26.</p>
 */
abstract class AbstractDateTimeConverter<T> implements TypeConverter<T> {

    protected String findFormat(Annotation[] annotations) {
        if (annotations != null) {
            for (Annotation annotation : annotations) {
                if (annotation.annotationType() == Format.class) {
                    return ((Format)annotation).value();
                }
            }
        }
        return null;
    }

}
