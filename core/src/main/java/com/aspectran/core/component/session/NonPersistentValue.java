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
import org.jspecify.annotations.NonNull;

/**
 * A utility class that wraps an object implementing the {@link NonPersistent} interface.
 *
 * <p>This wrapper ensures that the wrapped object is not persisted when the session
 * data is saved to a {@link SessionStore}. It provides a convenient way to handle
 * transient session attributes without directly modifying the session attribute map.</p>
 *
 * <p>Created: 2024-12-31</p>
 */
public class NonPersistentValue implements NonPersistent {

    private final Object value;

    /**
     * Creates a new NonPersistentValue instance wrapping the given value.
     * @param value the object to wrap
     */
    public NonPersistentValue(Object value) {
        this.value = value;
    }

    /**
     * Returns the wrapped object.
     * @return the wrapped object
     */
    public Object getValue() {
        return value;
    }

    /**
     * Unwraps a value if it is an instance of {@link NonPersistentValue}.
     * If the provided value is not a {@link NonPersistentValue}, it is returned as is.
     * @param value the object to unwrap
     * @param <T> the expected type of the unwrapped value
     * @return the unwrapped object, or the original object if not wrapped
     */
    @SuppressWarnings("unchecked")
    public static <T> T unwrap(T value) {
        if (value instanceof NonPersistentValue nonPersistentValue) {
            return (T)nonPersistentValue.getValue();
        } else {
            return value;
        }
    }

    /**
     * A static factory method to wrap a value in a {@link NonPersistentValue}.
     * @param value the object to wrap
     * @return a new {@link NonPersistentValue} instance
     */
    @NonNull
    public static NonPersistentValue wrap(Object value) {
        Assert.notNull(value, "value must not be null");
        return new NonPersistentValue(value);
    }

}
