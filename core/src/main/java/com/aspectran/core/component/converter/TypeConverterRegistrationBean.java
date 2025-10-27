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

import com.aspectran.core.component.bean.aware.ActivityContextAware;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.utils.Assert;
import com.aspectran.utils.annotation.jsr305.NonNull;

/**
 * A helper bean that implements {@link TypeConverterRegistration} to allow other beans
 * to register {@link TypeConverter}s.
 *
 * <p>This bean is {@link ActivityContextAware}, so it automatically gains access to the
 * {@link TypeConverterRegistry} and acts as a bridge for registration.
 *
 * <p>Created: 2025-10-27</p>
 */
public class TypeConverterRegistrationBean implements ActivityContextAware, TypeConverterRegistration {

    private TypeConverterRegistry registry;

    @Override
    public void setActivityContext(@NonNull ActivityContext context) {
        registry = context.getTypeConverterRegistry();
    }

    @Override
    public void register(Class<?> type, TypeConverter<?> converter) {
        Assert.notNull(type, "type must not be null");
        Assert.notNull(converter, "converter must not be null");
        registry.register(type, converter);
    }

}
