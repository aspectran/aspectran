/*
 * Copyright (c) 2008-2018 The Aspectran Project
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

import com.aspectran.core.component.bean.scope.ApplicationScope;
import com.aspectran.core.util.ToStringBuilder;

/**
 * The Class AbstractApplicationAdapter.
 *
 * @since 2011. 3. 13.
*/
public abstract class AbstractApplicationAdapter implements ApplicationAdapter {

    protected final Object adaptee;

    protected final ApplicationScope scope;

    /**
     * Instantiates a new AbstractApplicationAdapter.
     *
     * @param adaptee the adaptee object
     */
    public AbstractApplicationAdapter(Object adaptee) {
        this.adaptee = adaptee;
        this.scope = new ApplicationScope();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAdaptee() {
        return (T)adaptee;
    }

    @Override
    public ApplicationScope getApplicationScope() {
        return scope;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("adaptee", adaptee);
        return tsb.toString();
    }

}
