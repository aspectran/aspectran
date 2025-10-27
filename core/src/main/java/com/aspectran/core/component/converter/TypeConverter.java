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
package com.aspectran.core.component.converter;

import com.aspectran.core.activity.Activity;

import java.lang.annotation.Annotation;

/**
 * An interface for converting a value to a specific type.
 *
 * <p>Created: 2025. 10. 26.</p>
 */
public interface TypeConverter<T> {

    /**
     * Converts the given value to the specified type.
     * @param value the value to convert, typically a String or String[]
     * @param annotations the annotations on the target parameter or property,
     *                    which can be used to influence the conversion
     * @param activity the current activity
     * @return the converted object
     * @throws Exception if the conversion fails
     */
    T convert(String value, Annotation[] annotations, Activity activity) throws Exception;

}
