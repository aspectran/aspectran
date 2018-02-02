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

import com.aspectran.core.component.bean.annotation.Value;

public class TestFieldValueAutowireBean {

    @Value("#{properties^property1}")
    private String property1;

    @Value("#{properties^property2}")
    private String property2;

    @Value("#{properties^property3}")
    private String property3;

    @Value("%{classpath:test.properties^hello}")
    private String property4;

    public String getProperty1() {
        return property1;
    }

    public String getProperty2() {
        return property2;
    }

    public String getProperty3() {
        return property3;
    }

    public String getProperty4() {
        return property4;
    }

}
