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
 * The Class AbstractResponseAdapter.
 *
 * @since 2011. 3. 13.
 */
public abstract class AbstractResponseAdapter implements ResponseAdapter {

    private final Object adaptee;

    /**
     * Instantiates a new AbstractResponseAdapter.
     * @param adaptee the adaptee object
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
