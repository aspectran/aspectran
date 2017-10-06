/*
 * Copyright 2008-2017 Juho Jeong
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
package com.aspectran.scheduler;

import com.aspectran.core.context.builder.config.AspectranConfig;
import com.aspectran.embed.service.EmbeddedAspectranService;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * <p>Created: 2016. 9. 7.</p>
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AspectranSchedulerTest {

    private EmbeddedAspectranService aspectranService;

    @Before
    public void ready() throws Exception {
        AspectranConfig aspectranConfig = new AspectranConfig();
        aspectranConfig.updateRootConfigLocation("classpath:config/scheduler/scheduler-config.xml");
        aspectranConfig.updateSchedulerConfig(0, true, true);

        aspectranService = EmbeddedAspectranService.create(aspectranConfig);
        aspectranService.start();
    }

    @Test
    public void dummyTest() throws InterruptedException {
        Thread.sleep(3000);
    }

    @After
    public void finish() {
        if (aspectranService != null) {
            aspectranService.stop();
        }
    }

}