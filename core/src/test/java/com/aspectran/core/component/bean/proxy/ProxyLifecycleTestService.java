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
package com.aspectran.core.component.bean.proxy;

import com.aspectran.core.component.bean.annotation.Advisable;
import com.aspectran.core.component.bean.annotation.Bean;
import com.aspectran.core.component.bean.annotation.Component;

import java.util.List;

/**
 * Service bean used to test AOP lifecycle order in {@link AbstractBeanProxy}.
 *
 * <p>Created: 2026. 10. 18.</p>
 */
@Component
@Bean
public class ProxyLifecycleTestService {

    @Advisable
    public String doSuccess(List<String> events) {
        events.add("method:success");
        return "success";
    }

    @Advisable
    public String doFailure(List<String> events) {
        events.add("method:failure");
        throw new IllegalStateException("Intentional Error");
    }

}
