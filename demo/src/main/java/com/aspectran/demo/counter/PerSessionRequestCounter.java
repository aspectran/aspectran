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
package com.aspectran.demo.counter;

import com.aspectran.core.component.bean.annotation.After;
import com.aspectran.core.component.bean.annotation.Aspect;
import com.aspectran.core.component.bean.annotation.Bean;
import com.aspectran.core.component.bean.annotation.Before;
import com.aspectran.core.component.bean.annotation.Component;
import com.aspectran.core.component.bean.annotation.Scope;
import com.aspectran.core.context.rule.type.ScopeType;
import com.aspectran.core.util.ToStringBuilder;
import com.aspectran.core.util.logging.Logger;
import com.aspectran.core.util.logging.LoggerFactory;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Bean
@Scope(ScopeType.SESSION)
@Aspect("perSessionRequestCounter")
public class PerSessionRequestCounter implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(PerSessionRequestCounter.class);

    private static final long serialVersionUID = -7254733724811233759L;

    private final AtomicInteger requests = new AtomicInteger();

    private final AtomicLong startTime = new AtomicLong();

    private final AtomicLong stopTime = new AtomicLong();

    @Before
    public void before() {
        requests.incrementAndGet();
        startTime.set(System.currentTimeMillis());
    }

    @After
    public void after(PerSessionRequestCounter perSessionRequestCounter) {
        stopTime.set(System.currentTimeMillis());

        if (logger.isDebugEnabled()) {
            ToStringBuilder tsb = new ToStringBuilder("PerSessionRequestCounter");
            tsb.append("requests", perSessionRequestCounter.getRequests());
            tsb.append("start", perSessionRequestCounter.getStartTime());
            tsb.append("stop", perSessionRequestCounter.getStopTime());
            logger.debug(tsb.toString());
        }
    }

    public int getRequests() {
        return requests.get();
    }

    public long getStartTime() {
        return startTime.get();
    }

    public long getStopTime() {
        return stopTime.get();
    }

}
