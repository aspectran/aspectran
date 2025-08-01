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
package com.aspectran.core.component.session;

import com.aspectran.utils.Assert;
import com.aspectran.utils.annotation.jsr305.NonNull;

/**
 * <p>Created: 2024-12-31</p>
 */
public class NonPersistentValue implements NonPersistent {

    private final Object value;

    public NonPersistentValue(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    @SuppressWarnings("unchecked")
    public static <T> T unwrap(T value) {
        if (value instanceof NonPersistentValue nonPersistentValue) {
            return (T)nonPersistentValue.getValue();
        } else {
            return value;
        }
    }

    @NonNull
    public static NonPersistentValue wrap(Object value) {
        Assert.notNull(value, "value must not be null");
        return new NonPersistentValue(value);
    }

}
