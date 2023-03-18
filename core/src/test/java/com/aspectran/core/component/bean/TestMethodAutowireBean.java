/*
 * Copyright (c) 2008-2023 The Aspectran Project
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
import com.aspectran.core.component.bean.annotation.Value;

/**
 * <p>Created: 2017. 11. 29.</p>
 */
public class TestMethodAutowireBean {

    private TestFieldValueAutowireBean bean1;

    private TestFieldValueAutowireBean bean2;

    private int number;

    @Autowired
    @Qualifier("bean.TestFieldValueAutowireBean")
    public void setBean1(
            TestFieldValueAutowireBean bean
    ) {
        this.bean1 = bean;
    }

    public TestFieldValueAutowireBean getBean1() {
        return bean1;
    }

    @Autowired(required = false)
    public void setBean2(
            @Qualifier("bean.TestFieldValueAutowireBean3-FOR-TEST") TestFieldValueAutowireBean bean
    ) {
        this.bean2 = bean;
    }

    public TestFieldValueAutowireBean getBean2() {
        return bean2;
    }

    @Autowired
    public void setNumber(@Value("123 + 100") int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

}
