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
package com.aspectran.core.component.bean;

import com.aspectran.core.component.bean.annotation.Autowired;
import com.aspectran.core.component.bean.annotation.Qualifier;

/**
 * <p>Created: 2017. 11. 29.</p>
 */
public class TestFieldAutowireBean {

    @Autowired
    @Qualifier("bean.TestFieldValueAutowireBean")
    private TestFieldValueAutowireBean bean1;

    @Autowired(required = false)
    @Qualifier("bean.TestFieldValueAutowireBean3")
    private TestFieldValueAutowireBean bean2;

    public TestFieldValueAutowireBean getBean1() {
        return bean1;
    }
    public TestFieldValueAutowireBean getBean2() {
        return bean2;
    }

}
