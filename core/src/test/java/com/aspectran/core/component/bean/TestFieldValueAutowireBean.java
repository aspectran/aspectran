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

import com.aspectran.core.component.bean.annotation.Value;

public class TestFieldValueAutowireBean {

    @Value("#{properties^property1}")
    public String property1;

    @Value("#{properties^property2}")
    public String property2;

    @Value("#{properties^property3}")
    public String property3;

    @Value("%{classpath:test.properties^property4}")
    public String property4;

    @Value("'property5'")
    public String property5;

    @Value("(1 + 2 + 3) * 5")
    public Integer property6;

    @Value("#{properties^property1} + \"/\" + #{properties^property2} + \"/\" + #{properties^property3}")
    public String property7;

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

    public String getProperty5() {
        return property5;
    }

    public Integer getProperty6() {
        return property6;
    }

    public String getProperty7() {
        return property7;
    }

}
