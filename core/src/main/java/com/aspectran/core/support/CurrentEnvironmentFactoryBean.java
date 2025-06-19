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
package com.aspectran.core.support;

import com.aspectran.core.component.bean.ablility.FactoryBean;
import com.aspectran.core.component.bean.aware.EnvironmentAware;
import com.aspectran.core.context.env.Environment;
import com.aspectran.utils.Assert;

/**
 * {@link CurrentEnvironmentFactoryBean} that returns the {@link Environment}.
 *
 * <p>Created: 2019. 12. 18.</p>
 *
 * @since 6.6.0
 */
public class CurrentEnvironmentFactoryBean implements EnvironmentAware, FactoryBean<Environment> {

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        Assert.state(this.environment == null, "Environment is already set");
        this.environment = environment;
    }

    @Override
    public Environment getObject() throws Exception {
        return environment;
    }

}
