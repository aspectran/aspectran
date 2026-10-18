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

import com.aspectran.core.activity.Translet;
import com.aspectran.core.component.bean.annotation.After;
import com.aspectran.core.component.bean.annotation.Aspect;
import com.aspectran.core.component.bean.annotation.Before;
import com.aspectran.core.component.bean.annotation.Component;
import com.aspectran.core.component.bean.annotation.ExceptionThrown;
import com.aspectran.core.component.bean.annotation.Finally;
import com.aspectran.core.component.bean.annotation.Joinpoint;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * Aspect used to record advice execution events in {@link AbstractBeanProxy}.
 *
 * <p>Created: 2026. 10. 18.</p>
 */
@Component
@Aspect(
        id = "proxyLifecycleTestAspect",
        order = 1
)
@Joinpoint(
        pointcut = "+: @class:com.aspectran.core.component.bean.proxy.ProxyLifecycleTestService^*"
)
public class ProxyLifecycleTestAspect {

    @Before
    public void before(@NonNull Translet translet) {
        List<String> events = translet.getAttribute("events");
        if (events != null) {
            events.add("before");
        }
    }

    @After
    public void after(@NonNull Translet translet) {
        List<String> events = translet.getAttribute("events");
        if (events != null) {
            events.add("after");
        }
    }

    @ExceptionThrown(IllegalStateException.class)
    public void thrown(@NonNull Translet translet) {
        List<String> events = translet.getAttribute("events");
        if (events != null) {
            events.add("thrown");
        }
    }

    @Finally
    public void onFinally(@NonNull Translet translet) {
        List<String> events = translet.getAttribute("events");
        if (events != null) {
            events.add("finally");
        }
    }

}
