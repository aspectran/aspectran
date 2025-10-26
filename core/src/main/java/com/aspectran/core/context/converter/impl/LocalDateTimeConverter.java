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

import com.aspectran.core.activity.Activity;
import com.aspectran.utils.StringifyContext;

import java.lang.annotation.Annotation;
import java.time.LocalDateTime;

/**
 * Converts a String to a {@link java.time.LocalDateTime}.
 * <p>This converter supports custom date/time formats using the {@link com.aspectran.core.component.bean.annotation.Format}
 * annotation on the target parameter or property.</p>
 *
 * <p>Created: 2025. 10. 26.</p>
 */
public class LocalDateTimeConverter extends AbstractDateTimeConverter<LocalDateTime> {

    @Override
    public LocalDateTime convert(String value, Annotation[] annotations, Activity activity) throws Exception {
        if (value == null) {
            return null;
        }
        String format = findFormat(annotations);
        StringifyContext stringifyContext = activity.getStringifyContext();
        return stringifyContext.toLocalDateTime(value, format);
    }

}
