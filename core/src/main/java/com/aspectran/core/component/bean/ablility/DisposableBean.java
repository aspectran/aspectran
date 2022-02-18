/*
 * Copyright (c) 2008-2022 The Aspectran Project
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
package com.aspectran.core.component.bean.ablility;

import com.aspectran.core.component.bean.annotation.AvoidAdvice;

/**
 * The Interface DisposableBean.
 * 
 * @since 2011. 2. 20.
 */
public interface DisposableBean {

    /**
     * A Dispose implementation that calls the destroy() method.
     * @throws Exception if destruction fails
     */
    @AvoidAdvice
    void destroy() throws Exception;

}
