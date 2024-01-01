/*
 * Copyright (c) 2008-2024 The Aspectran Project
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
package com.aspectran.core.support;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.component.bean.ablility.FactoryBean;
import com.aspectran.core.component.bean.annotation.AvoidAdvice;
import com.aspectran.core.component.bean.aware.ApplicationAdapterAware;

/**
 * {@code BasePathFactoryBean} that returns the base path where the root application is running.
 * This can be used to reference the base path by declaring it as a bean in the Aspectran
 * configuration metadata.
 *
 * <p>Created: 2017. 1. 29.</p>
 */
@AvoidAdvice
public class BasePathFactoryBean implements ApplicationAdapterAware, FactoryBean<String> {

    private String basePath;

    @Override
    public void setApplicationAdapter(ApplicationAdapter applicationAdapter) {
        if (basePath != null) {
            throw new UnsupportedOperationException();
        }
        basePath = applicationAdapter.getBasePath();
    }

    @Override
    public String getObject() {
        return basePath;
    }

}
