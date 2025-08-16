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
package com.aspectran.core.component.bean.aware;

import com.aspectran.core.context.env.Environment;

/**
 * Interface to be implemented by beans that wish to be notified of the
 * {@link Environment} in which they run. The container will call
 * {@link #setEnvironment(Environment)} during initialization.
 */
public interface EnvironmentAware extends Aware {

    /**
     * Set the {@link Environment} that this bean runs in.
     * @param environment the current environment
     */
    void setEnvironment(Environment environment);

}
