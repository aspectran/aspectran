/*
 * Copyright (c) 2008-2017 The Aspectran Project
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

import com.aspectran.core.service.AspectranServiceException;
import com.aspectran.embed.service.EmbeddedAspectranService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * <p>Created: 2016. 11. 5.</p>
 */
public class AspectranSimpleAopTest {

    private EmbeddedAspectranService aspectranService;

    @Before
    public void ready() throws Exception {
        String rootConfigLocation = "classpath:config/aop/simple-aop-test-config.xml";
        aspectranService = EmbeddedAspectranService.create(rootConfigLocation);
        aspectranService.start();
    }

    @Test
    public void test1() throws AspectranServiceException {
        aspectranService.translet("aop/test/target1");
    }

    @Test
    public void test2() throws AspectranServiceException {
        aspectranService.translet("aop/test/target2");
    }

    @After
    public void finish() {
        aspectranService.stop();
    }

}
