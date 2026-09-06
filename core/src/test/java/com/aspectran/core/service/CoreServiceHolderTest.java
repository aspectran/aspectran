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
package com.aspectran.core.service;

import com.aspectran.core.context.ActivityContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link CoreServiceHolder}.
 */
class CoreServiceHolderTest {

    private final List<CoreService> heldServices = new ArrayList<>();

    @AfterEach
    void tearDown() {
        for (CoreService service : heldServices) {
            try {
                CoreServiceHolder.release(service);
            } catch (Exception ignored) {
            }
        }
        heldServices.clear();
    }

    private ActivityContext createMockActivityContext(String name) {
        return (ActivityContext) Proxy.newProxyInstance(
                ActivityContext.class.getClassLoader(),
                new Class<?>[] { ActivityContext.class },
                (proxy, method, args) -> {
                    if ("equals".equals(method.getName())) {
                        return proxy == args[0];
                    }
                    if ("hashCode".equals(method.getName())) {
                        return System.identityHashCode(proxy);
                    }
                    if ("getName".equals(method.getName())) {
                        return name;
                    }
                    if ("toString".equals(method.getName())) {
                        return "MockActivityContext[" + name + "]";
                    }
                    return null;
                }
        );
    }

    private StubCoreService createService(String name, ClassLoader classLoader, ActivityContext context) {
        StubCoreService service = new StubCoreService(context, classLoader);
        CoreServiceHolder.hold(service);
        heldServices.add(service);
        return service;
    }

    @Test
    void testHoldAndAcquireSingleService() {
        ActivityContext context = createMockActivityContext("context1");
        ClassLoader cl = new URLClassLoader(new URL[0], getClass().getClassLoader());
        StubCoreService service = createService("service1", cl, context);

        CoreServiceHolder.hold(DummyEndpoint.class, service);

        CoreService acquired = CoreServiceHolder.acquire(DummyEndpoint.class);
        assertNotNull(acquired);
        assertEquals(service, acquired);
        assertEquals(context, CoreServiceHolder.findActivityContext(DummyEndpoint.class));
    }

    @Test
    void testHoldMultipleServicesForSameClass() {
        ActivityContext context = createMockActivityContext("sharedContext");
        ClassLoader cl1 = new URLClassLoader(new URL[0], getClass().getClassLoader());
        ClassLoader cl2 = new URLClassLoader(new URL[0], getClass().getClassLoader());

        StubCoreService service1 = createService("service1", cl1, context);
        StubCoreService service2 = createService("service2", cl2, context);

        // Both services hold the same endpoint class
        CoreServiceHolder.hold(DummyEndpoint.class, service1);
        CoreServiceHolder.hold(DummyEndpoint.class, service2);

        // When thread context classloader matches cl2
        ClassLoader originalCl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(cl2);
            assertEquals(service2, CoreServiceHolder.acquire(DummyEndpoint.class));

            Thread.currentThread().setContextClassLoader(cl1);
            assertEquals(service1, CoreServiceHolder.acquire(DummyEndpoint.class));
        } finally {
            Thread.currentThread().setContextClassLoader(originalCl);
        }

        // Release service1; service2 should still remain mapped
        CoreServiceHolder.release(service1);
        assertEquals(service2, CoreServiceHolder.acquire(DummyEndpoint.class));

        // Release service2; mapping should be cleared
        CoreServiceHolder.release(service2);
        // After both are released, acquire(DummyEndpoint.class) falls back to acquire() which is null
        assertNull(CoreServiceHolder.findActivityContext(DummyEndpoint.class));
    }

    @Test
    void testHoldDuplicateServiceForSameClass() {
        ActivityContext context = createMockActivityContext("context1");
        ClassLoader cl = new URLClassLoader(new URL[0], getClass().getClassLoader());
        StubCoreService service = createService("service1", cl, context);

        CoreServiceHolder.hold(DummyEndpoint.class, service);
        // Duplicate hold call should not throw and not duplicate
        CoreServiceHolder.hold(DummyEndpoint.class, service);

        assertEquals(service, CoreServiceHolder.acquire(DummyEndpoint.class));

        CoreServiceHolder.release(service);
        assertNull(CoreServiceHolder.findActivityContext(DummyEndpoint.class));
    }

    @Test
    void testHoldMultipleServicesWithDifferentContexts() {
        ActivityContext context1 = createMockActivityContext("context1");
        ActivityContext context2 = createMockActivityContext("context2");
        ClassLoader cl1 = new URLClassLoader(new URL[0], getClass().getClassLoader());
        ClassLoader cl2 = new URLClassLoader(new URL[0], getClass().getClassLoader());

        StubCoreService service1 = createService("service1", cl1, context1);
        StubCoreService service2 = createService("service2", cl2, context2);

        CoreServiceHolder.hold(DummyEndpoint.class, service1);
        // Holding with another service having different context logs a warning but succeeds
        CoreServiceHolder.hold(DummyEndpoint.class, service2);

        CoreServiceHolder.release(service1);
        assertEquals(service2, CoreServiceHolder.acquire(DummyEndpoint.class));

        CoreServiceHolder.release(service2);
        assertNull(CoreServiceHolder.findActivityContext(DummyEndpoint.class));
    }

    private static class StubCoreService extends DefaultCoreService {
        private final ActivityContext context;
        private final ClassLoader classLoader;

        StubCoreService(ActivityContext context, ClassLoader classLoader) {
            this.context = context;
            this.classLoader = classLoader;
        }

        @Override
        public ActivityContext getActivityContext() {
            return context;
        }

        @Override
        public ClassLoader getServiceClassLoader() {
            return classLoader;
        }
    }

    private static class DummyEndpoint {
    }

}
