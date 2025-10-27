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
package com.aspectran.core.component.converter.impl;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.component.converter.TypeConverter;

import java.lang.annotation.Annotation;

/**
 * Converts a String to a {@link Double}.
 *
 * <p>Created: 2025. 10. 26.</p>
 */
public class DoubleConverter implements TypeConverter<Double> {

    @Override
    public Double convert(String value, Annotation[] annotations, Activity activity) {
        return (value != null ? Double.valueOf(value) : null);
    }

}
