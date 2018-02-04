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
package com.aspectran.core.component.bean.ablility;

import com.aspectran.core.activity.Translet;

/**
 * The Interface InitializableTransletBean.
 * 
 * @since 2015. 3. 30.
 */
public interface InitializableTransletBean {

    /**
     * Invoke on initialization after it has set all bean properties supplied.
     *
     * @param translet the translet
     * @throws Exception if initialization fails
     */
    void initialize(Translet translet) throws Exception;

}
