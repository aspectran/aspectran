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
package com.aspectran.core.component.bean.event;

import com.aspectran.core.component.bean.BeanRegistry;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.builder.ActivityContextBuilder;
import com.aspectran.core.context.builder.ActivityContextBuilderException;
import com.aspectran.core.context.builder.HybridActivityContextBuilder;
import com.aspectran.utils.ResourceUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test case for beans.
 *
 * <p>Created: 2016. 3. 26.</p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BeanEventBusTest {

    private ActivityContextBuilder builder;

    private ActivityContext context;

    private BeanRegistry beanRegistry;

    @BeforeAll
    void ready() throws IOException, ActivityContextBuilderException {
        File baseDir = ResourceUtils.getResourceAsFile(".");

        builder = new HybridActivityContextBuilder();
        builder.setBasePath(baseDir.getCanonicalPath());
        builder.setDebugMode(true);
        builder.setActiveProfiles("dev", "local");
        builder.setBasePackages("com.aspectran.core.component.bean.event");
        context = builder.build();
        beanRegistry = context.getBeanRegistry();
    }

    @AfterAll
    void finish() {
        if (builder != null) {
            builder.destroy();
        }
    }

    @Test
    void testEventBus() {
        SamplePublisher publisher = beanRegistry.getBean(SamplePublisher.class);
        SampleListener listener = beanRegistry.getBean(SampleListener.class);

        assertNotNull(publisher);
        assertNotNull(listener);

        listener.clear();
        assertEquals(0, listener.getReceivedEvents().size());

        String message1 = "Hello, Aspectran Event Bus!";
        publisher.publish(message1);

        assertEquals(1, listener.getReceivedEvents().size());
        assertEquals(message1, listener.getReceivedEvents().getFirst().getMessage());

        String message2 = "This is the second event.";
        publisher.publish(message2);

        assertEquals(2, listener.getReceivedEvents().size());
        assertEquals(message2, listener.getReceivedEvents().get(1).getMessage());
    }

}
