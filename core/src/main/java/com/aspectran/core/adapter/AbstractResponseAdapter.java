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
package com.aspectran.core.adapter;

/**
 * Abstract base implementation of {@link ResponseAdapter}.
 * <p>This class holds a reference to the underlying native response object
 * (the "adaptee") and provides a type-safe accessor for it. Subclasses must
 * implement all other methods from the {@link ResponseAdapter} interface.
 * </p>
 *
 * @author Juho Jeong
 * @since 2011. 3. 13.
 */
public abstract class AbstractResponseAdapter implements ResponseAdapter {

    /**
     * The underlying, framework-specific response object.
     */
    private final Object adaptee;

    /**
     * Creates a new {@code AbstractResponseAdapter}.
     * @param adaptee the native, framework-specific response object to adapt, may be {@code null}
     */
    public AbstractResponseAdapter(Object adaptee) {
        this.adaptee = adaptee;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAdaptee() {
        return (T)adaptee;
    }

}
