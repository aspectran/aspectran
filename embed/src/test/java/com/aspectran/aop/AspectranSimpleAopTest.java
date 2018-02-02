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
package com.aspectran.aop;

import com.aspectran.core.activity.Translet;
import com.aspectran.core.service.AspectranServiceException;
import com.aspectran.embed.service.EmbeddedService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * <p>Created: 2016. 11. 5.</p>
 */
public class AspectranSimpleAopTest {

    private EmbeddedService service;

    @Before
    public void ready() throws Exception {
        String rootConfigLocation = "classpath:config/aop/simple-aop-test-config.xml";
        service = EmbeddedService.create(rootConfigLocation);
        service.start();
    }

    @Test
    public void test1() throws AspectranServiceException {
        Translet translet = service.translet("aop/test/action1");
        SampleAnnotatedAspect sampleAnnotatedAspect = translet.getAspectAdviceBean("aspect02");
        assertEquals(sampleAnnotatedAspect.foo(), "foo");
    }

    @Test
    public void test2() throws AspectranServiceException {
        service.translet("aop/test/action2");
    }

    @After
    public void finish() {
        service.stop();
    }

}
