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
import com.aspectran.test.AspectranTest;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test case for beans.
 *
 * <p>Created: 2016. 3. 26.</p>
 */
@AspectranTest(
    basePackages = "com.aspectran.core.component.bean.event",
    async = true
)
class BeanEventBusTest {

    @Test
    void testEventBus(@NonNull ActivityContext context) {
        BeanRegistry beanRegistry = context.getBeanRegistry();
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
